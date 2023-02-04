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
package com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.packets;

import com.github.steveice10.opennbt.tag.builtin.ByteTag;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_19_3Types;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ClientboundPackets1_19_3;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.Protocol1_19_4To1_19_3;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public final class EntityPackets extends EntityRewriter<ClientboundPackets1_19_3, Protocol1_19_4To1_19_3> {

    public EntityPackets(final Protocol1_19_4To1_19_3 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        protocol.registerClientbound(ClientboundPackets1_19_3.JOIN_GAME, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // Entity id
                map(Type.BOOLEAN); // Hardcore
                map(Type.UNSIGNED_BYTE); // Gamemode
                map(Type.BYTE); // Previous Gamemode
                map(Type.STRING_ARRAY); // World List
                map(Type.NBT); // Dimension registry
                map(Type.STRING); // Dimension key
                map(Type.STRING); // World
                handler(dimensionDataHandler());
                handler(biomeSizeTracker());
                handler(worldDataTrackerHandlerByKey());
                handler(wrapper -> {
                    final CompoundTag registry = wrapper.get(Type.NBT, 0);
                    final CompoundTag biomeRegistry = registry.get("minecraft:worldgen/biome");
                    final ListTag biomes = biomeRegistry.get("value");
                    for (final Tag biomeTag : biomes) {
                        final CompoundTag biomeData = ((CompoundTag) biomeTag).get("element");
                        final StringTag precipitation = biomeData.get("precipitation");
                        final byte precipitationByte = precipitation.getValue().equals("none") ? (byte) 0 : 1;
                        biomeData.put("has_precipitation", new ByteTag(precipitationByte));
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_3.ENTITY_ANIMATION, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // Entity id
                handler(wrapper -> {
                    final short action = wrapper.read(Type.UNSIGNED_BYTE);
                    if (action != 1) {
                        wrapper.write(Type.UNSIGNED_BYTE, action);
                        return;
                    }

                    wrapper.setPacketType(ClientboundPackets1_19_4.HIT_ANIMATION);
                    wrapper.write(Type.FLOAT, 0F);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_3.RESPAWN, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // Dimension
                map(Type.STRING); // World
                handler(worldDataTrackerHandlerByKey());
            }
        });
    }

    @Override
    public EntityType typeFromId(final int type) {
        return Entity1_19_3Types.getTypeFromId(type);
    }
}