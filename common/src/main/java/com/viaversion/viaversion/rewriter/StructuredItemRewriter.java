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
package com.viaversion.viaversion.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.type.Type;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public class StructuredItemRewriter<C extends ClientboundPacketType, S extends ServerboundPacketType,
    T extends Protocol<C, ?, ?, S>> extends ItemRewriter<C, S, T> {

    public StructuredItemRewriter(
        T protocol,
        Type<Item> itemType, Type<Item[]> itemArrayType, Type<Item> mappedItemType, Type<Item[]> mappedItemArrayType,
        Type<Item> itemCostType, Type<Item> optionalItemCostType, Type<Item> mappedItemCostType, Type<Item> mappedOptionalItemCostType,
        Type<Particle> particleType, Type<Particle> mappedParticleType
    ) {
        super(protocol, itemType, itemArrayType, mappedItemType, mappedItemArrayType, itemCostType, optionalItemCostType, mappedItemCostType, mappedOptionalItemCostType, particleType, mappedParticleType);
    }

    public StructuredItemRewriter(T protocol, Type<Item> itemType, Type<Item[]> itemArrayType, Type<Item> mappedItemType, Type<Item[]> mappedItemArrayType) {
        super(protocol, itemType, itemArrayType, mappedItemType, mappedItemArrayType);
    }

    public StructuredItemRewriter(T protocol, Type<Item> itemType, Type<Item[]> itemArrayType) {
        super(protocol, itemType, itemArrayType, itemType, itemArrayType);
    }

    @Override
    public Item handleItemToClient(UserConnection connection, Item item) {
        if (item.isEmpty()) {
            return item;
        }

        final MappingData mappingData = protocol.getMappingData();
        if (mappingData != null && mappingData.getItemMappings() != null) {
            item.setIdentifier(mappingData.getNewItemId(item.identifier()));
        }

        updateItemDataComponentTypeIds(item.dataContainer(), true);
        updateItemDataComponents(connection, item, true);
        return item;
    }

    @Override
    public Item handleItemToServer(UserConnection connection, Item item) {
        if (item.isEmpty()) {
            return item;
        }

        final MappingData mappingData = protocol.getMappingData();
        if (mappingData != null && mappingData.getItemMappings() != null) {
            item.setIdentifier(mappingData.getOldItemId(item.identifier()));
        }

        updateItemDataComponentTypeIds(item.dataContainer(), false);
        updateItemDataComponents(connection, item, false);
        restoreTextComponents(item);
        return item;
    }

    protected void updateItemDataComponentTypeIds(final StructuredDataContainer container, final boolean mappedNames) {
        final MappingData mappingData = protocol.getMappingData();
        if (mappingData == null) {
            return;
        }

        FullMappings dataComponentMappings = mappingData.getDataComponentSerializerMappings();
        if (dataComponentMappings == null) {
            return;
        }

        if (!mappedNames) {
            dataComponentMappings = dataComponentMappings.inverse();
        }

        container.setIdLookup(protocol, mappedNames); // Necessary to be set before trying to add values to the container
        container.updateIds(protocol, dataComponentMappings::getNewId);
    }

    protected void updateItemDataComponents(final UserConnection connection, final Item item, final boolean clientbound) {
        // Specific types that need deep handling
        final StructuredDataContainer container = item.dataContainer();
        final MappingData mappingData = protocol.getMappingData();
        if (mappingData.getItemMappings() != null) {
            final Int2IntFunction itemIdRewriter = clientbound ? mappingData::getNewItemId : mappingData::getOldItemId;
            container.replace(StructuredDataKey.TRIM, value -> value.rewrite(itemIdRewriter));
            container.replace(StructuredDataKey.POT_DECORATIONS, value -> value.rewrite(itemIdRewriter));
        }
        if (mappingData.getBlockMappings() != null) {
            final Int2IntFunction blockIdRewriter = clientbound ? mappingData::getNewBlockId : mappingData::getOldBlockId;
            container.replace(StructuredDataKey.TOOL, value -> value.rewrite(blockIdRewriter));
            container.replace(StructuredDataKey.CAN_PLACE_ON, value -> value.rewrite(blockIdRewriter));
            container.replace(StructuredDataKey.CAN_BREAK, value -> value.rewrite(blockIdRewriter));
        }
        if (mappingData.getSoundMappings() != null) {
            final Int2IntFunction soundIdRewriter = clientbound ? mappingData::getNewSoundId : mappingData::getOldSoundId;
            container.replace(StructuredDataKey.INSTRUMENT, value -> value.isDirect() ? Holder.of(value.value().rewrite(soundIdRewriter)) : value);
            container.replace(StructuredDataKey.JUKEBOX_PLAYABLE, value -> value.rewrite(soundIdRewriter));
        }
        if (clientbound && protocol.getComponentRewriter() != null) {
            updateComponent(connection, item, StructuredDataKey.ITEM_NAME, "item_name");
            updateComponent(connection, item, StructuredDataKey.CUSTOM_NAME, "custom_name");

            final Tag[] lore = container.get(StructuredDataKey.LORE);
            if (lore != null) {
                for (final Tag tag : lore) {
                    protocol.getComponentRewriter().processTag(connection, tag);
                }
            }
        }

        // Look for item types
        final ItemHandler itemHandler = clientbound ? this::handleItemToClient : this::handleItemToServer;
        for (final Map.Entry<StructuredDataKey<?>, StructuredData<?>> entry : container.data().entrySet()) {
            final StructuredData<?> data = entry.getValue();
            if (data.isEmpty()) {
                continue;
            }

            final StructuredDataKey<?> key = entry.getKey();
            final Class<?> outputClass = key.type().getOutputClass();
            if (outputClass == Item.class) {
                //noinspection unchecked
                final StructuredData<Item> itemData = (StructuredData<Item>) data;
                itemData.setValue(itemHandler.rewrite(connection, itemData.value()));
            } else if (outputClass == Item[].class) {
                //noinspection unchecked
                final StructuredData<Item[]> itemArrayData = (StructuredData<Item[]>) data;
                final Item[] items = itemArrayData.value();
                for (int i = 0; i < items.length; i++) {
                    items[i] = itemHandler.rewrite(connection, items[i]);
                }
            }
        }
    }

    protected void updateComponent(final UserConnection connection, final Item item, final StructuredDataKey<Tag> key, final String backupKey) {
        final Tag name = item.dataContainer().get(key);
        if (name == null) {
            return;
        }

        final Tag originalName = name.copy();
        protocol.getComponentRewriter().processTag(connection, name);
        if (!name.equals(originalName)) {
            saveTag(createCustomTag(item), originalName, backupKey);
        }
    }

    protected void restoreTextComponents(final Item item) {
        final StructuredDataContainer data = item.dataContainer();
        final CompoundTag customData = data.get(StructuredDataKey.CUSTOM_DATA);
        if (customData == null) {
            return;
        }

        // Remove custom name
        if (customData.remove(nbtTagName("added_custom_name")) != null) {
            data.remove(StructuredDataKey.CUSTOM_NAME);
        } else {
            final Tag customName = removeBackupTag(customData, "custom_name");
            if (customName != null) {
                data.set(StructuredDataKey.CUSTOM_NAME, customName);
            }

            final Tag itemName = removeBackupTag(customData, "item_name");
            if (itemName != null) {
                data.set(StructuredDataKey.ITEM_NAME, itemName);
            }
        }
    }

    protected CompoundTag createCustomTag(final Item item) {
        final StructuredDataContainer data = item.dataContainer();
        CompoundTag customData = data.get(StructuredDataKey.CUSTOM_DATA);
        if (customData == null) {
            customData = new CompoundTag();
            data.set(StructuredDataKey.CUSTOM_DATA, customData);
        }
        return customData;
    }

    protected void saveTag(final CompoundTag customData, final Tag tag, final String name) {
        final String backupName = nbtTagName(name);
        if (!customData.contains(backupName)) {
            customData.put(backupName, tag);
        }
    }

    protected @Nullable Tag removeBackupTag(final CompoundTag customData, final String tagName) {
        return customData.remove(nbtTagName(tagName));
    }

    @FunctionalInterface
    private interface ItemHandler {

        Item rewrite(UserConnection connection, Item item);
    }
}
