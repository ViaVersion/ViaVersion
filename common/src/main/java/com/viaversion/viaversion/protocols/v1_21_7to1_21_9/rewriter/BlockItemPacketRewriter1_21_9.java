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
package com.viaversion.viaversion.protocols.v1_21_7to1_21_9.rewriter;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.ResolvableProfile;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_9;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.Bee;
import com.viaversion.viaversion.api.minecraft.item.data.BlockEntityData;
import com.viaversion.viaversion.api.minecraft.item.data.EntityData;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter.RecipeDisplayRewriter1_21_5;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundPacket1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.Protocol1_21_7To1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ServerboundPacket1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.storage.DimensionScaleStorage;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.RecipeDisplayRewriter;
import com.viaversion.viaversion.rewriter.StructuredItemRewriter;

public final class BlockItemPacketRewriter1_21_9 extends StructuredItemRewriter<ClientboundPacket1_21_6, ServerboundPacket1_21_9, Protocol1_21_7To1_21_9> {

    public BlockItemPacketRewriter1_21_9(final Protocol1_21_7To1_21_9 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        final BlockRewriter<ClientboundPacket1_21_6> blockRewriter = BlockRewriter.for1_20_2(protocol);
        blockRewriter.registerBlockEvent(ClientboundPackets1_21_6.BLOCK_EVENT);
        blockRewriter.registerBlockUpdate(ClientboundPackets1_21_6.BLOCK_UPDATE);
        blockRewriter.registerSectionBlocksUpdate1_20(ClientboundPackets1_21_6.SECTION_BLOCKS_UPDATE);
        blockRewriter.registerLevelEvent1_21(ClientboundPackets1_21_6.LEVEL_EVENT, 2001);
        blockRewriter.registerLevelChunk1_19(ClientboundPackets1_21_6.LEVEL_CHUNK_WITH_LIGHT, ChunkType1_21_5::new);
        blockRewriter.registerBlockEntityData(ClientboundPackets1_21_6.BLOCK_ENTITY_DATA);

        registerSetCursorItem(ClientboundPackets1_21_6.SET_CURSOR_ITEM);
        registerSetPlayerInventory(ClientboundPackets1_21_6.SET_PLAYER_INVENTORY);
        registerCooldown1_21_2(ClientboundPackets1_21_6.COOLDOWN);
        registerSetContent1_21_2(ClientboundPackets1_21_6.CONTAINER_SET_CONTENT);
        registerSetSlot1_21_2(ClientboundPackets1_21_6.CONTAINER_SET_SLOT);
        registerAdvancements1_20_3(ClientboundPackets1_21_6.UPDATE_ADVANCEMENTS);
        registerSetEquipment(ClientboundPackets1_21_6.SET_EQUIPMENT);
        registerMerchantOffers1_20_5(ClientboundPackets1_21_6.MERCHANT_OFFERS);
        registerContainerClick1_21_5(ServerboundPackets1_21_6.CONTAINER_CLICK);
        registerSetCreativeModeSlot1_21_5(ServerboundPackets1_21_6.SET_CREATIVE_MODE_SLOT);

        final RecipeDisplayRewriter<ClientboundPacket1_21_6> recipeRewriter = new RecipeDisplayRewriter1_21_5<>(protocol);
        recipeRewriter.registerUpdateRecipes(ClientboundPackets1_21_6.UPDATE_RECIPES);
        recipeRewriter.registerRecipeBookAdd(ClientboundPackets1_21_6.RECIPE_BOOK_ADD);
        recipeRewriter.registerPlaceGhostRecipe(ClientboundPackets1_21_6.PLACE_GHOST_RECIPE);

        protocol.registerClientbound(ClientboundPackets1_21_6.INITIALIZE_BORDER, this::updateBorderCenter);
        protocol.registerClientbound(ClientboundPackets1_21_6.SET_BORDER_CENTER, this::updateBorderCenter);
    }

    @Override
    protected void handleItemDataComponentsToClient(final UserConnection connection, final Item item, final StructuredDataContainer container) {
        upgradeData(item, container);
        super.handleItemDataComponentsToClient(connection, item, container);
    }

    public static void upgradeData(final Item item, final StructuredDataContainer container) {
        container.replace(StructuredDataKey.BEES1_20_5, StructuredDataKey.BEES1_21_9, bees -> {
            for (int i = 0; i < bees.length; i++) {
                final Bee bee = bees[i];
                bees[i] = new Bee(new EntityData(EntityTypes1_21_9.BEE.getId(), bee.entityData().tag()), bee.ticksInHive(), bee.minTicksInHive());
            }
            return bees;
        });
        // The actual type shouldn't matter for the display of items
        container.replace(StructuredDataKey.ENTITY_DATA1_20_5, StructuredDataKey.ENTITY_DATA1_21_9, tag -> {
            final int id = Protocol1_21_7To1_21_9.MAPPINGS.getEntityMappings().mappedId(tag.getString("id", "pig"));
            return new EntityData(id == -1 ? 0 : id, tag);
        });
        container.replace(StructuredDataKey.BLOCK_ENTITY_DATA1_20_5, StructuredDataKey.BLOCK_ENTITY_DATA1_21_9, tag -> {
            final int id = Protocol1_21_7To1_21_9.MAPPINGS.getBlockEntityMappings().mappedId(tag.getString("id", "furnace"));
            return new BlockEntityData(id == -1 ? 0 : id, tag);
        });
        container.replace(StructuredDataKey.PROFILE1_20_5, StructuredDataKey.PROFILE1_21_9, ResolvableProfile::new);
    }

    @Override
    protected void handleItemDataComponentsToServer(final UserConnection connection, final Item item, final StructuredDataContainer container) {
        downgradeData(item, container);
        super.handleItemDataComponentsToServer(connection, item, container);
    }

    public static void downgradeData(final Item item, final StructuredDataContainer container) {
        container.replaceKey(StructuredDataKey.BEES1_21_9, StructuredDataKey.BEES1_20_5);
        container.replace(StructuredDataKey.ENTITY_DATA1_21_9, StructuredDataKey.ENTITY_DATA1_20_5, entityData -> {
            final String id = Protocol1_21_7To1_21_9.MAPPINGS.getEntityMappings().mappedIdentifier(entityData.type());
            entityData.tag().putString("id", id);
            return entityData.tag();
        });
        container.replace(StructuredDataKey.BLOCK_ENTITY_DATA1_21_9, StructuredDataKey.BLOCK_ENTITY_DATA1_20_5, blockEntityData -> {
            final String id = Protocol1_21_7To1_21_9.MAPPINGS.getBlockEntityMappings().mappedIdentifier(blockEntityData.type());
            blockEntityData.tag().putString("id", id);
            return blockEntityData.tag();
        });
        container.replace(StructuredDataKey.PROFILE1_21_9, StructuredDataKey.PROFILE1_20_5, ResolvableProfile::profile);
    }

    private void updateBorderCenter(final PacketWrapper wrapper) {
        double centerX = wrapper.read(Types.DOUBLE);
        double centerZ = wrapper.read(Types.DOUBLE);

        final EntityTracker tracker = protocol.getEntityRewriter().tracker(wrapper.user());
        if (tracker.currentDimensionId() != -1) {
            final double scale = wrapper.user().get(DimensionScaleStorage.class).getScale(tracker.currentDimensionId());
            centerX /= scale;
            centerZ /= scale;
        }

        wrapper.write(Types.DOUBLE, centerX);
        wrapper.write(Types.DOUBLE, centerZ);
    }
}
