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
package com.viaversion.viaversion.protocols.protocol1_9_1_2to1_9_3_4.chunks;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.NumberTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_9_1_2to1_9_3_4.Protocol1_9_1_2To1_9_3_4;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockEntity {
    private static final Map<String, Integer> types = new HashMap<>();

    static {
        types.put("MobSpawner", 1);
        types.put("Control", 2);
        types.put("Beacon", 3);
        types.put("Skull", 4);
        types.put("FlowerPot", 5);
        types.put("Banner", 6);
        types.put("UNKNOWN", 7);
        types.put("EndGateway", 8);
        types.put("Sign", 9);
    }

    public static void handle(List<CompoundTag> tags, UserConnection connection) {
        for (CompoundTag tag : tags) {
            try {
                if (!tag.contains("id"))
                    throw new Exception("NBT tag not handled because the id key is missing");

                String id = (String) tag.get("id").getValue();
                if (!types.containsKey(id))
                    throw new Exception("Not handled id: " + id);

                int newId = types.get(id);
                if (newId == -1)
                    continue;

                int x = ((NumberTag) tag.get("x")).asInt();
                int y = ((NumberTag) tag.get("y")).asInt();
                int z = ((NumberTag) tag.get("z")).asInt();

                Position pos = new Position(x, (short) y, z);

                updateBlockEntity(pos, (short) newId, tag, connection);
            } catch (Exception e) {
                if (Via.getManager().isDebug()) {
                    Via.getPlatform().getLogger().warning("Block Entity: " + e.getMessage() + ": " + tag);
                }
            }
        }
    }

    private static void updateBlockEntity(Position pos, short id, CompoundTag tag, UserConnection connection) throws Exception {
        PacketWrapper wrapper = PacketWrapper.create(0x09, null, connection);
        wrapper.write(Type.POSITION, pos);
        wrapper.write(Type.UNSIGNED_BYTE, id);
        wrapper.write(Type.NBT, tag);
        wrapper.scheduleSend(Protocol1_9_1_2To1_9_3_4.class, false);
    }
}
