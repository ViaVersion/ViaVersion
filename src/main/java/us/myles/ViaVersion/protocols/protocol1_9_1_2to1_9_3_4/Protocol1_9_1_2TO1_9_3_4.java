package us.myles.ViaVersion.protocols.protocol1_9_1_2to1_9_3_4;

import org.spacehq.opennbt.tag.builtin.CompoundTag;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;

public class Protocol1_9_1_2TO1_9_3_4 extends Protocol {
    public static Type<Chunk> CHUNK = new Chunk1_9_3_4Type();

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

        registerOutgoing(State.PLAY, 0x20, 0x20, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Chunk chunk = wrapper.passthrough(CHUNK);
                        BlockEntity.handle(((Chunk1_9_3_4) chunk).getBlockEntities(), wrapper.user());
                    }
                });
            }
        });
    }

    @Override
    public void init(UserConnection userConnection) {

    }
}
