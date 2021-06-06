/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
package com.viaversion.viaversion.api.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.util.GsonUtil;
import com.viaversion.viaversion.util.Int2IntBiMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MappingDataLoader {

    private static final Map<String, JsonObject> MAPPINGS_CACHE = new ConcurrentHashMap<>();
    private static boolean cacheJsonMappings;

    /**
     * Returns true if a selected number of mappings should be cached.
     * If enabled, cleanup should be done after the cache is no longer needed.
     *
     * @return true if mappings should be cached
     */
    public static boolean isCacheJsonMappings() {
        return cacheJsonMappings;
    }

    public static void enableMappingsCache() {
        cacheJsonMappings = true;
    }

    /**
     * Returns the cached mappings. Cleared after ViaVersion has been fully loaded.
     *
     * @return cached mapping file json objects
     * @see #isCacheJsonMappings()
     */
    public static Map<String, JsonObject> getMappingsCache() {
        return MAPPINGS_CACHE;
    }

    /**
     * Loads the file from the plugin folder if present, else from the bundled resources.
     *
     * @return loaded json object, or null if not found or invalid
     */
    public static @Nullable JsonObject loadFromDataDir(String name) {
        File file = new File(Via.getPlatform().getDataFolder(), name);
        if (!file.exists()) return loadData(name);

        // Load the file from the platform's directory if present
        try (FileReader reader = new FileReader(file)) {
            return GsonUtil.getGson().fromJson(reader, JsonObject.class);
        } catch (JsonSyntaxException e) {
            // Users might mess up the format, so let's catch the syntax error
            Via.getPlatform().getLogger().warning(name + " is badly formatted!");
            e.printStackTrace();
        } catch (IOException | JsonIOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Loads the file from the bundled resources. Uses the cache if enabled.
     *
     * @return loaded json object from bundled resources if present
     */
    public static @Nullable JsonObject loadData(String name) {
        return loadData(name, false);
    }

    /**
     * Loads the file from the bundled resources. Uses the cache if enabled.
     *
     * @param cacheIfEnabled whether loaded files should be cached
     * @return loaded json object from bundled resources if present
     */
    public static @Nullable JsonObject loadData(String name, boolean cacheIfEnabled) {
        if (cacheJsonMappings) {
            JsonObject cached = MAPPINGS_CACHE.get(name);
            if (cached != null) {
                return cached;
            }
        }

        InputStream stream = getResource(name);
        if (stream == null) return null;

        InputStreamReader reader = new InputStreamReader(stream);
        try {
            JsonObject object = GsonUtil.getGson().fromJson(reader, JsonObject.class);
            if (cacheIfEnabled && cacheJsonMappings) {
                MAPPINGS_CACHE.put(name, object);
            }
            return object;
        } finally {
            try {
                reader.close();
            } catch (IOException ignored) {
                // Ignored
            }
        }
    }

    public static void mapIdentifiers(Int2IntBiMap output, JsonObject oldIdentifiers, JsonObject newIdentifiers, @Nullable JsonObject diffIdentifiers) {
        Object2IntMap<String> newIdentifierMap = MappingDataLoader.indexedObjectToMap(newIdentifiers);
        for (Map.Entry<String, JsonElement> entry : oldIdentifiers.entrySet()) {
            int value = mapIdentifierEntry(entry, newIdentifierMap, diffIdentifiers);
            if (value != -1) {
                output.put(Integer.parseInt(entry.getKey()), value);
            }
        }
    }

    public static void mapIdentifiers(int[] output, JsonObject oldIdentifiers, JsonObject newIdentifiers) {
        MappingDataLoader.mapIdentifiers(output, oldIdentifiers, newIdentifiers, null);
    }

    public static void mapIdentifiers(int[] output, JsonObject oldIdentifiers, JsonObject newIdentifiers, @Nullable JsonObject diffIdentifiers) {
        Object2IntMap<String> newIdentifierMap = MappingDataLoader.indexedObjectToMap(newIdentifiers);
        for (Map.Entry<String, JsonElement> entry : oldIdentifiers.entrySet()) {
            int value = mapIdentifierEntry(entry, newIdentifierMap, diffIdentifiers);
            if (value != -1) {
                output[Integer.parseInt(entry.getKey())] = value;
            }
        }
    }

    private static int mapIdentifierEntry(Map.Entry<String, JsonElement> entry, Object2IntMap newIdentifierMap, @Nullable JsonObject diffIdentifiers) {
        int value = newIdentifierMap.getInt(entry.getValue().getAsString());
        if (value == -1) {
            // Search in diff mappings
            if (diffIdentifiers != null) {
                JsonElement diffElement = diffIdentifiers.get(entry.getKey());
                if (diffElement != null) {
                    value = newIdentifierMap.getInt(diffElement.getAsString());
                }
            }
            if (value == -1) {
                if (!Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug()) {
                    Via.getPlatform().getLogger().warning("No key for " + entry.getValue() + " :( ");
                }
                return -1;
            }
        }
        return value;
    }

    public static void mapIdentifiers(int[] output, JsonArray oldIdentifiers, JsonArray newIdentifiers, boolean warnOnMissing) {
        mapIdentifiers(output, oldIdentifiers, newIdentifiers, null, warnOnMissing);
    }

    public static void mapIdentifiers(int[] output, JsonArray oldIdentifiers, JsonArray newIdentifiers, @Nullable JsonObject diffIdentifiers, boolean warnOnMissing) {
        Object2IntMap<String> newIdentifierMap = MappingDataLoader.arrayToMap(newIdentifiers);
        for (int i = 0; i < oldIdentifiers.size(); i++) {
            JsonElement oldIdentifier = oldIdentifiers.get(i);
            int mappedId = newIdentifierMap.getInt(oldIdentifier.getAsString());
            if (mappedId == -1) {
                // Search in diff mappings
                if (diffIdentifiers != null) {
                    JsonElement diffElement = diffIdentifiers.get(oldIdentifier.getAsString());
                    if (diffElement != null) {
                        String mappedName = diffElement.getAsString();
                        if (mappedName.isEmpty()) continue; // "empty" remaps

                        mappedId = newIdentifierMap.getInt(mappedName);
                    }
                }
                if (mappedId == -1) {
                    if (warnOnMissing && !Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug()) {
                        Via.getPlatform().getLogger().warning("No key for " + oldIdentifier + " :( ");
                    }
                    continue;
                }
            }
            output[i] = mappedId;
        }
    }

    /**
     * Returns a map of the object entries hashed by their id value.
     *
     * @param object json object
     * @return map with indexes hashed by their id value
     */
    public static Object2IntMap<String> indexedObjectToMap(JsonObject object) {
        Object2IntMap<String> map = new Object2IntOpenHashMap<>(object.size(), 1F);
        map.defaultReturnValue(-1);
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            map.put(entry.getValue().getAsString(), Integer.parseInt(entry.getKey()));
        }
        return map;
    }

    /**
     * Returns a map of the array entries hashed by their id value.
     *
     * @param array json array
     * @return map with indexes hashed by their id value
     */
    public static Object2IntMap<String> arrayToMap(JsonArray array) {
        Object2IntMap<String> map = new Object2IntOpenHashMap<>(array.size(), 1F);
        map.defaultReturnValue(-1);
        for (int i = 0; i < array.size(); i++) {
            map.put(array.get(i).getAsString(), i);
        }
        return map;
    }

    public static @Nullable InputStream getResource(String name) {
        return MappingDataLoader.class.getClassLoader().getResourceAsStream("assets/viaversion/data/" + name);
    }
}
