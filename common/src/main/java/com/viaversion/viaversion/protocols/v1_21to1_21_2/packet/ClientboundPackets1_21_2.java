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
package com.viaversion.viaversion.protocols.v1_21to1_21_2.packet;

public enum ClientboundPackets1_21_2 implements ClientboundPacket1_21_2 {

    BUNDLE_DELIMITER, // 0x00
    ADD_ENTITY, // 0x01
    ADD_EXPERIENCE_ORB, // 0x02
    ANIMATE, // 0x03
    AWARD_STATS, // 0x04
    BLOCK_CHANGED_ACK, // 0x05
    BLOCK_DESTRUCTION, // 0x06
    BLOCK_ENTITY_DATA, // 0x07
    BLOCK_EVENT, // 0x08
    BLOCK_UPDATE, // 0x09
    BOSS_EVENT, // 0x0A
    CHANGE_DIFFICULTY, // 0x0B
    CHUNK_BATCH_FINISHED, // 0x0C
    CHUNK_BATCH_START, // 0x0D
    CHUNKS_BIOMES, // 0x0E
    CLEAR_TITLES, // 0x0F
    COMMAND_SUGGESTIONS, // 0x10
    COMMANDS, // 0x11
    CONTAINER_CLOSE, // 0x12
    CONTAINER_SET_CONTENT, // 0x13
    CONTAINER_SET_DATA, // 0x14
    CONTAINER_SET_SLOT, // 0x15
    COOKIE_REQUEST, // 0x16
    COOLDOWN, // 0x17
    CUSTOM_CHAT_COMPLETIONS, // 0x18
    CUSTOM_PAYLOAD, // 0x19
    DAMAGE_EVENT, // 0x1A
    DEBUG_SAMPLE, // 0x1B
    DELETE_CHAT, // 0x1C
    DISCONNECT, // 0x1D
    DISGUISED_CHAT, // 0x1E
    ENTITY_EVENT, // 0x1F
    ENTITY_POSITION_SYNC, // 0x20
    EXPLODE, // 0x21
    FORGET_LEVEL_CHUNK, // 0x22
    GAME_EVENT, // 0x23
    HORSE_SCREEN_OPEN, // 0x24
    HURT_ANIMATION, // 0x25
    INITIALIZE_BORDER, // 0x26
    KEEP_ALIVE, // 0x27
    LEVEL_CHUNK_WITH_LIGHT, // 0x28
    LEVEL_EVENT, // 0x29
    LEVEL_PARTICLES, // 0x2A
    LIGHT_UPDATE, // 0x2B
    LOGIN, // 0x2C
    MAP_ITEM_DATA, // 0x2D
    MERCHANT_OFFERS, // 0x2E
    MOVE_ENTITY_POS, // 0x2F
    MOVE_ENTITY_POS_ROT, // 0x30
    MOVE_MINECART_ALONG_TRACK, // 0x31
    MOVE_ENTITY_ROT, // 0x32
    MOVE_VEHICLE, // 0x33
    OPEN_BOOK, // 0x34
    OPEN_SCREEN, // 0x35
    OPEN_SIGN_EDITOR, // 0x36
    PING, // 0x37
    PONG_RESPONSE, // 0x38
    PLACE_GHOST_RECIPE, // 0x39
    PLAYER_ABILITIES, // 0x3A
    PLAYER_CHAT, // 0x3B
    PLAYER_COMBAT_END, // 0x3C
    PLAYER_COMBAT_ENTER, // 0x3D
    PLAYER_COMBAT_KILL, // 0x3E
    PLAYER_INFO_REMOVE, // 0x3F
    PLAYER_INFO_UPDATE, // 0x40
    PLAYER_LOOK_AT, // 0x41
    PLAYER_POSITION, // 0x42
    PLAYER_ROTATION, // 0x43
    RECIPE_BOOK_ADD, // 0x44
    RECIPE_BOOK_REMOVE, // 0x45
    RECIPE_BOOK_SETTINGS, // 0x46
    REMOVE_ENTITIES, // 0x47
    REMOVE_MOB_EFFECT, // 0x48
    RESET_SCORE, // 0x49
    RESOURCE_PACK_POP, // 0x4A
    RESOURCE_PACK_PUSH, // 0x4B
    RESPAWN, // 0x4C
    ROTATE_HEAD, // 0x4D
    SECTION_BLOCKS_UPDATE, // 0x4E
    SELECT_ADVANCEMENTS_TAB, // 0x4F
    SERVER_DATA, // 0x50
    SET_ACTION_BAR_TEXT, // 0x51
    SET_BORDER_CENTER, // 0x52
    SET_BORDER_LERP_SIZE, // 0x53
    SET_BORDER_SIZE, // 0x54
    SET_BORDER_WARNING_DELAY, // 0x55
    SET_BORDER_WARNING_DISTANCE, // 0x56
    SET_CAMERA, // 0x57
    SET_CHUNK_CACHE_CENTER, // 0x58
    SET_CHUNK_CACHE_RADIUS, // 0x59
    SET_CURSOR_ITEM, // 0x5A
    SET_DEFAULT_SPAWN_POSITION, // 0x5B
    SET_DISPLAY_OBJECTIVE, // 0x5C
    SET_ENTITY_DATA, // 0x5D
    SET_ENTITY_LINK, // 0x5E
    SET_ENTITY_MOTION, // 0x5F
    SET_EQUIPMENT, // 0x60
    SET_EXPERIENCE, // 0x61
    SET_HEALTH, // 0x62
    SET_HELD_SLOT, // 0x63
    SET_OBJECTIVE, // 0x64
    SET_PASSENGERS, // 0x65
    SET_PLAYER_INVENTORY, // 0x66
    SET_PLAYER_TEAM, // 0x67
    SET_SCORE, // 0x68
    SET_SIMULATION_DISTANCE, // 0x69
    SET_SUBTITLE_TEXT, // 0x6A
    SET_TIME, // 0x6B
    SET_TITLE_TEXT, // 0x6C
    SET_TITLES_ANIMATION, // 0x6D
    SOUND_ENTITY, // 0x6E
    SOUND, // 0x6F
    START_CONFIGURATION, // 0x70
    STOP_SOUND, // 0x71
    STORE_COOKIE, // 0x72
    SYSTEM_CHAT, // 0x73
    TAB_LIST, // 0x74
    TAG_QUERY, // 0x75
    TAKE_ITEM_ENTITY, // 0x76
    TELEPORT_ENTITY, // 0x77
    TICKING_STATE, // 0x78
    TICKING_STEP, // 0x79
    TRANSFER, // 0x7A
    UPDATE_ADVANCEMENTS, // 0x7B
    UPDATE_ATTRIBUTES, // 0x7C
    UPDATE_MOB_EFFECT, // 0x7D
    UPDATE_RECIPES, // 0x7E
    UPDATE_TAGS, // 0x7F
    PROJECTILE_POWER, // 0x80
    CUSTOM_REPORT_DETAILS, // 0x81
    SERVER_LINKS; // 0x82

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
