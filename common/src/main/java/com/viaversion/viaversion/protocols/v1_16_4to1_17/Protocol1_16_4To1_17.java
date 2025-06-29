/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_16_4to1_17;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_17;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_17;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ClientboundPackets1_16_2;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ServerboundPackets1_16_2;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ClientboundPackets1_17;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ServerboundPackets1_17;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.rewriter.ComponentRewriter1_17;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.rewriter.EntityPacketRewriter1_17;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.rewriter.ItemPacketRewriter1_17;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.rewriter.WorldPacketRewriter1_17;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class Protocol1_16_4To1_17 extends AbstractProtocol<ClientboundPackets1_16_2, ClientboundPackets1_17, ServerboundPackets1_16_2, ServerboundPackets1_17> {

    public static final MappingData MAPPINGS = new MappingDataBase("1.16.2", "1.17");
    private final EntityPacketRewriter1_17 entityRewriter = new EntityPacketRewriter1_17(this);
    private final ItemPacketRewriter1_17 itemRewriter = new ItemPacketRewriter1_17(this);
    private final ParticleRewriter<ClientboundPackets1_16_2> particleRewriter = new ParticleRewriter<>(this);
    private final ComponentRewriter1_17 componentRewriter = new ComponentRewriter1_17(this);
    private final TagRewriter<ClientboundPackets1_16_2> tagRewriter = new TagRewriter<>(this);

    public Protocol1_16_4To1_17() {
        super(ClientboundPackets1_16_2.class, ClientboundPackets1_17.class, ServerboundPackets1_16_2.class, ServerboundPackets1_17.class);
    }

    @Override
    protected void registerPackets() {
        entityRewriter.register();
        itemRewriter.register();

        WorldPacketRewriter1_17.register(this);

        registerClientbound(ClientboundPackets1_16_2.UPDATE_TAGS, wrapper -> {
            // Tags are now generically written with resource location - 5 different Vanilla types
            wrapper.write(Types.VAR_INT, 5);
            for (RegistryType type : RegistryType.getValues()) {
                // Prefix with resource location
                wrapper.write(Types.STRING, type.resourceLocation());

                // Id conversion
                tagRewriter.handle(wrapper, type);

                // Stop iterating after entity types
                if (type == RegistryType.ENTITY) {
                    break;
                }
            }

            // New Game Event tags type
            wrapper.write(Types.STRING, RegistryType.GAME_EVENT.resourceLocation());
            tagRewriter.appendNewTags(wrapper, RegistryType.GAME_EVENT);
        });

        new StatisticsRewriter<>(this).register(ClientboundPackets1_16_2.AWARD_STATS);

        componentRewriter.registerComponentPacket(ClientboundPackets1_16_2.CHAT);
        componentRewriter.registerBossEvent(ClientboundPackets1_16_2.BOSS_EVENT);
        componentRewriter.registerComponentPacket(ClientboundPackets1_16_2.DISCONNECT);
        componentRewriter.registerTabList(ClientboundPackets1_16_2.TAB_LIST);
        componentRewriter.registerOpenScreen1_14(ClientboundPackets1_16_2.OPEN_SCREEN);
        componentRewriter.registerPing();

        SoundRewriter<ClientboundPackets1_16_2> soundRewriter = new SoundRewriter<>(this);
        soundRewriter.registerSound(ClientboundPackets1_16_2.SOUND);
        soundRewriter.registerSound(ClientboundPackets1_16_2.SOUND_ENTITY);

        particleRewriter.registerLevelParticles1_13(ClientboundPackets1_16_2.LEVEL_PARTICLES, Types.DOUBLE);

        registerClientbound(ClientboundPackets1_16_2.RESOURCE_PACK, wrapper -> {
            wrapper.passthrough(Types.STRING);
            wrapper.passthrough(Types.STRING);
            wrapper.write(Types.BOOLEAN, Via.getConfig().isForcedUse1_17ResourcePack()); // Required
            wrapper.write(Types.OPTIONAL_COMPONENT, Via.getConfig().get1_17ResourcePackPrompt()); // Prompt message
        });

        registerClientbound(ClientboundPackets1_16_2.MAP_ITEM_DATA, wrapper -> {
            wrapper.passthrough(Types.VAR_INT);
            wrapper.passthrough(Types.BYTE);
            wrapper.read(Types.BOOLEAN); // Tracking position removed
            wrapper.passthrough(Types.BOOLEAN);

            int size = wrapper.read(Types.VAR_INT);
            // Write whether markers exist or not
            if (size != 0) {
                wrapper.write(Types.BOOLEAN, true);
                wrapper.write(Types.VAR_INT, size);
            } else {
                wrapper.write(Types.BOOLEAN, false);
            }
        });

        registerClientbound(ClientboundPackets1_16_2.SET_TITLES, null, wrapper -> {
            // Title packet actions have been split into individual packets (the content hasn't changed)
            int type = wrapper.read(Types.VAR_INT);
            ClientboundPacketType packetType;
            switch (type) {
                case 0 -> packetType = ClientboundPackets1_17.SET_TITLE_TEXT;
                case 1 -> packetType = ClientboundPackets1_17.SET_SUBTITLE_TEXT;
                case 2 -> packetType = ClientboundPackets1_17.SET_ACTION_BAR_TEXT;
                case 3 -> packetType = ClientboundPackets1_17.SET_TITLES_ANIMATION;
                case 4 -> {
                    packetType = ClientboundPackets1_17.CLEAR_TITLES;
                    wrapper.write(Types.BOOLEAN, false); // Reset times
                }
                case 5 -> {
                    packetType = ClientboundPackets1_17.CLEAR_TITLES;
                    wrapper.write(Types.BOOLEAN, true); // Reset times
                }
                default -> throw new IllegalArgumentException("Invalid title type received: " + type);
            }

            if (type < 3) {
                componentRewriter.processText(wrapper.user(), wrapper.passthrough(Types.COMPONENT));
            }
            wrapper.setPacketType(packetType);
        });

        registerClientbound(ClientboundPackets1_16_2.EXPLODE, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.FLOAT); // X
                map(Types.FLOAT); // Y
                map(Types.FLOAT); // Z
                map(Types.FLOAT); // Strength
                handler(wrapper -> wrapper.write(Types.VAR_INT, wrapper.read(Types.INT))); // Collection length is now a var int
            }
        });

        registerClientbound(ClientboundPackets1_16_2.SET_DEFAULT_SPAWN_POSITION, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.BLOCK_POSITION1_14);
                handler(wrapper -> wrapper.write(Types.FLOAT, 0f)); // Angle (which Mojang just forgot to write to the buffer, lol)
            }
        });

        registerServerbound(ServerboundPackets1_17.CLIENT_INFORMATION, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // Locale
                map(Types.BYTE); // View distance
                map(Types.VAR_INT); // Chat mode
                map(Types.BOOLEAN); // Chat colors
                map(Types.UNSIGNED_BYTE); // Chat flags
                map(Types.VAR_INT); // Main hand
                read(Types.BOOLEAN); // Text filtering
            }
        });
    }

    @Override
    protected void onMappingDataLoaded() {
        EntityTypes1_17.initialize(this);
        Types1_17.PARTICLE.filler(this)
            .reader("block", ParticleType.Readers.BLOCK)
            .reader("dust", ParticleType.Readers.DUST)
            .reader("falling_dust", ParticleType.Readers.BLOCK)
            .reader("dust_color_transition", ParticleType.Readers.DUST_TRANSITION)
            .reader("item", ParticleType.Readers.ITEM1_13_2)
            .reader("vibration", ParticleType.Readers.VIBRATION);

        tagRewriter.addEmptyTags(RegistryType.ITEM, "minecraft:axolotl_tempt_items", "minecraft:candles", "minecraft:cluster_max_harvestables",
            "minecraft:copper_ores", "minecraft:freeze_immune_wearables", "minecraft:occludes_vibration_signals");
        tagRewriter.addEmptyTags(RegistryType.BLOCK, "minecraft:candle_cakes", "minecraft:candles", "minecraft:cave_vines", "minecraft:copper_ores",
            "minecraft:crystal_sound_blocks", "minecraft:deepslate_ore_replaceables", "minecraft:dripstone_replaceable_blocks", "minecraft:geode_invalid_blocks",
            "minecraft:lush_ground_replaceable", "minecraft:moss_replaceable", "minecraft:occludes_vibration_signals", "minecraft:small_dripleaf_placeable");
        tagRewriter.addEmptyTags(RegistryType.ENTITY, "minecraft:axolotl_always_hostiles", "minecraft:axolotl_hunt_targets",
            "minecraft:freeze_hurts_extra_types", "minecraft:freeze_immune_entity_types", "minecraft:powder_snow_walkable_mobs");
        tagRewriter.addEmptyTags(RegistryType.GAME_EVENT, "minecraft:ignore_vibrations_sneaking", "minecraft:vibrations");

        super.onMappingDataLoaded();
    }

    @Override
    public void init(UserConnection user) {
        addEntityTracker(user, new EntityTrackerBase(user, EntityTypes1_17.PLAYER));
    }

    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    public EntityPacketRewriter1_17 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public ItemPacketRewriter1_17 getItemRewriter() {
        return itemRewriter;
    }

    @Override
    public ParticleRewriter<ClientboundPackets1_16_2> getParticleRewriter() {
        return particleRewriter;
    }

    @Override
    public @Nullable ComponentRewriter1_17 getComponentRewriter() {
        return componentRewriter;
    }

    @Override
    public TagRewriter<ClientboundPackets1_16_2> getTagRewriter() {
        return tagRewriter;
    }
}
