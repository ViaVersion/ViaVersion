/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_17to1_16_4.metadata;

import com.viaversion.viaversion.api.minecraft.entities.Entity1_16_2Types;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_17Types;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_17;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.Protocol1_17To1_16_4;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.packets.InventoryPackets;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public class MetadataRewriter1_17To1_16_4 extends EntityRewriter {

    public MetadataRewriter1_17To1_16_4(Protocol1_17To1_16_4 protocol) {
        super(protocol);
        mapTypes(Entity1_16_2Types.values(), Entity1_17Types.class);
    }

    @Override
    protected void registerRewrites() {
        filter().handler((event, meta) -> {
            meta.setMetaType(MetaType1_17.byId(meta.metaType().typeId()));

            if (meta.metaType() == MetaType1_17.Pose) {
                int pose = meta.value();
                if (pose > 5) {
                    // Added LONG_JUMP at 6
                    meta.setValue(pose + 1);
                }
            }
        });
        registerDumMetaTypeHandler(MetaType1_17.Slot, MetaType1_17.BlockID, MetaType1_17.PARTICLE, InventoryPackets::toClient);

        // Ticks frozen added with id 7
        filter().filterFamily(Entity1_17Types.ENTITY).addIndex(7);

        filter().filterFamily(Entity1_17Types.MINECART_ABSTRACT).index(11).handler((event, meta) -> {
            // Convert to new block id
            int data = (int) meta.getValue();
            meta.setValue(protocol.getMappingData().getNewBlockStateId(data));
        });

        // Attachment position removed
        filter().type(Entity1_17Types.SHULKER).removeIndex(16);
    }

    @Override
    protected EntityType typeFromId(int type) {
        return Entity1_17Types.getTypeFromId(type);
    }
}
