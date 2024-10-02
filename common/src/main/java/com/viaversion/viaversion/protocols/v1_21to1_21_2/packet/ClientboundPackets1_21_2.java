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
    ENTITY_POSITION_SYNC, // 0x73
    EXPLODE, // 0x20
    FORGET_LEVEL_CHUNK, // 0x21
    GAME_EVENT, // 0x22
    HORSE_SCREEN_OPEN, // 0x23
    HURT_ANIMATION, // 0x24
    INITIALIZE_BORDER, // 0x25
    KEEP_ALIVE, // 0x26
    LEVEL_CHUNK_WITH_LIGHT, // 0x27
    LEVEL_EVENT, // 0x28
    LEVEL_PARTICLES, // 0x29
    LIGHT_UPDATE, // 0x2A
    LOGIN, // 0x2B
    MAP_ITEM_DATA, // 0x2C
    MERCHANT_OFFERS, // 0x2D
    MOVE_ENTITY_POS, // 0x2E
    MOVE_ENTITY_POS_ROT, // 0x2F
    MOVE_MINECART_ALONG_TRACK, // 0x30
    MOVE_ENTITY_ROT, // 0x31
    MOVE_VEHICLE, // 0x32
    OPEN_BOOK, // 0x33
    OPEN_SCREEN, // 0x34
    OPEN_SIGN_EDITOR, // 0x35
    PING, // 0x36
    PONG_RESPONSE, // 0x37
    PLACE_GHOST_RECIPE, // 0x38
    PLAYER_ABILITIES, // 0x39
    PLAYER_CHAT, // 0x3A
    PLAYER_COMBAT_END, // 0x3B
    PLAYER_COMBAT_ENTER, // 0x3C
    PLAYER_COMBAT_KILL, // 0x3D
    PLAYER_INFO_REMOVE, // 0x3E
    PLAYER_INFO_UPDATE, // 0x3F
    PLAYER_LOOK_AT, // 0x40
    PLAYER_POSITION, // 0x41
    PLAYER_ROTATION, // 0x41
    RECIPE_BOOK_ADD, // 0x42
    RECIPE_BOOK_REMOVE, // 0x42
    RECIPE_BOOK_SETTINGS, // 0x42
    REMOVE_ENTITIES, // 0x43
    REMOVE_MOB_EFFECT, // 0x44
    RESET_SCORE, // 0x45
    RESOURCE_PACK_POP, // 0x46
    RESOURCE_PACK_PUSH, // 0x47
    RESPAWN, // 0x48
    ROTATE_HEAD, // 0x49
    SECTION_BLOCKS_UPDATE, // 0x4A
    SELECT_ADVANCEMENTS_TAB, // 0x4B
    SERVER_DATA, // 0x4C
    SET_ACTION_BAR_TEXT, // 0x4D
    SET_BORDER_CENTER, // 0x4E
    SET_BORDER_LERP_SIZE, // 0x4F
    SET_BORDER_SIZE, // 0x50
    SET_BORDER_WARNING_DELAY, // 0x51
    SET_BORDER_WARNING_DISTANCE, // 0x52
    SET_CAMERA, // 0x53
    SET_CHUNK_CACHE_CENTER, // 0x54
    SET_CHUNK_CACHE_RADIUS, // 0x55
    SET_CURSOR_ITEM, // 0x56
    SET_DEFAULT_SPAWN_POSITION, // 0x57
    SET_DISPLAY_OBJECTIVE, // 0x58
    SET_ENTITY_DATA, // 0x59
    SET_ENTITY_LINK, // 0x5A
    SET_ENTITY_MOTION, // 0x5B
    SET_EQUIPMENT, // 0x5C
    SET_EXPERIENCE, // 0x5D
    SET_HEALTH, // 0x5E
    SET_HELD_SLOT, // 0x5F
    SET_OBJECTIVE, // 0x60
    SET_PASSENGERS, // 0x61
    SET_PLAYER_INVENTORY, // 0x62
    SET_PLAYER_TEAM, // 0x63
    SET_SCORE, // 0x64
    SET_SIMULATION_DISTANCE, // 0x65
    SET_SUBTITLE_TEXT, // 0x66
    SET_TIME, // 0x67
    SET_TITLE_TEXT, // 0x68
    SET_TITLES_ANIMATION, // 0x69
    SOUND_ENTITY, // 0x6A
    SOUND, // 0x6B
    START_CONFIGURATION, // 0x6C
    STOP_SOUND, // 0x6D
    STORE_COOKIE, // 0x6E
    SYSTEM_CHAT, // 0x6F
    TAB_LIST, // 0x70
    TAG_QUERY, // 0x71
    TAKE_ITEM_ENTITY, // 0x72
    TELEPORT_ENTITY, // 0x73
    TICKING_STATE, // 0x74
    TICKING_STEP, // 0x75
    TRANSFER, // 0x76
    UPDATE_ADVANCEMENTS, // 0x77
    UPDATE_ATTRIBUTES, // 0x78
    UPDATE_MOB_EFFECT, // 0x79
    UPDATE_RECIPES, // 0x7A
    UPDATE_TAGS, // 0x7B
    PROJECTILE_POWER, // 0x7C
    CUSTOM_REPORT_DETAILS, // 0x7D
    SERVER_LINKS; // 0x7E

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
