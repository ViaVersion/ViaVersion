package us.myles.ViaVersion.api.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.Nullable;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.util.GsonUtil;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
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
     * Returns the cached mappings. Cleared after Via has been fully loaded.
     *
     * @see #isCacheJsonMappings()
     */
    public static Map<String, JsonObject> getMappingsCache() {
        return MAPPINGS_CACHE;
    }

    /**
     * Loads the file from the plugin folder if present, else from the bundled resources.
     */
    public static JsonObject loadFromDataDir(String name) {
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
     */
    public static JsonObject loadData(String name) {
        return loadData(name, false);
    }

    /**
     * Loads the file from the bundled resources. Uses the cache if enabled.
     *
     * @param cacheIfEnabled whether loaded files should be cached
     */
    public static JsonObject loadData(String name, boolean cacheIfEnabled) {
        if (cacheJsonMappings) {
            JsonObject cached = MAPPINGS_CACHE.get(name);
            if (cached != null) {
                return cached;
            }
        }

        InputStream stream = getResource(name);
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

    public static void mapIdentifiers(Map<Integer, Integer> output, JsonObject oldIdentifiers, JsonObject newIdentifiers) {
        mapIdentifiers(output, oldIdentifiers, newIdentifiers, null);
    }

    public static void mapIdentifiers(Map<Integer, Integer> output, JsonObject oldIdentifiers, JsonObject newIdentifiers, JsonObject diffIdentifiers) {
        for (Map.Entry<String, JsonElement> entry : oldIdentifiers.entrySet()) {
            Map.Entry<String, JsonElement> value = mapIdentifierEntry(entry, oldIdentifiers, newIdentifiers, diffIdentifiers);
            if (value != null) {
                output.put(Integer.parseInt(entry.getKey()), Integer.parseInt(value.getKey()));
            }
        }
    }

    public static void mapIdentifiers(short[] output, JsonObject oldIdentifiers, JsonObject newIdentifiers) {
        MappingDataLoader.mapIdentifiers(output, oldIdentifiers, newIdentifiers, null);
    }

    public static void mapIdentifiers(short[] output, JsonObject oldIdentifiers, JsonObject newIdentifiers, JsonObject diffIdentifiers) {
        for (Map.Entry<String, JsonElement> entry : oldIdentifiers.entrySet()) {
            Map.Entry<String, JsonElement> value = mapIdentifierEntry(entry, oldIdentifiers, newIdentifiers, diffIdentifiers);
            if (value != null) {
                output[Integer.parseInt(entry.getKey())] = Short.parseShort(value.getKey());
            }
        }
    }

    @Nullable
    private static Map.Entry<String, JsonElement> mapIdentifierEntry(Map.Entry<String, JsonElement> entry, JsonObject oldIdentifiers, JsonObject newIdentifiers, JsonObject diffIdentifiers) {
        Map.Entry<String, JsonElement> value = findValue(newIdentifiers, entry.getValue().getAsString());
        if (value == null) {
            // Search in diff mappings
            if (diffIdentifiers != null) {
                value = findValue(newIdentifiers, diffIdentifiers.get(entry.getKey()).getAsString());
            }
            if (value == null) {
                if (!Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug()) {
                    Via.getPlatform().getLogger().warning("No key for " + entry.getValue() + " :( ");
                }
                return null;
            }
        }
        return value;
    }

    public static void mapIdentifiers(short[] output, JsonArray oldIdentifiers, JsonArray newIdentifiers, boolean warnOnMissing) {
        mapIdentifiers(output, oldIdentifiers, newIdentifiers, null, warnOnMissing);
    }

    public static void mapIdentifiers(short[] output, JsonArray oldIdentifiers, JsonArray newIdentifiers, JsonObject diffIdentifiers, boolean warnOnMissing) {
        for (int i = 0; i < oldIdentifiers.size(); i++) {
            JsonElement value = oldIdentifiers.get(i);
            Integer index = findIndex(newIdentifiers, value.getAsString());
            if (index == null) {
                if (diffIdentifiers != null) {
                    JsonElement diffElement = diffIdentifiers.get(value.getAsString());
                    if (diffElement != null) {
                        index = findIndex(newIdentifiers, diffElement.getAsString());
                    }
                }
                if (index == null) {
                    if (warnOnMissing && !Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug()) {
                        Via.getPlatform().getLogger().warning("No key for " + value + " :( ");
                    }
                    continue;
                }
            }
            output[i] = index.shortValue();
        }
    }

    public static void mapIdentifiers(byte[] output, JsonObject oldIdentifiers, JsonObject newIdentifiers) {
        for (Map.Entry<String, JsonElement> entry : oldIdentifiers.entrySet()) {
            Map.Entry<String, JsonElement> value = MappingDataLoader.findValue(newIdentifiers, entry.getValue().getAsString());
            if (value == null) {
                Via.getPlatform().getLogger().warning("No key for " + entry.getValue() + " :( ");
                continue;
            }
            output[Integer.parseInt(entry.getKey())] = Byte.parseByte(value.getKey());
        }
    }

    @Nullable
    public static Map.Entry<String, JsonElement> findValue(JsonObject object, String needle) {
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String value = entry.getValue().getAsString();
            if (value.equals(needle)) {
                return entry;
            }
        }
        return null;
    }

    @Nullable
    public static Integer findIndex(JsonArray array, String value) {
        for (int i = 0; i < array.size(); i++) {
            JsonElement v = array.get(i);
            if (v.getAsString().equals(value)) {
                return i;
            }
        }
        return null;
    }

    public static InputStream getResource(String name) {
        return MappingDataLoader.class.getClassLoader().getResourceAsStream("assets/viaversion/data/" + name);
    }

    public static URL getResourceUrl(String name) {
        return MappingDataLoader.class.getClassLoader().getResource("assets/viaversion/data/" + name);
    }
}
