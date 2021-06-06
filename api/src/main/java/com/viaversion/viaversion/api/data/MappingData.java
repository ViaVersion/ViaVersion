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

import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.TagData;
import com.viaversion.viaversion.util.Int2IntBiMap;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

public interface MappingData {

    /**
     * Loads the mapping data.
     */
    void load();

    /**
     * Returns the mapped block state id, or -1 if unmapped.
     *
     * @param id unmapped block state id
     * @return mapped block state id, or -1 if unmapped
     * @throws NullPointerException if this mappingdata does not hold block state mappings
     */
    int getNewBlockStateId(int id);

    /**
     * Returns the mapped block id, or -1 if unmapped.
     *
     * @param id unmapped block id
     * @return mapped block id, or -1 if unmapped
     * @throws NullPointerException if this mappingdata does not hold block mappings
     */
    int getNewBlockId(int id);

    /**
     * Returns the mapped item id, or -1 if unmapped.
     *
     * @param id unmapped item id
     * @return mapped item id, or -1 if unmapped
     * @throws NullPointerException if this mappingdata does not hold item mappings
     */
    int getNewItemId(int id);

    /**
     * Returns the backwards mapped item id, or -1 if unmapped.
     *
     * @param id mapped item id
     * @return backwards mapped item id, or -1 if unmapped
     * @throws NullPointerException if this mappingdata does not hold item mappings
     */
    int getOldItemId(int id);

    /**
     * Returns the mapped particle id, or -1 if unmapped.
     *
     * @param id unmapped particle id
     * @return mapped particle id, or -1 if unmapped
     * @throws NullPointerException if this mappingdata does not hold particle mappings
     */
    int getNewParticleId(int id);

    /**
     * Returns a list of tags to send if present.
     *
     * @param type registry tag type
     * @return list of tags to send if present, else null
     */
    @Nullable List<TagData> getTags(RegistryType type);

    @Nullable Int2IntBiMap getItemMappings();

    @Nullable ParticleMappings getParticleMappings();

    @Nullable Mappings getBlockMappings();

    @Nullable Mappings getBlockStateMappings();

    @Nullable Mappings getSoundMappings();

    @Nullable Mappings getStatisticsMappings();
}
