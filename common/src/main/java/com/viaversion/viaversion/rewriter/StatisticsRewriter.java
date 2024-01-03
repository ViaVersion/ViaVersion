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
package com.viaversion.viaversion.rewriter;

import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.type.Type;
import org.checkerframework.checker.nullness.qual.Nullable;

public class StatisticsRewriter<C extends ClientboundPacketType> {
    private static final int CUSTOM_STATS_CATEGORY = 8; // Make this changeable if it differs in a future version
    private final Protocol<C, ?, ?, ?> protocol;

    public StatisticsRewriter(Protocol<C, ?, ?, ?> protocol) {
        this.protocol = protocol;
    }

    public void register(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            int size = wrapper.passthrough(Type.VAR_INT);
            int newSize = size;
            for (int i = 0; i < size; i++) {
                int categoryId = wrapper.read(Type.VAR_INT);
                int statisticId = wrapper.read(Type.VAR_INT);
                int value = wrapper.read(Type.VAR_INT);
                if (categoryId == CUSTOM_STATS_CATEGORY && protocol.getMappingData().getStatisticsMappings() != null) {
                    // Rewrite custom statistics id
                    statisticId = protocol.getMappingData().getStatisticsMappings().getNewId(statisticId);
                    if (statisticId == -1) {
                        // Remove entry
                        newSize--;
                        continue;
                    }
                } else {
                    // Rewrite the block/item/entity id
                    RegistryType type = getRegistryTypeForStatistic(categoryId);
                    IdRewriteFunction statisticsRewriter;
                    if (type != null && (statisticsRewriter = getRewriter(type)) != null) {
                        statisticId = statisticsRewriter.rewrite(statisticId);
                    }
                }

                wrapper.write(Type.VAR_INT, categoryId);
                wrapper.write(Type.VAR_INT, statisticId);
                wrapper.write(Type.VAR_INT, value);
            }

            if (newSize != size) {
                wrapper.set(Type.VAR_INT, 0, newSize);
            }
        });
    }

    protected @Nullable IdRewriteFunction getRewriter(RegistryType type) {
        switch (type) {
            case BLOCK:
                return protocol.getMappingData().getBlockMappings() != null ? id -> protocol.getMappingData().getNewBlockId(id) : null;
            case ITEM:
                return protocol.getMappingData().getItemMappings() != null ? id -> protocol.getMappingData().getNewItemId(id) : null;
            case ENTITY:
                return protocol.getEntityRewriter() != null ? id -> protocol.getEntityRewriter().newEntityId(id) : null;
        }
        throw new IllegalArgumentException("Unknown registry type in statistics packet: " + type);
    }

    public @Nullable RegistryType getRegistryTypeForStatistic(int statisticsId) {
        switch (statisticsId) {
            case 0:
                return RegistryType.BLOCK;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return RegistryType.ITEM;
            case 6:
            case 7:
                return RegistryType.ENTITY;
            default:
                return null;
        }
    }
}
