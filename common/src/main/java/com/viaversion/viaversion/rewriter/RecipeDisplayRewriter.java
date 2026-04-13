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
package com.viaversion.viaversion.rewriter;

import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.rewriter.ItemRewriter;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Key;
import java.util.HashMap;
import java.util.Map;

// Base for 1.21.2 and onwards
public class RecipeDisplayRewriter<C extends ClientboundPacketType> {
    protected final Map<String, SlotDisplayConsumer> slotDisplayHandlers = new HashMap<>();
    protected final Protocol<C, ?, ?, ?> protocol;

    public RecipeDisplayRewriter(final Protocol<C, ?, ?, ?> protocol) {
        this.protocol = protocol;

        // empty and any_fuel are empty
        slotDisplayHandlers.put("with_any_potion", this::handleSlotDisplay);
        slotDisplayHandlers.put("only_with_component", this::handleOnlyWithComponentSlotDisplay);
        slotDisplayHandlers.put("item", this::handleItemId);
        slotDisplayHandlers.put("item_stack", this::handleItem);
        slotDisplayHandlers.put("tag", wrapper -> wrapper.passthrough(Types.STRING));
        slotDisplayHandlers.put("dyed", this::handleDyedSlotDisplay);
        slotDisplayHandlers.put("smithing_trim", this::handleSmithingTrimSlotDisplay);
        slotDisplayHandlers.put("with_remainder", this::handleWithRemainderSlotDisplay);
        slotDisplayHandlers.put("composite", this::handleSlotDisplayList);
    }

    public void registerUpdateRecipes(final C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            final int size = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < size; i++) {
                wrapper.passthrough(Types.STRING); // Recipe group
                rewriteItemIds(wrapper.passthrough(Types.VAR_INT_ARRAY_PRIMITIVE));
            }

            final int stonecutterRecipesSize = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < stonecutterRecipesSize; i++) {
                handleIngredient(wrapper);
                handleSlotDisplay(wrapper);
            }
        });
    }

    public void registerRecipeBookAdd(final C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            final int size = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < size; i++) {
                wrapper.passthrough(Types.VAR_INT); // Display ID
                handleRecipeDisplay(wrapper);
                wrapper.passthrough(Types.OPTIONAL_VAR_INT); // Group
                wrapper.passthrough(Types.VAR_INT); // Category
                if (wrapper.passthrough(Types.BOOLEAN)) {
                    final int ingredientsSize = wrapper.passthrough(Types.VAR_INT);
                    for (int j = 0; j < ingredientsSize; j++) {
                        handleIngredient(wrapper); // Items
                    }
                }
                wrapper.passthrough(Types.BYTE); // Flags
            }
        });
    }

    public void registerPlaceGhostRecipe(final C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Container ID
            handleRecipeDisplay(wrapper);
        });
    }

    protected void handleShapeless(final PacketWrapper wrapper) {
        handleSlotDisplayList(wrapper); // Ingredients
        handleSlotDisplay(wrapper); // Result
        handleSlotDisplay(wrapper); // Crafting station
    }

    protected void handleShaped(final PacketWrapper wrapper) {
        wrapper.passthrough(Types.VAR_INT); // Width
        wrapper.passthrough(Types.VAR_INT); // Height
        handleSlotDisplayList(wrapper); // Ingredients
        handleSlotDisplay(wrapper); // Result
        handleSlotDisplay(wrapper); // Crafting station
    }

    protected void handleFurnace(final PacketWrapper wrapper) {
        handleSlotDisplay(wrapper); // Ingredient
        handleSlotDisplay(wrapper); // Fuel
        handleSlotDisplay(wrapper); // Result
        handleSlotDisplay(wrapper); // Crafting station
        wrapper.passthrough(Types.VAR_INT); // Duration
        wrapper.passthrough(Types.FLOAT); // Experience
    }

    protected void handleStoneCutter(final PacketWrapper wrapper) {
        handleSlotDisplay(wrapper); // Input
        handleSlotDisplay(wrapper); // Result
        handleSlotDisplay(wrapper); // Crafting station
    }

    protected void handleSmithing(final PacketWrapper wrapper) {
        handleSlotDisplay(wrapper); // Template
        handleSlotDisplay(wrapper); // Base
        handleSlotDisplay(wrapper); // Addition
        handleSlotDisplay(wrapper); // Result
        handleSlotDisplay(wrapper); // Crafting station
    }

    protected void handleRecipeDisplay(final PacketWrapper wrapper) {
        final int type = wrapper.passthrough(Types.VAR_INT);
        switch (type) {
            case 0 -> handleShapeless(wrapper);
            case 1 -> handleShaped(wrapper);
            case 2 -> handleFurnace(wrapper);
            case 3 -> handleStoneCutter(wrapper);
            case 4 -> handleSmithing(wrapper);
        }
    }

    protected void handleSlotDisplay(final PacketWrapper wrapper) {
        final FullMappings mappings = protocol.getMappingData().getSlotDisplayMappings();
        final int type = wrapper.read(Types.VAR_INT);
        final int mappedType = mappings.getNewId(type);
        if (mappedType != -1) {
            wrapper.write(Types.VAR_INT, mappedType);
            runSlotDisplayHandler(wrapper, mappings, type);
        } else {
            // Write a dummy item if needed
            wrapper.write(Types.VAR_INT, 0);
            wrapper.consumeReadsOnly(() -> runSlotDisplayHandler(wrapper, mappings, type));
        }
    }

    private void runSlotDisplayHandler(final PacketWrapper wrapper, final FullMappings mappings, final int type) {
        final String identifier = mappings.identifier(type); // use the original type
        final SlotDisplayConsumer handler = slotDisplayHandlers.get(Key.stripMinecraftNamespace(identifier));
        if (handler != null) {
            handler.accept(wrapper);
        }
    }

    protected void handleSlotDisplayList(final PacketWrapper wrapper) {
        final int size = wrapper.passthrough(Types.VAR_INT);
        for (int i = 0; i < size; i++) {
            handleSlotDisplay(wrapper);
        }
    }

    protected void handleSmithingTrimSlotDisplay(final PacketWrapper wrapper) {
        handleSlotDisplay(wrapper); // Base
        handleSlotDisplay(wrapper); // Material
        handleSlotDisplay(wrapper); // Pattern
    }

    protected void handleWithRemainderSlotDisplay(final PacketWrapper wrapper) {
        handleSlotDisplay(wrapper); // Input
        handleSlotDisplay(wrapper); // Remainder
    }

    protected void handleOnlyWithComponentSlotDisplay(final PacketWrapper wrapper) {
        handleSlotDisplay(wrapper); // Source
        final int mappedDataComponentType = protocol.getMappingData().getDataComponentSerializerMappings().getNewIdOrDefault(wrapper.read(Types.VAR_INT), 0);
        wrapper.write(Types.VAR_INT, mappedDataComponentType);
    }

    protected void handleDyedSlotDisplay(final PacketWrapper wrapper) {
        handleSlotDisplay(wrapper); // Dye
        handleSlotDisplay(wrapper); // Target
    }

    protected void handleIngredient(final PacketWrapper wrapper) {
        final HolderSet items = wrapper.passthrough(Types.HOLDER_SET);
        if (items.hasTagKey()) {
            return;
        }

        final int[] ids = items.ids();
        for (int i = 0; i < ids.length; i++) {
            ids[i] = rewriteItemId(ids[i]);
        }
    }

    protected void handleItemId(final PacketWrapper wrapper) {
        final int id = wrapper.read(Types.VAR_INT);
        wrapper.write(Types.VAR_INT, rewriteItemId(id));
    }

    protected void handleItem(final PacketWrapper wrapper) {
        final ItemRewriter<?> itemRewriter = protocol.getItemRewriter();
        final Item item = wrapper.read(itemRewriter.itemTemplateType());
        itemRewriter.handleItemToClient(wrapper.user(), item);
        wrapper.write(itemRewriter.mappedItemTemplateType(), item);
    }

    protected int rewriteItemId(final int id) {
        final MappingData mappingData = protocol.getMappingData();
        if (mappingData != null && mappingData.getItemMappings() != null) {
            return mappingData.getItemMappings().getNewIdOrDefault(id, id);
        }
        return id;
    }

    protected void rewriteItemIds(final int[] ids) {
        for (int i = 0; i < ids.length; i++) {
            final int id = ids[i];
            ids[i] = rewriteItemId(id);
        }
    }

    @FunctionalInterface
    public interface SlotDisplayConsumer {

        void accept(PacketWrapper wrapper);
    }
}
