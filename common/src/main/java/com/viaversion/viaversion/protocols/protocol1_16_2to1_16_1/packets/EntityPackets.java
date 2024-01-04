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
package com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_16_2;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_16;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.Protocol1_16_2To1_16_1;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.metadata.MetadataRewriter1_16_2To1_16_1;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.ClientboundPackets1_16;

public class EntityPackets {

    public static void register(Protocol1_16_2To1_16_1 protocol) {
        MetadataRewriter1_16_2To1_16_1 metadataRewriter = protocol.get(MetadataRewriter1_16_2To1_16_1.class);
        metadataRewriter.registerTrackerWithData(ClientboundPackets1_16.SPAWN_ENTITY, EntityTypes1_16_2.FALLING_BLOCK);
        metadataRewriter.registerTracker(ClientboundPackets1_16.SPAWN_MOB);
        metadataRewriter.registerTracker(ClientboundPackets1_16.SPAWN_PLAYER, EntityTypes1_16_2.PLAYER);
        metadataRewriter.registerMetadataRewriter(ClientboundPackets1_16.ENTITY_METADATA, Types1_16.METADATA_LIST);
        metadataRewriter.registerRemoveEntities(ClientboundPackets1_16.DESTROY_ENTITIES);

        protocol.registerClientbound(ClientboundPackets1_16.JOIN_GAME, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // Entity ID
                handler(wrapper -> {
                    short gamemode = wrapper.read(Type.UNSIGNED_BYTE);
                    wrapper.write(Type.BOOLEAN, (gamemode & 0x08) != 0); // Hardcore
                    wrapper.write(Type.BYTE, (byte) (gamemode & ~0x08)); // Gamemode
                });
                map(Type.BYTE); // Previous Gamemode
                map(Type.STRING_ARRAY); // World List
                handler(wrapper -> {
                    // Throw away the old dimension registry, extra conversion would be too hard of a hit
                    wrapper.read(Type.NAMED_COMPOUND_TAG);
                    wrapper.write(Type.NAMED_COMPOUND_TAG, protocol.getMappingData().getDimensionRegistry());

                    // Instead of the dimension's resource key, it now just wants the data directly
                    String dimensionType = wrapper.read(Type.STRING);
                    wrapper.write(Type.NAMED_COMPOUND_TAG, getDimensionData(dimensionType));
                });
                map(Type.STRING); // Dimension
                map(Type.LONG); // Seed
                map(Type.UNSIGNED_BYTE, Type.VAR_INT); // Max players
                // ...
                handler(metadataRewriter.playerTrackerHandler());
            }
        });

        protocol.registerClientbound(ClientboundPackets1_16.RESPAWN, wrapper -> {
            String dimensionType = wrapper.read(Type.STRING);
            wrapper.write(Type.NAMED_COMPOUND_TAG, getDimensionData(dimensionType));
        });
    }

    public static CompoundTag getDimensionData(String dimensionType) {
        CompoundTag tag = Protocol1_16_2To1_16_1.MAPPINGS.getDimensionDataMap().get(dimensionType);
        if (tag == null) {
            Via.getPlatform().getLogger().severe("Could not get dimension data of " + dimensionType);
            throw new NullPointerException("Dimension data for " + dimensionType + " is null!");
        }
        return tag.copy();
    }
}
