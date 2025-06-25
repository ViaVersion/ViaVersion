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
package com.viaversion.viaversion.protocols.v1_21_5to1_21_6.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_6;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_5;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ClientboundPacket1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ClientboundPackets1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ServerboundPackets1_21_5;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.Protocol1_21_5To1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.storage.SneakStorage;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.rewriter.RegistryDataRewriter;
import com.viaversion.viaversion.util.Key;

public final class EntityPacketRewriter1_21_6 extends EntityRewriter<ClientboundPacket1_21_5, Protocol1_21_5To1_21_6> {

    public EntityPacketRewriter1_21_6(final Protocol1_21_5To1_21_6 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerTrackerWithData1_19(ClientboundPackets1_21_5.ADD_ENTITY, EntityTypes1_21_6.FALLING_BLOCK);
        registerSetEntityData(ClientboundPackets1_21_5.SET_ENTITY_DATA);
        registerRemoveEntities(ClientboundPackets1_21_5.REMOVE_ENTITIES);
        registerPlayerAbilities(ClientboundPackets1_21_5.PLAYER_ABILITIES);
        registerGameEvent(ClientboundPackets1_21_5.GAME_EVENT);
        registerLogin1_20_5(ClientboundPackets1_21_5.LOGIN);
        registerRespawn1_20_5(ClientboundPackets1_21_5.RESPAWN);

        final RegistryDataRewriter registryDataRewriter = new RegistryDataRewriter(protocol);
        registryDataRewriter.addHandler("dimension_type", (key, dimension) -> {
            // the client will render clouds if effects aren't set to either the_nether or the_end
            String effects = dimension.getString("effects");
            if (effects != null) {
                effects = Key.stripMinecraftNamespace(effects);
                if ("the_nether".equals(effects) || "the_end".equals(effects)) {
                    return; // don't show clouds
                }
            }
            if (!dimension.contains("cloud_height")) {
                dimension.putInt("cloud_height", 192);
            }
        });
        protocol.registerClientbound(ClientboundConfigurationPackets1_21.REGISTRY_DATA, registryDataRewriter::handle);

        protocol.registerFinishConfiguration(ClientboundConfigurationPackets1_21.FINISH_CONFIGURATION, wrapper -> {
            // send server links dialog as vanilla doesn't show server links by default otherwise
            final PacketWrapper dialogsPacket = PacketWrapper.create(ClientboundConfigurationPackets1_21.REGISTRY_DATA, wrapper.user());
            dialogsPacket.write(Types.STRING, "minecraft:dialog");
            dialogsPacket.write(Types.REGISTRY_ENTRY_ARRAY, new RegistryEntry[]{serverLinksDialog()});
            dialogsPacket.send(Protocol1_21_5To1_21_6.class);
        });

        protocol.appendClientbound(ClientboundPackets1_21_5.RESPAWN, wrapper -> {
            wrapper.user().get(SneakStorage.class).setSneaking(false);
        });

        protocol.registerServerbound(ServerboundPackets1_21_6.PLAYER_COMMAND, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Entity ID
            final int action = wrapper.read(Types.VAR_INT);
            wrapper.write(Types.VAR_INT, action + 2); // press_shift_key and release_shift_key gone
        });

        protocol.registerServerbound(ServerboundPackets1_21_6.PLAYER_INPUT, wrapper -> {
            final byte flags = wrapper.passthrough(Types.BYTE);
            final boolean pressingShift = (flags & 1 << 5) != 0;
            if (wrapper.user().get(SneakStorage.class).setSneaking(pressingShift)) {
                // Send the pressing/releasing shift action
                final PacketWrapper playerCommandPacket = wrapper.create(ServerboundPackets1_21_5.PLAYER_COMMAND);
                playerCommandPacket.write(Types.VAR_INT, tracker(wrapper.user()).clientEntityId());
                playerCommandPacket.write(Types.VAR_INT, pressingShift ? 0 : 1);
                playerCommandPacket.write(Types.VAR_INT, 0); // No data
                playerCommandPacket.sendToServer(Protocol1_21_5To1_21_6.class);
            }
        });
    }

    private RegistryEntry serverLinksDialog() {
        final CompoundTag serverLinksDialog = new CompoundTag();
        serverLinksDialog.putString("type", "minecraft:server_links");

        final CompoundTag title = new CompoundTag();
        title.putString("translate", "menu.server_links.title");
        serverLinksDialog.put("title", title);

        final CompoundTag externalTitle = new CompoundTag();
        externalTitle.putString("translate", "menu.server_links");
        serverLinksDialog.put("external_title", externalTitle);

        final CompoundTag exitAction = new CompoundTag();
        exitAction.putInt("width", 200);
        final CompoundTag exitActionLabel = new CompoundTag();
        exitActionLabel.putString("translate", "gui.back");
        exitAction.put("label", exitActionLabel);
        serverLinksDialog.put("exit_action", exitAction);

        serverLinksDialog.putInt("columns", 1);
        serverLinksDialog.putInt("button_width", 310);
        return new RegistryEntry("server_links", serverLinksDialog);
    }

    @Override
    protected void registerRewrites() {
        final EntityDataTypes1_21_5 entityDataTypes = VersionedTypes.V1_21_6.entityDataTypes;
        filter().mapDataType(entityDataTypes::byId);
        registerEntityDataTypeHandler(
            entityDataTypes.itemType,
            entityDataTypes.blockStateType,
            entityDataTypes.optionalBlockStateType,
            entityDataTypes.particleType,
            entityDataTypes.particlesType,
            entityDataTypes.componentType,
            entityDataTypes.optionalComponentType
        );

        filter().type(EntityTypes1_21_6.HANGING_ENTITY).addIndex(8); // Direction
    }

    @Override
    public void onMappingDataLoaded() {
        mapTypes();
    }

    @Override
    public EntityType typeFromId(final int type) {
        return EntityTypes1_21_6.getTypeFromId(type);
    }
}
