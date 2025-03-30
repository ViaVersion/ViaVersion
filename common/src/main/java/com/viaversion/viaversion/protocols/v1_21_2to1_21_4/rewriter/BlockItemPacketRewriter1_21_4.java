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
package com.viaversion.viaversion.protocols.v1_21_2to1_21_4.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.FloatTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.Consumable1_21_2;
import com.viaversion.viaversion.api.minecraft.item.data.CustomModelData1_21_4;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.api.type.types.version.Types1_21_2;
import com.viaversion.viaversion.api.type.types.version.Types1_21_4;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.Protocol1_21_2To1_21_4;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.packet.ServerboundPacket1_21_4;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.packet.ServerboundPackets1_21_4;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.provider.PickItemProvider;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPacket1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.RecipeDisplayRewriter;
import com.viaversion.viaversion.rewriter.StructuredItemRewriter;
import com.viaversion.viaversion.util.TagUtil;

public final class BlockItemPacketRewriter1_21_4 extends StructuredItemRewriter<ClientboundPacket1_21_2, ServerboundPacket1_21_4, Protocol1_21_2To1_21_4> {

    public BlockItemPacketRewriter1_21_4(final Protocol1_21_2To1_21_4 protocol) {
        super(protocol,
            Types1_21_2.ITEM, Types1_21_2.ITEM_ARRAY, Types1_21_4.ITEM, Types1_21_4.ITEM_ARRAY,
            Types1_21_2.ITEM_COST, Types1_21_2.OPTIONAL_ITEM_COST, Types1_21_4.ITEM_COST, Types1_21_4.OPTIONAL_ITEM_COST
        );
    }

    @Override
    public void registerPackets() {
        final BlockRewriter<ClientboundPacket1_21_2> blockRewriter = BlockRewriter.for1_20_2(protocol);
        blockRewriter.registerBlockEvent(ClientboundPackets1_21_2.BLOCK_EVENT);
        blockRewriter.registerBlockUpdate(ClientboundPackets1_21_2.BLOCK_UPDATE);
        blockRewriter.registerSectionBlocksUpdate1_20(ClientboundPackets1_21_2.SECTION_BLOCKS_UPDATE);
        blockRewriter.registerLevelEvent1_21(ClientboundPackets1_21_2.LEVEL_EVENT, 2001);
        blockRewriter.registerLevelChunk1_19(ClientboundPackets1_21_2.LEVEL_CHUNK_WITH_LIGHT, ChunkType1_20_2::new, (connection, blockEntity) -> handleBlockEntity(blockEntity.tag()));
        blockRewriter.registerBlockEntityData(ClientboundPackets1_21_2.BLOCK_ENTITY_DATA, blockEntity -> handleBlockEntity(blockEntity.tag()));

        protocol.registerClientbound(ClientboundPackets1_21_2.SET_HELD_SLOT, wrapper -> {
            final byte slot = wrapper.read(Types.BYTE);
            wrapper.write(Types.VAR_INT, (int) slot);
        });

        protocol.registerServerbound(ServerboundPackets1_21_4.PICK_ITEM_FROM_BLOCK, null, wrapper -> {
            final BlockPosition blockPosition = wrapper.read(Types.BLOCK_POSITION1_14);
            final boolean includeData = wrapper.read(Types.BOOLEAN);
            Via.getManager().getProviders().get(PickItemProvider.class).pickItemFromBlock(wrapper.user(), blockPosition, includeData);
            wrapper.cancel();
        });
        protocol.registerServerbound(ServerboundPackets1_21_4.PICK_ITEM_FROM_ENTITY, null, wrapper -> {
            final int entityId = wrapper.read(Types.VAR_INT);
            final boolean includeData = wrapper.read(Types.BOOLEAN);
            Via.getManager().getProviders().get(PickItemProvider.class).pickItemFromEntity(wrapper.user(), entityId, includeData);
            wrapper.cancel();
        });

        protocol.registerClientbound(ClientboundPackets1_21_2.SET_CURSOR_ITEM, this::passthroughClientboundItem);
        registerSetPlayerInventory(ClientboundPackets1_21_2.SET_PLAYER_INVENTORY);
        registerCooldown1_21_2(ClientboundPackets1_21_2.COOLDOWN);
        registerSetContent1_21_2(ClientboundPackets1_21_2.CONTAINER_SET_CONTENT);
        registerSetSlot1_21_2(ClientboundPackets1_21_2.CONTAINER_SET_SLOT);
        registerAdvancements1_20_3(ClientboundPackets1_21_2.UPDATE_ADVANCEMENTS);
        registerSetEquipment(ClientboundPackets1_21_2.SET_EQUIPMENT);
        registerContainerClick1_21_2(ServerboundPackets1_21_4.CONTAINER_CLICK);
        registerMerchantOffers1_20_5(ClientboundPackets1_21_2.MERCHANT_OFFERS);
        registerSetCreativeModeSlot(ServerboundPackets1_21_4.SET_CREATIVE_MODE_SLOT);

        final RecipeDisplayRewriter<ClientboundPacket1_21_2> recipeRewriter = new RecipeDisplayRewriter<>(protocol);
        recipeRewriter.registerUpdateRecipes(ClientboundPackets1_21_2.UPDATE_RECIPES);
        recipeRewriter.registerRecipeBookAdd(ClientboundPackets1_21_2.RECIPE_BOOK_ADD);
        recipeRewriter.registerPlaceGhostRecipe(ClientboundPackets1_21_2.PLACE_GHOST_RECIPE);
    }

    private void handleBlockEntity(final CompoundTag tag) {
        if (tag == null) {
            return;
        }

        final CompoundTag item = tag.getCompoundTag("item");
        if (item == null) {
            return;
        }

        final CompoundTag components = item.getCompoundTag("components");
        if (components == null) {
            return;
        }

        // May be displayed in brushable blocks and other block entities
        final NumberTag customModelData = TagUtil.getNamespacedNumberTag(components, "custom_model_data");
        if (customModelData != null) {
            final ListTag<FloatTag> floats = new ListTag<>(FloatTag.class);
            floats.add(new FloatTag(customModelData.asFloat()));

            final CompoundTag updatedCustomModelData = new CompoundTag();
            updatedCustomModelData.put("floats", floats);
            TagUtil.removeNamespaced(components, "custom_model_data");
            components.put("custom_model_data", updatedCustomModelData);
        }
    }

    @Override
    public Item handleItemToClient(final UserConnection connection, final Item item) {
        super.handleItemToClient(connection, item);

        final StructuredDataContainer dataContainer = item.dataContainer();
        final Integer modelData = dataContainer.get(StructuredDataKey.CUSTOM_MODEL_DATA1_20_5);
        if (modelData != null) {
            dataContainer.set(StructuredDataKey.CUSTOM_MODEL_DATA1_21_4, new CustomModelData1_21_4(
                new float[]{modelData.floatValue()},
                new boolean[0],
                new String[0],
                new int[0]
            ));
            saveTag(createCustomTag(item), new IntTag(modelData), "custom_model_data");
        }

        updateItemData(item);

        // Add data components to fix issues in older protocols
        appendItemDataFixComponents(connection, item);
        return item;
    }

    @Override
    public Item handleItemToServer(final UserConnection connection, final Item item) {
        super.handleItemToServer(connection, item);

        final StructuredDataContainer dataContainer = item.dataContainer();
        final CompoundTag customData = dataContainer.get(StructuredDataKey.CUSTOM_DATA);
        if (customData != null) {
            if (customData.remove(nbtTagName("custom_model_data")) instanceof final IntTag customModelData) {
                dataContainer.set(StructuredDataKey.CUSTOM_MODEL_DATA1_20_5, customModelData.asInt());
                removeCustomTag(dataContainer, customData);
            }
        }

        downgradeItemData(item);
        return item;
    }

    private void appendItemDataFixComponents(final UserConnection connection, final Item item) {
        final ProtocolVersion serverVersion = connection.getProtocolInfo().serverProtocolVersion();
        if (serverVersion.olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            if (item.identifier() == 849 || item.identifier() == 854 || item.identifier() == 859 || item.identifier() == 864 || item.identifier() == 869) { // swords
                // Make sword "eatable" to enable clientside instant blocking on 1.8. Set consume animation to block,
                // and consume time really high, so the eating animation doesn't play
                item.dataContainer().set(StructuredDataKey.CONSUMABLE1_21_2,
                    new Consumable1_21_2(
                        3600,
                        3,
                        Holder.of(new SoundEvent("minecraft:intentionally_empty", null)),
                        false,
                        new Consumable1_21_2.ConsumeEffect[0])
                );
            }
        }
    }

    public static void updateItemData(final Item item) {
        final StructuredDataContainer dataContainer = item.dataContainer();
        dataContainer.replaceKey(StructuredDataKey.CHARGED_PROJECTILES1_21_2, StructuredDataKey.CHARGED_PROJECTILES1_21_4);
        dataContainer.replaceKey(StructuredDataKey.BUNDLE_CONTENTS1_21_2, StructuredDataKey.BUNDLE_CONTENTS1_21_4);
        dataContainer.replaceKey(StructuredDataKey.CONTAINER1_21_2, StructuredDataKey.CONTAINER1_21_4);
        dataContainer.replaceKey(StructuredDataKey.USE_REMAINDER1_21_2, StructuredDataKey.USE_REMAINDER1_21_4);
        dataContainer.replaceKey(StructuredDataKey.TRIM1_21_2, StructuredDataKey.TRIM1_21_4);
        dataContainer.remove(StructuredDataKey.CUSTOM_MODEL_DATA1_20_5);
    }

    public static void downgradeItemData(final Item item) {
        final StructuredDataContainer dataContainer = item.dataContainer();
        dataContainer.replaceKey(StructuredDataKey.CHARGED_PROJECTILES1_21_4, StructuredDataKey.CHARGED_PROJECTILES1_21_2);
        dataContainer.replaceKey(StructuredDataKey.BUNDLE_CONTENTS1_21_4, StructuredDataKey.BUNDLE_CONTENTS1_21_2);
        dataContainer.replaceKey(StructuredDataKey.CONTAINER1_21_4, StructuredDataKey.CONTAINER1_21_2);
        dataContainer.replaceKey(StructuredDataKey.USE_REMAINDER1_21_4, StructuredDataKey.USE_REMAINDER1_21_2);
        dataContainer.replaceKey(StructuredDataKey.TRIM1_21_4, StructuredDataKey.TRIM1_21_2);
        dataContainer.remove(StructuredDataKey.CUSTOM_MODEL_DATA1_21_4);
    }
}
