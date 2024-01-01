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
package com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1;

import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;

public enum ClientboundPackets1_16_2 implements ClientboundPacketType {

    SPAWN_ENTITY, // 0x00
    SPAWN_EXPERIENCE_ORB, // 0x01
    SPAWN_MOB, // 0x02
    SPAWN_PAINTING, // 0x03
    SPAWN_PLAYER, // 0x04
    ENTITY_ANIMATION, // 0x05
    STATISTICS, // 0x06
    ACKNOWLEDGE_PLAYER_DIGGING, // 0x07
    BLOCK_BREAK_ANIMATION, // 0x08
    BLOCK_ENTITY_DATA, // 0x09
    BLOCK_ACTION, // 0x0A
    BLOCK_CHANGE, // 0x0B
    BOSSBAR, // 0x0C
    SERVER_DIFFICULTY, // 0x0D
    CHAT_MESSAGE, // 0x0E
    TAB_COMPLETE, // 0x0F
    DECLARE_COMMANDS, // 0x10
    WINDOW_CONFIRMATION, // 0x11
    CLOSE_WINDOW, // 0x12
    WINDOW_ITEMS, // 0x13
    WINDOW_PROPERTY, // 0x14
    SET_SLOT, // 0x15
    COOLDOWN, // 0x16
    PLUGIN_MESSAGE, // 0x17
    NAMED_SOUND, // 0x18
    DISCONNECT, // 0x19
    ENTITY_STATUS, // 0x1A
    EXPLOSION, // 0x1B
    UNLOAD_CHUNK, // 0x1C
    GAME_EVENT, // 0x1D
    OPEN_HORSE_WINDOW, // 0x1E
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
    ENTITY_MOVEMENT, // 0x2A
    VEHICLE_MOVE, // 0x2B
    OPEN_BOOK, // 0x2C
    OPEN_WINDOW, // 0x2D
    OPEN_SIGN_EDITOR, // 0x2E
    CRAFT_RECIPE_RESPONSE, // 0x2F
    PLAYER_ABILITIES, // 0x30
    COMBAT_EVENT, // 0x31
    PLAYER_INFO, // 0x32
    FACE_PLAYER, // 0x33
    PLAYER_POSITION, // 0x34
    UNLOCK_RECIPES, // 0x35
    DESTROY_ENTITIES, // 0x36
    REMOVE_ENTITY_EFFECT, // 0x37
    RESOURCE_PACK, // 0x38
    RESPAWN, // 0x39
    ENTITY_HEAD_LOOK, // 0x3A
    MULTI_BLOCK_CHANGE, // 0x3B
    SELECT_ADVANCEMENTS_TAB, // 0x3C
    WORLD_BORDER, // 0x3D
    CAMERA, // 0x3E
    HELD_ITEM_CHANGE, // 0x3F
    UPDATE_VIEW_POSITION, // 0x40
    UPDATE_VIEW_DISTANCE, // 0x41
    SPAWN_POSITION, // 0x42
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
    TIME_UPDATE, // 0x4E
    TITLE, // 0x4F
    ENTITY_SOUND, // 0x50
    SOUND, // 0x51
    STOP_SOUND, // 0x52
    TAB_LIST, // 0x53
    NBT_QUERY, // 0x54
    COLLECT_ITEM, // 0x55
    ENTITY_TELEPORT, // 0x56
    ADVANCEMENTS, // 0x57
    ENTITY_PROPERTIES, // 0x58
    ENTITY_EFFECT, // 0x59
    DECLARE_RECIPES, // 0x5A
    TAGS; // 0x5B

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
