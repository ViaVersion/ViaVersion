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

import com.google.common.base.Preconditions;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.item.ItemHasher;
import com.viaversion.viaversion.api.minecraft.EitherHolder;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.HashedItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.FilterableComponent;
import com.viaversion.viaversion.api.minecraft.item.data.WrittenBook;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypesHolder;
import com.viaversion.viaversion.data.item.ItemHasherBase;
import com.viaversion.viaversion.util.Rewritable;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public class StructuredItemRewriter<C extends ClientboundPacketType, S extends ServerboundPacketType,
    T extends Protocol<C, ?, ?, S>> extends ItemRewriter<C, S, T> {

    public static final String MARKER_KEY = "VV|custom_data";

    public StructuredItemRewriter(T protocol) {
        super(protocol);
    }

    /**
     * Rewrites an item to the client, including item hash tracking if necessary.
     *
     * @param connection user connection
     * @param item item
     * @return the rewritten item, can be the same or a new object
     * @see #handleItemDataComponentsToClient(UserConnection, Item, StructuredDataContainer)
     * @see #handleItemToServer(UserConnection, Item)
     */
    @Override
    public Item handleItemToClient(UserConnection connection, Item item) {
        if (item.isEmpty()) {
            return item;
        }

        final ItemHasherBase itemHasher = itemHasher(connection); // get the original hashed item and store it later if there are any changes that could affect the data hashes
        final HashedItem originalHashedItem = hashItem(item, itemHasher);

        final StructuredDataContainer dataContainer = item.dataContainer();
        updateItemDataComponentTypeIds(dataContainer, true);

        // Save the inconvertible data before we start modifying the item
        final CompoundTag backupTag = new CompoundTag();
        backupInconvertibleData(connection, item, dataContainer, backupTag);
        if (!backupTag.isEmpty()) {
            saveTag(createCustomTag(item), backupTag, "backup");
        }

        final MappingData mappingData = protocol.getMappingData();
        if (mappingData != null && mappingData.getItemMappings() != null) {
            item.setIdentifier(mappingData.getNewItemId(item.identifier()));
        }

        handleRewritablesToClient(connection, dataContainer, originalHashedItem != null ? itemHasher : null);
        handleItemDataComponentsToClient(connection, item, dataContainer);

        storeOriginalHashedItem(item, itemHasher, originalHashedItem); // has to be called AFTER all modifications - override handleItemDataComponentsToClient instead of this method if needed
        return item;
    }

    protected @Nullable HashedItem hashItem(final Item item, @Nullable final ItemHasherBase hasher) {
        // Hash the original item from open inventory data to be able to get it back out of serverbound hashed items
        return hasher == null || !hasher.isProcessingClientboundInventoryPacket() ? null : hasher.toHashedItem(item, false);
    }

    protected void storeOriginalHashedItem(final Item item, final ItemHasherBase hasher, @Nullable final HashedItem originalHashedItem) {
        if (originalHashedItem == null || item.dataContainer().isEmpty()) {
            return;
        }

        // Check if the hashed data is the same, this will also prevent unnecessary backups due to missing converters
        final HashedItem hashedItem = hasher.toHashedItem(item, true);
        if (hashedItem.dataHashesById().equals(originalHashedItem.dataHashesById()) && hashedItem.removedDataIds().equals(originalHashedItem.removedDataIds())) {
            return;
        }

        // Always has to be AFTER any modification - Use the custom_data hash as a key to the original hashes.
        // This is much easier/cheaper than tracking via the full hashed item, as collisions are both acceptable and still unlikely.
        final CompoundTag originalHashes = new CompoundTag();
        for (final Int2IntMap.Entry entry : originalHashedItem.dataHashesById().int2IntEntrySet()) {
            originalHashes.putInt(Integer.toString(entry.getIntKey()), entry.getIntValue());
        }
        originalHashes.put("removed", new IntArrayTag(originalHashedItem.removedDataIds().toIntArray()));

        final CompoundTag customTag = createCustomTag(item);
        saveTag(customTag, originalHashes, "original_hashes");

        hasher.trackOriginalHashedItem(customTag, originalHashedItem);
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

        // Handle rewritables first, then restore backup data, then the rest
        updateItemDataComponentTypeIds(item.dataContainer(), false);
        handleRewritablesToServer(connection, item.dataContainer());
        restoreBackupData(item);
        handleItemDataComponentsToServer(connection, item, item.dataContainer());
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

    /**
     * This method is called last after changing the item identifier and data component type ids in clientbound item packets.
     * Always remember to call the super method.
     *
     * @param connection user connection
     * @param item item to update
     * @param container item data container
     */
    protected void handleItemDataComponentsToClient(final UserConnection connection, final Item item, final StructuredDataContainer container) {
        if (protocol.getComponentRewriter() != null) {
            updateTextComponent(connection, item, StructuredDataKey.ITEM_NAME, "item_name");
            updateTextComponent(connection, item, StructuredDataKey.CUSTOM_NAME, "custom_name");

            final Tag[] lore = container.get(StructuredDataKey.LORE);
            if (lore != null) {
                for (final Tag tag : lore) {
                    protocol.getComponentRewriter().processTag(connection, tag);
                }
            }

            final WrittenBook book = container.get(StructuredDataKey.WRITTEN_BOOK_CONTENT);
            if (book != null) {
                for (final FilterableComponent page : book.pages()) {
                    protocol.getComponentRewriter().processTag(connection, page.raw());
                    if (page.isFiltered()) {
                        protocol.getComponentRewriter().processTag(connection, page.filtered());
                    }
                }
            }
        }
        replaceAnnoyingKeys(container, protocol.types(), protocol.mappedTypes());
    }

    protected void handleItemDataComponentsToServer(final UserConnection connection, final Item item, final StructuredDataContainer container) {
        replaceAnnoyingKeys(container, protocol.mappedTypes(), protocol.types());
    }

    protected void handleRewritablesToClient(final UserConnection connection, final StructuredDataContainer container, @Nullable final ItemHasher itemHasher) {
        if (itemHasher == null) {
            handleRewritables(connection, true, container, this::handleItemToClient);
            return;
        }

        // Don't track items inside items
        itemHasher.setProcessingClientboundInventoryPacket(false);
        try {
            handleRewritables(connection, true, container, this::handleItemToClient);
        } finally {
            itemHasher.setProcessingClientboundInventoryPacket(true);
        }
    }

    protected void handleRewritablesToServer(final UserConnection connection, final StructuredDataContainer container) {
        handleRewritables(connection, false, container, this::handleItemToServer);
    }

    // Casting around Rewritable and especially Holder gets ugly, but the only good alternative is to do everything manually
    @SuppressWarnings("unchecked")
    private void handleRewritables(UserConnection connection, boolean clientbound, StructuredDataContainer container, ItemHandler itemHandler) {
        for (final Map.Entry<StructuredDataKey<?>, StructuredData<?>> entry : container.data().entrySet()) {
            final StructuredData<?> data = entry.getValue();
            if (data.isEmpty()) {
                continue;
            }

            final Object value = data.value();
            if (value instanceof Item itemValue) {
                final StructuredData<Item> itemData = (StructuredData<Item>) data;
                itemData.setValue(itemHandler.rewrite(connection, itemValue));
            } else if (value instanceof Item[] items) {
                for (int i = 0; i < items.length; i++) {
                    items[i] = itemHandler.rewrite(connection, items[i]);
                }
            } else if (value instanceof Rewritable rewritable) {
                setDataUnchecked(data, rewritable.rewrite(connection, protocol, clientbound));
            } else if (value instanceof Holder<?> holder) {
                final StructuredData<Holder<?>> holderData = (StructuredData<Holder<?>>) data;
                if (holder.isDirect() && holder.value() instanceof Rewritable) {
                    holderData.setValue(updateHolderUnchecked(holder, connection, clientbound));
                }
            } else if (value instanceof EitherHolder<?> eitherHolder) {
                final StructuredData<EitherHolder<?>> holderData = (StructuredData<EitherHolder<?>>) data;
                if (eitherHolder.hasHolder() && eitherHolder.holder().isDirect() && eitherHolder.holder().value() instanceof Rewritable) {
                    holderData.setValue(EitherHolder.of(updateHolderUnchecked(eitherHolder.holder(), connection, clientbound)));
                }
            }
        }
    }

    private <V> void setDataUnchecked(final StructuredData<V> data, final Object value) {
        //noinspection unchecked
        data.setValue((V) value);
    }

    private <V> Holder<V> updateHolderUnchecked(final Holder<V> holder, final UserConnection connection, final boolean clientbound) {
        //noinspection unchecked
        return holder.updateValue(val -> val instanceof Rewritable rewritable ? (V) rewritable.rewrite(connection, protocol, clientbound) : val);
    }

    protected void updateTextComponent(final UserConnection connection, final Item item, final StructuredDataKey<Tag> key, final String backupKey) {
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

    protected void restoreBackupData(final Item item) {
        final StructuredDataContainer container = item.dataContainer();
        final CompoundTag customData = container.get(StructuredDataKey.CUSTOM_DATA);
        if (customData != null) {
            restoreBackupData(item, container, customData);
            removeCustomTag(container, customData);
        }
    }

    /**
     * Stores inconvertible data in a backup tag. Called before data component modification to the item.
     *
     * @param connection user connection
     * @param item item to save data for
     * @param dataContainer item data container
     */
    protected void backupInconvertibleData(final UserConnection connection, final Item item, final StructuredDataContainer dataContainer, final CompoundTag backupTag) {
    }

    /**
     * Restored inconvertible backup data from the item. Called after rewritables and before the remaining data component modification.
     *
     * @param item item
     * @param container item data container
     * @param customData custom data tag
     */
    protected void restoreBackupData(final Item item, final StructuredDataContainer container, final CompoundTag customData) {
        customData.remove(nbtTagName("original_hashes"));

        // Remove custom name
        if (removeBackupTag(customData, "added_custom_name") != null) {
            container.remove(StructuredDataKey.CUSTOM_NAME);
            // Remove the others as well
        } else {
            final Tag customName = removeBackupTag(customData, "custom_name");
            if (customName != null) {
                container.set(StructuredDataKey.CUSTOM_NAME, customName);
            }

            final Tag itemName = removeBackupTag(customData, "item_name");
            if (itemName != null) {
                container.set(StructuredDataKey.ITEM_NAME, itemName);
            }
        }
    }

    protected CompoundTag createCustomTag(final Item item) {
        final StructuredDataContainer data = item.dataContainer();
        CompoundTag customData = data.get(StructuredDataKey.CUSTOM_DATA);
        if (customData == null) {
            customData = new CompoundTag();
            customData.putBoolean(MARKER_KEY, true);
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

    protected void removeCustomTag(final StructuredDataContainer data, final CompoundTag customData) {
        // Only remove if we initially added it and only the marker is left
        if (customData.size() == 1 && customData.contains(MARKER_KEY)) {
            data.remove(StructuredDataKey.CUSTOM_DATA);
        }
    }

    protected void passthroughLengthPrefixedItem(final PacketWrapper wrapper) {
        final Item item = handleItemToServer(wrapper.user(), wrapper.read(protocol.mappedTypes().lengthPrefixedItem()));
        wrapper.write(protocol.types().lengthPrefixedItem(), item);
    }

    private void replaceAnnoyingKeys(final StructuredDataContainer container, final VersionedTypesHolder types, final VersionedTypesHolder mappedTypes) {
        final List<StructuredDataKey<?>> keys = types.structuredDataKeys().keys();
        final List<StructuredDataKey<?>> mappedKeys = mappedTypes.structuredDataKeys().keys();
        final int minSize = Math.min(keys.size(), mappedKeys.size());
        for (int i = 0; i < minSize; i++) {
            // Just assume they are index matched
            final StructuredDataKey<?> key = keys.get(i);
            final StructuredDataKey<?> mappedKey = mappedKeys.get(i);
            replaceKeyUnchecked(container, key, mappedKey);
        }
    }

    private static <T> void replaceKeyUnchecked(final StructuredDataContainer container, final StructuredDataKey<T> key, final StructuredDataKey<?> mappedKey) {
        Preconditions.checkArgument(key.type().getOutputClass() == mappedKey.type().getOutputClass(), "Type mismatch: %s vs %s", key, mappedKey);
        //noinspection unchecked
        container.replaceKey(key, (StructuredDataKey<T>) mappedKey);
    }

    @FunctionalInterface
    private interface ItemHandler {

        Item rewrite(UserConnection connection, Item item);
    }

    // -----------------------

    public void registerSetCreativeModeSlot1_21_5(S packetType) {
        protocol.registerServerbound(packetType, wrapper -> {
            if (!protocol.getEntityRewriter().tracker(wrapper.user()).canInstaBuild()) {
                // Mimic server/client behavior
                wrapper.cancel();
                return;
            }

            wrapper.passthrough(Types.SHORT); // Slot
            passthroughLengthPrefixedItem(wrapper);
        });
    }
}
