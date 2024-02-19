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
package com.viaversion.viaversion.api.protocol.packet.provider;

import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.State;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Provides unmapped and mapped packet types for a Protocol.
 *
 * @param <CU> unmapped clientbound packet type
 * @param <CM> mapped clientbound packet type
 * @param <SM> mapped serverbound packet type
 * @param <SU> unmapped serverbound packet type
 * @see SimplePacketTypesProvider
 */
public interface PacketTypesProvider<CU extends ClientboundPacketType, CM extends ClientboundPacketType, SM extends ServerboundPacketType, SU extends ServerboundPacketType> {

    /**
     * Returns a map of all unmapped clientbound packet types that are expected to be used within a protocol.
     * This means that if {@code C1} encompasses more than just {@link State#PLAY} packets, the other types are included as well.
     *
     * @return map of unmapped clientbound packet types
     */
    Map<State, PacketTypeMap<CU>> unmappedClientboundPacketTypes();

    /**
     * Return a map of all unmapped serverbound packet types that are expected to be used within the protocol.
     * This means that if {@code S2} encompasses more than just {@link State#PLAY} packets, the other types have to be included as well.
     *
     * @return map of unmapped serverbound packet types
     */
    Map<State, PacketTypeMap<SU>> unmappedServerboundPacketTypes();

    /**
     * Returns a map of all mapped clientbound packet types that are expected to be used within the protocol.
     *
     * @return map of mapped clientbound packet types
     */
    Map<State, PacketTypeMap<CM>> mappedClientboundPacketTypes();

    /**
     * Returns a map of all mapped serverbound packet types that are expected to be used within the protocol.
     *
     * @return map of mapped serverbound packet types
     */
    Map<State, PacketTypeMap<SM>> mappedServerboundPacketTypes();

    default @Nullable CU unmappedClientboundType(final State state, final String typeName) {
        PacketTypeMap<CU> map = unmappedClientboundPacketTypes().get(state);
        return map != null ? map.typeByName(typeName) : null;
    }

    default @Nullable SU unmappedServerboundType(final State state, final String typeName) {
        PacketTypeMap<SU> map = unmappedServerboundPacketTypes().get(state);
        return map != null ? map.typeByName(typeName) : null;
    }

    default @Nullable CU unmappedClientboundType(final State state, final int packetId) {
        PacketTypeMap<CU> map = unmappedClientboundPacketTypes().get(state);
        return map != null ? map.typeById(packetId) : null;
    }

    default @Nullable SU unmappedServerboundType(final State state, final int packetId) {
        PacketTypeMap<SU> map = unmappedServerboundPacketTypes().get(state);
        return map != null ? map.typeById(packetId) : null;
    }
}
