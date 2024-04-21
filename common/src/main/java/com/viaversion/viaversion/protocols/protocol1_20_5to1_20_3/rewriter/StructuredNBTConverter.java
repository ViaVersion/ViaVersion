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
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 1.20.5 nbt -> 1.20.5 data component conversion, technically only needed in VB but kept here to make maintenance easier
public final class StructuredNBTConverter {

    private static final Map<String, DataConverter<?>> rewriters = new HashMap<>();

    static {
        register(StructuredDataKey.CUSTOM_DATA, tag -> (CompoundTag) tag);
        // TODO Add missing handlers for other data types, this will probably be done after the component rework
    }

    public static List<StructuredData<?>> toData(final CompoundTag tag) {
        final List<StructuredData<?>> data = new ArrayList<>();
        for (final Map.Entry<String, Tag> entry : tag.entrySet()) {
            final StructuredData<?> structuredData = readFromTag(entry.getKey(), entry.getValue());
            data.add(structuredData);
        }
        return data;
    }

    @SuppressWarnings("unchecked")
    public static <T> StructuredData<T> readFromTag(final String identifier, final Tag tag) {
        final DataConverter<T> converter = (DataConverter<T>) rewriters.get(identifier);
        Preconditions.checkNotNull(converter, "No converter for %s found", identifier);
        return (StructuredData<T>) converter.convert(tag);
    }

    private static <T> void register(final StructuredDataKey<T> key, final DataConverter<T> converter) {
        rewriters.put(key.identifier(), converter);
    }

    @FunctionalInterface
    interface DataConverter<T> {

        T convert(final Tag tag);
    }
}