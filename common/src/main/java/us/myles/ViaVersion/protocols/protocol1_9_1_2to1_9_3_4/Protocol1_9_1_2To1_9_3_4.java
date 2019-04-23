package us.myles.ViaVersion.protocols.protocol1_9_1_2to1_9_3_4;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_9_1_2to1_9_3_4.chunks.BlockEntity;
import us.myles.ViaVersion.protocols.protocol1_9_1_2to1_9_3_4.types.Chunk1_9_3_4Type;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.types.Chunk1_9_1_2Type;

public class Protocol1_9_1_2To1_9_3_4 extends Protocol {

    @Override
    protected void registerPackets() {

        //Unchanged packet structure
        registerOutgoing(State.PLAY, 0x46, 0x47); //Sound effect
        registerOutgoing(State.PLAY, 0x47, 0x48); //Player list header and footer
        registerOutgoing(State.PLAY, 0x48, 0x49); //Collect item
        registerOutgoing(State.PLAY, 0x49, 0x4A); //Entity teleport
        registerOutgoing(State.PLAY, 0x4A, 0x4B); //Entity properties
        registerOutgoing(State.PLAY, 0x4B, 0x4C); //Entity effect

        //Update block entity
        registerOutgoing(State.PLAY, 0x09, 0x09, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION); //Position
                map(Type.UNSIGNED_BYTE); //Type
                map(Type.NBT); //NBT
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        if (wrapper.get(Type.UNSIGNED_BYTE, 0) == 9) {
                            Position position = wrapper.get(Type.POSITION, 0);
                            CompoundTag tag = wrapper.get(Type.NBT, 0);

                            wrapper.clearPacket(); //Clear the packet

                            wrapper.setId(0x46); //Update sign packet
                            wrapper.write(Type.POSITION, position); // Position
                            for (int i = 1; i < 5; i++)
                                wrapper.write(Type.STRING, (String) tag.get("Text" + i).getValue()); // Sign line
                        }
                    }
                });
            }
        });

        // Chunk Packet
        registerOutgoing(State.PLAY, 0x20, 0x20, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);

                        Chunk1_9_3_4Type newType = new Chunk1_9_3_4Type(clientWorld);
                        Chunk1_9_1_2Type oldType = new Chunk1_9_1_2Type(clientWorld); // Get the old type to not write Block Entities

                        Chunk chunk = wrapper.read(newType);
                        wrapper.write(oldType, chunk);
                        BlockEntity.handle(chunk.getBlockEntities(), wrapper.user());
                    }
                });
            }
        });

        // Join (save dimension id)
        registerOutgoing(State.PLAY, 0x23, 0x23, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // 0 - Entity ID
                map(Type.UNSIGNED_BYTE); // 1 - Gamemode
                map(Type.INT); // 2 - Dimension

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        ClientWorld clientChunks = wrapper.user().get(ClientWorld.class);

                        int dimensionId = wrapper.get(Type.INT, 1);
                        clientChunks.setEnvironment(dimensionId);
                    }
                });
            }
        });

        // Respawn (save dimension id)
        registerOutgoing(State.PLAY, 0x33, 0x33, new PacketRemapper() {
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
    }

    @Override
    public void init(UserConnection userConnection) {
        if (!userConnection.has(ClientWorld.class))
            userConnection.put(new ClientWorld(userConnection));
    }
}
