package us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.rewriters.SoundRewriter;
import us.myles.ViaVersion.api.rewriters.TagRewriter;
import us.myles.ViaVersion.api.rewriters.TagType;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.data.MappingData;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.metadata.MetadataRewriter1_16_2To1_16_1;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.packets.EntityPackets;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.packets.WorldPackets;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.storage.EntityTracker1_16_2;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.ClientboundPackets1_16;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.ServerboundPackets1_16;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

public class Protocol1_16_2To1_16_1 extends Protocol<ClientboundPackets1_16, ClientboundPackets1_16_2, ServerboundPackets1_16, ServerboundPackets1_16_2> {

    private TagRewriter tagRewriter;

    public Protocol1_16_2To1_16_1() {
        super(ClientboundPackets1_16.class, ClientboundPackets1_16_2.class, ServerboundPackets1_16.class, ServerboundPackets1_16_2.class, true);
    }

    @Override
    protected void registerPackets() {
        MetadataRewriter1_16_2To1_16_1 metadataRewriter = new MetadataRewriter1_16_2To1_16_1(this);

        EntityPackets.register(this);
        WorldPackets.register(this);
        InventoryPackets.register(this);

        tagRewriter = new TagRewriter(this, Protocol1_16_2To1_16_1::getNewBlockId, InventoryPackets::getNewItemId, metadataRewriter::getNewEntityId);
        tagRewriter.register(ClientboundPackets1_16.TAGS);

        SoundRewriter soundRewriter = new SoundRewriter(this, id -> MappingData.soundMappings.getNewId(id));
        soundRewriter.registerSound(ClientboundPackets1_16.SOUND);
        soundRewriter.registerSound(ClientboundPackets1_16.ENTITY_SOUND);

        // Recipe book data has been split into 2 separate packets
        registerIncoming(ServerboundPackets1_16_2.RECIPE_BOOK_DATA, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    int recipeType = wrapper.read(Type.VAR_INT);
                    boolean open = wrapper.read(Type.BOOLEAN);
                    boolean filter = wrapper.read(Type.BOOLEAN);
                    wrapper.write(Type.VAR_INT, 1); // Settings
                    wrapper.write(Type.BOOLEAN, recipeType == 0); // Crafting
                    wrapper.write(Type.BOOLEAN, filter);
                    wrapper.write(Type.BOOLEAN, recipeType == 1); // Furnace
                    wrapper.write(Type.BOOLEAN, filter);
                    wrapper.write(Type.BOOLEAN, recipeType == 2); // Blast Furnace
                    wrapper.write(Type.BOOLEAN, filter);
                    wrapper.write(Type.BOOLEAN, recipeType == 3); // Smoker
                    wrapper.write(Type.BOOLEAN, filter);
                });
            }
        });
        registerIncoming(ServerboundPackets1_16_2.SEEN_RECIPE, ServerboundPackets1_16.RECIPE_BOOK_DATA, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    String recipe = wrapper.read(Type.STRING);
                    wrapper.write(Type.VAR_INT, 0); // Shown
                    wrapper.write(Type.STRING, recipe);
                });
            }
        });
    }

    @Override
    protected void loadMappingData() {
        MappingData.init();

        tagRewriter.addTag(TagType.ITEM, "minecraft:stone_crafting_materials", 14, 962);
        tagRewriter.addEmptyTag(TagType.BLOCK, "minecraft:mushroom_grow_block");

        // The client now wants ALL (previous) tags to be sent, sooooo :>
        tagRewriter.addEmptyTags(TagType.ITEM, "minecraft:soul_fire_base_blocks", "minecraft:furnace_materials", "minecraft:crimson_stems",
                "minecraft:gold_ores", "minecraft:piglin_loved", "minecraft:piglin_repellents", "minecraft:creeper_drop_music_discs",
                "minecraft:logs_that_burn", "minecraft:stone_tool_materials", "minecraft:warped_stems");
        tagRewriter.addEmptyTags(TagType.BLOCK, "minecraft:infiniburn_nether", "minecraft:crimson_stems",
                "minecraft:wither_summon_base_blocks", "minecraft:infiniburn_overworld", "minecraft:piglin_repellents",
                "minecraft:hoglin_repellents", "minecraft:prevent_mob_spawning_inside", "minecraft:wart_blocks",
                "minecraft:stone_pressure_plates", "minecraft:nylium", "minecraft:gold_ores", "minecraft:pressure_plates",
                "minecraft:logs_that_burn", "minecraft:strider_warm_blocks", "minecraft:warped_stems", "minecraft:infiniburn_end",
                "minecraft:base_stone_nether", "minecraft:base_stone_overworld");
    }

    public static int getNewBlockStateId(int id) {
        int newId = MappingData.blockStateMappings.getNewId(id);
        if (newId == -1) {
            Via.getPlatform().getLogger().warning("Missing 1.16.2 blockstate for 1.16 blockstate " + id);
            return 0;
        }
        return newId;
    }

    public static int getNewBlockId(int id) {
        int newId = MappingData.blockMappings.getNewId(id);
        if (newId == -1) {
            Via.getPlatform().getLogger().warning("Missing 1.16.2 block for 1.16 block " + id);
            return 0;
        }
        return newId;
    }

    @Override
    public void init(UserConnection userConnection) {
        userConnection.put(new EntityTracker1_16_2(userConnection));
        if (!userConnection.has(ClientWorld.class)) {
            userConnection.put(new ClientWorld(userConnection));
        }
    }
}
