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
package us.myles.ViaVersion.sponge.providers;

import com.google.common.collect.Lists;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.BulkChunkTranslatorProvider;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.ClientChunks;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.util.List;
import java.util.logging.Level;

public class SpongeViaBulkChunkTranslator extends BulkChunkTranslatorProvider {
    // Reflection
    private static ReflectionUtil.ClassReflection mapChunkBulkRef;
    private static ReflectionUtil.ClassReflection mapChunkRef;

    static {

        try {
            mapChunkBulkRef = new ReflectionUtil.ClassReflection(Class.forName("net.minecraft.network.play.server.S26PacketMapChunkBulk"));
            mapChunkRef = new ReflectionUtil.ClassReflection(Class.forName("net.minecraft.network.play.server.S21PacketChunkData"));
        } catch (ClassNotFoundException e) {
            // Ignore as server is probably 1.9+
        } catch (Exception e) {
            Via.getPlatform().getLogger().log(Level.WARNING, "Failed to initialise chunks reflection", e);
        }
    }

    @Override
    public List<Object> transformMapChunkBulk(Object packet, ClientChunks clientChunks) {
        List<Object> list = Lists.newArrayList();
        try {
            int[] xcoords = mapChunkBulkRef.getFieldValue("field_149266_a", packet, int[].class);
            int[] zcoords = mapChunkBulkRef.getFieldValue("field_149264_b", packet, int[].class);
            Object[] chunkMaps = mapChunkBulkRef.getFieldValue("field_179755_c", packet, Object[].class);
            for (int i = 0; i < chunkMaps.length; i++) {
                int x = xcoords[i];
                int z = zcoords[i];
                Object chunkMap = chunkMaps[i];
                Object chunkPacket = mapChunkRef.newInstance();
                mapChunkRef.setFieldValue("field_149284_a", chunkPacket, x);
                mapChunkRef.setFieldValue("field_149282_b", chunkPacket, z);
                mapChunkRef.setFieldValue("field_179758_c", chunkPacket, chunkMap);
                mapChunkRef.setFieldValue("field_149279_g", chunkPacket, true); // Chunk bulk chunks are always ground-up
                clientChunks.getBulkChunks().add(ClientChunks.toLong(x, z)); // Store for later
                list.add(chunkPacket);
            }
        } catch (Exception e) {
            Via.getPlatform().getLogger().log(Level.WARNING, "Failed to transform chunks bulk", e);
        }
        return list;
    }

    @Override
    public boolean isFiltered(Class<?> packetClass) {
        return packetClass.getName().endsWith("S26PacketMapChunkBulk");
    }

    @Override
    public boolean isPacketLevel() {
        return false;
    }
}
