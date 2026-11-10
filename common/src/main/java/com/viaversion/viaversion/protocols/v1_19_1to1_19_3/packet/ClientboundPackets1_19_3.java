/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_19_1to1_19_3.packet;

import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;

public enum ClientboundPackets1_19_3 implements ClientboundPacketType {

    ADD_ENTITY, // 0x00
    ADD_EXPERIENCE_ORB, // 0x01
    ADD_PLAYER, // 0x02
    ANIMATE, // 0x03
    AWARD_STATS, // 0x04
    BLOCK_CHANGED_ACK, // 0x05
    BLOCK_DESTRUCTION, // 0x06
    BLOCK_ENTITY_DATA, // 0x07
    BLOCK_EVENT, // 0x08
    BLOCK_UPDATE, // 0x09
    BOSS_EVENT, // 0x0A
    CHANGE_DIFFICULTY, // 0x0B
    CLEAR_TITLES, // 0x0C
    COMMAND_SUGGESTIONS, // 0x0D
    COMMANDS, // 0x0E
    CONTAINER_CLOSE, // 0x0F
    CONTAINER_SET_CONTENT, // 0x10
    CONTAINER_SET_DATA, // 0x11
    CONTAINER_SET_SLOT, // 0x12
    COOLDOWN, // 0x13
    CUSTOM_CHAT_COMPLETIONS, // 0x14
    CUSTOM_PAYLOAD, // 0x15
    DELETE_CHAT, // 0x16
    DISCONNECT, // 0x17
    DISGUISED_CHAT, // 0x18
    ENTITY_EVENT, // 0x19
    EXPLODE, // 0x1A
    FORGET_LEVEL_CHUNK, // 0x1B
    GAME_EVENT, // 0x1C
    HORSE_SCREEN_OPEN, // 0x1D
    INITIALIZE_BORDER, // 0x1E
    KEEP_ALIVE, // 0x1F
    LEVEL_CHUNK_WITH_LIGHT, // 0x20
    LEVEL_EVENT, // 0x21
    LEVEL_PARTICLES, // 0x22
    LIGHT_UPDATE, // 0x23
    LOGIN, // 0x24
    MAP_ITEM_DATA, // 0x25
    MERCHANT_OFFERS, // 0x26
    MOVE_ENTITY_POS, // 0x27
    MOVE_ENTITY_POS_ROT, // 0x28
    MOVE_ENTITY_ROT, // 0x29
    MOVE_VEHICLE, // 0x2A
    OPEN_BOOK, // 0x2B
    OPEN_SCREEN, // 0x2C
    OPEN_SIGN_EDITOR, // 0x2D
    PING, // 0x2E
    PLACE_GHOST_RECIPE, // 0x2F
    PLAYER_ABILITIES, // 0x30
    PLAYER_CHAT, // 0x31
    PLAYER_COMBAT_END, // 0x32
    PLAYER_COMBAT_ENTER, // 0x33
    PLAYER_COMBAT_KILL, // 0x34
    PLAYER_INFO_REMOVE, // 0x35
    PLAYER_INFO_UPDATE, // 0x36
    PLAYER_LOOK_AT, // 0x37
    PLAYER_POSITION, // 0x38
    RECIPE, // 0x39
    REMOVE_ENTITIES, // 0x3A
    REMOVE_MOB_EFFECT, // 0x3B
    RESOURCE_PACK, // 0x3C
    RESPAWN, // 0x3D
    ROTATE_HEAD, // 0x3E
    SECTION_BLOCKS_UPDATE, // 0x3F
    SELECT_ADVANCEMENTS_TAB, // 0x40
    SERVER_DATA, // 0x41
    SET_ACTION_BAR_TEXT, // 0x42
    SET_BORDER_CENTER, // 0x43
    SET_BORDER_LERP_SIZE, // 0x44
    SET_BORDER_SIZE, // 0x45
    SET_BORDER_WARNING_DELAY, // 0x46
    SET_BORDER_WARNING_DISTANCE, // 0x47
    SET_CAMERA, // 0x48
    SET_CARRIED_ITEM, // 0x49
    SET_CHUNK_CACHE_CENTER, // 0x4A
    SET_CHUNK_CACHE_RADIUS, // 0x4B
    SET_DEFAULT_SPAWN_POSITION, // 0x4C
    SET_DISPLAY_OBJECTIVE, // 0x4D
    SET_ENTITY_DATA, // 0x4E
    SET_ENTITY_LINK, // 0x4F
    SET_ENTITY_MOTION, // 0x50
    SET_EQUIPMENT, // 0x51
    SET_EXPERIENCE, // 0x52
    SET_HEALTH, // 0x53
    SET_OBJECTIVE, // 0x54
    SET_PASSENGERS, // 0x55
    SET_PLAYER_TEAM, // 0x56
    SET_SCORE, // 0x57
    SET_SIMULATION_DISTANCE, // 0x58
    SET_SUBTITLE_TEXT, // 0x59
    SET_TIME, // 0x5A
    SET_TITLE_TEXT, // 0x5B
    SET_TITLES_ANIMATION, // 0x5C
    SOUND_ENTITY, // 0x5D
    SOUND, // 0x5E
    STOP_SOUND, // 0x5F
    SYSTEM_CHAT, // 0x60
    TAB_LIST, // 0x61
    TAG_QUERY, // 0x62
    TAKE_ITEM_ENTITY, // 0x63
    TELEPORT_ENTITY, // 0x64
    UPDATE_ADVANCEMENTS, // 0x65
    UPDATE_ATTRIBUTES, // 0x66
    UPDATE_ENABLED_FEATURES, // 0x67
    UPDATE_MOB_EFFECT, // 0x68
    UPDATE_RECIPES, // 0x69
    UPDATE_TAGS; // 0x6A

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
