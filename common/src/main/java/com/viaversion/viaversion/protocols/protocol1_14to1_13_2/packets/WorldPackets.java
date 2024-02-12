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
package com.viaversion.viaversion.protocols.protocol1_14to1_13_2.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.LongArrayTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockFace;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.NibbleArray;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_13;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_14;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.ClientboundPackets1_14;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.Protocol1_14To1_13_2;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.storage.EntityTracker1_14;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.util.CompactArrayUtil;
import java.util.Arrays;

public class WorldPackets {
    public static final int SERVERSIDE_VIEW_DISTANCE = 64;
    private static final byte[] FULL_LIGHT = new byte[2048];
    public static int air;
    public static int voidAir;
    public static int caveAir;

    static {
        Arrays.fill(FULL_LIGHT, (byte) 0xff);
    }

    public static void register(Protocol1_14To1_13_2 protocol) {
        BlockRewriter<ClientboundPackets1_13> blockRewriter = BlockRewriter.for1_14(protocol);

        protocol.registerClientbound(ClientboundPackets1_13.BLOCK_BREAK_ANIMATION, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT);
                map(Type.POSITION1_8, Type.POSITION1_14);
                map(Type.BYTE);
            }
        });
        protocol.registerClientbound(ClientboundPackets1_13.BLOCK_ENTITY_DATA, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.POSITION1_8, Type.POSITION1_14);
            }
        });
        protocol.registerClientbound(ClientboundPackets1_13.BLOCK_ACTION, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.POSITION1_8, Type.POSITION1_14); // Location
                map(Type.UNSIGNED_BYTE); // Action id
                map(Type.UNSIGNED_BYTE); // Action param
                map(Type.VAR_INT); // Block id - /!\ NOT BLOCK STATE
                handler(wrapper -> wrapper.set(Type.VAR_INT, 0, protocol.getMappingData().getNewBlockId(wrapper.get(Type.VAR_INT, 0))));
            }
        });
        protocol.registerClientbound(ClientboundPackets1_13.BLOCK_CHANGE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.POSITION1_8, Type.POSITION1_14);
                map(Type.VAR_INT);
                handler(wrapper -> {
                    int id = wrapper.get(Type.VAR_INT, 0);

                    wrapper.set(Type.VAR_INT, 0, protocol.getMappingData().getNewBlockStateId(id));
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.SERVER_DIFFICULTY, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UNSIGNED_BYTE);
                handler(wrapper -> {
                    wrapper.write(Type.BOOLEAN, false);  // Added in 19w11a. Maybe https://bugs.mojang.com/browse/MC-44471 ?
                });
            }
        });

        blockRewriter.registerMultiBlockChange(ClientboundPackets1_13.MULTI_BLOCK_CHANGE);

        protocol.registerClientbound(ClientboundPackets1_13.EXPLOSION, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.FLOAT); // X
                map(Type.FLOAT); // Y
                map(Type.FLOAT); // Z
                map(Type.FLOAT); // Radius
                handler(wrapper -> {
                    for (int i = 0; i < 3; i++) {
                        float coord = wrapper.get(Type.FLOAT, i);

                        if (coord < 0f) {
                            coord = (int) coord;
                            wrapper.set(Type.FLOAT, i, coord);
                        }
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.CHUNK_DATA, wrapper -> {
            ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
            Chunk chunk = wrapper.read(ChunkType1_13.forEnvironment(clientWorld.getEnvironment()));
            wrapper.write(ChunkType1_14.TYPE, chunk);

            int[] motionBlocking = new int[16 * 16];
            int[] worldSurface = new int[16 * 16];

            for (int s = 0; s < chunk.getSections().length; s++) {
                ChunkSection section = chunk.getSections()[s];
                if (section == null) continue;
                DataPalette blocks = section.palette(PaletteType.BLOCKS);

                boolean hasBlock = false;
                for (int i = 0; i < blocks.size(); i++) {
                    int old = blocks.idByIndex(i);
                    int newId = protocol.getMappingData().getNewBlockStateId(old);
                    if (!hasBlock && newId != air && newId != voidAir && newId != caveAir) { // air, void_air, cave_air
                        hasBlock = true;
                    }
                    blocks.setIdByIndex(i, newId);
                }
                if (!hasBlock) {
                    section.setNonAirBlocksCount(0);
                    continue;
                }

                int nonAirBlockCount = 0;
                int sy = s << 4;
                for (int idx = 0; idx < ChunkSection.SIZE; idx++) {
                    int id = blocks.idAt(idx);
                    if (id == air || id == voidAir || id == caveAir) continue;
                    nonAirBlockCount++;

                    int xz = idx & 0xFF;
                    int y = ChunkSection.yFromIndex(idx);
                    worldSurface[xz] = sy + y + 1; // +1 (top of the block)

                    if (protocol.getMappingData().getMotionBlocking().contains(id)) {
                        motionBlocking[xz] = sy + y + 1; // +1 (top of the block)
                    }

                    // Manually update light for non-full blocks (block light must not be sent)
                    if (Via.getConfig().isNonFullBlockLightFix() && protocol.getMappingData().getNonFullBlocks().contains(id)) {
                        int x = ChunkSection.xFromIndex(idx);
                        int z = ChunkSection.zFromIndex(idx);
                        setNonFullLight(chunk, section, s, x, y, z);
                    }
                }

                section.setNonAirBlocksCount(nonAirBlockCount);
            }

            CompoundTag heightMap = new CompoundTag();
            heightMap.put("MOTION_BLOCKING", new LongArrayTag(encodeHeightMap(motionBlocking)));
            heightMap.put("WORLD_SURFACE", new LongArrayTag(encodeHeightMap(worldSurface)));
            chunk.setHeightMap(heightMap);

            PacketWrapper lightPacket = wrapper.create(ClientboundPackets1_14.UPDATE_LIGHT);
            lightPacket.write(Type.VAR_INT, chunk.getX());
            lightPacket.write(Type.VAR_INT, chunk.getZ());

            int skyLightMask = chunk.isFullChunk() ? 0x3ffff : 0; // all 18 bits set if ground up
            int blockLightMask = 0;
            for (int i = 0; i < chunk.getSections().length; i++) {
                ChunkSection sec = chunk.getSections()[i];
                if (sec == null) continue;
                if (!chunk.isFullChunk() && sec.getLight().hasSkyLight()) {
                    skyLightMask |= (1 << (i + 1));
                }
                blockLightMask |= (1 << (i + 1));
            }

            lightPacket.write(Type.VAR_INT, skyLightMask);
            lightPacket.write(Type.VAR_INT, blockLightMask);
            lightPacket.write(Type.VAR_INT, 0);  // empty sky light mask
            lightPacket.write(Type.VAR_INT, 0);  // empty block light mask

            // not sending skylight/setting empty skylight causes client lag due to some weird calculations
            // only do this on the initial chunk send (not when chunk.isGroundUp() is false)
            if (chunk.isFullChunk())
                lightPacket.write(Type.BYTE_ARRAY_PRIMITIVE, FULL_LIGHT); // chunk below 0
            for (ChunkSection section : chunk.getSections()) {
                if (section == null || !section.getLight().hasSkyLight()) {
                    if (chunk.isFullChunk()) {
                        lightPacket.write(Type.BYTE_ARRAY_PRIMITIVE, FULL_LIGHT);
                    }
                    continue;
                }
                lightPacket.write(Type.BYTE_ARRAY_PRIMITIVE, section.getLight().getSkyLight());
            }
            if (chunk.isFullChunk())
                lightPacket.write(Type.BYTE_ARRAY_PRIMITIVE, FULL_LIGHT); // chunk above 255

            for (ChunkSection section : chunk.getSections()) {
                if (section == null) continue;
                lightPacket.write(Type.BYTE_ARRAY_PRIMITIVE, section.getLight().getBlockLight());
            }

            EntityTracker1_14 entityTracker = wrapper.user().getEntityTracker(Protocol1_14To1_13_2.class);
            int diffX = Math.abs(entityTracker.getChunkCenterX() - chunk.getX());
            int diffZ = Math.abs(entityTracker.getChunkCenterZ() - chunk.getZ());
            if (entityTracker.isForceSendCenterChunk()
                    || diffX >= SERVERSIDE_VIEW_DISTANCE
                    || diffZ >= SERVERSIDE_VIEW_DISTANCE) {
                PacketWrapper fakePosLook = wrapper.create(ClientboundPackets1_14.UPDATE_VIEW_POSITION); // Set center chunk
                fakePosLook.write(Type.VAR_INT, chunk.getX());
                fakePosLook.write(Type.VAR_INT, chunk.getZ());
                fakePosLook.send(Protocol1_14To1_13_2.class);
                entityTracker.setChunkCenterX(chunk.getX());
                entityTracker.setChunkCenterZ(chunk.getZ());
            }

            lightPacket.send(Protocol1_14To1_13_2.class);

            // Remove light references from chunk sections
            for (ChunkSection section : chunk.getSections()) {
                if (section != null) {
                    section.setLight(null);
                }
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.EFFECT, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // Effect Id
                map(Type.POSITION1_8, Type.POSITION1_14); // Location
                map(Type.INT); // Data
                handler(wrapper -> {
                    int id = wrapper.get(Type.INT, 0);
                    int data = wrapper.get(Type.INT, 1);
                    if (id == 1010) { // Play record
                        wrapper.set(Type.INT, 1, protocol.getMappingData().getNewItemId(data));
                    } else if (id == 2001) { // Block break + block break sound
                        wrapper.set(Type.INT, 1, protocol.getMappingData().getNewBlockStateId(data));
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.MAP_DATA, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT);
                map(Type.BYTE);
                map(Type.BOOLEAN);
                handler(wrapper -> {
                    wrapper.write(Type.BOOLEAN, false);  // new value, probably if the map is locked (added in 19w02a), old maps are not locked
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // 0 - Dimension ID
                handler(wrapper -> {
                    ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                    int dimensionId = wrapper.get(Type.INT, 0);
                    clientWorld.setEnvironment(dimensionId);
                    EntityTracker1_14 entityTracker = wrapper.user().getEntityTracker(Protocol1_14To1_13_2.class);
                    // The client may reset the center chunk if dimension is changed
                    entityTracker.setForceSendCenterChunk(true);
                });
                handler(wrapper -> {
                    short difficulty = wrapper.read(Type.UNSIGNED_BYTE); // 19w11a removed difficulty from respawn
                    PacketWrapper difficultyPacket = wrapper.create(ClientboundPackets1_14.SERVER_DIFFICULTY);
                    difficultyPacket.write(Type.UNSIGNED_BYTE, difficulty);
                    difficultyPacket.write(Type.BOOLEAN, false); // Unknown value added in 19w11a
                    difficultyPacket.scheduleSend(protocol.getClass());
                });
                handler(wrapper -> {
                    // Manually send the packet and update the viewdistance after
                    wrapper.send(Protocol1_14To1_13_2.class);
                    wrapper.cancel();
                    sendViewDistancePacket(wrapper.user());
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.SPAWN_POSITION, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.POSITION1_8, Type.POSITION1_14);
            }
        });
    }

    static void sendViewDistancePacket(UserConnection connection) throws Exception {
        PacketWrapper setViewDistance = PacketWrapper.create(ClientboundPackets1_14.UPDATE_VIEW_DISTANCE, connection);
        setViewDistance.write(Type.VAR_INT, WorldPackets.SERVERSIDE_VIEW_DISTANCE);
        setViewDistance.send(Protocol1_14To1_13_2.class);
    }

    private static long[] encodeHeightMap(int[] heightMap) {
        return CompactArrayUtil.createCompactArray(9, heightMap.length, i -> heightMap[i]);
    }

    private static void setNonFullLight(Chunk chunk, ChunkSection section, int ySection, int x, int y, int z) {
        int skyLight = 0;
        int blockLight = 0;
        for (BlockFace blockFace : BlockFace.values()) {
            NibbleArray skyLightArray = section.getLight().getSkyLightNibbleArray();
            NibbleArray blockLightArray = section.getLight().getBlockLightNibbleArray();
            int neighbourX = x + blockFace.modX();
            int neighbourY = y + blockFace.modY();
            int neighbourZ = z + blockFace.modZ();

            if (blockFace.modX() != 0) {
                // Another chunk, nothing we can do without an unnecessary amount of caching
                if (neighbourX == 16 || neighbourX == -1) continue;
            } else if (blockFace.modY() != 0) {
                if (neighbourY == 16 || neighbourY == -1) {
                    if (neighbourY == 16) {
                        ySection += 1;
                        neighbourY = 0;
                    } else {
                        ySection -= 1;
                        neighbourY = 15;
                    }

                    if (ySection == chunk.getSections().length || ySection == -1) continue;

                    ChunkSection newSection = chunk.getSections()[ySection];
                    if (newSection == null) continue;

                    skyLightArray = newSection.getLight().getSkyLightNibbleArray();
                    blockLightArray = newSection.getLight().getBlockLightNibbleArray();
                }
            } else if (blockFace.modZ() != 0) {
                // Another chunk, nothing we can do without an unnecessary amount of caching
                if (neighbourZ == 16 || neighbourZ == -1) continue;
            }

            if (blockLightArray != null && blockLight != 15) {
                int neighbourBlockLight = blockLightArray.get(neighbourX, neighbourY, neighbourZ);
                if (neighbourBlockLight == 15) {
                    blockLight = 14;
                } else if (neighbourBlockLight > blockLight) {
                    blockLight = neighbourBlockLight - 1; // lower light level by one
                }
            }
            if (skyLightArray != null && skyLight != 15) {
                int neighbourSkyLight = skyLightArray.get(neighbourX, neighbourY, neighbourZ);
                if (neighbourSkyLight == 15) {
                    if (blockFace.modY() == 1) {
                        // Keep 15 if block is exposed to sky
                        skyLight = 15;
                        continue;
                    }

                    skyLight = 14;
                } else if (neighbourSkyLight > skyLight) {
                    skyLight = neighbourSkyLight - 1; // lower light level by one
                }
            }
        }

        if (skyLight != 0) {
            if (!section.getLight().hasSkyLight()) {
                byte[] newSkyLight = new byte[2028];
                section.getLight().setSkyLight(newSkyLight);
            }

            section.getLight().getSkyLightNibbleArray().set(x, y, z, skyLight);
        }
        if (blockLight != 0) {
            section.getLight().getBlockLightNibbleArray().set(x, y, z, blockLight);
        }
    }

    private static long getChunkIndex(int x, int z) {
        return ((x & 0x3FFFFFFL) << 38) | (z & 0x3FFFFFFL);
    }
}
