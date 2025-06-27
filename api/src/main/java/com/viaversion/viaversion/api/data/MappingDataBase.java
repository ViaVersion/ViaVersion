/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.TagData;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MappingDataBase implements MappingData {

    protected final String unmappedVersion;
    protected final String mappedVersion;
    protected FullMappings argumentTypeMappings;
    protected FullMappings entityMappings;
    protected FullMappings recipeSerializerMappings;
    protected FullMappings itemDataSerializerMappings;
    protected FullMappings attributeMappings;
    protected ParticleMappings particleMappings;
    protected BiMappings itemMappings;
    protected BiMappings blockMappings;
    protected Mappings blockStateMappings;
    protected Mappings blockEntityMappings;
    protected Mappings soundMappings;
    protected Mappings statisticsMappings;
    protected Mappings enchantmentMappings;
    protected Mappings paintingMappings;
    protected Mappings menuMappings;
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

        final CompoundTag data = readMappingsFile("mappings-" + unmappedVersion + "to" + mappedVersion + ".nbt");
        blockMappings = loadBiMappings(data, "blocks");
        blockStateMappings = loadMappings(data, "blockstates");
        blockEntityMappings = loadMappings(data, "blockentities");
        soundMappings = loadMappings(data, "sounds");
        statisticsMappings = loadMappings(data, "statistics");
        menuMappings = loadMappings(data, "menus");
        enchantmentMappings = loadMappings(data, "enchantments");
        paintingMappings = loadMappings(data, "paintings");

        final CompoundTag unmappedIdentifierData = readUnmappedIdentifiersFile("identifiers-" + unmappedVersion + ".nbt");
        final CompoundTag mappedIdentifierData = readMappedIdentifiersFile("identifiers-" + mappedVersion + ".nbt");
        if (unmappedIdentifierData != null && mappedIdentifierData != null) {
            itemMappings = loadFullMappings(data, unmappedIdentifierData, mappedIdentifierData, "items");
            entityMappings = loadFullMappings(data, unmappedIdentifierData, mappedIdentifierData, "entities");
            argumentTypeMappings = loadFullMappings(data, unmappedIdentifierData, mappedIdentifierData, "argumenttypes");
            recipeSerializerMappings = loadFullMappings(data, unmappedIdentifierData, mappedIdentifierData, "recipe_serializers");
            itemDataSerializerMappings = loadFullMappings(data, unmappedIdentifierData, mappedIdentifierData, "data_component_type");
            attributeMappings = loadFullMappings(data, unmappedIdentifierData, mappedIdentifierData, "attributes");

            final List<String> unmappedParticles = identifiersFromGlobalIds(unmappedIdentifierData, "particles");
            final List<String> mappedParticles = identifiersFromGlobalIds(mappedIdentifierData, "particles");
            if (unmappedParticles != null && mappedParticles != null) {
                Mappings particleMappings = loadMappings(data, "particles");
                if (particleMappings == null) {
                    particleMappings = new IdentityMappings(unmappedParticles.size(), mappedParticles.size());
                }
                this.particleMappings = new ParticleMappings(unmappedParticles, mappedParticles, particleMappings);
            }
        } else {
            // Might not have identifiers in older versions
            itemMappings = loadBiMappings(data, "items");
        }

        final CompoundTag tagsTag = data.getCompoundTag("tags");
        if (tagsTag != null) {
            this.tags = new EnumMap<>(RegistryType.class);
            loadTags(RegistryType.ITEM, tagsTag);
            loadTags(RegistryType.BLOCK, tagsTag);
            loadTags(RegistryType.ENTITY, tagsTag);
        }

        loadExtras(data);
    }

    protected @Nullable List<String> identifiersFromGlobalIds(final CompoundTag mappingsTag, final String key) {
        return MappingDataLoader.INSTANCE.identifiersFromGlobalIds(mappingsTag, key);
    }

    protected @Nullable CompoundTag readMappingsFile(final String name) {
        return MappingDataLoader.INSTANCE.loadNBT(name);
    }

    protected @Nullable CompoundTag readUnmappedIdentifiersFile(final String name) {
        return MappingDataLoader.INSTANCE.loadNBT(name, true);
    }

    protected @Nullable CompoundTag readMappedIdentifiersFile(final String name) {
        return MappingDataLoader.INSTANCE.loadNBT(name, true);
    }

    protected @Nullable Mappings loadMappings(final CompoundTag data, final String key) {
        return MappingDataLoader.INSTANCE.loadMappings(data, key);
    }

    protected @Nullable FullMappings loadFullMappings(final CompoundTag data, final CompoundTag unmappedIdentifiersTag, final CompoundTag mappedIdentifiersTag, final String key) {
        if (!unmappedIdentifiersTag.contains(key) || !mappedIdentifiersTag.contains(key)) {
            return null;
        }

        final List<String> unmappedIdentifiers = identifiersFromGlobalIds(unmappedIdentifiersTag, key);
        final List<String> mappedIdentifiers = identifiersFromGlobalIds(mappedIdentifiersTag, key);
        Mappings mappings = loadBiMappings(data, key); // Load as bi-mappings to keep the inverse cached
        if (mappings == null) {
            mappings = new IdentityMappings(unmappedIdentifiers.size(), mappedIdentifiers.size());
        }

        return new FullMappingsBase(unmappedIdentifiers, mappedIdentifiers, mappings);
    }

    protected @Nullable BiMappings loadBiMappings(final CompoundTag data, final String key) {
        final Mappings mappings = loadMappings(data, key);
        return mappings != null ? BiMappings.of(mappings) : null;
    }

    private void loadTags(final RegistryType type, final CompoundTag data) {
        final CompoundTag tag = data.getCompoundTag(type.resourceLocation());
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
    public int getOldBlockId(final int id) {
        return blockMappings.inverse().getNewIdOrDefault(id, 1);
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
    public int getNewAttributeId(final int id) {
        return checkValidity(id, attributeMappings.getNewId(id), "attributes");
    }

    @Override
    public int getNewSoundId(final int id) {
        return checkValidity(id, soundMappings.getNewId(id), "sound");
    }

    @Override
    public int getOldSoundId(final int id) {
        return soundMappings.inverse().getNewIdOrDefault(id, 0);
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
    public @Nullable FullMappings getFullItemMappings() {
        if (itemMappings instanceof FullMappings) {
            return (FullMappings) itemMappings;
        }
        return null;
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
    public @Nullable Mappings getMenuMappings() {
        return menuMappings;
    }

    @Override
    public @Nullable Mappings getEnchantmentMappings() {
        return enchantmentMappings;
    }

    @Override
    public @Nullable FullMappings getAttributeMappings() {
        return attributeMappings;
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
    public @Nullable FullMappings getDataComponentSerializerMappings() {
        return itemDataSerializerMappings;
    }

    @Override
    public @Nullable Mappings getPaintingMappings() {
        return paintingMappings;
    }

    @Override
    public @Nullable FullMappings getRecipeSerializerMappings() {
        return recipeSerializerMappings;
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
            if (!Via.getConfig().isSuppressConversionWarnings()) {
                getLogger().warning(String.format("Missing %s %s for %s %s %d", mappedVersion, type, unmappedVersion, type, id));
            }
            return 0;
        }
        return mappedId;
    }

    protected void loadExtras(final CompoundTag data) {
    }
}
