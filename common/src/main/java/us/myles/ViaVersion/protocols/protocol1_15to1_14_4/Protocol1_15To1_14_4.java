package us.myles.ViaVersion.protocols.protocol1_15to1_14_4;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.data.MappingData;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.metadata.MetadataRewriter1_15To1_14_4;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.packets.EntityPackets;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.packets.PlayerPackets;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.packets.WorldPackets;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.storage.EntityTracker1_15;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

public class Protocol1_15To1_14_4 extends Protocol {

    @Override
    protected void registerPackets() {
        new MetadataRewriter1_15To1_14_4(this);

        MappingData.init();
        EntityPackets.register(this);
        PlayerPackets.register(this);
        WorldPackets.register(this);
        InventoryPackets.register(this);

        // Entity Sound Effect (added somewhere in 1.14)
        registerOutgoing(State.PLAY, 0x50, 0x51, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // Sound Id
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        wrapper.set(Type.VAR_INT, 0, MappingData.soundMappings.getNewId(wrapper.get(Type.VAR_INT, 0)));
                    }
                });
            }
        });

        // Sound Effect
        registerOutgoing(State.PLAY, 0x51, 0x52, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // Sound Id
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        wrapper.set(Type.VAR_INT, 0, MappingData.soundMappings.getNewId(wrapper.get(Type.VAR_INT, 0)));
                    }
                });
            }
        });

        // Edit Book
        registerIncoming(State.PLAY, 0x0C, 0x0C, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        InventoryPackets.toServer(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                    }
                });
            }
        });

        // Advancements
        registerOutgoing(State.PLAY, 0x57, 0x58, new PacketRemapper() {
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
                                InventoryPackets.toClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Icon
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

        // Tags
        registerOutgoing(State.PLAY, 0x5B, 0x5C, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        //TODO do the new (flower) tags have to be sent?
                        int blockTagsSize = wrapper.passthrough(Type.VAR_INT);
                        for (int i = 0; i < blockTagsSize; i++) {
                            wrapper.passthrough(Type.STRING);
                            int[] blockIds = wrapper.passthrough(Type.VAR_INT_ARRAY_PRIMITIVE);
                            for (int j = 0; j < blockIds.length; j++) {
                                blockIds[j] = getNewBlockId(blockIds[j]);
                            }
                        }

                        int itemTagsSize = wrapper.passthrough(Type.VAR_INT);
                        for (int i = 0; i < itemTagsSize; i++) {
                            wrapper.passthrough(Type.STRING);
                            int[] itemIds = wrapper.passthrough(Type.VAR_INT_ARRAY_PRIMITIVE);
                            for (int j = 0; j < itemIds.length; j++) {
                                itemIds[j] = InventoryPackets.getNewItemId(itemIds[j]);
                            }
                        }

                        int fluidTagsSize = wrapper.passthrough(Type.VAR_INT); // fluid tags
                        for (int i = 0; i < fluidTagsSize; i++) {
                            wrapper.passthrough(Type.STRING);
                            wrapper.passthrough(Type.VAR_INT_ARRAY_PRIMITIVE);
                        }

                        int entityTagsSize = wrapper.passthrough(Type.VAR_INT); // entity tags
                        for (int i = 0; i < entityTagsSize; i++) {
                            wrapper.passthrough(Type.STRING);
                            int[] entitIds = wrapper.passthrough(Type.VAR_INT_ARRAY_PRIMITIVE);
                            for (int j = 0; j < entitIds.length; j++) {
                                entitIds[j] = EntityPackets.getNewEntityId(entitIds[j]);
                            }
                        }
                    }
                });
            }
        });


        registerOutgoing(State.PLAY, 0x08, 0x09);
        registerOutgoing(State.PLAY, 0x09, 0x0A);

        registerOutgoing(State.PLAY, 0x0C, 0x0D);
        registerOutgoing(State.PLAY, 0x0D, 0x0E);
        registerOutgoing(State.PLAY, 0x0E, 0x0F);
        registerOutgoing(State.PLAY, 0x10, 0x11);
        registerOutgoing(State.PLAY, 0x11, 0x12);
        registerOutgoing(State.PLAY, 0x12, 0x13);
        registerOutgoing(State.PLAY, 0x13, 0x14);

        registerOutgoing(State.PLAY, 0x15, 0x16);

        registerOutgoing(State.PLAY, 0x18, 0x19);
        registerOutgoing(State.PLAY, 0x19, 0x1A);
        registerOutgoing(State.PLAY, 0x1A, 0x1B);
        registerOutgoing(State.PLAY, 0x1B, 0x1C);
        registerOutgoing(State.PLAY, 0x1C, 0x1D);
        registerOutgoing(State.PLAY, 0x1D, 0x1E);
        registerOutgoing(State.PLAY, 0x1E, 0x1F);
        registerOutgoing(State.PLAY, 0x1F, 0x20);
        registerOutgoing(State.PLAY, 0x20, 0x21);


        registerOutgoing(State.PLAY, 0x24, 0x25);

        registerOutgoing(State.PLAY, 0x26, 0x27);

        registerOutgoing(State.PLAY, 0x28, 0x29);
        registerOutgoing(State.PLAY, 0x29, 0x2A);
        registerOutgoing(State.PLAY, 0x2A, 0x2B);
        registerOutgoing(State.PLAY, 0x2B, 0x2C);
        registerOutgoing(State.PLAY, 0x2C, 0x2D);
        registerOutgoing(State.PLAY, 0x2D, 0x2E);
        registerOutgoing(State.PLAY, 0x2E, 0x2F);
        registerOutgoing(State.PLAY, 0x2F, 0x30);
        registerOutgoing(State.PLAY, 0x30, 0x31);
        registerOutgoing(State.PLAY, 0x31, 0x32);
        registerOutgoing(State.PLAY, 0x32, 0x33);
        registerOutgoing(State.PLAY, 0x33, 0x34);
        registerOutgoing(State.PLAY, 0x34, 0x35);
        registerOutgoing(State.PLAY, 0x35, 0x36);
        registerOutgoing(State.PLAY, 0x36, 0x37);

        registerOutgoing(State.PLAY, 0x38, 0x39);
        registerOutgoing(State.PLAY, 0x39, 0x3A);
        registerOutgoing(State.PLAY, 0x3B, 0x3C);
        registerOutgoing(State.PLAY, 0x3C, 0x3D);
        registerOutgoing(State.PLAY, 0x3D, 0x3E);
        registerOutgoing(State.PLAY, 0x3E, 0x3F);
        registerOutgoing(State.PLAY, 0x3F, 0x40);
        registerOutgoing(State.PLAY, 0x40, 0x41);
        registerOutgoing(State.PLAY, 0x41, 0x42);
        registerOutgoing(State.PLAY, 0x42, 0x43);

        registerOutgoing(State.PLAY, 0x44, 0x45);
        registerOutgoing(State.PLAY, 0x45, 0x46);

        registerOutgoing(State.PLAY, 0x47, 0x48);
        registerOutgoing(State.PLAY, 0x48, 0x49);
        registerOutgoing(State.PLAY, 0x49, 0x4A);
        registerOutgoing(State.PLAY, 0x4A, 0x4B);
        registerOutgoing(State.PLAY, 0x4B, 0x4C);
        registerOutgoing(State.PLAY, 0x4C, 0x4D);
        registerOutgoing(State.PLAY, 0x4D, 0x4E);
        registerOutgoing(State.PLAY, 0x4E, 0x4F);
        registerOutgoing(State.PLAY, 0x4F, 0x50);


        registerOutgoing(State.PLAY, 0x52, 0x53);
        registerOutgoing(State.PLAY, 0x53, 0x54);
        registerOutgoing(State.PLAY, 0x54, 0x55);
        registerOutgoing(State.PLAY, 0x55, 0x56);
        registerOutgoing(State.PLAY, 0x56, 0x57);

        registerOutgoing(State.PLAY, 0x58, 0x59);
        registerOutgoing(State.PLAY, 0x59, 0x5A);
    }

    public static int getNewBlockStateId(int id) {
        int newId = MappingData.blockStateMappings.getNewId(id);
        if (newId == -1) {
            Via.getPlatform().getLogger().warning("Missing 1.15 blockstate for 1.14.4 blockstate " + id);
            return 0;
        }
        return newId;
    }

    public static int getNewBlockId(int id) {
        int newId = MappingData.blockMappings.getNewId(id);
        if (newId == -1) {
            Via.getPlatform().getLogger().warning("Missing 1.15 block for 1.14.4 block " + id);
            return 0;
        }
        return newId;
    }

    @Override
    public void init(UserConnection userConnection) {
        userConnection.put(new EntityTracker1_15(userConnection));
        if (!userConnection.has(ClientWorld.class))
            userConnection.put(new ClientWorld(userConnection));
    }
}
