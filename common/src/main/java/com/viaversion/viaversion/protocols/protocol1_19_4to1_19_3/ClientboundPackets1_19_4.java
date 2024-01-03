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
package com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3;

import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;

public enum ClientboundPackets1_19_4 implements ClientboundPacketType {

    BUNDLE, // 0x00
    SPAWN_ENTITY, // 0x01
    SPAWN_EXPERIENCE_ORB, // 0x02
    SPAWN_PLAYER, // 0x03
    ENTITY_ANIMATION, // 0x04
    STATISTICS, // 0x05
    BLOCK_CHANGED_ACK, // 0x06
    BLOCK_BREAK_ANIMATION, // 0x07
    BLOCK_ENTITY_DATA, // 0x08
    BLOCK_ACTION, // 0x09
    BLOCK_CHANGE, // 0x0A
    BOSSBAR, // 0x0B
    SERVER_DIFFICULTY, // 0x0C
    CHUNK_BIOMES, // 0x0D
    CLEAR_TITLES, // 0x0E
    TAB_COMPLETE, // 0x0F
    DECLARE_COMMANDS, // 0x10
    CLOSE_WINDOW, // 0x11
    WINDOW_ITEMS, // 0x12
    WINDOW_PROPERTY, // 0x13
    SET_SLOT, // 0x14
    COOLDOWN, // 0x15
    CUSTOM_CHAT_COMPLETIONS, // 0x16
    PLUGIN_MESSAGE, // 0x17
    DAMAGE_EVENT, // 0x18
    DELETE_CHAT_MESSAGE, // 0x19
    DISCONNECT, // 0x1A
    DISGUISED_CHAT, // 0x1B
    ENTITY_STATUS, // 0x1C
    EXPLOSION, // 0x1D
    UNLOAD_CHUNK, // 0x1E
    GAME_EVENT, // 0x1F
    OPEN_HORSE_WINDOW, // 0x20
    HIT_ANIMATION, // 0x21
    WORLD_BORDER_INIT, // 0x22
    KEEP_ALIVE, // 0x23
    CHUNK_DATA, // 0x24
    EFFECT, // 0x25
    SPAWN_PARTICLE, // 0x26
    UPDATE_LIGHT, // 0x27
    JOIN_GAME, // 0x28
    MAP_DATA, // 0x29
    TRADE_LIST, // 0x2A
    ENTITY_POSITION, // 0x2B
    ENTITY_POSITION_AND_ROTATION, // 0x2C
    ENTITY_ROTATION, // 0x2D
    VEHICLE_MOVE, // 0x2E
    OPEN_BOOK, // 0x2F
    OPEN_WINDOW, // 0x30
    OPEN_SIGN_EDITOR, // 0x31
    PING, // 0x32
    CRAFT_RECIPE_RESPONSE, // 0x33
    PLAYER_ABILITIES, // 0x34
    PLAYER_CHAT, // 0x35
    COMBAT_END, // 0x36
    COMBAT_ENTER, // 0x37
    COMBAT_KILL, // 0x38
    PLAYER_INFO_REMOVE, // 0x39
    PLAYER_INFO_UPDATE, // 0x3A
    FACE_PLAYER, // 0x3B
    PLAYER_POSITION, // 0x3C
    UNLOCK_RECIPES, // 0x3D
    REMOVE_ENTITIES, // 0x3E
    REMOVE_ENTITY_EFFECT, // 0x3F
    RESOURCE_PACK, // 0x40
    RESPAWN, // 0x41
    ENTITY_HEAD_LOOK, // 0x42
    MULTI_BLOCK_CHANGE, // 0x43
    SELECT_ADVANCEMENTS_TAB, // 0x44
    SERVER_DATA, // 0x45
    ACTIONBAR, // 0x46
    WORLD_BORDER_CENTER, // 0x47
    WORLD_BORDER_LERP_SIZE, // 0x48
    WORLD_BORDER_SIZE, // 0x49
    WORLD_BORDER_WARNING_DELAY, // 0x4A
    WORLD_BORDER_WARNING_DISTANCE, // 0x4B
    CAMERA, // 0x4C
    HELD_ITEM_CHANGE, // 0x4D
    UPDATE_VIEW_POSITION, // 0x4E
    UPDATE_VIEW_DISTANCE, // 0x4F
    SPAWN_POSITION, // 0x50
    DISPLAY_SCOREBOARD, // 0x51
    ENTITY_METADATA, // 0x52
    ATTACH_ENTITY, // 0x53
    ENTITY_VELOCITY, // 0x54
    ENTITY_EQUIPMENT, // 0x55
    SET_EXPERIENCE, // 0x56
    UPDATE_HEALTH, // 0x57
    SCOREBOARD_OBJECTIVE, // 0x58
    SET_PASSENGERS, // 0x59
    TEAMS, // 0x5A
    UPDATE_SCORE, // 0x5B
    SET_SIMULATION_DISTANCE, // 0x5C
    TITLE_SUBTITLE, // 0x5D
    TIME_UPDATE, // 0x5E
    TITLE_TEXT, // 0x5F
    TITLE_TIMES, // 0x60
    ENTITY_SOUND, // 0x61
    SOUND, // 0x62
    STOP_SOUND, // 0x63
    SYSTEM_CHAT, // 0x64
    TAB_LIST, // 0x65
    NBT_QUERY, // 0x66
    COLLECT_ITEM, // 0x67
    ENTITY_TELEPORT, // 0x68
    ADVANCEMENTS, // 0x69
    ENTITY_PROPERTIES, // 0x6A
    UPDATE_ENABLED_FEATURES, // 0x6B
    ENTITY_EFFECT, // 0x6C
    DECLARE_RECIPES, // 0x6D
    TAGS; // 0x6E

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
