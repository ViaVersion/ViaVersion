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
package com.viaversion.viaversion.protocols.v1_18_2to1_19.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;

public final class SequenceStorage implements StorableObject {

    // Bukkit fix
    private final Object lock = new Object();
    private int sequenceId = -1;

    // Protocol level fix
    private final Object2IntSortedMap<BlockPosition> pendingBlockChanges = new Object2IntLinkedOpenHashMap<>();

    public int setSequenceId(final int sequenceId) {
        synchronized (lock) {
            final int previousSequence = this.sequenceId;
            this.sequenceId = sequenceId;
            return previousSequence;
        }
    }

    public void addPendingBlockChange(final BlockPosition position, final int id) {
        final int lastSequence = this.pendingBlockChanges.isEmpty() ? 0 : this.pendingBlockChanges.getInt(this.pendingBlockChanges.lastKey());
        if (id > 0 && id >= lastSequence && !this.pendingBlockChanges.containsKey(position)) {
            // Store the last 200 pending block changes. Some may never get acked, so we need to limit the size.
            while (this.pendingBlockChanges.size() > 200) {
                this.pendingBlockChanges.removeInt(this.pendingBlockChanges.firstKey());
            }
            this.pendingBlockChanges.put(position, id);
        }
    }

    public int removePendingBlockChange(final BlockPosition position) {
        final int ackedSequence = this.pendingBlockChanges.getInt(position); // 0 if not found
        if (ackedSequence > 0) {
            this.pendingBlockChanges.values().removeIf(sequence -> sequence <= ackedSequence); // Remove all previous pending changes
        }
        return ackedSequence;
    }

    public void clearPendingBlockChanges() {
        this.pendingBlockChanges.clear();
    }

}
