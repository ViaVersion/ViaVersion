package us.myles.ViaVersion.protocols.protocol1_17to1_16_4;

import org.jetbrains.annotations.Nullable;
import us.myles.ViaVersion.api.data.MappingData;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.rewriters.RegistryType;
import us.myles.ViaVersion.api.rewriters.SoundRewriter;
import us.myles.ViaVersion.api.rewriters.StatisticsRewriter;
import us.myles.ViaVersion.api.rewriters.TagRewriter;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.ClientboundPackets1_16_2;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.ServerboundPackets1_16_2;
import us.myles.ViaVersion.protocols.protocol1_17to1_16_4.metadata.MetadataRewriter1_17To1_16_4;
import us.myles.ViaVersion.protocols.protocol1_17to1_16_4.packets.EntityPackets;
import us.myles.ViaVersion.protocols.protocol1_17to1_16_4.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocol1_17to1_16_4.packets.WorldPackets;
import us.myles.ViaVersion.protocols.protocol1_17to1_16_4.storage.BiomeStorage;
import us.myles.ViaVersion.protocols.protocol1_17to1_16_4.storage.EntityTracker1_17;

public class Protocol1_17To1_16_4 extends Protocol<ClientboundPackets1_16_2, ClientboundPackets1_17, ServerboundPackets1_16_2, ServerboundPackets1_16_2> {

    public static final MappingData MAPPINGS = new MappingData("1.16.2", "1.17", true);
    private static final String[] NEW_GAME_EVENT_TAGS = {"minecraft:ignore_vibrations_sneaking", "minecraft:vibrations"};
    private TagRewriter tagRewriter;

    public Protocol1_17To1_16_4() {
        super(ClientboundPackets1_16_2.class, ClientboundPackets1_17.class, ServerboundPackets1_16_2.class, ServerboundPackets1_16_2.class);
    }

    @Override
    protected void registerPackets() {
        new MetadataRewriter1_17To1_16_4(this);

        EntityPackets.register(this);
        InventoryPackets.register(this);
        WorldPackets.register(this);

        tagRewriter = new TagRewriter(this, null);
        registerOutgoing(ClientboundPackets1_16_2.TAGS, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    // Tags are now generically written with resource location - 5 different Vanilla types
                    wrapper.write(Type.VAR_INT, 5);
                    for (RegistryType type : RegistryType.getValues()) {
                        // Prefix with resource location
                        wrapper.write(Type.STRING, type.getResourceLocation());

                        // Id conversion
                        tagRewriter.handle(wrapper, tagRewriter.getRewriter(type), tagRewriter.getNewTags(type));

                        // Stop iterating after entity types
                        if (type == RegistryType.ENTITY) {
                            break;
                        }
                    }

                    // New Game Event tags type
                    wrapper.write(Type.STRING, RegistryType.GAME_EVENT.getResourceLocation());
                    wrapper.write(Type.VAR_INT, NEW_GAME_EVENT_TAGS.length);
                    for (String tag : NEW_GAME_EVENT_TAGS) {
                        wrapper.write(Type.STRING, tag);
                        wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[0]);
                    }
                });
            }
        });

        new StatisticsRewriter(this, null).register(ClientboundPackets1_16_2.STATISTICS);

        SoundRewriter soundRewriter = new SoundRewriter(this);
        soundRewriter.registerSound(ClientboundPackets1_16_2.SOUND);
        soundRewriter.registerSound(ClientboundPackets1_16_2.ENTITY_SOUND);

        registerOutgoing(ClientboundPackets1_16_2.RESOURCE_PACK, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    wrapper.passthrough(Type.STRING);
                    wrapper.passthrough(Type.STRING);
                    wrapper.write(Type.BOOLEAN, false); // Required
                });
            }
        });

        registerOutgoing(ClientboundPackets1_16_2.MAP_DATA, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    wrapper.passthrough(Type.VAR_INT);
                    wrapper.passthrough(Type.BYTE);
                    wrapper.read(Type.BOOLEAN); // Tracking position removed
                    wrapper.passthrough(Type.BOOLEAN);

                    int size = wrapper.read(Type.VAR_INT);
                    // Write whether markers exists or not
                    if (size != 0) {
                        wrapper.write(Type.BOOLEAN, true);
                        wrapper.write(Type.VAR_INT, size);
                    } else {
                        wrapper.write(Type.BOOLEAN, false);
                    }
                });
            }
        });
    }

    @Override
    protected void onMappingDataLoaded() {
        tagRewriter.addEmptyTags(RegistryType.ITEM, "minecraft:candles", "minecraft:ignored_by_piglin_babies", "minecraft:piglin_food", "minecraft:freeze_immune_wearables",
                "minecraft:axolotl_tempt_items", "minecraft:occludes_vibration_signals");
        tagRewriter.addEmptyTags(RegistryType.BLOCK, "minecraft:crystal_sound_blocks", "minecraft:candle_cakes", "minecraft:candles",
                "minecraft:snow_step_sound_blocks", "minecraft:inside_step_sound_blocks", "minecraft:occludes_vibration_signals", "minecraft:dripstone_replaceable_blocks",
                "azalea_log_replaceable", "cave_vines", "lush_plants_replaceable");
        tagRewriter.addEmptyTags(RegistryType.ENTITY, "minecraft:powder_snow_walkable_mobs", "minecraft:axolotl_always_hostiles", "minecraft:axolotl_tempted_hostiles");

        tagRewriter.addTag(RegistryType.BLOCK, "minecraft:cauldrons", 261);
        tagRewriter.addTag(RegistryType.ITEM, "minecraft:fox_food", 948);
    }

    @Override
    public void init(UserConnection user) {
        user.put(new BiomeStorage(user));
        user.put(new EntityTracker1_17(user));
    }

    @Override
    @Nullable
    public MappingData getMappingData() {
        return MAPPINGS;
    }
}
