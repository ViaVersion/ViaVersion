/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2023 ViaVersion and contributors
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
package com.viaversion.viaversion.api.protocol.packet.provider;

import com.viaversion.viaversion.api.protocol.packet.PacketType;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface PacketTypeMap<P> {

    /**
     * Returns the packet type by the given name.
     *
     * @param packetTypeName packet type name
     * @return packet type if present
     */
    @Nullable P typeByName(String packetTypeName);

    /**
     * Returns the packet type by the given id.
     *
     * @param packetTypeId packet type id
     * @return packet type if present
     */
    @Nullable P typeById(int packetTypeId);

    /**
     * Returns a collection of all contained packet types.
     *
     * @return collection of all packet types
     */
    Collection<P> types();

    static <S extends PacketType, T extends S> PacketTypeMap<S> of(final Class<T> enumClass) {
        if (!enumClass.isEnum()) {
            throw new IllegalArgumentException("Given class is not an enum");
        }

        final S[] types = enumClass.getEnumConstants();
        final Map<String, S> byName = new HashMap<>(types.length);
        for (final S type : types) {
            byName.put(type.getName(), type);
        }
        return of(byName, types);
    }

    static <T> PacketTypeMap<T> of(final Map<String, T> packetsByName, final Int2ObjectMap<T> packetsById) {
        return new PacketTypeMapMap<>(packetsByName, packetsById);
    }

    static <T> PacketTypeMap<T> of(final Map<String, T> packetsByName, final T[] packets) {
        return new PacketTypeArrayMap<>(packetsByName, packets);
    }
}