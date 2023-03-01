/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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
import com.viaversion.viaversion.util.Key;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MappingDataBase implements MappingData {
    protected final String oldVersion;
    protected final String newVersion;
    protected final boolean hasDiffFile;
    protected BiMappings itemMappings;
    protected FullMappings argumentTypeMappings;
    protected FullMappings entityMappings;
    protected ParticleMappings particleMappings;
    protected Mappings blockMappings;
    protected Mappings blockStateMappings;
    protected Mappings blockEntityMappings;
    protected Mappings soundMappings;
    protected Mappings statisticsMappings;
    protected Mappings enchantmentMappings;
    protected Mappings paintingMappings;
    protected Map<RegistryType, List<TagData>> tags;

    public MappingDataBase(String unmappedVersion, String mappedVersion) {
        this(unmappedVersion, mappedVersion, false);
    }

    public MappingDataBase(String unmappedVersion, String mappedVersion, boolean hasDiffFile) {
        this.oldVersion = unmappedVersion;
        this.newVersion = mappedVersion;
        this.hasDiffFile = hasDiffFile;
    }

    @Override
    public void load() {
        if (Via.getManager().isDebug()) {
            getLogger().info("Loading " + oldVersion + " -> " + newVersion + " mappings...");
        }
        JsonObject diffmapping = hasDiffFile ? loadDiffFile() : null;
        JsonObject unmappedIdentifiers = MappingDataLoader.loadData("mapping-" + oldVersion + ".json", true);
        JsonObject mappedIdentifiers = MappingDataLoader.loadData("mapping-" + newVersion + ".json", true);

        blockMappings = loadFromArray(unmappedIdentifiers, mappedIdentifiers, diffmapping, "blocks");
        blockStateMappings = loadFromArray(unmappedIdentifiers, mappedIdentifiers, diffmapping, "blockstates");
        blockEntityMappings = loadFromArray(unmappedIdentifiers, mappedIdentifiers, diffmapping, "blockentities");
        soundMappings = loadFromArray(unmappedIdentifiers, mappedIdentifiers, diffmapping, "sounds");
        statisticsMappings = loadFromArray(unmappedIdentifiers, mappedIdentifiers, diffmapping, "statistics");
        enchantmentMappings = loadFromArray(unmappedIdentifiers, mappedIdentifiers, diffmapping, "enchantments");
        paintingMappings = loadFromArray(unmappedIdentifiers, mappedIdentifiers, diffmapping, "paintings");

        entityMappings = loadFullMappings(unmappedIdentifiers, mappedIdentifiers, diffmapping, "entities");
        argumentTypeMappings = loadFullMappings(unmappedIdentifiers, mappedIdentifiers, diffmapping, "argumenttypes");

        Mappings particles = loadFromArray(unmappedIdentifiers, mappedIdentifiers, diffmapping, "particles");
        if (particles != null) {
            particleMappings = new ParticleMappings(unmappedIdentifiers.getAsJsonArray("particles"), mappedIdentifiers.getAsJsonArray("particles"), particles);
        }

        itemMappings = loadBiFromArray(unmappedIdentifiers, mappedIdentifiers, diffmapping, "items");

        if (diffmapping != null && diffmapping.has("tags")) {
            this.tags = new EnumMap<>(RegistryType.class);
            JsonObject tags = diffmapping.getAsJsonObject("tags");
            if (tags.has(RegistryType.ITEM.resourceLocation())) {
                loadTags(RegistryType.ITEM, tags, MappingDataLoader.arrayToMap(mappedIdentifiers.getAsJsonArray("items")));
            }
            if (tags.has(RegistryType.BLOCK.resourceLocation())) {
                loadTags(RegistryType.BLOCK, tags, MappingDataLoader.arrayToMap(mappedIdentifiers.getAsJsonArray("blocks")));
            }
        }

        loadExtras(unmappedIdentifiers, mappedIdentifiers, diffmapping);
    }

    protected FullMappings loadFullMappings(JsonObject unmappedIdentifiers, JsonObject mappedIdentifiers, @Nullable JsonObject diffMappings, String key) {
        Mappings mappings = loadFromArray(unmappedIdentifiers, mappedIdentifiers, diffMappings, key);
        return mappings != null ? new FullMappingsBase(unmappedIdentifiers.getAsJsonArray(key), mappedIdentifiers.getAsJsonArray(key), mappings) : null;
    }

    private void loadTags(RegistryType type, JsonObject object, Object2IntMap<String> typeMapping) {
        JsonObject tags = object.getAsJsonObject(type.resourceLocation());
        List<TagData> tagsList = new ArrayList<>(tags.size());
        for (Map.Entry<String, JsonElement> entry : tags.entrySet()) {
            JsonArray array = entry.getValue().getAsJsonArray();
            int[] entries = new int[array.size()];
            int i = 0;
            for (JsonElement element : array) {
                String stringId = element.getAsString();
                if (!typeMapping.containsKey(stringId) && !typeMapping.containsKey(stringId = Key.stripMinecraftNamespace(stringId))) { // aaa
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
        return checkValidity(id, itemMappings.getNewId(id), "item");
    }

    @Override
    public int getOldItemId(int id) {
        return itemMappings.inverse().getNewIdOrDefault(id, 1);
    }

    @Override
    public int getNewParticleId(int id) {
        return checkValidity(id, particleMappings.mappings().getNewId(id), "particles");
    }

    @Override
    public @Nullable List<TagData> getTags(RegistryType type) {
        return tags != null ? tags.get(type) : null;
    }

    @Override
    public @Nullable BiMappings getItemMappings() {
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
    public @Nullable Mappings getBlockEntityMappings() {
        return blockEntityMappings;
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

    @Override
    public @Nullable Mappings getEnchantmentMappings() {
        return enchantmentMappings;
    }

    @Override
    public @Nullable FullMappings getEntityMappings() {
        return entityMappings;
    }

    @Override
    public @Nullable FullMappings getArgumentTypeMappings() {
        return argumentTypeMappings;
    }

    @Override
    public @Nullable Mappings getPaintingMappings() {
        return paintingMappings;
    }

    protected @Nullable Mappings loadFromArray(JsonObject unmappedIdentifiers, JsonObject mappedIdentifiers, @Nullable JsonObject diffMappings, String key) {
        if (!unmappedIdentifiers.has(key) || !mappedIdentifiers.has(key) || !shouldLoad(key)) {
            return null;
        }

        JsonObject diff = diffMappings != null ? diffMappings.getAsJsonObject(key) : null;
        return IntArrayMappings.builder().unmapped(unmappedIdentifiers.getAsJsonArray(key))
                .mapped(mappedIdentifiers.getAsJsonArray(key)).diffMappings(diff).build();
    }

    protected @Nullable Mappings loadFromObject(JsonObject unmappedIdentifiers, JsonObject mappedIdentifiers, @Nullable JsonObject diffMappings, String key) {
        if (!unmappedIdentifiers.has(key) || !mappedIdentifiers.has(key) || !shouldLoad(key)) {
            return null;
        }

        JsonObject diff = diffMappings != null ? diffMappings.getAsJsonObject(key) : null;
        return IntArrayMappings.builder().unmapped(unmappedIdentifiers.getAsJsonObject(key))
                .mapped(mappedIdentifiers.getAsJsonObject(key)).diffMappings(diff).build();
    }

    protected @Nullable BiMappings loadBiFromArray(JsonObject unmappedIdentifiers, JsonObject mappedIdentifiers, @Nullable JsonObject diffMappings, String key) {
        if (!unmappedIdentifiers.has(key) || !mappedIdentifiers.has(key) || !shouldLoad(key)) {
            return null;
        }

        JsonObject diff = diffMappings != null ? diffMappings.getAsJsonObject(key) : null;
        return IntArrayBiMappings.builder().unmapped(unmappedIdentifiers.getAsJsonArray(key)).mapped(mappedIdentifiers.getAsJsonArray(key))
                .diffMappings(diff).build();
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
     * To be overridden if needed.
     *
     * @param unmappedIdentifiers old mappings
     * @param mappedIdentifiers   new mappings
     * @param diffMappings        diff mappings if present
     */
    protected void loadExtras(JsonObject unmappedIdentifiers, JsonObject mappedIdentifiers, @Nullable JsonObject diffMappings) {
    }

    protected boolean shouldLoad(String key) {
        return true;
    }
}
