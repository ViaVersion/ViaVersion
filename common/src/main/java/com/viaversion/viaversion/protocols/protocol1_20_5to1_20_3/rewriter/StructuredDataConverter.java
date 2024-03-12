/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.rewriter;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.util.ComponentUtil;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Map;

final class StructuredDataConverter {

    private static final Map<StructuredDataKey<?>, Rewriter<?>> REWRITERS = new Reference2ObjectOpenHashMap<>();

    static {
        // TODO
        register(StructuredDataKey.CUSTOM_NAME, (data, tag) -> tag.putString("CustomName", ComponentUtil.tagToJsonString(data.value())));
    }

    public static <T> void rewrite(final StructuredData<T> data, final CompoundTag tag) {
        if (data.isEmpty()) {
            return;
        }

        //noinspection unchecked
        final Rewriter<T> rewriter = (Rewriter<T>) REWRITERS.get(data.key());
        if (rewriter != null) {
            rewriter.rewrite(data, tag);
        }
    }

    private static <T> void register(final StructuredDataKey<T> key, final Rewriter<T> rewriter) {
        REWRITERS.put(key, rewriter);
    }

    @FunctionalInterface
    interface Rewriter<T> {

        void rewrite(StructuredData<T> data, CompoundTag tag);
    }
}
