package us.myles.ViaVersion.protocols.protocol1_15to1_14_4.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.util.GsonUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

public class MappingData {

    public static SoundMappings soundMappings;

    public static void init() {
        JsonObject mapping1_14 = loadData("mapping-1.14.json");
        JsonObject mapping1_15 = loadData("mapping-1.15.json");

        Via.getPlatform().getLogger().info("Loading 1.14 -> 1.15 sound mapping...");
        soundMappings = new SoundMappingShortArray(mapping1_14.getAsJsonArray("sounds"), mapping1_15.getAsJsonArray("sounds"));
    }

    private static JsonObject loadData(String name) {
        InputStream stream = MappingData.class.getClassLoader().getResourceAsStream("assets/viaversion/data/" + name);
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

    private static void mapIdentifiers(short[] output, JsonArray oldIdentifiers, JsonArray newIdentifiers) {
        for (int i = 0; i < oldIdentifiers.size(); i++) {
            JsonElement v = oldIdentifiers.get(i);
            Integer index = findIndex(newIdentifiers, v.getAsString());
            if (index == null) {
                if (Via.getManager().isDebug()) {
                    Via.getPlatform().getLogger().warning("No key for " + v + " :( ");
                }
                continue;
            }
            output[i] = index.shortValue();
        }
    }

    private static Integer findIndex(JsonArray array, String value) {
        for (int i = 0; i < array.size(); i++) {
            JsonElement v = array.get(i);
            if (v.getAsString().equals(value)) {
                return i;
            }
        }
        return null;
    }

    public interface SoundMappings {
        int getNewSound(int old);
    }

    private static class SoundMappingShortArray implements SoundMappings {
        private short[] oldToNew;

        private SoundMappingShortArray(JsonArray mapping1_14, JsonArray mapping1_15) {
            oldToNew = new short[mapping1_14.size()];
            Arrays.fill(oldToNew, (short) -1);
            mapIdentifiers(oldToNew, mapping1_14, mapping1_15);
        }

        @Override
        public int getNewSound(int old) {
            return old >= 0 && old < oldToNew.length ? oldToNew[old] : -1;
        }
    }
}
