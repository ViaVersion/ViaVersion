package us.myles.ViaVersion.protocols.protocol1_16to1_15_2;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.rewriters.SoundRewriter;
import us.myles.ViaVersion.api.rewriters.TagRewriter;
import us.myles.ViaVersion.api.rewriters.TagType;
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

    public static final UUID ZERO_UUID = new UUID(0, 0);
    private TagRewriter tagRewriter;

    public Protocol1_16To1_15_2() {
        super(true);
    }

    @Override
    protected void registerPackets() {
        MetadataRewriter1_16To1_15_2 metadataRewriter = new MetadataRewriter1_16To1_15_2(this);

        EntityPackets.register(this);
        WorldPackets.register(this);
        InventoryPackets.register(this);

        tagRewriter = new TagRewriter(this, Protocol1_16To1_15_2::getNewBlockId, InventoryPackets::getNewItemId, metadataRewriter::getNewEntityId);
        tagRewriter.register(0x5C, 0x5B);

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

        // Chat Message
        registerOutgoing(State.PLAY, 0x0F, 0x0E, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING);
                map(Type.BYTE);
                handler(wrapper -> wrapper.write(Type.UUID, ZERO_UUID)); // sender uuid
            }
        });

        SoundRewriter soundRewriter = new SoundRewriter(this, id -> MappingData.soundMappings.getNewId(id));
        soundRewriter.registerSound(0x51, 0x50);
        soundRewriter.registerSound(0x52, 0x51);

        // Advancements
        registerOutgoing(State.PLAY, 0x58, 0x57, new PacketRemapper() {
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

        registerOutgoing(State.PLAY, 0x04, 0x03);
        registerOutgoing(State.PLAY, 0x06, 0x05);
        registerOutgoing(State.PLAY, 0x07, 0x06);
        registerOutgoing(State.PLAY, 0x09, 0x08);
        registerOutgoing(State.PLAY, 0x0A, 0x09);
        registerOutgoing(State.PLAY, 0x0D, 0x0C);
        registerOutgoing(State.PLAY, 0x0E, 0x0D);
        registerOutgoing(State.PLAY, 0x11, 0x10);
        registerOutgoing(State.PLAY, 0x12, 0x11);
        registerOutgoing(State.PLAY, 0x13, 0x12);
        registerOutgoing(State.PLAY, 0x14, 0x13);
        registerOutgoing(State.PLAY, 0x19, 0x18);
        registerOutgoing(State.PLAY, 0x1A, 0x19);
        registerOutgoing(State.PLAY, 0x1B, 0x1A);
        registerOutgoing(State.PLAY, 0x1C, 0x1B);
        registerOutgoing(State.PLAY, 0x1D, 0x1C);
        registerOutgoing(State.PLAY, 0x1E, 0x1D);
        registerOutgoing(State.PLAY, 0x1F, 0x1E);
        registerOutgoing(State.PLAY, 0x20, 0x1F);
        registerOutgoing(State.PLAY, 0x21, 0x20);
        registerOutgoing(State.PLAY, 0x25, 0x24);
        registerOutgoing(State.PLAY, 0x27, 0x26);
        registerOutgoing(State.PLAY, 0x29, 0x28);
        registerOutgoing(State.PLAY, 0x2A, 0x29);
        registerOutgoing(State.PLAY, 0x2B, 0x2A);
        registerOutgoing(State.PLAY, 0x2C, 0x2B);
        registerOutgoing(State.PLAY, 0x2D, 0x2C);
        registerOutgoing(State.PLAY, 0x2E, 0x2D);
        registerOutgoing(State.PLAY, 0x30, 0x2F);
        registerOutgoing(State.PLAY, 0x31, 0x30);
        registerOutgoing(State.PLAY, 0x32, 0x31);
        registerOutgoing(State.PLAY, 0x33, 0x32);
        registerOutgoing(State.PLAY, 0x34, 0x33);
        registerOutgoing(State.PLAY, 0x35, 0x34);
        registerOutgoing(State.PLAY, 0x36, 0x35);
        registerOutgoing(State.PLAY, 0x37, 0x36);
        registerOutgoing(State.PLAY, 0x39, 0x38);
        registerOutgoing(State.PLAY, 0x3A, 0x39);
        registerOutgoing(State.PLAY, 0x3C, 0x3B);
        registerOutgoing(State.PLAY, 0x3D, 0x3C);
        registerOutgoing(State.PLAY, 0x3E, 0x3D);
        registerOutgoing(State.PLAY, 0x3F, 0x3E);
        registerOutgoing(State.PLAY, 0x40, 0x3F);
        registerOutgoing(State.PLAY, 0x41, 0x40);
        registerOutgoing(State.PLAY, 0x42, 0x41);
        registerOutgoing(State.PLAY, 0x4E, 0x42);
        registerOutgoing(State.PLAY, 0x4F, 0x4E);
        registerOutgoing(State.PLAY, 0x50, 0x4F);
        registerOutgoing(State.PLAY, 0x53, 0x52);
        registerOutgoing(State.PLAY, 0x54, 0x53);
        registerOutgoing(State.PLAY, 0x55, 0x54);
        registerOutgoing(State.PLAY, 0x56, 0x55);
        registerOutgoing(State.PLAY, 0x57, 0x56);
        registerOutgoing(State.PLAY, 0x5A, 0x59);

        cancelIncoming(State.PLAY, 0x0F); // Generate jisaw
        cancelIncoming(State.PLAY, 0x28); // Jigsaw update
        registerIncoming(State.PLAY, 0x0F, 0x10);
        registerIncoming(State.PLAY, 0x10, 0x11);
        registerIncoming(State.PLAY, 0x11, 0x12);
        registerIncoming(State.PLAY, 0x12, 0x13);
        registerIncoming(State.PLAY, 0x13, 0x14);
        registerIncoming(State.PLAY, 0x14, 0x15);
        registerIncoming(State.PLAY, 0x15, 0x16);
        registerIncoming(State.PLAY, 0x16, 0x17);
        registerIncoming(State.PLAY, 0x17, 0x18);
        registerIncoming(State.PLAY, 0x18, 0x19);
        registerIncoming(State.PLAY, 0x19, 0x1A);
        registerIncoming(State.PLAY, 0x1A, 0x1B);
        registerIncoming(State.PLAY, 0x1B, 0x1C);
        registerIncoming(State.PLAY, 0x1C, 0x1D);
        registerIncoming(State.PLAY, 0x1D, 0x1E);
        registerIncoming(State.PLAY, 0x1E, 0x1F);
        registerIncoming(State.PLAY, 0x1F, 0x20);
        registerIncoming(State.PLAY, 0x20, 0x21);
        registerIncoming(State.PLAY, 0x21, 0x22);
        registerIncoming(State.PLAY, 0x22, 0x23);
        registerIncoming(State.PLAY, 0x23, 0x24);
        registerIncoming(State.PLAY, 0x24, 0x25);
        registerIncoming(State.PLAY, 0x28, 0x29);
        registerIncoming(State.PLAY, 0x29, 0x2A);
        registerIncoming(State.PLAY, 0x2A, 0x2B);
        registerIncoming(State.PLAY, 0x2B, 0x2C);
        registerIncoming(State.PLAY, 0x2C, 0x2D);
        registerIncoming(State.PLAY, 0x2D, 0x2E);
    }

    @Override
    protected void loadMappingData() {
        MappingData.init();

        int[] wallPostOverrideTag = new int[47];
        int arrayIndex = 0;
        wallPostOverrideTag[arrayIndex++] = 140;
        wallPostOverrideTag[arrayIndex++] = 179;
        wallPostOverrideTag[arrayIndex++] = 264;
        for (int i = 153; i <= 158; i++) {
            wallPostOverrideTag[arrayIndex++] = i;
        }
        for (int i = 163; i <= 168; i++) {
            wallPostOverrideTag[arrayIndex++] = i;
        }
        for (int i = 408; i <= 439; i++) {
            wallPostOverrideTag[arrayIndex++] = i;
        }

        tagRewriter.addTag(TagType.BLOCK, "minecraft:wall_post_override", wallPostOverrideTag);
        tagRewriter.addTag(TagType.BLOCK, "minecraft:beacon_base_blocks", 133, 134, 148, 265);
        tagRewriter.addTag(TagType.BLOCK, "minecraft:climbable", 160, 241, 658);
        tagRewriter.addTag(TagType.BLOCK, "minecraft:fire", 142);
        tagRewriter.addTag(TagType.BLOCK, "minecraft:campfires", 679);
        tagRewriter.addTag(TagType.BLOCK, "minecraft:fence_gates", 242, 467, 468, 469, 470, 471);
        tagRewriter.addTag(TagType.BLOCK, "minecraft:unstable_bottom_center", 242, 467, 468, 469, 470, 471);
        tagRewriter.addTag(TagType.ITEM, "minecraft:beacon_payment_items", 529, 530, 531, 760);
        // The client crashes if we don't send all tags it may use
        tagRewriter.addEmptyTag(TagType.BLOCK, "minecraft:guarded_by_piglins");
        tagRewriter.addEmptyTag(TagType.BLOCK, "minecraft:soul_speed_blocks");
        tagRewriter.addEmptyTag(TagType.BLOCK, "minecraft:soul_fire_base_blocks");
        tagRewriter.addEmptyTag(TagType.BLOCK, "minecraft:non_flammable_wood");
        tagRewriter.addEmptyTag(TagType.ITEM, "minecraft:non_flammable_wood");
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
