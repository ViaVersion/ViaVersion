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
package com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import java.util.Arrays;
import java.util.BitSet;

public final class AcknowledgedMessagesStorage implements StorableObject {
    private static final int MAX_HISTORY = 20;
    private final boolean[] trackedMessages = new boolean[MAX_HISTORY];
    private int offset;
    private int tail;
    private byte[] lastMessage;

    public boolean add(final byte[] message) {
        if (Arrays.equals(message, lastMessage)) {
            return false;
        }
        this.lastMessage = message;
        this.offset++;
        this.trackedMessages[this.tail] = true;
        this.tail = (this.tail + 1) % MAX_HISTORY;
        return true;
    }

    public BitSet toAck() {
        final BitSet acks = new BitSet(MAX_HISTORY);
        for (int i = 0; i < MAX_HISTORY; i++) {
            final int messageIndex = (this.tail + i) % MAX_HISTORY;
            acks.set(i, this.trackedMessages[messageIndex]);
        }
        return acks;
    }

    public int offset() {
        return this.offset;
    }

    public void clearOffset() {
        this.offset = 0;
    }
}
