package us.myles.ViaVersion.protocols.protocol18w33ato1_13.packets;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.minecraft.BlockChangeRecord;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol18w32ato1_13.Protocol18w33aTO1_13;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.types.Chunk1_13Type;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

public class WorldPackets {

    public static void register(Protocol protocol) {
        //Chunk
        protocol.registerOutgoing(State.PLAY, 0x22, 0x22, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                        Chunk chunk = wrapper.passthrough(new Chunk1_13Type(clientWorld));

                        for (int i = 0; i < chunk.getSections().length; i++) {
                            ChunkSection section = chunk.getSections()[i];
                            if (section == null)
                                continue;

                            for (int x = 0; x < 16; x++) {
                                for (int y = 0; y < 16; y++) {
                                    for (int z = 0; z < 16; z++) {
                                        int block = section.getBlock(x, y, z);

                                        section.setFlatBlock(x, y, z, Protocol18w33aTO1_13.getMapBlockId(block));
                                    }
                                }
                            }
                        }

                    }
                });
            }
        });

        // Block Change
        protocol.registerOutgoing(State.PLAY, 0xB, 0xB, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION);
                map(Type.VAR_INT);
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int id = wrapper.get(Type.VAR_INT, 0);

                        wrapper.set(Type.VAR_INT, 0, Protocol18w33aTO1_13.getMapBlockId(id));
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
                            record.setBlockId(Protocol18w33aTO1_13.getMapBlockId(id));
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

        //respawn
        protocol.registerOutgoing(State.PLAY, 0x38, 0x38, new PacketRemapper() {
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
                        if(id == 3 || id == 20){
                            int data = wrapper.passthrough(Type.VAR_INT);
                            wrapper.set(Type.VAR_INT, 0, Protocol18w33aTO1_13.getMapBlockId(data));
                        }
                    }
                });
            }
        });
    }

}
