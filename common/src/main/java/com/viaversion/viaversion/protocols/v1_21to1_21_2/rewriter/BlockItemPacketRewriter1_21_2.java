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

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.Consumable1_21_2;
import com.viaversion.viaversion.api.minecraft.item.data.DamageResistant;
import com.viaversion.viaversion.api.minecraft.item.data.FoodProperties1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.FoodProperties1_21_2;
import com.viaversion.viaversion.api.minecraft.item.data.Instrument1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.Instrument1_21_2;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.api.type.types.version.Types1_21;
import com.viaversion.viaversion.api.type.types.version.Types1_21_2;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPacket1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.Protocol1_21To1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ServerboundPacket1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ServerboundPackets1_21_2;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StructuredItemRewriter;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.TagUtil;
import com.viaversion.viaversion.util.Unit;
import java.util.Set;

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

        registerAdvancements1_20_3(ClientboundPackets1_21.UPDATE_ADVANCEMENTS);
        registerSetEquipment(ClientboundPackets1_21.SET_EQUIPMENT);
        registerMerchantOffers1_20_5(ClientboundPackets1_21.MERCHANT_OFFERS);
        registerSetCreativeModeSlot(ServerboundPackets1_21_2.SET_CREATIVE_MODE_SLOT);
        registerLevelParticles1_20_5(ClientboundPackets1_21.LEVEL_PARTICLES);

        protocol.registerClientbound(ClientboundPackets1_21.COOLDOWN, wrapper -> {
            final MappingData mappingData = protocol.getMappingData();
            final int itemId = wrapper.read(Types.VAR_INT);
            final int mappedItemId = mappingData.getNewItemId(itemId);
            wrapper.write(Types.STRING, mappingData.getFullItemMappings().mappedIdentifier(mappedItemId));
        });

        protocol.registerClientbound(ClientboundPackets1_21.CONTAINER_SET_CONTENT, wrapper -> {
            updateContainerId(wrapper);
            wrapper.passthrough(Types.VAR_INT); // State id
            final Item[] items = wrapper.read(itemArrayType());
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
        protocol.registerClientbound(ClientboundPackets1_21.SET_CARRIED_ITEM, ClientboundPackets1_21_2.SET_HELD_SLOT);
        protocol.registerServerbound(ServerboundPackets1_21_2.CONTAINER_CLOSE, this::updateContainerIdServerbound);
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
        protocol.registerClientbound(ClientboundPackets1_21.PLACE_GHOST_RECIPE, wrapper -> {
            this.updateContainerId(wrapper);

            final String recipe = wrapper.read(Types.STRING);
            wrapper.write(Types.VAR_INT, recipeDisplay(recipe)); // TODO
        });
        protocol.registerServerbound(ServerboundPackets1_21_2.PLACE_RECIPE, wrapper -> {
            this.updateContainerIdServerbound(wrapper);

            final String recipe = wrapper.read(Types.STRING);
            wrapper.write(Types.VAR_INT, 0); // TODO Display id, from recipe packet
            wrapper.cancel();
        });
        protocol.registerServerbound(ServerboundPackets1_21_2.RECIPE_BOOK_SEEN_RECIPE, wrapper -> {
            this.updateContainerIdServerbound(wrapper);

            final String recipe = wrapper.read(Types.STRING);
            wrapper.write(Types.VAR_INT, 0); // TODO Display id, from recipe packet
            wrapper.cancel();
        });

        protocol.registerServerbound(ServerboundPackets1_21_2.USE_ITEM_ON, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Hand
            wrapper.passthrough(Types.BLOCK_POSITION1_14); // Block position
            wrapper.passthrough(Types.VAR_INT); // Direction
            wrapper.passthrough(Types.FLOAT); // X
            wrapper.passthrough(Types.FLOAT); // Y
            wrapper.passthrough(Types.FLOAT); // Z
            wrapper.passthrough(Types.BOOLEAN); // Inside
            wrapper.read(Types.BOOLEAN); // World border hit
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

        protocol.registerClientbound(ClientboundPackets1_21.UPDATE_RECIPES, wrapper -> {
            final RecipeRewriter rewriter = new RecipeRewriter(protocol);
            wrapper.cancel(); // TODO

            final int size = wrapper.passthrough(Types.VAR_INT);
            int newSize = size;
            for (int i = 0; i < size; i++) {
                final String recipeIdentifier = wrapper.read(Types.STRING);

                final FullMappings recipeSerializerMappings = protocol.getMappingData().getRecipeSerializerMappings();
                final int typeId = wrapper.read(Types.VAR_INT);
                final int mappedId = recipeSerializerMappings.getNewId(typeId);
                if (mappedId != -1) {
                    wrapper.write(Types.STRING, recipeIdentifier);
                    wrapper.write(Types.VAR_INT, mappedId);
                } else {
                    wrapper.set(Types.VAR_INT, 0, --newSize);
                }

                rewriter.handleRecipeType(wrapper, Key.stripMinecraftNamespace(recipeSerializerMappings.identifier(typeId))); // Use the original
            }
        });

        protocol.registerClientbound(ClientboundPackets1_21.RECIPE, ClientboundPackets1_21_2.RECIPE_BOOK_ADD, wrapper -> {
            final int state = wrapper.passthrough(Types.VAR_INT);

            // Pairs of open + filtering for: Crafting, furnace, blast furnace, smoker
            final PacketWrapper settingsPacket = wrapper.create(ClientboundPackets1_21_2.RECIPE_BOOK_SETTINGS);
            for (int i = 0; i < 4 * 2; i++) {
                settingsPacket.write(Types.BOOLEAN, wrapper.read(Types.BOOLEAN));
            }
            settingsPacket.send(Protocol1_21To1_21_2.class);

            // TODO
            wrapper.cancel();

            final String[] recipes = wrapper.passthrough(Types.STRING_ARRAY);
            Set<String> toHighlight = Set.of();
            if (state == 0) { // Init (1=Add, 2=Remove)
                final String[] highlightRecipes = wrapper.passthrough(Types.STRING_ARRAY);
                toHighlight = Set.of(highlightRecipes);
            }

            wrapper.write(Types.VAR_INT, recipes.length);
            for (final String recipe : recipes) {
                // Display id
                // Display type
                // Optional group
                // Category type
                // Item contents

                byte flags = 0;
                if (toHighlight.contains(recipe)) {
                    flags |= (1 << 1); // Highlight
                }
                wrapper.write(Types.BYTE, flags);
            }
        });
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

    private int recipeDisplay(String type) {
        type = Key.stripMinecraftNamespace(type);
        return switch (type) {
            case "crafting_shapeless" -> 0;
            case "crafting_shaped" -> 1;
            case "furnace" -> 2;
            case "stonecutter" -> 3;
            case "smithing" -> 4;
            default -> 0;
        };
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
            return Holder.of(new Instrument1_21_2(value.soundEvent(), value.useDuration() / 20F, value.range(), new StringTag("")));
        });
        dataContainer.replace(StructuredDataKey.FOOD1_21, StructuredDataKey.FOOD1_21_2, food -> {
            // Just assume the item type default for CONSUMABLE; add USE_REMAINDER from old food properties
            if (food.usingConvertsTo() != null) {
                dataContainer.set(StructuredDataKey.USE_REMAINDER, food.usingConvertsTo());
            }
            return new FoodProperties1_21_2(food.nutrition(), food.saturationModifier(), food.canAlwaysEat());
        });
        dataContainer.replaceKey(StructuredDataKey.CONTAINER1_21, StructuredDataKey.CONTAINER1_21_2);
        dataContainer.replaceKey(StructuredDataKey.CHARGED_PROJECTILES1_21, StructuredDataKey.CHARGED_PROJECTILES1_21_2);
        dataContainer.replaceKey(StructuredDataKey.BUNDLE_CONTENTS1_21, StructuredDataKey.BUNDLE_CONTENTS1_21_2);
        dataContainer.replaceKey(StructuredDataKey.POTION_CONTENTS1_20_5, StructuredDataKey.POTION_CONTENTS1_21_2);
        dataContainer.replace(StructuredDataKey.FIRE_RESISTANT, StructuredDataKey.DAMAGE_RESISTANT, fireResistant -> new DamageResistant("minecraft:is_fire"));
        dataContainer.replace(StructuredDataKey.LOCK, lock -> {
            final CompoundTag predicateTag = new CompoundTag();
            final CompoundTag itemComponentsTag = new CompoundTag();
            predicateTag.put("components", itemComponentsTag);
            itemComponentsTag.put("custom_name", lock);
            return predicateTag;
        });
    }

    public static void downgradeItemData(final Item item) {
        final StructuredDataContainer dataContainer = item.dataContainer();
        dataContainer.replace(StructuredDataKey.LOCK, StructuredDataKey.LOCK, lock -> {
            final CompoundTag predicateTag = (CompoundTag) lock;
            final CompoundTag itemComponentsTag = predicateTag.getCompoundTag("components");
            if (itemComponentsTag != null) {
                return TagUtil.getNamespacedStringTag(itemComponentsTag, "custom_name");
            }
            return null;
        });
        dataContainer.replace(StructuredDataKey.INSTRUMENT1_21_2, StructuredDataKey.INSTRUMENT1_20_5, instrument -> {
            if (instrument.hasId()) {
                return Holder.of(instrument.id());
            }
            final Instrument1_21_2 value = instrument.value();
            return Holder.of(new Instrument1_20_5(value.soundEvent(), (int) (value.useDuration() * 20), value.range()));
        });
        dataContainer.replace(StructuredDataKey.FOOD1_21_2, StructuredDataKey.FOOD1_21, food -> {
            final Consumable1_21_2 consumableData = dataContainer.get(StructuredDataKey.CONSUMABLE1_21_2);
            final Item useRemainderData = dataContainer.get(StructuredDataKey.USE_REMAINDER);
            final float eatSeconds = consumableData != null ? consumableData.consumeSeconds() : 1.6F;
            return new FoodProperties1_20_5(food.nutrition(), food.saturationModifier(), food.canAlwaysEat(), eatSeconds, useRemainderData, new FoodProperties1_20_5.FoodEffect[0]);
        });
        dataContainer.replaceKey(StructuredDataKey.CONTAINER1_21_2, StructuredDataKey.CONTAINER1_21);
        dataContainer.replaceKey(StructuredDataKey.CHARGED_PROJECTILES1_21_2, StructuredDataKey.CHARGED_PROJECTILES1_21);
        dataContainer.replaceKey(StructuredDataKey.BUNDLE_CONTENTS1_21_2, StructuredDataKey.BUNDLE_CONTENTS1_21);
        dataContainer.replaceKey(StructuredDataKey.POTION_CONTENTS1_21_2, StructuredDataKey.POTION_CONTENTS1_20_5);
        dataContainer.replace(StructuredDataKey.DAMAGE_RESISTANT, StructuredDataKey.FIRE_RESISTANT, damageResistant -> {
            if (Key.stripMinecraftNamespace(damageResistant.typesTagKey()).equals("is_fire")) {
                return Unit.INSTANCE;
            }
            return null;
        });
        dataContainer.remove(StructuredDataKey.REPAIRABLE);
        dataContainer.remove(StructuredDataKey.ENCHANTABLE);
        dataContainer.remove(StructuredDataKey.CONSUMABLE1_21_2);
        dataContainer.remove(StructuredDataKey.USE_REMAINDER);
        dataContainer.remove(StructuredDataKey.USE_COOLDOWN);
        dataContainer.remove(StructuredDataKey.ITEM_MODEL);
        dataContainer.remove(StructuredDataKey.EQUIPPABLE);
        dataContainer.remove(StructuredDataKey.GLIDER);
        dataContainer.remove(StructuredDataKey.TOOLTIP_STYLE);
        dataContainer.remove(StructuredDataKey.DEATH_PROTECTION);
    }
}
