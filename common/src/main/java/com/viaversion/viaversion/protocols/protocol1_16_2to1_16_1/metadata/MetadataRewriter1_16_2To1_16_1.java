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
package com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.metadata;

import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_16;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_16_2;
import com.viaversion.viaversion.api.type.types.version.Types1_16;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.Protocol1_16_2To1_16_1;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.ClientboundPackets1_16;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public class MetadataRewriter1_16_2To1_16_1 extends EntityRewriter<ClientboundPackets1_16, Protocol1_16_2To1_16_1> {

    public MetadataRewriter1_16_2To1_16_1(Protocol1_16_2To1_16_1 protocol) {
        super(protocol);
        mapTypes(EntityTypes1_16.values(), EntityTypes1_16_2.class);
    }

    @Override
    protected void registerRewrites() {
        registerMetaTypeHandler(Types1_16.META_TYPES.itemType, Types1_16.META_TYPES.blockStateType, Types1_16.META_TYPES.particleType);
        filter().type(EntityTypes1_16_2.MINECART_ABSTRACT).index(10).handler((metadatas, meta) -> {
            int data = meta.value();
            meta.setValue(protocol.getMappingData().getNewBlockStateId(data));
        });
        filter().type(EntityTypes1_16_2.ABSTRACT_PIGLIN).handler((metadatas, meta) -> {
            if (meta.id() == 15) {
                meta.setId(16);
            } else if (meta.id() == 16) {
                meta.setId(15);
            }
        });
    }

    @Override
    public EntityType typeFromId(int type) {
        return EntityTypes1_16_2.getTypeFromId(type);
    }
}
