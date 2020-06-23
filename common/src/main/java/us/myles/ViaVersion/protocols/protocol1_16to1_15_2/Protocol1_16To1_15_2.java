package us.myles.ViaVersion.protocols.protocol1_16to1_15_2;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.rewriters.SoundRewriter;
import us.myles.ViaVersion.api.rewriters.TagRewriter;
import us.myles.ViaVersion.api.rewriters.TagType;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.ServerboundPackets1_14;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.ClientboundPackets1_15;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.data.MappingData;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.metadata.MetadataRewriter1_16To1_15_2;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.packets.EntityPackets;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.packets.WorldPackets;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.storage.EntityTracker1_16;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import us.myles.ViaVersion.util.GsonUtil;

import java.util.UUID;

public class Protocol1_16To1_15_2 extends Protocol<ClientboundPackets1_15, ClientboundPackets1_16, ServerboundPackets1_14, ServerboundPackets1_16> {

    private static final UUID ZERO_UUID = new UUID(0, 0);
    private TagRewriter tagRewriter;

    public Protocol1_16To1_15_2() {
        super(ClientboundPackets1_15.class, ClientboundPackets1_16.class, ServerboundPackets1_14.class, ServerboundPackets1_16.class, true);
    }

    @Override
    protected void registerPackets() {
        MetadataRewriter1_16To1_15_2 metadataRewriter = new MetadataRewriter1_16To1_15_2(this);

        EntityPackets.register(this);
        WorldPackets.register(this);
        InventoryPackets.register(this);

        tagRewriter = new TagRewriter(this, Protocol1_16To1_15_2::getNewBlockId, InventoryPackets::getNewItemId, metadataRewriter::getNewEntityId);
        tagRewriter.register(ClientboundPackets1_15.TAGS);

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

        // Motd Status - line breaks are no longer allowed for player samples
        registerOutgoing(State.STATUS, 0x00, 0x00, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    String original = wrapper.read(Type.STRING);
                    JsonObject object = GsonUtil.getGson().fromJson(original, JsonObject.class);
                    JsonObject players = object.getAsJsonObject("players");
                    if (players == null) return;

                    JsonArray sample = players.getAsJsonArray("sample");
                    if (sample == null) return;

                    JsonArray splitSamples = new JsonArray();
                    for (JsonElement element : sample) {
                        JsonObject playerInfo = element.getAsJsonObject();
                        String name = playerInfo.getAsJsonPrimitive("name").getAsString();
                        if (name.indexOf('\n') == -1) {
                            splitSamples.add(playerInfo);
                            continue;
                        }

                        String id = playerInfo.getAsJsonPrimitive("id").getAsString();
                        for (String s : name.split("\n")) {
                            JsonObject newSample = new JsonObject();
                            newSample.addProperty("name", s);
                            newSample.addProperty("id", id);
                            splitSamples.add(newSample);
                        }
                    }

                    if (splitSamples.size() != sample.size()) {
                        players.add("sample", splitSamples);
                        wrapper.write(Type.STRING, object.toString());
                    } else {
                        wrapper.write(Type.STRING, original);
                    }
                });
            }
        });

        registerOutgoing(ClientboundPackets1_15.CHAT_MESSAGE, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.COMPONENT_STRING);
                map(Type.BYTE);
                handler(wrapper -> wrapper.write(Type.UUID, ZERO_UUID)); // sender uuid
            }
        });

        SoundRewriter soundRewriter = new SoundRewriter(this, id -> MappingData.soundMappings.getNewId(id));
        soundRewriter.registerSound(ClientboundPackets1_15.SOUND);
        soundRewriter.registerSound(ClientboundPackets1_15.ENTITY_SOUND);

        registerOutgoing(ClientboundPackets1_15.ADVANCEMENTS, new PacketRemapper() {
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

        registerIncoming(ServerboundPackets1_16.INTERACT_ENTITY, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    wrapper.passthrough(Type.VAR_INT); // Entity Id
                    int action = wrapper.passthrough(Type.VAR_INT);
                    if (action == 0 || action == 2) {
                        if (action == 2) {
                            // Location
                            wrapper.passthrough(Type.FLOAT);
                            wrapper.passthrough(Type.FLOAT);
                            wrapper.passthrough(Type.FLOAT);
                        }

                        wrapper.passthrough(Type.VAR_INT); // Hand
                    }

                    // New boolean: Whether the client is sneaking/pressing shift
                    wrapper.read(Type.BOOLEAN);
                });
            }
        });

        registerIncoming(ServerboundPackets1_16.PLAYER_ABILITIES, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    wrapper.passthrough(Type.BYTE);
                    // Flying and walking speed - not important anyways
                    wrapper.write(Type.FLOAT, 0.05F);
                    wrapper.write(Type.FLOAT, 0.1F);
                });
            }
        });

        cancelIncoming(ServerboundPackets1_16.GENERATE_JIGSAW);
        cancelIncoming(ServerboundPackets1_16.UPDATE_JIGSAW_BLOCK);
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
        tagRewriter.addTag(TagType.BLOCK, "minecraft:wooden_trapdoors", 193, 194, 195, 196, 197, 198);
        tagRewriter.addTag(TagType.ITEM, "minecraft:wooden_trapdoors", 215, 216, 217, 218, 219, 220);
        tagRewriter.addTag(TagType.ITEM, "minecraft:beacon_payment_items", 529, 530, 531, 760);
        tagRewriter.addTag(TagType.ENTITY, "minecraft:impact_projectiles", 2, 72, 71, 37, 69, 79, 83, 15, 93);

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
        if (!userConnection.has(ClientWorld.class)) {
            userConnection.put(new ClientWorld(userConnection));
        }
    }
}
