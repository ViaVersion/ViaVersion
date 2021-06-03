/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.protocols.protocol1_17to1_16_4;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_17Types;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.rewriter.EntityRewriter;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.ClientboundPackets1_16_2;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.ServerboundPackets1_16_2;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.metadata.MetadataRewriter1_17To1_16_4;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.packets.EntityPackets;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.packets.InventoryPackets;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.packets.WorldPackets;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.storage.InventoryAcknowledgements;
import com.viaversion.viaversion.rewriter.RegistryType;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;

public class Protocol1_17To1_16_4 extends AbstractProtocol<ClientboundPackets1_16_2, ClientboundPackets1_17, ServerboundPackets1_16_2, ServerboundPackets1_17> {

    public static final MappingData MAPPINGS = new MappingDataBase("1.16.2", "1.17", true);
    private static final String[] NEW_GAME_EVENT_TAGS = {"minecraft:ignore_vibrations_sneaking", "minecraft:vibrations"};
    private final EntityRewriter metadataRewriter = new MetadataRewriter1_17To1_16_4(this);
    private TagRewriter tagRewriter;

    public Protocol1_17To1_16_4() {
        super(ClientboundPackets1_16_2.class, ClientboundPackets1_17.class, ServerboundPackets1_16_2.class, ServerboundPackets1_17.class);
    }

    @Override
    protected void registerPackets() {
        metadataRewriter.register();

        EntityPackets.register(this);
        InventoryPackets.register(this);
        WorldPackets.register(this);

        tagRewriter = new TagRewriter(this);
        registerClientbound(ClientboundPackets1_16_2.TAGS, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    // Tags are now generically written with resource location - 5 different Vanilla types
                    wrapper.write(Type.VAR_INT, 5);
                    for (RegistryType type : RegistryType.getValues()) {
                        // Prefix with resource location
                        wrapper.write(Type.STRING, type.getResourceLocation());

                        // Id conversion
                        tagRewriter.handle(wrapper, tagRewriter.getRewriter(type), tagRewriter.getNewTags(type));

                        // Stop iterating after entity types
                        if (type == RegistryType.ENTITY) {
                            break;
                        }
                    }

                    // New Game Event tags type
                    wrapper.write(Type.STRING, RegistryType.GAME_EVENT.getResourceLocation());
                    wrapper.write(Type.VAR_INT, NEW_GAME_EVENT_TAGS.length);
                    for (String tag : NEW_GAME_EVENT_TAGS) {
                        wrapper.write(Type.STRING, tag);
                        wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[0]);
                    }
                });
            }
        });

        new StatisticsRewriter(this).register(ClientboundPackets1_16_2.STATISTICS);

        SoundRewriter soundRewriter = new SoundRewriter(this);
        soundRewriter.registerSound(ClientboundPackets1_16_2.SOUND);
        soundRewriter.registerSound(ClientboundPackets1_16_2.ENTITY_SOUND);

        registerClientbound(ClientboundPackets1_16_2.RESOURCE_PACK, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    wrapper.passthrough(Type.STRING);
                    wrapper.passthrough(Type.STRING);
                    wrapper.write(Type.BOOLEAN, Via.getConfig().isForcedUse1_17ResourcePack()); // Required
                    wrapper.write(Type.OPTIONAL_COMPONENT, null); // Prompt message
                });
            }
        });

        registerClientbound(ClientboundPackets1_16_2.MAP_DATA, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    wrapper.passthrough(Type.VAR_INT);
                    wrapper.passthrough(Type.BYTE);
                    wrapper.read(Type.BOOLEAN); // Tracking position removed
                    wrapper.passthrough(Type.BOOLEAN);

                    int size = wrapper.read(Type.VAR_INT);
                    // Write whether markers exists or not
                    if (size != 0) {
                        wrapper.write(Type.BOOLEAN, true);
                        wrapper.write(Type.VAR_INT, size);
                    } else {
                        wrapper.write(Type.BOOLEAN, false);
                    }
                });
            }
        });

        registerClientbound(ClientboundPackets1_16_2.TITLE, null, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    // Title packet actions have been split into individual packets (the content hasn't changed)
                    int type = wrapper.read(Type.VAR_INT);
                    ClientboundPacketType packetType;
                    switch (type) {
                        case 0:
                            packetType = ClientboundPackets1_17.TITLE_TEXT;
                            break;
                        case 1:
                            packetType = ClientboundPackets1_17.TITLE_SUBTITLE;
                            break;
                        case 2:
                            packetType = ClientboundPackets1_17.ACTIONBAR;
                            break;
                        case 3:
                            packetType = ClientboundPackets1_17.TITLE_TIMES;
                            break;
                        case 4:
                            packetType = ClientboundPackets1_17.CLEAR_TITLES;
                            wrapper.write(Type.BOOLEAN, false); // Reset times
                            break;
                        case 5:
                            packetType = ClientboundPackets1_17.CLEAR_TITLES;
                            wrapper.write(Type.BOOLEAN, true); // Reset times
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid title type received: " + type);
                    }

                    wrapper.setId(packetType.getId());
                });
            }
        });

        registerClientbound(ClientboundPackets1_16_2.EXPLOSION, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.FLOAT); // X
                map(Type.FLOAT); // Y
                map(Type.FLOAT); // Z
                map(Type.FLOAT); // Strength
                handler(wrapper -> {
                    // Collection length is now a var int
                    wrapper.write(Type.VAR_INT, wrapper.read(Type.INT));
                });
            }
        });

        registerClientbound(ClientboundPackets1_16_2.SPAWN_POSITION, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION1_14);
                handler(wrapper -> {
                    // Angle (which Mojang just forgot to write to the buffer, lol)
                    wrapper.write(Type.FLOAT, 0f);
                });
            }
        });

        registerServerbound(ServerboundPackets1_17.CLIENT_SETTINGS, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // Locale
                map(Type.BYTE); // View distance
                map(Type.VAR_INT); // Chat mode
                map(Type.BOOLEAN); // Chat colors
                map(Type.UNSIGNED_BYTE); // Chat flags
                map(Type.VAR_INT); // Main hand
                handler(wrapper -> {
                    wrapper.read(Type.BOOLEAN); // Text filtering
                });
            }
        });
    }

    @Override
    protected void onMappingDataLoaded() {
        tagRewriter.addEmptyTags(RegistryType.ITEM, "minecraft:candles", "minecraft:ignored_by_piglin_babies", "minecraft:piglin_food", "minecraft:freeze_immune_wearables",
                "minecraft:axolotl_tempt_items", "minecraft:occludes_vibration_signals",
                "minecraft:diamond_ores", "minecraft:iron_ores", "minecraft:lapis_ores", "minecraft:redstone_ores",
                "minecraft:coal_ores", "minecraft:copper_ores", "minecraft:emerald_ores", "minecraft:cluster_max_harvestables");
        tagRewriter.addEmptyTags(RegistryType.BLOCK, "minecraft:crystal_sound_blocks", "minecraft:candle_cakes", "minecraft:candles",
                "minecraft:snow_step_sound_blocks", "minecraft:inside_step_sound_blocks", "minecraft:occludes_vibration_signals", "minecraft:dripstone_replaceable_blocks",
                "minecraft:cave_vines", "minecraft:moss_replaceable", "minecraft:deepslate_ore_replaceables", "minecraft:lush_ground_replaceable",
                "minecraft:diamond_ores", "minecraft:iron_ores", "minecraft:lapis_ores", "minecraft:redstone_ores", "minecraft:stone_ore_replaceables",
                "minecraft:coal_ores", "minecraft:copper_ores", "minecraft:emerald_ores", "minecraft:dirt", "minecraft:snow", "minecraft:small_dripleaf_placeable",
                "minecraft:features_cannot_replace", "minecraft:lava_pool_stone_replaceables", "minecraft:geode_invalid_blocks");
        tagRewriter.addEmptyTags(RegistryType.ENTITY, "minecraft:powder_snow_walkable_mobs", "minecraft:axolotl_always_hostiles", "minecraft:axolotl_tempted_hostiles",
                "minecraft:axolotl_hunt_targets", "minecraft:freeze_hurts_extra_types", "minecraft:freeze_immune_entity_types");

        // Mmmm numbers
        tagRewriter.addTag(RegistryType.BLOCK, "minecraft:mineable/axe", 74, 246, 245, 622, 668, 730, 731, 497, 138, 238, 132, 680, 306, 671, 202, 147, 492, 491, 267, 728, 151, 697, 333, 97, 96, 672, 95, 203, 190, 162, 415, 674, 254, 667, 248, 244, 240, 258, 307, 247, 192, 239, 133, 666, 675, 681, 189, 682, 414, 329, 702, 701, 249, 688, 700, 699, 152, 416, 417, 418, 419, 420, 421, 422, 423, 424, 425, 426, 427, 428, 429, 430, 431, 432, 433, 434, 435, 436, 437, 438, 439, 440, 441, 442, 443, 444, 445, 446, 447, 478, 476, 479, 477, 250, 475, 714, 715, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 131, 130, 410, 411, 413, 412, 40, 52, 45, 58, 35, 47, 46, 53, 39, 51, 44, 57, 37, 49, 42, 55, 38, 50, 43, 56, 36, 48, 41, 54, 692, 693, 694, 695, 683, 684, 685, 686, 13, 14, 15, 16, 17, 18, 704, 705, 19, 20, 21, 22, 23, 24, 155, 156, 157, 158, 159, 160, 722, 723, 165, 166, 167, 168, 169, 170, 724, 725, 308, 309, 310, 311, 312, 313, 718, 719, 161, 485, 486, 487, 488, 489, 720, 721, 191, 483, 484, 480, 481, 482, 710, 711, 174, 175, 176, 177, 178, 179, 708, 709, 452, 453, 454, 455, 456, 457, 706, 707, 146, 274, 275, 276, 375, 376, 716, 717, 226, 224, 227, 225, 222, 223, 712, 713);
        tagRewriter.addTag(RegistryType.BLOCK, "minecraft:mineable/hoe", 504, 689, 390, 576, 729, 698, 65, 66, 62, 59, 60, 64, 63, 61);
        tagRewriter.addTag(RegistryType.BLOCK, "minecraft:mineable/pickaxe", 1, 2, 3, 4, 5, 6, 7, 12, 31, 32, 33, 34, 68, 69, 70, 71, 72, 73, 134, 135, 136, 139, 140, 145, 149, 150, 154, 164, 172, 173, 180, 193, 196, 197, 228, 229, 230, 231, 241, 242, 251, 252, 255, 256, 257, 259, 260, 264, 268, 269, 270, 273, 330, 331, 334, 335, 336, 337, 338, 339, 340, 342, 343, 344, 345, 346, 347, 348, 349, 350, 351, 352, 353, 354, 355, 356, 357, 358, 379, 380, 381, 382, 383, 384, 385, 386, 387, 388, 407, 408, 448, 449, 450, 451, 458, 459, 460, 461, 462, 463, 464, 465, 466, 467, 468, 469, 470, 471, 472, 473, 474, 493, 494, 495, 496, 503, 505, 506, 508, 526, 527, 528, 529, 530, 531, 532, 533, 534, 535, 536, 537, 538, 539, 540, 541, 542, 543, 544, 545, 546, 547, 548, 549, 550, 551, 552, 553, 554, 555, 556, 557, 578, 579, 580, 581, 582, 583, 584, 585, 586, 587, 588, 589, 590, 591, 592, 598, 599, 600, 601, 602, 608, 609, 610, 611, 612, 627, 628, 629, 630, 631, 632, 633, 634, 635, 636, 637, 638, 639, 640, 641, 642, 643, 644, 645, 646, 647, 648, 649, 650, 651, 652, 653, 669, 670, 673, 676, 677, 678, 679, 687, 696, 734, 735, 736, 737, 742, 743, 744, 746, 747, 748, 749, 750, 751, 752, 754, 755, 756, 757, 760, 761, 762, 185, 409, 619, 183, 100, 93, 101, 233, 237, 236, 232, 235, 234, 279, 280, 654, 655, 656, 657, 658, 659, 660, 661, 662, 663, 664, 665, 745, 753, 759, 509, 525, 521, 522, 519, 517, 523, 513, 518, 515, 512, 511, 516, 520, 524, 510, 514, 326, 327, 328, 261, 163, 91, 92, 341);
        tagRewriter.addTag(RegistryType.BLOCK, "minecraft:mineable/shovel", 188, 9, 10, 11, 153, 8, 30, 253, 28, 29, 186, 184, 194, 558, 559, 560, 561, 562, 563, 564, 565, 566, 567, 568, 569, 570, 571, 572, 573, 195);
        tagRewriter.addTag(RegistryType.BLOCK, "minecraft:needs_stone_tool", 135, 32, 69, 68);
        tagRewriter.addTag(RegistryType.BLOCK, "minecraft:needs_diamond_tool", 140, 736, 734, 737, 735);
        tagRewriter.addTag(RegistryType.BLOCK, "minecraft:needs_iron_tool", 150, 149, 269, 273, 134, 31, 180);

        tagRewriter.addTag(RegistryType.BLOCK, "minecraft:cauldrons", 261);
        tagRewriter.addTag(RegistryType.ITEM, "minecraft:fox_food", 948);
    }

    @Override
    public void init(UserConnection user) {
        user.addEntityTracker(this.getClass(), new EntityTrackerBase(user, Entity1_17Types.PLAYER));
        user.put(new InventoryAcknowledgements());
    }

    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    public EntityRewriter getEntityRewriter() {
        return metadataRewriter;
    }
}
