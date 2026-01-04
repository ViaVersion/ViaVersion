/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_14to1_14_1.rewriter;

import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_14;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_14;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.packet.ClientboundPackets1_14;
import com.viaversion.viaversion.protocols.v1_14to1_14_1.Protocol1_14To1_14_1;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public class EntityPacketRewriter1_14_1 extends EntityRewriter<ClientboundPackets1_14, Protocol1_14To1_14_1> {

    public EntityPacketRewriter1_14_1(Protocol1_14To1_14_1 protocol) {
        super(protocol);
    }

    @Override
    protected void registerPackets() {
        protocol.registerClientbound(ClientboundPackets1_14.ADD_MOB, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Entity ID
                map(Types.UUID); // 1 - Entity UUID
                map(Types.VAR_INT); // 2 - Entity Type
                map(Types.DOUBLE); // 3 - X
                map(Types.DOUBLE); // 4 - Y
                map(Types.DOUBLE); // 5 - Z
                map(Types.BYTE); // 6 - Yaw
                map(Types.BYTE); // 7 - Pitch
                map(Types.BYTE); // 8 - Head Pitch
                map(Types.SHORT); // 9 - Velocity X
                map(Types.SHORT); // 10 - Velocity Y
                map(Types.SHORT); // 11 - Velocity Z
                map(Types1_14.ENTITY_DATA_LIST); // 12 - Entity data

                handler(trackerAndRewriterHandler(Types1_14.ENTITY_DATA_LIST));
            }
        });

        registerRemoveEntities(ClientboundPackets1_14.REMOVE_ENTITIES);

        protocol.registerClientbound(ClientboundPackets1_14.ADD_PLAYER, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Entity ID
                map(Types.UUID); // 1 - Player UUID
                map(Types.DOUBLE); // 2 - X
                map(Types.DOUBLE); // 3 - Y
                map(Types.DOUBLE); // 4 - Z
                map(Types.BYTE); // 5 - Yaw
                map(Types.BYTE); // 6 - Pitch
                map(Types1_14.ENTITY_DATA_LIST); // 7 - Entity data

                handler(trackerAndRewriterHandler(Types1_14.ENTITY_DATA_LIST, EntityTypes1_14.PLAYER));
            }
        });

        registerSetEntityData(ClientboundPackets1_14.SET_ENTITY_DATA, Types1_14.ENTITY_DATA_LIST);
    }

    @Override
    protected void registerRewrites() {
        filter().type(EntityTypes1_14.VILLAGER).addIndex(15);
        filter().type(EntityTypes1_14.WANDERING_TRADER).addIndex(15);
    }

    @Override
    public EntityType typeFromId(int type) {
        return EntityTypes1_14.getTypeFromId(type);
    }
}
