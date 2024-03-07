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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.providers.blockentities;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.NumberTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.providers.BlockEntityProvider;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.storage.BlockStorage;
import com.viaversion.viaversion.util.ComponentUtil;

public class BannerHandler implements BlockEntityProvider.BlockEntityHandler {
    private static final int WALL_BANNER_START = 7110; // 4 each
    private static final int WALL_BANNER_STOP = 7173;

    private static final int BANNER_START = 6854; // 16 each
    private static final int BANNER_STOP = 7109;

    @Override
    public int transform(UserConnection user, CompoundTag tag) {
        BlockStorage storage = user.get(BlockStorage.class);
        Position position = new Position(tag.getNumberTag("x").asInt(), tag.getNumberTag("y").asShort(), tag.getNumberTag("z").asInt());

        if (!storage.contains(position)) {
            Via.getPlatform().getLogger().warning("Received an banner color update packet, but there is no banner! O_o " + tag);
            return -1;
        }

        int blockId = storage.get(position).getOriginal();

        NumberTag base = tag.getNumberTag("Base");
        int color = base != null ? base.asInt() : 0;
        // Standing banner
        if (blockId >= BANNER_START && blockId <= BANNER_STOP) {
            blockId += ((15 - color) * 16);
            // Wall banner
        } else if (blockId >= WALL_BANNER_START && blockId <= WALL_BANNER_STOP) {
            blockId += ((15 - color) * 4);
        } else {
            Via.getPlatform().getLogger().warning("Why does this block have the banner block entity? :(" + tag);
        }

        ListTag<CompoundTag> patterns = tag.getListTag("Patterns", CompoundTag.class);
        if (patterns != null) {
            for (CompoundTag pattern : patterns) {
                NumberTag colorTag = pattern.getNumberTag("Color");
                if (colorTag != null) {
                    pattern.putInt("Color", 15 - colorTag.asInt()); // Invert color id
                }
            }
        }

        StringTag name = tag.getStringTag("CustomName");
        if (name != null) {
            name.setValue(ComponentUtil.legacyToJsonString(name.getValue()));
        }

        return blockId;
    }
}