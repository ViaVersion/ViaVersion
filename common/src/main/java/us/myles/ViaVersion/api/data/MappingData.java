package us.myles.ViaVersion.api.data;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.util.Int2IntBiMap;

public class MappingData {
    protected final String oldVersion;
    protected final String newVersion;
    protected final boolean hasDiffFile;
    protected Int2IntBiMap itemMappings;
    protected ParticleMappings particleMappings;
    protected Mappings blockMappings;
    protected Mappings blockStateMappings;
    protected Mappings soundMappings;
    protected Mappings statisticsMappings;
    protected boolean loadItems = true;

    public MappingData(String oldVersion, String newVersion) {
        this(oldVersion, newVersion, false);
    }

    public MappingData(String oldVersion, String newVersion, boolean hasDiffFile) {
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
        this.hasDiffFile = hasDiffFile;
    }

    public void load() {
        Via.getPlatform().getLogger().info("Loading " + oldVersion + " -> " + newVersion + " mappings...");
        JsonObject diffmapping = hasDiffFile ? loadDiffFile() : null;
        JsonObject oldMappings = MappingDataLoader.loadData("mapping-" + oldVersion + ".json", true);
        JsonObject newMappings = MappingDataLoader.loadData("mapping-" + newVersion + ".json", true);

        blockMappings = loadFromObject(oldMappings, newMappings, diffmapping, "blocks");
        blockStateMappings = loadFromObject(oldMappings, newMappings, diffmapping, "blockstates");
        soundMappings = loadFromArray(oldMappings, newMappings, diffmapping, "sounds");
        statisticsMappings = loadFromArray(oldMappings, newMappings, diffmapping, "statistics");

        Mappings particles = loadFromArray(oldMappings, newMappings, diffmapping, "particles");
        if (particles != null) {
            particleMappings = new ParticleMappings(oldMappings.getAsJsonArray("particles"), particles);
        }

        if (loadItems && newMappings.has("items")) {
            itemMappings = new Int2IntBiMap();
            itemMappings.defaultReturnValue(-1);
            MappingDataLoader.mapIdentifiers(itemMappings, oldMappings.getAsJsonObject("items"), newMappings.getAsJsonObject("items"),
                    diffmapping != null ? diffmapping.getAsJsonObject("items") : null);
        }

        loadExtras(oldMappings, newMappings, diffmapping);
    }

    public int getNewBlockStateId(int id) {
        return checkValidity(id, blockStateMappings.getNewId(id), "blockstate");
    }

    public int getNewBlockId(int id) {
        return checkValidity(id, blockMappings.getNewId(id), "block");
    }

    public int getNewItemId(int id) {
        return checkValidity(id, itemMappings.get(id), "item");
    }

    public int getOldItemId(int id) {
        int oldId = itemMappings.inverse().get(id);
        // Remap new items to stone
        return oldId != -1 ? oldId : 1;
    }

    public int getNewParticleId(int id) {
        return checkValidity(id, particleMappings.getMappings().getNewId(id), "particles");
    }

    @Nullable
    public Int2IntBiMap getItemMappings() {
        return itemMappings;
    }

    @Nullable
    public ParticleMappings getParticleMappings() {
        return particleMappings;
    }

    @Nullable
    public Mappings getBlockMappings() {
        return blockMappings;
    }

    @Nullable
    public Mappings getBlockStateMappings() {
        return blockStateMappings;
    }

    @Nullable
    public Mappings getSoundMappings() {
        return soundMappings;
    }

    @Nullable
    public Mappings getStatisticsMappings() {
        return statisticsMappings;
    }

    @Nullable
    protected Mappings loadFromArray(JsonObject oldMappings, JsonObject newMappings, @Nullable JsonObject diffMappings, String key) {
        if (!oldMappings.has(key) || !newMappings.has(key)) return null;

        JsonObject diff = diffMappings != null ? diffMappings.getAsJsonObject(key) : null;
        return new Mappings(oldMappings.getAsJsonArray(key), newMappings.getAsJsonArray(key), diff);
    }

    @Nullable
    protected Mappings loadFromObject(JsonObject oldMappings, JsonObject newMappings, @Nullable JsonObject diffMappings, String key) {
        if (!oldMappings.has(key) || !newMappings.has(key)) return null;

        JsonObject diff = diffMappings != null ? diffMappings.getAsJsonObject(key) : null;
        return new Mappings(oldMappings.getAsJsonObject(key), newMappings.getAsJsonObject(key), diff);
    }

    protected JsonObject loadDiffFile() {
        return MappingDataLoader.loadData("mappingdiff-" + oldVersion + "to" + newVersion + ".json");
    }

    protected int checkValidity(int id, int mappedId, String type) {
        if (mappedId == -1) {
            Via.getPlatform().getLogger().warning(String.format("Missing %s %s for %s %s %d", newVersion, type, oldVersion, type, id));
            return 0;
        }
        return mappedId;
    }

    /**
     * To be overridden.
     *
     * @param oldMappings  old mappings
     * @param newMappings  new mappings
     * @param diffMappings diff mappings if present
     */
    protected void loadExtras(JsonObject oldMappings, JsonObject newMappings, @Nullable JsonObject diffMappings) {
    }
}
