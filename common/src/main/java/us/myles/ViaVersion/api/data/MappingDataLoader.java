package us.myles.ViaVersion.api.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.util.GsonUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class MappingDataLoader {

    public static JsonObject loadData(String name) {
        InputStream stream = MappingDataLoader.class.getClassLoader().getResourceAsStream("assets/viaversion/data/" + name);
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
        for (Map.Entry<String, JsonElement> entry : oldIdentifiers.entrySet()) {
            Map.Entry<String, JsonElement> value = findValue(newIdentifiers, entry.getValue().getAsString());
            if (value == null) {
                if (!Via.getConfig().isSuppress1_13ConversionErrors() || Via.getManager().isDebug()) {
                    Via.getPlatform().getLogger().warning("No key for " + entry.getValue() + " :( ");
                }
                continue;
            }
            output.put(Integer.parseInt(entry.getKey()), Integer.parseInt(value.getKey()));
        }
    }

    public static void mapIdentifiers(short[] output, JsonObject oldIdentifiers, JsonObject newIdentifiers) {
        for (Map.Entry<String, JsonElement> entry : oldIdentifiers.entrySet()) {
            Map.Entry<String, JsonElement> value = findValue(newIdentifiers, entry.getValue().getAsString());
            if (value == null) {
                if (!Via.getConfig().isSuppress1_13ConversionErrors() || Via.getManager().isDebug()) {
                    Via.getPlatform().getLogger().warning("No key for " + entry.getValue() + " :( ");
                }
                continue;
            }
            output[Integer.parseInt(entry.getKey())] = Short.parseShort(value.getKey());
        }
    }

    public static void mapIdentifiers(short[] output, JsonArray oldIdentifiers, JsonArray newIdentifiers) {
        for (int i = 0; i < oldIdentifiers.size(); i++) {
            JsonElement v = oldIdentifiers.get(i);
            Integer index = findIndex(newIdentifiers, v.getAsString());
            if (index == null) {
                if (!Via.getConfig().isSuppress1_13ConversionErrors() || Via.getManager().isDebug()) {
                    Via.getPlatform().getLogger().warning("No key for " + v + " :( ");
                }
                continue;
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
}
