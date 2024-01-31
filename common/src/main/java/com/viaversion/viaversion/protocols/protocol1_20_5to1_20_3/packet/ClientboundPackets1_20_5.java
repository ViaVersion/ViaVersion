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
package com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.packet;

public enum ClientboundPackets1_20_5 implements ClientboundPacket1_20_5 {

    BUNDLE, // 0x00
    SPAWN_ENTITY, // 0x01
    SPAWN_EXPERIENCE_ORB, // 0x02
    ENTITY_ANIMATION, // 0x03
    STATISTICS, // 0x04
    BLOCK_CHANGED_ACK, // 0x05
    BLOCK_BREAK_ANIMATION, // 0x06
    BLOCK_ENTITY_DATA, // 0x07
    BLOCK_ACTION, // 0x08
    BLOCK_CHANGE, // 0x09
    BOSSBAR, // 0x0A
    SERVER_DIFFICULTY, // 0x0B
    CHUNK_BATCH_FINISHED, // 0x0C
    CHUNK_BATCH_START, // 0x0D
    CHUNK_BIOMES, // 0x0E
    CLEAR_TITLES, // 0x0F
    TAB_COMPLETE, // 0x10
    DECLARE_COMMANDS, // 0x11
    CLOSE_WINDOW, // 0x12
    WINDOW_ITEMS, // 0x13
    WINDOW_PROPERTY, // 0x14
    SET_SLOT, // 0x15
    COOKIE_REQUEST, // 0x16
    COOLDOWN, // 0x17
    CUSTOM_CHAT_COMPLETIONS, // 0x18
    PLUGIN_MESSAGE, // 0x19
    DAMAGE_EVENT, // 0x1A
    DEBUG_SAMPLE, // 0x1B
    DELETE_CHAT_MESSAGE, // 0x1B
    DISCONNECT, // 0x1C
    DISGUISED_CHAT, // 0x1D
    ENTITY_STATUS, // 0x1E
    EXPLOSION, // 0x1F
    UNLOAD_CHUNK, // 0x20
    GAME_EVENT, // 0x21
    OPEN_HORSE_WINDOW, // 0x22
    HIT_ANIMATION, // 0x23
    WORLD_BORDER_INIT, // 0x24
    KEEP_ALIVE, // 0x25
    CHUNK_DATA, // 0x26
    EFFECT, // 0x27
    SPAWN_PARTICLE, // 0x28
    UPDATE_LIGHT, // 0x29
    JOIN_GAME, // 0x2A
    MAP_DATA, // 0x2B
    TRADE_LIST, // 0x2C
    ENTITY_POSITION, // 0x2D
    ENTITY_POSITION_AND_ROTATION, // 0x2E
    ENTITY_ROTATION, // 0x2F
    VEHICLE_MOVE, // 0x30
    OPEN_BOOK, // 0x31
    OPEN_WINDOW, // 0x32
    OPEN_SIGN_EDITOR, // 0x33
    PING, // 0x34
    PONG_RESPONSE, // 0x35
    CRAFT_RECIPE_RESPONSE, // 0x36
    PLAYER_ABILITIES, // 0x37
    PLAYER_CHAT, // 0x38
    COMBAT_END, // 0x39
    COMBAT_ENTER, // 0x3A
    COMBAT_KILL, // 0x3B
    PLAYER_INFO_REMOVE, // 0x3C
    PLAYER_INFO_UPDATE, // 0x3D
    FACE_PLAYER, // 0x3E
    PLAYER_POSITION, // 0x3F
    UNLOCK_RECIPES, // 0x40
    REMOVE_ENTITIES, // 0x41
    REMOVE_ENTITY_EFFECT, // 0x42
    RESET_SCORE, // 0x43
    RESOURCE_PACK_POP, // 0x44
    RESOURCE_PACK_PUSH, // 0x45
    RESPAWN, // 0x46
    ENTITY_HEAD_LOOK, // 0x47
    MULTI_BLOCK_CHANGE, // 0x48
    SELECT_ADVANCEMENTS_TAB, // 0x49
    SERVER_DATA, // 0x4A
    ACTIONBAR, // 0x4B
    WORLD_BORDER_CENTER, // 0x4C
    WORLD_BORDER_LERP_SIZE, // 0x4D
    WORLD_BORDER_SIZE, // 0x4E
    WORLD_BORDER_WARNING_DELAY, // 0x4F
    WORLD_BORDER_WARNING_DISTANCE, // 0x50
    CAMERA, // 0x51
    HELD_ITEM_CHANGE, // 0x52
    UPDATE_VIEW_POSITION, // 0x53
    UPDATE_VIEW_DISTANCE, // 0x54
    SPAWN_POSITION, // 0x55
    DISPLAY_SCOREBOARD, // 0x56
    ENTITY_METADATA, // 0x57
    ATTACH_ENTITY, // 0x58
    ENTITY_VELOCITY, // 0x59
    ENTITY_EQUIPMENT, // 0x5A
    SET_EXPERIENCE, // 0x5B
    UPDATE_HEALTH, // 0x5C
    SCOREBOARD_OBJECTIVE, // 0x5D
    SET_PASSENGERS, // 0x5E
    TEAMS, // 0x5F
    UPDATE_SCORE, // 0x60
    SET_SIMULATION_DISTANCE, // 0x61
    TITLE_SUBTITLE, // 0x62
    TIME_UPDATE, // 0x63
    TITLE_TEXT, // 0x64
    TITLE_TIMES, // 0x65
    ENTITY_SOUND, // 0x66
    SOUND, // 0x67
    START_CONFIGURATION, // 0x68
    STOP_SOUND, // 0x69
    STORE_COOKIE, // 0x6A
    SYSTEM_CHAT, // 0x6B
    TAB_LIST, // 0x6C
    NBT_QUERY, // 0x6D
    COLLECT_ITEM, // 0x6E
    ENTITY_TELEPORT, // 0x6F
    TICKING_STATE, // 0x70
    TICKING_STEP, // 0x71
    TRANSFER, // 0x72
    ADVANCEMENTS, // 0x73
    ENTITY_PROPERTIES, // 0x74
    ENTITY_EFFECT, // 0x75
    DECLARE_RECIPES, // 0x76
    TAGS; // 0x77

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
