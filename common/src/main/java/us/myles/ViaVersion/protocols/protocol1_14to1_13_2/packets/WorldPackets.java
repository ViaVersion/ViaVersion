package us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.LongArrayTag;
import com.google.common.primitives.Bytes;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.entities.Entity1_14Types;
import us.myles.ViaVersion.api.minecraft.BlockChangeRecord;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.remapper.ValueCreator;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.types.Chunk1_13Type;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.metadata.MetadataRewriter1_14To1_13_2;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.Protocol1_14To1_13_2;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data.MappingData;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.storage.EntityTracker1_14;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.types.Chunk1_14Type;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

public class WorldPackets {
    private static final int AIR = MappingData.blockStateMappings.getNewBlock(0);
    private static final int VOID_AIR = MappingData.blockStateMappings.getNewBlock(8591);
    private static final int CAVE_AIR = MappingData.blockStateMappings.getNewBlock(8592);
    public static final int SERVERSIDE_VIEW_DISTANCE = 64;

    public static void register(final Protocol protocol) {

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
        protocol.registerOutgoing(State.PLAY, 0x0F, 0x0F, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // 0 - Chunk X
                map(Type.INT); // 1 - Chunk Z
                map(Type.BLOCK_CHANGE_RECORD_ARRAY); // 2 - Records
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        // Convert ids
                        for (BlockChangeRecord record : wrapper.get(Type.BLOCK_CHANGE_RECORD_ARRAY, 0)) {
                            int id = record.getBlockId();
                            record.setBlockId(Protocol1_14To1_13_2.getNewBlockStateId(id));
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
                                            worldSurface[x + z * 16] = y + s * 16 + 2; // Should be +1 (top of the block) but +2 works :tm:
                                        }
                                        if (MappingData.motionBlocking.contains(id)) {
                                            motionBlocking[x + z * 16] = y + s * 16 + 2; // Should be +1 (top of the block) but +2 works :tm:
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
                        int skyLightMask = 0;
                        int blockLightMask = 0;
                        for (int i = 0; i < chunk.getSections().length; i++) {
                            ChunkSection sec = chunk.getSections()[i];
                            if (sec == null) continue;
                            if (sec.hasSkyLight()) {
                                skyLightMask |= (1 << (i + 1));
                            }
                            blockLightMask |= (1 << (i + 1));
                        }
                        lightPacket.write(Type.VAR_INT, skyLightMask);
                        lightPacket.write(Type.VAR_INT, blockLightMask);
                        lightPacket.write(Type.VAR_INT, 0);  // empty sky light mask
                        lightPacket.write(Type.VAR_INT, 0);  // empty block light mask
                        for (ChunkSection section : chunk.getSections()) {
                            if (section == null || !section.hasSkyLight()) continue;
                            lightPacket.write(Type.BYTE_ARRAY, Bytes.asList(section.getSkyLight()).toArray(new Byte[0]));
                        }
                        for (ChunkSection section : chunk.getSections()) {
                            if (section == null) continue;
                            lightPacket.write(Type.BYTE_ARRAY, Bytes.asList(section.getBlockLight()).toArray(new Byte[0]));
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
                            wrapper.set(Type.INT, 1, data = InventoryPackets.getNewItemId(data));
                        } else if (id == 2001) { // Block break + block break sound
                            wrapper.set(Type.INT, 1, data = Protocol1_14To1_13_2.getNewBlockStateId(data));
                        }
                    }
                });
            }
        });

        // Spawn Particle
        protocol.registerOutgoing(State.PLAY, 0x24, 0x23, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // 0 - Particle ID
                map(Type.BOOLEAN); // 1 - Long Distance
                map(Type.FLOAT); // 2 - X
                map(Type.FLOAT); // 3 - Y
                map(Type.FLOAT); // 4 - Z
                map(Type.FLOAT); // 5 - Offset X
                map(Type.FLOAT); // 6 - Offset Y
                map(Type.FLOAT); // 7 - Offset Z
                map(Type.FLOAT); // 8 - Particle Data
                map(Type.INT); // 9 - Particle Count
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int id = wrapper.get(Type.INT, 0);
                        if (id == 3 || id == 20) {
                            int data = wrapper.passthrough(Type.VAR_INT);
                            wrapper.set(Type.VAR_INT, 0, Protocol1_14To1_13_2.getNewBlockStateId(data));
                        } else if (id == 27) {
                            InventoryPackets.toClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                        }

                        int newId = MetadataRewriter1_14To1_13_2.getNewParticleId(id);
                        if (newId != id) {
                            wrapper.set(Type.INT, 0, newId);
                        }
                    }
                });
            }
        });

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
        final int bitsPerBlock = 9;
        long maxEntryValue = (1L << bitsPerBlock) - 1;

        int length = (int) Math.ceil(heightMap.length * bitsPerBlock / 64.0);
        long[] data = new long[length];

        for (int index = 0; index < heightMap.length; index++) {
            int value = heightMap[index];
            int bitIndex = index * 9;
            int startIndex = bitIndex / 64;
            int endIndex = ((index + 1) * bitsPerBlock - 1) / 64;
            int startBitSubIndex = bitIndex % 64;
            data[startIndex] = data[startIndex] & ~(maxEntryValue << startBitSubIndex) | ((long) value & maxEntryValue) << startBitSubIndex;
            if (startIndex != endIndex) {
                int endBitSubIndex = 64 - startBitSubIndex;
                data[endIndex] = data[endIndex] >>> endBitSubIndex << endBitSubIndex | ((long) value & maxEntryValue) >> endBitSubIndex;
            }
        }

        return data;
    }
}
