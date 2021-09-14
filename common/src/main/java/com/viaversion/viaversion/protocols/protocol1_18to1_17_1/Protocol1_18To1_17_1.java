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
package com.viaversion.viaversion.protocols.protocol1_18to1_17_1;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_17Types;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.protocol1_17_1to1_17.ClientboundPackets1_17_1;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.ServerboundPackets1_17;
import com.viaversion.viaversion.protocols.protocol1_18to1_17_1.packets.EntityPackets;
import com.viaversion.viaversion.protocols.protocol1_18to1_17_1.packets.WorldPackets;
import com.viaversion.viaversion.protocols.protocol1_18to1_17_1.storage.ChunkLightStorage;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;

public final class Protocol1_18To1_17_1 extends AbstractProtocol<ClientboundPackets1_17_1, ClientboundPackets1_17_1, ServerboundPackets1_17, ServerboundPackets1_17> {

    public static final MappingData MAPPINGS = new MappingDataBase("1.17", "1.18");
    private final EntityRewriter<Protocol1_18To1_17_1> entityRewriter = new EntityPackets(this);

    public Protocol1_18To1_17_1() {
        super(ClientboundPackets1_17_1.class, ClientboundPackets1_17_1.class, ServerboundPackets1_17.class, ServerboundPackets1_17.class);

        final SoundRewriter soundRewriter = new SoundRewriter(this);
        soundRewriter.registerSound(ClientboundPackets1_17_1.SOUND);
        soundRewriter.registerSound(ClientboundPackets1_17_1.NAMED_SOUND);

        registerClientbound(ClientboundPackets1_17_1.BLOCK_ENTITY_DATA, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION1_14);
                map(Type.UNSIGNED_BYTE, Type.VAR_INT);
            }
        });
    }

    @Override
    protected void registerPackets() {
        entityRewriter.register();
        WorldPackets.register(this);
    }

    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    public void init(final UserConnection connection) {
        addEntityTracker(connection, new EntityTrackerBase(connection, Entity1_17Types.PLAYER)); //TODO Entity1_18Types
        connection.put(new ChunkLightStorage());
    }

    @Override
    public EntityRewriter<Protocol1_18To1_17_1> getEntityRewriter() {
        return entityRewriter;
    }
}
