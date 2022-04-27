/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2022 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_19to1_18_2;

import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;

public enum ClientboundPackets1_19 implements ClientboundPacketType {

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
    PLUGIN_MESSAGE, // 0x14
    NAMED_SOUND, // 0x15
    DISCONNECT, // 0x16
    ENTITY_STATUS, // 0x17
    EXPLOSION, // 0x18
    UNLOAD_CHUNK, // 0x19
    GAME_EVENT, // 0x1A
    OPEN_HORSE_WINDOW, // 0x1B
    WORLD_BORDER_INIT, // 0x1C
    KEEP_ALIVE, // 0x1D
    CHUNK_DATA, // 0x1E
    EFFECT, // 0x1F
    SPAWN_PARTICLE, // 0x20
    UPDATE_LIGHT, // 0x21
    JOIN_GAME, // 0x22
    MAP_DATA, // 0x23
    TRADE_LIST, // 0x24
    ENTITY_POSITION, // 0x25
    ENTITY_POSITION_AND_ROTATION, // 0x26
    ENTITY_ROTATION, // 0x27
    VEHICLE_MOVE, // 0x28
    OPEN_BOOK, // 0x29
    OPEN_WINDOW, // 0x2A
    OPEN_SIGN_EDITOR, // 0x2B
    PING, // 0x2C
    CRAFT_RECIPE_RESPONSE, // 0x2D
    PLAYER_ABILITIES, // 0x2E
    PLAYER_CHAT, // 0x2F
    COMBAT_END, // 0x30
    COMBAT_ENTER, // 0x31
    COMBAT_KILL, // 0x32
    PLAYER_INFO, // 0x33
    FACE_PLAYER, // 0x34
    PLAYER_POSITION, // 0x35
    UNLOCK_RECIPES, // 0x36
    REMOVE_ENTITIES, // 0x37
    REMOVE_ENTITY_EFFECT, // 0x38
    RESOURCE_PACK, // 0x39
    RESPAWN, // 0x3A
    ENTITY_HEAD_LOOK, // 0x3B
    MULTI_BLOCK_CHANGE, // 0x3C
    SELECT_ADVANCEMENTS_TAB, // 0x3D
    ACTIONBAR, // 0x3E
    WORLD_BORDER_CENTER, // 0x3F
    WORLD_BORDER_LERP_SIZE, // 0x40
    WORLD_BORDER_SIZE, // 0x41
    WORLD_BORDER_WARNING_DELAY, // 0x42
    WORLD_BORDER_WARNING_DISTANCE, // 0x43
    CAMERA, // 0x44
    HELD_ITEM_CHANGE, // 0x45
    UPDATE_VIEW_POSITION, // 0x46
    UPDATE_VIEW_DISTANCE, // 0x47
    SPAWN_POSITION, // 0x48
    DISPLAY_SCOREBOARD, // 0x49
    ENTITY_METADATA, // 0x4A
    ATTACH_ENTITY, // 0x4B
    ENTITY_VELOCITY, // 0x4C
    ENTITY_EQUIPMENT, // 0x4D
    SET_EXPERIENCE, // 0x4E
    UPDATE_HEALTH, // 0x4F
    SCOREBOARD_OBJECTIVE, // 0x50
    SET_PASSENGERS, // 0x51
    TEAMS, // 0x52
    UPDATE_SCORE, // 0x53
    SET_SIMULATION_DISTANCE, // 0x54
    TITLE_SUBTITLE, // 0x55
    TIME_UPDATE, // 0x56
    TITLE_TEXT, // 0x57
    TITLE_TIMES, // 0x58
    ENTITY_SOUND, // 0x59
    SOUND, // 0x5A
    STOP_SOUND, // 0x5B
    SYSTEM_CHAT, // 0x5C
    TAB_LIST, // 0x5D
    NBT_QUERY, // 0x5E
    COLLECT_ITEM, // 0x5F
    ENTITY_TELEPORT, // 0x60
    ADVANCEMENTS, // 0x61
    ENTITY_PROPERTIES, // 0x62
    ENTITY_EFFECT, // 0x63
    DECLARE_RECIPES, // 0x64
    TAGS; // 0x65

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
