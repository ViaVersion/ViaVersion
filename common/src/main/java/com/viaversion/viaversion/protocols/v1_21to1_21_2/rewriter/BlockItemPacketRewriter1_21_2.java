/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_21to1_21_2.rewriter;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.Instrument1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.Instrument1_21_2;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.api.type.types.version.Types1_21;
import com.viaversion.viaversion.api.type.types.version.Types1_21_2;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.rewriter.RecipeRewriter1_20_3;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPacket1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.Protocol1_21To1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ServerboundPacket1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ServerboundPackets1_21_2;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StructuredItemRewriter;

public final class BlockItemPacketRewriter1_21_2 extends StructuredItemRewriter<ClientboundPacket1_21, ServerboundPacket1_21_2, Protocol1_21To1_21_2> {

    public BlockItemPacketRewriter1_21_2(final Protocol1_21To1_21_2 protocol) {
        super(protocol,
            Types1_21.ITEM, Types1_21.ITEM_ARRAY, Types1_21_2.ITEM, Types1_21_2.ITEM_ARRAY,
            Types1_21.ITEM_COST, Types1_21.OPTIONAL_ITEM_COST, Types1_21_2.ITEM_COST, Types1_21_2.OPTIONAL_ITEM_COST,
            Types1_21.PARTICLE, Types1_21_2.PARTICLE
        );
    }

    @Override
    public void registerPackets() {
        final BlockRewriter<ClientboundPacket1_21> blockRewriter = BlockRewriter.for1_20_2(protocol);
        blockRewriter.registerBlockEvent(ClientboundPackets1_21.BLOCK_EVENT);
        blockRewriter.registerBlockUpdate(ClientboundPackets1_21.BLOCK_UPDATE);
        blockRewriter.registerSectionBlocksUpdate1_20(ClientboundPackets1_21.SECTION_BLOCKS_UPDATE);
        blockRewriter.registerLevelEvent1_21(ClientboundPackets1_21.LEVEL_EVENT, 2001);
        blockRewriter.registerLevelChunk1_19(ClientboundPackets1_21.LEVEL_CHUNK_WITH_LIGHT, ChunkType1_20_2::new);
        blockRewriter.registerBlockEntityData(ClientboundPackets1_21.BLOCK_ENTITY_DATA);

        registerCooldown(ClientboundPackets1_21.COOLDOWN);
        registerAdvancements1_20_3(ClientboundPackets1_21.UPDATE_ADVANCEMENTS);
        registerSetEquipment(ClientboundPackets1_21.SET_EQUIPMENT);
        registerMerchantOffers1_20_5(ClientboundPackets1_21.MERCHANT_OFFERS);
        registerSetCreativeModeSlot(ServerboundPackets1_21_2.SET_CREATIVE_MODE_SLOT);
        registerLevelParticles1_20_5(ClientboundPackets1_21.LEVEL_PARTICLES);

        protocol.registerClientbound(ClientboundPackets1_21.CONTAINER_SET_CONTENT, wrapper -> {
            updateContainerId(wrapper);
            wrapper.passthrough(Types.VAR_INT); // State id
            Item[] items = wrapper.read(itemArrayType());
            wrapper.write(mappedItemArrayType(), items);
            for (int i = 0; i < items.length; i++) {
                items[i] = handleItemToClient(wrapper.user(), items[i]);
            }
            passthroughClientboundItem(wrapper);
        });
        protocol.registerClientbound(ClientboundPackets1_21.CONTAINER_SET_SLOT, wrapper -> {
            updateContainerId(wrapper);
            wrapper.passthrough(Types.VAR_INT); // State id
            wrapper.passthrough(Types.SHORT); // Slot id
            passthroughClientboundItem(wrapper);
        });
        protocol.registerClientbound(ClientboundPackets1_21.CONTAINER_CLOSE, this::updateContainerId);
        protocol.registerClientbound(ClientboundPackets1_21.CONTAINER_SET_DATA, this::updateContainerId);
        protocol.registerClientbound(ClientboundPackets1_21.HORSE_SCREEN_OPEN, this::updateContainerId);
        protocol.registerClientbound(ClientboundPackets1_21.PLACE_GHOST_RECIPE, this::updateContainerId);
        protocol.registerClientbound(ClientboundPackets1_21.SET_CARRIED_ITEM, ClientboundPackets1_21_2.SET_HELD_SLOT);
        protocol.registerServerbound(ServerboundPackets1_21_2.CONTAINER_CLOSE, this::updateContainerIdServerbound);
        protocol.registerServerbound(ServerboundPackets1_21_2.PLACE_RECIPE, this::updateContainerIdServerbound);
        protocol.registerServerbound(ServerboundPackets1_21_2.CONTAINER_CLICK, wrapper -> {
            updateContainerIdServerbound(wrapper);
            wrapper.passthrough(Types.VAR_INT); // State id
            wrapper.passthrough(Types.SHORT); // Slot
            wrapper.passthrough(Types.BYTE); // Button
            wrapper.passthrough(Types.VAR_INT); // Mode
            final int length = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < length; i++) {
                wrapper.passthrough(Types.SHORT); // Slot
                passthroughServerboundItem(wrapper);
            }
            passthroughServerboundItem(wrapper);
        });

        protocol.registerClientbound(ClientboundPackets1_21.EXPLODE, wrapper -> {
            wrapper.passthrough(Types.DOUBLE); // Center X
            wrapper.passthrough(Types.DOUBLE); // Center Y
            wrapper.passthrough(Types.DOUBLE); // Center Z

            final float power = wrapper.read(Types.FLOAT); // Power
            final int blocks = wrapper.read(Types.VAR_INT);
            for (int i = 0; i < blocks; i++) {
                // TODO ?
                wrapper.read(Types.BYTE); // Relative X
                wrapper.read(Types.BYTE); // Relative Y
                wrapper.read(Types.BYTE); // Relative Z
            }

            final float knockbackX = wrapper.read(Types.FLOAT);
            final float knockbackY = wrapper.read(Types.FLOAT);
            final float knockbackZ = wrapper.read(Types.FLOAT);
            if (knockbackX != 0 && knockbackY != 0 && knockbackZ != 0) {
                wrapper.write(Types.BOOLEAN, true);
                wrapper.write(Types.DOUBLE, (double) knockbackX);
                wrapper.write(Types.DOUBLE, (double) knockbackY);
                wrapper.write(Types.DOUBLE, (double) knockbackZ);
            } else {
                wrapper.write(Types.BOOLEAN, false);
            }

            wrapper.read(Types.VAR_INT); // Block interaction type

            final Particle smallExplosionParticle = wrapper.read(Types1_21.PARTICLE);
            final Particle largeExplosionParticle = wrapper.read(Types1_21.PARTICLE);
            if (power >= 2.0F && blocks != 0) {
                rewriteParticle(wrapper.user(), largeExplosionParticle);
                wrapper.write(Types1_21_2.PARTICLE, largeExplosionParticle);
            } else {
                rewriteParticle(wrapper.user(), smallExplosionParticle);
                wrapper.write(Types1_21_2.PARTICLE, smallExplosionParticle);
            }

            new SoundRewriter<>(protocol).soundHolderHandler().handle(wrapper);
        });

        new RecipeRewriter1_20_3<>(protocol) {
            @Override
            protected void handleIngredient(final PacketWrapper wrapper) {
                wrapper.write(Types.HOLDER_SET, ingredient(wrapper));
            }

            @Override
            public void handleCraftingShaped(final PacketWrapper wrapper) {
                wrapper.passthrough(Types.STRING); // Group
                wrapper.passthrough(Types.VAR_INT); // Crafting book category
                final int width = wrapper.passthrough(Types.VAR_INT);
                final int height = wrapper.passthrough(Types.VAR_INT);
                final int ingredients = width * height;

                wrapper.write(Types.VAR_INT, ingredients);
                for (int i = 0; i < ingredients; i++) {
                    wrapper.write(Types.HOLDER_SET, ingredient(wrapper));
                }

                wrapper.write(mappedItemType(), rewrite(wrapper.user(), wrapper.read(itemType()))); // Result
                wrapper.passthrough(Types.BOOLEAN); // Show notification
            }

            @Override
            public void handleCraftingShapeless(final PacketWrapper wrapper) {
                wrapper.passthrough(Types.STRING); // Group
                wrapper.passthrough(Types.VAR_INT); // Crafting book category

                final int ingredients = wrapper.read(Types.VAR_INT);
                final HolderSet[] ingredient = new HolderSet[ingredients];
                for (int i = 0; i < ingredients; i++) {
                    ingredient[i] = ingredient(wrapper);
                }

                wrapper.write(mappedItemType(), rewrite(wrapper.user(), wrapper.read(itemType())));

                // Also moved below here
                wrapper.write(Types.VAR_INT, ingredients);
                for (final HolderSet item : ingredient) {
                    wrapper.write(Types.HOLDER_SET, item);
                }
            }

            private HolderSet ingredient(final PacketWrapper wrapper) {
                final Item[] items = wrapper.read(itemArrayType());
                final int[] ids = new int[items.length];
                for (int i = 0; i < items.length; i++) {
                    final Item item = rewrite(wrapper.user(), items[i]);
                    ids[i] = item.identifier();
                }
                return HolderSet.of(ids);
            }

            @Override
            public void handleRecipeType(final PacketWrapper wrapper, final String type) {
                if (type.equals("crafting_special_suspiciousstew")) {
                    wrapper.read(Types.VAR_INT); // Crafting book category
                } else {
                    super.handleRecipeType(wrapper, type);
                }
            }
        }.register1_20_5(ClientboundPackets1_21.UPDATE_RECIPES);
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

    private void updateContainerId(final PacketWrapper wrapper) {
        // Container id handling was always a bit whack with most reading them as unsigned bytes, some as bytes, some already as var ints.
        // In VV they're generally read as unsigned bytesto not have to look the type up every time, but we need to make sure they're
        // properly converted to ints when used
        final short containerId = wrapper.read(Types.UNSIGNED_BYTE);
        final int intId = (byte) containerId;
        wrapper.write(Types.VAR_INT, intId);
    }

    private void updateContainerIdServerbound(final PacketWrapper wrapper) {
        final int containerId = wrapper.read(Types.VAR_INT);
        wrapper.write(Types.UNSIGNED_BYTE, (short) containerId);
    }

    public static void updateItemData(final Item item) {
        final StructuredDataContainer dataContainer = item.dataContainer();
        dataContainer.replace(StructuredDataKey.INSTRUMENT1_20_5, StructuredDataKey.INSTRUMENT1_21_2, instrument -> {
            if (instrument.hasId()) {
                return Holder.of(instrument.id());
            }
            final Instrument1_20_5 value = instrument.value();
            return Holder.of(new Instrument1_21_2(value.soundEvent(), value.useDuration(), value.range(), null));
        });
        dataContainer.replaceKey(StructuredDataKey.CONTAINER1_21, StructuredDataKey.CONTAINER1_21_2);
        dataContainer.replaceKey(StructuredDataKey.CHARGED_PROJECTILES1_21, StructuredDataKey.CHARGED_PROJECTILES1_21_2);
        dataContainer.replaceKey(StructuredDataKey.BUNDLE_CONTENTS1_21, StructuredDataKey.BUNDLE_CONTENTS1_21_2);
    }

    public static void downgradeItemData(final Item item) {
        final StructuredDataContainer dataContainer = item.dataContainer();
        dataContainer.replace(StructuredDataKey.INSTRUMENT1_21_2, StructuredDataKey.INSTRUMENT1_20_5, instrument -> {
            if (instrument.hasId()) {
                return Holder.of(instrument.id());
            }
            final Instrument1_21_2 value = instrument.value();
            return Holder.of(new Instrument1_20_5(value.soundEvent(), (int) value.useDuration(), value.range()));
        });
        dataContainer.replaceKey(StructuredDataKey.CONTAINER1_21_2, StructuredDataKey.CONTAINER1_21);
        dataContainer.replaceKey(StructuredDataKey.CHARGED_PROJECTILES1_21_2, StructuredDataKey.CHARGED_PROJECTILES1_21);
        dataContainer.replaceKey(StructuredDataKey.BUNDLE_CONTENTS1_21_2, StructuredDataKey.BUNDLE_CONTENTS1_21);
        dataContainer.remove(StructuredDataKey.REPAIRABLE);
        dataContainer.remove(StructuredDataKey.ENCHANTABLE);
    }
}
