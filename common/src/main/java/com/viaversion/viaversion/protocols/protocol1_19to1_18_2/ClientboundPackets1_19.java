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
    SPAWN_PAINTING, // 0x02
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
    CHAT_MESSAGE, // 0x0D
    CLEAR_TITLES, // 0x0E
    TAB_COMPLETE, // 0x0F
    DECLARE_COMMANDS, // 0x10
    CLOSE_WINDOW, // 0x11
    WINDOW_ITEMS, // 0x12
    WINDOW_PROPERTY, // 0x13
    SET_SLOT, // 0x14
    COOLDOWN, // 0x15
    PLUGIN_MESSAGE, // 0x16
    NAMED_SOUND, // 0x17
    DISCONNECT, // 0x18
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
    COMBAT_END, // 0x31
    COMBAT_ENTER, // 0x32
    COMBAT_KILL, // 0x33
    PLAYER_INFO, // 0x34
    FACE_PLAYER, // 0x35
    PLAYER_POSITION, // 0x36
    UNLOCK_RECIPES, // 0x37
    REMOVE_ENTITIES, // 0x38
    REMOVE_ENTITY_EFFECT, // 0x39
    RESOURCE_PACK, // 0x3A
    RESPAWN, // 0x3B
    ENTITY_HEAD_LOOK, // 0x3C
    MULTI_BLOCK_CHANGE, // 0x3D
    SELECT_ADVANCEMENTS_TAB, // 0x3E
    ACTIONBAR, // 0x3F
    WORLD_BORDER_CENTER, // 0x40
    WORLD_BORDER_LERP_SIZE, // 0x41
    WORLD_BORDER_SIZE, // 0x42
    WORLD_BORDER_WARNING_DELAY, // 0x43
    WORLD_BORDER_WARNING_DISTANCE, // 0x44
    CAMERA, // 0x45
    HELD_ITEM_CHANGE, // 0x46
    UPDATE_VIEW_POSITION, // 0x47
    UPDATE_VIEW_DISTANCE, // 0x48
    SPAWN_POSITION, // 0x49
    DISPLAY_SCOREBOARD, // 0x4A
    ENTITY_METADATA, // 0x4B
    ATTACH_ENTITY, // 0x4C
    ENTITY_VELOCITY, // 0x4D
    ENTITY_EQUIPMENT, // 0x4E
    SET_EXPERIENCE, // 0x4F
    UPDATE_HEALTH, // 0x50
    SCOREBOARD_OBJECTIVE, // 0x51
    SET_PASSENGERS, // 0x52
    TEAMS, // 0x53
    UPDATE_SCORE, // 0x54
    SET_SIMULATION_DISTANCE, // 0x55
    TITLE_SUBTITLE, // 0x56
    TIME_UPDATE, // 0x57
    TITLE_TEXT, // 0x58
    TITLE_TIMES, // 0x59
    ENTITY_SOUND, // 0x5A
    SOUND, // 0x5B
    STOP_SOUND, // 0x5C
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
