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
import us.myles.ViaVersion.protocols.protocol1_17to1_16_4.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocol1_17to1_16_4.packets.WorldPackets;
import us.myles.ViaVersion.protocols.protocol1_17to1_16_4.storage.BiomeStorage;

public class Protocol1_17To1_16_4 extends Protocol<ClientboundPackets1_16_2, ClientboundPackets1_16_2, ServerboundPackets1_16_2, ServerboundPackets1_16_2> {

    public static final MappingData MAPPINGS = new MappingData("1.16.2", "1.17", true);
    private TagRewriter tagRewriter;

    public Protocol1_17To1_16_4() {
        super(ClientboundPackets1_16_2.class, ClientboundPackets1_16_2.class, ServerboundPackets1_16_2.class, ServerboundPackets1_16_2.class);
    }

    @Override
    protected void registerPackets() {
        InventoryPackets.register(this);
        WorldPackets.register(this);

        tagRewriter = new TagRewriter(this, null);
        tagRewriter.register(ClientboundPackets1_16_2.TAGS);

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
    }

    @Override
    protected void onMappingDataLoaded() {
        tagRewriter.addEmptyTags(RegistryType.ITEM, "minecraft:candles", "minecraft:ignored_by_piglin_babies", "minecraft:piglin_food");
        tagRewriter.addEmptyTags(RegistryType.BLOCK, "minecraft:crystal_sound_blocks", "minecraft:candle_cakes", "minecraft:candles");
        tagRewriter.addTag(RegistryType.BLOCK, "minecraft:cauldrons", 261);
    }

    @Override
    public void init(UserConnection user) {
        user.put(new BiomeStorage(user));
    }

    @Override
    @Nullable
    public MappingData getMappingData() {
        return MAPPINGS;
    }
}
