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
    CLEAR_TITLES, // 0x0D
    TAB_COMPLETE, // 0x0E
    DECLARE_COMMANDS, // 0x0F
    CLOSE_WINDOW, // 0x10
    WINDOW_ITEMS, // 0x11
    WINDOW_PROPERTY, // 0x12
    SET_SLOT, // 0x13
    COOLDOWN, // 0x14
    PLUGIN_MESSAGE, // 0x15
    NAMED_SOUND, // 0x16
    DISCONNECT, // 0x17
    ENTITY_STATUS, // 0x18
    EXPLOSION, // 0x19
    UNLOAD_CHUNK, // 0x1A
    GAME_EVENT, // 0x1B
    OPEN_HORSE_WINDOW, // 0x1C
    WORLD_BORDER_INIT, // 0x1D
    KEEP_ALIVE, // 0x1E
    CHUNK_DATA, // 0x1F
    EFFECT, // 0x20
    SPAWN_PARTICLE, // 0x21
    UPDATE_LIGHT, // 0x22
    JOIN_GAME, // 0x23
    MAP_DATA, // 0x24
    TRADE_LIST, // 0x25
    ENTITY_POSITION, // 0x26
    ENTITY_POSITION_AND_ROTATION, // 0x27
    ENTITY_ROTATION, // 0x28
    VEHICLE_MOVE, // 0x29
    OPEN_BOOK, // 0x2A
    OPEN_WINDOW, // 0x2B
    OPEN_SIGN_EDITOR, // 0x2C
    PING, // 0x2D
    CRAFT_RECIPE_RESPONSE, // 0x2E
    PLAYER_ABILITIES, // 0x2F
    PLAYER_CHAT,
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
    SYSTEM_CHAT,
    TAB_LIST, // 0x5C
    NBT_QUERY, // 0x5D
    COLLECT_ITEM, // 0x5E
    ENTITY_TELEPORT, // 0x5F
    ADVANCEMENTS, // 0x60
    ENTITY_PROPERTIES, // 0x61
    ENTITY_EFFECT, // 0x62
    DECLARE_RECIPES, // 0x63
    TAGS; // 0x64

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
