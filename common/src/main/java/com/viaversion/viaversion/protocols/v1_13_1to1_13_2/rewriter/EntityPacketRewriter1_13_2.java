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
package com.viaversion.viaversion.protocols.v1_13_1to1_13_2.rewriter;

import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityDataType;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_13;
import com.viaversion.viaversion.api.type.types.version.Types1_13_2;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_13_1to1_13_2.Protocol1_13_1To1_13_2;

public class EntityPacketRewriter1_13_2 {

    public static void register(Protocol1_13_1To1_13_2 protocol) {
        final PacketHandler dataTypeHandler = wrapper -> {
            for (final EntityData data : wrapper.get(Types1_13_2.ENTITY_DATA_LIST, 0)) {
                final EntityDataType dataType = Types1_13_2.ENTITY_DATA_TYPES.byId(data.dataType().typeId());
                if (dataType == Types1_13_2.ENTITY_DATA_TYPES.particleType) {
                    final Particle particle = data.value();
                    if (particle.id() == 27) {
                        final Item item = particle.<Item>getArgument(0).getValue();
                        particle.set(0, Types.ITEM1_13_2, item);
                    }
                }
                data.setDataType(dataType);
            }
        };

        protocol.registerClientbound(ClientboundPackets1_13.ADD_MOB, new PacketHandlers() {
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
                map(Types1_13.ENTITY_DATA_LIST, Types1_13_2.ENTITY_DATA_LIST); // 12 - Entity data

                handler(dataTypeHandler);
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.ADD_PLAYER, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Entity ID
                map(Types.UUID); // 1 - Player UUID
                map(Types.DOUBLE); // 2 - X
                map(Types.DOUBLE); // 3 - Y
                map(Types.DOUBLE); // 4 - Z
                map(Types.BYTE); // 5 - Yaw
                map(Types.BYTE); // 6 - Pitch
                map(Types1_13.ENTITY_DATA_LIST, Types1_13_2.ENTITY_DATA_LIST); // 7 - Entity data

                handler(dataTypeHandler);
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.SET_ENTITY_DATA, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Entity ID
                map(Types1_13.ENTITY_DATA_LIST, Types1_13_2.ENTITY_DATA_LIST); // 1 - Entity data list

                handler(dataTypeHandler);
            }
        });
    }
}
