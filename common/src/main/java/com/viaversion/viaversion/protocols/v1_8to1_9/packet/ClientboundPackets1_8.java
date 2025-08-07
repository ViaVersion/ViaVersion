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
package com.viaversion.viaversion.protocols.v1_8to1_9.packet;

import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;

public enum ClientboundPackets1_8 implements ClientboundPacketType {

    KEEP_ALIVE,
    LOGIN,
    CHAT,
    SET_TIME,
    SET_EQUIPPED_ITEM,
    SET_DEFAULT_SPAWN_POSITION,
    SET_HEALTH,
    RESPAWN,
    PLAYER_POSITION,
    SET_CARRIED_ITEM,
    PLAYER_SLEEP,
    ANIMATE,
    ADD_PLAYER,
    TAKE_ITEM_ENTITY,
    ADD_ENTITY,
    ADD_MOB,
    ADD_PAINTING,
    ADD_EXPERIENCE_ORB,
    SET_ENTITY_MOTION,
    REMOVE_ENTITIES,
    MOVE_ENTITY,
    MOVE_ENTITY_POS,
    MOVE_ENTITY_ROT,
    MOVE_ENTITY_POS_ROT,
    TELEPORT_ENTITY,
    ROTATE_HEAD,
    ENTITY_EVENT,
    SET_ENTITY_LINK,
    SET_ENTITY_DATA,
    UPDATE_MOB_EFFECT,
    REMOVE_MOB_EFFECT,
    SET_EXPERIENCE,
    UPDATE_ATTRIBUTES,
    LEVEL_CHUNK,
    CHUNK_BLOCKS_UPDATE,
    BLOCK_UPDATE,
    BLOCK_EVENT,
    BLOCK_DESTRUCTION,
    MAP_BULK_CHUNK,
    EXPLODE,
    LEVEL_EVENT,
    CUSTOM_SOUND,
    LEVEL_PARTICLES,
    GAME_EVENT,
    ADD_GLOBAL_ENTITY,
    OPEN_SCREEN,
    CONTAINER_CLOSE,
    CONTAINER_SET_SLOT,
    CONTAINER_SET_CONTENT,
    CONTAINER_SET_DATA,
    CONTAINER_ACK,
    UPDATE_SIGN,
    MAP_ITEM_DATA,
    BLOCK_ENTITY_DATA,
    OPEN_SIGN_EDITOR,
    AWARD_STATS,
    PLAYER_INFO,
    PLAYER_ABILITIES,
    COMMAND_SUGGESTIONS,
    SET_OBJECTIVE,
    SET_SCORE,
    SET_DISPLAY_OBJECTIVE,
    SET_PLAYER_TEAM,
    CUSTOM_PAYLOAD,
    DISCONNECT,
    CHANGE_DIFFICULTY,
    PLAYER_COMBAT,
    SET_CAMERA,
    SET_BORDER,
    SET_TITLES,
    SET_COMPRESSION,
    TAB_LIST,
    RESOURCE_PACK,
    UPDATE_ENTITY_NBT;

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
