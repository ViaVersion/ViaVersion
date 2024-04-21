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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.metadata;

import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_13;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.type.types.version.Types1_13;
import com.viaversion.viaversion.protocols.protocol1_12_1to1_12.ClientboundPackets1_12_1;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data.EntityTypeRewriter;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data.ParticleRewriter;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.packets.WorldPackets;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.util.ComponentUtil;

public class MetadataRewriter1_13To1_12_2 extends EntityRewriter<ClientboundPackets1_12_1, Protocol1_13To1_12_2> {

    public MetadataRewriter1_13To1_12_2(Protocol1_13To1_12_2 protocol) {
        super(protocol);
    }

    @Override
    protected void registerRewrites() {
        filter().mapMetaType(typeId -> Types1_13.META_TYPES.byId(typeId > 4 ? typeId + 1 : typeId));
        filter().metaType(Types1_13.META_TYPES.itemType).handler(((event, meta) -> protocol.getItemRewriter().handleItemToClient(event.user(), meta.value())));
        filter().metaType(Types1_13.META_TYPES.blockStateType).handler(((event, meta) -> {
            final int oldId = meta.value();
            if (oldId != 0) {
                final int combined = (((oldId & 4095) << 4) | (oldId >> 12 & 15));
                final int newId = WorldPackets.toNewId(combined);
                meta.setValue(newId);
            }
        }));

        // Previously unused, now swimming
        filter().index(0).handler((event, meta) -> meta.setValue((byte) ((byte) meta.getValue() & ~0x10)));

        filter().index(2).handler(((event, meta) -> {
            if (meta.getValue() != null && !((String) meta.getValue()).isEmpty()) {
                meta.setTypeAndValue(Types1_13.META_TYPES.optionalComponentType, ComponentUtil.legacyToJson((String) meta.getValue()));
            } else {
                meta.setTypeAndValue(Types1_13.META_TYPES.optionalComponentType, null);
            }
        }));

        filter().type(EntityTypes1_13.EntityType.WOLF).index(17).handler((event, meta) -> {
            // Handle new colors
            meta.setValue(15 - (int) meta.getValue());
        });

        filter().type(EntityTypes1_13.EntityType.ZOMBIE).addIndex(15); // Shaking

        filter().type(EntityTypes1_13.EntityType.MINECART_ABSTRACT).index(9).handler((event, meta) -> {
            final int oldId = meta.value();
            final int combined = (((oldId & 4095) << 4) | (oldId >> 12 & 15));
            final int newId = WorldPackets.toNewId(combined);
            meta.setValue(newId);
        });

        filter().type(EntityTypes1_13.EntityType.AREA_EFFECT_CLOUD).handler((event, meta) -> {
            if (meta.id() == 9) {
                int particleId = meta.value();
                Metadata parameter1Meta = event.metaAtIndex(10);
                Metadata parameter2Meta = event.metaAtIndex(11);
                int parameter1 = parameter1Meta != null ? parameter1Meta.value() : 0;
                int parameter2 = parameter2Meta != null ? parameter2Meta.value() : 0;

                Particle particle = ParticleRewriter.rewriteParticle(particleId, new Integer[]{parameter1, parameter2});
                if (particle != null && particle.getId() != -1) {
                    event.createExtraMeta(new Metadata(9, Types1_13.META_TYPES.particleType, particle));
                }
            }
            if (meta.id() >= 9) {
                event.cancel();
            }
        });
    }

    @Override
    public int newEntityId(final int id) {
        return EntityTypeRewriter.getNewId(id);
    }

    @Override
    public EntityType typeFromId(int type) {
        return EntityTypes1_13.getTypeFromId(type, false);
    }

    @Override
    public EntityType objectTypeFromId(int type) {
        return EntityTypes1_13.getTypeFromId(type, true);
    }
}