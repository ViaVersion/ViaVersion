package us.myles.ViaVersion.protocols.protocol18w32ato1_13;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol18w32ato1_13.packets.EntityPackets;
import us.myles.ViaVersion.protocols.protocol18w32ato1_13.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocol18w32ato1_13.packets.WorldPackets;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.EntityTracker;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

public class Protocol18w32aTO1_13 extends Protocol {

    @Override
    protected void registerPackets() {
        EntityPackets.register(this);
        InventoryPackets.register(this);
        WorldPackets.register(this);

        //Tab complete
        registerIncoming(State.PLAY, 0x05, 0x05, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT);
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        String s = wrapper.passthrough(Type.STRING);
                        if(s.length() > 256){
                            wrapper.cancel();
                        }
                    }
                });
            }
        });

        //Edit Book
        registerIncoming(State.PLAY, 0x0B, 0x0B, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.FLAT_ITEM);
                map(Type.BOOLEAN);
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int hand = wrapper.read(Type.VAR_INT);
                        if(hand == 1){
                            wrapper.cancel();
                        }
                    }
                });
            }
        });

        //boss bar
        registerOutgoing(State.PLAY, 0x0C, 0x0C, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.UUID);
                map(Type.VAR_INT);
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int action = wrapper.get(Type.VAR_INT, 0);
                        if(action == 0){
                            wrapper.passthrough(Type.STRING);
                            wrapper.passthrough(Type.FLOAT);
                            wrapper.passthrough(Type.VAR_INT);
                            short flags = wrapper.read(Type.UNSIGNED_BYTE);
                            if ((flags & 0x02) != 0) flags |= 0x04;
                            wrapper.write(Type.UNSIGNED_BYTE, flags);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void init(UserConnection userConnection) {
        userConnection.put(new EntityTracker(userConnection));
        if (!userConnection.has(ClientWorld.class))
            userConnection.put(new ClientWorld(userConnection));
    }


    public static int getMapBlockId(int blockId) {
        if (blockId > 8573) {
            blockId += 17;
        } else if (blockId > 8463) {
            blockId += 16;
        } else if (blockId > 8458) {
            blockId = 8470 + (blockId - 8459) * 2;
        } else if (blockId > 1126) {
            blockId += 1;
        }

        return blockId;
    }
}
