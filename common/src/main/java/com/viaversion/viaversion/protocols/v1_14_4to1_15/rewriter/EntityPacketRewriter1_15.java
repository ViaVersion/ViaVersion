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
package com.viaversion.viaversion.protocols.v1_14_4to1_15.rewriter;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_15;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_14;
import com.viaversion.viaversion.protocols.v1_14_3to1_14_4.packet.ClientboundPackets1_14_4;
import com.viaversion.viaversion.protocols.v1_14_4to1_15.Protocol1_14_4To1_15;
import com.viaversion.viaversion.protocols.v1_14_4to1_15.metadata.MetadataRewriter1_15To1_14_4;
import com.viaversion.viaversion.protocols.v1_14_4to1_15.packet.ClientboundPackets1_15;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import java.util.List;

public final class EntityPacketRewriter1_15 {

    public static void register(Protocol1_14_4To1_15 protocol) {
        MetadataRewriter1_15To1_14_4 metadataRewriter = protocol.get(MetadataRewriter1_15To1_14_4.class);

        metadataRewriter.registerTrackerWithData(ClientboundPackets1_14_4.ADD_ENTITY, EntityTypes1_15.FALLING_BLOCK);

        protocol.registerClientbound(ClientboundPackets1_14_4.ADD_MOB, new PacketHandlers() {
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

                handler(metadataRewriter.trackerHandler());
                handler(wrapper -> sendMetadataPacket(wrapper, wrapper.get(Types.VAR_INT, 0), metadataRewriter));
            }
        });

        protocol.registerClientbound(ClientboundPackets1_14_4.ADD_PLAYER, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Entity ID
                map(Types.UUID); // 1 - Player UUID
                map(Types.DOUBLE); // 2 - X
                map(Types.DOUBLE); // 3 - Y
                map(Types.DOUBLE); // 4 - Z
                map(Types.BYTE); // 5 - Yaw
                map(Types.BYTE); // 6 - Pitch

                handler(wrapper -> {
                    int entityId = wrapper.get(Types.VAR_INT, 0);
                    wrapper.user().getEntityTracker(Protocol1_14_4To1_15.class).addEntity(entityId, EntityTypes1_15.PLAYER);

                    sendMetadataPacket(wrapper, entityId, metadataRewriter);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_14_4.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT);
                handler(wrapper -> wrapper.write(Types.LONG, 0L)); // Level Seed
            }
        });

        protocol.registerClientbound(ClientboundPackets1_14_4.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // 0 - Entity ID
                map(Types.UNSIGNED_BYTE); // 1 - Gamemode
                map(Types.INT); // 2 - Dimension
                handler(metadataRewriter.playerTrackerHandler());
                handler(wrapper -> wrapper.write(Types.LONG, 0L)); // Level Seed

                map(Types.UNSIGNED_BYTE); // 3 - Max Players
                map(Types.STRING); // 4 - Level Type
                map(Types.VAR_INT); // 5 - View Distance
                map(Types.BOOLEAN); // 6 - Reduce Debug Info

                handler(wrapper -> wrapper.write(Types.BOOLEAN, !Via.getConfig().is1_15InstantRespawn())); // Show Death Screen
            }
        });

        metadataRewriter.registerMetadataRewriter(ClientboundPackets1_14_4.SET_ENTITY_DATA, Types1_14.METADATA_LIST);
        metadataRewriter.registerRemoveEntities(ClientboundPackets1_14_4.REMOVE_ENTITIES);
    }

    private static void sendMetadataPacket(PacketWrapper wrapper, int entityId, EntityRewriter<?, ?> rewriter) {
        // Meta is no longer included in the spawn packets, but sent separately
        List<Metadata> metadata = wrapper.read(Types1_14.METADATA_LIST);
        if (metadata.isEmpty()) {
            return;
        }

        // Send the spawn packet manually
        wrapper.send(Protocol1_14_4To1_15.class);
        wrapper.cancel();

        // Handle meta
        rewriter.handleMetadata(entityId, metadata, wrapper.user());

        PacketWrapper metadataPacket = PacketWrapper.create(ClientboundPackets1_15.SET_ENTITY_DATA, wrapper.user());
        metadataPacket.write(Types.VAR_INT, entityId);
        metadataPacket.write(Types1_14.METADATA_LIST, metadata);
        metadataPacket.send(Protocol1_14_4To1_15.class);
    }

    public static int getNewEntityId(int oldId) {
        return oldId >= 4 ? oldId + 1 : oldId; // 4 = bee
    }
}
