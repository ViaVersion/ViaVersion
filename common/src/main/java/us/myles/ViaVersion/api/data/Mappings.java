package us.myles.ViaVersion.api.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Arrays;

public class Mappings {
    protected final short[] oldToNew;

    /**
     * Maps old identifiers to the new ones.
     * If an old value cannot be found in the new mappings, the diffmapping will be checked for the given entry.
     *
     * @param size set size of the underlying short array
     * @param oldMapping mappings to map from
     * @param newMapping mappings to map to
     * @param diffMapping extra mappings that will be used/scanned when an entry cannot be found
     */
    public Mappings(int size, JsonObject oldMapping, JsonObject newMapping, JsonObject diffMapping) {
        oldToNew = new short[size];
        Arrays.fill(oldToNew, (short) -1);
        MappingDataLoader.mapIdentifiers(oldToNew, oldMapping, newMapping, diffMapping);
    }

    public Mappings(JsonObject oldMapping, JsonObject newMapping, JsonObject diffMapping) {
        this(oldMapping.entrySet().size(), oldMapping, newMapping, diffMapping);
    }

    /**
     * Maps old identifiers to the new ones.
     *
     * @param size set size of the underlying short array
     * @param oldMapping mappings to map from
     * @param newMapping mappings to map to
     */
    public Mappings(int size, JsonObject oldMapping, JsonObject newMapping) {
        oldToNew = new short[size];
        Arrays.fill(oldToNew, (short) -1);
        MappingDataLoader.mapIdentifiers(oldToNew, oldMapping, newMapping);
    }

    public Mappings(JsonObject oldMapping, JsonObject newMapping) {
        this(oldMapping.entrySet().size(), oldMapping, newMapping);
    }

    /**
     * Maps old identifiers to the new ones.
     *
     * @param size set size of the underlying short array
     * @param oldMapping mappings to map from
     * @param newMapping mappings to map to
     */
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
