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
package com.viaversion.viaversion.protocols.v1_15_2to1_16.rewriter;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.WorldIdentifiers;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_16;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_14;
import com.viaversion.viaversion.api.type.types.version.Types1_16;
import com.viaversion.viaversion.protocols.v1_14_4to1_15.packet.ClientboundPackets1_15;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.Protocol1_15_2To1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.data.AttributeMappings1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.data.DimensionRegistries1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.packet.ClientboundPackets1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.packet.ServerboundPackets1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.storage.InventoryTracker1_16;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.util.Key;
import java.util.UUID;

public class EntityPacketRewriter1_16 extends EntityRewriter<ClientboundPackets1_15, Protocol1_15_2To1_16> {

    private final PacketHandler DIMENSION_HANDLER = wrapper -> {
        WorldIdentifiers map = Via.getConfig().get1_16WorldNamesMap();
        WorldIdentifiers userMap = wrapper.user().get(WorldIdentifiers.class);
        if (userMap != null) {
            map = userMap;
        }
        int dimension = wrapper.read(Types.INT);
        String dimensionName;
        String outputName;
        switch (dimension) {
            case -1 -> {
                dimensionName = "minecraft:the_nether";
                outputName = map.nether();
            }
            case 0 -> {
                dimensionName = "minecraft:overworld";
                outputName = map.overworld();
            }
            case 1 -> {
                dimensionName = "minecraft:the_end";
                outputName = map.end();
            }
            default -> {
                protocol.getLogger().warning("Invalid dimension id: " + dimension);
                dimensionName = "minecraft:overworld";
                outputName = map.overworld();
            }
        }

        wrapper.write(Types.STRING, dimensionName); // dimension
        wrapper.write(Types.STRING, outputName); // world
        trackWorld(wrapper.user(), outputName);
    };

    public EntityPacketRewriter1_16(Protocol1_15_2To1_16 protocol) {
        super(protocol);
    }

    @Override
    protected void registerPackets() {
        // Spawn lightning -> Spawn entity
        protocol.registerClientbound(ClientboundPackets1_15.ADD_GLOBAL_ENTITY, ClientboundPackets1_16.ADD_ENTITY, wrapper -> {
            int entityId = wrapper.passthrough(Types.VAR_INT);
            byte type = wrapper.read(Types.BYTE);
            if (type != 1) {
                // Cancel if not lightning/invalid id
                wrapper.cancel();
                return;
            }

            wrapper.user().getEntityTracker(Protocol1_15_2To1_16.class).addEntity(entityId, EntityTypes1_16.LIGHTNING_BOLT);

            wrapper.write(Types.UUID, UUID.randomUUID()); // uuid
            wrapper.write(Types.VAR_INT, EntityTypes1_16.LIGHTNING_BOLT.getId()); // entity type

            wrapper.passthrough(Types.DOUBLE); // x
            wrapper.passthrough(Types.DOUBLE); // y
            wrapper.passthrough(Types.DOUBLE); // z
            wrapper.write(Types.BYTE, (byte) 0); // yaw
            wrapper.write(Types.BYTE, (byte) 0); // pitch
            wrapper.write(Types.INT, 0); // data
            wrapper.write(Types.SHORT, (short) 0); // velocity
            wrapper.write(Types.SHORT, (short) 0); // velocity
            wrapper.write(Types.SHORT, (short) 0); // velocity
        });

        registerTrackerWithData(ClientboundPackets1_15.ADD_ENTITY, EntityTypes1_16.FALLING_BLOCK);
        registerTracker(ClientboundPackets1_15.ADD_MOB);
        registerTracker(ClientboundPackets1_15.ADD_PLAYER, EntityTypes1_16.PLAYER);
        registerSetEntityData(ClientboundPackets1_15.SET_ENTITY_DATA, Types1_14.ENTITY_DATA_LIST, Types1_16.ENTITY_DATA_LIST);
        registerRemoveEntities(ClientboundPackets1_15.REMOVE_ENTITIES);

        protocol.registerClientbound(ClientboundPackets1_15.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                handler(DIMENSION_HANDLER);
                map(Types.LONG); // Seed
                map(Types.UNSIGNED_BYTE); // Gamemode
                handler(wrapper -> {
                    wrapper.write(Types.BYTE, (byte) -1); // Previous gamemode, set to none

                    // <= 1.14.4 didn't keep attributes on respawn and 1.15.x always kept them
                    final boolean keepAttributes = wrapper.user().getProtocolInfo().serverProtocolVersion().newerThanOrEqualTo(ProtocolVersion.v1_15);

                    String levelType = wrapper.read(Types.STRING);
                    wrapper.write(Types.BOOLEAN, false); // debug
                    wrapper.write(Types.BOOLEAN, levelType.equals("flat"));
                    wrapper.write(Types.BOOLEAN, keepAttributes); // keep player attributes
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_15.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // Entity ID
                map(Types.UNSIGNED_BYTE); //  Gamemode
                handler(wrapper -> {
                    wrapper.write(Types.BYTE, (byte) -1); // Previous gamemode, set to none
                    wrapper.write(Types.STRING_ARRAY, DimensionRegistries1_16.getWorldNames()); // World list - only used for command completion
                    wrapper.write(Types.NAMED_COMPOUND_TAG, DimensionRegistries1_16.getDimensionsTag()); // Dimension registry
                });
                handler(DIMENSION_HANDLER); // Dimension
                map(Types.LONG); // Seed
                map(Types.UNSIGNED_BYTE); // Max players
                handler(playerTrackerHandler());
                handler(wrapper -> {
                    final String type = wrapper.read(Types.STRING);// level type
                    wrapper.passthrough(Types.VAR_INT); // View distance
                    wrapper.passthrough(Types.BOOLEAN); // Reduced debug info
                    wrapper.passthrough(Types.BOOLEAN); // Show death screen

                    wrapper.write(Types.BOOLEAN, false); // Debug
                    wrapper.write(Types.BOOLEAN, type.equals("flat"));
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_15.UPDATE_ATTRIBUTES, wrapper -> {
            wrapper.passthrough(Types.VAR_INT);
            int size = wrapper.passthrough(Types.INT);
            int actualSize = size;
            for (int i = 0; i < size; i++) {
                // Attributes have been renamed and are now namespaced identifiers
                String key = wrapper.read(Types.STRING);
                String attributeIdentifier = AttributeMappings1_16.attributeIdentifierMappings().get(key);
                if (attributeIdentifier == null) {
                    attributeIdentifier = Key.namespaced(key);
                    if (!Key.isValid(attributeIdentifier)) {
                        if (!Via.getConfig().isSuppressConversionWarnings()) {
                            protocol.getLogger().warning("Invalid attribute: " + key);
                        }
                        actualSize--;
                        wrapper.read(Types.DOUBLE);
                        int modifierSize = wrapper.read(Types.VAR_INT);
                        for (int j = 0; j < modifierSize; j++) {
                            wrapper.read(Types.UUID);
                            wrapper.read(Types.DOUBLE);
                            wrapper.read(Types.BYTE);
                        }
                        continue;
                    }
                }

                wrapper.write(Types.STRING, attributeIdentifier);

                wrapper.passthrough(Types.DOUBLE);
                int modifierSize = wrapper.passthrough(Types.VAR_INT);
                for (int j = 0; j < modifierSize; j++) {
                    wrapper.passthrough(Types.UUID);
                    wrapper.passthrough(Types.DOUBLE);
                    wrapper.passthrough(Types.BYTE);
                }
            }
            if (size != actualSize) {
                wrapper.set(Types.INT, 0, actualSize);
            }
        });

        protocol.registerServerbound(ServerboundPackets1_16.SWING, wrapper -> {
            InventoryTracker1_16 inventoryTracker = wrapper.user().get(InventoryTracker1_16.class);
            // Don't send an arm swing if the player has an inventory opened.
            if (inventoryTracker.isInventoryOpen()) {
                wrapper.cancel();
            }
        });
    }

    @Override
    protected void registerRewrites() {
        filter().mapDataType(Types1_16.ENTITY_DATA_TYPES::byId);
        registerEntityDataTypeHandler(Types1_16.ENTITY_DATA_TYPES.itemType, Types1_16.ENTITY_DATA_TYPES.optionalBlockStateType, Types1_16.ENTITY_DATA_TYPES.particleType);
        registerBlockStateHandler(EntityTypes1_16.ABSTRACT_MINECART, 10);

        filter().type(EntityTypes1_16.ABSTRACT_ARROW).removeIndex(8);
        filter().type(EntityTypes1_16.WOLF).index(16).handler((event, data) -> {
            byte mask = data.value();
            int angerTime = (mask & 0x02) != 0 ? Integer.MAX_VALUE : 0;
            event.createExtraData(new EntityData(20, Types1_16.ENTITY_DATA_TYPES.varIntType, angerTime));
        });
    }

    @Override
    public void onMappingDataLoaded() {
        mapTypes();
    }

    @Override
    public EntityType typeFromId(int type) {
        return EntityTypes1_16.getTypeFromId(type);
    }
}
