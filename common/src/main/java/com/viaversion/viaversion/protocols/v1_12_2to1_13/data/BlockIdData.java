/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_12_2to1_13.data;

import com.google.common.collect.ObjectArrays;
import com.google.gson.reflect.TypeToken;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.Protocol1_12_2To1_13;
import com.viaversion.viaversion.util.GsonUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class BlockIdData {
    public static final String[] PREVIOUS = new String[0];
    public static final Map<String, String[]> blockIdMapping = new HashMap<>();
    public static final Map<String, String[]> fallbackReverseMapping = new HashMap<>();
    public static final Int2ObjectMap<String> numberIdToString = new Int2ObjectOpenHashMap<>();

    public static void init() {
        // Data from https://minecraft.gamepedia.com/1.13/Flattening
        InputStream stream = MappingData1_13.class.getClassLoader()
            .getResourceAsStream("assets/viaversion/data/blockIds1.12to1.13.json");
        try (InputStreamReader reader = new InputStreamReader(stream)) {
            Map<String, String[]> map = GsonUtil.getGson().fromJson(
                reader,
                new TypeToken<Map<String, String[]>>() {
                }.getType());
            blockIdMapping.putAll(map);
            for (Map.Entry<String, String[]> entry : blockIdMapping.entrySet()) {
                for (String val : entry.getValue()) {
                    String[] previous = fallbackReverseMapping.get(val);
                    if (previous == null) previous = PREVIOUS;
                    fallbackReverseMapping.put(val, ObjectArrays.concat(previous, entry.getKey()));
                }
            }
        } catch (IOException e) {
            Protocol1_12_2To1_13.LOGGER.log(Level.SEVERE, "Failed to load block id mappings", e);
        }

        InputStream blockS = MappingData1_13.class.getClassLoader()
            .getResourceAsStream("assets/viaversion/data/blockNumberToString1.12.json");
        try (InputStreamReader blockR = new InputStreamReader(blockS)) {
            Map<Integer, String> map = GsonUtil.getGson().fromJson(
                blockR,
                new TypeToken<Map<Integer, String>>() {
                }.getType()
            );
            numberIdToString.putAll(map);
        } catch (IOException e) {
            Protocol1_12_2To1_13.LOGGER.log(Level.SEVERE, "Failed to load block number to string mappings", e);
        }
    }
}
