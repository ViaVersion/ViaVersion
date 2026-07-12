/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.api.data;

import com.viaversion.nbt.tag.ByteArrayTag;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.function.IntConsumer;

/**
 * Reads sets of non-negative ints stored as ranges, packed into a byte array of varints.
 * Each range is stored as an offset and length pair. The offset is the start of the first range and the
 * difference to the previous range's exclusive end; the length is the inclusive end minus the start.
 */
public final class IdRanges {

    /**
     * Returns the encoded id set as an int set.
     *
     * @param rangesTag tag with the encoded ranges
     * @return decoded ids
     */
    public static IntSet decode(final ByteArrayTag rangesTag) {
        final IntSet ids = new IntOpenHashSet();
        forEachId(rangesTag, ids::add);
        return ids;
    }

    public static void forEachId(final ByteArrayTag rangesTag, final IntConsumer consumer) {
        final ByteBuf buf = Unpooled.wrappedBuffer(rangesTag.getValue());
        int prevEnd = 0;
        while (buf.isReadable()) {
            final int start = prevEnd + Types.VAR_INT.readPrimitive(buf);
            final int end = start + Types.VAR_INT.readPrimitive(buf);
            for (int id = start; id <= end; id++) {
                consumer.accept(id);
            }
            prevEnd = end + 1;
        }
    }
}
