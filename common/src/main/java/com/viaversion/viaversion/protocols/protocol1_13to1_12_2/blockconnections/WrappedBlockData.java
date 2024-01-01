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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.blockconnections;

import com.viaversion.viaversion.util.Key;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public final class WrappedBlockData {
    private final LinkedHashMap<String, String> blockData = new LinkedHashMap<>();
    private final String minecraftKey;
    private final int savedBlockStateId;

    public static WrappedBlockData fromString(String s) {
        String[] array = s.split("\\[");
        String key = array[0];
        WrappedBlockData wrappedBlockdata = new WrappedBlockData(key, ConnectionData.getId(s));
        if (array.length > 1) {
            String blockData = array[1];
            blockData = blockData.replace("]", "");
            String[] data = blockData.split(",");
            for (String d : data) {
                String[] a = d.split("=");
                wrappedBlockdata.blockData.put(a[0], a[1]);
            }
        }
        return wrappedBlockdata;
    }

    private WrappedBlockData(String minecraftKey, int savedBlockStateId) {
        this.minecraftKey = Key.namespaced(minecraftKey);
        this.savedBlockStateId = savedBlockStateId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(minecraftKey + "[");
        for (Entry<String, String> entry : blockData.entrySet()) {
            sb.append(entry.getKey()).append('=').append(entry.getValue()).append(',');
        }
        return sb.substring(0, sb.length() - 1) + "]";
    }

    public String getMinecraftKey() {
        return minecraftKey;
    }

    public int getSavedBlockStateId() {
        return savedBlockStateId;
    }

    public int getBlockStateId() {
        return ConnectionData.getId(toString());
    }

    public WrappedBlockData set(String data, Object value) {
        if (!hasData(data))
            throw new UnsupportedOperationException("No blockdata found for " + data + " at " + minecraftKey);
        blockData.put(data, value.toString());
        return this;
    }

    public String getValue(String data) {
        return blockData.get(data);
    }

    public boolean hasData(String key) {
        return blockData.containsKey(key);
    }
}
