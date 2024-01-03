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
package com.viaversion.viaversion.protocols.protocol1_17to1_16_4;

import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;

public enum ClientboundPackets1_17 implements ClientboundPacketType {

    SPAWN_ENTITY, // 0x00
    SPAWN_EXPERIENCE_ORB, // 0x01
    SPAWN_MOB, // 0x02
    SPAWN_PAINTING, // 0x03
    SPAWN_PLAYER, // 0x04
    ADD_VIBRATION_SIGNAL, // 0x05
    ENTITY_ANIMATION, // 0x06
    STATISTICS, // 0x07
    ACKNOWLEDGE_PLAYER_DIGGING, // 0x08
    BLOCK_BREAK_ANIMATION, // 0x09
    BLOCK_ENTITY_DATA, // 0x0A
    BLOCK_ACTION, // 0x0B
    BLOCK_CHANGE, // 0x0C
    BOSSBAR, // 0x0D
    SERVER_DIFFICULTY, // 0x0E
    CHAT_MESSAGE, // 0x0F
    CLEAR_TITLES, // 0x10
    TAB_COMPLETE, // 0x11
    DECLARE_COMMANDS, // 0x12
    CLOSE_WINDOW, // 0x13
    WINDOW_ITEMS, // 0x14
    WINDOW_PROPERTY, // 0x15
    SET_SLOT, // 0x16
    COOLDOWN, // 0x17
    PLUGIN_MESSAGE, // 0x18
    NAMED_SOUND, // 0x19
    DISCONNECT, // 0x1A
    ENTITY_STATUS, // 0x1B
    EXPLOSION, // 0x1C
    UNLOAD_CHUNK, // 0x1D
    GAME_EVENT, // 0x1E
    OPEN_HORSE_WINDOW, // 0x1F
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
    COMBAT_END, // 0x33
    COMBAT_ENTER, // 0x34
    COMBAT_KILL, // 0x35
    PLAYER_INFO, // 0x36
    FACE_PLAYER, // 0x37
    PLAYER_POSITION, // 0x38
    UNLOCK_RECIPES, // 0x39
    REMOVE_ENTITY, // 0x3A
    REMOVE_ENTITY_EFFECT, // 0x3B
    RESOURCE_PACK, // 0x3C
    RESPAWN, // 0x3D
    ENTITY_HEAD_LOOK, // 0x3E
    MULTI_BLOCK_CHANGE, // 0x3F
    SELECT_ADVANCEMENTS_TAB, // 0x40
    ACTIONBAR, // 0x41
    WORLD_BORDER_CENTER, // 0x42
    WORLD_BORDER_LERP_SIZE, // 0x43
    WORLD_BORDER_SIZE, // 0x44
    WORLD_BORDER_WARNING_DELAY, // 0x45
    WORLD_BORDER_WARNING_DISTANCE, // 0x46
    CAMERA, // 0x47
    HELD_ITEM_CHANGE, // 0x48
    UPDATE_VIEW_POSITION, // 0x49
    UPDATE_VIEW_DISTANCE, // 0x4A
    SPAWN_POSITION, // 0x4B
    DISPLAY_SCOREBOARD, // 0x4C
    ENTITY_METADATA, // 0x4D
    ATTACH_ENTITY, // 0x4E
    ENTITY_VELOCITY, // 0x4F
    ENTITY_EQUIPMENT, // 0x50
    SET_EXPERIENCE, // 0x51
    UPDATE_HEALTH, // 0x52
    SCOREBOARD_OBJECTIVE, // 0x53
    SET_PASSENGERS, // 0x54
    TEAMS, // 0x55
    UPDATE_SCORE, // 0x56
    TITLE_SUBTITLE, // 0x57
    TIME_UPDATE, // 0x58
    TITLE_TEXT, // 0x59
    TITLE_TIMES, // 0x5A
    ENTITY_SOUND, // 0x5B
    SOUND, // 0x5C
    STOP_SOUND, // 0x5D
    TAB_LIST, // 0x5E
    NBT_QUERY, // 0x5F
    COLLECT_ITEM, // 0x60
    ENTITY_TELEPORT, // 0x61
    ADVANCEMENTS, // 0x62
    ENTITY_PROPERTIES, // 0x63
    ENTITY_EFFECT, // 0x64
    DECLARE_RECIPES, // 0x65
    TAGS; // 0x66

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
