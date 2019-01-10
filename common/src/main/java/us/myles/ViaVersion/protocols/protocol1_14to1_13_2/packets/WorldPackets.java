package us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets;

import com.google.common.primitives.Bytes;
import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.PacketWrapper;
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
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.Protocol1_14To1_13_2;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data.MappingData;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.types.Chunk1_14Type;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

public class WorldPackets {
    private static final int AIR = MappingData.blockStateMappings.getNewBlock(0);
    private static final int VOID_AIR = MappingData.blockStateMappings.getNewBlock(8591);
    private static final int CAVE_AIR = MappingData.blockStateMappings.getNewBlock(8592);

    public static void register(Protocol protocol) {

        // Block break animation
        protocol.registerOutgoing(State.PLAY, 0x08, 0x08, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT);
                map(Type.POSITION, Type.POSITION1_14);
                map(Type.BYTE);
            }
        });

        // Update block entity
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
        protocol.registerOutgoing(State.PLAY, 0xB, 0xB, new PacketRemapper() {
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

        // Multi Block Change
        protocol.registerOutgoing(State.PLAY, 0xF, 0xF, new PacketRemapper() {
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

        //Chunk
        protocol.registerOutgoing(State.PLAY, 0x22, 0x22, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                        Chunk chunk = wrapper.read(new Chunk1_13Type(clientWorld));
                        wrapper.write(new Chunk1_14Type(clientWorld), chunk);

                        for (ChunkSection section : chunk.getSections()) {
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
                                        }
                                    }
                                }
                            }
                            section.setNonAirBlocksCount(nonAirBlockCount);
                        }

                        PacketWrapper lightPacket = wrapper.create(0x58);
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
                        lightPacket.write(Type.VAR_INT, 0);  //TODO find out what these two bitmasks mean
                        lightPacket.write(Type.VAR_INT, 0);  //TODO
                        for (ChunkSection section : chunk.getSections()) {
                            if (section == null || !section.hasSkyLight()) continue;
                            ByteBuf buf = wrapper.user().getChannel().alloc().buffer();
                            section.writeSkyLight(buf);
                            byte[] data = new byte[buf.readableBytes()];
                            buf.readBytes(data);
                            buf.release();
                            lightPacket.write(Type.BYTE_ARRAY, Bytes.asList(data).toArray(new Byte[0]));
                        }
                        for (ChunkSection section : chunk.getSections()) {
                            if (section == null) continue;
                            ByteBuf buf = wrapper.user().getChannel().alloc().buffer();
                            section.writeBlockLight(buf);
                            byte[] data = new byte[buf.readableBytes()];
                            buf.readBytes(data);
                            buf.release();
                            lightPacket.write(Type.BYTE_ARRAY, Bytes.asList(data).toArray(new Byte[0]));
                        }
                        lightPacket.send(Protocol1_14To1_13_2.class);
                    }
                });
            }
        });

        // Effect packet
        protocol.registerOutgoing(State.PLAY, 0x23, 0x23, new PacketRemapper() {
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

        //spawn particle
        protocol.registerOutgoing(State.PLAY, 0x24, 0x24, new PacketRemapper() {
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
                    }
                });
            }
        });

        //join game
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
                    }
                });
            }
        });

        //Map Data
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

        //respawn
        protocol.registerOutgoing(State.PLAY, 0x38, 0x39, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // 0 - Dimension ID
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                        int dimensionId = wrapper.get(Type.INT, 0);
                        clientWorld.setEnvironment(dimensionId);
                    }
                });
            }
        });

        // Spawn position
        protocol.registerOutgoing(State.PLAY, 0x49, 0x4A, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION, Type.POSITION1_14);
            }
        });
    }

}
