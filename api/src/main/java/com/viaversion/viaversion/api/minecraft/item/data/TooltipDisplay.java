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
package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;

public record TooltipDisplay(boolean hideTooltip,
                             IntSortedSet hiddenComponents) { // Keeping the ids raw makes this less annoying to deal with...

    public static final Type<TooltipDisplay> TYPE = new Type<>(TooltipDisplay.class) {

        @Override
        public TooltipDisplay read(final ByteBuf buffer) {
            final boolean hideTooltip = buffer.readBoolean();
            final IntSortedSet hiddenComponents = new IntLinkedOpenHashSet();
            final int size = Types.VAR_INT.readPrimitive(buffer);
            for (int i = 0; i < size; i++) {
                hiddenComponents.add(Types.VAR_INT.readPrimitive(buffer));
            }
            return new TooltipDisplay(hideTooltip, hiddenComponents);
        }

        @Override
        public void write(final ByteBuf buffer, final TooltipDisplay value) {
            buffer.writeBoolean(value.hideTooltip());
            Types.VAR_INT.writePrimitive(buffer, value.hiddenComponents().size());
            for (int hiddenComponent : value.hiddenComponents()) {
                Types.VAR_INT.writePrimitive(buffer, hiddenComponent);
            }
        }
    };

    public TooltipDisplay rewrite(final Int2IntFunction idRewriter) {
        if (hiddenComponents.isEmpty()) {
            return this;
        }

        final IntSortedSet newHiddenComponents = new IntLinkedOpenHashSet();
        for (final int hiddenComponent : hiddenComponents) {
            newHiddenComponents.add(idRewriter.applyAsInt(hiddenComponent));
        }
        return new TooltipDisplay(hideTooltip, newHiddenComponents);
    }
}
