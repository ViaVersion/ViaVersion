package us.myles.ViaVersion.api.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Arrays;

public class Mappings {
    protected final short[] oldToNew;

    public Mappings(int size, JsonObject oldMapping, JsonObject newMapping) {
        oldToNew = new short[size];
        Arrays.fill(oldToNew, (short) -1);
        MappingDataLoader.mapIdentifiers(oldToNew, oldMapping, newMapping);
    }

    public Mappings(JsonObject oldMapping, JsonObject newMapping) {
        this(oldMapping.entrySet().size(), oldMapping, newMapping);
    }

    public Mappings(int size, JsonArray oldMapping, JsonArray newMapping) {
        oldToNew = new short[size];
        Arrays.fill(oldToNew, (short) -1);
        MappingDataLoader.mapIdentifiers(oldToNew, oldMapping, newMapping);
    }

    public Mappings(JsonArray oldMapping, JsonArray newMapping) {
        this(oldMapping.size(), oldMapping, newMapping);
    }

    public int getNewId(int old) {
        return old >= 0 && old < oldToNew.length ? oldToNew[old] : -1;
    }
}
