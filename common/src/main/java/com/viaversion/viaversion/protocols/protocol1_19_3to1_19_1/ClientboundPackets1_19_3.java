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
package com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1;

import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;

public enum ClientboundPackets1_19_3 implements ClientboundPacketType {

    SPAWN_ENTITY, // 0x00
    SPAWN_EXPERIENCE_ORB, // 0x01
    SPAWN_PLAYER, // 0x02
    ENTITY_ANIMATION, // 0x03
    STATISTICS, // 0x04
    BLOCK_CHANGED_ACK, // 0x05
    BLOCK_BREAK_ANIMATION, // 0x06
    BLOCK_ENTITY_DATA, // 0x07
    BLOCK_ACTION, // 0x08
    BLOCK_CHANGE, // 0x09
    BOSSBAR, // 0x0A
    SERVER_DIFFICULTY, // 0x0B
    CLEAR_TITLES, // 0x0C
    TAB_COMPLETE, // 0x0D
    DECLARE_COMMANDS, // 0x0E
    CLOSE_WINDOW, // 0x0F
    WINDOW_ITEMS, // 0x10
    WINDOW_PROPERTY, // 0x11
    SET_SLOT, // 0x12
    COOLDOWN, // 0x13
    CUSTOM_CHAT_COMPLETIONS, // 0x14
    PLUGIN_MESSAGE, // 0x15
    DELETE_CHAT_MESSAGE, // 0x16
    DISCONNECT, // 0x17
    DISGUISED_CHAT, // 0x18
    ENTITY_STATUS, // 0x19
    EXPLOSION, // 0x1A
    UNLOAD_CHUNK, // 0x1B
    GAME_EVENT, // 0x1C
    OPEN_HORSE_WINDOW, // 0x1D
    WORLD_BORDER_INIT, // 0x1E
    KEEP_ALIVE, // 0x1F
    CHUNK_DATA, // 0x20
    EFFECT, // 0x21
    SPAWN_PARTICLE, // 0x22
    UPDATE_LIGHT, // 0x23
    JOIN_GAME, // 0x24
    MAP_DATA, // 0x25
    TRADE_LIST, // 0x26
    ENTITY_POSITION, // 0x27
    ENTITY_POSITION_AND_ROTATION, // 0x28
    ENTITY_ROTATION, // 0x29
    VEHICLE_MOVE, // 0x2A
    OPEN_BOOK, // 0x2B
    OPEN_WINDOW, // 0x2C
    OPEN_SIGN_EDITOR, // 0x2D
    PING, // 0x2E
    CRAFT_RECIPE_RESPONSE, // 0x2F
    PLAYER_ABILITIES, // 0x30
    PLAYER_CHAT, // 0x31
    COMBAT_END, // 0x32
    COMBAT_ENTER, // 0x33
    COMBAT_KILL, // 0x34
    PLAYER_INFO_REMOVE, // 0x35
    PLAYER_INFO_UPDATE, // 0x36
    FACE_PLAYER, // 0x37
    PLAYER_POSITION, // 0x38
    UNLOCK_RECIPES, // 0x39
    REMOVE_ENTITIES, // 0x3A
    REMOVE_ENTITY_EFFECT, // 0x3B
    RESOURCE_PACK, // 0x3C
    RESPAWN, // 0x3D
    ENTITY_HEAD_LOOK, // 0x3E
    MULTI_BLOCK_CHANGE, // 0x3F
    SELECT_ADVANCEMENTS_TAB, // 0x40
    SERVER_DATA, // 0x41
    ACTIONBAR, // 0x42
    WORLD_BORDER_CENTER, // 0x43
    WORLD_BORDER_LERP_SIZE, // 0x44
    WORLD_BORDER_SIZE, // 0x45
    WORLD_BORDER_WARNING_DELAY, // 0x46
    WORLD_BORDER_WARNING_DISTANCE, // 0x47
    CAMERA, // 0x48
    HELD_ITEM_CHANGE, // 0x49
    UPDATE_VIEW_POSITION, // 0x4A
    UPDATE_VIEW_DISTANCE, // 0x4B
    SPAWN_POSITION, // 0x4C
    DISPLAY_SCOREBOARD, // 0x4D
    ENTITY_METADATA, // 0x4E
    ATTACH_ENTITY, // 0x4F
    ENTITY_VELOCITY, // 0x50
    ENTITY_EQUIPMENT, // 0x51
    SET_EXPERIENCE, // 0x52
    UPDATE_HEALTH, // 0x53
    SCOREBOARD_OBJECTIVE, // 0x54
    SET_PASSENGERS, // 0x55
    TEAMS, // 0x56
    UPDATE_SCORE, // 0x57
    SET_SIMULATION_DISTANCE, // 0x58
    TITLE_SUBTITLE, // 0x59
    TIME_UPDATE, // 0x5A
    TITLE_TEXT, // 0x5B
    TITLE_TIMES, // 0x5C
    ENTITY_SOUND, // 0x5D
    SOUND, // 0x5E
    STOP_SOUND, // 0x5F
    SYSTEM_CHAT, // 0x60
    TAB_LIST, // 0x61
    NBT_QUERY, // 0x62
    COLLECT_ITEM, // 0x63
    ENTITY_TELEPORT, // 0x64
    ADVANCEMENTS, // 0x65
    ENTITY_PROPERTIES, // 0x66
    UPDATE_ENABLED_FEATURES, // 0x67
    ENTITY_EFFECT, // 0x68
    DECLARE_RECIPES, // 0x69
    TAGS; // 0x6A

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
