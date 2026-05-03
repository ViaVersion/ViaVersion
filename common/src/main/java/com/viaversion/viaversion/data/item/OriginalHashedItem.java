/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.data.item;

import com.viaversion.viaversion.api.minecraft.item.HashedItem;
import com.viaversion.viaversion.api.minecraft.item.HashedStructuredItem;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Marker class to avoid continuous hashing and restoration of items in every protocol.
 * Instead, this restores it once and passes the result through to the protocol that originally stored the backup.
 */
public final class OriginalHashedItem extends HashedStructuredItem {
    private final String backupTagName;

    public OriginalHashedItem(final HashedItem item, final String backupTagName) {
        this(item.identifier(), item.amount(), item.dataHashesById(), item.removedDataIds(), backupTagName); // don't copy
    }

    public OriginalHashedItem(final int identifier, final int amount, final String backupTagName) {
        super(identifier, amount);
        this.backupTagName = backupTagName;
    }

    private OriginalHashedItem(final int identifier, final int amount, final Int2IntMap dataHashes, final IntSet removedData, final String backupTagName) {
        super(identifier, amount, dataHashes, removedData);
        this.backupTagName = backupTagName;
    }

    public String backupTagName() {
        return backupTagName;
    }

    public HashedItem asRegularItem() {
        return new HashedStructuredItem(identifier(), amount(), dataHashesById(), removedDataIds());
    }

    @Override
    public OriginalHashedItem copy() {
        return new OriginalHashedItem(identifier(), amount(), new Int2IntOpenHashMap(dataHashesById()), new IntOpenHashSet(removedDataIds()), backupTagName);
    }
}
