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
package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

public record WrittenBook(FilterableString title, String author, int generation, FilterableComponent[] pages,
                          boolean resolved) {

    public static final Type<WrittenBook> TYPE = new Type<>(WrittenBook.class) {
        @Override
        public WrittenBook read(final ByteBuf buffer) {
            final FilterableString title = FilterableString.TYPE.read(buffer);
            final String author = Type.STRING.read(buffer);
            final int generation = Type.VAR_INT.readPrimitive(buffer);
            final FilterableComponent[] pages = FilterableComponent.ARRAY_TYPE.read(buffer);
            final boolean resolved = buffer.readBoolean();
            return new WrittenBook(title, author, generation, pages, resolved);
        }

        @Override
        public void write(final ByteBuf buffer, final WrittenBook value) {
            FilterableString.TYPE.write(buffer, value.title);
            Type.STRING.write(buffer, value.author);
            Type.VAR_INT.writePrimitive(buffer, value.generation);
            FilterableComponent.ARRAY_TYPE.write(buffer, value.pages);
            buffer.writeBoolean(value.resolved);
        }
    };

}
