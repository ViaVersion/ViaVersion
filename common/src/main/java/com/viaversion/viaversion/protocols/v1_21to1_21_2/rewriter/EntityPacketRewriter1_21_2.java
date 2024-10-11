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
package com.viaversion.viaversion.protocols.v1_21to1_21_2.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_2;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_21;
import com.viaversion.viaversion.api.type.types.version.Types1_21_2;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPacket1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.Protocol1_21To1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ServerboundPackets1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.storage.ClientVehicleStorage;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.rewriter.RegistryDataRewriter;

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

        protocol.registerClientbound(ClientboundConfigurationPackets1_21.FINISH_CONFIGURATION, wrapper -> {
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
            trackWorldDataByKey1_20_5(wrapper.user(), dimensionId, world);

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
        });

        protocol.registerClientbound(ClientboundPackets1_21.SET_PASSENGERS, wrapper -> {
            final int vehicleId = wrapper.passthrough(Types.VAR_INT);
            final int[] passengerIds = wrapper.passthrough(Types.VAR_INT_ARRAY_PRIMITIVE);
            final ClientVehicleStorage storage = wrapper.user().get(ClientVehicleStorage.class);
            if (storage != null && vehicleId == storage.vehicleId()) {
                wrapper.user().remove(ClientVehicleStorage.class);
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
            // Filter them appropriately
            if (!wrapper.user().has(ClientVehicleStorage.class)) {
                wrapper.cancel();
                return;
            }

            final byte flags = wrapper.read(Types.BYTE);
            final boolean left = (flags & 1 << 2) != 0;
            final boolean right = (flags & 1 << 3) != 0;
            wrapper.write(Types.FLOAT, left ? IMPULSE : (right ? -IMPULSE : 0F));

            final boolean forward = (flags & 1 << 0) != 0;
            final boolean backward = (flags & 1 << 1) != 0;
            wrapper.write(Types.FLOAT, forward ? IMPULSE : (backward ? -IMPULSE : 0F));

            byte updatedFlags = 0;
            if ((flags & 1 << 4) != 0) {
                updatedFlags |= 1;
            }
            if ((flags & 1 << 5) != 0) {
                updatedFlags |= 2;
            }
            wrapper.write(Types.BYTE, updatedFlags);
        });

        protocol.registerClientbound(ClientboundPackets1_21.TELEPORT_ENTITY, ClientboundPackets1_21_2.ENTITY_POSITION_SYNC, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Entity ID

            wrapper.passthrough(Types.DOUBLE); // X
            wrapper.passthrough(Types.DOUBLE); // Y
            wrapper.passthrough(Types.DOUBLE); // Z

            // Unused...
            wrapper.write(Types.DOUBLE, 0D); // Delta movement X
            wrapper.write(Types.DOUBLE, 0D); // Delta movement Y
            wrapper.write(Types.DOUBLE, 0D); // Delta movement Z

            // Unpack y and x rot
            final byte yaw = wrapper.read(Types.BYTE);
            final byte pitch = wrapper.read(Types.BYTE);
            wrapper.write(Types.FLOAT, yaw * 360F / 256F);
            wrapper.write(Types.FLOAT, pitch * 360F / 256F);
        });

        protocol.registerServerbound(ServerboundPackets1_21_2.MOVE_PLAYER_POS, wrapper -> {
            wrapper.passthrough(Types.DOUBLE); // X
            wrapper.passthrough(Types.DOUBLE); // Y
            wrapper.passthrough(Types.DOUBLE); // Z
            readOnGround(wrapper);
        });
        protocol.registerServerbound(ServerboundPackets1_21_2.MOVE_PLAYER_POS_ROT, wrapper -> {
            wrapper.passthrough(Types.DOUBLE); // X
            wrapper.passthrough(Types.DOUBLE); // Y
            wrapper.passthrough(Types.DOUBLE); // Z
            wrapper.passthrough(Types.FLOAT); // Yaw
            wrapper.passthrough(Types.FLOAT); // Pitch
            readOnGround(wrapper);
        });
        protocol.registerServerbound(ServerboundPackets1_21_2.MOVE_PLAYER_ROT, wrapper -> {
            wrapper.passthrough(Types.FLOAT); // Yaw
            wrapper.passthrough(Types.FLOAT); // Pitch
            readOnGround(wrapper);
        });
        protocol.registerServerbound(ServerboundPackets1_21_2.MOVE_PLAYER_STATUS_ONLY, this::readOnGround);
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

    private void readOnGround(final PacketWrapper wrapper) {
        final short data = wrapper.read(Types.UNSIGNED_BYTE);
        wrapper.write(Types.BOOLEAN, (data & 1) != 0); // On ground, ignoring horizontal collision data
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

        filter().type(EntityTypes1_21_2.ABSTRACT_BOAT).removeIndex(11); // Goodbye boat type

        filter().type(EntityTypes1_21_2.SALMON).addIndex(17); // Data type
        filter().type(EntityTypes1_21_2.DOLPHIN).addIndex(16); // Baby
        filter().type(EntityTypes1_21_2.GLOW_SQUID).addIndex(16); // Baby
        filter().type(EntityTypes1_21_2.SQUID).addIndex(16); // Baby

        filter().type(EntityTypes1_21_2.ABSTRACT_ARROW).addIndex(10); // In ground
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
