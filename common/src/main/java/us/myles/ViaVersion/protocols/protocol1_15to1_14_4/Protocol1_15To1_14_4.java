package us.myles.ViaVersion.protocols.protocol1_15to1_14_4;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.rewriters.SoundRewriter;
import us.myles.ViaVersion.api.rewriters.TagRewriter;
import us.myles.ViaVersion.api.rewriters.TagType;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.ClientboundPackets1_14;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.ServerboundPackets1_14;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.data.MappingData;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.metadata.MetadataRewriter1_15To1_14_4;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.packets.EntityPackets;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.packets.PlayerPackets;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.packets.WorldPackets;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.storage.EntityTracker1_15;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

public class Protocol1_15To1_14_4 extends Protocol {

    private TagRewriter tagRewriter;

    public Protocol1_15To1_14_4() {
        super(ClientboundPackets1_14.class, ClientboundPackets1_15.class, ServerboundPackets1_14.class, ServerboundPackets1_14.class, true);
    }

    @Override
    protected void registerPackets() {
        new MetadataRewriter1_15To1_14_4(this);

        EntityPackets.register(this);
        PlayerPackets.register(this);
        WorldPackets.register(this);
        InventoryPackets.register(this);

        SoundRewriter soundRewriter = new SoundRewriter(this, id -> MappingData.soundMappings.getNewId(id));
        soundRewriter.registerSound(ClientboundPackets1_14.ENTITY_SOUND); // Entity Sound Effect (added somewhere in 1.14)
        soundRewriter.registerSound(ClientboundPackets1_14.SOUND);

        registerIncoming(ServerboundPackets1_14.EDIT_BOOK, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> InventoryPackets.toServer(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)));
            }
        });

        registerOutgoing(ClientboundPackets1_14.ADVANCEMENTS, new PacketRemapper() {
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

        tagRewriter = new TagRewriter(this, Protocol1_15To1_14_4::getNewBlockId, InventoryPackets::getNewItemId, EntityPackets::getNewEntityId);
        tagRewriter.register(ClientboundPackets1_14.TAGS);
    }

    @Override
    protected void loadMappingData() {
        MappingData.init();

        int[] shulkerBoxes = new int[17];
        int shulkerBoxOffset = 501;
        for (int i = 0; i < 17; i++) {
            shulkerBoxes[i] = shulkerBoxOffset + i;
        }
        tagRewriter.addTag(TagType.BLOCK, "minecraft:shulker_boxes", shulkerBoxes);
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
