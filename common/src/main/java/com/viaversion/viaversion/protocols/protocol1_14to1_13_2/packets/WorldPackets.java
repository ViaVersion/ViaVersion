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
package us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.LongArrayTag;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.entities.Entity1_14Types;
import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import us.myles.ViaVersion.api.minecraft.chunks.NibbleArray;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.remapper.ValueCreator;
import us.myles.ViaVersion.api.rewriters.BlockRewriter;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.ClientboundPackets1_13;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.types.Chunk1_13Type;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.Protocol1_14To1_13_2;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.storage.EntityTracker1_14;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.types.Chunk1_14Type;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import us.myles.ViaVersion.util.CompactArrayUtil;

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
        BlockRewriter blockRewriter = new BlockRewriter(protocol, null);

        protocol.registerOutgoing(ClientboundPackets1_13.BLOCK_BREAK_ANIMATION, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT);
                map(Type.POSITION, Type.POSITION1_14);
                map(Type.BYTE);
            }
        });
        protocol.registerOutgoing(ClientboundPackets1_13.BLOCK_ENTITY_DATA, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION, Type.POSITION1_14);
            }
        });
        protocol.registerOutgoing(ClientboundPackets1_13.BLOCK_ACTION, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION, Type.POSITION1_14); // Location
                map(Type.UNSIGNED_BYTE); // Action id
                map(Type.UNSIGNED_BYTE); // Action param
                map(Type.VAR_INT); // Block id - /!\ NOT BLOCK STATE
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        wrapper.set(Type.VAR_INT, 0, protocol.getMappingData().getNewBlockId(wrapper.get(Type.VAR_INT, 0)));
                    }
                });
            }
        });
        protocol.registerOutgoing(ClientboundPackets1_13.BLOCK_CHANGE, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION, Type.POSITION1_14);
                map(Type.VAR_INT);
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int id = wrapper.get(Type.VAR_INT, 0);

                        wrapper.set(Type.VAR_INT, 0, protocol.getMappingData().getNewBlockStateId(id));
                    }
                });
            }
        });

        protocol.registerOutgoing(ClientboundPackets1_13.SERVER_DIFFICULTY, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE);
                create(new ValueCreator() {
                    @Override
                    public void write(PacketWrapper wrapper) throws Exception {
                        wrapper.write(Type.BOOLEAN, false);  // Added in 19w11a. Maybe https://bugs.mojang.com/browse/MC-44471 ?
                    }
                });
            }
        });

        blockRewriter.registerMultiBlockChange(ClientboundPackets1_13.MULTI_BLOCK_CHANGE);

        protocol.registerOutgoing(ClientboundPackets1_13.EXPLOSION, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.FLOAT); // X
                map(Type.FLOAT); // Y
                map(Type.FLOAT); // Z
                map(Type.FLOAT); // Radius
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        for (int i = 0; i < 3; i++) {
                            float coord = wrapper.get(Type.FLOAT, i);

                            if (coord < 0f) {
                                coord = (int) coord;
                                wrapper.set(Type.FLOAT, i, coord);
                            }
                        }
                    }
                });
            }
        });

        protocol.registerOutgoing(ClientboundPackets1_13.CHUNK_DATA, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                        Chunk chunk = wrapper.read(new Chunk1_13Type(clientWorld));
                        wrapper.write(new Chunk1_14Type(), chunk);

                        int[] motionBlocking = new int[16 * 16];
                        int[] worldSurface = new int[16 * 16];

                        for (int s = 0; s < chunk.getSections().length; s++) {
                            ChunkSection section = chunk.getSections()[s];
                            if (section == null) continue;

                            boolean hasBlock = false;
                            for (int i = 0; i < section.getPaletteSize(); i++) {
                                int old = section.getPaletteEntry(i);
                                int newId = protocol.getMappingData().getNewBlockStateId(old);
                                if (!hasBlock && newId != air && newId != voidAir && newId != caveAir) { // air, void_air, cave_air
                                    hasBlock = true;
                                }
                                section.setPaletteEntry(i, newId);
                            }
                            if (!hasBlock) {
                                section.setNonAirBlocksCount(0);
                                continue;
                            }

                            int nonAirBlockCount = 0;
                            for (int x = 0; x < 16; x++) {
                                for (int y = 0; y < 16; y++) {
                                    for (int z = 0; z < 16; z++) {
                                        int id = section.getFlatBlock(x, y, z);
                                        if (id != air && id != voidAir && id != caveAir) {
                                            nonAirBlockCount++;
                                            worldSurface[x + z * 16] = y + s * 16 + 1; // +1 (top of the block)
                                        }
                                        if (protocol.getMappingData().getMotionBlocking().contains(id)) {
                                            motionBlocking[x + z * 16] = y + s * 16 + 1; // +1 (top of the block)
                                        }

                                        // Manually update light for non full blocks (block light must not be sent)
                                        if (Via.getConfig().isNonFullBlockLightFix() && protocol.getMappingData().getNonFullBlocks().contains(id)) {
                                            setNonFullLight(chunk, section, s, x, y, z);
                                        }
                                    }
                                }
                            }

                            section.setNonAirBlocksCount(nonAirBlockCount);
                        }

                        CompoundTag heightMap = new CompoundTag();
                        heightMap.put("MOTION_BLOCKING", new LongArrayTag(encodeHeightMap(motionBlocking)));
                        heightMap.put("WORLD_SURFACE", new LongArrayTag(encodeHeightMap(worldSurface)));
                        chunk.setHeightMap(heightMap);

                        PacketWrapper lightPacket = wrapper.create(0x24);
                        lightPacket.write(Type.VAR_INT, chunk.getX());
                        lightPacket.write(Type.VAR_INT, chunk.getZ());

                        int skyLightMask = chunk.isFullChunk() ? 0x3ffff : 0; // all 18 bits set if ground up
                        int blockLightMask = 0;
                        for (int i = 0; i < chunk.getSections().length; i++) {
                            ChunkSection sec = chunk.getSections()[i];
                            if (sec == null) continue;
                            if (!chunk.isFullChunk() && sec.hasSkyLight()) {
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
                            if (section == null || !section.hasSkyLight()) {
                                if (chunk.isFullChunk()) {
                                    lightPacket.write(Type.BYTE_ARRAY_PRIMITIVE, FULL_LIGHT);
                                }
                                continue;
                            }
                            lightPacket.write(Type.BYTE_ARRAY_PRIMITIVE, section.getSkyLight());
                        }
                        if (chunk.isFullChunk())
                            lightPacket.write(Type.BYTE_ARRAY_PRIMITIVE, FULL_LIGHT); // chunk above 255

                        for (ChunkSection section : chunk.getSections()) {
                            if (section == null) continue;
                            lightPacket.write(Type.BYTE_ARRAY_PRIMITIVE, section.getBlockLight());
                        }

                        EntityTracker1_14 entityTracker = wrapper.user().get(EntityTracker1_14.class);
                        int diffX = Math.abs(entityTracker.getChunkCenterX() - chunk.getX());
                        int diffZ = Math.abs(entityTracker.getChunkCenterZ() - chunk.getZ());
                        if (entityTracker.isForceSendCenterChunk()
                                || diffX >= SERVERSIDE_VIEW_DISTANCE
                                || diffZ >= SERVERSIDE_VIEW_DISTANCE) {
                            PacketWrapper fakePosLook = wrapper.create(0x40); // Set center chunk
                            fakePosLook.write(Type.VAR_INT, chunk.getX());
                            fakePosLook.write(Type.VAR_INT, chunk.getZ());
                            fakePosLook.send(Protocol1_14To1_13_2.class, true, true);
                            entityTracker.setChunkCenterX(chunk.getX());
                            entityTracker.setChunkCenterZ(chunk.getZ());
                        }

                        lightPacket.send(Protocol1_14To1_13_2.class, true, true);
                    }

                    private Byte[] fromPrimitiveArray(byte[] bytes) {
                        Byte[] newArray = new Byte[bytes.length];
                        for (int i = 0; i < bytes.length; i++) {
                            newArray[i] = bytes[i];
                        }
                        return newArray;
                    }
                });
            }
        });

        protocol.registerOutgoing(ClientboundPackets1_13.EFFECT, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // Effect Id
                map(Type.POSITION, Type.POSITION1_14); // Location
                map(Type.INT); // Data
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int id = wrapper.get(Type.INT, 0);
                        int data = wrapper.get(Type.INT, 1);
                        if (id == 1010) { // Play record
                            wrapper.set(Type.INT, 1, protocol.getMappingData().getNewItemId(data));
                        } else if (id == 2001) { // Block break + block break sound
                            wrapper.set(Type.INT, 1, protocol.getMappingData().getNewBlockStateId(data));
                        }
                    }
                });
            }
        });

        protocol.registerOutgoing(ClientboundPackets1_13.JOIN_GAME, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // 0 - Entity ID
                map(Type.UNSIGNED_BYTE); // 1 - Gamemode
                map(Type.INT); // 2 - Dimension

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        // Store the player
                        ClientWorld clientChunks = wrapper.user().get(ClientWorld.class);
                        int dimensionId = wrapper.get(Type.INT, 1);
                        clientChunks.setEnvironment(dimensionId);

                        int entityId = wrapper.get(Type.INT, 0);

                        Entity1_14Types entType = Entity1_14Types.PLAYER;
                        // Register Type ID
                        EntityTracker1_14 tracker = wrapper.user().get(EntityTracker1_14.class);
                        tracker.addEntity(entityId, entType);
                        tracker.setClientEntityId(entityId);
                    }
                });

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        short difficulty = wrapper.read(Type.UNSIGNED_BYTE); // 19w11a removed difficulty from join game
                        PacketWrapper difficultyPacket = wrapper.create(0x0D);
                        difficultyPacket.write(Type.UNSIGNED_BYTE, difficulty);
                        difficultyPacket.write(Type.BOOLEAN, false); // Unknown value added in 19w11a
                        difficultyPacket.send(protocol.getClass());

                        wrapper.passthrough(Type.UNSIGNED_BYTE); // Max Players
                        wrapper.passthrough(Type.STRING); // Level Type

                        wrapper.write(Type.VAR_INT, SERVERSIDE_VIEW_DISTANCE);  // Serverside view distance, added in 19w13a
                    }
                });
            }
        });

        protocol.registerOutgoing(ClientboundPackets1_13.MAP_DATA, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT);
                map(Type.BYTE);
                map(Type.BOOLEAN);
                create(new ValueCreator() {
                    @Override
                    public void write(PacketWrapper wrapper) throws Exception {
                        wrapper.write(Type.BOOLEAN, false);  // new value, probably if the map is locked (added in 19w02a), old maps are not locked
                    }
                });
            }
        });

        protocol.registerOutgoing(ClientboundPackets1_13.RESPAWN, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // 0 - Dimension ID
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                        int dimensionId = wrapper.get(Type.INT, 0);
                        clientWorld.setEnvironment(dimensionId);
                        EntityTracker1_14 entityTracker = wrapper.user().get(EntityTracker1_14.class);
                        // The client may reset the center chunk if dimension is changed
                        entityTracker.setForceSendCenterChunk(true);
                    }
                });
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        short difficulty = wrapper.read(Type.UNSIGNED_BYTE); // 19w11a removed difficulty from respawn
                        PacketWrapper difficultyPacket = wrapper.create(0x0D);
                        difficultyPacket.write(Type.UNSIGNED_BYTE, difficulty);
                        difficultyPacket.write(Type.BOOLEAN, false); // Unknown value added in 19w11a
                        difficultyPacket.send(protocol.getClass());
                    }
                });
            }
        });

        protocol.registerOutgoing(ClientboundPackets1_13.SPAWN_POSITION, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION, Type.POSITION1_14);
            }
        });
    }

    private static long[] encodeHeightMap(int[] heightMap) {
        return CompactArrayUtil.createCompactArray(9, heightMap.length, i -> heightMap[i]);
    }

    private static void setNonFullLight(Chunk chunk, ChunkSection section, int ySection, int x, int y, int z) {
        int skyLight = 0;
        int blockLight = 0;
        for (BlockFace blockFace : BlockFace.values()) {
            NibbleArray skyLightArray = section.getSkyLightNibbleArray();
            NibbleArray blockLightArray = section.getBlockLightNibbleArray();
            int neighbourX = x + blockFace.getModX();
            int neighbourY = y + blockFace.getModY();
            int neighbourZ = z + blockFace.getModZ();

            if (blockFace.getModX() != 0) {
                // Another chunk, nothing we can do without an unnecessary amount of caching
                if (neighbourX == 16 || neighbourX == -1) continue;
            } else if (blockFace.getModY() != 0) {
                if (neighbourY == 16 || neighbourY == -1) {
                    if (neighbourY == 16) {
                        ySection += 1;
                        neighbourY = 0;
                    } else {
                        ySection -= 1;
                        neighbourY = 15;
                    }

                    if (ySection == 16 || ySection == -1) continue;

                    ChunkSection newSection = chunk.getSections()[ySection];
                    if (newSection == null) continue;

                    skyLightArray = newSection.getSkyLightNibbleArray();
                    blockLightArray = newSection.getBlockLightNibbleArray();
                }
            } else if (blockFace.getModZ() != 0) {
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
                    if (blockFace.getModY() == 1) {
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
            if (!section.hasSkyLight()) {
                byte[] newSkyLight = new byte[2028];
                section.setSkyLight(newSkyLight);
            }

            section.getSkyLightNibbleArray().set(x, y, z, skyLight);
        }
        if (blockLight != 0) {
            section.getBlockLightNibbleArray().set(x, y, z, blockLight);
        }
    }

    private static long getChunkIndex(int x, int z) {
        return ((x & 0x3FFFFFFL) << 38) | (z & 0x3FFFFFFL);
    }
}
