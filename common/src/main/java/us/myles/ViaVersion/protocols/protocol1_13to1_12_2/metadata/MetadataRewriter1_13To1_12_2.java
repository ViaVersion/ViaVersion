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
package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.metadata;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_13Types;
import us.myles.ViaVersion.api.entities.EntityType;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_13;
import us.myles.ViaVersion.api.rewriters.MetadataRewriter;
import us.myles.ViaVersion.api.type.types.Particle;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.ChatRewriter;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.EntityTypeRewriter;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.ParticleRewriter;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.packets.WorldPackets;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.EntityTracker1_13;

import java.util.List;

public class MetadataRewriter1_13To1_12_2 extends MetadataRewriter {

    public MetadataRewriter1_13To1_12_2(Protocol1_13To1_12_2 protocol) {
        super(protocol, EntityTracker1_13.class);
    }

    @Override
    protected void handleMetadata(int entityId, EntityType type, Metadata metadata, List<Metadata> metadatas, UserConnection connection) throws Exception {
        // Handle new MetaTypes
        if (metadata.getMetaType().getTypeID() > 4) {
            metadata.setMetaType(MetaType1_13.byId(metadata.getMetaType().getTypeID() + 1));
        } else {
            metadata.setMetaType(MetaType1_13.byId(metadata.getMetaType().getTypeID()));
        }

        // Handle String -> Chat DisplayName
        if (metadata.getId() == 2) {
            metadata.setMetaType(MetaType1_13.OptChat);
            if (metadata.getValue() != null && !((String) metadata.getValue()).isEmpty()) {
                metadata.setValue(ChatRewriter.legacyTextToJson((String) metadata.getValue()));
            } else {
                metadata.setValue(null);
            }
        }

        // Remap held block to match new format for remapping to flat block
        if (type == Entity1_13Types.EntityType.ENDERMAN && metadata.getId() == 12) {
            int stateId = (int) metadata.getValue();
            int id = stateId & 4095;
            int data = stateId >> 12 & 15;
            metadata.setValue((id << 4) | (data & 0xF));
        }

        // 1.13 changed item to flat item (no data)
        if (metadata.getMetaType() == MetaType1_13.Slot) {
            metadata.setMetaType(MetaType1_13.Slot);
            InventoryPackets.toClient((Item) metadata.getValue());
        } else if (metadata.getMetaType() == MetaType1_13.BlockID) {
            // Convert to new block id
            metadata.setValue(WorldPackets.toNewId((int) metadata.getValue()));
        }

        // Skip type related changes when the type is null
        if (type == null) return;

        // Handle new colors
        if (type == Entity1_13Types.EntityType.WOLF && metadata.getId() == 17) {
            metadata.setValue(15 - (int) metadata.getValue());
        }

        // Handle new zombie meta (INDEX 15 - Boolean - Zombie is shaking while enabled)
        if (type.isOrHasParent(Entity1_13Types.EntityType.ZOMBIE)) {
            if (metadata.getId() > 14)
                metadata.setId(metadata.getId() + 1);
        }

        // Handle Minecart inner block
        if (type.isOrHasParent(Entity1_13Types.EntityType.MINECART_ABSTRACT) && metadata.getId() == 9) {
            // New block format
            int oldId = (int) metadata.getValue();
            int combined = (((oldId & 4095) << 4) | (oldId >> 12 & 15));
            int newId = WorldPackets.toNewId(combined);
            metadata.setValue(newId);
        }

        // Handle other changes
        if (type == Entity1_13Types.EntityType.AREA_EFFECT_CLOUD) {
            if (metadata.getId() == 9) {
                int particleId = (int) metadata.getValue();
                Metadata parameter1Meta = getMetaByIndex(10, metadatas);
                Metadata parameter2Meta = getMetaByIndex(11, metadatas);
                int parameter1 = parameter1Meta != null ? (int) parameter1Meta.getValue() : 0;
                int parameter2 = parameter2Meta != null ? (int) parameter2Meta.getValue() : 0;

                Particle particle = ParticleRewriter.rewriteParticle(particleId, new Integer[]{parameter1, parameter2});
                if (particle != null && particle.getId() != -1) {
                    metadatas.add(new Metadata(9, MetaType1_13.PARTICLE, particle));
                }
            }

            if (metadata.getId() >= 9)
                metadatas.remove(metadata); // Remove
        }

        if (metadata.getId() == 0) {
            metadata.setValue((byte) ((byte) metadata.getValue() & ~0x10)); // Previously unused, now swimming
        }

        // TODO: Boat has changed
    }

    @Override
    public int getNewEntityId(final int oldId) {
        return EntityTypeRewriter.getNewId(oldId);
    }

    @Override
    protected EntityType getTypeFromId(int type) {
        return Entity1_13Types.getTypeFromId(type, false);
    }

    @Override
    protected EntityType getObjectTypeFromId(int type) {
        return Entity1_13Types.getTypeFromId(type, true);
    }
}
