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
package com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.minecraft.PlayerMessageSignature;
import java.util.Arrays;

public final class ReceivedMessagesStorage implements StorableObject {
    private final PlayerMessageSignature[] signatures = new PlayerMessageSignature[5];
    private PlayerMessageSignature lastSignature;
    private int size;
    private int unacknowledged;

    public boolean add(final PlayerMessageSignature signature) {
        if (signature.equals(this.lastSignature)) {
            return false;
        }

        this.lastSignature = signature;
        PlayerMessageSignature toPush = signature;
        for (int i = 0; i < this.size; ++i) {
            final PlayerMessageSignature entry = this.signatures[i];
            this.signatures[i] = toPush;
            toPush = entry;
            if (entry.uuid().equals(signature.uuid())) {
                return true;
            }
        }

        if (this.size < this.signatures.length) {
            this.signatures[this.size++] = toPush;
        }
        return true;
    }

    public PlayerMessageSignature[] lastSignatures() {
        return Arrays.copyOf(this.signatures, size);
    }

    public int tickUnacknowledged() {
        return unacknowledged++;
    }

    public void resetUnacknowledgedCount() {
        unacknowledged = 0;
    }

    public void clear() {
        this.size = 0;
        this.unacknowledged = 0;
        this.lastSignature = null;
        Arrays.fill(this.signatures, null);
    }
}
