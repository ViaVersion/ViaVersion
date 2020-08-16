package us.myles.ViaVersion.protocols.protocol1_15to1_14_4;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.rewriters.MetadataRewriter;
import us.myles.ViaVersion.api.rewriters.RegistryType;
import us.myles.ViaVersion.api.rewriters.SoundRewriter;
import us.myles.ViaVersion.api.rewriters.StatisticsRewriter;
import us.myles.ViaVersion.api.rewriters.TagRewriter;
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

public class Protocol1_15To1_14_4 extends Protocol<ClientboundPackets1_14, ClientboundPackets1_15, ServerboundPackets1_14, ServerboundPackets1_14> {

    public static final MappingData MAPPINGS = new MappingData();
    private TagRewriter tagRewriter;

    public Protocol1_15To1_14_4() {
        super(ClientboundPackets1_14.class, ClientboundPackets1_15.class, ServerboundPackets1_14.class, ServerboundPackets1_14.class);
    }

    @Override
    protected void registerPackets() {
        MetadataRewriter metadataRewriter = new MetadataRewriter1_15To1_14_4(this);

        EntityPackets.register(this);
        PlayerPackets.register(this);
        WorldPackets.register(this);
        InventoryPackets.register(this);

        SoundRewriter soundRewriter = new SoundRewriter(this);
        soundRewriter.registerSound(ClientboundPackets1_14.ENTITY_SOUND); // Entity Sound Effect (added somewhere in 1.14)
        soundRewriter.registerSound(ClientboundPackets1_14.SOUND);

        new StatisticsRewriter(this, metadataRewriter::getNewEntityId).register(ClientboundPackets1_14.STATISTICS);

        registerIncoming(ServerboundPackets1_14.EDIT_BOOK, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> InventoryPackets.toServer(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)));
            }
        });

        tagRewriter = new TagRewriter(this, EntityPackets::getNewEntityId);
        tagRewriter.register(ClientboundPackets1_14.TAGS);
    }

    @Override
    protected void onMappingDataLoaded() {
        int[] shulkerBoxes = new int[17];
        int shulkerBoxOffset = 501;
        for (int i = 0; i < 17; i++) {
            shulkerBoxes[i] = shulkerBoxOffset + i;
        }
        tagRewriter.addTag(RegistryType.BLOCK, "minecraft:shulker_boxes", shulkerBoxes);
    }

    @Override
    public void init(UserConnection userConnection) {
        userConnection.put(new EntityTracker1_15(userConnection));
    }

    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }
}
