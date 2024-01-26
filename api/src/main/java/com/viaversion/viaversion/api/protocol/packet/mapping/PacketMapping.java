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
package com.viaversion.viaversion.api.protocol.packet.mapping;

import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Packet mapping over packet types or ids containing a packet transformer.
 */
public interface PacketMapping {

    /**
     * Applies the changed packet type or id to the given packet wrapper.
     *
     * @param wrapper packet wrapper
     */
    void applyType(PacketWrapper wrapper);

    /**
     * Returns a packet transformer to transform a packet from one protocol version to another.
     *
     * @return packet transformer, or null if no action has to be taken
     */
    @Nullable PacketHandler handler();

    /**
     * Appends a packet transformer to the current packet transformer.
     *
     * @param handler packet transformer
     */
    void appendHandler(PacketHandler handler);

    static PacketMapping of(final int mappedPacketId, @Nullable final PacketHandler handler) {
        return new PacketIdMapping(mappedPacketId, handler);
    }

    static PacketMapping of(@Nullable final PacketType mappedPacketType, @Nullable final PacketHandler handler) {
        return new PacketTypeMapping(mappedPacketType, handler);
    }
}