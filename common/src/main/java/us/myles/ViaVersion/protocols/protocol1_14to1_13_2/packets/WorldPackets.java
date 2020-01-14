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
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.remapper.ValueCreator;
import us.myles.ViaVersion.api.rewriters.BlockRewriter;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.types.Chunk1_13Type;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.Protocol1_14To1_13_2;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data.MappingData;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.metadata.MetadataRewriter1_14To1_13_2;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.storage.EntityTracker1_14;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.types.Chunk1_14Type;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import us.myles.ViaVersion.util.CompactArrayUtil;

import java.util.Arrays;

public class WorldPackets {
    private static final int AIR = MappingData.blockStateMappings.getNewId(0);
    private static final int VOID_AIR = MappingData.blockStateMappings.getNewId(8591);
    private static final int CAVE_AIR = MappingData.blockStateMappings.getNewId(8592);
    public static final int SERVERSIDE_VIEW_DISTANCE = 64;
    private static final byte[] FULL_LIGHT = new byte[2048];

    static {
        Arrays.fill(FULL_LIGHT, (byte) 0xff);
    }

    public static void register(final Protocol protocol) {
        BlockRewriter blockRewriter = new BlockRewriter(protocol, null, Protocol1_14To1_13_2::getNewBlockStateId, Protocol1_14To1_13_2::getNewBlockId);

        // Block Break Animation
        protocol.registerOutgoing(State.PLAY, 0x08, 0x08, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT);
                map(Type.POSITION, Type.POSITION1_14);
                map(Type.BYTE);
            }
        });

        // Update Block Entity
        protocol.registerOutgoing(State.PLAY, 0x09, 0x09, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION, Type.POSITION1_14);
            }
        });

        // Block Action
        protocol.registerOutgoing(State.PLAY, 0x0A, 0x0A, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION, Type.POSITION1_14); // Location
                map(Type.UNSIGNED_BYTE); // Action id
                map(Type.UNSIGNED_BYTE); // Action param
                map(Type.VAR_INT); // Block id - /!\ NOT BLOCK STATE
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        wrapper.set(Type.VAR_INT, 0, Protocol1_14To1_13_2.getNewBlockId(wrapper.get(Type.VAR_INT, 0)));
                    }
                });
            }
        });

        // Block Change
        protocol.registerOutgoing(State.PLAY, 0x0B, 0x0B, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION, Type.POSITION1_14);
                map(Type.VAR_INT);
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int id = wrapper.get(Type.VAR_INT, 0);

                        wrapper.set(Type.VAR_INT, 0, Protocol1_14To1_13_2.getNewBlockStateId(id));
                    }
                });
            }
        });

        // Server Difficulty
        protocol.registerOutgoing(State.PLAY, 0x0D, 0x0D, new PacketRemapper() {
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

        // Multi Block Change
        blockRewriter.registerMultiBlockChange(0x0F, 0x0F);

        // Explosion
        protocol.registerOutgoing(State.PLAY, 0x1E, 0x1C, new PacketRemapper() {
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

        // Chunk
        protocol.registerOutgoing(State.PLAY, 0x22, 0x21, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                        Chunk chunk = wrapper.read(new Chunk1_13Type(clientWorld));
                        wrapper.write(new Chunk1_14Type(clientWorld), chunk);

                        int[] motionBlocking = new int[16 * 16];
                        int[] worldSurface = new int[16 * 16];

                        for (int s = 0; s < 16; s++) {
                            ChunkSection section = chunk.getSections()[s];
                            if (section == null) continue;

                            boolean hasBlock = false;
                            for (int i = 0; i < section.getPaletteSize(); i++) {
                                int old = section.getPaletteEntry(i);
                                int newId = Protocol1_14To1_13_2.getNewBlockStateId(old);
                                if (!hasBlock && newId != AIR && newId != VOID_AIR && newId != CAVE_AIR) { // air, void_air, cave_air
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
                                        if (id != AIR && id != VOID_AIR && id != CAVE_AIR) {
                                            nonAirBlockCount++;
                                            worldSurface[x + z * 16] = y + s * 16 + 1; // +1 (top of the block)
                                        }
                                        if (MappingData.motionBlocking.contains(id)) {
                                            motionBlocking[x + z * 16] = y + s * 16 + 1; // +1 (top of the block)
                                        }

                                        // Manually update light for non full blocks (block light must not be sent)
                                        if (Via.getConfig().isNonFullBlockLightFix() && MappingData.nonFullBlocks.contains(id)) {
                                            setNonFullLight(chunk, section, s, x, y, z);
                                        }
                                    }
                                }
                            }

                            section.setNonAirBlocksCount(nonAirBlockCount);
                        }

                        CompoundTag heightMap = new CompoundTag("");
                        heightMap.put(new LongArrayTag("MOTION_BLOCKING", encodeHeightMap(motionBlocking)));
                        heightMap.put(new LongArrayTag("WORLD_SURFACE", encodeHeightMap(worldSurface)));
                        chunk.setHeightMap(heightMap);

                        PacketWrapper lightPacket = wrapper.create(0x24);
                        lightPacket.write(Type.VAR_INT, chunk.getX());
                        lightPacket.write(Type.VAR_INT, chunk.getZ());

                        int skyLightMask = chunk.isGroundUp() ? 0x3ffff : 0; // all 18 bits set if ground up
                        int blockLightMask = 0;
                        for (int i = 0; i < chunk.getSections().length; i++) {
                            ChunkSection sec = chunk.getSections()[i];
                            if (sec == null) continue;
                            if (!chunk.isGroundUp() && sec.hasSkyLight()) {
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
                        if (chunk.isGroundUp())
                            lightPacket.write(Type.BYTE_ARRAY_PRIMITIVE, FULL_LIGHT); // chunk below 0
                        for (ChunkSection section : chunk.getSections()) {
                            if (section == null || !section.hasSkyLight()) {
                                if (chunk.isGroundUp()) {
                                    lightPacket.write(Type.BYTE_ARRAY_PRIMITIVE, FULL_LIGHT);
                                }
                                continue;
                            }
                            lightPacket.write(Type.BYTE_ARRAY_PRIMITIVE, section.getSkyLight());
                        }
                        if (chunk.isGroundUp())
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

        // Effect
        protocol.registerOutgoing(State.PLAY, 0x23, 0x22, new PacketRemapper() {
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
                            wrapper.set(Type.INT, 1, InventoryPackets.getNewItemId(data));
                        } else if (id == 2001) { // Block break + block break sound
                            wrapper.set(Type.INT, 1, Protocol1_14To1_13_2.getNewBlockStateId(data));
                        }
                    }
                });
            }
        });

        // Spawn particle
        blockRewriter.registerSpawnParticle(Type.FLOAT, 0x24, 0x23, 3, 20, 27,
                MetadataRewriter1_14To1_13_2::getNewParticleId, InventoryPackets::toClient, Type.FLAT_VAR_INT_ITEM);

        // Join Game
        protocol.registerOutgoing(State.PLAY, 0x25, 0x25, new PacketRemapper() {
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

                        Entity1_14Types.EntityType entType = Entity1_14Types.EntityType.PLAYER;
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

        // Map Data
        protocol.registerOutgoing(State.PLAY, 0x26, 0x26, new PacketRemapper() {
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

        // Respawn
        protocol.registerOutgoing(State.PLAY, 0x38, 0x3A, new PacketRemapper() {
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

        // Spawn Position
        protocol.registerOutgoing(State.PLAY, 0x49, 0x4D, new PacketRemapper() {
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
