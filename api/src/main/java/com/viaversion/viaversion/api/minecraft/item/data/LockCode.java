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

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.TransformingType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.Rewritable;

public record LockCode(CompoundTag tag) implements Rewritable, Copyable {

    public static final Type<LockCode> TYPE = TransformingType.of(Types.COMPOUND_TAG, LockCode.class, LockCode::new, LockCode::tag);

    @Override
    public LockCode rewrite(final UserConnection connection, final Protocol<?, ?, ?, ?> protocol, final boolean clientbound) {
        CompoundTag updatedTag = tag;
        if (clientbound && protocol.getComponentRewriter() != null) {
            // Just abuse the component rewriter and keep this one-way
            updatedTag = tag.copy();
            updatedTag.putString("id", "air");
            protocol.getComponentRewriter().handleShowItem(connection, updatedTag);
            updatedTag.remove("id");

            final FullMappings mappings = protocol.getMappingData().getFullItemMappings();
            final Tag itemsTag = updatedTag.get("items");
            if (itemsTag != null) {
                final HolderSet items = HolderSet.fromTag(itemsTag, key -> mappings.id(key));
                items.rewrite(mappings::getNewId);
                updatedTag.put("items", toTag(items, mappings));
            }
        }
        return new LockCode(updatedTag);
    }

    private Tag toTag(final HolderSet holderSet, final FullMappings mappings) {
        if (holderSet.hasTagKey()) {
            return new StringTag("#" + holderSet.tagKey());
        }

        final int[] ids = holderSet.ids();
        if (ids.length == 1) {
            return new StringTag(mappings.mappedIdentifier(ids[0]));
        }

        final ListTag<StringTag> listTag = new ListTag<>(StringTag.class);
        for (final int id : ids) {
            listTag.add(new StringTag(mappings.mappedIdentifier(id)));
        }
        return listTag;
    }

    @Override
    public LockCode copy() {
        return new LockCode(tag.copy());
    }
}
