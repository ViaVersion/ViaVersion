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
package us.myles.ViaVersion.bukkit.providers;

import com.google.common.collect.Lists;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.bukkit.util.NMSUtil;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.BulkChunkTranslatorProvider;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.ClientChunks;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;

public class BukkitViaBulkChunkTranslator extends BulkChunkTranslatorProvider {
    // Reflection
    private static ReflectionUtil.ClassReflection mapChunkBulkRef;
    private static ReflectionUtil.ClassReflection mapChunkRef;
    private static Method obfuscateRef;

    static {
        try {
            mapChunkBulkRef = new ReflectionUtil.ClassReflection(NMSUtil.nms("PacketPlayOutMapChunkBulk"));
            mapChunkRef = new ReflectionUtil.ClassReflection(NMSUtil.nms("PacketPlayOutMapChunk"));
            if (((ViaVersionPlugin) Via.getPlatform()).isSpigot()) {
                obfuscateRef = Class.forName("org.spigotmc.AntiXray").getMethod("obfuscate", int.class, int.class, int.class, byte[].class, NMSUtil.nms("World"));
            }
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
            int[] xcoords = mapChunkBulkRef.getFieldValue("a", packet, int[].class);
            int[] zcoords = mapChunkBulkRef.getFieldValue("b", packet, int[].class);
            Object[] chunkMaps = mapChunkBulkRef.getFieldValue("c", packet, Object[].class);

            if (Via.getConfig().isAntiXRay() && ((ViaVersionPlugin) Via.getPlatform()).isSpigot()) { //Spigot anti-xray patch
                try {
                    Object world = mapChunkBulkRef.getFieldValue("world", packet, Object.class);
                    Object spigotConfig = ReflectionUtil.getPublic(world, "spigotConfig", Object.class);
                    Object antiXrayInstance = ReflectionUtil.getPublic(spigotConfig, "antiXrayInstance", Object.class);

                    for (int i = 0; i < xcoords.length; ++i) {
                        // TODO: Possibly optimise this
                        Object b = ReflectionUtil.get(chunkMaps[i], "b", Object.class);
                        Object a = ReflectionUtil.get(chunkMaps[i], "a", Object.class);

                        obfuscateRef.invoke(antiXrayInstance, xcoords[i], zcoords[i], b, a, world);
                    }
                } catch (Exception e) {
                    // not spigot, or it failed.
                }
            }
            for (int i = 0; i < chunkMaps.length; i++) {
                int x = xcoords[i];
                int z = zcoords[i];
                Object chunkMap = chunkMaps[i];
                Object chunkPacket = mapChunkRef.newInstance();
                mapChunkRef.setFieldValue("a", chunkPacket, x);
                mapChunkRef.setFieldValue("b", chunkPacket, z);
                mapChunkRef.setFieldValue("c", chunkPacket, chunkMap);
                mapChunkRef.setFieldValue("d", chunkPacket, true); // Chunk bulk chunks are always ground-up
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
        return packetClass.getName().endsWith("PacketPlayOutMapChunkBulk");
    }

    @Override
    public boolean isPacketLevel() {
        return false;
    }
}
