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
package com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet;

public enum ClientboundPackets1_20_3 implements ClientboundPacket1_20_3 {

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
    COOLDOWN, // 0x16
    CUSTOM_CHAT_COMPLETIONS, // 0x17
    PLUGIN_MESSAGE, // 0x18
    DAMAGE_EVENT, // 0x19
    DELETE_CHAT_MESSAGE, // 0x1A
    DISCONNECT, // 0x1B
    DISGUISED_CHAT, // 0x1C
    ENTITY_STATUS, // 0x1D
    EXPLOSION, // 0x1E
    UNLOAD_CHUNK, // 0x1F
    GAME_EVENT, // 0x20
    OPEN_HORSE_WINDOW, // 0x21
    HIT_ANIMATION, // 0x22
    WORLD_BORDER_INIT, // 0x23
    KEEP_ALIVE, // 0x24
    CHUNK_DATA, // 0x25
    EFFECT, // 0x26
    SPAWN_PARTICLE, // 0x27
    UPDATE_LIGHT, // 0x28
    JOIN_GAME, // 0x29
    MAP_DATA, // 0x2A
    TRADE_LIST, // 0x2B
    ENTITY_POSITION, // 0x2C
    ENTITY_POSITION_AND_ROTATION, // 0x2D
    ENTITY_ROTATION, // 0x2E
    VEHICLE_MOVE, // 0x2F
    OPEN_BOOK, // 0x30
    OPEN_WINDOW, // 0x31
    OPEN_SIGN_EDITOR, // 0x32
    PING, // 0x33
    PONG_RESPONSE, // 0x34
    CRAFT_RECIPE_RESPONSE, // 0x35
    PLAYER_ABILITIES, // 0x36
    PLAYER_CHAT, // 0x37
    COMBAT_END, // 0x38
    COMBAT_ENTER, // 0x39
    COMBAT_KILL, // 0x3A
    PLAYER_INFO_REMOVE, // 0x3B
    PLAYER_INFO_UPDATE, // 0x3C
    FACE_PLAYER, // 0x3D
    PLAYER_POSITION, // 0x3E
    UNLOCK_RECIPES, // 0x3F
    REMOVE_ENTITIES, // 0x40
    REMOVE_ENTITY_EFFECT, // 0x41
    RESET_SCORE, // 0x42
    RESOURCE_PACK_POP, // 0x43
    RESOURCE_PACK_PUSH, // 0x44
    RESPAWN, // 0x45
    ENTITY_HEAD_LOOK, // 0x46
    MULTI_BLOCK_CHANGE, // 0x47
    SELECT_ADVANCEMENTS_TAB, // 0x48
    SERVER_DATA, // 0x49
    ACTIONBAR, // 0x4A
    WORLD_BORDER_CENTER, // 0x4B
    WORLD_BORDER_LERP_SIZE, // 0x4C
    WORLD_BORDER_SIZE, // 0x4D
    WORLD_BORDER_WARNING_DELAY, // 0x4E
    WORLD_BORDER_WARNING_DISTANCE, // 0x4F
    CAMERA, // 0x50
    HELD_ITEM_CHANGE, // 0x51
    UPDATE_VIEW_POSITION, // 0x52
    UPDATE_VIEW_DISTANCE, // 0x53
    SPAWN_POSITION, // 0x54
    DISPLAY_SCOREBOARD, // 0x55
    ENTITY_METADATA, // 0x56
    ATTACH_ENTITY, // 0x57
    ENTITY_VELOCITY, // 0x58
    ENTITY_EQUIPMENT, // 0x59
    SET_EXPERIENCE, // 0x5A
    UPDATE_HEALTH, // 0x5B
    SCOREBOARD_OBJECTIVE, // 0x5C
    SET_PASSENGERS, // 0x5D
    TEAMS, // 0x5E
    UPDATE_SCORE, // 0x5F
    SET_SIMULATION_DISTANCE, // 0x60
    TITLE_SUBTITLE, // 0x61
    TIME_UPDATE, // 0x62
    TITLE_TEXT, // 0x63
    TITLE_TIMES, // 0x64
    ENTITY_SOUND, // 0x65
    SOUND, // 0x66
    START_CONFIGURATION, // 0x67
    STOP_SOUND, // 0x68
    SYSTEM_CHAT, // 0x69
    TAB_LIST, // 0x6A
    NBT_QUERY, // 0x6B
    COLLECT_ITEM, // 0x6C
    ENTITY_TELEPORT, // 0x6D
    TICKING_STATE, // 0x6E
    TICKING_STEP, // 0x6F
    ADVANCEMENTS, // 0x70
    ENTITY_PROPERTIES, // 0x71
    ENTITY_EFFECT, // 0x72
    DECLARE_RECIPES, // 0x73
    TAGS; // 0x74

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
