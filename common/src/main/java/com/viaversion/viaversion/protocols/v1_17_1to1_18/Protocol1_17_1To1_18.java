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
package com.viaversion.viaversion.protocols.v1_17_1to1_18;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_17;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_18;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ServerboundPackets1_17;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.packet.ClientboundPackets1_18;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.rewriter.ComponentRewriter1_18;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.rewriter.EntityPacketRewriter1_18;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.rewriter.ItemPacketRewriter1_18;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.rewriter.WorldPacketRewriter1_18;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.storage.ChunkLightStorage;
import com.viaversion.viaversion.protocols.v1_17to1_17_1.packet.ClientboundPackets1_17_1;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.util.ProtocolLogger;

public final class Protocol1_17_1To1_18 extends AbstractProtocol<ClientboundPackets1_17_1, ClientboundPackets1_18, ServerboundPackets1_17, ServerboundPackets1_17> {

    public static final MappingData MAPPINGS = new MappingDataBase("1.17", "1.18");
    public static final ProtocolLogger LOGGER = new ProtocolLogger(Protocol1_17_1To1_18.class);
    private final EntityPacketRewriter1_18 entityRewriter = new EntityPacketRewriter1_18(this);
    private final ItemPacketRewriter1_18 itemRewriter = new ItemPacketRewriter1_18(this);
    private final ParticleRewriter<ClientboundPackets1_17_1> particleRewriter = new ParticleRewriter<>(this);
    private final ComponentRewriter1_18 componentRewriter = new ComponentRewriter1_18(this);
    private final TagRewriter<ClientboundPackets1_17_1> tagRewriter = new TagRewriter<>(this);

    public Protocol1_17_1To1_18() {
        super(ClientboundPackets1_17_1.class, ClientboundPackets1_18.class, ServerboundPackets1_17.class, ServerboundPackets1_17.class);
    }

    @Override
    protected void registerPackets() {
        entityRewriter.register();
        itemRewriter.register();
        WorldPacketRewriter1_18.register(this);

        final SoundRewriter<ClientboundPackets1_17_1> soundRewriter = new SoundRewriter<>(this);
        soundRewriter.registerSound(ClientboundPackets1_17_1.SOUND);
        soundRewriter.registerSound(ClientboundPackets1_17_1.SOUND_ENTITY);

        tagRewriter.registerGeneric(ClientboundPackets1_17_1.UPDATE_TAGS);

        new StatisticsRewriter<>(this).register(ClientboundPackets1_17_1.AWARD_STATS);

        componentRewriter.registerComponentPacket(ClientboundPackets1_17_1.CHAT);
        componentRewriter.registerComponentPacket(ClientboundPackets1_17_1.SET_ACTION_BAR_TEXT);
        componentRewriter.registerComponentPacket(ClientboundPackets1_17_1.SET_TITLE_TEXT);
        componentRewriter.registerComponentPacket(ClientboundPackets1_17_1.SET_SUBTITLE_TEXT);
        componentRewriter.registerBossEvent(ClientboundPackets1_17_1.BOSS_EVENT);
        componentRewriter.registerComponentPacket(ClientboundPackets1_17_1.DISCONNECT);
        componentRewriter.registerTabList(ClientboundPackets1_17_1.TAB_LIST);
        componentRewriter.registerOpenScreen1_14(ClientboundPackets1_17_1.OPEN_SCREEN);
        componentRewriter.registerPlayerCombatKill(ClientboundPackets1_17_1.PLAYER_COMBAT_KILL);
        componentRewriter.registerSetPlayerTeam1_13(ClientboundPackets1_17_1.SET_PLAYER_TEAM);
        componentRewriter.registerPing();

        registerServerbound(ServerboundPackets1_17.CLIENT_INFORMATION, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // Language
                map(Types.BYTE); // View distance
                map(Types.VAR_INT); // Chat visibility
                map(Types.BOOLEAN); // Chat colors
                map(Types.UNSIGNED_BYTE); // Model customization
                map(Types.VAR_INT); // Main hand
                map(Types.BOOLEAN); // Text filtering enabled
                read(Types.BOOLEAN); // Allow listing in server list preview
            }
        });
    }

    @Override
    protected void onMappingDataLoaded() {
        Types1_18.PARTICLE.filler(this)
            .reader("block", ParticleType.Readers.BLOCK)
            .reader("block_marker", ParticleType.Readers.BLOCK)
            .reader("dust", ParticleType.Readers.DUST)
            .reader("falling_dust", ParticleType.Readers.BLOCK)
            .reader("dust_color_transition", ParticleType.Readers.DUST_TRANSITION)
            .reader("item", ParticleType.Readers.ITEM1_13_2)
            .reader("vibration", ParticleType.Readers.VIBRATION);

        tagRewriter.renameTag(RegistryType.BLOCK, "minecraft:lava_pool_stone_replaceables", "minecraft:lava_pool_stone_cannot_replace");

        super.onMappingDataLoaded();
    }

    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    public ProtocolLogger getLogger() {
        return LOGGER;
    }

    @Override
    public void init(final UserConnection connection) {
        addEntityTracker(connection, new EntityTrackerBase(connection, EntityTypes1_17.PLAYER));
        connection.put(new ChunkLightStorage());
    }

    @Override
    public EntityPacketRewriter1_18 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public ItemPacketRewriter1_18 getItemRewriter() {
        return itemRewriter;
    }

    @Override
    public ParticleRewriter<ClientboundPackets1_17_1> getParticleRewriter() {
        return particleRewriter;
    }

    @Override
    public ComponentRewriter1_18 getComponentRewriter() {
        return componentRewriter;
    }

    @Override
    public TagRewriter<ClientboundPackets1_17_1> getTagRewriter() {
        return tagRewriter;
    }
}
