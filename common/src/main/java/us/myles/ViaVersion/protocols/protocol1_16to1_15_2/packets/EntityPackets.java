package us.myles.ViaVersion.protocols.protocol1_16to1_15_2.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.entities.Entity1_16Types;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.remapper.ValueTransformer;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_14;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.Protocol1_16To1_15_2;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.data.MappingData;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.metadata.MetadataRewriter1_16To1_15_2;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.storage.EntityTracker1_16;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

public class EntityPackets {

    private static final PacketHandler DIMENSION_HANDLER = wrapper -> {
        int dimension = wrapper.read(Type.INT);
        String dimensionName;
        switch (dimension) {
            case -1:
                dimensionName = "minecraft:the_nether";
                break;
            case 0:
                dimensionName = "minecraft:overworld";
                break;
            case 1:
                dimensionName = "minecraft:the_end";
                break;
            default:
                Via.getPlatform().getLogger().warning("Invalid dimension id: " + dimension);
                dimensionName = "minecraft:overworld";
        }

        wrapper.write(Type.STRING, dimensionName); // dimension type
        wrapper.write(Type.STRING, dimensionName); // dimension
    };
    private static final CompoundTag DIMENSIONS_TAG = new CompoundTag("");
    private static final String[] STRINGS = new String[0];

    static {
        ListTag list = new ListTag("dimension", CompoundTag.class);
        list.add(createDimensionEntry("minecraft:overworld"));
        list.add(createDimensionEntry("minecraft:the_nether"));
        list.add(createDimensionEntry("minecraft:the_end"));
        DIMENSIONS_TAG.put(list);
    }

    private static CompoundTag createDimensionEntry(String dimension) {
        CompoundTag tag = new CompoundTag("");
        tag.put(new StringTag("key", dimension));
        tag.put(new StringTag("element", dimension));
        return tag;
    }

    public static void register(Protocol protocol) {
        MetadataRewriter1_16To1_15_2 metadataRewriter = protocol.get(MetadataRewriter1_16To1_15_2.class);

        // Spawn entity
        metadataRewriter.registerSpawnTrackerWithData(0x00, 0x00, Entity1_16Types.EntityType.FALLING_BLOCK, Protocol1_16To1_15_2::getNewBlockStateId);

        // Spawn mob packet
        metadataRewriter.registerTracker(0x03, 0x03);

        // Spawn player packet
        metadataRewriter.registerTracker(0x05, 0x05, Entity1_16Types.EntityType.PLAYER);

        // Metadata
        metadataRewriter.registerMetadataRewriter(0x44, 0x45, Types1_14.METADATA_LIST);

        // Entity Destroy
        metadataRewriter.registerEntityDestroy(0x38, 0x38);

        // Respawn
        protocol.registerOutgoing(State.PLAY, 0x3B, 0x3B, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(DIMENSION_HANDLER);
                map(Type.LONG);
                map(Type.UNSIGNED_BYTE);
                handler(wrapper -> {
                    ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                    String dimensionId = wrapper.get(Type.STRING, 0);
                    clientWorld.setEnvironment(dimensionId);

                    String levelType = wrapper.read(Type.STRING);
                    wrapper.write(Type.BOOLEAN, false); // debug
                    wrapper.write(Type.BOOLEAN, levelType.equals("flat"));
                    wrapper.write(Type.BOOLEAN, true); // keep all playerdata
                });
            }
        });

        // Join Game
        protocol.registerOutgoing(State.PLAY, 0x26, 0x26, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // Entity ID
                map(Type.UNSIGNED_BYTE); //  Gamemode
                map(Type.NOTHING, new ValueTransformer<Void, String[]>(Type.STRING_ARRAY) { // World list
                    @Override
                    public String[] transform(PacketWrapper wrapper, Void input) throws Exception {
                        return STRINGS;
                    }
                });
                map(Type.NOTHING, new ValueTransformer<Void, CompoundTag>(Type.NBT) { // whatever this is
                    @Override
                    public CompoundTag transform(PacketWrapper wrapper, Void input) throws Exception {
                        return DIMENSIONS_TAG;
                    }
                });
                handler(DIMENSION_HANDLER); // Dimension
                map(Type.LONG); // Seed
                map(Type.UNSIGNED_BYTE); // Max players
                handler(wrapper -> {
                    ClientWorld clientChunks = wrapper.user().get(ClientWorld.class);
                    String dimension = wrapper.get(Type.STRING, 0);
                    clientChunks.setEnvironment(dimension);

                    wrapper.user().get(EntityTracker1_16.class).addEntity(wrapper.get(Type.INT, 0), Entity1_16Types.EntityType.PLAYER);

                    final String type = wrapper.read(Type.STRING);// level type
                    wrapper.passthrough(Type.VAR_INT); // View distance
                    wrapper.passthrough(Type.BOOLEAN); // Reduced debug info
                    wrapper.passthrough(Type.BOOLEAN); // Show death screen

                    wrapper.write(Type.BOOLEAN, false); // Debug
                    wrapper.write(Type.BOOLEAN, type.equals("flat"));
                });
            }
        });

        // Entity Properties
        protocol.registerOutgoing(State.PLAY, 0x59, 0x59, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    wrapper.passthrough(Type.VAR_INT);
                    int size = wrapper.passthrough(Type.INT);
                    for (int i = 0; i < size; i++) {
                        // Attributes have been renamed and are now namespaced identifiers
                        String key = wrapper.read(Type.STRING);
                        String attributeIdentifier = MappingData.attributeMappings.get(key);
                        if (attributeIdentifier == null) {
                            attributeIdentifier = "minecraft:" + key;
                            if (!us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.MappingData.isValid1_13Channel(attributeIdentifier)) {
                                Via.getPlatform().getLogger().warning("Invalid attribute: " + key);
                                wrapper.read(Type.DOUBLE);
                                int modifierSize = wrapper.read(Type.VAR_INT);
                                for (int j = 0; j < modifierSize; j++) {
                                    wrapper.read(Type.UUID);
                                    wrapper.read(Type.DOUBLE);
                                    wrapper.read(Type.BYTE);
                                }
                                continue;
                            }
                        }

                        wrapper.write(Type.STRING, attributeIdentifier);

                        wrapper.passthrough(Type.DOUBLE);
                        int modifierSize = wrapper.passthrough(Type.VAR_INT);
                        for (int j = 0; j < modifierSize; j++) {
                            wrapper.passthrough(Type.UUID);
                            wrapper.passthrough(Type.DOUBLE);
                            wrapper.passthrough(Type.BYTE);
                        }
                    }
                });
            }
        });
    }
}
