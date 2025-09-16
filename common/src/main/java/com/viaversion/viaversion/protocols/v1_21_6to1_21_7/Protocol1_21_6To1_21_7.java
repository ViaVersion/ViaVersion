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
package com.viaversion.viaversion.protocols.v1_21_6to1_21_7;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.data.version.StructuredDataKeys1_21_5;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_6;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_5;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.type.types.version.Types1_20_5;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.data.item.ItemHasherBase;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundConfigurationPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundPacket1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundConfigurationPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPacket1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_6to1_21_7.rewriter.BlockItemPacketRewriter1_21_7;
import com.viaversion.viaversion.protocols.v1_21_6to1_21_7.rewriter.EntityPacketRewriter1_21_7;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;

import static com.viaversion.viaversion.util.ProtocolUtil.packetTypeMap;

public final class Protocol1_21_6To1_21_7 extends AbstractProtocol<ClientboundPacket1_21_6, ClientboundPacket1_21_6, ServerboundPacket1_21_6, ServerboundPacket1_21_6> {

    public static final MappingData MAPPINGS = new MappingDataBase("1.21.6", "1.21.7");
    private final EntityPacketRewriter1_21_7 entityRewriter = new EntityPacketRewriter1_21_7(this);
    private final BlockItemPacketRewriter1_21_7 itemRewriter = new BlockItemPacketRewriter1_21_7(this);
    private final ParticleRewriter<ClientboundPacket1_21_6> particleRewriter = new ParticleRewriter<>(this);
    private final TagRewriter<ClientboundPacket1_21_6> tagRewriter = new TagRewriter<>(this);

    public Protocol1_21_6To1_21_7() {
        super(ClientboundPacket1_21_6.class, ClientboundPacket1_21_6.class, ServerboundPacket1_21_6.class, ServerboundPacket1_21_6.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        tagRewriter.registerGeneric(ClientboundPackets1_21_6.UPDATE_TAGS);
        tagRewriter.registerGeneric(ClientboundConfigurationPackets1_21_6.UPDATE_TAGS);

        particleRewriter.registerLevelParticles1_21_4(ClientboundPackets1_21_6.LEVEL_PARTICLES);
        particleRewriter.registerExplode1_21_2(ClientboundPackets1_21_6.EXPLODE); // Rewrites the included sound and particles

        final SoundRewriter<ClientboundPacket1_21_6> soundRewriter = new SoundRewriter<>(this);
        soundRewriter.registerSound1_19_3(ClientboundPackets1_21_6.SOUND);
        soundRewriter.registerSound1_19_3(ClientboundPackets1_21_6.SOUND_ENTITY);

        new StatisticsRewriter<>(this).register(ClientboundPackets1_21_6.AWARD_STATS);
    }

    @Override
    public void init(final UserConnection connection) {
        addEntityTracker(connection, new EntityTrackerBase(connection, EntityTypes1_21_6.PLAYER));
        addItemHasher(connection, new ItemHasherBase(this, connection));
    }

    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    public Types1_20_5<StructuredDataKeys1_21_5, EntityDataTypes1_21_5> types() {
        return VersionedTypes.V1_21_6;
    }

    @Override
    public Types1_20_5<StructuredDataKeys1_21_5, EntityDataTypes1_21_5> mappedTypes() {
        return VersionedTypes.V1_21_6;
    }

    @Override
    public TagRewriter<ClientboundPacket1_21_6> getTagRewriter() {
        return tagRewriter;
    }

    @Override
    public EntityPacketRewriter1_21_7 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public BlockItemPacketRewriter1_21_7 getItemRewriter() {
        return itemRewriter;
    }

    @Override
    public ParticleRewriter<ClientboundPacket1_21_6> getParticleRewriter() {
        return particleRewriter;
    }

    @Override
    protected PacketTypesProvider<ClientboundPacket1_21_6, ClientboundPacket1_21_6, ServerboundPacket1_21_6, ServerboundPacket1_21_6> createPacketTypesProvider() {
        return new SimplePacketTypesProvider<>(
            packetTypeMap(unmappedClientboundPacketType, ClientboundPackets1_21_6.class, ClientboundConfigurationPackets1_21_6.class),
            packetTypeMap(mappedClientboundPacketType, ClientboundPackets1_21_6.class, ClientboundConfigurationPackets1_21_6.class),
            packetTypeMap(mappedServerboundPacketType, ServerboundPackets1_21_6.class, ServerboundConfigurationPackets1_21_6.class),
            packetTypeMap(unmappedServerboundPacketType, ServerboundPackets1_21_6.class, ServerboundConfigurationPackets1_21_6.class)
        );
    }
}
