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
package com.viaversion.viaversion.protocols.v26_2to26_3.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.protocols.v26_2to26_3.Protocol26_2To26_3;
import com.viaversion.viaversion.rewriter.text.NBTComponentRewriter;

public final class ComponentRewriter26_3 extends NBTComponentRewriter<ClientboundPacket26_1> {

    public ComponentRewriter26_3(final Protocol26_2To26_3 protocol) {
        super(protocol);
    }

    @Override
    protected void handleTranslate(final UserConnection connection, final CompoundTag parentTag, final StringTag translateTag) {
        switch (translateTag.getValue()) {
            case "filled_map.buried_treasure" -> translateTag.setValue("item.minecraft.buried_treasure_map");
            case "filled_map.monument" -> translateTag.setValue("item.minecraft.ocean_monument_map");
            case "filled_map.mansion" -> translateTag.setValue("item.minecraft.woodland_mansion_map");
            case "filled_map.trial_chambers" -> translateTag.setValue("item.minecraft.buried_trial_chambers_map");
            case "filled_map.village_desert" -> translateTag.setValue("item.minecraft.desert_village_map");
            case "filled_map.village_plains" -> translateTag.setValue("item.minecraft.plains_village_map");
            case "filled_map.village_savanna" -> translateTag.setValue("item.minecraft.savanna_village_map");
            case "filled_map.village_snowy" -> translateTag.setValue("item.minecraft.snowy_village_map");
            case "filled_map.village_taiga" -> translateTag.setValue("item.minecraft.taiga_village_map");
            case "filled_map.explorer_swamp" -> translateTag.setValue("item.minecraft.swamp_hut_map");
            case "filled_map.explorer_jungle" -> translateTag.setValue("item.minecraft.jungle_pyramid_map");
        }
    }

    @Override
    protected void handleShowItem(final UserConnection connection, final CompoundTag itemTag, final CompoundTag componentsTag) {
        super.handleShowItem(connection, itemTag, componentsTag);
        if (componentsTag == null) {
            return;
        }

        removeDataComponents(componentsTag, StructuredDataKey.PROVIDES_TRIM_MATERIAL26_1, StructuredDataKey.POT_DECORATIONS1_20_5,
            StructuredDataKey.MAP_COLOR, StructuredDataKey.TRIM1_21_5, StructuredDataKey.SWING_ANIMATION);
    }
}
