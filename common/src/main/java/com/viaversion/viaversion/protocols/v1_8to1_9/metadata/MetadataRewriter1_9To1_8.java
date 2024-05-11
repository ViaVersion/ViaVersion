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
package com.viaversion.viaversion.protocols.v1_8to1_9.metadata;

import com.viaversion.viaversion.api.minecraft.EulerAngle;
import com.viaversion.viaversion.api.minecraft.Vector;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.MetaType;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_8;
import com.viaversion.viaversion.protocols.v1_8.packet.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.rewriter.ItemRewriter;
import com.viaversion.viaversion.protocols.v1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.rewriter.meta.MetaHandlerEvent;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.SerializerVersion;
import java.util.UUID;

public class MetadataRewriter1_9To1_8 extends EntityRewriter<ClientboundPackets1_8, Protocol1_8To1_9> {

    public MetadataRewriter1_9To1_8(Protocol1_8To1_9 protocol) {
        super(protocol);
    }

    @Override
    protected void registerRewrites() {
        filter().handler(this::handleMetadata);
    }

    private void handleMetadata(MetaHandlerEvent event, Metadata metadata) {
        EntityType type = event.entityType();
        MetaIndex metaIndex = MetaIndex.searchIndex(type, metadata.id());
        if (metaIndex == null) {
            // Almost certainly bad data, remove it
            event.cancel();
            return;
        }

        if (metaIndex.getNewType() == null) {
            event.cancel();
            return;
        }

        metadata.setId(metaIndex.getNewIndex());
        metadata.setMetaTypeUnsafe(metaIndex.getNewType());

        Object value = metadata.getValue();
        switch (metaIndex.getNewType()) {
            case BYTE:
                // convert from int, byte
                if (metaIndex.getOldType() == MetaType1_8.BYTE) {
                    metadata.setValue(value);
                }
                if (metaIndex.getOldType() == MetaType1_8.INT) {
                    metadata.setValue(((Integer) value).byteValue());
                }
                // After writing the last one
                if (metaIndex == MetaIndex.ENTITY_STATUS && type == EntityTypes1_10.EntityType.PLAYER) {
                    byte val = 0;
                    if ((((Byte) value) & 0x10) == 0x10) { // Player eating/aiming/drinking
                        val = 1;
                    }
                    int newIndex = MetaIndex.PLAYER_HAND.getNewIndex();
                    MetaType metaType = MetaIndex.PLAYER_HAND.getNewType();
                    event.createExtraMeta(new Metadata(newIndex, metaType, val));
                }
                break;
            case OPTIONAL_UUID:
                String owner = (String) value;
                UUID toWrite = null;
                if (!owner.isEmpty()) {
                    try {
                        toWrite = UUID.fromString(owner);
                    } catch (Exception ignored) {
                    }
                }
                metadata.setValue(toWrite);
                break;
            case VAR_INT:
                // convert from int, short, byte
                if (metaIndex.getOldType() == MetaType1_8.BYTE) {
                    metadata.setValue(((Byte) value).intValue());
                }
                if (metaIndex.getOldType() == MetaType1_8.SHORT) {
                    metadata.setValue(((Short) value).intValue());
                }
                if (metaIndex.getOldType() == MetaType1_8.INT) {
                    metadata.setValue(value);
                }
                break;
            case FLOAT, STRING:
                metadata.setValue(value);
                break;
            case BOOLEAN:
                if (metaIndex == MetaIndex.AGEABLE_CREATURE_AGE)
                    metadata.setValue((Byte) value < 0);
                else
                    metadata.setValue((Byte) value != 0);
                break;
            case ITEM:
                metadata.setValue(value);
                ItemRewriter.toClient((Item) metadata.getValue());
                break;
            case BLOCK_POSITION:
                Vector vector = (Vector) value;
                metadata.setValue(vector);
                break;
            case ROTATIONS:
                EulerAngle angle = (EulerAngle) value;
                metadata.setValue(angle);
                break;
            case COMPONENT:
                // Was previously also a component, so just convert it
                String text = (String) value;
                metadata.setValue(ComponentUtil.convertJsonOrEmpty(text, SerializerVersion.V1_8, SerializerVersion.V1_9));
                break;
            case OPTIONAL_BLOCK_STATE:
                // Convert from int, short, byte
                metadata.setValue(((Number) value).intValue());
                break;
            default:
                throw new RuntimeException("Unhandled MetaDataType: " + metaIndex.getNewType());
        }
    }

    @Override
    public EntityType typeFromId(int type) {
        return EntityTypes1_10.getTypeFromId(type, false);
    }

    @Override
    public EntityType objectTypeFromId(int type) {
        return EntityTypes1_10.getTypeFromId(type, true);
    }
}
