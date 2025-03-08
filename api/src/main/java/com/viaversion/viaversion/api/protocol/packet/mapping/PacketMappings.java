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
package com.viaversion.viaversion.api.protocol.packet.mapping;

import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.State;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Mappings to transform packets between two protocol versions.
 */
public interface PacketMappings {

    /**
     * Returns a packet mapping for the given packet.
     *
     * @param state      protocol state
     * @param unmappedId unmapped packet id
     * @return packet mapping if present
     */
    @Nullable PacketMapping mappedPacket(State state, int unmappedId);

    /**
     * Returns whether the given packet type has a mapping.
     *
     * @param packetType unmapped packet type
     * @return whether the given packet type has a mapping
     */
    default boolean hasMapping(PacketType packetType) {
        return mappedPacket(packetType.state(), packetType.getId()) != null;
    }

    /**
     * Returns whether the given packet type has a mapping.
     *
     * @param state      protocol state
     * @param unmappedId unmapped packet id
     * @return whether the given packet type has a mapping
     */
    default boolean hasMapping(State state, int unmappedId) {
        return mappedPacket(state, unmappedId) != null;
    }

    /**
     * Adds a packet mapping.
     *
     * @param packetType unmapped packet type
     * @param mapping    packet mapping
     */
    default void addMapping(PacketType packetType, PacketMapping mapping) {
        addMapping(packetType.state(), packetType.getId(), mapping);
    }

    /**
     * Adds a packet mapping.
     *
     * @param state      protocol state
     * @param unmappedId unmapped packet id
     * @param mapping    packet mapping
     */
    void addMapping(State state, int unmappedId, PacketMapping mapping);

    static PacketMappings arrayMappings() {
        return new PacketArrayMappings();
    }
}
