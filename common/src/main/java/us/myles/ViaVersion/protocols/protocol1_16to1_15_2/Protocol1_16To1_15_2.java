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

import java.util.UUID;

public class Protocol1_16To1_15_2 extends Protocol {


    @Override
    public void loadMappings() {
        MappingData.init();
    }

    @Override
    protected void registerPackets() {
        new MetadataRewriter1_16To1_15_2(this);

        EntityPackets.register(this);
        WorldPackets.register(this);
        InventoryPackets.register(this);

        // Login Success
        registerOutgoing(State.LOGIN, 0x02, 0x02, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    // Transform string to int array
                    UUID uuid = UUID.fromString(wrapper.read(Type.STRING));
                    wrapper.write(Type.UUID_INT_ARRAY, uuid);
                });
            }
        });

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
                    int blockTagsSize = wrapper.read(Type.VAR_INT);
                    wrapper.write(Type.VAR_INT, blockTagsSize + 2); // new tag(s)

                    for (int i = 0; i < blockTagsSize; i++) {
                        wrapper.passthrough(Type.STRING);
                        int[] blockIds = wrapper.passthrough(Type.VAR_INT_ARRAY_PRIMITIVE);
                        for (int j = 0; j < blockIds.length; j++) {
                            blockIds[j] = getNewBlockId(blockIds[j]);
                        }
                    }

                    // Only send the necessary new tags
                    wrapper.write(Type.STRING, "minecraft:beacon_base_blocks");
                    wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[]{getNewBlockId(133), getNewBlockId(134), getNewBlockId(148), getNewBlockId(265)});
                    wrapper.write(Type.STRING, "minecraft:climbable");
                    wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[]{getNewBlockId(160), getNewBlockId(241), getNewBlockId(658)});

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

        registerOutgoing(State.PLAY, 0x43, 0x44);

        registerOutgoing(State.PLAY, 0x45, 0x46);
        registerOutgoing(State.PLAY, 0x46, 0x47);

        registerOutgoing(State.PLAY, 0x48, 0x49);
        registerOutgoing(State.PLAY, 0x49, 0x4A);
        registerOutgoing(State.PLAY, 0x4A, 0x4B);
        registerOutgoing(State.PLAY, 0x4B, 0x4C);
        registerOutgoing(State.PLAY, 0x4C, 0x4D);
        registerOutgoing(State.PLAY, 0x4D, 0x4E);
        registerOutgoing(State.PLAY, 0x4E, 0x43);
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
