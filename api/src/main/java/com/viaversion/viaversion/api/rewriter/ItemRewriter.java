/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.viaversion.viaversion.api.rewriter;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.HashedItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface ItemRewriter<T extends Protocol<?, ?, ?, ?>> extends Rewriter<T> {

    /**
     * Returns the rewritten item, which may or may not be the same given Item instance.
     *
     * @param connection user connection
     * @param item       item
     * @return rewritten item
     */
    @Nullable
    Item handleItemToClient(UserConnection connection, @Nullable Item item);

    /**
     * Returns the rewritten item, which may or may not be the same given Item instance.
     *
     * @param connection user connection
     * @param item       item
     * @return rewritten item
     */
    @Nullable
    Item handleItemToServer(UserConnection connection, @Nullable Item item);

    /**
     * Returns the rewritten item, which may or may not be the same given HashedItem instance.
     * <p>
     * Used starting with 1.21.6.
     *
     * @param connection user connection
     * @param item       hashed item
     * @return rewritten hashed item
     */
    HashedItem handleHashedItem(UserConnection connection, HashedItem item);

    /**
     * Returns the item type of the current protocol.
     *
     * @return item type
     */
    @Nullable
    default Type<Item> itemType() {
        return null;
    }

    /**
     * Returns the item array type of the current protocol.
     *
     * @return item array type
     */
    @Nullable
    default Type<Item[]> itemArrayType() {
        return null;
    }

    /**
     * Returns the mapped item type of the target protocol.
     *
     * @return mapped item type
     */
    @Nullable
    default Type<Item> mappedItemType() {
        return itemType();
    }

    /**
     * Returns the mapped item array type of the target protocol.
     *
     * @return mapped item array type
     */
    @Nullable
    default Type<Item[]> mappedItemArrayType() {
        return itemArrayType();
    }

    /**
     * Returns the NBT tag name used for storing original item data.
     *
     * @return NBT tag name
     */
    default String nbtTagName() {
        return "VV|" + protocol().getClass().getSimpleName();
    }

    /**
     * Prefixes the NBT tag name with the current protocol's {@link #nbtTagName()}.
     *
     * @param nbt NBT tag name
     * @return prefixed NBT tag name
     */
    default String nbtTagName(final String nbt) {
        return nbtTagName() + "|" + nbt;
    }
}
