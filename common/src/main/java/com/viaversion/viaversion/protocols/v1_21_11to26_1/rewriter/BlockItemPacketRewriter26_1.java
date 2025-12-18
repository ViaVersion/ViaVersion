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
package com.viaversion.viaversion.protocols.v1_21_11to26_1.rewriter;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_21_5;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.Protocol1_21_11To26_1;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter.RecipeDisplayRewriter1_21_5;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ServerboundPacket1_21_9;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPacket1_21_11;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPackets1_21_11;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.RecipeDisplayRewriter;
import com.viaversion.viaversion.rewriter.StructuredItemRewriter;

public final class BlockItemPacketRewriter26_1 extends StructuredItemRewriter<ClientboundPacket1_21_11, ServerboundPacket1_21_9, Protocol1_21_11To26_1> {

    public BlockItemPacketRewriter26_1(final Protocol1_21_11To26_1 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        final BlockRewriter<ClientboundPacket1_21_11> blockRewriter = BlockRewriter.for1_20_2(protocol);
        blockRewriter.registerBlockEvent(ClientboundPackets1_21_11.BLOCK_EVENT);
        blockRewriter.registerBlockUpdate(ClientboundPackets1_21_11.BLOCK_UPDATE);
        blockRewriter.registerSectionBlocksUpdate1_20(ClientboundPackets1_21_11.SECTION_BLOCKS_UPDATE);
        blockRewriter.registerLevelEvent1_21(ClientboundPackets1_21_11.LEVEL_EVENT, 2001);
        blockRewriter.registerLevelChunk1_19(ClientboundPackets1_21_11.LEVEL_CHUNK_WITH_LIGHT, ChunkType1_21_5::new);
        blockRewriter.registerBlockEntityData(ClientboundPackets1_21_11.BLOCK_ENTITY_DATA);

        registerSetCursorItem(ClientboundPackets1_21_11.SET_CURSOR_ITEM);
        registerSetPlayerInventory(ClientboundPackets1_21_11.SET_PLAYER_INVENTORY);
        registerCooldown1_21_2(ClientboundPackets1_21_11.COOLDOWN);
        registerSetContent1_21_2(ClientboundPackets1_21_11.CONTAINER_SET_CONTENT);
        registerSetSlot1_21_2(ClientboundPackets1_21_11.CONTAINER_SET_SLOT);
        registerAdvancements1_20_3(ClientboundPackets1_21_11.UPDATE_ADVANCEMENTS);
        registerSetEquipment(ClientboundPackets1_21_11.SET_EQUIPMENT);
        registerMerchantOffers1_20_5(ClientboundPackets1_21_11.MERCHANT_OFFERS);
        registerContainerClick1_21_5(ServerboundPackets1_21_6.CONTAINER_CLICK);
        registerSetCreativeModeSlot1_21_5(ServerboundPackets1_21_6.SET_CREATIVE_MODE_SLOT);

        final RecipeDisplayRewriter<ClientboundPacket1_21_11> recipeRewriter = new RecipeDisplayRewriter1_21_5<>(protocol);
        recipeRewriter.registerUpdateRecipes(ClientboundPackets1_21_11.UPDATE_RECIPES);
        recipeRewriter.registerRecipeBookAdd(ClientboundPackets1_21_11.RECIPE_BOOK_ADD);
        recipeRewriter.registerPlaceGhostRecipe(ClientboundPackets1_21_11.PLACE_GHOST_RECIPE);
    }

    @Override
    protected void handleItemDataComponentsToClient(final UserConnection connection, final Item item, final StructuredDataContainer container) {
        upgradeData(item, container);
        super.handleItemDataComponentsToClient(connection, item, container);
    }

    public static void upgradeData(final Item item, final StructuredDataContainer container) {
    }

    @Override
    protected void handleItemDataComponentsToServer(final UserConnection connection, final Item item, final StructuredDataContainer container) {
        downgradeData(item, container);
        super.handleItemDataComponentsToServer(connection, item, container);
    }

    public static void downgradeData(final Item item, final StructuredDataContainer container) {
        container.remove(StructuredDataKey.ADDITIONAL_TRADE_COST);
    }
}
