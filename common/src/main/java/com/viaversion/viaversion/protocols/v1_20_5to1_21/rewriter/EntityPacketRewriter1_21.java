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
package com.viaversion.viaversion.protocols.v1_20_5to1_21.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.PaintingVariant;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_5;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityDataType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.Enchantments1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundPacket1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.Protocol1_20_5To1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.data.Paintings1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.storage.EfficiencyAttributeStorage;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.storage.PlayerPositionStorage;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.storage.WolfVariantRegistryMarker;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.rewriter.RegistryDataRewriter;
import com.viaversion.viaversion.util.Key;

public final class EntityPacketRewriter1_21 extends EntityRewriter<ClientboundPacket1_20_5, Protocol1_20_5To1_21> {

    public EntityPacketRewriter1_21(final Protocol1_20_5To1_21 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerTrackerWithData1_19(ClientboundPackets1_20_5.ADD_ENTITY, EntityTypes1_20_5.FALLING_BLOCK);
        registerSetEntityData(ClientboundPackets1_20_5.SET_ENTITY_DATA);
        registerRemoveEntities(ClientboundPackets1_20_5.REMOVE_ENTITIES);

        final RegistryDataRewriter registryDataRewriter = new RegistryDataRewriter(protocol);
        final CompoundTag campfireDamageType = new CompoundTag();
        campfireDamageType.putString("scaling", "when_caused_by_living_non_player");
        campfireDamageType.putString("message_id", "inFire");
        campfireDamageType.putFloat("exhaustion", 0.1F);
        registryDataRewriter.addEntries("damage_type", new RegistryEntry("minecraft:campfire", campfireDamageType));
        protocol.registerClientbound(ClientboundConfigurationPackets1_20_5.REGISTRY_DATA, wrapper -> {
            final String registryKey = Key.stripMinecraftNamespace(wrapper.passthrough(Types.STRING));
            RegistryEntry[] entries = wrapper.read(Types.REGISTRY_ENTRY_ARRAY);
            entries = registryDataRewriter.handle(wrapper.user(), registryKey, entries);
            wrapper.write(Types.REGISTRY_ENTRY_ARRAY, entries);
            if (registryKey.equals("wolf_variant")) {
                wrapper.user().put(new WolfVariantRegistryMarker());
            }
        });

        protocol.registerFinishConfiguration(ClientboundConfigurationPackets1_20_5.FINISH_CONFIGURATION, wrapper -> {
            // Add new registries
            final PacketWrapper paintingRegistryPacket = wrapper.create(ClientboundConfigurationPackets1_20_5.REGISTRY_DATA);
            paintingRegistryPacket.write(Types.STRING, "minecraft:painting_variant");
            final RegistryEntry[] paintingsRegistry = new RegistryEntry[Paintings1_20_5.PAINTINGS.length];
            for (int i = 0; i < Paintings1_20_5.PAINTINGS.length; i++) {
                final PaintingVariant painting = Paintings1_20_5.PAINTINGS[i];
                final CompoundTag tag = new CompoundTag();
                tag.putInt("width", painting.width());
                tag.putInt("height", painting.height());
                tag.putString("asset_id", painting.assetId());
                paintingsRegistry[i] = new RegistryEntry(painting.assetId(), tag);
            }
            paintingRegistryPacket.write(Types.REGISTRY_ENTRY_ARRAY, paintingsRegistry);
            paintingRegistryPacket.send(Protocol1_20_5To1_21.class);

            final PacketWrapper enchantmentRegistryPacket = wrapper.create(ClientboundConfigurationPackets1_20_5.REGISTRY_DATA);
            enchantmentRegistryPacket.write(Types.STRING, "minecraft:enchantment");
            final RegistryEntry[] enchantmentRegistry = new RegistryEntry[Enchantments1_20_5.ENCHANTMENTS.size()];
            for (int i = 0; i < Enchantments1_20_5.ENCHANTMENTS.size(); i++) {
                final String key = Enchantments1_20_5.idToKey(i);
                final CompoundTag tag = protocol.getMappingData().enchantment(i);
                enchantmentRegistry[i] = new RegistryEntry(key, tag);
            }
            enchantmentRegistryPacket.write(Types.REGISTRY_ENTRY_ARRAY, enchantmentRegistry);
            enchantmentRegistryPacket.send(Protocol1_20_5To1_21.class);

            final PacketWrapper jukeboxSongsPacket = wrapper.create(ClientboundConfigurationPackets1_20_5.REGISTRY_DATA);
            jukeboxSongsPacket.write(Types.STRING, "minecraft:jukebox_song");
            jukeboxSongsPacket.write(Types.REGISTRY_ENTRY_ARRAY, protocol.getMappingData().jukeboxSongs());
            jukeboxSongsPacket.send(Protocol1_20_5To1_21.class);

            if (!wrapper.user().has(WolfVariantRegistryMarker.class)) {
                createDefaultWolfVariantRegistryDataPacket(wrapper).send(Protocol1_20_5To1_21.class);
            }
        });

        registerLogin1_20_5(ClientboundPackets1_20_5.LOGIN);
        protocol.appendClientbound(ClientboundPackets1_20_5.LOGIN, wrapper -> {
            wrapper.user().get(EfficiencyAttributeStorage.class).onLoginSent(wrapper.get(Types.INT, 0), wrapper.user());
        });

        protocol.registerClientbound(ClientboundPackets1_20_5.RESPAWN, wrapper -> {
            final int dimensionId = wrapper.passthrough(Types.VAR_INT);
            final String world = wrapper.passthrough(Types.STRING);
            trackWorldDataByKey1_20_5(wrapper.user(), dimensionId, world); // Tracks world height and name for chunk data and entity (un)tracking

            // Resend attribute modifiers from items
            wrapper.user().get(EfficiencyAttributeStorage.class).onRespawn(wrapper.user());

            wrapper.user().put(new PlayerPositionStorage());
        });

        // Tracking player position and on ground for block interactions, rotations is kept from the interaction packet
        protocol.registerServerbound(ServerboundPackets1_20_5.MOVE_PLAYER_POS, wrapper -> {
            if (Via.getConfig().fix1_21PlacementRotation()) {
                storePosition(wrapper);
                storeOnGround(wrapper);
            }
        });
        protocol.registerServerbound(ServerboundPackets1_20_5.MOVE_PLAYER_ROT, wrapper -> {
            if (Via.getConfig().fix1_21PlacementRotation()) {
                wrapper.passthrough(Types.FLOAT); // Yaw
                wrapper.passthrough(Types.FLOAT); // Pitch
                storeOnGround(wrapper);
            }
        });
        protocol.registerServerbound(ServerboundPackets1_20_5.MOVE_PLAYER_POS_ROT, wrapper -> {
            if (Via.getConfig().fix1_21PlacementRotation()) {
                storePosition(wrapper);
                wrapper.passthrough(Types.FLOAT); // Yaw
                wrapper.passthrough(Types.FLOAT); // Pitch
                storeOnGround(wrapper);
            }
        });
        protocol.registerServerbound(ServerboundPackets1_20_5.MOVE_PLAYER_STATUS_ONLY, wrapper -> {
            if (Via.getConfig().fix1_21PlacementRotation()) {
                storeOnGround(wrapper);
            }
        });
    }

    private static PacketWrapper createDefaultWolfVariantRegistryDataPacket(final PacketWrapper wrapper) {
        final PacketWrapper wolfVariantPacket = wrapper.create(ClientboundConfigurationPackets1_20_5.REGISTRY_DATA);
        wolfVariantPacket.write(Types.STRING, "minecraft:wolf_variant");
        wolfVariantPacket.write(Types.REGISTRY_ENTRY_ARRAY, getDefaultWolfVariantRegistryEntries());
        return wolfVariantPacket;
    }

    private static RegistryEntry[] getDefaultWolfVariantRegistryEntries() {
        final CompoundTag paleWolfVariant = new CompoundTag();
        paleWolfVariant.putString("wild_texture", "minecraft:entity/wolf/wolf");
        paleWolfVariant.putString("angry_texture", "minecraft:entity/wolf/wolf_angry");
        paleWolfVariant.put("biomes", new ListTag<>(StringTag.class));
        paleWolfVariant.putString("tame_texture", "minecraft:entity/wolf/wolf_tame");
        return new RegistryEntry[]{
            new RegistryEntry("minecraft:pale", paleWolfVariant),
        };
    }

    private void storePosition(final PacketWrapper wrapper) {
        final double x = wrapper.passthrough(Types.DOUBLE);
        final double y = wrapper.passthrough(Types.DOUBLE);
        final double z = wrapper.passthrough(Types.DOUBLE);
        wrapper.user().get(PlayerPositionStorage.class).setPosition(x, y, z);
    }

    private void storeOnGround(final PacketWrapper wrapper) {
        final boolean onGround = wrapper.passthrough(Types.BOOLEAN);
        wrapper.user().get(PlayerPositionStorage.class).setOnGround(onGround);
    }

    @Override
    protected void registerRewrites() {
        filter().handler((event, data) -> {
            final EntityDataType type = data.dataType();
            if (type == VersionedTypes.V1_20_5.entityDataTypes.wolfVariantType) {
                final int variant = data.value();
                data.setTypeAndValue(VersionedTypes.V1_21.entityDataTypes.wolfVariantType, Holder.of(variant));
            } else if (type == VersionedTypes.V1_20_5.entityDataTypes.paintingVariantType) {
                final int variant = data.value();
                data.setTypeAndValue(VersionedTypes.V1_21.entityDataTypes.paintingVariantType, Holder.of(variant));
            } else {
                data.setDataType(VersionedTypes.V1_21.entityDataTypes.byId(type.typeId()));
            }
        });
        registerEntityDataTypeHandler(
            VersionedTypes.V1_21.entityDataTypes.itemType,
            VersionedTypes.V1_21.entityDataTypes.blockStateType,
            VersionedTypes.V1_21.entityDataTypes.optionalBlockStateType,
            VersionedTypes.V1_21.entityDataTypes.particleType,
            VersionedTypes.V1_21.entityDataTypes.particlesType,
            VersionedTypes.V1_21.entityDataTypes.componentType,
            VersionedTypes.V1_21.entityDataTypes.optionalComponentType
        );
        registerBlockStateHandler(EntityTypes1_20_5.ABSTRACT_MINECART, 11);
    }

    @Override
    public EntityType typeFromId(final int type) {
        return EntityTypes1_20_5.getTypeFromId(type);
    }
}
