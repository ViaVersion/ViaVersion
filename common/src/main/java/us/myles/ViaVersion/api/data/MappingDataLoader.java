package us.myles.ViaVersion.api.data;

import com.google.gson.*;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.util.GsonUtil;

import java.io.*;
import java.util.Map;

public class MappingDataLoader {

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

    public static JsonObject loadData(String name) {
        InputStream stream = getResource(name);
        InputStreamReader reader = new InputStreamReader(stream);
        try {
            return GsonUtil.getGson().fromJson(reader, JsonObject.class);
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
                    index = findIndex(newIdentifiers, diffIdentifiers.get(value.getAsString()).getAsString());
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

    public static Map.Entry<String, JsonElement> findValue(JsonObject object, String needle) {
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String value = entry.getValue().getAsString();
            if (value.equals(needle)) {
                return entry;
            }
        }
        return null;
    }

    public static Integer findIndex(JsonArray array, String value) {
        for (int i = 0; i < array.size(); i++) {
            JsonElement v = array.get(i);
            if (v.getAsString().equals(value)) {
                return i;
            }
        }
        return null;
    }

    private static InputStream getResource(String name) {
        return MappingDataLoader.class.getClassLoader().getResourceAsStream("assets/viaversion/data/" + name);
    }
}
