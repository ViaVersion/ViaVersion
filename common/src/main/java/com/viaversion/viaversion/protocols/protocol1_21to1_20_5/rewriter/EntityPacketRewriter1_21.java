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
package com.viaversion.viaversion.protocols.protocol1_21to1_20_5.rewriter;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_5;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_20_5;
import com.viaversion.viaversion.api.type.types.version.Types1_21;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data.Enchantments1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.packet.ClientboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.packet.ClientboundPacket1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.packet.ClientboundPackets1_20_5;
import com.viaversion.viaversion.protocols.protocol1_21to1_20_5.Protocol1_21To1_20_5;
import com.viaversion.viaversion.protocols.protocol1_21to1_20_5.data.Paintings1_20_5;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.util.Key;

public final class EntityPacketRewriter1_21 extends EntityRewriter<ClientboundPacket1_20_5, Protocol1_21To1_20_5> {

    public EntityPacketRewriter1_21(final Protocol1_21To1_20_5 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerTrackerWithData1_19(ClientboundPackets1_20_5.SPAWN_ENTITY, EntityTypes1_20_5.FALLING_BLOCK);
        registerMetadataRewriter(ClientboundPackets1_20_5.ENTITY_METADATA, Types1_20_5.METADATA_LIST, Types1_21.METADATA_LIST);
        registerRemoveEntities(ClientboundPackets1_20_5.REMOVE_ENTITIES);

        protocol.registerClientbound(ClientboundConfigurationPackets1_20_5.REGISTRY_DATA, wrapper -> {
            final String type = Key.stripMinecraftNamespace(wrapper.passthrough(Type.STRING));
            final RegistryEntry[] entries = wrapper.passthrough(Type.REGISTRY_ENTRY_ARRAY);
            if (type.equals("damage_type")) {
                // Add required damage type
                final CompoundTag campfireDamageType = new CompoundTag();
                campfireDamageType.putString("scaling", "when_caused_by_living_non_player");
                campfireDamageType.putString("message_id", "inFire");
                campfireDamageType.putFloat("exhaustion", 0.1F);
                wrapper.set(Type.REGISTRY_ENTRY_ARRAY, 0, addRegistryEntries(entries, new RegistryEntry("minecraft:campfire", campfireDamageType)));
            } else {
                handleRegistryData1_20_5(wrapper.user(), type, entries);
            }
        });

        protocol.registerClientbound(ClientboundConfigurationPackets1_20_5.FINISH_CONFIGURATION, wrapper -> {
            // Add new registries
            final PacketWrapper paintingRegistryPacket = wrapper.create(ClientboundConfigurationPackets1_20_5.REGISTRY_DATA);
            paintingRegistryPacket.write(Type.STRING, "minecraft:painting_variant");
            final RegistryEntry[] paintingsRegistry = new RegistryEntry[Paintings1_20_5.PAINTINGS.length];
            for (int i = 0; i < Paintings1_20_5.PAINTINGS.length; i++) {
                final Paintings1_20_5.PaintingVariant painting = Paintings1_20_5.PAINTINGS[i];
                final CompoundTag tag = new CompoundTag();
                tag.putInt("width", painting.width());
                tag.putInt("height", painting.height());
                tag.putString("asset_id", painting.key());
                paintingsRegistry[i] = new RegistryEntry(painting.key(), tag);
            }
            paintingRegistryPacket.write(Type.REGISTRY_ENTRY_ARRAY, paintingsRegistry);
            paintingRegistryPacket.send(Protocol1_21To1_20_5.class);

            final PacketWrapper enchantmentRegistryPacket = wrapper.create(ClientboundConfigurationPackets1_20_5.REGISTRY_DATA);
            enchantmentRegistryPacket.write(Type.STRING, "minecraft:enchantment");
            final RegistryEntry[] enchantmentRegistry = new RegistryEntry[Enchantments1_20_5.ENCHANTMENTS.size()];
            for (int i = 0; i < Enchantments1_20_5.ENCHANTMENTS.size(); i++) {
                final String key = Enchantments1_20_5.idToKey(i);
                final CompoundTag tag = protocol.getMappingData().enchantment(i);
                enchantmentRegistry[i] = new RegistryEntry(key, tag);
            }
            enchantmentRegistryPacket.write(Type.REGISTRY_ENTRY_ARRAY, enchantmentRegistry);
            enchantmentRegistryPacket.send(Protocol1_21To1_20_5.class);
        });

        protocol.registerClientbound(ClientboundPackets1_20_5.JOIN_GAME, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // Entity id
                map(Type.BOOLEAN); // Hardcore
                map(Type.STRING_ARRAY); // World List
                map(Type.VAR_INT); // Max players
                map(Type.VAR_INT); // View distance
                map(Type.VAR_INT); // Simulation distance
                map(Type.BOOLEAN); // Reduced debug info
                map(Type.BOOLEAN); // Show death screen
                map(Type.BOOLEAN); // Limited crafting
                map(Type.VAR_INT); // Dimension id
                map(Type.STRING); // World
                handler(worldDataTrackerHandlerByKey1_20_5(3));
                handler(playerTrackerHandler());
            }
        });

        protocol.registerClientbound(ClientboundPackets1_20_5.RESPAWN, wrapper -> {
            final int dimensionId = wrapper.passthrough(Type.VAR_INT);
            final String world = wrapper.passthrough(Type.STRING);
            trackWorldDataByKey1_20_5(wrapper.user(), dimensionId, world); // Tracks world height and name for chunk data and entity (un)tracking
        });
    }

    @Override
    protected void registerRewrites() {
        filter().mapMetaType(Types1_21.META_TYPES::byId);

        registerMetaTypeHandler(
            Types1_21.META_TYPES.itemType,
            Types1_21.META_TYPES.blockStateType,
            Types1_21.META_TYPES.optionalBlockStateType,
            Types1_21.META_TYPES.particleType,
            Types1_21.META_TYPES.particlesType
        );

        filter().type(EntityTypes1_20_5.MINECART_ABSTRACT).index(11).handler((event, meta) -> {
            final int blockState = meta.value();
            meta.setValue(protocol.getMappingData().getNewBlockStateId(blockState));
        });
    }

    @Override
    public EntityType typeFromId(final int type) {
        return EntityTypes1_20_5.getTypeFromId(type);
    }
}