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
package com.viaversion.viaversion.protocols.v26_2to26_3.packet;

public enum ClientboundPackets26_3 implements ClientboundPacket26_3 {

    BUNDLE_DELIMITER, // 0x00
    ADD_ENTITY, // 0x01
    ANIMATE, // 0x02
    AWARD_STATS, // 0x03
    BLOCK_CHANGED_ACK, // 0x04
    BLOCK_DESTRUCTION, // 0x05
    BLOCK_ENTITY_DATA, // 0x06
    BLOCK_EVENT, // 0x07
    BLOCK_UPDATE, // 0x08
    BOSS_EVENT, // 0x09
    CHANGE_DIFFICULTY, // 0x0A
    CHUNK_BATCH_FINISHED, // 0x0B
    CHUNK_BATCH_START, // 0x0C
    CHUNKS_BIOMES, // 0x0D
    CLEAR_TITLES, // 0x0E
    COMMAND_SUGGESTIONS, // 0x0F
    COMMANDS, // 0x10
    CONTAINER_CLOSE, // 0x11
    CONTAINER_SET_CONTENT, // 0x12
    CONTAINER_SET_DATA, // 0x13
    CONTAINER_SET_SLOT, // 0x14
    COOKIE_REQUEST, // 0x15
    COOLDOWN, // 0x16
    CUSTOM_CHAT_COMPLETIONS, // 0x17
    CUSTOM_PAYLOAD, // 0x18
    DAMAGE_EVENT, // 0x19
    DEBUG_BLOCK_VALUE, // 0x1A
    DEBUG_CHUNK_VALUE, // 0x1B
    DEBUG_ENTITY_VALUE, // 0x1C
    DEBUG_EVENT, // 0x1D
    DEBUG_SAMPLE, // 0x1E
    DELETE_CHAT, // 0x1F
    DISCONNECT, // 0x20
    DISGUISED_CHAT, // 0x21
    ENTITY_EVENT, // 0x22
    ENTITY_POSITION_SYNC, // 0x23
    EXPLODE, // 0x24
    ADD_TRANSIENT_BLOCK, // 0x25
    FORGET_LEVEL_CHUNK, // 0x26
    GAME_EVENT, // 0x27
    GAME_RULE_VALUES, // 0x28
    GAME_TEST_HIGHLIGHT_POS, // 0x29
    MOUNT_SCREEN_OPEN, // 0x2A
    HURT_ANIMATION, // 0x2B
    INITIALIZE_BORDER, // 0x2C
    KEEP_ALIVE, // 0x2D
    LEVEL_CHUNK_WITH_LIGHT, // 0x2E
    LEVEL_EVENT, // 0x2F
    LEVEL_PARTICLES, // 0x30
    LIGHT_UPDATE, // 0x31
    LOGIN, // 0x32
    LOW_DISK_SPACE_WARNING, // 0x33
    MAP_ITEM_DATA, // 0x34
    MERCHANT_OFFERS, // 0x35
    MOVE_ENTITY_POS, // 0x36
    MOVE_ENTITY_POS_ROT, // 0x37
    MOVE_MINECART_ALONG_TRACK, // 0x38
    MOVE_ENTITY_ROT, // 0x39
    MOVE_VEHICLE, // 0x3A
    OPEN_BOOK, // 0x3B
    OPEN_SCREEN, // 0x3C
    OPEN_SIGN_EDITOR, // 0x3D
    PING, // 0x3E
    PONG_RESPONSE, // 0x3F
    PLACE_GHOST_RECIPE, // 0x40
    PLAYER_ABILITIES, // 0x41
    PLAYER_CHAT, // 0x42
    PLAYER_COMBAT_END, // 0x43
    PLAYER_COMBAT_ENTER, // 0x44
    PLAYER_COMBAT_KILL, // 0x45
    PLAYER_INFO_REMOVE, // 0x46
    PLAYER_INFO_UPDATE, // 0x47
    PLAYER_LOOK_AT, // 0x48
    PLAYER_POSITION, // 0x49
    PLAYER_ROTATION, // 0x4A
    RECIPE_BOOK_ADD, // 0x4B
    RECIPE_BOOK_REMOVE, // 0x4C
    RECIPE_BOOK_SETTINGS, // 0x4D
    REMOVE_ENTITIES, // 0x4E
    REMOVE_MOB_EFFECT, // 0x4F
    RESET_SCORE, // 0x50
    RESOURCE_PACK_POP, // 0x51
    RESOURCE_PACK_PUSH, // 0x52
    POST_EFFECTS, // 0x53
    RESPAWN, // 0x54
    ROTATE_HEAD, // 0x55
    SECTION_BLOCKS_UPDATE, // 0x56
    SELECT_ADVANCEMENTS_TAB, // 0x57
    SERVER_DATA, // 0x58
    SET_ACTION_BAR_TEXT, // 0x59
    SET_BORDER_CENTER, // 0x5A
    SET_BORDER_LERP_SIZE, // 0x5B
    SET_BORDER_SIZE, // 0x5C
    SET_BORDER_WARNING_DELAY, // 0x5D
    SET_BORDER_WARNING_DISTANCE, // 0x5E
    SET_CAMERA, // 0x5F
    SET_CHUNK_CACHE_CENTER, // 0x60
    SET_CHUNK_CACHE_RADIUS, // 0x61
    SET_CURSOR_ITEM, // 0x62
    SET_DEFAULT_SPAWN_POSITION, // 0x63
    SET_DISPLAY_OBJECTIVE, // 0x64
    SET_ENTITY_DATA, // 0x65
    SET_ENTITY_LINK, // 0x66
    SET_ENTITY_MOTION, // 0x67
    SET_EQUIPMENT, // 0x68
    SET_EXPERIENCE, // 0x69
    SET_HEALTH, // 0x6A
    SET_HELD_SLOT, // 0x6B
    SET_OBJECTIVE, // 0x6C
    SET_PASSENGERS, // 0x6D
    SET_PLAYER_INVENTORY, // 0x6E
    SET_PLAYER_TEAM, // 0x6F
    SET_SCORE, // 0x70
    SET_SIMULATION_DISTANCE, // 0x71
    SET_SUBTITLE_TEXT, // 0x72
    SET_TIME, // 0x73
    SET_TITLE_TEXT, // 0x74
    SET_TITLES_ANIMATION, // 0x75
    SOUND_ENTITY, // 0x76
    SOUND, // 0x77
    START_CONFIGURATION, // 0x78
    STOP_SOUND, // 0x79
    STORE_COOKIE, // 0x7A
    SWING_ANIMATION, // 0x7B
    SYSTEM_CHAT, // 0x7C
    TAB_LIST, // 0x7D
    TAG_QUERY, // 0x7E
    TAKE_ITEM_ENTITY, // 0x7F
    TELEPORT_ENTITY, // 0x80
    TEST_INSTANCE_BLOCK_STATUS, // 0x81
    TICKING_STATE, // 0x82
    TICKING_STEP, // 0x83
    TRANSFER, // 0x84
    UPDATE_ADVANCEMENTS, // 0x85
    UPDATE_ATTRIBUTES, // 0x86
    UPDATE_MOB_EFFECT, // 0x87
    UPDATE_RECIPES, // 0x88
    UPDATE_TAGS, // 0x89
    PROJECTILE_POWER, // 0x8A
    CUSTOM_REPORT_DETAILS, // 0x8B
    SERVER_LINKS, // 0x8C
    TRACKED_WAYPOINT, // 0x8D
    CLEAR_DIALOG, // 0x8E
    SHOW_DIALOG; // 0x8F

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
