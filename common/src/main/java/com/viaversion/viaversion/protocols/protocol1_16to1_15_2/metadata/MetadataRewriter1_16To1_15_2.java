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
package com.viaversion.viaversion.protocols.protocol1_16to1_15_2.metadata;

import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_15;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_16;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.type.types.version.Types1_16;
import com.viaversion.viaversion.protocols.protocol1_15to1_14_4.ClientboundPackets1_15;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.Protocol1_16To1_15_2;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public class MetadataRewriter1_16To1_15_2 extends EntityRewriter<ClientboundPackets1_15, Protocol1_16To1_15_2> {

    public MetadataRewriter1_16To1_15_2(Protocol1_16To1_15_2 protocol) {
        super(protocol);
        mapEntityType(EntityTypes1_15.ZOMBIE_PIGMAN, EntityTypes1_16.ZOMBIFIED_PIGLIN);
        mapTypes(EntityTypes1_15.values(), EntityTypes1_16.class);
    }

    @Override
    protected void registerRewrites() {
        filter().mapMetaType(Types1_16.META_TYPES::byId);
        registerMetaTypeHandler(Types1_16.META_TYPES.itemType, Types1_16.META_TYPES.blockStateType, Types1_16.META_TYPES.particleType);
        filter().type(EntityTypes1_16.MINECART_ABSTRACT).index(10).handler((metadatas, meta) -> {
            int data = meta.value();
            meta.setValue(protocol.getMappingData().getNewBlockStateId(data));
        });
        filter().type(EntityTypes1_16.ABSTRACT_ARROW).removeIndex(8);
        filter().type(EntityTypes1_16.WOLF).index(16).handler((event, meta) -> {
            byte mask = meta.value();
            int angerTime = (mask & 0x02) != 0 ? Integer.MAX_VALUE : 0;
            event.createExtraMeta(new Metadata(20, Types1_16.META_TYPES.varIntType, angerTime));
        });
    }

    @Override
    public EntityType typeFromId(int type) {
        return EntityTypes1_16.getTypeFromId(type);
    }
}
