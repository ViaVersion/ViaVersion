/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.template;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPackets26_1;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter.RecipeDisplayRewriter1_21_5;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ServerboundPacket1_21_9;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.RecipeDisplayRewriter;
import com.viaversion.viaversion.rewriter.StructuredItemRewriter;
import com.viaversion.viaversion.rewriter.block.BlockRewriter1_21_5;

// To replace if needed:
//   ChunkType26_1
//   RecipeDisplayRewriter
final class BlockItemPacketRewriter99_1 extends StructuredItemRewriter<ClientboundPacket26_1, ServerboundPacket1_21_9, Protocol98_1To99_1> {

    public BlockItemPacketRewriter99_1(final Protocol98_1To99_1 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        // Register block and block state id changes
        // Other places using block state id mappings: Spawn particle, entity data, entity spawn (falling blocks)
        // Tags and statistics use block (!) ids
        final BlockRewriter<ClientboundPacket26_1> blockRewriter = new BlockRewriter1_21_5<>(protocol);
        blockRewriter.registerBlockEvent(ClientboundPackets26_1.BLOCK_EVENT);
        blockRewriter.registerBlockUpdate(ClientboundPackets26_1.BLOCK_UPDATE);
        blockRewriter.registerSectionBlocksUpdate1_20(ClientboundPackets26_1.SECTION_BLOCKS_UPDATE);
        blockRewriter.registerLevelEvent1_21(ClientboundPackets26_1.LEVEL_EVENT, 2001);
        blockRewriter.registerLevelChunk1_19(ClientboundPackets26_1.LEVEL_CHUNK_WITH_LIGHT, ChunkType26_1::new);
        blockRewriter.registerBlockEntityData(ClientboundPackets26_1.BLOCK_ENTITY_DATA);

        // Registers item id changes
        // Other places using item ids are: Entity data, tags, statistics, effect
        // registerOpenScreen(ClientboundPackets26_1.OPEN_SCREEN); If a new container type was added; also remove from the component rewriter registration
        registerSetCursorItem(ClientboundPackets26_1.SET_CURSOR_ITEM);
        registerSetPlayerInventory(ClientboundPackets26_1.SET_PLAYER_INVENTORY);
        registerCooldown1_21_2(ClientboundPackets26_1.COOLDOWN);
        registerSetContent1_21_2(ClientboundPackets26_1.CONTAINER_SET_CONTENT);
        registerSetSlot1_21_2(ClientboundPackets26_1.CONTAINER_SET_SLOT);
        registerAdvancements1_20_3(ClientboundPackets26_1.UPDATE_ADVANCEMENTS);
        registerSetEquipment(ClientboundPackets26_1.SET_EQUIPMENT);
        registerMerchantOffers1_20_5(ClientboundPackets26_1.MERCHANT_OFFERS);
        registerContainerClick1_21_5(ServerboundPackets1_21_6.CONTAINER_CLICK);
        registerSetCreativeModeSlot1_21_5(ServerboundPackets1_21_6.SET_CREATIVE_MODE_SLOT);

        final RecipeDisplayRewriter<ClientboundPacket26_1> recipeRewriter = new RecipeDisplayRewriter1_21_5<>(protocol);
        recipeRewriter.registerUpdateRecipes(ClientboundPackets26_1.UPDATE_RECIPES);
        recipeRewriter.registerRecipeBookAdd(ClientboundPackets26_1.RECIPE_BOOK_ADD);
        recipeRewriter.registerPlaceGhostRecipe(ClientboundPackets26_1.PLACE_GHOST_RECIPE);
        // OR do this if serialization of recipes changed and override the relevant method
        // Add new serializers to RecipeDisplayRewriter, or extend the last one for changes
    }

    @Override
    protected void backupInconvertibleData(final UserConnection connection, final Item item, final StructuredDataContainer dataContainer, final CompoundTag backupTag) {
        super.backupInconvertibleData(connection, item, dataContainer, backupTag);
        // back up any data if needed here, called before the method below
    }

    @Override
    protected void handleItemDataComponentsToClient(final UserConnection connection, final Item item, final StructuredDataContainer container) {
        upgradeData(item, container);
        super.handleItemDataComponentsToClient(connection, item, container);
    }

    public static void upgradeData(final Item item, final StructuredDataContainer container) { // public for VB
    }

    @Override
    protected void handleItemDataComponentsToServer(final UserConnection connection, final Item item, final StructuredDataContainer container) {
        downgradeData(item, container);
        super.handleItemDataComponentsToServer(connection, item, container);
    }

    public static void downgradeData(final Item item, final StructuredDataContainer container) {
    }

    @Override
    protected void restoreBackupData(final Item item, final StructuredDataContainer container, final CompoundTag customData) {
        super.restoreBackupData(item, container, customData);
        // restore any data if needed here
    }
}
