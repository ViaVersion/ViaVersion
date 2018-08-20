package us.myles.ViaVersion.protocols.protocol1_13_1_pre2to1_13;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_13_1_pre2to1_13.packets.EntityPackets;
import us.myles.ViaVersion.protocols.protocol1_13_1_pre2to1_13.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocol1_13_1_pre2to1_13.packets.WorldPackets;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.EntityTracker;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

public class Protocol13_1_PRE2TO1_13 extends Protocol {

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
                        if (s.length() > 256) {
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
                        if (hand == 1) {
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
                        if (action == 0) {
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

        // Advancements
        registerOutgoing(State.PLAY, 0x51, 0x51, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        wrapper.passthrough(Type.BOOLEAN); // Reset/clear
                        int size = wrapper.passthrough(Type.VAR_INT); // Mapping size

                        for (int i = 0; i < size; i++) {
                            wrapper.passthrough(Type.STRING); // Identifier

                            // Parent
                            if (wrapper.passthrough(Type.BOOLEAN))
                                wrapper.passthrough(Type.STRING);

                            // Display data
                            if (wrapper.passthrough(Type.BOOLEAN)) {
                                wrapper.passthrough(Type.STRING); // Title
                                wrapper.passthrough(Type.STRING); // Description
                                Item icon = wrapper.passthrough(Type.FLAT_ITEM);
                                InventoryPackets.toClient(icon);
                                wrapper.passthrough(Type.VAR_INT); // Frame type
                                int flags = wrapper.passthrough(Type.INT); // Flags
                                if ((flags & 1) != 0)
                                    wrapper.passthrough(Type.STRING); // Background texture
                                wrapper.passthrough(Type.FLOAT); // X
                                wrapper.passthrough(Type.FLOAT); // Y
                            }

                            wrapper.passthrough(Type.STRING_ARRAY); // Criteria

                            int arrayLength = wrapper.passthrough(Type.VAR_INT);
                            for (int array = 0; array < arrayLength; array++) {
                                wrapper.passthrough(Type.STRING_ARRAY); // String array
                            }
                        }
                    }
                });
            }
        });



        //Tags
        registerOutgoing(State.PLAY, 0x55, 0x55, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int blockTagsSize = wrapper.passthrough(Type.VAR_INT); // block tags
                        for (int i = 0; i < blockTagsSize; i++) {
                            wrapper.passthrough(Type.STRING);
                            Integer[] blocks = wrapper.passthrough(Type.VAR_INT_ARRAY);
                            for (int j = 0; j < blocks.length; j++) {
                                blocks[j] = getNewBlockId(blocks[j]);
                            }
                        }
                        int itemTagsSize = wrapper.passthrough(Type.VAR_INT); // item tags
                        for (int i = 0; i < itemTagsSize; i++) {
                            wrapper.passthrough(Type.STRING);
                            Integer[] items = wrapper.passthrough(Type.VAR_INT_ARRAY);
                            for (int j = 0; j < items.length; j++) {
                                items[j] = InventoryPackets.getNewItemId(items[j]);
                            }
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


    public static int getNewBlockStateId(int blockId) {
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

    public static int getNewBlockId(final int oldBlockId) {
        int blockId = oldBlockId;
        if (oldBlockId >= 561) {
            blockId += 5;
        }
        return blockId;
    }
}
