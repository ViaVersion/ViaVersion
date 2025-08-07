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
package com.viaversion.viaversion.protocols.v1_9_1to1_9_3.packet;

import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;

public enum ClientboundPackets1_9_3 implements ClientboundPacketType {

    ADD_ENTITY,
    ADD_EXPERIENCE_ORB,
    ADD_GLOBAL_ENTITY,
    ADD_MOB,
    ADD_PAINTING,
    ADD_PLAYER,
    ANIMATE,
    AWARD_STATS,
    BLOCK_DESTRUCTION,
    BLOCK_ENTITY_DATA,
    BLOCK_EVENT,
    BLOCK_UPDATE,
    BOSS_EVENT,
    CHANGE_DIFFICULTY,
    COMMAND_SUGGESTIONS,
    CHAT,
    CHUNK_BLOCKS_UPDATE,
    CONTAINER_ACK,
    CONTAINER_CLOSE,
    OPEN_SCREEN,
    CONTAINER_SET_CONTENT,
    CONTAINER_SET_DATA,
    CONTAINER_SET_SLOT,
    COOLDOWN,
    CUSTOM_PAYLOAD,
    CUSTOM_SOUND,
    DISCONNECT,
    ENTITY_EVENT,
    EXPLODE,
    FORGET_LEVEL_CHUNK,
    GAME_EVENT,
    KEEP_ALIVE,
    LEVEL_CHUNK,
    LEVEL_EVENT,
    LEVEL_PARTICLES,
    LOGIN,
    MAP_ITEM_DATA,
    MOVE_ENTITY_POS,
    MOVE_ENTITY_POS_ROT,
    MOVE_ENTITY_ROT,
    MOVE_ENTITY,
    MOVE_VEHICLE,
    OPEN_SIGN_EDITOR,
    PLAYER_ABILITIES,
    PLAYER_COMBAT,
    PLAYER_INFO,
    PLAYER_POSITION,
    PLAYER_SLEEP,
    REMOVE_ENTITIES,
    REMOVE_MOB_EFFECT,
    RESOURCE_PACK,
    RESPAWN,
    ROTATE_HEAD,
    SET_BORDER,
    SET_CAMERA,
    SET_CARRIED_ITEM,
    SET_DISPLAY_OBJECTIVE,
    SET_ENTITY_DATA,
    SET_ENTITY_LINK,
    SET_ENTITY_MOTION,
    SET_EQUIPPED_ITEM,
    SET_EXPERIENCE,
    SET_HEALTH,
    SET_OBJECTIVE,
    SET_PASSENGERS,
    SET_PLAYER_TEAM,
    SET_SCORE,
    SET_DEFAULT_SPAWN_POSITION,
    SET_TIME,
    SET_TITLES,
    SOUND,
    TAB_LIST,
    TAKE_ITEM_ENTITY,
    TELEPORT_ENTITY,
    UPDATE_ATTRIBUTES,
    UPDATE_MOB_EFFECT;

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
