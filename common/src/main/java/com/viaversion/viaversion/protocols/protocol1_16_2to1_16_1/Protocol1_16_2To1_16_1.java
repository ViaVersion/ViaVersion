/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_16_2Types;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.rewriter.EntityRewriter;
import com.viaversion.viaversion.api.rewriter.ItemRewriter;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.data.MappingData;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.metadata.MetadataRewriter1_16_2To1_16_1;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.packets.EntityPackets;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.packets.InventoryPackets;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.packets.WorldPackets;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.ClientboundPackets1_16;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.ServerboundPackets1_16;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;

public class Protocol1_16_2To1_16_1 extends AbstractProtocol<ClientboundPackets1_16, ClientboundPackets1_16_2, ServerboundPackets1_16, ServerboundPackets1_16_2> {

    public static final MappingData MAPPINGS = new MappingData();
    private final EntityRewriter metadataRewriter = new MetadataRewriter1_16_2To1_16_1(this);
    private final ItemRewriter itemRewriter = new InventoryPackets(this);
    private TagRewriter tagRewriter;

    public Protocol1_16_2To1_16_1() {
        super(ClientboundPackets1_16.class, ClientboundPackets1_16_2.class, ServerboundPackets1_16.class, ServerboundPackets1_16_2.class);
    }

    @Override
    protected void registerPackets() {
        metadataRewriter.register();
        itemRewriter.register();

        EntityPackets.register(this);
        WorldPackets.register(this);

        tagRewriter = new TagRewriter(this);
        tagRewriter.register(ClientboundPackets1_16.TAGS, RegistryType.ENTITY);

        new StatisticsRewriter(this).register(ClientboundPackets1_16.STATISTICS);

        SoundRewriter soundRewriter = new SoundRewriter(this);
        soundRewriter.registerSound(ClientboundPackets1_16.SOUND);
        soundRewriter.registerSound(ClientboundPackets1_16.ENTITY_SOUND);

        // Recipe book data has been split into 2 separate packets
        registerServerbound(ServerboundPackets1_16_2.RECIPE_BOOK_DATA, new PacketRemapper() {
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
        registerServerbound(ServerboundPackets1_16_2.SEEN_RECIPE, ServerboundPackets1_16.RECIPE_BOOK_DATA, new PacketRemapper() {
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
    protected void onMappingDataLoaded() {
        tagRewriter.addTag(RegistryType.ITEM, "minecraft:stone_crafting_materials", 14, 962);
        tagRewriter.addEmptyTag(RegistryType.BLOCK, "minecraft:mushroom_grow_block");

        // The client now wants ALL (previous) tags to be sent, sooooo :>
        tagRewriter.addEmptyTags(RegistryType.ITEM, "minecraft:soul_fire_base_blocks", "minecraft:furnace_materials", "minecraft:crimson_stems",
                "minecraft:gold_ores", "minecraft:piglin_loved", "minecraft:piglin_repellents", "minecraft:creeper_drop_music_discs",
                "minecraft:logs_that_burn", "minecraft:stone_tool_materials", "minecraft:warped_stems");
        tagRewriter.addEmptyTags(RegistryType.BLOCK, "minecraft:infiniburn_nether", "minecraft:crimson_stems",
                "minecraft:wither_summon_base_blocks", "minecraft:infiniburn_overworld", "minecraft:piglin_repellents",
                "minecraft:hoglin_repellents", "minecraft:prevent_mob_spawning_inside", "minecraft:wart_blocks",
                "minecraft:stone_pressure_plates", "minecraft:nylium", "minecraft:gold_ores", "minecraft:pressure_plates",
                "minecraft:logs_that_burn", "minecraft:strider_warm_blocks", "minecraft:warped_stems", "minecraft:infiniburn_end",
                "minecraft:base_stone_nether", "minecraft:base_stone_overworld");
    }

    @Override
    public void init(UserConnection userConnection) {
        userConnection.addEntityTracker(this.getClass(), new EntityTrackerBase(userConnection, Entity1_16_2Types.PLAYER));
    }

    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    public EntityRewriter getEntityRewriter() {
        return metadataRewriter;
    }

    @Override
    public ItemRewriter getItemRewriter() {
        return itemRewriter;
    }
}
