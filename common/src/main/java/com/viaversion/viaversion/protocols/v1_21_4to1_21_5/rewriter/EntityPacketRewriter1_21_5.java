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
package com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.api.minecraft.WolfVariant;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_5;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.Protocol1_21_4To1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ClientboundPackets1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.storage.MessageIndexStorage;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPacket1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.rewriter.entitydata.EntityDataHandlerEvent;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class EntityPacketRewriter1_21_5 extends EntityRewriter<ClientboundPacket1_21_2, Protocol1_21_4To1_21_5> {
    private static final int ATTACK_BLOCKED_ENTITY_EVENT = 29;
    private static final int SHIELD_DISABLED_ENTITY_EVENT = 30;
    private static final int SADDLE_ITEM_ID = 800;
    private static final byte SADDLE_EQUIPMENT_SLOT = 7;

    public EntityPacketRewriter1_21_5(final Protocol1_21_4To1_21_5 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerTrackerWithData1_19(ClientboundPackets1_21_2.ADD_ENTITY, EntityTypes1_21_5.FALLING_BLOCK);
        registerSetEntityData(ClientboundPackets1_21_2.SET_ENTITY_DATA);
        registerRemoveEntities(ClientboundPackets1_21_2.REMOVE_ENTITIES);
        registerPlayerAbilities(ClientboundPackets1_21_2.PLAYER_ABILITIES);
        registerGameEvent(ClientboundPackets1_21_2.GAME_EVENT);

        // No more special experience orb add packet
        protocol.registerClientbound(ClientboundPackets1_21_2.ADD_EXPERIENCE_ORB, ClientboundPackets1_21_5.ADD_ENTITY, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Entity ID
            wrapper.write(Types.UUID, UUID.randomUUID()); // UUID...
            wrapper.write(Types.VAR_INT, EntityTypes1_21_5.EXPERIENCE_ORB.getId()); // Type
            wrapper.passthrough(Types.DOUBLE); // X
            wrapper.passthrough(Types.DOUBLE); // Y
            wrapper.passthrough(Types.DOUBLE); // Z
            wrapper.write(Types.BYTE, (byte) 0); // Pitch
            wrapper.write(Types.BYTE, (byte) 0); // Yaw
            wrapper.write(Types.BYTE, (byte) 0); // Head Yaw
            wrapper.passthroughAndMap(Types.SHORT, Types.VAR_INT); // Data
            wrapper.write(Types.SHORT, (short) 0); // Velocity
            wrapper.write(Types.SHORT, (short) 0);
            wrapper.write(Types.SHORT, (short) 0);
        });

        protocol.registerFinishConfiguration(ClientboundConfigurationPackets1_21.FINISH_CONFIGURATION, wrapper -> {
            // Old registries, but now also modifiable
            sendEntityVariants(wrapper.user(), "minecraft:frog_variant", "frog", true, "temperate", "warm", "cold");
            sendEntityVariants(wrapper.user(), "minecraft:cat_variant", "cat", false, "tabby", "black", "red", "siamese", "british_shorthair", "calico", "persian", "ragdoll", "white", "jellie", "all_black");
            // New variants
            sendEntityVariants(wrapper.user(), "minecraft:pig_variant", "pig", true, "temperate");
            sendEntityVariants(wrapper.user(), "minecraft:cow_variant", "cow", true, "temperate");
            sendEntityVariants(wrapper.user(), "minecraft:chicken_variant", "chicken", true, "temperate");

            // Wolf sound variants
            final PacketWrapper wolfSoundVariantsPacket = PacketWrapper.create(ClientboundConfigurationPackets1_21.REGISTRY_DATA, wrapper.user());
            wolfSoundVariantsPacket.write(Types.STRING, "minecraft:wolf_sound_variant");
            wolfSoundVariantsPacket.write(Types.REGISTRY_ENTRY_ARRAY, new RegistryEntry[]{wolfSoundVariant()});
            wolfSoundVariantsPacket.send(Protocol1_21_4To1_21_5.class);
        });

        registerRespawn1_20_5(ClientboundPackets1_21_2.RESPAWN);
        registerLogin1_20_5(ClientboundPackets1_21_2.LOGIN);
        protocol.appendClientbound(ClientboundPackets1_21_2.LOGIN, wrapper -> {
            wrapper.user().get(MessageIndexStorage.class).setIndex(0);
        });

        protocol.registerClientbound(ClientboundPackets1_21_2.SET_PLAYER_TEAM, wrapper -> {
            wrapper.passthrough(Types.STRING); // Team Name
            final byte action = wrapper.passthrough(Types.BYTE); // Mode
            if (action == 0 || action == 2) {
                wrapper.passthrough(Types.TAG); // Display Name
                wrapper.passthrough(Types.BYTE); // Flags

                final String nametagVisibility = wrapper.read(Types.STRING);
                final String collisionRule = wrapper.read(Types.STRING);
                wrapper.write(Types.VAR_INT, visibilityId(nametagVisibility));
                wrapper.write(Types.VAR_INT, collisionId(collisionRule));

                wrapper.passthrough(Types.VAR_INT); // Color
                wrapper.passthrough(Types.TAG); // Prefix
                wrapper.passthrough(Types.TAG); // Suffix
            }
        });

        protocol.registerClientbound(ClientboundPackets1_21_2.ENTITY_EVENT, wrapper -> {
            final int entityId = wrapper.read(Types.INT);
            final byte event = wrapper.read(Types.BYTE);
            if (event == ATTACK_BLOCKED_ENTITY_EVENT) {
                playShieldSound(wrapper, entityId, 1273, 1F);
                return;
            } else if (event == SHIELD_DISABLED_ENTITY_EVENT) {
                playShieldSound(wrapper, entityId, 1274, 0.8F);
                return;
            }

            wrapper.write(Types.INT, entityId);
            wrapper.write(Types.BYTE, event);
        });
    }

    private void playShieldSound(final PacketWrapper wrapper, final int entityId, final int soundId, final float volume) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        wrapper.setPacketType(ClientboundPackets1_21_5.SOUND_ENTITY);
        wrapper.write(Types.SOUND_EVENT, Holder.of(soundId));
        wrapper.write(Types.VAR_INT, 7); // Assume player sound source
        wrapper.write(Types.VAR_INT, entityId);
        wrapper.write(Types.FLOAT, volume); // Volume
        wrapper.write(Types.FLOAT, 0.8F + random.nextFloat() * 0.4F); // Pitch
        wrapper.write(Types.LONG, random.nextLong()); // Seed
    }

    private RegistryEntry wolfSoundVariant() {
        final CompoundTag classicWolfSoundVariant = new CompoundTag();
        classicWolfSoundVariant.putString("ambient_sound", "entity.wolf.ambient");
        classicWolfSoundVariant.putString("death_sound", "entity.wolf.death");
        classicWolfSoundVariant.putString("growl_sound", "entity.wolf.growl");
        classicWolfSoundVariant.putString("hurt_sound", "entity.wolf.hurt");
        classicWolfSoundVariant.putString("pant_sound", "entity.wolf.pant");
        classicWolfSoundVariant.putString("whine_sound", "entity.wolf.whine");
        return new RegistryEntry("classic", classicWolfSoundVariant);
    }

    private int collisionId(final String collisionRule) {
        return switch (collisionRule) {
            case "always" -> 0;
            case "never" -> 1;
            case "pushOtherTeams" -> 2;
            case "pushOwnTeam" -> 3;
            default -> 0;
        };
    }

    private int visibilityId(final String visibilityRule) {
        return switch (visibilityRule) {
            case "always" -> 0;
            case "never" -> 1;
            case "hideForOtherTeams" -> 2;
            case "hideForOwnTeam" -> 3;
            default -> 0;
        };
    }

    private void sendEntityVariants(final UserConnection connection, final String key, final String entityName, final boolean suffixedWithOwnName, final String... entryKeys) {
        final PacketWrapper variantsPacket = PacketWrapper.create(ClientboundConfigurationPackets1_21.REGISTRY_DATA, connection);
        variantsPacket.write(Types.STRING, key);

        final RegistryEntry[] entries = new RegistryEntry[entryKeys.length];
        for (int i = 0; i < entryKeys.length; i++) {
            final CompoundTag tag = new CompoundTag();
            String assetId = "entity/" + entityName + "/" + entryKeys[i];
            if (suffixedWithOwnName) {
                // Because frogs have that...
                assetId = assetId + "_" + entityName;
            }
            tag.putString("asset_id", assetId);
            entries[i] = new RegistryEntry(entryKeys[i], tag);
        }

        variantsPacket.write(Types.REGISTRY_ENTRY_ARRAY, entries);
        variantsPacket.send(Protocol1_21_4To1_21_5.class);
    }

    @Override
    protected void registerRewrites() {
        filter().handler((event, data) -> {
            final int id = data.dataType().typeId();
            if (id == VersionedTypes.V1_21_4.entityDataTypes.wolfVariantType.typeId()) {
                final Holder<WolfVariant> wolfVariant = data.value();
                data.setTypeAndValue(VersionedTypes.V1_21_5.entityDataTypes.wolfVariantType, wolfVariant.hasId() ? wolfVariant.id() : 0);
                return;
            }

            int mappedId = id;
            if (mappedId >= VersionedTypes.V1_21_5.entityDataTypes.cowVariantType.typeId()) {
                mappedId++;
            }
            if (mappedId >= VersionedTypes.V1_21_5.entityDataTypes.wolfSoundVariantType.typeId()) {
                mappedId++;
            }
            if (mappedId >= VersionedTypes.V1_21_5.entityDataTypes.pigVariantType.typeId()) {
                mappedId++;
            }
            if (mappedId >= VersionedTypes.V1_21_5.entityDataTypes.chickenVariantType.typeId()) {
                mappedId++;
            }
            data.setDataType(VersionedTypes.V1_21_5.entityDataTypes.byId(mappedId));
        });

        registerEntityDataTypeHandler(
            VersionedTypes.V1_21_5.entityDataTypes.itemType,
            VersionedTypes.V1_21_5.entityDataTypes.blockStateType,
            VersionedTypes.V1_21_5.entityDataTypes.optionalBlockStateType,
            VersionedTypes.V1_21_5.entityDataTypes.particleType,
            VersionedTypes.V1_21_5.entityDataTypes.particlesType,
            VersionedTypes.V1_21_5.entityDataTypes.componentType,
            VersionedTypes.V1_21_5.entityDataTypes.optionalComponentType
        );

        // Minecarts finally have the block state data type
        filter().type(EntityTypes1_21_5.ABSTRACT_MINECART).index(11).handler((event, data) -> {
            final int state = (int) data.getValue();
            final int mappedBlockState = protocol.getMappingData().getNewBlockStateId(state);
            if (mappedBlockState == 0) {
                event.cancel();
                return;
            }

            data.setTypeAndValue(VersionedTypes.V1_21_5.entityDataTypes.optionalBlockStateType, mappedBlockState);
        });
        filter().type(EntityTypes1_21_5.ABSTRACT_MINECART).removeIndex(13); // Custom display

        filter().type(EntityTypes1_21_5.MOOSHROOM).index(17).handler(((event, data) -> {
            final String typeName = data.value();
            final int typeId = typeName.equals("red") ? 0 : 1;
            data.setTypeAndValue(VersionedTypes.V1_21_5.entityDataTypes.varIntType, typeId);
        }));

        // Removed saddles
        filter().type(EntityTypes1_21_5.PIG).index(17).handler((event, data) -> {
            final boolean saddled = data.value();
            sendSaddleEquipment(event, saddled);
        });
        filter().type(EntityTypes1_21_5.PIG).removeIndex(17);
        filter().type(EntityTypes1_21_5.STRIDER).index(19).handler((event, data) -> {
            event.cancel();
            final boolean saddled = data.value();
            sendSaddleEquipment(event, saddled);
        });
        filter().type(EntityTypes1_21_5.ABSTRACT_HORSE).index(17).handler((event, data) -> {
            final byte flags = data.value();
            sendSaddleEquipment(event, (flags & 0x04) != 0);
        });

        filter().type(EntityTypes1_21_5.DOLPHIN).removeIndex(17); // Treasure pos

        filter().type(EntityTypes1_21_5.TURTLE).cancel(22); // Travelling
        filter().type(EntityTypes1_21_5.TURTLE).cancel(21); // Going home
        filter().type(EntityTypes1_21_5.TURTLE).cancel(20); // Travel pos
        filter().type(EntityTypes1_21_5.TURTLE).removeIndex(17); // Home pos
    }

    private void sendSaddleEquipment(final EntityDataHandlerEvent event, final boolean saddled) {
        final PacketWrapper equipmentPacket = PacketWrapper.create(ClientboundPackets1_21_5.SET_EQUIPMENT, event.user());
        equipmentPacket.write(Types.VAR_INT, event.entityId());
        equipmentPacket.write(Types.BYTE, SADDLE_EQUIPMENT_SLOT);
        equipmentPacket.write(VersionedTypes.V1_21_5.item, saddled ? new StructuredItem(SADDLE_ITEM_ID, 1) : StructuredItem.empty());
        equipmentPacket.send(Protocol1_21_4To1_21_5.class);
    }

    @Override
    public void onMappingDataLoaded() {
        mapTypes();
    }

    @Override
    public EntityType typeFromId(final int type) {
        return EntityTypes1_21_5.getTypeFromId(type);
    }
}
