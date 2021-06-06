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
import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.TagData;
import com.viaversion.viaversion.util.Int2IntBiHashMap;
import com.viaversion.viaversion.util.Int2IntBiMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class MappingDataBase implements MappingData {
    protected final String oldVersion;
    protected final String newVersion;
    protected final boolean hasDiffFile;
    protected Int2IntBiMap itemMappings;
    protected ParticleMappings particleMappings;
    protected Mappings blockMappings;
    protected Mappings blockStateMappings;
    protected Mappings soundMappings;
    protected Mappings statisticsMappings;
    protected Map<RegistryType, List<TagData>> tags;
    protected boolean loadItems = true;

    public MappingDataBase(String oldVersion, String newVersion) {
        this(oldVersion, newVersion, false);
    }

    public MappingDataBase(String oldVersion, String newVersion, boolean hasDiffFile) {
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
        this.hasDiffFile = hasDiffFile;
    }

    @Override
    public void load() {
        getLogger().info("Loading " + oldVersion + " -> " + newVersion + " mappings...");
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
            itemMappings = new Int2IntBiHashMap();
            itemMappings.defaultReturnValue(-1);
            MappingDataLoader.mapIdentifiers(itemMappings, oldMappings.getAsJsonObject("items"), newMappings.getAsJsonObject("items"),
                    diffmapping != null ? diffmapping.getAsJsonObject("items") : null);
        }

        if (diffmapping != null && diffmapping.has("tags")) {
            this.tags = new EnumMap<>(RegistryType.class);
            JsonObject tags = diffmapping.getAsJsonObject("tags");
            if (tags.has(RegistryType.ITEM.getResourceLocation())) {
                loadTags(RegistryType.ITEM, tags, MappingDataLoader.indexedObjectToMap(newMappings.getAsJsonObject("items")));
            }
            if (tags.has(RegistryType.BLOCK.getResourceLocation())) {
                loadTags(RegistryType.BLOCK, tags, MappingDataLoader.indexedObjectToMap(newMappings.getAsJsonObject("blocks")));
            }
        }

        loadExtras(oldMappings, newMappings, diffmapping);
    }

    private void loadTags(RegistryType type, JsonObject object, Object2IntMap<String> typeMapping) {
        JsonObject tags = object.getAsJsonObject(type.getResourceLocation());
        List<TagData> tagsList = new ArrayList<>(tags.size());
        for (Map.Entry<String, JsonElement> entry : tags.entrySet()) {
            JsonArray array = entry.getValue().getAsJsonArray();
            int[] entries = new int[array.size()];
            int i = 0;
            for (JsonElement element : array) {
                String stringId = element.getAsString();
                if (!typeMapping.containsKey(stringId) && !typeMapping.containsKey(stringId = stringId.replace("minecraft:", ""))) { // aaa
                    getLogger().warning(type + " Tags contains invalid type identifier " + stringId + " in tag " + entry.getKey());
                    continue;
                }

                entries[i++] = typeMapping.getInt(stringId);
            }
            tagsList.add(new TagData(entry.getKey(), entries));
        }

        this.tags.put(type, tagsList);
    }

    @Override
    public int getNewBlockStateId(int id) {
        return checkValidity(id, blockStateMappings.getNewId(id), "blockstate");
    }

    @Override
    public int getNewBlockId(int id) {
        return checkValidity(id, blockMappings.getNewId(id), "block");
    }

    @Override
    public int getNewItemId(int id) {
        return checkValidity(id, itemMappings.get(id), "item");
    }

    @Override
    public int getOldItemId(int id) {
        int oldId = itemMappings.inverse().get(id);
        // Remap new items to stone
        return oldId != -1 ? oldId : 1;
    }

    @Override
    public int getNewParticleId(int id) {
        return checkValidity(id, particleMappings.getMappings().getNewId(id), "particles");
    }

    @Override
    public @Nullable List<TagData> getTags(RegistryType type) {
        return tags != null ? tags.get(type) : null;
    }

    @Override
    public @Nullable Int2IntBiMap getItemMappings() {
        return itemMappings;
    }

    @Override
    public @Nullable ParticleMappings getParticleMappings() {
        return particleMappings;
    }

    @Override
    public @Nullable Mappings getBlockMappings() {
        return blockMappings;
    }

    @Override
    public @Nullable Mappings getBlockStateMappings() {
        return blockStateMappings;
    }

    @Override
    public @Nullable Mappings getSoundMappings() {
        return soundMappings;
    }

    @Override
    public @Nullable Mappings getStatisticsMappings() {
        return statisticsMappings;
    }

    protected @Nullable Mappings loadFromArray(JsonObject oldMappings, JsonObject newMappings, @Nullable JsonObject diffMappings, String key) {
        if (!oldMappings.has(key) || !newMappings.has(key)) return null;

        JsonObject diff = diffMappings != null ? diffMappings.getAsJsonObject(key) : null;
        return new IntArrayMappings(oldMappings.getAsJsonArray(key), newMappings.getAsJsonArray(key), diff);
    }

    protected @Nullable Mappings loadFromObject(JsonObject oldMappings, JsonObject newMappings, @Nullable JsonObject diffMappings, String key) {
        if (!oldMappings.has(key) || !newMappings.has(key)) return null;

        JsonObject diff = diffMappings != null ? diffMappings.getAsJsonObject(key) : null;
        return new IntArrayMappings(oldMappings.getAsJsonObject(key), newMappings.getAsJsonObject(key), diff);
    }

    protected @Nullable JsonObject loadDiffFile() {
        return MappingDataLoader.loadData("mappingdiff-" + oldVersion + "to" + newVersion + ".json");
    }

    protected Logger getLogger() {
        return Via.getPlatform().getLogger();
    }

    /**
     * Returns the given mapped id if valid, else 0 with a warning logged to the console.
     *
     * @param id       unmapped id
     * @param mappedId mapped id
     * @param type     mapping type (e.g. "item")
     * @return the given mapped id if valid, else 0
     */
    protected int checkValidity(int id, int mappedId, String type) {
        if (mappedId == -1) {
            getLogger().warning(String.format("Missing %s %s for %s %s %d", newVersion, type, oldVersion, type, id));
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
