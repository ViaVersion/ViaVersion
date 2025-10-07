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

import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.TagData;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface MappingData {

    /**
     * Loads the mapping data.
     */
    void load();

    /**
     * Returns the mapped block state id, or 0 if unmapped.
     *
     * @param id unmapped block state id
     * @return mapped block state id, or 0 if unmapped
     * @throws NullPointerException if this mappingdata does not hold block state mappings
     */
    int getNewBlockStateId(int id);

    /**
     * Returns the mapped block id, or 0 if unmapped.
     *
     * @param id unmapped block id
     * @return mapped block id, or 0 if unmapped
     * @throws NullPointerException if this mappingdata does not hold block mappings
     */
    int getNewBlockId(int id);

    /**
     * Returns the backwards mapped block id, or 1 if unmapped.
     *
     * @param id mapped block id
     * @return backwards mapped block id, or 1 if unmapped
     */
    int getOldBlockId(int id);

    /**
     * Returns the mapped item id, or 0 if unmapped.
     *
     * @param id unmapped item id
     * @return mapped item id, or 0 if unmapped
     * @throws NullPointerException if this mappingdata does not hold item mappings
     */
    int getNewItemId(int id);

    /**
     * Returns the backwards mapped item id, or 1 if unmapped.
     *
     * @param id mapped item id
     * @return backwards mapped item id, or 1 if unmapped
     * @throws NullPointerException if this mappingdata does not hold item mappings
     */
    int getOldItemId(int id);

    /**
     * Returns the mapped particle id, or 0 if unmapped.
     *
     * @param id unmapped particle id
     * @return mapped particle id, or 0 if unmapped
     * @throws NullPointerException if this mappingdata does not hold particle mappings
     */
    int getNewParticleId(int id);

    /**
     * Returns the mapped attribute id, or 0 if unmapped.
     *
     * @param id unmapped attribute id
     * @return mapped attribute id, or 0 if unmapped
     * @throws NullPointerException if this mappingdata does not hold attribute mappings
     */
    int getNewAttributeId(int id);

    /**
     * Returns the mapped sound id, or 0 if unmapped.
     *
     * @param id unmapped sound id
     * @return mapped sound id, or 0 if unmapped
     * @throws NullPointerException if this mappingdata does not hold sound mappings
     */
    int getNewSoundId(int id);

    /**
     * Returns the backwards mapped sound id, or 1 if unmapped.
     *
     * @param i mapped sound id
     * @return backwards mapped sound id, or 1 if unmapped
     */
    int getOldSoundId(int i);

    /**
     * Returns a list of tags to send if present.
     *
     * @param type registry tag type
     * @return list of tags to send if present, else null
     */
    @Nullable List<TagData> getTags(RegistryType type);

    /**
     * Returns item mappings.
     *
     * @return item mappings
     */
    @Nullable BiMappings getItemMappings();

    /**
     * Returns item mappings if they also have identifier data present.
     *
     * @return item mappings if they also have identifier data present
     * @see #getItemMappings()
     */
    @Nullable FullMappings getFullItemMappings();

    @Nullable ParticleMappings getParticleMappings();

    @Nullable Mappings getBlockMappings();

    @Nullable FullMappings getFullBlockMappings();

    @Nullable Mappings getBlockStateMappings();

    @Nullable Mappings getSoundMappings();

    @Nullable FullMappings getFullSoundMappings();

    @Nullable Mappings getStatisticsMappings();

    @Nullable Mappings getMenuMappings();

    @Nullable Mappings getEnchantmentMappings();

    @Nullable Mappings getPaintingMappings();

    @Nullable FullMappings getBlockEntityMappings();

    @Nullable FullMappings getAttributeMappings();

    @Nullable FullMappings getEntityMappings();

    @Nullable FullMappings getArgumentTypeMappings();

    @Nullable FullMappings getRecipeSerializerMappings();

    @Nullable FullMappings getDataComponentSerializerMappings();

    default @Nullable FullMappings getFullMappings(final MappingType mappingType) {
        return switch (mappingType) {
            case ITEM -> getFullItemMappings();
            case SOUND -> getFullSoundMappings();
            case ENTITY -> getEntityMappings();
        };
    }

    /**
     * Set of block (not block state) ids that had their base type or properties changed.
     *
     * @return set of changed block ids, or null if not tracked/none are present
     */
    @Nullable IntSet changedBlocks();

    /**
     * Type of mappings. Currently only relevant for ops writing of generic holder classes and expanded when needed.
     */
    enum MappingType {
        ITEM, SOUND, ENTITY
    }
}
