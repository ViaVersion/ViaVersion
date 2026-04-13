/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.protocols.v1_21_11to26_1.data;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.data.MappingDataLoader;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public final class MappingData26_1 extends MappingDataBase {

    private IntSet fluidBlockStates;
    private CompoundTag catSoundVariants;
    private CompoundTag cowSoundVariants;
    private CompoundTag pigSoundVariants;
    private CompoundTag chickenSoundVariants;

    public MappingData26_1() {
        super("1.21.11", "26.1");
    }

    @Override
    protected void loadExtras(final CompoundTag data) {
        final CompoundTag tag = MappingDataLoader.INSTANCE.loadNBTFromFile("sound-variant-registries-26.1.nbt");
        catSoundVariants = tag.getCompoundTag("cat_sound_variant");
        cowSoundVariants = tag.getCompoundTag("cow_sound_variant");
        pigSoundVariants = tag.getCompoundTag("pig_sound_variant");
        chickenSoundVariants = tag.getCompoundTag("chicken_sound_variant");

        fluidBlockStates = new IntOpenHashSet(MappingDataLoader.INSTANCE.loadNBTFromFile("fluids-26.1.nbt").getIntArrayTag("fluids").getValue());
    }

    public CompoundTag catSoundVariants() {
        return catSoundVariants;
    }

    public CompoundTag cowSoundVariants() {
        return cowSoundVariants;
    }

    public CompoundTag pigSoundVariants() {
        return pigSoundVariants;
    }

    public CompoundTag chickenSoundVariants() {
        return chickenSoundVariants;
    }

    public IntSet fluidBlockStates() {
        return fluidBlockStates;
    }
}
