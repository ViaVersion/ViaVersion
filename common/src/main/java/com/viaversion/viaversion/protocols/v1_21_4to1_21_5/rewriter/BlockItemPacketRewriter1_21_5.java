/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.api.type.types.version.Types1_21_4;
import com.viaversion.viaversion.api.type.types.version.Types1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.Protocol1_21_4To1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ServerboundPacket1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ServerboundPackets1_21_5;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPacket1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.RecipeDisplayRewriter;
import com.viaversion.viaversion.rewriter.StructuredItemRewriter;

public final class BlockItemPacketRewriter1_21_5 extends StructuredItemRewriter<ClientboundPacket1_21_2, ServerboundPacket1_21_5, Protocol1_21_4To1_21_5> {

    public BlockItemPacketRewriter1_21_5(final Protocol1_21_4To1_21_5 protocol) {
        super(protocol,
            Types1_21_4.ITEM, Types1_21_4.ITEM_ARRAY, Types1_21_5.ITEM, Types1_21_5.ITEM_ARRAY,
            Types1_21_4.ITEM_COST, Types1_21_4.OPTIONAL_ITEM_COST, Types1_21_5.ITEM_COST, Types1_21_5.OPTIONAL_ITEM_COST
        );
    }

    @Override
    public void registerPackets() {
        final BlockRewriter<ClientboundPacket1_21_2> blockRewriter = BlockRewriter.for1_20_2(protocol);
        blockRewriter.registerBlockEvent(ClientboundPackets1_21_2.BLOCK_EVENT);
        blockRewriter.registerBlockUpdate(ClientboundPackets1_21_2.BLOCK_UPDATE);
        blockRewriter.registerSectionBlocksUpdate1_20(ClientboundPackets1_21_2.SECTION_BLOCKS_UPDATE);
        blockRewriter.registerLevelEvent1_21(ClientboundPackets1_21_2.LEVEL_EVENT, 2001);
        blockRewriter.registerLevelChunk1_19(ClientboundPackets1_21_2.LEVEL_CHUNK_WITH_LIGHT, ChunkType1_20_2::new);
        blockRewriter.registerBlockEntityData(ClientboundPackets1_21_2.BLOCK_ENTITY_DATA);

        protocol.registerClientbound(ClientboundPackets1_21_2.SET_CURSOR_ITEM, this::passthroughClientboundItem);
        registerSetPlayerInventory(ClientboundPackets1_21_2.SET_PLAYER_INVENTORY);
        registerCooldown1_21_2(ClientboundPackets1_21_2.COOLDOWN);
        registerSetContent1_21_2(ClientboundPackets1_21_2.CONTAINER_SET_CONTENT);
        registerSetSlot1_21_2(ClientboundPackets1_21_2.CONTAINER_SET_SLOT);
        registerSetEquipment(ClientboundPackets1_21_2.SET_EQUIPMENT);
        registerMerchantOffers1_20_5(ClientboundPackets1_21_2.MERCHANT_OFFERS);
        registerContainerClick1_21_2(ServerboundPackets1_21_5.CONTAINER_CLICK);
        registerSetCreativeModeSlot(ServerboundPackets1_21_5.SET_CREATIVE_MODE_SLOT);

        registerAdvancements1_20_3(ClientboundPackets1_21_2.UPDATE_ADVANCEMENTS);
        protocol.appendClientbound(ClientboundPackets1_21_2.UPDATE_ADVANCEMENTS, wrapper -> {
            wrapper.passthrough(Types.STRING_ARRAY); // Removed
            final int progressSize = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < progressSize; i++) {
                wrapper.passthrough(Types.STRING); // Key

                final int criterionSize = wrapper.passthrough(Types.VAR_INT);
                for (int j = 0; j < criterionSize; j++) {
                    wrapper.passthrough(Types.STRING); // Key
                    wrapper.passthrough(Types.OPTIONAL_LONG); // Obtained instant
                }
            }

            // Show advancements
            wrapper.write(Types.BOOLEAN, true);
        });

        final RecipeDisplayRewriter<ClientboundPacket1_21_2> recipeRewriter = new RecipeDisplayRewriter<>(protocol);
        recipeRewriter.registerUpdateRecipes(ClientboundPackets1_21_2.UPDATE_RECIPES);
        recipeRewriter.registerRecipeBookAdd(ClientboundPackets1_21_2.RECIPE_BOOK_ADD);
        recipeRewriter.registerPlaceGhostRecipe(ClientboundPackets1_21_2.PLACE_GHOST_RECIPE);
    }

    @Override
    public Item handleItemToClient(final UserConnection connection, final Item item) {
        super.handleItemToClient(connection, item);
        updateItemData(item);
        return item;
    }

    @Override
    public Item handleItemToServer(final UserConnection connection, final Item item) {
        super.handleItemToServer(connection, item);
        downgradeItemData(item);
        return item;
    }

    public static void updateItemData(final Item item) {
        final StructuredDataContainer dataContainer = item.dataContainer();
        dataContainer.replaceKey(StructuredDataKey.CHARGED_PROJECTILES1_21_4, StructuredDataKey.CHARGED_PROJECTILES1_21_5);
        dataContainer.replaceKey(StructuredDataKey.BUNDLE_CONTENTS1_21_4, StructuredDataKey.BUNDLE_CONTENTS1_21_5);
        dataContainer.replaceKey(StructuredDataKey.CONTAINER1_21_4, StructuredDataKey.CONTAINER1_21_5);
        dataContainer.replaceKey(StructuredDataKey.USE_REMAINDER1_21_4, StructuredDataKey.USE_REMAINDER1_21_5);
        dataContainer.replaceKey(StructuredDataKey.TOOL1_20_5, StructuredDataKey.TOOL1_21_5);
        dataContainer.replaceKey(StructuredDataKey.EQUIPPABLE1_21_2, StructuredDataKey.EQUIPPABLE1_21_5);
    }

    public static void downgradeItemData(final Item item) {
        final StructuredDataContainer dataContainer = item.dataContainer();
        dataContainer.replaceKey(StructuredDataKey.CHARGED_PROJECTILES1_21_5, StructuredDataKey.CHARGED_PROJECTILES1_21_4);
        dataContainer.replaceKey(StructuredDataKey.BUNDLE_CONTENTS1_21_5, StructuredDataKey.BUNDLE_CONTENTS1_21_4);
        dataContainer.replaceKey(StructuredDataKey.CONTAINER1_21_5, StructuredDataKey.CONTAINER1_21_4);
        dataContainer.replaceKey(StructuredDataKey.USE_REMAINDER1_21_5, StructuredDataKey.USE_REMAINDER1_21_4);
        dataContainer.replaceKey(StructuredDataKey.TOOL1_21_5, StructuredDataKey.TOOL1_20_5);
        dataContainer.replaceKey(StructuredDataKey.EQUIPPABLE1_21_5, StructuredDataKey.EQUIPPABLE1_21_2);
        dataContainer.remove(StructuredDataKey.POTION_DURATION_SCALE);
        dataContainer.remove(StructuredDataKey.WEAPON);
        dataContainer.remove(StructuredDataKey.VILLAGER_VARIANT);
        dataContainer.remove(StructuredDataKey.WOLF_VARIANT);
        dataContainer.remove(StructuredDataKey.WOLF_COLLAR);
        dataContainer.remove(StructuredDataKey.FOX_VARIANT);
        dataContainer.remove(StructuredDataKey.SALMON_SIZE);
        dataContainer.remove(StructuredDataKey.PARROT_VARIANT);
        dataContainer.remove(StructuredDataKey.TROPICAL_FISH_PATTERN);
        dataContainer.remove(StructuredDataKey.TROPICAL_FISH_BASE_COLOR);
        dataContainer.remove(StructuredDataKey.TROPICAL_FISH_PATTERN_COLOR);
        dataContainer.remove(StructuredDataKey.MOOSHROOM_VARIANT);
        dataContainer.remove(StructuredDataKey.RABBIT_VARIANT);
        dataContainer.remove(StructuredDataKey.PIG_VARIANT);
        dataContainer.remove(StructuredDataKey.FROG_VARIANT);
        dataContainer.remove(StructuredDataKey.HORSE_VARIANT);
        dataContainer.remove(StructuredDataKey.PAINTING_VARIANT);
        dataContainer.remove(StructuredDataKey.LLAMA_VARIANT);
        dataContainer.remove(StructuredDataKey.AXOLOTL_VARIANT);
        dataContainer.remove(StructuredDataKey.CAT_VARIANT);
        dataContainer.remove(StructuredDataKey.CAT_COLLAR);
        dataContainer.remove(StructuredDataKey.SHEEP_COLOR);
        dataContainer.remove(StructuredDataKey.SHULKER_COLOR);
    }
}
