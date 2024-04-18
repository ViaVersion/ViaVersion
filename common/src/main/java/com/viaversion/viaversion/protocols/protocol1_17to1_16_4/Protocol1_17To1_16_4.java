/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_17;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_17;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.ClientboundPackets1_16_2;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.ServerboundPackets1_16_2;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.packets.EntityPackets;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.packets.InventoryPackets;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.packets.WorldPackets;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.storage.InventoryAcknowledgements;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;

public final class Protocol1_17To1_16_4 extends AbstractProtocol<ClientboundPackets1_16_2, ClientboundPackets1_17, ServerboundPackets1_16_2, ServerboundPackets1_17> {

    public static final MappingData MAPPINGS = new MappingDataBase("1.16.2", "1.17");
    private static final String[] NEW_GAME_EVENT_TAGS = {"minecraft:ignore_vibrations_sneaking", "minecraft:vibrations"};
    private final EntityPackets entityRewriter = new EntityPackets(this);
    private final InventoryPackets itemRewriter = new InventoryPackets(this);
    private final TagRewriter<ClientboundPackets1_16_2> tagRewriter = new TagRewriter<>(this);

    public Protocol1_17To1_16_4() {
        super(ClientboundPackets1_16_2.class, ClientboundPackets1_17.class, ServerboundPackets1_16_2.class, ServerboundPackets1_17.class);
    }

    @Override
    protected void registerPackets() {
        entityRewriter.register();
        itemRewriter.register();

        WorldPackets.register(this);

        registerClientbound(ClientboundPackets1_16_2.TAGS, wrapper -> {
            // Tags are now generically written with resource location - 5 different Vanilla types
            wrapper.write(Type.VAR_INT, 5);
            for (RegistryType type : RegistryType.getValues()) {
                // Prefix with resource location
                wrapper.write(Type.STRING, type.resourceLocation());

                // Id conversion
                tagRewriter.handle(wrapper, tagRewriter.getRewriter(type), tagRewriter.getNewTags(type));

                // Stop iterating after entity types
                if (type == RegistryType.ENTITY) {
                    break;
                }
            }

            // New Game Event tags type
            wrapper.write(Type.STRING, RegistryType.GAME_EVENT.resourceLocation());
            wrapper.write(Type.VAR_INT, NEW_GAME_EVENT_TAGS.length);
            for (String tag : NEW_GAME_EVENT_TAGS) {
                wrapper.write(Type.STRING, tag);
                wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[0]);
            }
        });

        new StatisticsRewriter<>(this).register(ClientboundPackets1_16_2.STATISTICS);

        SoundRewriter<ClientboundPackets1_16_2> soundRewriter = new SoundRewriter<>(this);
        soundRewriter.registerSound(ClientboundPackets1_16_2.SOUND);
        soundRewriter.registerSound(ClientboundPackets1_16_2.ENTITY_SOUND);

        registerClientbound(ClientboundPackets1_16_2.RESOURCE_PACK, wrapper -> {
            wrapper.passthrough(Type.STRING);
            wrapper.passthrough(Type.STRING);
            wrapper.write(Type.BOOLEAN, Via.getConfig().isForcedUse1_17ResourcePack()); // Required
            wrapper.write(Type.OPTIONAL_COMPONENT, Via.getConfig().get1_17ResourcePackPrompt()); // Prompt message
        });

        registerClientbound(ClientboundPackets1_16_2.MAP_DATA, wrapper -> {
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

        registerClientbound(ClientboundPackets1_16_2.TITLE, null, wrapper -> {
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

            wrapper.setPacketType(packetType);
        });

        registerClientbound(ClientboundPackets1_16_2.EXPLOSION, new PacketHandlers() {
            @Override
            public void register() {
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

        registerClientbound(ClientboundPackets1_16_2.SPAWN_POSITION, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.POSITION1_14);
                handler(wrapper -> {
                    // Angle (which Mojang just forgot to write to the buffer, lol)
                    wrapper.write(Type.FLOAT, 0f);
                });
            }
        });

        registerServerbound(ServerboundPackets1_17.CLIENT_SETTINGS, new PacketHandlers() {
            @Override
            public void register() {
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
        super.onMappingDataLoaded();

        tagRewriter.addEmptyTags(RegistryType.ITEM, "minecraft:candles", "minecraft:ignored_by_piglin_babies", "minecraft:piglin_food", "minecraft:freeze_immune_wearables",
                "minecraft:axolotl_tempt_items", "minecraft:occludes_vibration_signals", "minecraft:fox_food",
                "minecraft:diamond_ores", "minecraft:iron_ores", "minecraft:lapis_ores", "minecraft:redstone_ores",
                "minecraft:coal_ores", "minecraft:copper_ores", "minecraft:emerald_ores", "minecraft:cluster_max_harvestables");
        tagRewriter.addEmptyTags(RegistryType.BLOCK, "minecraft:crystal_sound_blocks", "minecraft:candle_cakes", "minecraft:candles",
                "minecraft:snow_step_sound_blocks", "minecraft:inside_step_sound_blocks", "minecraft:occludes_vibration_signals", "minecraft:dripstone_replaceable_blocks",
                "minecraft:cave_vines", "minecraft:moss_replaceable", "minecraft:deepslate_ore_replaceables", "minecraft:lush_ground_replaceable",
                "minecraft:diamond_ores", "minecraft:iron_ores", "minecraft:lapis_ores", "minecraft:redstone_ores", "minecraft:stone_ore_replaceables",
                "minecraft:coal_ores", "minecraft:copper_ores", "minecraft:emerald_ores", "minecraft:snow", "minecraft:small_dripleaf_placeable",
                "minecraft:features_cannot_replace", "minecraft:lava_pool_stone_replaceables", "minecraft:geode_invalid_blocks");
        tagRewriter.addEmptyTags(RegistryType.ENTITY, "minecraft:powder_snow_walkable_mobs", "minecraft:axolotl_always_hostiles", "minecraft:axolotl_tempted_hostiles",
                "minecraft:axolotl_hunt_targets", "minecraft:freeze_hurts_extra_types", "minecraft:freeze_immune_entity_types");

        Types1_17.PARTICLE.filler(this)
                .reader("block", ParticleType.Readers.BLOCK)
                .reader("dust", ParticleType.Readers.DUST)
                .reader("falling_dust", ParticleType.Readers.BLOCK)
                .reader("dust_color_transition", ParticleType.Readers.DUST_TRANSITION)
                .reader("item", ParticleType.Readers.ITEM1_13_2)
                .reader("vibration", ParticleType.Readers.VIBRATION);
    }

    @Override
    public void init(UserConnection user) {
        addEntityTracker(user, new EntityTrackerBase(user, EntityTypes1_17.PLAYER));
        user.put(new InventoryAcknowledgements());
    }

    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    public EntityPackets getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public InventoryPackets getItemRewriter() {
        return itemRewriter;
    }

    @Override
    public TagRewriter<ClientboundPackets1_16_2> getTagRewriter() {
        return tagRewriter;
    }
}
