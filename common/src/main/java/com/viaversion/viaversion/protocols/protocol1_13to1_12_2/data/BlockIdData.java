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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data;

import com.google.common.collect.ObjectArrays;
import com.google.gson.reflect.TypeToken;
import com.viaversion.viaversion.api.Via;
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
    public static Map<String, String[]> blockIdMapping;
    public static Map<String, String[]> fallbackReverseMapping;
    public static Int2ObjectMap<String> numberIdToString;

    public static void init() {
        InputStream stream = MappingData.class.getClassLoader()
                .getResourceAsStream("assets/viaversion/data/blockIds1.12to1.13.json");
        try (InputStreamReader reader = new InputStreamReader(stream)) {
            Map<String, String[]> map = GsonUtil.getGson().fromJson(
                    reader,
                    new TypeToken<Map<String, String[]>>() {
                    }.getType());
            blockIdMapping = new HashMap<>(map);
            fallbackReverseMapping = new HashMap<>();
            for (Map.Entry<String, String[]> entry : blockIdMapping.entrySet()) {
                for (String val : entry.getValue()) {
                    String[] previous = fallbackReverseMapping.get(val);
                    if (previous == null) previous = PREVIOUS;
                    fallbackReverseMapping.put(val, ObjectArrays.concat(previous, entry.getKey()));
                }
            }
        } catch (IOException e) {
            Via.getPlatform().getLogger().log(Level.SEVERE, "Failed to load block id mappings (1.12.2 -> 1.13)", e);
        }

        InputStream blockS = MappingData.class.getClassLoader()
                .getResourceAsStream("assets/viaversion/data/blockNumberToString1.12.json");
        try (InputStreamReader blockR = new InputStreamReader(blockS)) {
            Map<Integer, String> map = GsonUtil.getGson().fromJson(
                    blockR,
                    new TypeToken<Map<Integer, String>>() {
                    }.getType()
            );
            numberIdToString = new Int2ObjectOpenHashMap<>(map);
        } catch (IOException e) {
            Via.getPlatform().getLogger().log(Level.SEVERE, "Failed to load block number to string mappings (1.12.2)", e);
        }
        // Ignored
    }
}
