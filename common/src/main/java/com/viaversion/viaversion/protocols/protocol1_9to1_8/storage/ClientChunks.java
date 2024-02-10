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
package com.viaversion.viaversion.protocols.protocol1_9to1_8.storage;

import com.google.common.collect.Sets;
import com.viaversion.viaversion.api.connection.StorableObject;
import java.util.Set;

public class ClientChunks implements StorableObject {
    private final Set<Long> loadedChunks = Sets.newConcurrentHashSet();

    public static long toLong(int msw, int lsw) {
        return ((long) msw << 32) + lsw + 2147483648L;
    }

    public Set<Long> getLoadedChunks() {
        return loadedChunks;
    }

}
