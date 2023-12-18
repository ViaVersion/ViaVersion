/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2023 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_5;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ServerboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundConfigurationPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ServerboundPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.rewriter.BlockItemPacketRewriter1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.rewriter.EntityPacketRewriter1_20_5;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;

public final class Protocol1_20_5To1_20_3 extends AbstractProtocol<ClientboundPackets1_20_3, ClientboundPackets1_20_3, ServerboundPackets1_20_3, ServerboundPackets1_20_3> {

    public static final MappingData MAPPINGS = new MappingDataBase("1.20.3", "1.20.5");
    private final EntityPacketRewriter1_20_5 entityRewriter = new EntityPacketRewriter1_20_5(this);
    private final BlockItemPacketRewriter1_20_5 itemRewriter = new BlockItemPacketRewriter1_20_5(this);

    public Protocol1_20_5To1_20_3() {
        super(ClientboundPackets1_20_3.class, ClientboundPackets1_20_3.class, ServerboundPackets1_20_3.class, ServerboundPackets1_20_3.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        final TagRewriter<ClientboundPackets1_20_3> tagRewriter = new TagRewriter<>(this);
        tagRewriter.registerGeneric(ClientboundPackets1_20_3.TAGS);
        tagRewriter.registerGeneric(State.CONFIGURATION, ClientboundConfigurationPackets1_20_3.UPDATE_TAGS);

        final SoundRewriter<ClientboundPackets1_20_3> soundRewriter = new SoundRewriter<>(this);
        soundRewriter.register1_19_3Sound(ClientboundPackets1_20_3.SOUND);
        soundRewriter.registerSound(ClientboundPackets1_20_3.ENTITY_SOUND);

        new StatisticsRewriter<>(this).register(ClientboundPackets1_20_3.STATISTICS);
    }

    @Override
    protected void onMappingDataLoaded() {
        super.onMappingDataLoaded();

        EntityTypes1_20_5.initialize(this);
    }

    @Override
    public void init(final UserConnection connection) {
        addEntityTracker(connection, new EntityTrackerBase(connection, EntityTypes1_20_5.PLAYER));
    }

    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    public EntityPacketRewriter1_20_5 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public BlockItemPacketRewriter1_20_5 getItemRewriter() {
        return itemRewriter;
    }

    @Override
    protected ClientboundPacketType clientboundFinishConfigurationPacket() {
        return ClientboundConfigurationPackets1_20_3.FINISH_CONFIGURATION;
    }

    @Override
    protected ServerboundPacketType serverboundFinishConfigurationPacket() {
        return ServerboundConfigurationPackets1_20_2.FINISH_CONFIGURATION;
    }
}