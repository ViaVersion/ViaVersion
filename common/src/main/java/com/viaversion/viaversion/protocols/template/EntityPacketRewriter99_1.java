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
package com.viaversion.viaversion.protocols.template;

import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_11;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.rewriter.EntityRewriter;

// Replace if needed
//  EntityTypes1_21_11
final class EntityPacketRewriter99_1 extends EntityRewriter<ClientboundPacket26_1, Protocol98_1To99_1> {

    public EntityPacketRewriter99_1(final Protocol98_1To99_1 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        // Common entity registrations (including LOGIN, RESPAWN) are handled by SharedRegistrations.
    }

    @Override
    protected void registerRewrites() {
        final EntityDataTypes26_1 entityDataTypes = protocol.mappedTypes().entityDataTypes();
        dataTypeMapper().register();
        /* ... or like this for additions and removals that are not at the very end
        dataTypeMapper()
            .added(entityDataTypes.catSoundVariant)
            .removed(entityDataTypes.cowSoundVariant)
            .skip(entityDataTypes.pigSoundVariant) // if neither removed nor added, but the value type has to be changed separately
            .register();*/

        // Registers registry type id changes
        registerEntityDataTypeHandler(
            entityDataTypes.itemType,
            entityDataTypes.blockStateType,
            entityDataTypes.optionalBlockStateType,
            entityDataTypes.particleType,
            entityDataTypes.particlesType,
            entityDataTypes.componentType,
            entityDataTypes.optionalComponentType
        );
    }

    @Override
    public EntityType typeFromId(final int type) {
        return EntityTypes1_21_11.getTypeFromId(type);
    }
}
