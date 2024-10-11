/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_21to1_21_2.data;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.data.MappingDataLoader;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import java.util.ArrayList;
import java.util.List;

public final class MappingData1_21_2 extends MappingDataBase {

    private final List<RecipeInputs> recipeInputs = new ArrayList<>();

    public MappingData1_21_2() {
        super("1.21", "1.21.2");
    }

    @Override
    protected void loadExtras(final CompoundTag data) {
        final CompoundTag extraMappings = MappingDataLoader.INSTANCE.loadNBT("recipe-inputs-1.21.2.nbt");
        addRecipeInputs(extraMappings, "smithing_addition");
        addRecipeInputs(extraMappings, "smithing_template");
        addRecipeInputs(extraMappings, "smithing_base");
        addRecipeInputs(extraMappings, "furnace_input");
        addRecipeInputs(extraMappings, "smoker_input");
        addRecipeInputs(extraMappings, "blast_furnace_input");
        addRecipeInputs(extraMappings, "campfire_input");
    }

    private void addRecipeInputs(final CompoundTag tag, final String key) {
        final int[] ids = tag.getIntArrayTag(key).getValue();
        recipeInputs.add(new RecipeInputs(key, ids));
    }

    public void writeInputs(final PacketWrapper wrapper) {
        wrapper.write(Types.VAR_INT, recipeInputs.size());
        for (final RecipeInputs inputs : recipeInputs) {
            wrapper.write(Types.STRING, inputs.key);
            wrapper.write(Types.VAR_INT_ARRAY_PRIMITIVE, inputs.ids.clone());
        }
    }

    public record RecipeInputs(String key, int[] ids) {
    }
}
