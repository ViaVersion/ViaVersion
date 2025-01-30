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
package com.viaversion.viaversion.util;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypeMap;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ProtocolUtil {

    /**
     * Returns a map of packet types by state.
     *
     * @param parent            parent packet type class as given by the Protocol generics
     * @param packetTypeClasses packet type enum classes belonging to the parent type
     * @param <P>               packet type class
     * @return map of packet types by state
     */
    @SafeVarargs
    public static <P extends PacketType> Map<State, PacketTypeMap<P>> packetTypeMap(@Nullable Class<P> parent, Class<? extends P>... packetTypeClasses) {
        if (parent == null) {
            return Collections.emptyMap();
        }

        final Map<State, PacketTypeMap<P>> map = new EnumMap<>(State.class);
        for (final Class<? extends P> packetTypeClass : packetTypeClasses) {
            // Get state from first enum type
            final P[] types = packetTypeClass.getEnumConstants();
            Preconditions.checkArgument(types != null, "%s not an enum", packetTypeClass);
            Preconditions.checkArgument(types.length > 0, "Enum %s has no types", packetTypeClass);
            final State state = types[0].state();
            map.put(state, PacketTypeMap.createArrayMap(packetTypeClass));
        }
        return map;
    }

    /**
     * Returns a hex string of a packet id.
     *
     * @param id packet id
     * @return packet id as a nice hex string
     */
    public static String toNiceHex(int id) {
        final String hex = Integer.toHexString(id).toUpperCase(Locale.ROOT);
        return (hex.length() == 1 ? "0x0" : "0x") + hex;
    }

    /**
     * Returns a readable name of a protocol. For example, "Protocol1_12_2To1_13" becomes "1.12.2->1.13".
     *
     * @param protocol protocol class
     * @return readable name of the protocol
     */
    public static String toNiceName(Class<? extends Protocol> protocol) {
        return protocol.getSimpleName().
            replace("Protocol", "").
            replace("To", "->").
            replace("_", ".");
    }
}
