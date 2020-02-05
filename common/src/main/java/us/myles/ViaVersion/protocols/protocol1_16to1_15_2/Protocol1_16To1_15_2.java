package us.myles.ViaVersion.protocols.protocol1_16to1_15_2;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.data.MappingData;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.metadata.MetadataRewriter1_16To1_15_2;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.packets.EntityPackets;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.packets.WorldPackets;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.storage.EntityTracker1_16;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

public class Protocol1_16To1_15_2 extends Protocol {

    @Override
    protected void registerPackets() {
        new MetadataRewriter1_16To1_15_2(this);

        MappingData.init();
        EntityPackets.register(this);
        WorldPackets.register(this);
        InventoryPackets.register(this);

        // Entity Sound Effect
        registerOutgoing(State.PLAY, 0x51, 0x51, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // Sound Id
                handler(wrapper -> wrapper.set(Type.VAR_INT, 0, MappingData.soundMappings.getNewId(wrapper.get(Type.VAR_INT, 0))));
            }
        });

        // Sound Effect
        registerOutgoing(State.PLAY, 0x52, 0x52, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // Sound Id
                handler(wrapper -> wrapper.set(Type.VAR_INT, 0, MappingData.soundMappings.getNewId(wrapper.get(Type.VAR_INT, 0))));
            }
        });

        // Edit Book
        registerIncoming(State.PLAY, 0x0C, 0x0C, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> InventoryPackets.toServer(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)));
            }
        });

        // Advancements
        registerOutgoing(State.PLAY, 0x58, 0x58, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
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
                });
            }
        });

        // Tags
        registerOutgoing(State.PLAY, 0x5C, 0x5C, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
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
                });
            }
        });
    }

    public static int getNewBlockStateId(int id) {
        int newId = MappingData.blockStateMappings.getNewId(id);
        if (newId == -1) {
            Via.getPlatform().getLogger().warning("Missing 1.16 blockstate for 1.15.2 blockstate " + id);
            return 0;
        }
        return newId;
    }

    public static int getNewBlockId(int id) {
        int newId = MappingData.blockMappings.getNewId(id);
        if (newId == -1) {
            Via.getPlatform().getLogger().warning("Missing 1.16 block for 1.15.2 block " + id);
            return 0;
        }
        return newId;
    }

    @Override
    public void init(UserConnection userConnection) {
        userConnection.put(new EntityTracker1_16(userConnection));
        if (!userConnection.has(ClientWorld.class))
            userConnection.put(new ClientWorld(userConnection));
    }
}
