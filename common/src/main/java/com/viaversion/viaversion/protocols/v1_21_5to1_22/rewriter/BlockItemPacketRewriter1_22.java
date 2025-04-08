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
package com.viaversion.viaversion.protocols.v1_21_5to1_22.rewriter;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_21_5;
import com.viaversion.viaversion.api.type.types.version.Types1_21_5;
import com.viaversion.viaversion.api.type.types.version.Types1_22;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ClientboundPacket1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ClientboundPackets1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ServerboundPacket1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ServerboundPackets1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter.RecipeDisplayRewriter1_21_5;
import com.viaversion.viaversion.protocols.v1_21_5to1_22.Protocol1_21_5To1_22;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.RecipeDisplayRewriter;
import com.viaversion.viaversion.rewriter.StructuredItemRewriter;

public final class BlockItemPacketRewriter1_22 extends StructuredItemRewriter<ClientboundPacket1_21_5, ServerboundPacket1_21_5, Protocol1_21_5To1_22> {

    public BlockItemPacketRewriter1_22(final Protocol1_21_5To1_22 protocol) {
        super(protocol,
            Types1_21_5.ITEM, Types1_21_5.ITEM_ARRAY, Types1_22.ITEM, Types1_22.ITEM_ARRAY,
            Types1_21_5.ITEM_COST, Types1_21_5.OPTIONAL_ITEM_COST, Types1_22.ITEM_COST, Types1_22.OPTIONAL_ITEM_COST
        );
    }

    @Override
    public void registerPackets() {
        final BlockRewriter<ClientboundPacket1_21_5> blockRewriter = BlockRewriter.for1_20_2(protocol);
        blockRewriter.registerBlockEvent(ClientboundPackets1_21_5.BLOCK_EVENT);
        blockRewriter.registerBlockUpdate(ClientboundPackets1_21_5.BLOCK_UPDATE);
        blockRewriter.registerSectionBlocksUpdate1_20(ClientboundPackets1_21_5.SECTION_BLOCKS_UPDATE);
        blockRewriter.registerLevelEvent1_21(ClientboundPackets1_21_5.LEVEL_EVENT, 2001);
        blockRewriter.registerLevelChunk1_19(ClientboundPackets1_21_5.LEVEL_CHUNK_WITH_LIGHT, ChunkType1_21_5::new);
        blockRewriter.registerBlockEntityData(ClientboundPackets1_21_5.BLOCK_ENTITY_DATA);

        protocol.registerClientbound(ClientboundPackets1_21_5.SET_CURSOR_ITEM, this::passthroughClientboundItem);
        registerSetPlayerInventory(ClientboundPackets1_21_5.SET_PLAYER_INVENTORY);
        registerCooldown1_21_2(ClientboundPackets1_21_5.COOLDOWN);
        registerSetContent1_21_2(ClientboundPackets1_21_5.CONTAINER_SET_CONTENT);
        registerSetSlot1_21_2(ClientboundPackets1_21_5.CONTAINER_SET_SLOT);
        registerAdvancements1_20_3(ClientboundPackets1_21_5.UPDATE_ADVANCEMENTS);
        registerSetEquipment(ClientboundPackets1_21_5.SET_EQUIPMENT);
        registerMerchantOffers1_20_5(ClientboundPackets1_21_5.MERCHANT_OFFERS);
        registerContainerClick1_21_5(ServerboundPackets1_21_5.CONTAINER_CLICK);
        registerSetCreativeModeSlot1_21_5(ServerboundPackets1_21_5.SET_CREATIVE_MODE_SLOT, Types1_21_5.LENGTH_PREFIXED_ITEM, Types1_22.LENGTH_PREFIXED_ITEM);

        final RecipeDisplayRewriter<ClientboundPacket1_21_5> recipeRewriter = new RecipeDisplayRewriter1_21_5<>(protocol);
        recipeRewriter.registerUpdateRecipes(ClientboundPackets1_21_5.UPDATE_RECIPES);
        recipeRewriter.registerRecipeBookAdd(ClientboundPackets1_21_5.RECIPE_BOOK_ADD);
        recipeRewriter.registerPlaceGhostRecipe(ClientboundPackets1_21_5.PLACE_GHOST_RECIPE);
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
        dataContainer.replaceKey(StructuredDataKey.CHARGED_PROJECTILES1_21_5, StructuredDataKey.CHARGED_PROJECTILES1_22);
        dataContainer.replaceKey(StructuredDataKey.BUNDLE_CONTENTS1_21_5, StructuredDataKey.BUNDLE_CONTENTS1_22);
        dataContainer.replaceKey(StructuredDataKey.CONTAINER1_21_5, StructuredDataKey.CONTAINER1_22);
        dataContainer.replaceKey(StructuredDataKey.USE_REMAINDER1_21_5, StructuredDataKey.USE_REMAINDER1_22);
        dataContainer.replaceKey(StructuredDataKey.CAN_PLACE_ON1_21_5, StructuredDataKey.CAN_PLACE_ON1_22);
        dataContainer.replaceKey(StructuredDataKey.CAN_BREAK1_21_5, StructuredDataKey.CAN_BREAK1_22);
        dataContainer.replaceKey(StructuredDataKey.ATTRIBUTE_MODIFIERS1_21_5, StructuredDataKey.ATTRIBUTE_MODIFIERS1_22);
    }

    public static void downgradeItemData(final Item item) {
        final StructuredDataContainer dataContainer = item.dataContainer();
        dataContainer.replaceKey(StructuredDataKey.CHARGED_PROJECTILES1_22, StructuredDataKey.CHARGED_PROJECTILES1_21_5);
        dataContainer.replaceKey(StructuredDataKey.BUNDLE_CONTENTS1_22, StructuredDataKey.BUNDLE_CONTENTS1_21_5);
        dataContainer.replaceKey(StructuredDataKey.CONTAINER1_22, StructuredDataKey.CONTAINER1_21_5);
        dataContainer.replaceKey(StructuredDataKey.USE_REMAINDER1_22, StructuredDataKey.USE_REMAINDER1_21_5);
        dataContainer.replaceKey(StructuredDataKey.CAN_PLACE_ON1_22, StructuredDataKey.CAN_PLACE_ON1_21_5);
        dataContainer.replaceKey(StructuredDataKey.CAN_BREAK1_22, StructuredDataKey.CAN_BREAK1_21_5);
        dataContainer.replaceKey(StructuredDataKey.ATTRIBUTE_MODIFIERS1_22, StructuredDataKey.ATTRIBUTE_MODIFIERS1_21_5);
    }
}
