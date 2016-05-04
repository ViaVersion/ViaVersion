package us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2;

import org.spacehq.opennbt.tag.builtin.CompoundTag;
import org.spacehq.opennbt.tag.builtin.IntTag;
import org.spacehq.opennbt.tag.builtin.StringTag;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;

public class Protocol1_9_3TO1_9_1_2 extends Protocol {
    @Override
    protected void registerPackets() {

        //Unchanged packet structure
        registerOutgoing(State.PLAY, 0x47, 0x46); //Sound effect
        registerOutgoing(State.PLAY, 0x48, 0x47); //Player list header and footer
        registerOutgoing(State.PLAY, 0x49, 0x48); //Collect item
        registerOutgoing(State.PLAY, 0x4A, 0x49); //Entity teleport
        registerOutgoing(State.PLAY, 0x4B, 0x4A); //Entity properties
        registerOutgoing(State.PLAY, 0x4C, 0x4B); //Entity effect

        //Sign update
        registerOutgoing(State.PLAY, 0x46, -1, new PacketRemapper() {
            @Override
            public void registerMap() {

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        //read data
                        Position position = wrapper.read(Type.POSITION);
                        String[] lines = new String[4];
                        for (int i = 0; i < 4; i++)
                            lines[i] = wrapper.read(Type.STRING);

                        wrapper.clearInputBuffer();

                        //write data
                        wrapper.setId(0x09); //Update block entity
                        wrapper.write(Type.POSITION, position); //Block location
                        wrapper.write(Type.UNSIGNED_BYTE, (short) 9); //Action type (9 update sign)

                        //Create nbt
                        CompoundTag tag = new CompoundTag("");
                        tag.put(new StringTag("id", "Sign"));
                        tag.put(new IntTag("x", position.getX().intValue()));
                        tag.put(new IntTag("y", position.getY().intValue()));
                        tag.put(new IntTag("z", position.getZ().intValue()));
                        for (int i = 0; i < lines.length; i++)
                            tag.put(new StringTag("Text" + (i + 1), lines[i]));

                        wrapper.write(Type.NBT, tag);
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
                        wrapper.passthroughAll();
                        wrapper.write(Type.VAR_INT, 0);
                    }
                });
            }
        });
    }

    @Override
    public void init(UserConnection userConnection) {

    }
}
