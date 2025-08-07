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
package com.viaversion.viaversion.protocols.v1_20to1_20_2.packet;

public enum ClientboundPackets1_20_2 implements ClientboundPacket1_20_2 {

    BUNDLE_DELIMITER,
    ADD_ENTITY,
    ADD_EXPERIENCE_ORB,
    ANIMATE,
    AWARD_STATS,
    BLOCK_CHANGED_ACK,
    BLOCK_DESTRUCTION,
    BLOCK_ENTITY_DATA,
    BLOCK_EVENT,
    BLOCK_UPDATE,
    BOSS_EVENT,
    CHANGE_DIFFICULTY,
    CHUNK_BATCH_FINISHED,
    CHUNK_BATCH_START,
    CHUNKS_BIOMES,
    CLEAR_TITLES,
    COMMAND_SUGGESTIONS,
    COMMANDS,
    CONTAINER_CLOSE,
    CONTAINER_SET_CONTENT,
    CONTAINER_SET_DATA,
    CONTAINER_SET_SLOT,
    COOLDOWN,
    CUSTOM_CHAT_COMPLETIONS,
    CUSTOM_PAYLOAD,
    DAMAGE_EVENT,
    DELETE_CHAT,
    DISCONNECT,
    DISGUISED_CHAT,
    ENTITY_EVENT,
    EXPLODE,
    FORGET_LEVEL_CHUNK,
    GAME_EVENT,
    HORSE_SCREEN_OPEN,
    HURT_ANIMATION,
    INITIALIZE_BORDER,
    KEEP_ALIVE,
    LEVEL_CHUNK_WITH_LIGHT,
    LEVEL_EVENT,
    LEVEL_PARTICLES,
    LIGHT_UPDATE,
    LOGIN,
    MAP_ITEM_DATA,
    MERCHANT_OFFERS,
    MOVE_ENTITY_POS,
    MOVE_ENTITY_POS_ROT,
    MOVE_ENTITY_ROT,
    MOVE_VEHICLE,
    OPEN_BOOK,
    OPEN_SCREEN,
    OPEN_SIGN_EDITOR,
    PING,
    PONG_RESPONSE,
    PLACE_GHOST_RECIPE,
    PLAYER_ABILITIES,
    PLAYER_CHAT,
    PLAYER_COMBAT_END,
    PLAYER_COMBAT_ENTER,
    PLAYER_COMBAT_KILL,
    PLAYER_INFO_REMOVE,
    PLAYER_INFO_UPDATE,
    PLAYER_LOOK_AT,
    PLAYER_POSITION,
    RECIPE,
    REMOVE_ENTITIES,
    REMOVE_MOB_EFFECT,
    RESOURCE_PACK,
    RESPAWN,
    ROTATE_HEAD,
    SECTION_BLOCKS_UPDATE,
    SELECT_ADVANCEMENTS_TAB,
    SERVER_DATA,
    SET_ACTION_BAR_TEXT,
    SET_BORDER_CENTER,
    SET_BORDER_LERP_SIZE,
    SET_BORDER_SIZE,
    SET_BORDER_WARNING_DELAY,
    SET_BORDER_WARNING_DISTANCE,
    SET_CAMERA,
    SET_CARRIED_ITEM,
    SET_CHUNK_CACHE_CENTER,
    SET_CHUNK_CACHE_RADIUS,
    SET_DEFAULT_SPAWN_POSITION,
    SET_DISPLAY_OBJECTIVE,
    SET_ENTITY_DATA,
    SET_ENTITY_LINK,
    SET_ENTITY_MOTION,
    SET_EQUIPMENT,
    SET_EXPERIENCE,
    SET_HEALTH,
    SET_OBJECTIVE,
    SET_PASSENGERS,
    SET_PLAYER_TEAM,
    SET_SCORE,
    SET_SIMULATION_DISTANCE,
    SET_SUBTITLE_TEXT,
    SET_TIME,
    SET_TITLE_TEXT,
    SET_TITLES_ANIMATION,
    SOUND_ENTITY,
    SOUND,
    START_CONFIGURATION,
    STOP_SOUND,
    SYSTEM_CHAT,
    TAB_LIST,
    TAG_QUERY,
    TAKE_ITEM_ENTITY,
    TELEPORT_ENTITY,
    UPDATE_ADVANCEMENTS,
    UPDATE_ATTRIBUTES,
    UPDATE_MOB_EFFECT,
    UPDATE_RECIPES,
    UPDATE_TAGS;

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
