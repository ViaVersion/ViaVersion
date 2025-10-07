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
package com.viaversion.viaversion.api.minecraft;

import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.function.Function;

/**
 * Set of ids that either holds a string tag key or an array of ids.
 */
public interface HolderSet {

    /**
     * Creates a new holder set for the given tag.
     *
     * @param tagKey the tag key, not including '#'
     * @return a new holder set
     */
    static HolderSet of(final String tagKey) {
        return new HolderSetImpl.Tag(tagKey);
    }

    /**
     * Creates a new holder set for the given ids.
     *
     * @param ids the direct ids
     * @return a new holder set
     */
    static HolderSet of(final int[] ids) {
        return new HolderSetImpl.Ids(ids);
    }

    static HolderSet fromTag(final Tag tag, final Function<String, Integer> mappingFunction) {
        if (tag instanceof StringTag stringTag) {
            if (stringTag.getValue().startsWith("#")) {
                return HolderSet.of(stringTag.getValue().substring(1));
            }

            final int id = mappingFunction.apply(stringTag.getValue());
            return HolderSet.of(new int[]{id});
        } else if (tag instanceof ListTag<?> listTag) {
            final int[] ids = new int[listTag.size()];
            for (int i = 0; i < listTag.size(); i++) {
                final String value = ((StringTag) listTag.get(i)).getValue();
                ids[i] = mappingFunction.apply(value);
            }
            return HolderSet.of(ids);
        }
        throw new IllegalArgumentException();
    }

    /**
     * Gets the tag key, not including '#'.
     *
     * @return the tag key without a '#'
     * @see #hasTagKey()
     */
    String tagKey();

    /**
     * Returns whether this holder set has a tag key.
     *
     * @return true if this holder set has a tag key, false if it has direct ids
     */
    boolean hasTagKey();

    /**
     * Gets the direct ids.
     *
     * @return direct ids
     * @see #hasIds()
     */
    int[] ids();

    /**
     * Returns whether this holder set has direct ids.
     *
     * @return true if this holder set has direct ids, false if it has a tag key
     */
    boolean hasIds();

    /**
     * Returns a new holder set with the ids rewritten.
     *
     * @param idRewriter the id rewriter
     * @return a new holder set with the ids rewritten, or self if it has a tag key
     */
    HolderSet rewrite(Int2IntFunction idRewriter);
}
