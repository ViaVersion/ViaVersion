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
package com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet;

import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;

public enum ClientboundPackets1_19_4 implements ClientboundPacketType {

    BUNDLE_DELIMITER, // 0x00
    ADD_ENTITY, // 0x01
    ADD_EXPERIENCE_ORB, // 0x02
    ADD_PLAYER, // 0x03
    ANIMATE, // 0x04
    AWARD_STATS, // 0x05
    BLOCK_CHANGED_ACK, // 0x06
    BLOCK_DESTRUCTION, // 0x07
    BLOCK_ENTITY_DATA, // 0x08
    BLOCK_EVENT, // 0x09
    BLOCK_UPDATE, // 0x0A
    BOSS_EVENT, // 0x0B
    CHANGE_DIFFICULTY, // 0x0C
    CHUNKS_BIOMES, // 0x0D
    CLEAR_TITLES, // 0x0E
    COMMAND_SUGGESTIONS, // 0x0F
    COMMANDS, // 0x10
    CONTAINER_CLOSE, // 0x11
    CONTAINER_SET_CONTENT, // 0x12
    CONTAINER_SET_DATA, // 0x13
    CONTAINER_SET_SLOT, // 0x14
    COOLDOWN, // 0x15
    CUSTOM_CHAT_COMPLETIONS, // 0x16
    CUSTOM_PAYLOAD, // 0x17
    DAMAGE_EVENT, // 0x18
    DELETE_CHAT, // 0x19
    DISCONNECT, // 0x1A
    DISGUISED_CHAT, // 0x1B
    ENTITY_EVENT, // 0x1C
    EXPLODE, // 0x1D
    FORGET_LEVEL_CHUNK, // 0x1E
    GAME_EVENT, // 0x1F
    HORSE_SCREEN_OPEN, // 0x20
    HURT_ANIMATION, // 0x21
    INITIALIZE_BORDER, // 0x22
    KEEP_ALIVE, // 0x23
    LEVEL_CHUNK_WITH_LIGHT, // 0x24
    LEVEL_EVENT, // 0x25
    LEVEL_PARTICLES, // 0x26
    LIGHT_UPDATE, // 0x27
    LOGIN, // 0x28
    MAP_ITEM_DATA, // 0x29
    MERCHANT_OFFERS, // 0x2A
    MOVE_ENTITY_POS, // 0x2B
    MOVE_ENTITY_POS_ROT, // 0x2C
    MOVE_ENTITY_ROT, // 0x2D
    MOVE_VEHICLE, // 0x2E
    OPEN_BOOK, // 0x2F
    OPEN_SCREEN, // 0x30
    OPEN_SIGN_EDITOR, // 0x31
    PING, // 0x32
    PLACE_GHOST_RECIPE, // 0x33
    PLAYER_ABILITIES, // 0x34
    PLAYER_CHAT, // 0x35
    PLAYER_COMBAT_END, // 0x36
    PLAYER_COMBAT_ENTER, // 0x37
    PLAYER_COMBAT_KILL, // 0x38
    PLAYER_INFO_REMOVE, // 0x39
    PLAYER_INFO_UPDATE, // 0x3A
    PLAYER_LOOK_AT, // 0x3B
    PLAYER_POSITION, // 0x3C
    RECIPE, // 0x3D
    REMOVE_ENTITIES, // 0x3E
    REMOVE_MOB_EFFECT, // 0x3F
    RESOURCE_PACK, // 0x40
    RESPAWN, // 0x41
    ROTATE_HEAD, // 0x42
    SECTION_BLOCKS_UPDATE, // 0x43
    SELECT_ADVANCEMENTS_TAB, // 0x44
    SERVER_DATA, // 0x45
    SET_ACTION_BAR_TEXT, // 0x46
    SET_BORDER_CENTER, // 0x47
    SET_BORDER_LERP_SIZE, // 0x48
    SET_BORDER_SIZE, // 0x49
    SET_BORDER_WARNING_DELAY, // 0x4A
    SET_BORDER_WARNING_DISTANCE, // 0x4B
    SET_CAMERA, // 0x4C
    SET_CARRIED_ITEM, // 0x4D
    SET_CHUNK_CACHE_CENTER, // 0x4E
    SET_CHUNK_CACHE_RADIUS, // 0x4F
    SET_DEFAULT_SPAWN_POSITION, // 0x50
    SET_DISPLAY_OBJECTIVE, // 0x51
    SET_ENTITY_DATA, // 0x52
    SET_ENTITY_LINK, // 0x53
    SET_ENTITY_MOTION, // 0x54
    SET_EQUIPMENT, // 0x55
    SET_EXPERIENCE, // 0x56
    SET_HEALTH, // 0x57
    SET_OBJECTIVE, // 0x58
    SET_PASSENGERS, // 0x59
    SET_PLAYER_TEAM, // 0x5A
    SET_SCORE, // 0x5B
    SET_SIMULATION_DISTANCE, // 0x5C
    SET_SUBTITLE_TEXT, // 0x5D
    SET_TIME, // 0x5E
    SET_TITLE_TEXT, // 0x5F
    SET_TITLES_ANIMATION, // 0x60
    SOUND_ENTITY, // 0x61
    SOUND, // 0x62
    STOP_SOUND, // 0x63
    SYSTEM_CHAT, // 0x64
    TAB_LIST, // 0x65
    TAG_QUERY, // 0x66
    TAKE_ITEM_ENTITY, // 0x67
    TELEPORT_ENTITY, // 0x68
    UPDATE_ADVANCEMENTS, // 0x69
    UPDATE_ATTRIBUTES, // 0x6A
    UPDATE_ENABLED_FEATURES, // 0x6B
    UPDATE_MOB_EFFECT, // 0x6C
    UPDATE_RECIPES, // 0x6D
    UPDATE_TAGS; // 0x6E

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
