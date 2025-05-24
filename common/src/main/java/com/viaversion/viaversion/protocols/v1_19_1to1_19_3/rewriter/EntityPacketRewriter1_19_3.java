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
package com.viaversion.viaversion.protocols.v1_19_1to1_19_3.rewriter;

import com.google.gson.JsonElement;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_19_3;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_19;
import com.viaversion.viaversion.api.type.types.version.Types1_19_3;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.Protocol1_19_1To1_19_3;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.packet.ClientboundPackets1_19_3;
import com.viaversion.viaversion.protocols.v1_19to1_19_1.packet.ClientboundPackets1_19_1;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import java.util.BitSet;
import java.util.UUID;

public final class EntityPacketRewriter1_19_3 extends EntityRewriter<ClientboundPackets1_19_1, Protocol1_19_1To1_19_3> {

    public EntityPacketRewriter1_19_3(final Protocol1_19_1To1_19_3 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerTrackerWithData1_19(ClientboundPackets1_19_1.ADD_ENTITY, EntityTypes1_19_3.FALLING_BLOCK);
        registerTracker(ClientboundPackets1_19_1.ADD_EXPERIENCE_ORB, EntityTypes1_19_3.EXPERIENCE_ORB);
        registerTracker(ClientboundPackets1_19_1.ADD_PLAYER, EntityTypes1_19_3.PLAYER);
        registerSetEntityData(ClientboundPackets1_19_1.SET_ENTITY_DATA, Types1_19.ENTITY_DATA_LIST, Types1_19_3.ENTITY_DATA_LIST);
        registerRemoveEntities(ClientboundPackets1_19_1.REMOVE_ENTITIES);

        protocol.registerClientbound(ClientboundPackets1_19_1.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // Entity id
                map(Types.BOOLEAN); // Hardcore
                map(Types.BYTE); // Gamemode
                map(Types.BYTE); // Previous Gamemode
                map(Types.STRING_ARRAY); // World List
                map(Types.NAMED_COMPOUND_TAG); // Dimension registry
                map(Types.STRING); // Dimension key
                map(Types.STRING); // World
                handler(dimensionDataHandler());
                handler(biomeSizeTracker());
                handler(worldDataTrackerHandlerByKey());
                handler(playerTrackerHandler());
                handler(wrapper -> {
                    // Also enable vanilla features (set by default in later versions, but keeping it explicit is nicer)
                    final PacketWrapper enableFeaturesPacket = wrapper.create(ClientboundPackets1_19_3.UPDATE_ENABLED_FEATURES);
                    enableFeaturesPacket.write(Types.STRING_ARRAY, new String[]{"minecraft:vanilla"});

                    if (wrapper.user().getProtocolInfo().protocolVersion().newerThanOrEqualTo(ProtocolVersion.v1_20_2)) {
                        // Make sure it's included in the configuration packets
                        enableFeaturesPacket.send(Protocol1_19_1To1_19_3.class);
                    } else {
                        enableFeaturesPacket.scheduleSend(Protocol1_19_1To1_19_3.class);
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_1.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // Dimension
                map(Types.STRING); // World
                map(Types.LONG); // Seed
                map(Types.UNSIGNED_BYTE); // Gamemode
                map(Types.BYTE); // Previous gamemode
                map(Types.BOOLEAN); // Debug
                map(Types.BOOLEAN); // Flat
                handler(worldDataTrackerHandlerByKey());
                handler(wrapper -> {
                    final boolean keepAttributes = wrapper.read(Types.BOOLEAN);
                    byte keepDataMask = 0x02; // Always keep entity data
                    if (keepAttributes) {
                        keepDataMask |= 0x01;
                    }
                    wrapper.write(Types.BYTE, keepDataMask);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_1.PLAYER_INFO, ClientboundPackets1_19_3.PLAYER_INFO_UPDATE, wrapper -> {
            final int action = wrapper.read(Types.VAR_INT);
            if (action == 4) { // Remove player
                // Write into new packet type
                final int entries = wrapper.read(Types.VAR_INT);
                final UUID[] uuidsToRemove = new UUID[entries];
                for (int i = 0; i < entries; i++) {
                    uuidsToRemove[i] = wrapper.read(Types.UUID);
                }
                wrapper.write(Types.UUID_ARRAY, uuidsToRemove);
                wrapper.setPacketType(ClientboundPackets1_19_3.PLAYER_INFO_REMOVE);
                return;
            }

            final BitSet set = new BitSet(6);
            if (action == 0) {
                // Includes add player, profile key, gamemode, listed status, latency, and display name
                set.set(0, 6);
            } else {
                // Update listed added at 3, initialize chat added at index 1
                set.set(action == 1 ? action + 1 : action + 2);
            }

            wrapper.write(Types.PROFILE_ACTIONS_ENUM1_19_3, set);
            final int entries = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < entries; i++) {
                wrapper.passthrough(Types.UUID); // UUID
                if (action == 0) { // Add player
                    wrapper.passthrough(Types.STRING); // Player Name
                    wrapper.passthrough(Types.PROFILE_PROPERTY_ARRAY);

                    final int gamemode = wrapper.read(Types.VAR_INT);
                    final int ping = wrapper.read(Types.VAR_INT);
                    final JsonElement displayName = wrapper.read(Types.OPTIONAL_COMPONENT);
                    wrapper.read(Types.OPTIONAL_PROFILE_KEY);

                    wrapper.write(Types.BOOLEAN, false); // No chat session data
                    wrapper.write(Types.VAR_INT, gamemode);
                    wrapper.write(Types.BOOLEAN, true); // Also update listed
                    wrapper.write(Types.VAR_INT, ping);
                    wrapper.write(Types.OPTIONAL_COMPONENT, displayName);
                } else if (action == 1 || action == 2) { // Update gamemode/update latency
                    wrapper.passthrough(Types.VAR_INT);
                } else if (action == 3) { // Update display name
                    wrapper.passthrough(Types.OPTIONAL_COMPONENT);
                }
            }
        });
    }

    @Override
    protected void registerRewrites() {
        filter().mapDataType(typeId -> Types1_19_3.ENTITY_DATA_TYPES.byId(typeId >= 2 ? typeId + 1 : typeId)); // Long added
        registerEntityDataTypeHandler(Types1_19_3.ENTITY_DATA_TYPES.itemType, Types1_19_3.ENTITY_DATA_TYPES.optionalBlockStateType, Types1_19_3.ENTITY_DATA_TYPES.particleType);
        registerBlockStateHandler(EntityTypes1_19_3.ABSTRACT_MINECART, 11);

        filter().type(EntityTypes1_19_3.ENTITY).index(6).handler((event, data) -> {
            // Sitting pose added
            final int pose = data.value();
            if (pose >= 10) {
                data.setValue(pose + 1);
            }
        });
    }

    @Override
    public void onMappingDataLoaded() {
        mapTypes();
    }

    @Override
    public EntityType typeFromId(final int type) {
        return EntityTypes1_19_3.getTypeFromId(type);
    }
}
