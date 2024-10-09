/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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

import com.viaversion.viaversion.util.EitherImpl;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;

final class HolderSetImpl extends EitherImpl<String, int[]> implements HolderSet {

    HolderSetImpl(final String tagKey) {
        super(tagKey, null);
    }

    HolderSetImpl(final int[] ids) {
        super(null, ids);
    }

    @Override
    public String tagKey() {
        return left();
    }

    @Override
    public boolean hasTagKey() {
        return isLeft();
    }

    @Override
    public int[] ids() {
        return right();
    }

    @Override
    public boolean hasIds() {
        return isRight();
    }

    @Override
    public HolderSet rewrite(final Int2IntFunction idRewriter) {
        if (hasTagKey()) {
            return this;
        }

        final int[] ids = ids();
        final int[] mappedIds = new int[ids.length];
        for (int i = 0; i < mappedIds.length; i++) {
            mappedIds[i] = idRewriter.applyAsInt(ids[i]);
        }
        return new HolderSetImpl(mappedIds);
    }
}
