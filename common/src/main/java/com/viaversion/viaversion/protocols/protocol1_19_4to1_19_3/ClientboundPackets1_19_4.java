/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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
    CLEAR_TITLES, // 0x0D
    TAB_COMPLETE, // 0x0E
    DECLARE_COMMANDS, // 0x0F
    CLOSE_WINDOW, // 0x10
    WINDOW_ITEMS, // 0x11
    WINDOW_PROPERTY, // 0x12
    SET_SLOT, // 0x13
    COOLDOWN, // 0x14
    CUSTOM_CHAT_COMPLETIONS, // 0x15
    PLUGIN_MESSAGE, // 0x16
    DELETE_CHAT_MESSAGE, // 0x17
    DISCONNECT, // 0x18
    DISGUISED_CHAT, // 0x19
    ENTITY_STATUS, // 0x1A
    EXPLOSION, // 0x1B
    UNLOAD_CHUNK, // 0x1C
    GAME_EVENT, // 0x1D
    OPEN_HORSE_WINDOW, // 0x1E
    HIT_ANIMATION, // 0x1F
    WORLD_BORDER_INIT, // 0x20
    KEEP_ALIVE, // 0x21
    CHUNK_DATA, // 0x22
    EFFECT, // 0x23
    SPAWN_PARTICLE, // 0x24
    UPDATE_LIGHT, // 0x25
    JOIN_GAME, // 0x26
    MAP_DATA, // 0x27
    TRADE_LIST, // 0x28
    ENTITY_POSITION, // 0x29
    ENTITY_POSITION_AND_ROTATION, // 0x2A
    ENTITY_ROTATION, // 0x2B
    VEHICLE_MOVE, // 0x2C
    OPEN_BOOK, // 0x2D
    OPEN_WINDOW, // 0x2E
    OPEN_SIGN_EDITOR, // 0x2F
    PING, // 0x30
    CRAFT_RECIPE_RESPONSE, // 0x31
    PLAYER_ABILITIES, // 0x32
    PLAYER_CHAT, // 0x33
    COMBAT_END, // 0x34
    COMBAT_ENTER, // 0x35
    COMBAT_KILL, // 0x36
    PLAYER_INFO_REMOVE, // 0x37
    PLAYER_INFO_UPDATE, // 0x38
    FACE_PLAYER, // 0x39
    PLAYER_POSITION, // 0x3A
    UNLOCK_RECIPES, // 0x3B
    REMOVE_ENTITIES, // 0x3C
    REMOVE_ENTITY_EFFECT, // 0x3D
    RESOURCE_PACK, // 0x3E
    RESPAWN, // 0x3F
    ENTITY_HEAD_LOOK, // 0x40
    MULTI_BLOCK_CHANGE, // 0x41
    SELECT_ADVANCEMENTS_TAB, // 0x42
    SERVER_DATA, // 0x43
    ACTIONBAR, // 0x44
    WORLD_BORDER_CENTER, // 0x45
    WORLD_BORDER_LERP_SIZE, // 0x46
    WORLD_BORDER_SIZE, // 0x47
    WORLD_BORDER_WARNING_DELAY, // 0x48
    WORLD_BORDER_WARNING_DISTANCE, // 0x49
    CAMERA, // 0x4A
    HELD_ITEM_CHANGE, // 0x4B
    UPDATE_VIEW_POSITION, // 0x4C
    UPDATE_VIEW_DISTANCE, // 0x4D
    SPAWN_POSITION, // 0x4E
    DISPLAY_SCOREBOARD, // 0x4F
    ENTITY_METADATA, // 0x50
    ATTACH_ENTITY, // 0x51
    ENTITY_VELOCITY, // 0x52
    ENTITY_EQUIPMENT, // 0x53
    SET_EXPERIENCE, // 0x54
    UPDATE_HEALTH, // 0x55
    SCOREBOARD_OBJECTIVE, // 0x56
    SET_PASSENGERS, // 0x57
    TEAMS, // 0x58
    UPDATE_SCORE, // 0x59
    SET_SIMULATION_DISTANCE, // 0x5A
    TITLE_SUBTITLE, // 0x5B
    TIME_UPDATE, // 0x5C
    TITLE_TEXT, // 0x5D
    TITLE_TIMES, // 0x5E
    ENTITY_SOUND, // 0x5F
    SOUND, // 0x60
    STOP_SOUND, // 0x61
    SYSTEM_CHAT, // 0x62
    TAB_LIST, // 0x63
    NBT_QUERY, // 0x64
    COLLECT_ITEM, // 0x65
    ENTITY_TELEPORT, // 0x66
    ADVANCEMENTS, // 0x67
    ENTITY_PROPERTIES, // 0x68
    UPDATE_ENABLED_FEATURES, // 0x69
    ENTITY_EFFECT, // 0x6A
    DECLARE_RECIPES, // 0x6B
    TAGS; // 0x6C

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
