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

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntArrayTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.TagData;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MappingDataBase implements MappingData {

    protected final String unmappedVersion;
    protected final String mappedVersion;
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

    public MappingDataBase(final String unmappedVersion, final String mappedVersion) {
        this.unmappedVersion = unmappedVersion;
        this.mappedVersion = mappedVersion;
    }

    @Override
    public void load() {
        if (Via.getManager().isDebug()) {
            getLogger().info("Loading " + unmappedVersion + " -> " + mappedVersion + " mappings...");
        }

        final CompoundTag data = readNBTFile("mappings-" + unmappedVersion + "to" + mappedVersion + ".nbt");
        blockMappings = loadMappings(data, "blocks");
        blockStateMappings = loadMappings(data, "blockstates");
        blockEntityMappings = loadMappings(data, "blockentities");
        soundMappings = loadMappings(data, "sounds");
        statisticsMappings = loadMappings(data, "statistics");
        enchantmentMappings = loadMappings(data, "enchantments");
        paintingMappings = loadMappings(data, "paintings");
        itemMappings = loadBiMappings(data, "items");

        final CompoundTag unmappedIdentifierData = MappingDataLoader.loadNBT("identifiers-" + unmappedVersion + ".nbt", true);
        final CompoundTag mappedIdentifierData = MappingDataLoader.loadNBT("identifiers-" + mappedVersion + ".nbt", true);
        if (unmappedIdentifierData != null && mappedIdentifierData != null) {
            entityMappings = loadFullMappings(data, unmappedIdentifierData, mappedIdentifierData, "entities");
            argumentTypeMappings = loadFullMappings(data, unmappedIdentifierData, mappedIdentifierData, "argumenttypes");

            final ListTag unmappedParticles = unmappedIdentifierData.get("particles");
            final ListTag mappedParticles = mappedIdentifierData.get("particles");
            if (unmappedParticles != null && mappedParticles != null) {
                Mappings particleMappings = loadMappings(data, "particles");
                if (particleMappings == null) {
                    particleMappings = new IdentityMappings(unmappedParticles.size(), mappedParticles.size());
                }

                final List<String> identifiers = unmappedParticles.getValue().stream().map(t -> (String) t.getValue()).collect(Collectors.toList());
                final List<String> mappedIdentifiers = mappedParticles.getValue().stream().map(t -> (String) t.getValue()).collect(Collectors.toList());
                this.particleMappings = new ParticleMappings(identifiers, mappedIdentifiers, particleMappings);
            }
        }

        final CompoundTag tagsTag = data.get("tags");
        if (tagsTag != null) {
            this.tags = new EnumMap<>(RegistryType.class);
            loadTags(RegistryType.ITEM, tagsTag);
            loadTags(RegistryType.BLOCK, tagsTag);
        }

        loadExtras(data);
    }

    protected @Nullable CompoundTag readNBTFile(final String name) {
        return MappingDataLoader.loadNBT(name);
    }

    protected @Nullable Mappings loadMappings(final CompoundTag data, final String key) {
        return MappingDataLoader.loadMappings(data, key);
    }

    protected @Nullable FullMappings loadFullMappings(final CompoundTag data, final CompoundTag unmappedIdentifiers, final CompoundTag mappedIdentifiers, final String key) {
        return MappingDataLoader.loadFullMappings(data, unmappedIdentifiers, mappedIdentifiers, key);
    }

    protected @Nullable BiMappings loadBiMappings(final CompoundTag data, final String key) {
        final Mappings mappings = loadMappings(data, key);
        return mappings != null ? BiMappings.of(mappings) : null;
    }

    private void loadTags(final RegistryType type, final CompoundTag data) {
        final CompoundTag tag = data.get(type.resourceLocation());
        if (tag == null) {
            return;
        }

        final List<TagData> tagsList = new ArrayList<>(tags.size());
        for (final Map.Entry<String, Tag> entry : tag.entrySet()) {
            final IntArrayTag entries = (IntArrayTag) entry.getValue();
            tagsList.add(new TagData(entry.getKey(), entries.getValue()));
        }

        this.tags.put(type, tagsList);
    }

    @Override
    public int getNewBlockStateId(final int id) {
        return checkValidity(id, blockStateMappings.getNewId(id), "blockstate");
    }

    @Override
    public int getNewBlockId(final int id) {
        return checkValidity(id, blockMappings.getNewId(id), "block");
    }

    @Override
    public int getNewItemId(final int id) {
        return checkValidity(id, itemMappings.getNewId(id), "item");
    }

    @Override
    public int getOldItemId(final int id) {
        return itemMappings.inverse().getNewIdOrDefault(id, 1);
    }

    @Override
    public int getNewParticleId(final int id) {
        return checkValidity(id, particleMappings.getNewId(id), "particles");
    }

    @Override
    public @Nullable List<TagData> getTags(final RegistryType type) {
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
    protected int checkValidity(final int id, final int mappedId, final String type) {
        if (mappedId == -1) {
            getLogger().warning(String.format("Missing %s %s for %s %s %d", mappedVersion, type, unmappedVersion, type, id));
            return 0;
        }
        return mappedId;
    }

    protected void loadExtras(final CompoundTag data) {
    }
}
