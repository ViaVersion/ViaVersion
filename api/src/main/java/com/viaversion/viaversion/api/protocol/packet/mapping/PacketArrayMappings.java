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

import com.viaversion.viaversion.api.protocol.packet.State;
import java.util.Arrays;
import org.checkerframework.checker.nullness.qual.Nullable;

final class PacketArrayMappings implements PacketMappings {
    private final PacketMapping[][] packets = new PacketMapping[State.values().length][];

    @Override
    public @Nullable PacketMapping mappedPacket(final State state, final int unmappedId) {
        final PacketMapping[] packets = this.packets[state.ordinal()];
        if (packets != null && unmappedId >= 0 && unmappedId < packets.length) {
            return packets[unmappedId];
        }
        return null;
    }

    @Override
    public void addMapping(final State state, final int unmappedId, final PacketMapping mapping) {
        final int ordinal = state.ordinal();
        PacketMapping[] packets = this.packets[ordinal];
        if (packets == null) {
            packets = new PacketMapping[unmappedId + 8];
            this.packets[ordinal] = packets;
        } else if (unmappedId >= packets.length) {
            packets = Arrays.copyOf(packets, unmappedId + 32);
            this.packets[ordinal] = packets;
        }

        packets[unmappedId] = mapping;
    }
}