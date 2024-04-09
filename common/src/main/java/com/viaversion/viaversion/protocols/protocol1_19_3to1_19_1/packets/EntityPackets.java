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
package com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.packets;

import com.google.gson.JsonElement;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_19_3;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_19;
import com.viaversion.viaversion.api.type.types.version.Types1_19_3;
import com.viaversion.viaversion.protocols.protocol1_19_1to1_19.ClientboundPackets1_19_1;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ClientboundPackets1_19_3;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.Protocol1_19_3To1_19_1;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import java.util.BitSet;
import java.util.UUID;

public final class EntityPackets extends EntityRewriter<ClientboundPackets1_19_1, Protocol1_19_3To1_19_1> {

    public EntityPackets(final Protocol1_19_3To1_19_1 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerTrackerWithData1_19(ClientboundPackets1_19_1.SPAWN_ENTITY, EntityTypes1_19_3.FALLING_BLOCK);
        registerTracker(ClientboundPackets1_19_1.SPAWN_EXPERIENCE_ORB, EntityTypes1_19_3.EXPERIENCE_ORB);
        registerTracker(ClientboundPackets1_19_1.SPAWN_PLAYER, EntityTypes1_19_3.PLAYER);
        registerMetadataRewriter(ClientboundPackets1_19_1.ENTITY_METADATA, Types1_19.METADATA_LIST, Types1_19_3.METADATA_LIST);
        registerRemoveEntities(ClientboundPackets1_19_1.REMOVE_ENTITIES);

        protocol.registerClientbound(ClientboundPackets1_19_1.JOIN_GAME, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // Entity id
                map(Type.BOOLEAN); // Hardcore
                map(Type.BYTE); // Gamemode
                map(Type.BYTE); // Previous Gamemode
                map(Type.STRING_ARRAY); // World List
                map(Type.NAMED_COMPOUND_TAG); // Dimension registry
                map(Type.STRING); // Dimension key
                map(Type.STRING); // World
                handler(dimensionDataHandler());
                handler(biomeSizeTracker());
                handler(worldDataTrackerHandlerByKey());
                handler(playerTrackerHandler());
                handler(wrapper -> {
                    // Also enable vanilla features
                    final PacketWrapper enableFeaturesPacket = wrapper.create(ClientboundPackets1_19_3.UPDATE_ENABLED_FEATURES);
                    enableFeaturesPacket.write(Type.VAR_INT, 1);
                    enableFeaturesPacket.write(Type.STRING, "minecraft:vanilla");
                    enableFeaturesPacket.scheduleSend(Protocol1_19_3To1_19_1.class);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_1.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // Dimension
                map(Type.STRING); // World
                map(Type.LONG); // Seed
                map(Type.UNSIGNED_BYTE); // Gamemode
                map(Type.BYTE); // Previous gamemode
                map(Type.BOOLEAN); // Debug
                map(Type.BOOLEAN); // Flat
                handler(worldDataTrackerHandlerByKey());
                handler(wrapper -> {
                    final boolean keepAttributes = wrapper.read(Type.BOOLEAN);
                    byte keepDataMask = 0x02; // Always keep entity data
                    if (keepAttributes) {
                        keepDataMask |= 0x01;
                    }
                    wrapper.write(Type.BYTE, keepDataMask);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_1.PLAYER_INFO, ClientboundPackets1_19_3.PLAYER_INFO_UPDATE, wrapper -> {
            final int action = wrapper.read(Type.VAR_INT);
            if (action == 4) { // Remove player
                // Write into new packet type
                final int entries = wrapper.read(Type.VAR_INT);
                final UUID[] uuidsToRemove = new UUID[entries];
                for (int i = 0; i < entries; i++) {
                    uuidsToRemove[i] = wrapper.read(Type.UUID);
                }
                wrapper.write(Type.UUID_ARRAY, uuidsToRemove);
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

            wrapper.write(Type.PROFILE_ACTIONS_ENUM, set);
            final int entries = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < entries; i++) {
                wrapper.passthrough(Type.UUID); // UUID
                if (action == 0) { // Add player
                    wrapper.passthrough(Type.STRING); // Player Name

                    final int properties = wrapper.passthrough(Type.VAR_INT);
                    for (int j = 0; j < properties; j++) {
                        wrapper.passthrough(Type.STRING); // Name
                        wrapper.passthrough(Type.STRING); // Value
                        wrapper.passthrough(Type.OPTIONAL_STRING); // Signature
                    }

                    final int gamemode = wrapper.read(Type.VAR_INT);
                    final int ping = wrapper.read(Type.VAR_INT);
                    final JsonElement displayName = wrapper.read(Type.OPTIONAL_COMPONENT);
                    wrapper.read(Type.OPTIONAL_PROFILE_KEY);

                    wrapper.write(Type.BOOLEAN, false); // No chat session data
                    wrapper.write(Type.VAR_INT, gamemode);
                    wrapper.write(Type.BOOLEAN, true); // Also update listed
                    wrapper.write(Type.VAR_INT, ping);
                    wrapper.write(Type.OPTIONAL_COMPONENT, displayName);
                } else if (action == 1 || action == 2) { // Update gamemode/update latency
                    wrapper.passthrough(Type.VAR_INT);
                } else if (action == 3) { // Update display name
                    wrapper.passthrough(Type.OPTIONAL_COMPONENT);
                }
            }
        });
    }

    @Override
    protected void registerRewrites() {
        filter().mapMetaType(typeId -> Types1_19_3.META_TYPES.byId(typeId >= 2 ? typeId + 1 : typeId)); // Long added
        registerMetaTypeHandler(Types1_19_3.META_TYPES.itemType, Types1_19_3.META_TYPES.blockStateType, Types1_19_3.META_TYPES.particleType);

        filter().type(EntityTypes1_19_3.ENTITY).index(6).handler((event, meta) -> {
            // Sitting pose added
            final int pose = meta.value();
            if (pose >= 10) {
                meta.setValue(pose + 1);
            }
        });
        filter().type(EntityTypes1_19_3.MINECART_ABSTRACT).index(11).handler((event, meta) -> {
            // Convert to new block id
            final int data = (int) meta.getValue();
            meta.setValue(protocol.getMappingData().getNewBlockStateId(data));
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
