package us.myles.ViaVersion.protocols.protocol1_14to1_13_2;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data.MappingData;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets.EntityPackets;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets.PlayerPackets;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets.WorldPackets;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.storage.EntityTracker;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

public class Protocol1_14To1_13_2 extends Protocol {

    static {
        MappingData.init();
    }

    @Override
    protected void registerPackets() {
        InventoryPackets.register(this);
        EntityPackets.register(this);
        WorldPackets.register(this);
        PlayerPackets.register(this);

        registerOutgoing(State.PLAY, 0x2D, 0x2E);
        registerOutgoing(State.PLAY, 0x2E, 0x2F);
        registerOutgoing(State.PLAY, 0x2F, 0x30);
        registerOutgoing(State.PLAY, 0x30, 0x31);
        registerOutgoing(State.PLAY, 0x31, 0x32);
        registerOutgoing(State.PLAY, 0x32, 0x33);

        registerOutgoing(State.PLAY, 0x34, 0x35);

        registerOutgoing(State.PLAY, 0x36, 0x37);
        registerOutgoing(State.PLAY, 0x37, 0x38);

        registerOutgoing(State.PLAY, 0x39, 0x3A);
        registerOutgoing(State.PLAY, 0x3A, 0x3B);
        registerOutgoing(State.PLAY, 0x3B, 0x3C);
        registerOutgoing(State.PLAY, 0x3C, 0x3D);
        registerOutgoing(State.PLAY, 0x3D, 0x3E);
        registerOutgoing(State.PLAY, 0x3E, 0x3F);

        registerOutgoing(State.PLAY, 0x40, 0x41);
        registerOutgoing(State.PLAY, 0x41, 0x42);

        registerOutgoing(State.PLAY, 0x43, 0x44);
        registerOutgoing(State.PLAY, 0x44, 0x45);
        registerOutgoing(State.PLAY, 0x45, 0x46);
        registerOutgoing(State.PLAY, 0x46, 0x47);
        registerOutgoing(State.PLAY, 0x47, 0x48);
        registerOutgoing(State.PLAY, 0x48, 0x49);

        registerOutgoing(State.PLAY, 0x4A, 0x4B);
        registerOutgoing(State.PLAY, 0x4B, 0x4C);
        registerOutgoing(State.PLAY, 0x4C, 0x4D);

        // Sound Effect
        registerOutgoing(State.PLAY, 0x4D, 0x4E, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // Sound Id
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        wrapper.set(Type.VAR_INT, 0, getNewSoundId(wrapper.get(Type.VAR_INT, 0)));
                    }
                });
            }
        });
        registerOutgoing(State.PLAY, 0x4E, 0x50);
        registerOutgoing(State.PLAY, 0x4F, 0x51);
        registerOutgoing(State.PLAY, 0x50, 0x52);

        registerOutgoing(State.PLAY, 0x51, 0x53, new PacketRemapper() {
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

        registerOutgoing(State.PLAY, 0x52, 0x54);
        registerOutgoing(State.PLAY, 0x53, 0x55);

        registerOutgoing(State.PLAY, 0x55, 0x57, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int blockTagsSize = wrapper.read(Type.VAR_INT);
                        wrapper.write(Type.VAR_INT, blockTagsSize + 3); // block tags
                        for (int i = 0; i < blockTagsSize; i++) {
                            wrapper.passthrough(Type.STRING);
                            Integer[] blockIds = wrapper.passthrough(Type.VAR_INT_ARRAY);
                            for (int j = 0; j < blockIds.length; j++) {
                                blockIds[j] = getNewBlockId(blockIds[j]);
                            }
                        }
                        // Minecraft crashes if we not send signs tags
                        wrapper.write(Type.STRING, "minecraft:signs");
                        wrapper.write(Type.VAR_INT_ARRAY, new Integer[]{
                                getNewBlockId(150), getNewBlockId(155)
                        });
                        wrapper.write(Type.STRING, "minecraft:wall_signs");
                        wrapper.write(Type.VAR_INT_ARRAY, new Integer[]{
                                getNewBlockId(155)
                        });
                        wrapper.write(Type.STRING, "minecraft:standing_signs");
                        wrapper.write(Type.VAR_INT_ARRAY, new Integer[]{
                                getNewBlockId(150)
                        });
                        int itemTagsSize = wrapper.read(Type.VAR_INT);
                        wrapper.write(Type.VAR_INT, itemTagsSize + 1); // item tags
                        for (int i = 0; i < itemTagsSize; i++) {
                            wrapper.passthrough(Type.STRING);
                            Integer[] itemIds = wrapper.passthrough(Type.VAR_INT_ARRAY);
                            for (int j = 0; j < itemIds.length; j++) {
                                itemIds[j] = InventoryPackets.getNewItemId(itemIds[j]);
                            }
                        }
                        // Should fix fuel shift clicking
                        wrapper.write(Type.STRING, "minecraft:signs");
                        wrapper.write(Type.VAR_INT_ARRAY, new Integer[]{
                                InventoryPackets.getNewItemId(541)
                        });
                        int fluidTagsSize = wrapper.passthrough(Type.VAR_INT); // fluid tags
                        for (int i = 0; i < fluidTagsSize; i++) {
                            wrapper.passthrough(Type.STRING);
                            wrapper.passthrough(Type.VAR_INT_ARRAY);
                        }
                        wrapper.write(Type.VAR_INT, 0);  // new entity tags - do we need to send this?
                    }
                });
            }
        });
    }

    public static int getNewSoundId(int id) {
        int newId = MappingData.soundMappings.getNewSound(id);
        if (newId == -1) {
            Via.getPlatform().getLogger().warning("Missing 1.14 sound for 1.13.2 sound " + id);
            return 0;
        }
        return newId;
    }

    public static int getNewBlockStateId(int id) {
        int newId = MappingData.blockStateMappings.getNewBlock(id);
        if (newId == -1) {
            Via.getPlatform().getLogger().warning("Missing 1.14 blockstate for 1.13.2 blockstate " + id);
            return 0;
        }
        return newId;
    }

    public static int getNewBlockId(int id) {
        int newId = MappingData.blockMappings.getNewBlock(id);
        if (newId == -1) {
            Via.getPlatform().getLogger().warning("Missing 1.14 block for 1.13.2 block " + id);
            return 0;
        }
        return newId;
    }

    @Override
    public void init(UserConnection userConnection) {
        userConnection.put(new EntityTracker(userConnection));
        if (!userConnection.has(ClientWorld.class))
            userConnection.put(new ClientWorld(userConnection));

    }
}
