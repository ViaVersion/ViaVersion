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
package com.viaversion.viaversion.protocols.protocol1_9to1_8;

import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;

public enum ClientboundPackets1_9 implements ClientboundPacketType {

    SPAWN_ENTITY, // 0x00
    SPAWN_EXPERIENCE_ORB, // 0x01
    SPAWN_GLOBAL_ENTITY, // 0x02
    SPAWN_MOB, // 0x03
    SPAWN_PAINTING, // 0x04
    SPAWN_PLAYER, // 0x05
    ENTITY_ANIMATION, // 0x06
    STATISTICS, // 0x07
    BLOCK_BREAK_ANIMATION, // 0x08
    BLOCK_ENTITY_DATA, // 0x09
    BLOCK_ACTION, // 0x0A
    BLOCK_CHANGE, // 0x0B
    BOSSBAR, // 0x0C
    SERVER_DIFFICULTY, // 0x0D
    TAB_COMPLETE, // 0x0E
    CHAT_MESSAGE, // 0x0F
    MULTI_BLOCK_CHANGE, // 0x10
    WINDOW_CONFIRMATION, // 0x11
    CLOSE_WINDOW, // 0x12
    OPEN_WINDOW, // 0x13
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
    KEEP_ALIVE, // 0x1F
    CHUNK_DATA, // 0x20
    EFFECT, // 0x21
    SPAWN_PARTICLE, // 0x22
    JOIN_GAME, // 0x23
    MAP_DATA, // 0x24
    ENTITY_POSITION, // 0x25
    ENTITY_POSITION_AND_ROTATION, // 0x26
    ENTITY_ROTATION, // 0x27
    ENTITY_MOVEMENT, // 0x28
    VEHICLE_MOVE, // 0x29
    OPEN_SIGN_EDITOR, // 0x2A
    PLAYER_ABILITIES, // 0x2B
    COMBAT_EVENT, // 0x2C
    PLAYER_INFO, // 0x2D
    PLAYER_POSITION, // 0x2E
    USE_BED, // 0x2F
    DESTROY_ENTITIES, // 0x30
    REMOVE_ENTITY_EFFECT, // 0x31
    RESOURCE_PACK, // 0x32
    RESPAWN, // 0x33
    ENTITY_HEAD_LOOK, // 0x34
    WORLD_BORDER, // 0x35
    CAMERA, // 0x36
    HELD_ITEM_CHANGE, // 0x37
    DISPLAY_SCOREBOARD, // 0x38
    ENTITY_METADATA, // 0x39
    ATTACH_ENTITY, // 0x3A
    ENTITY_VELOCITY, // 0x3B
    ENTITY_EQUIPMENT, // 0x3C
    SET_EXPERIENCE, // 0x3D
    UPDATE_HEALTH, // 0x3E
    SCOREBOARD_OBJECTIVE, // 0x3F
    SET_PASSENGERS, // 0x40
    TEAMS, // 0x41
    UPDATE_SCORE, // 0x42
    SPAWN_POSITION, // 0x43
    TIME_UPDATE, // 0x44
    TITLE, // 0x45
    UPDATE_SIGN, // 0x46
    SOUND, // 0x47
    TAB_LIST, // 0x48
    COLLECT_ITEM, // 0x49
    ENTITY_TELEPORT, // 0x4A
    ENTITY_PROPERTIES, // 0x4B
    ENTITY_EFFECT; // 0x4C

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
