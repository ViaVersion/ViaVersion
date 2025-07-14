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
package com.viaversion.viaversion.api.minecraft.item;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface Item extends ItemBase {

    /**
     * Returns the item data. Always 0 for 1.13+ items.
     *
     * @return item data
     */
    default short data() {
        return 0;
    }

    /**
     * Sets the item data used in versions before 1.13.
     *
     * @param data item data
     * @throws UnsupportedOperationException if the item implementation does not store data
     */
    default void setData(short data) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the item compound tag if present.
     *
     * @return item tag
     */
    @Nullable
    CompoundTag tag();

    /**
     * Sets the item compound tag.
     *
     * @param tag item tag
     */
    void setTag(@Nullable CompoundTag tag);

    /**
     * Returns the data container for item data components.
     *
     * @return the data container
     */
    StructuredDataContainer dataContainer();

    /**
     * Returns a copy of the item.
     *
     * @return copy of the item
     */
    @Override
    Item copy();
}
