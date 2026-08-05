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
package com.viaversion.viaversion.protocols.v1_12_2to1_13.storage;

import com.viaversion.viaversion.connection.ProtocolStorablesBase;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ProtocolStorables1_13 extends ProtocolStorablesBase {

    private final TabCompleteTracker tabCompleteTracker = new TabCompleteTracker();
    private final BlockStorage blockStorage = new BlockStorage();
    private BlockConnectionStorage blockConnectionStorage;

    public TabCompleteTracker tabCompleteTracker() {
        return tabCompleteTracker;
    }

    public BlockStorage blockStorage() {
        return blockStorage;
    }

    public @Nullable BlockConnectionStorage blockConnectionStorage() {
        return blockConnectionStorage;
    }

    public void setBlockConnectionStorage(final BlockConnectionStorage blockConnectionStorage) {
        this.blockConnectionStorage = blockConnectionStorage;
    }
}
