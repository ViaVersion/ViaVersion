package us.myles.ViaVersion.protocols.protocol1_16to1_15_2.packets;

import com.github.steveice10.opennbt.tag.builtin.ByteTag;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.FloatTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.LongTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.entities.Entity1_16Types;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_14;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.ClientboundPackets1_15;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.ClientboundPackets1_16;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.Protocol1_16To1_15_2;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.data.MappingData;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.metadata.MetadataRewriter1_16To1_15_2;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.storage.EntityTracker1_16;

import java.util.UUID;

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
    public static final CompoundTag DIMENSIONS_TAG = new CompoundTag("");
    private static final String[] WORLD_NAMES = {"minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"};

    static {
        ListTag list = new ListTag("dimension", CompoundTag.class);
        list.add(createOverworldEntry());
        list.add(createOverworldCavesEntry());
        list.add(createNetherEntry());
        list.add(createEndEntry());
        DIMENSIONS_TAG.put(list);
    }

    private static CompoundTag createOverworldEntry() {
        CompoundTag tag = new CompoundTag("");
        tag.put(new StringTag("name", "minecraft:overworld"));
        tag.put(new ByteTag("has_ceiling", (byte) 0));
        addSharedOverwaldEntries(tag);
        return tag;
    }

    private static CompoundTag createOverworldCavesEntry() {
        CompoundTag tag = new CompoundTag("");
        tag.put(new StringTag("name", "minecraft:overworld_caves"));
        tag.put(new ByteTag("has_ceiling", (byte) 1));
        addSharedOverwaldEntries(tag);
        return tag;
    }

    private static void addSharedOverwaldEntries(CompoundTag tag) {
        tag.put(new ByteTag("piglin_safe", (byte) 0));
        tag.put(new ByteTag("natural", (byte) 1));
        tag.put(new FloatTag("ambient_light", 0));
        tag.put(new StringTag("infiniburn", "minecraft:infiniburn_overworld"));
        tag.put(new ByteTag("respawn_anchor_works", (byte) 0));
        tag.put(new ByteTag("has_skylight", (byte) 1));
        tag.put(new ByteTag("bed_works", (byte) 1));
        tag.put(new ByteTag("has_raids", (byte) 1));
        tag.put(new IntTag("logical_height", 256));
        tag.put(new ByteTag("shrunk", (byte) 0));
        tag.put(new ByteTag("ultrawarm", (byte) 0));
    }

    private static CompoundTag createNetherEntry() {
        CompoundTag tag = new CompoundTag("");
        tag.put(new ByteTag("piglin_safe", (byte) 1));
        tag.put(new ByteTag("natural", (byte) 0));
        tag.put(new FloatTag("ambient_light", 0.1F));
        tag.put(new StringTag("infiniburn", "minecraft:infiniburn_nether"));
        tag.put(new ByteTag("respawn_anchor_works", (byte) 1));
        tag.put(new ByteTag("has_skylight", (byte) 0));
        tag.put(new ByteTag("bed_works", (byte) 0));
        tag.put(new LongTag("fixed_time", 18000));
        tag.put(new ByteTag("has_raids", (byte) 0));
        tag.put(new StringTag("name", "minecraft:the_nether"));
        tag.put(new IntTag("logical_height", 128));
        tag.put(new ByteTag("shrunk", (byte) 1));
        tag.put(new ByteTag("ultrawarm", (byte) 1));
        tag.put(new ByteTag("has_ceiling", (byte) 1));
        return tag;
    }

    private static CompoundTag createEndEntry() {
        CompoundTag tag = new CompoundTag("");
        tag.put(new ByteTag("piglin_safe", (byte) 0));
        tag.put(new ByteTag("natural", (byte) 0));
        tag.put(new FloatTag("ambient_light", 0));
        tag.put(new StringTag("infiniburn", "minecraft:infiniburn_end"));
        tag.put(new ByteTag("respawn_anchor_works", (byte) 0));
        tag.put(new ByteTag("has_skylight", (byte) 0));
        tag.put(new ByteTag("bed_works", (byte) 0));
        tag.put(new LongTag("fixed_time", 6000));
        tag.put(new ByteTag("has_raids", (byte) 1));
        tag.put(new StringTag("name", "minecraft:the_end"));
        tag.put(new IntTag("logical_height", 256));
        tag.put(new ByteTag("shrunk", (byte) 0));
        tag.put(new ByteTag("ultrawarm", (byte) 0));
        tag.put(new ByteTag("has_ceiling", (byte) 0));
        return tag;
    }

    public static void register(Protocol1_16To1_15_2 protocol) {
        MetadataRewriter1_16To1_15_2 metadataRewriter = protocol.get(MetadataRewriter1_16To1_15_2.class);

        // Spawn lightning -> Spawn entity
        protocol.registerOutgoing(ClientboundPackets1_15.SPAWN_GLOBAL_ENTITY, ClientboundPackets1_16.SPAWN_ENTITY, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    int entityId = wrapper.passthrough(Type.VAR_INT);
                    wrapper.user().get(EntityTracker1_16.class).addEntity(entityId, Entity1_16Types.EntityType.LIGHTNING_BOLT);

                    wrapper.write(Type.UUID, UUID.randomUUID()); // uuid
                    wrapper.write(Type.VAR_INT, Entity1_16Types.EntityType.LIGHTNING_BOLT.getId()); // entity type

                    wrapper.read(Type.BYTE); // remove type

                    wrapper.passthrough(Type.DOUBLE); // x
                    wrapper.passthrough(Type.DOUBLE); // y
                    wrapper.passthrough(Type.DOUBLE); // z
                    wrapper.write(Type.BYTE, (byte) 0); // yaw
                    wrapper.write(Type.BYTE, (byte) 0); // pitch
                    wrapper.write(Type.INT, 0); // data
                    wrapper.write(Type.SHORT, (short) 0); // velocity
                    wrapper.write(Type.SHORT, (short) 0); // velocity
                    wrapper.write(Type.SHORT, (short) 0); // velocity
                });
            }
        });

        metadataRewriter.registerSpawnTrackerWithData(ClientboundPackets1_15.SPAWN_ENTITY, Entity1_16Types.EntityType.FALLING_BLOCK, Protocol1_16To1_15_2::getNewBlockStateId);
        metadataRewriter.registerTracker(ClientboundPackets1_15.SPAWN_MOB);
        metadataRewriter.registerTracker(ClientboundPackets1_15.SPAWN_PLAYER, Entity1_16Types.EntityType.PLAYER);
        metadataRewriter.registerMetadataRewriter(ClientboundPackets1_15.ENTITY_METADATA, Types1_14.METADATA_LIST);
        metadataRewriter.registerEntityDestroy(ClientboundPackets1_15.DESTROY_ENTITIES);

        protocol.registerOutgoing(ClientboundPackets1_15.RESPAWN, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(DIMENSION_HANDLER);
                map(Type.LONG); // Seed
                map(Type.UNSIGNED_BYTE); // Gamemode
                handler(wrapper -> {
                    wrapper.write(Type.BYTE, (byte) -1); // Previous gamemode, set to none

                    String levelType = wrapper.read(Type.STRING);
                    wrapper.write(Type.BOOLEAN, false); // debug
                    wrapper.write(Type.BOOLEAN, levelType.equals("flat"));
                    wrapper.write(Type.BOOLEAN, true); // keep all playerdata
                });
            }
        });

        protocol.registerOutgoing(ClientboundPackets1_15.JOIN_GAME, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // Entity ID
                map(Type.UNSIGNED_BYTE); //  Gamemode
                handler(wrapper -> {
                    wrapper.write(Type.BYTE, (byte) -1); // Previous gamemode, set to none
                    wrapper.write(Type.STRING_ARRAY, WORLD_NAMES); // World list - only used for command completion
                    wrapper.write(Type.NBT, DIMENSIONS_TAG); // Dimension registry
                });
                handler(DIMENSION_HANDLER); // Dimension
                map(Type.LONG); // Seed
                map(Type.UNSIGNED_BYTE); // Max players
                handler(wrapper -> {
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

        protocol.registerOutgoing(ClientboundPackets1_15.ENTITY_PROPERTIES, new PacketRemapper() {
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
