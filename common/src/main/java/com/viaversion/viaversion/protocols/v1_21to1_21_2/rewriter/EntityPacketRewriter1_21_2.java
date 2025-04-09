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
package com.viaversion.viaversion.protocols.v1_21to1_21_2.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_2;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_21;
import com.viaversion.viaversion.api.type.types.version.Types1_21_2;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPacket1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.Protocol1_21To1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ServerboundPackets1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.storage.BundleStateTracker;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.storage.ChunkLoadTracker;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.storage.ClientVehicleStorage;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.storage.EntityTracker1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.storage.GroundFlagTracker;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.storage.PlayerPositionStorage;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.rewriter.RegistryDataRewriter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class EntityPacketRewriter1_21_2 extends EntityRewriter<ClientboundPacket1_21, Protocol1_21To1_21_2> {

    private static final String[] GOAT_HORN_INSTRUMENTS = {
        "ponder_goat_horn",
        "sing_goat_horn",
        "seek_goat_horn",
        "feel_goat_horn",
        "admire_goat_horn",
        "call_goat_horn",
        "yearn_goat_horn",
        "dream_goat_horn"
    };
    private static final float IMPULSE = 0.98F;

    public EntityPacketRewriter1_21_2(final Protocol1_21To1_21_2 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerTrackerWithData1_19(ClientboundPackets1_21.ADD_ENTITY, EntityTypes1_21_2.FALLING_BLOCK);
        registerSetEntityData(ClientboundPackets1_21.SET_ENTITY_DATA, Types1_21.ENTITY_DATA_LIST, Types1_21_2.ENTITY_DATA_LIST);
        registerRemoveEntities(ClientboundPackets1_21.REMOVE_ENTITIES);

        protocol.appendClientbound(ClientboundPackets1_21.ADD_ENTITY, wrapper -> {
            final int entityType = wrapper.get(Types.VAR_INT, 1);

            final EntityType type = typeFromId(entityType);
            if (type == null || !type.isOrHasParent(EntityTypes1_21_2.ABSTRACT_BOAT)) {
                return;
            }

            final int entityId = wrapper.get(Types.VAR_INT, 0);
            final UUID uuid = wrapper.get(Types.UUID, 0);

            final double x = wrapper.get(Types.DOUBLE, 0);
            final double y = wrapper.get(Types.DOUBLE, 1);
            final double z = wrapper.get(Types.DOUBLE, 2);

            final float pitch = wrapper.get(Types.BYTE, 0) * 256.0F / 360.0F;
            final float yaw = wrapper.get(Types.BYTE, 1) * 256.0F / 360.0F;

            final int data = wrapper.get(Types.VAR_INT, 2);

            final EntityTracker1_21_2 tracker = tracker(wrapper.user());
            final EntityTracker1_21_2.BoatEntity entity = tracker.trackBoatEntity(entityId, uuid, data);
            entity.setPosition(x, y, z);
            entity.setRotation(yaw, pitch);
        });

        protocol.registerFinishConfiguration(ClientboundConfigurationPackets1_21.FINISH_CONFIGURATION, wrapper -> {
            final PacketWrapper instrumentsPacket = wrapper.create(ClientboundConfigurationPackets1_21.REGISTRY_DATA);
            instrumentsPacket.write(Types.STRING, "minecraft:instrument");
            final RegistryEntry[] entries = new RegistryEntry[GOAT_HORN_INSTRUMENTS.length];
            for (int i = 0; i < GOAT_HORN_INSTRUMENTS.length; i++) {
                final CompoundTag tag = new CompoundTag();
                tag.putString("sound_event", "item.goat_horn.sound." + i);
                tag.putFloat("use_duration", 7);
                tag.putInt("range", 256);
                tag.putString("description", "");
                entries[i] = new RegistryEntry(GOAT_HORN_INSTRUMENTS[i], tag);
            }
            instrumentsPacket.write(Types.REGISTRY_ENTRY_ARRAY, entries);
            instrumentsPacket.send(Protocol1_21To1_21_2.class);
        });

        final RegistryDataRewriter registryDataRewriter = registryDataRewriter();
        protocol.registerClientbound(ClientboundConfigurationPackets1_21.REGISTRY_DATA, registryDataRewriter::handle);

        protocol.registerClientbound(ClientboundPackets1_21.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // Entity id
                map(Types.BOOLEAN); // Hardcore
                map(Types.STRING_ARRAY); // World List
                map(Types.VAR_INT); // Max players
                map(Types.VAR_INT); // View distance
                map(Types.VAR_INT); // Simulation distance
                map(Types.BOOLEAN); // Reduced debug info
                map(Types.BOOLEAN); // Show death screen
                map(Types.BOOLEAN); // Limited crafting
                map(Types.VAR_INT); // Dimension id
                map(Types.STRING); // World
                map(Types.LONG); // Seed
                map(Types.BYTE); // Gamemode
                map(Types.BYTE); // Previous gamemode
                map(Types.BOOLEAN); // Debug
                map(Types.BOOLEAN); // Flat
                map(Types.OPTIONAL_GLOBAL_POSITION); // Last death location
                map(Types.VAR_INT); // Portal cooldown
                handler(worldDataTrackerHandlerByKey1_20_5(3));
                handler(playerTrackerHandler());
                create(Types.VAR_INT, 64); // Sea level, was hardcoded at 64 before
            }
        });

        protocol.registerClientbound(ClientboundPackets1_21.RESPAWN, wrapper -> {
            final int dimensionId = wrapper.passthrough(Types.VAR_INT);
            final String world = wrapper.passthrough(Types.STRING);
            wrapper.passthrough(Types.LONG); // Seed
            wrapper.passthrough(Types.BYTE); // Gamemode
            wrapper.passthrough(Types.BYTE); // Previous gamemode
            wrapper.passthrough(Types.BOOLEAN); // Debug
            wrapper.passthrough(Types.BOOLEAN); // Flat
            wrapper.passthrough(Types.OPTIONAL_GLOBAL_POSITION); // Last death location
            wrapper.passthrough(Types.VAR_INT); // Portal cooldown

            wrapper.write(Types.VAR_INT, 64); // Sea level

            final EntityTracker entityTracker = tracker(wrapper.user());
            if (entityTracker.currentWorld() != null && !entityTracker.currentWorld().equals(world)) {
                final ChunkLoadTracker chunkLoadTracker = wrapper.user().get(ChunkLoadTracker.class);
                if (chunkLoadTracker != null) {
                    chunkLoadTracker.clear();
                }
            }

            trackWorldDataByKey1_20_5(wrapper.user(), dimensionId, world);

            wrapper.user().put(new GroundFlagTracker());
            wrapper.user().remove(ClientVehicleStorage.class);
        });

        protocol.registerClientbound(ClientboundPackets1_21.PLAYER_POSITION, wrapper -> {
            wrapper.write(Types.VAR_INT, 0); // Teleport id, set at the end

            wrapper.passthrough(Types.DOUBLE); // X
            wrapper.passthrough(Types.DOUBLE); // Y
            wrapper.passthrough(Types.DOUBLE); // Z

            wrapper.write(Types.DOUBLE, 0D); // Delta movement X
            wrapper.write(Types.DOUBLE, 0D); // Delta movement Y
            wrapper.write(Types.DOUBLE, 0D); // Delta movement Z

            wrapper.passthrough(Types.FLOAT); // Y rot
            wrapper.passthrough(Types.FLOAT); // X rot

            int relativeArguments = wrapper.read(Types.BYTE) & 0b00011111;
            if ((relativeArguments & (1 << 0)) != 0) { // relative X
                relativeArguments |= 1 << 5; // relative delta movement X
            }
            if ((relativeArguments & (1 << 1)) != 0) { // relative Y
                relativeArguments |= 1 << 6; // relative delta movement Y
            }
            if ((relativeArguments & (1 << 2)) != 0) { // relative Z
                relativeArguments |= 1 << 7; // relative delta movement Z
            }
            wrapper.write(Types.INT, relativeArguments);

            final int teleportId = wrapper.read(Types.VAR_INT);
            wrapper.set(Types.VAR_INT, 0, teleportId);

            final PlayerPositionStorage positionStorage = wrapper.user().get(PlayerPositionStorage.class);
            if (positionStorage == null) {
                return;
            }

            // Accept teleportation and player position were swapped.
            // Send a ping first to then capture and send the player position the accept teleportation
            final boolean isBundling = wrapper.user().get(BundleStateTracker.class).isBundling();
            if (!isBundling) {
                final PacketWrapper bundleStart = wrapper.create(ClientboundPackets1_21_2.BUNDLE_DELIMITER);
                bundleStart.send(Protocol1_21To1_21_2.class);
            }

            final int pingId = ThreadLocalRandom.current().nextInt();
            positionStorage.addPendingPong(pingId);
            final PacketWrapper ping = wrapper.create(ClientboundPackets1_21_2.PING);
            ping.write(Types.INT, pingId); // id
            ping.send(Protocol1_21To1_21_2.class);
            wrapper.send(Protocol1_21To1_21_2.class);
            wrapper.cancel();

            if (!isBundling) {
                final PacketWrapper bundleEnd = wrapper.create(ClientboundPackets1_21_2.BUNDLE_DELIMITER);
                bundleEnd.send(Protocol1_21To1_21_2.class);
            }
        });

        protocol.registerClientbound(ClientboundPackets1_21.SET_PASSENGERS, wrapper -> {
            final int vehicleId = wrapper.passthrough(Types.VAR_INT);
            final int[] passengerIds = wrapper.passthrough(Types.VAR_INT_ARRAY_PRIMITIVE);
            final ClientVehicleStorage storage = wrapper.user().get(ClientVehicleStorage.class);
            if (storage != null && vehicleId == storage.vehicleId()) {
                wrapper.user().remove(ClientVehicleStorage.class);
            }

            final EntityTracker1_21_2 tracker = tracker(wrapper.user());
            final EntityTracker1_21_2.BoatEntity entity = tracker.trackedBoatEntity(vehicleId);
            if (entity != null) {
                entity.setPassengers(passengerIds);
            }

            final int clientEntityId = tracker(wrapper.user()).clientEntityId();
            for (final int passenger : passengerIds) {
                if (passenger == clientEntityId) {
                    wrapper.user().put(new ClientVehicleStorage(vehicleId));
                    break;
                }
            }
        });
        protocol.appendClientbound(ClientboundPackets1_21.REMOVE_ENTITIES, wrapper -> {
            final ClientVehicleStorage vehicleStorage = wrapper.user().get(ClientVehicleStorage.class);
            if (vehicleStorage == null) {
                return;
            }

            final int[] entityIds = wrapper.get(Types.VAR_INT_ARRAY_PRIMITIVE, 0);
            for (final int entityId : entityIds) {
                if (entityId == vehicleStorage.vehicleId()) {
                    wrapper.user().remove(ClientVehicleStorage.class);
                    break;
                }
            }
        });
        protocol.registerServerbound(ServerboundPackets1_21_2.PLAYER_INPUT, wrapper -> {
            // Previously only used while in a vehicle, now always sent
            // Filter them appropriately and always send them when in a vehicle
            wrapper.cancel();
            final ClientVehicleStorage vehicleStorage = wrapper.user().get(ClientVehicleStorage.class);
            if (vehicleStorage == null) {
                return;
            }

            final byte flags = wrapper.read(Types.BYTE);
            final boolean left = (flags & 1 << 2) != 0;
            final boolean right = (flags & 1 << 3) != 0;
            final float sidewaysMovement = left ? IMPULSE : (right ? -IMPULSE : 0F);

            final boolean forward = (flags & 1 << 0) != 0;
            final boolean backward = (flags & 1 << 1) != 0;
            final float forwardMovement = forward ? IMPULSE : (backward ? -IMPULSE : 0F);

            byte updatedFlags = 0;
            if ((flags & 1 << 4) != 0) {
                updatedFlags |= 1;
            }
            if ((flags & 1 << 5) != 0) {
                updatedFlags |= 2;
            }

            vehicleStorage.storeMovement(sidewaysMovement, forwardMovement, updatedFlags);
        });

        protocol.registerClientbound(ClientboundPackets1_21.TELEPORT_ENTITY, ClientboundPackets1_21_2.ENTITY_POSITION_SYNC, wrapper -> {
            final int entityId = wrapper.passthrough(Types.VAR_INT); // Entity ID

            final double x = wrapper.passthrough(Types.DOUBLE); // X
            final double y = wrapper.passthrough(Types.DOUBLE); // Y
            final double z = wrapper.passthrough(Types.DOUBLE); // Z

            // Unused...
            wrapper.write(Types.DOUBLE, 0D); // Delta movement X
            wrapper.write(Types.DOUBLE, 0D); // Delta movement Y
            wrapper.write(Types.DOUBLE, 0D); // Delta movement Z

            // Unpack y and x rot
            final float yaw = wrapper.read(Types.BYTE) * 360F / 256F;
            final float pitch = wrapper.read(Types.BYTE) * 360F / 256F;
            wrapper.write(Types.FLOAT, yaw);
            wrapper.write(Types.FLOAT, pitch);

            final EntityTracker1_21_2 tracker = tracker(wrapper.user());
            final EntityTracker1_21_2.BoatEntity trackedEntity = tracker.trackedBoatEntity(entityId);
            if (trackedEntity == null) {
                return;
            }
            trackedEntity.setPosition(x, y, z);
            trackedEntity.setRotation(yaw, pitch);
        });
        protocol.registerClientbound(ClientboundPackets1_21.MOVE_ENTITY_POS, wrapper -> storeEntityPositionRotation(wrapper, true, false));
        protocol.registerClientbound(ClientboundPackets1_21.MOVE_ENTITY_POS_ROT, wrapper -> storeEntityPositionRotation(wrapper, true, true));
        protocol.registerClientbound(ClientboundPackets1_21.MOVE_ENTITY_ROT, wrapper -> storeEntityPositionRotation(wrapper, false, true));

        protocol.registerServerbound(ServerboundPackets1_21_2.MOVE_PLAYER_POS, wrapper -> {
            wrapper.passthrough(Types.DOUBLE); // X
            wrapper.passthrough(Types.DOUBLE); // Y
            wrapper.passthrough(Types.DOUBLE); // Z
            handleOnGround(wrapper);
        });
        protocol.registerServerbound(ServerboundPackets1_21_2.MOVE_PLAYER_POS_ROT, wrapper -> {
            final double x = wrapper.passthrough(Types.DOUBLE); // X
            final double y = wrapper.passthrough(Types.DOUBLE); // Y
            final double z = wrapper.passthrough(Types.DOUBLE); // Z
            final float yaw = wrapper.passthrough(Types.FLOAT); // Yaw
            final float pitch = wrapper.passthrough(Types.FLOAT); // Pitch
            handleOnGround(wrapper);

            final PlayerPositionStorage playerPositionStorage = wrapper.user().get(PlayerPositionStorage.class);
            if (playerPositionStorage != null && playerPositionStorage.checkCaptureNextPlayerPositionPacket()) {
                // Capture this packet and send it after accept teleportation
                final boolean onGround = wrapper.get(Types.BOOLEAN, 0);
                playerPositionStorage.setPlayerPosition(new PlayerPositionStorage.PlayerPosition(x, y, z, yaw, pitch, onGround));
                wrapper.cancel();
            }
        });
        protocol.registerServerbound(ServerboundPackets1_21_2.MOVE_PLAYER_ROT, wrapper -> {
            wrapper.passthrough(Types.FLOAT); // Yaw
            wrapper.passthrough(Types.FLOAT); // Pitch
            handleOnGround(wrapper);

            final ClientVehicleStorage vehicleStorage = wrapper.user().get(ClientVehicleStorage.class);
            if (vehicleStorage == null) {
                return;
            }

            wrapper.sendToServer(Protocol1_21To1_21_2.class);
            wrapper.cancel();
            final PacketWrapper playerInput = wrapper.create(ServerboundPackets1_20_5.PLAYER_INPUT);
            playerInput.write(Types.FLOAT, vehicleStorage.sidewaysMovement());
            playerInput.write(Types.FLOAT, vehicleStorage.forwardMovement());
            playerInput.write(Types.BYTE, vehicleStorage.flags());
            playerInput.sendToServer(Protocol1_21To1_21_2.class);
        });
        protocol.registerServerbound(ServerboundPackets1_21_2.MOVE_PLAYER_STATUS_ONLY, wrapper -> {
            final GroundFlagTracker tracker = wrapper.user().get(GroundFlagTracker.class);
            final boolean prevOnGround = tracker.onGround();
            final boolean prevHorizontalCollision = tracker.horizontalCollision();

            handleOnGround(wrapper);
            if (prevOnGround == tracker.onGround() && prevHorizontalCollision != tracker.horizontalCollision()) {
                // Newer clients will send idle packets even though the on ground state didn't change, ignore them
                wrapper.cancel();
            }
        });
        protocol.registerServerbound(ServerboundPackets1_21_2.ACCEPT_TELEPORTATION, wrapper -> {
            final PlayerPositionStorage playerPositionStorage = wrapper.user().get(PlayerPositionStorage.class);
            if (playerPositionStorage != null && playerPositionStorage.checkHasPlayerPosition()) {
                // Send move player after accept teleportation
                wrapper.sendToServer(Protocol1_21To1_21_2.class);
                wrapper.cancel();
                playerPositionStorage.sendMovePlayerPosRot(wrapper.user());
            }
        });
    }

    private RegistryDataRewriter registryDataRewriter() {
        final CompoundTag enderpearlData = new CompoundTag();
        enderpearlData.putString("scaling", "when_caused_by_living_non_player");
        enderpearlData.putString("message_id", "fall");
        enderpearlData.putFloat("exhaustion", 0.0F);

        final CompoundTag maceSmashData = new CompoundTag();
        maceSmashData.putString("scaling", "when_caused_by_living_non_player");
        maceSmashData.putString("message_id", "mace_smash");
        maceSmashData.putFloat("exhaustion", 0.1F);

        final RegistryDataRewriter registryDataRewriter = new RegistryDataRewriter(protocol);
        registryDataRewriter.addEntries(
            "damage_type",
            new RegistryEntry("minecraft:ender_pearl", enderpearlData),
            new RegistryEntry("minecraft:mace_smash", maceSmashData)
        );
        registryDataRewriter.addEnchantmentEffectRewriter("damage_item", tag -> tag.putString("type", "change_item_damage"));
        return registryDataRewriter;
    }

    private void handleOnGround(final PacketWrapper wrapper) {
        final GroundFlagTracker tracker = wrapper.user().get(GroundFlagTracker.class);

        final short data = wrapper.read(Types.UNSIGNED_BYTE);
        wrapper.write(Types.BOOLEAN, tracker.setOnGround((data & 1) != 0)); // Ignoring horizontal collision data
        tracker.setHorizontalCollision((data & 2) != 0);
    }

    private void storeEntityPositionRotation(final PacketWrapper wrapper, final boolean position, final boolean rotation) {
        final int entityId = wrapper.passthrough(Types.VAR_INT); // Entity id

        final EntityTracker1_21_2 tracker = tracker(wrapper.user());
        final EntityTracker1_21_2.BoatEntity trackedEntity = tracker.trackedBoatEntity(entityId);
        if (trackedEntity == null) {
            return;
        }
        if (position) {
            final double x = wrapper.passthrough(Types.SHORT) / 4096.0; // Delta X
            final double y = wrapper.passthrough(Types.SHORT) / 4096.0; // Delta Y
            final double z = wrapper.passthrough(Types.SHORT) / 4096.0; // Delta Z
            trackedEntity.setPosition(trackedEntity.x() + x, trackedEntity.y() + y, trackedEntity.z() + z);
        }
        if (rotation) {
            final float yaw = wrapper.passthrough(Types.BYTE) * 360.0F / 256.0F;
            final float pitch = wrapper.passthrough(Types.BYTE) * 360.0F / 256.0F;
            trackedEntity.setRotation(yaw, pitch);
        }
    }

    @Override
    protected void registerRewrites() {
        filter().mapDataType(Types1_21_2.ENTITY_DATA_TYPES::byId);

        registerEntityDataTypeHandler(
            Types1_21_2.ENTITY_DATA_TYPES.itemType,
            Types1_21_2.ENTITY_DATA_TYPES.blockStateType,
            Types1_21_2.ENTITY_DATA_TYPES.optionalBlockStateType,
            Types1_21_2.ENTITY_DATA_TYPES.particleType,
            Types1_21_2.ENTITY_DATA_TYPES.particlesType,
            Types1_21_2.ENTITY_DATA_TYPES.componentType,
            Types1_21_2.ENTITY_DATA_TYPES.optionalComponentType
        );
        registerBlockStateHandler(EntityTypes1_21_2.ABSTRACT_MINECART, 11);

        filter().type(EntityTypes1_21_2.ABSTRACT_BOAT).handler((event, data) -> {
            final int dataIndex = event.index();
            // Boat type - now set as own entity type
            // Idea is to remove the old entity, then add a new one and re-apply entity data and passengers
            if (dataIndex > 11) {
                event.setIndex(dataIndex - 1);
                return;
            }
            if (dataIndex != 11) {
                return;
            }
            event.cancel();

            final EntityTracker1_21_2 tracker = tracker(event.user());
            final EntityTracker1_21_2.BoatEntity entity = tracker.trackedBoatEntity(event.entityId());
            if (entity == null) {
                return;
            }

            final boolean isBundling = event.user().get(BundleStateTracker.class).isBundling();
            if (!isBundling) {
                final PacketWrapper bundleStart = PacketWrapper.create(ClientboundPackets1_21_2.BUNDLE_DELIMITER, event.user());
                bundleStart.send(Protocol1_21To1_21_2.class);
            }

            // Remove old entity
            final PacketWrapper removeEntityPacket = PacketWrapper.create(ClientboundPackets1_21_2.REMOVE_ENTITIES, event.user());
            removeEntityPacket.write(Types.VAR_INT_ARRAY_PRIMITIVE, new int[]{event.entityId()});
            removeEntityPacket.send(Protocol1_21To1_21_2.class);

            // Detect correct boat entity type from entity data
            final int boatType = (int) data.getValue();
            EntityType entityType;
            if (tracker.entityType(event.entityId()).isOrHasParent(EntityTypes1_21_2.ABSTRACT_CHEST_BOAT)) {
                entityType = entityTypeFromChestBoatType(boatType);
            } else {
                entityType = entityTypeFromBoatType(boatType);
            }

            // Spawn new entity
            final PacketWrapper spawnEntityPacket = PacketWrapper.create(ClientboundPackets1_21_2.ADD_ENTITY, event.user());
            spawnEntityPacket.write(Types.VAR_INT, event.entityId()); // Entity ID
            spawnEntityPacket.write(Types.UUID, entity.uuid()); // Entity UUID
            spawnEntityPacket.write(Types.VAR_INT, entityType.getId()); // Entity type
            spawnEntityPacket.write(Types.DOUBLE, entity.x()); // X
            spawnEntityPacket.write(Types.DOUBLE, entity.y()); // Y
            spawnEntityPacket.write(Types.DOUBLE, entity.z()); // Z
            spawnEntityPacket.write(Types.BYTE, (byte) Math.floor(entity.pitch() * 256.0F / 360.0F)); // Pitch
            spawnEntityPacket.write(Types.BYTE, (byte) Math.floor(entity.yaw() * 256.0F / 360.0F)); // Yaw
            spawnEntityPacket.write(Types.BYTE, (byte) 0); // Head yaw
            spawnEntityPacket.write(Types.VAR_INT, entity.data()); // Data
            spawnEntityPacket.write(Types.SHORT, (short) 0); // Velocity X
            spawnEntityPacket.write(Types.SHORT, (short) 0); // Velocity Y
            spawnEntityPacket.write(Types.SHORT, (short) 0); // Velocity Z
            spawnEntityPacket.send(Protocol1_21To1_21_2.class);

            // Update tracked entity in storage with new entity type
            tracker.updateBoatType(event.entityId(), entityType);

            // Re-apply entity data previously set
            final PacketWrapper setEntityDataPacket = PacketWrapper.create(ClientboundPackets1_21_2.SET_ENTITY_DATA, event.user());
            setEntityDataPacket.write(Types.VAR_INT, event.entityId());
            setEntityDataPacket.write(Types1_21_2.ENTITY_DATA_LIST, entity.entityData());
            setEntityDataPacket.send(Protocol1_21To1_21_2.class);

            // Re-attach all passengers
            if (entity.passengers() != null) {
                final PacketWrapper setPassengersPacket = PacketWrapper.create(ClientboundPackets1_21_2.SET_PASSENGERS, event.user());
                setPassengersPacket.write(Types.VAR_INT, event.entityId());
                setPassengersPacket.write(Types.VAR_INT_ARRAY_PRIMITIVE, entity.passengers());
                setPassengersPacket.send(Protocol1_21To1_21_2.class);
            }

            if (!isBundling) {
                final PacketWrapper bundleEnd = PacketWrapper.create(ClientboundPackets1_21_2.BUNDLE_DELIMITER, event.user());
                bundleEnd.send(Protocol1_21To1_21_2.class);
            }
        });

        filter().type(EntityTypes1_21_2.SALMON).addIndex(17); // Data type
        filter().type(EntityTypes1_21_2.AGEABLE_WATER_CREATURE).addIndex(16); // Baby

        filter().type(EntityTypes1_21_2.ABSTRACT_ARROW).addIndex(10); // In ground
    }

    @Override
    public void handleEntityData(final int entityId, final List<EntityData> dataList, final UserConnection connection) {
        super.handleEntityData(entityId, dataList, connection);

        final EntityTracker1_21_2 tracker = tracker(connection);
        final EntityType entityType = tracker.entityType(entityId);
        if (entityType == null || !entityType.isOrHasParent(EntityTypes1_21_2.ABSTRACT_BOAT)) {
            return;
        }

        final List<EntityData> entityData = tracker.trackedBoatEntity(entityId).entityData();
        entityData.removeIf(first -> dataList.stream().anyMatch(second -> first.id() == second.id()));
        for (final EntityData data : dataList) {
            final Object value = data.value();
            if (value instanceof Item item) {
                entityData.add(new EntityData(data.id(), data.dataType(), item.copy()));
            } else {
                entityData.add(new EntityData(data.id(), data.dataType(), value));
            }
        }
    }

    private EntityType entityTypeFromBoatType(final int boatType) {
        if (boatType == 0) {
            return EntityTypes1_21_2.OAK_BOAT;
        } else if (boatType == 1) {
            return EntityTypes1_21_2.SPRUCE_BOAT;
        } else if (boatType == 2) {
            return EntityTypes1_21_2.BIRCH_BOAT;
        } else if (boatType == 3) {
            return EntityTypes1_21_2.JUNGLE_BOAT;
        } else if (boatType == 4) {
            return EntityTypes1_21_2.ACACIA_BOAT;
        } else if (boatType == 5) {
            return EntityTypes1_21_2.CHERRY_BOAT;
        } else if (boatType == 6) {
            return EntityTypes1_21_2.DARK_OAK_BOAT;
        } else if (boatType == 7) {
            return EntityTypes1_21_2.MANGROVE_BOAT;
        } else if (boatType == 8) {
            return EntityTypes1_21_2.BAMBOO_RAFT;
        } else {
            return EntityTypes1_21_2.OAK_BOAT; // Fallback
        }
    }

    private EntityType entityTypeFromChestBoatType(final int chestBoatType) {
        if (chestBoatType == 0) {
            return EntityTypes1_21_2.OAK_CHEST_BOAT;
        } else if (chestBoatType == 1) {
            return EntityTypes1_21_2.SPRUCE_CHEST_BOAT;
        } else if (chestBoatType == 2) {
            return EntityTypes1_21_2.BIRCH_CHEST_BOAT;
        } else if (chestBoatType == 3) {
            return EntityTypes1_21_2.JUNGLE_CHEST_BOAT;
        } else if (chestBoatType == 4) {
            return EntityTypes1_21_2.ACACIA_CHEST_BOAT;
        } else if (chestBoatType == 5) {
            return EntityTypes1_21_2.CHERRY_CHEST_BOAT;
        } else if (chestBoatType == 6) {
            return EntityTypes1_21_2.DARK_OAK_CHEST_BOAT;
        } else if (chestBoatType == 7) {
            return EntityTypes1_21_2.MANGROVE_CHEST_BOAT;
        } else if (chestBoatType == 8) {
            return EntityTypes1_21_2.BAMBOO_CHEST_RAFT;
        } else {
            return EntityTypes1_21_2.OAK_CHEST_BOAT; // Fallback
        }
    }

    @Override
    public EntityType typeFromId(final int type) {
        return EntityTypes1_21_2.getTypeFromId(type);
    }

    @Override
    public void onMappingDataLoaded() {
        mapTypes();
    }
}
