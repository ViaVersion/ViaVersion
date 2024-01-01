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
package com.viaversion.viaversion.api.protocol.packet;

/**
 * Interface representing PLAY state packets, ordered by their packet id.
 *
 * @see ClientboundPacketType
 * @see ServerboundPacketType
 */
public interface PacketType {

    /**
     * Returns the packet id for the implemented protocol.
     *
     * @return packet id for the implemented protocol
     */
    int getId();

    /**
     * Returns the name of the packet, to be consistent over multiple versions.
     *
     * @return name of the packet
     */
    String getName();

    /**
     * Clientbound or serverbound direction.
     *
     * @return direction
     */
    Direction direction();

    /**
     * Returns the protocol state the packet belongs to.
     *
     * @return protocol state
     */
    default State state() {
        return State.PLAY;
    }
}
