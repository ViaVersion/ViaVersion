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
package com.viaversion.viaversion.protocols.protocol1_15to1_14_4;

import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;

public enum ClientboundPackets1_15 implements ClientboundPacketType {

    SPAWN_ENTITY, // 0x00
    SPAWN_EXPERIENCE_ORB, // 0x01
    SPAWN_GLOBAL_ENTITY, // 0x02
    SPAWN_MOB, // 0x03
    SPAWN_PAINTING, // 0x04
    SPAWN_PLAYER, // 0x05
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
    MULTI_BLOCK_CHANGE, // 0x10
    TAB_COMPLETE, // 0x11
    DECLARE_COMMANDS, // 0x12
    WINDOW_CONFIRMATION, // 0x13
    CLOSE_WINDOW, // 0x14
    WINDOW_ITEMS, // 0x15
    WINDOW_PROPERTY, // 0x16
    SET_SLOT, // 0x17
    COOLDOWN, // 0x18
    PLUGIN_MESSAGE, // 0x19
    NAMED_SOUND, // 0x1A
    DISCONNECT, // 0x1B
    ENTITY_STATUS, // 0x1C
    EXPLOSION, // 0x1D
    UNLOAD_CHUNK, // 0x1E
    GAME_EVENT, // 0x1F
    OPEN_HORSE_WINDOW, // 0x20
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
    ENTITY_MOVEMENT, // 0x2C
    VEHICLE_MOVE, // 0x2D
    OPEN_BOOK, // 0x2E
    OPEN_WINDOW, // 0x2F
    OPEN_SIGN_EDITOR, // 0x30
    CRAFT_RECIPE_RESPONSE, // 0x31
    PLAYER_ABILITIES, // 0x32
    COMBAT_EVENT, // 0x33
    PLAYER_INFO, // 0x34
    FACE_PLAYER, // 0x35
    PLAYER_POSITION, // 0x36
    UNLOCK_RECIPES, // 0x37
    DESTROY_ENTITIES, // 0x38
    REMOVE_ENTITY_EFFECT, // 0x39
    RESOURCE_PACK, // 0x3A
    RESPAWN, // 0x3B
    ENTITY_HEAD_LOOK, // 0x3C
    SELECT_ADVANCEMENTS_TAB, // 0x3D
    WORLD_BORDER, // 0x3E
    CAMERA, // 0x3F
    HELD_ITEM_CHANGE, // 0x40
    UPDATE_VIEW_POSITION, // 0x41
    UPDATE_VIEW_DISTANCE, // 0x42
    DISPLAY_SCOREBOARD, // 0x43
    ENTITY_METADATA, // 0x44
    ATTACH_ENTITY, // 0x45
    ENTITY_VELOCITY, // 0x46
    ENTITY_EQUIPMENT, // 0x47
    SET_EXPERIENCE, // 0x48
    UPDATE_HEALTH, // 0x49
    SCOREBOARD_OBJECTIVE, // 0x4A
    SET_PASSENGERS, // 0x4B
    TEAMS, // 0x4C
    UPDATE_SCORE, // 0x4D
    SPAWN_POSITION, // 0x4E
    TIME_UPDATE, // 0x4F
    TITLE, // 0x50
    ENTITY_SOUND, // 0x51
    SOUND, // 0x52
    STOP_SOUND, // 0x53
    TAB_LIST, // 0x54
    NBT_QUERY, // 0x55
    COLLECT_ITEM, // 0x56
    ENTITY_TELEPORT, // 0x57
    ADVANCEMENTS, // 0x58
    ENTITY_PROPERTIES, // 0x59
    ENTITY_EFFECT, // 0x5A
    DECLARE_RECIPES, // 0x5B
    TAGS; // 0x5C

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
