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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.metadata;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_13Types;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_13;
import com.viaversion.viaversion.api.type.types.Particle;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ChatRewriter;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data.EntityTypeRewriter;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data.ParticleRewriter;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.packets.WorldPackets;
import com.viaversion.viaversion.rewriter.EntityRewriter;

import java.util.List;

public class MetadataRewriter1_13To1_12_2 extends EntityRewriter<Protocol1_13To1_12_2> {

    public MetadataRewriter1_13To1_12_2(Protocol1_13To1_12_2 protocol) {
        super(protocol);
    }

    @Override
    protected void handleMetadata(int entityId, EntityType type, Metadata metadata, List<Metadata> metadatas, UserConnection connection) throws Exception {
        // Handle new MetaTypes
        if (metadata.metaType().typeId() > 4) {
            metadata.setMetaType(MetaType1_13.byId(metadata.metaType().typeId() + 1));
        } else {
            metadata.setMetaType(MetaType1_13.byId(metadata.metaType().typeId()));
        }

        // Handle String -> Chat DisplayName
        if (metadata.id() == 2) {
            if (metadata.getValue() != null && !((String) metadata.getValue()).isEmpty()) {
                metadata.setTypeAndValue(MetaType1_13.OptChat, ChatRewriter.legacyTextToJson((String) metadata.getValue()));
            } else {
                metadata.setTypeAndValue(MetaType1_13.OptChat, null);
            }
        }

        // Remap held block to match new format for remapping to flat block
        if (type == Entity1_13Types.EntityType.ENDERMAN && metadata.id() == 12) {
            int stateId = (int) metadata.getValue();
            int id = stateId & 4095;
            int data = stateId >> 12 & 15;
            metadata.setValue((id << 4) | (data & 0xF));
        }

        // 1.13 changed item to flat item (no data)
        if (metadata.metaType() == MetaType1_13.Slot) {
            metadata.setMetaType(MetaType1_13.Slot);
            protocol.getItemRewriter().handleItemToClient((Item) metadata.getValue());
        } else if (metadata.metaType() == MetaType1_13.BlockID) {
            // Convert to new block id
            metadata.setValue(WorldPackets.toNewId((int) metadata.getValue()));
        }

        // Skip type related changes when the type is null
        if (type == null) return;

        // Handle new colors
        if (type == Entity1_13Types.EntityType.WOLF && metadata.id() == 17) {
            metadata.setValue(15 - (int) metadata.getValue());
        }

        // Handle new zombie meta (INDEX 15 - Boolean - Zombie is shaking while enabled)
        if (type.isOrHasParent(Entity1_13Types.EntityType.ZOMBIE)) {
            if (metadata.id() > 14)
                metadata.setId(metadata.id() + 1);
        }

        // Handle Minecart inner block
        if (type.isOrHasParent(Entity1_13Types.EntityType.MINECART_ABSTRACT) && metadata.id() == 9) {
            // New block format
            int oldId = (int) metadata.getValue();
            int combined = (((oldId & 4095) << 4) | (oldId >> 12 & 15));
            int newId = WorldPackets.toNewId(combined);
            metadata.setValue(newId);
        }

        // Handle other changes
        if (type == Entity1_13Types.EntityType.AREA_EFFECT_CLOUD) {
            if (metadata.id() == 9) {
                int particleId = (int) metadata.getValue();
                Metadata parameter1Meta = metaByIndex(10, metadatas);
                Metadata parameter2Meta = metaByIndex(11, metadatas);
                int parameter1 = parameter1Meta != null ? (int) parameter1Meta.getValue() : 0;
                int parameter2 = parameter2Meta != null ? (int) parameter2Meta.getValue() : 0;

                Particle particle = ParticleRewriter.rewriteParticle(particleId, new Integer[]{parameter1, parameter2});
                if (particle != null && particle.getId() != -1) {
                    metadatas.add(new Metadata(9, MetaType1_13.PARTICLE, particle));
                }
            }

            if (metadata.id() >= 9)
                metadatas.remove(metadata); // Remove
        }

        if (metadata.id() == 0) {
            metadata.setValue((byte) ((byte) metadata.getValue() & ~0x10)); // Previously unused, now swimming
        }

        // TODO: Boat has changed
    }

    @Override
    public int newEntityId(final int id) {
        return EntityTypeRewriter.getNewId(id);
    }

    @Override
    public EntityType typeFromId(int type) {
        return Entity1_13Types.getTypeFromId(type, false);
    }

    @Override
    public EntityType objectTypeFromId(int type) {
        return Entity1_13Types.getTypeFromId(type, true);
    }
}
