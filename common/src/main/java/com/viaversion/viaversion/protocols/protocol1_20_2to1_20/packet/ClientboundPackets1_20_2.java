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
package com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet;

public enum ClientboundPackets1_20_2 implements ClientboundPacket1_20_2 {

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
    RESOURCE_PACK, // 0x42
    RESPAWN, // 0x43
    ENTITY_HEAD_LOOK, // 0x44
    MULTI_BLOCK_CHANGE, // 0x45
    SELECT_ADVANCEMENTS_TAB, // 0x46
    SERVER_DATA, // 0x47
    ACTIONBAR, // 0x48
    WORLD_BORDER_CENTER, // 0x49
    WORLD_BORDER_LERP_SIZE, // 0x4A
    WORLD_BORDER_SIZE, // 0x4B
    WORLD_BORDER_WARNING_DELAY, // 0x4C
    WORLD_BORDER_WARNING_DISTANCE, // 0x4D
    CAMERA, // 0x4E
    HELD_ITEM_CHANGE, // 0x4F
    UPDATE_VIEW_POSITION, // 0x50
    UPDATE_VIEW_DISTANCE, // 0x51
    SPAWN_POSITION, // 0x52
    DISPLAY_SCOREBOARD, // 0x53
    ENTITY_METADATA, // 0x54
    ATTACH_ENTITY, // 0x55
    ENTITY_VELOCITY, // 0x56
    ENTITY_EQUIPMENT, // 0x57
    SET_EXPERIENCE, // 0x58
    UPDATE_HEALTH, // 0x59
    SCOREBOARD_OBJECTIVE, // 0x5A
    SET_PASSENGERS, // 0x5B
    TEAMS, // 0x5C
    UPDATE_SCORE, // 0x5D
    SET_SIMULATION_DISTANCE, // 0x5E
    TITLE_SUBTITLE, // 0x5F
    TIME_UPDATE, // 0x60
    TITLE_TEXT, // 0x61
    TITLE_TIMES, // 0x62
    ENTITY_SOUND, // 0x63
    SOUND, // 0x64
    START_CONFIGURATION, // 0x65
    STOP_SOUND, // 0x66
    SYSTEM_CHAT, // 0x67
    TAB_LIST, // 0x68
    NBT_QUERY, // 0x69
    COLLECT_ITEM, // 0x6A
    ENTITY_TELEPORT, // 0x6B
    ADVANCEMENTS, // 0x6C
    ENTITY_PROPERTIES, // 0x6D
    ENTITY_EFFECT, // 0x6E
    DECLARE_RECIPES, // 0x6F
    TAGS; // 0x70

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
