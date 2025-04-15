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

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.Rewritable;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class FilterableComponent extends Filterable<Tag> implements Copyable, Rewritable {

    public static final Type<FilterableComponent> TYPE = new FilterableType<>(Types.TAG, Types.OPTIONAL_TAG, FilterableComponent.class) {
        @Override
        protected FilterableComponent create(final Tag raw, final Tag filtered) {
            return new FilterableComponent(raw, filtered);
        }
    };
    public static final Type<FilterableComponent[]> ARRAY_TYPE = new ArrayType<>(FilterableComponent.TYPE);

    public FilterableComponent(final Tag raw, @Nullable final Tag filtered) {
        super(raw, filtered);
    }

    @Override
    public FilterableComponent rewrite(final UserConnection connection, final Protocol<?, ?, ?, ?> protocol, final boolean clientbound) {
        if (protocol.getComponentRewriter() == null) {
            return this;
        }

        final FilterableComponent copy = copy();
        protocol.getComponentRewriter().processTag(connection, copy.raw());
        protocol.getComponentRewriter().processTag(connection, copy.filtered());
        return copy;
    }

    @Override
    public FilterableComponent copy() {
        return new FilterableComponent(raw().copy(), filtered() != null ? filtered().copy() : null);
    }
}
