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
    DAMAGE_EVENT, // 0x17
    DELETE_CHAT_MESSAGE, // 0x18
    DISCONNECT, // 0x19
    DISGUISED_CHAT, // 0x1A
    ENTITY_STATUS, // 0x1B
    EXPLOSION, // 0x1C
    UNLOAD_CHUNK, // 0x1D
    GAME_EVENT, // 0x1E
    OPEN_HORSE_WINDOW, // 0x1F
    HIT_ANIMATION, // 0x20
    WORLD_BORDER_INIT, // 0x21
    KEEP_ALIVE, // 0x22
    CHUNK_DATA, // 0x23
    EFFECT, // 0x24
    SPAWN_PARTICLE, // 0x25
    UPDATE_LIGHT, // 0x26
    JOIN_GAME, // 0x27
    MAP_DATA, // 0x28
    TRADE_LIST, // 0x29
    ENTITY_POSITION, // 0x2A
    ENTITY_POSITION_AND_ROTATION, // 0x2B
    ENTITY_ROTATION, // 0x2C
    VEHICLE_MOVE, // 0x2D
    OPEN_BOOK, // 0x2E
    OPEN_WINDOW, // 0x2F
    OPEN_SIGN_EDITOR, // 0x30
    PING, // 0x31
    CRAFT_RECIPE_RESPONSE, // 0x32
    PLAYER_ABILITIES, // 0x33
    PLAYER_CHAT, // 0x34
    COMBAT_END, // 0x35
    COMBAT_ENTER, // 0x36
    COMBAT_KILL, // 0x37
    PLAYER_INFO_REMOVE, // 0x38
    PLAYER_INFO_UPDATE, // 0x39
    FACE_PLAYER, // 0x3A
    PLAYER_POSITION, // 0x3B
    UNLOCK_RECIPES, // 0x3C
    REMOVE_ENTITIES, // 0x3D
    REMOVE_ENTITY_EFFECT, // 0x3E
    RESOURCE_PACK, // 0x3F
    RESPAWN, // 0x40
    ENTITY_HEAD_LOOK, // 0x41
    MULTI_BLOCK_CHANGE, // 0x42
    SELECT_ADVANCEMENTS_TAB, // 0x43
    SERVER_DATA, // 0x44
    ACTIONBAR, // 0x45
    WORLD_BORDER_CENTER, // 0x46
    WORLD_BORDER_LERP_SIZE, // 0x47
    WORLD_BORDER_SIZE, // 0x48
    WORLD_BORDER_WARNING_DELAY, // 0x49
    WORLD_BORDER_WARNING_DISTANCE, // 0x4A
    CAMERA, // 0x4B
    HELD_ITEM_CHANGE, // 0x4C
    UPDATE_VIEW_POSITION, // 0x4D
    UPDATE_VIEW_DISTANCE, // 0x4E
    SPAWN_POSITION, // 0x4F
    DISPLAY_SCOREBOARD, // 0x50
    ENTITY_METADATA, // 0x51
    ATTACH_ENTITY, // 0x52
    ENTITY_VELOCITY, // 0x53
    ENTITY_EQUIPMENT, // 0x54
    SET_EXPERIENCE, // 0x55
    UPDATE_HEALTH, // 0x56
    SCOREBOARD_OBJECTIVE, // 0x57
    SET_PASSENGERS, // 0x58
    TEAMS, // 0x59
    UPDATE_SCORE, // 0x5A
    SET_SIMULATION_DISTANCE, // 0x5B
    TITLE_SUBTITLE, // 0x5C
    TIME_UPDATE, // 0x5D
    TITLE_TEXT, // 0x5E
    TITLE_TIMES, // 0x5F
    ENTITY_SOUND, // 0x60
    SOUND, // 0x61
    STOP_SOUND, // 0x62
    SYSTEM_CHAT, // 0x63
    TAB_LIST, // 0x64
    NBT_QUERY, // 0x65
    COLLECT_ITEM, // 0x66
    ENTITY_TELEPORT, // 0x67
    ADVANCEMENTS, // 0x68
    ENTITY_PROPERTIES, // 0x69
    UPDATE_ENABLED_FEATURES, // 0x6A
    ENTITY_EFFECT, // 0x6B
    DECLARE_RECIPES, // 0x6C
    TAGS; // 0x6D

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
