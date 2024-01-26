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

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import org.checkerframework.checker.nullness.qual.Nullable;

final class PacketIdMapping implements PacketMapping {
    private final int mappedPacketId;
    private PacketHandler handler;

    PacketIdMapping(int mappedPacketId, @Nullable PacketHandler handler) {
        this.mappedPacketId = mappedPacketId;
        this.handler = handler;
    }

    @Override
    public void applyType(final PacketWrapper wrapper) {
        //noinspection deprecation
        wrapper.setId(mappedPacketId);
    }

    @Override
    public void appendHandler(final PacketHandler handler) {
        if (this.handler == null) {
            this.handler = handler;
        } else {
            this.handler = this.handler.then(handler);
        }
    }

    @Override
    public @Nullable PacketHandler handler() {
        return handler;
    }
}