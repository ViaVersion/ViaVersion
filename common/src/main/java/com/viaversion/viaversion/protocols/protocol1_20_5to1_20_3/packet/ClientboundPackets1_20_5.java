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
    DELETE_CHAT_MESSAGE, // 0x1C
    DISCONNECT, // 0x1D
    DISGUISED_CHAT, // 0x1E
    ENTITY_STATUS, // 0x1F
    EXPLOSION, // 0x20
    UNLOAD_CHUNK, // 0x21
    GAME_EVENT, // 0x22
    OPEN_HORSE_WINDOW, // 0x23
    HIT_ANIMATION, // 0x24
    WORLD_BORDER_INIT, // 0x25
    KEEP_ALIVE, // 0x26
    CHUNK_DATA, // 0x27
    EFFECT, // 0x28
    SPAWN_PARTICLE, // 0x29
    UPDATE_LIGHT, // 0x2A
    JOIN_GAME, // 0x2B
    MAP_DATA, // 0x2C
    TRADE_LIST, // 0x2D
    ENTITY_POSITION, // 0x2E
    ENTITY_POSITION_AND_ROTATION, // 0x2F
    ENTITY_ROTATION, // 0x30
    VEHICLE_MOVE, // 0x31
    OPEN_BOOK, // 0x32
    OPEN_WINDOW, // 0x33
    OPEN_SIGN_EDITOR, // 0x34
    PING, // 0x35
    PONG_RESPONSE, // 0x36
    CRAFT_RECIPE_RESPONSE, // 0x37
    PLAYER_ABILITIES, // 0x38
    PLAYER_CHAT, // 0x39
    COMBAT_END, // 0x3A
    COMBAT_ENTER, // 0x3B
    COMBAT_KILL, // 0x3C
    PLAYER_INFO_REMOVE, // 0x3D
    PLAYER_INFO_UPDATE, // 0x3E
    FACE_PLAYER, // 0x3F
    PLAYER_POSITION, // 0x40
    UNLOCK_RECIPES, // 0x41
    REMOVE_ENTITIES, // 0x42
    REMOVE_ENTITY_EFFECT, // 0x43
    RESET_SCORE, // 0x44
    RESOURCE_PACK_POP, // 0x45
    RESOURCE_PACK_PUSH, // 0x46
    RESPAWN, // 0x47
    ENTITY_HEAD_LOOK, // 0x48
    MULTI_BLOCK_CHANGE, // 0x49
    SELECT_ADVANCEMENTS_TAB, // 0x4A
    SERVER_DATA, // 0x4B
    ACTIONBAR, // 0x4C
    WORLD_BORDER_CENTER, // 0x4D
    WORLD_BORDER_LERP_SIZE, // 0x4E
    WORLD_BORDER_SIZE, // 0x4F
    WORLD_BORDER_WARNING_DELAY, // 0x50
    WORLD_BORDER_WARNING_DISTANCE, // 0x51
    CAMERA, // 0x52
    HELD_ITEM_CHANGE, // 0x53
    UPDATE_VIEW_POSITION, // 0x54
    UPDATE_VIEW_DISTANCE, // 0x55
    SPAWN_POSITION, // 0x56
    DISPLAY_SCOREBOARD, // 0x57
    ENTITY_METADATA, // 0x58
    ATTACH_ENTITY, // 0x59
    ENTITY_VELOCITY, // 0x5A
    ENTITY_EQUIPMENT, // 0x5B
    SET_EXPERIENCE, // 0x5C
    UPDATE_HEALTH, // 0x5D
    SCOREBOARD_OBJECTIVE, // 0x5E
    SET_PASSENGERS, // 0x5F
    TEAMS, // 0x60
    UPDATE_SCORE, // 0x61
    SET_SIMULATION_DISTANCE, // 0x62
    TITLE_SUBTITLE, // 0x63
    TIME_UPDATE, // 0x64
    TITLE_TEXT, // 0x65
    TITLE_TIMES, // 0x66
    ENTITY_SOUND, // 0x67
    SOUND, // 0x68
    START_CONFIGURATION, // 0x69
    STOP_SOUND, // 0x6A
    STORE_COOKIE, // 0x6B
    SYSTEM_CHAT, // 0x6C
    TAB_LIST, // 0x6D
    NBT_QUERY, // 0x6E
    COLLECT_ITEM, // 0x6F
    ENTITY_TELEPORT, // 0x70
    TICKING_STATE, // 0x71
    TICKING_STEP, // 0x72
    TRANSFER, // 0x73
    ADVANCEMENTS, // 0x74
    ENTITY_PROPERTIES, // 0x75
    ENTITY_EFFECT, // 0x76
    DECLARE_RECIPES, // 0x77
    TAGS, // 0x78
    PROJECTILE_POWER; // 0x79

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
