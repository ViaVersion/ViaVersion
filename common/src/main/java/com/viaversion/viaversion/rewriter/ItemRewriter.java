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
package com.viaversion.viaversion.rewriter;

import com.google.gson.JsonElement;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.data.item.ItemHasher;
import com.viaversion.viaversion.api.minecraft.item.HashedItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.rewriter.ComponentRewriter;
import com.viaversion.viaversion.api.rewriter.RewriterBase;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypesHolder;
import com.viaversion.viaversion.data.item.ItemHasherBase;
import com.viaversion.viaversion.util.Limit;
import com.viaversion.viaversion.util.Rewritable;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ItemRewriter<C extends ClientboundPacketType, S extends ServerboundPacketType,
    T extends Protocol<C, ?, ?, S>> extends RewriterBase<T> implements com.viaversion.viaversion.api.rewriter.ItemRewriter<T> {
    private final Type<Item> itemType;
    private final Type<Item> mappedItemType;
    private final Type<Item[]> itemArrayType;
    private final Type<Item[]> mappedItemArrayType;
    private final Type<Item> itemCostType;
    private final Type<Item> mappedItemCostType;
    private final Type<Item> optionalItemCostType;
    private final Type<Item> mappedOptionalItemCostType;

    public ItemRewriter(T protocol) {
        super(protocol);
        final VersionedTypesHolder types = protocol.types();
        final VersionedTypesHolder mappedTypes = protocol.mappedTypes();
        this.itemType = types.item();
        this.itemArrayType = types.itemArray();
        this.mappedItemType = mappedTypes.item();
        this.mappedItemArrayType = mappedTypes.itemArray();
        this.itemCostType = types.itemCost();
        this.mappedItemCostType = mappedTypes.itemCost();
        this.optionalItemCostType = types.optionalItemCost();
        this.mappedOptionalItemCostType = mappedTypes.optionalItemCost();
    }

    public ItemRewriter(T protocol, Type<Item> itemType, Type<Item[]> itemArrayType, Type<Item> mappedItemType, Type<Item[]> mappedItemArrayType) {
        super(protocol);
        this.itemType = itemType;
        this.itemArrayType = itemArrayType;
        this.mappedItemType = mappedItemType;
        this.mappedItemArrayType = mappedItemArrayType;
        this.itemCostType = null;
        this.mappedItemCostType = null;
        this.optionalItemCostType = null;
        this.mappedOptionalItemCostType = null;
    }

    public ItemRewriter(T protocol, Type<Item> itemType, Type<Item[]> itemArrayType) {
        this(protocol, itemType, itemArrayType, itemType, itemArrayType);
    }

    @Override
    public @Nullable Item handleItemToClient(final UserConnection connection, @Nullable Item item) {
        if (item == null) return null;
        if (protocol.getMappingData() != null && protocol.getMappingData().getItemMappings() != null) {
            item.setIdentifier(protocol.getMappingData().getNewItemId(item.identifier()));
        }
        return item;
    }

    @Override
    public @Nullable Item handleItemToServer(final UserConnection connection, @Nullable Item item) {
        if (item == null) return null;
        if (protocol.getMappingData() != null && protocol.getMappingData().getItemMappings() != null) {
            item.setIdentifier(protocol.getMappingData().getOldItemId(item.identifier()));
        }
        return item;
    }

    @Override
    public HashedItem handleHashedItem(final UserConnection connection, final HashedItem item) {
        final MappingData mappingData = protocol.getMappingData();
        if (mappingData == null) {
            return item;
        }

        final FullMappings dataComponentMappings = mappingData.getDataComponentSerializerMappings();
        if (dataComponentMappings != null) {
            updateHashedItemDataComponentIds(item, dataComponentMappings.inverse());

            final int customDataId = dataComponentMappings.id("custom_data");
            if (item.dataHashesById().containsKey(customDataId)) {
                // Use the original hashed item if we can find it in the cache
                final int customDataHash = item.dataHashesById().get(customDataId);
                final ItemHasherBase itemHasher = itemHasher(connection);
                final HashedItem originalHashedItem = itemHasher.originalHashedItem(customDataHash, item);
                if (originalHashedItem != null) {
                    return originalHashedItem;
                }
            }
        }

        if (mappingData.getItemMappings() != null) {
            item.setIdentifier(mappingData.getOldItemId(item.identifier()));
        }
        return item;
    }

    public void registerSetContent(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.UNSIGNED_BYTE); // Container id
            Item[] items = wrapper.passthroughAndMap(itemArrayType, mappedItemArrayType);
            for (int i = 0; i < items.length; i++) {
                items[i] = handleItemToClient(wrapper.user(), items[i]);
            }
        });
    }

    public void registerSetContent1_17_1(C packetType) {
        registerSetContent1_17_1(packetType, Types.UNSIGNED_BYTE);
    }

    public void registerSetContent1_21_2(C packetType) {
        registerSetContent1_17_1(packetType, Types.VAR_INT);
    }

    private void registerSetContent1_17_1(C packetType, Type<? extends Number> containerIdType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(containerIdType); // Container id
            wrapper.passthrough(Types.VAR_INT); // State id
            Item[] items = wrapper.passthroughAndMap(itemArrayType, mappedItemArrayType);
            for (int i = 0; i < items.length; i++) {
                items[i] = handleItemToClientAndTrackHash(wrapper.user(), items[i]);
            }

            passthroughClientboundItemAndTrackHash(wrapper);
        });
    }

    public void registerOpenScreen(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Container id
            handleMenuType(wrapper);
        });
    }

    public void handleMenuType(final PacketWrapper wrapper) {
        final int windowType = wrapper.read(Types.VAR_INT);
        final int mappedId = protocol.getMappingData().getMenuMappings().getNewId(windowType);
        if (mappedId == -1) {
            wrapper.cancel();
            return;
        }

        wrapper.write(Types.VAR_INT, mappedId);
    }

    public void registerSetSlot(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.BYTE); // Container id
            wrapper.passthrough(Types.SHORT); // Slot id
            passthroughClientboundItem(wrapper);
        });
    }

    public void registerSetSlot1_17_1(C packetType) {
        registerSetSlot1_17_1(packetType, Types.BYTE);
    }

    public void registerSetSlot1_21_2(C packetType) {
        registerSetSlot1_17_1(packetType, Types.VAR_INT);
    }

    private void registerSetSlot1_17_1(C packetType, Type<? extends Number> containerIdType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(containerIdType); // Container id
            wrapper.passthrough(Types.VAR_INT); // State id
            wrapper.passthrough(Types.SHORT); // Slot id
            passthroughClientboundItemAndTrackHash(wrapper);
        });
    }

    // Sub 1.16
    public void registerSetEquippedItem(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Entity ID
            wrapper.passthrough(Types.VAR_INT); // Slot ID
            passthroughClientboundItem(wrapper);
        });
    }

    // 1.16+
    public void registerSetEquipment(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Entity ID

            byte slot;
            do {
                slot = wrapper.passthrough(Types.BYTE);
                // & 0x7F into an extra variable if slot is needed
                passthroughClientboundItem(wrapper);
            } while (slot < 0);
        });
    }

    public void registerSetCreativeModeSlot(S packetType) {
        protocol.registerServerbound(packetType, wrapper -> {
            wrapper.passthrough(Types.SHORT); // Slot
            wrapper.write(itemType, handleItemToServer(wrapper.user(), wrapper.read(mappedItemType)));
        });
    }

    public void registerContainerClick(S packetType) {
        protocol.registerServerbound(packetType, wrapper -> {
            wrapper.passthrough(Types.BYTE); // Container ID
            wrapper.passthrough(Types.SHORT); // Slot
            wrapper.passthrough(Types.BYTE); // Button
            wrapper.passthrough(Types.SHORT); // Action number
            wrapper.passthrough(Types.VAR_INT); // Mode
            wrapper.write(itemType, handleItemToServer(wrapper.user(), wrapper.read(mappedItemType)));
        });
    }

    public void registerContainerClick1_17_1(S packetType) {
        registerContainerClick1_17_1(packetType, Types.BYTE);
    }

    public void registerContainerClick1_21_2(S packetType) {
        registerContainerClick1_17_1(packetType, Types.VAR_INT);
    }

    public void registerContainerClick1_17_1(S packetType, Type<? extends Number> containerIdType) {
        protocol.registerServerbound(packetType, wrapper -> {
            wrapper.passthrough(containerIdType); // Container id
            wrapper.passthrough(Types.VAR_INT); // State id
            wrapper.passthrough(Types.SHORT); // Slot
            wrapper.passthrough(Types.BYTE); // Button
            wrapper.passthrough(Types.VAR_INT); // Mode

            // Affected items
            final int length = Limit.max(wrapper.passthrough(Types.VAR_INT), 128);
            for (int i = 0; i < length; i++) {
                wrapper.passthrough(Types.SHORT); // Slot
                wrapper.write(itemType, handleItemToServer(wrapper.user(), wrapper.read(mappedItemType)));
            }

            // Carried item
            wrapper.write(itemType, handleItemToServer(wrapper.user(), wrapper.read(mappedItemType)));
        });
    }

    public void registerContainerClick1_21_5(S packetType) {
        protocol.registerServerbound(packetType, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Container id
            wrapper.passthrough(Types.VAR_INT); // State id
            wrapper.passthrough(Types.SHORT); // Slot
            wrapper.passthrough(Types.BYTE); // Button
            wrapper.passthrough(Types.VAR_INT); // Mode
            final int affectedItems = Limit.max(wrapper.passthrough(Types.VAR_INT), 128);
            for (int i = 0; i < affectedItems; i++) {
                wrapper.passthrough(Types.SHORT); // Slot
                passthroughHashedItem(wrapper);
            }
            passthroughHashedItem(wrapper); // Carried item
        });
    }

    public void registerSetPlayerInventory(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Slot
            passthroughClientboundItemAndTrackHash(wrapper);
        });
    }

    public void registerCooldown(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            int itemId = wrapper.read(Types.VAR_INT);
            wrapper.write(Types.VAR_INT, protocol.getMappingData().getNewItemId(itemId));
        });
    }

    public void registerCooldown1_21_2(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            String itemIdentifier = wrapper.read(Types.STRING);
            if (itemIdentifier != null) {
                itemIdentifier = Rewritable.mappedIdentifier(protocol.getMappingData().getFullItemMappings(), itemIdentifier);
            }
            wrapper.write(Types.STRING, itemIdentifier);
        });
    }

    public void registerCustomPayloadTradeList(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            protected void register() {
                map(Types.STRING); // 0 - Channel
                handlerSoftFail(wrapper -> {
                    final String channel = wrapper.get(Types.STRING, 0);
                    if (channel.equals("MC|TrList")) {
                        handleTradeList(wrapper);
                    }
                });
            }
        });
    }

    public void handleTradeList(final PacketWrapper wrapper) {
        wrapper.passthrough(Types.INT); // Window ID

        final int size = wrapper.passthrough(Types.UNSIGNED_BYTE);
        for (int i = 0; i < size; i++) {
            passthroughClientboundItem(wrapper); // Input Item
            passthroughClientboundItem(wrapper); // Output Item

            if (wrapper.passthrough(Types.BOOLEAN)) {
                passthroughClientboundItem(wrapper); // Second Item
            }

            wrapper.passthrough(Types.BOOLEAN); // Trade disabled
            wrapper.passthrough(Types.INT); // Number of tools uses
            wrapper.passthrough(Types.INT); // Maximum number of trade uses
        }
    }


    // 1.14.4+
    public void registerMerchantOffers(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.VAR_INT);
            int size = wrapper.passthrough(Types.UNSIGNED_BYTE);
            for (int i = 0; i < size; i++) {
                passthroughClientboundItem(wrapper); // Input
                passthroughClientboundItem(wrapper); // Output

                if (wrapper.passthrough(Types.BOOLEAN)) { // Has second item
                    passthroughClientboundItem(wrapper); // Second item
                }

                wrapper.passthrough(Types.BOOLEAN); // Trade disabled
                wrapper.passthrough(Types.INT); // Number of tools uses
                wrapper.passthrough(Types.INT); // Maximum number of trade uses

                wrapper.passthrough(Types.INT); // XP
                wrapper.passthrough(Types.INT); // Special price
                wrapper.passthrough(Types.FLOAT); // Price multiplier
                wrapper.passthrough(Types.INT); // Demand
            }
            //...
        });
    }

    public void registerMerchantOffers1_19(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Container id
            int size = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < size; i++) {
                passthroughClientboundItem(wrapper); // Input
                passthroughClientboundItem(wrapper); // Output
                passthroughClientboundItem(wrapper); // Second item

                wrapper.passthrough(Types.BOOLEAN); // Trade disabled
                wrapper.passthrough(Types.INT); // Number of tools uses
                wrapper.passthrough(Types.INT); // Maximum number of trade uses

                wrapper.passthrough(Types.INT); // XP
                wrapper.passthrough(Types.INT); // Special price
                wrapper.passthrough(Types.FLOAT); // Price multiplier
                wrapper.passthrough(Types.INT); // Demand
            }
        });
    }

    // Hopefully the item cost weirdness is temporary
    public void registerMerchantOffers1_20_5(final C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Container id
            int size = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < size; i++) {
                final Item input = wrapper.read(itemCostType);
                wrapper.write(mappedItemCostType, handleItemToClient(wrapper.user(), input));

                passthroughClientboundItem(wrapper); // Result

                Item secondInput = wrapper.read(optionalItemCostType);
                if (secondInput != null) {
                    handleItemToClient(wrapper.user(), secondInput);
                }
                wrapper.write(mappedOptionalItemCostType, secondInput);

                wrapper.passthrough(Types.BOOLEAN); // Out of stock
                wrapper.passthrough(Types.INT); // Number of trade uses
                wrapper.passthrough(Types.INT); // Maximum number of trade uses

                wrapper.passthrough(Types.INT); // XP
                wrapper.passthrough(Types.INT); // Special price
                wrapper.passthrough(Types.FLOAT); // Price multiplier
                wrapper.passthrough(Types.INT); // Demand
            }
        });
    }

    public void registerAdvancements(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.BOOLEAN); // Reset/clear
            int size = wrapper.passthrough(Types.VAR_INT); // Mapping size
            for (int i = 0; i < size; i++) {
                wrapper.passthrough(Types.STRING); // Identifier
                wrapper.passthrough(Types.OPTIONAL_STRING); // Parent

                // Display data
                if (wrapper.passthrough(Types.BOOLEAN)) {
                    final JsonElement title = wrapper.passthrough(Types.COMPONENT); // Title
                    final JsonElement description = wrapper.passthrough(Types.COMPONENT); // Description
                    final ComponentRewriter componentRewriter = protocol.getComponentRewriter();
                    if (componentRewriter != null) {
                        componentRewriter.processText(wrapper.user(), title);
                        componentRewriter.processText(wrapper.user(), description);
                    }

                    passthroughClientboundItem(wrapper); // Icon
                    wrapper.passthrough(Types.VAR_INT); // Frame type
                    int flags = wrapper.passthrough(Types.INT); // Flags
                    if ((flags & 1) != 0) {
                        wrapper.passthrough(Types.STRING); // Background texture
                    }
                    wrapper.passthrough(Types.FLOAT); // X
                    wrapper.passthrough(Types.FLOAT); // Y
                }

                wrapper.passthrough(Types.STRING_ARRAY); // Criteria

                int arrayLength = wrapper.passthrough(Types.VAR_INT);
                for (int array = 0; array < arrayLength; array++) {
                    wrapper.passthrough(Types.STRING_ARRAY); // String array
                }
            }
        });
    }

    public void registerAdvancements1_20_3(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.BOOLEAN); // Reset/clear
            int size = wrapper.passthrough(Types.VAR_INT); // Mapping size
            for (int i = 0; i < size; i++) {
                wrapper.passthrough(Types.STRING); // Identifier
                wrapper.passthrough(Types.OPTIONAL_STRING); // Parent

                // Display data
                if (wrapper.passthrough(Types.BOOLEAN)) {
                    final Tag title = wrapper.passthrough(Types.TAG);
                    final Tag description = wrapper.passthrough(Types.TAG);
                    final ComponentRewriter componentRewriter = protocol.getComponentRewriter();
                    if (componentRewriter != null) {
                        componentRewriter.processTag(wrapper.user(), title);
                        componentRewriter.processTag(wrapper.user(), description);
                    }

                    passthroughClientboundItem(wrapper); // Icon
                    wrapper.passthrough(Types.VAR_INT); // Frame type
                    int flags = wrapper.passthrough(Types.INT); // Flags
                    if ((flags & 1) != 0) {
                        wrapper.passthrough(Types.STRING); // Background texture
                    }
                    wrapper.passthrough(Types.FLOAT); // X
                    wrapper.passthrough(Types.FLOAT); // Y
                }

                int requirements = wrapper.passthrough(Types.VAR_INT);
                for (int array = 0; array < requirements; array++) {
                    wrapper.passthrough(Types.STRING_ARRAY);
                }

                wrapper.passthrough(Types.BOOLEAN); // Send telemetry
            }
        });
    }

    public void registerSetCursorItem(C packetType) {
        protocol.registerClientbound(packetType, this::passthroughClientboundItemAndTrackHash);
    }

    // Pre 1.21 for enchantments
    public void registerContainerSetData(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.UNSIGNED_BYTE); // Container id

            Mappings mappings = protocol.getMappingData().getEnchantmentMappings();
            if (mappings == null) {
                return;
            }

            short property = wrapper.passthrough(Types.SHORT);
            if (property >= 4 && property <= 6) { // Enchantment id
                short enchantmentId = (short) mappings.getNewId(wrapper.read(Types.SHORT));
                wrapper.write(Types.SHORT, enchantmentId);
            }
        });
    }

    protected @Nullable Item handleItemToClientAndTrackHash(final UserConnection connection, @Nullable Item item) {
        final ItemHasher itemHasher = itemHasher(connection);
        if (itemHasher == null) {
            return handleItemToClient(connection, item);
        }

        itemHasher.setProcessingClientboundInventoryPacket(true);
        try {
            return this.handleItemToClient(connection, item);
        } finally {
            itemHasher.setProcessingClientboundInventoryPacket(false);
        }
    }

    protected void passthroughClientboundItemAndTrackHash(final PacketWrapper wrapper) {
        final ItemHasher itemHasher = itemHasher(wrapper.user());
        if (itemHasher == null) {
            passthroughClientboundItem(wrapper);
            return;
        }

        itemHasher.setProcessingClientboundInventoryPacket(true);
        try {
            this.passthroughClientboundItem(wrapper);
        } finally {
            itemHasher.setProcessingClientboundInventoryPacket(false);
        }
    }

    protected void passthroughClientboundItem(final PacketWrapper wrapper) {
        final Item item = handleItemToClient(wrapper.user(), wrapper.read(itemType));
        wrapper.write(mappedItemType, item);
    }

    protected void passthroughHashedItem(final PacketWrapper wrapper) {
        final HashedItem item = handleHashedItem(wrapper.user(), wrapper.read(Types.HASHED_ITEM));
        wrapper.write(Types.HASHED_ITEM, item);
    }

    protected <T extends ItemHasher> @Nullable T itemHasher(final UserConnection connection) {
        return connection.getItemHasher(protocol.getClass());
    }

    protected void updateHashedItemDataComponentIds(final HashedItem item, final FullMappings mappings) {
        final Int2IntMap addedData = item.dataHashesById();
        if (!addedData.isEmpty()) {
            for (final int id : addedData.keySet().toIntArray()) {
                final int mappedId = mappings.getNewId(id);
                if (mappedId == id) {
                    continue;
                }

                final int hash = addedData.remove(id);
                if (mappedId != -1) {
                    addedData.put(mappedId, hash);
                }
            }
        }

        final IntSet removedData = item.removedDataIds();
        if (!removedData.isEmpty()) {
            for (final int id : removedData.toIntArray()) {
                final int mappedId = mappings.getNewId(id);
                if (mappedId == id) {
                    continue;
                }

                removedData.remove(id);
                if (mappedId != -1) {
                    removedData.add(mappedId);
                }
            }
        }
    }

    @Override
    public Type<Item> itemType() {
        return itemType;
    }

    @Override
    public Type<Item[]> itemArrayType() {
        return itemArrayType;
    }

    @Override
    public Type<Item> mappedItemType() {
        return mappedItemType;
    }

    @Override
    public Type<Item[]> mappedItemArrayType() {
        return mappedItemArrayType;
    }
}
