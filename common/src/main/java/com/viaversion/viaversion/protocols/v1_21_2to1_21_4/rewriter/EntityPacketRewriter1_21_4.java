/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_21_2to1_21_4.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_4;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.Protocol1_21_2To1_21_4;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.packet.ServerboundPackets1_21_4;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPacket1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.rewriter.RegistryDataRewriter;

public final class EntityPacketRewriter1_21_4 extends EntityRewriter<ClientboundPacket1_21_2, Protocol1_21_2To1_21_4> {

    public EntityPacketRewriter1_21_4(final Protocol1_21_2To1_21_4 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerTrackerWithData1_19(ClientboundPackets1_21_2.ADD_ENTITY, EntityTypes1_21_4.FALLING_BLOCK);
        registerSetEntityData(ClientboundPackets1_21_2.SET_ENTITY_DATA);
        registerRemoveEntities(ClientboundPackets1_21_2.REMOVE_ENTITIES);

        final RegistryDataRewriter registryDataRewriter = new RegistryDataRewriter(protocol);
        registryDataRewriter.addHandler("worldgen/biome", (key, biome) -> {
            final CompoundTag effectsTag = biome.getCompoundTag("effects");
            final CompoundTag musicTag = effectsTag.getCompoundTag("music");
            if (musicTag == null) {
                return;
            }

            // Wrap music
            final ListTag<CompoundTag> weightedMusicTags = new ListTag<>(CompoundTag.class);
            final CompoundTag weightedMusicTag = new CompoundTag();
            weightedMusicTag.put("data", musicTag);
            weightedMusicTag.putInt("weight", 1);
            weightedMusicTags.add(weightedMusicTag);
            effectsTag.put("music", weightedMusicTags);
        });
        protocol.registerClientbound(ClientboundConfigurationPackets1_21.REGISTRY_DATA, registryDataRewriter::handle);

        registerLogin1_20_5(ClientboundPackets1_21_2.LOGIN);
        registerRespawn1_20_5(ClientboundPackets1_21_2.RESPAWN);

        protocol.registerServerbound(ServerboundPackets1_21_4.MOVE_VEHICLE, wrapper -> {
            wrapper.passthrough(Types.DOUBLE); // X
            wrapper.passthrough(Types.DOUBLE); // Y
            wrapper.passthrough(Types.DOUBLE); // Z
            wrapper.passthrough(Types.FLOAT); // Yaw
            wrapper.passthrough(Types.FLOAT); // Pitch
            wrapper.read(Types.BOOLEAN); // On ground
        });
        protocol.cancelServerbound(ServerboundPackets1_21_4.PLAYER_LOADED);
    }

    @Override
    protected void registerRewrites() {
        filter().mapDataType(VersionedTypes.V1_21_4.entityDataTypes::byId);

        registerEntityDataTypeHandler(
            VersionedTypes.V1_21_4.entityDataTypes.itemType,
            VersionedTypes.V1_21_4.entityDataTypes.blockStateType,
            VersionedTypes.V1_21_4.entityDataTypes.optionalBlockStateType,
            VersionedTypes.V1_21_4.entityDataTypes.particleType,
            VersionedTypes.V1_21_4.entityDataTypes.particlesType,
            VersionedTypes.V1_21_4.entityDataTypes.componentType,
            VersionedTypes.V1_21_4.entityDataTypes.optionalComponentType
        );
        registerBlockStateHandler(EntityTypes1_21_4.ABSTRACT_MINECART, 11);

        filter().type(EntityTypes1_21_4.CREAKING).addIndex(18); // Is tearing down
        filter().type(EntityTypes1_21_4.SALMON).index(17).handler((event, data) -> {
            final String type = data.value();
            final int typeId = switch (type) {
                case "small" -> 0;
                case "large" -> 2;
                default -> 1; // medium
            };
            data.setTypeAndValue(VersionedTypes.V1_21_4.entityDataTypes.varIntType, typeId);
        });
    }

    @Override
    public void onMappingDataLoaded() {
        mapTypes();
    }

    @Override
    public EntityType typeFromId(final int type) {
        return EntityTypes1_21_4.getTypeFromId(type);
    }
}
