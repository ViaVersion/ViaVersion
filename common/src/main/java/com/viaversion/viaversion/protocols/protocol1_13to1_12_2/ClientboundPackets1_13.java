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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2;

import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;

public enum ClientboundPackets1_13 implements ClientboundPacketType {

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
    CHAT_MESSAGE, // 0x0E
    MULTI_BLOCK_CHANGE, // 0x0F
    TAB_COMPLETE, // 0x10
    DECLARE_COMMANDS, // 0x11
    WINDOW_CONFIRMATION, // 0x12
    CLOSE_WINDOW, // 0x13
    OPEN_WINDOW, // 0x14
    WINDOW_ITEMS, // 0x15
    WINDOW_PROPERTY, // 0x16
    SET_SLOT, // 0x17
    COOLDOWN, // 0x18
    PLUGIN_MESSAGE, // 0x19
    NAMED_SOUND, // 0x1A
    DISCONNECT, // 0x1B
    ENTITY_STATUS, // 0x1C
    NBT_QUERY, // 0x1D
    EXPLOSION, // 0x1E
    UNLOAD_CHUNK, // 0x1F
    GAME_EVENT, // 0x20
    KEEP_ALIVE, // 0x21
    CHUNK_DATA, // 0x22
    EFFECT, // 0x23
    SPAWN_PARTICLE, // 0x24
    JOIN_GAME, // 0x25
    MAP_DATA, // 0x26
    ENTITY_MOVEMENT, // 0x27
    ENTITY_POSITION, // 0x28
    ENTITY_POSITION_AND_ROTATION, // 0x29
    ENTITY_ROTATION, // 0x2A
    VEHICLE_MOVE, // 0x2B
    OPEN_SIGN_EDITOR, // 0x2C
    CRAFT_RECIPE_RESPONSE, // 0x2D
    PLAYER_ABILITIES, // 0x2E
    COMBAT_EVENT, // 0x2F
    PLAYER_INFO, // 0x30
    FACE_PLAYER, // 0x31
    PLAYER_POSITION, // 0x32
    USE_BED, // 0x33
    UNLOCK_RECIPES, // 0x34
    DESTROY_ENTITIES, // 0x35
    REMOVE_ENTITY_EFFECT, // 0x36
    RESOURCE_PACK, // 0x37
    RESPAWN, // 0x38
    ENTITY_HEAD_LOOK, // 0x39
    SELECT_ADVANCEMENTS_TAB, // 0x3A
    WORLD_BORDER, // 0x3B
    CAMERA, // 0x3C
    HELD_ITEM_CHANGE, // 0x3D
    DISPLAY_SCOREBOARD, // 0x3E
    ENTITY_METADATA, // 0x3F
    ATTACH_ENTITY, // 0x40
    ENTITY_VELOCITY, // 0x41
    ENTITY_EQUIPMENT, // 0x42
    SET_EXPERIENCE, // 0x43
    UPDATE_HEALTH, // 0x44
    SCOREBOARD_OBJECTIVE, // 0x45
    SET_PASSENGERS, // 0x46
    TEAMS, // 0x47
    UPDATE_SCORE, // 0x48
    SPAWN_POSITION, // 0x49
    TIME_UPDATE, // 0x4A
    TITLE, // 0x4B
    STOP_SOUND, // 0x4C
    SOUND, // 0x4D
    TAB_LIST, // 0x4E
    COLLECT_ITEM, // 0x4F
    ENTITY_TELEPORT, // 0x50
    ADVANCEMENTS, // 0x51
    ENTITY_PROPERTIES, // 0x52
    ENTITY_EFFECT, // 0x53
    DECLARE_RECIPES, // 0x54
    TAGS; // 0x55

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
