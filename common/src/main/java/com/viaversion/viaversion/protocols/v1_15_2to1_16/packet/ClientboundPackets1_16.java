/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_15_2to1_16.packet;

import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;

public enum ClientboundPackets1_16 implements ClientboundPacketType {

    ADD_ENTITY, // 0x00
    ADD_EXPERIENCE_ORB, // 0x01
    ADD_MOB, // 0x02
    ADD_PAINTING, // 0x03
    ADD_PLAYER, // 0x04
    ANIMATE, // 0x05
    AWARD_STATS, // 0x06
    BLOCK_BREAK_ACK, // 0x07
    BLOCK_DESTRUCTION, // 0x08
    BLOCK_ENTITY_DATA, // 0x09
    BLOCK_EVENT, // 0x0A
    BLOCK_UPDATE, // 0x0B
    BOSS_EVENT, // 0x0C
    CHANGE_DIFFICULTY, // 0x0D
    CHAT, // 0x0E
    CHUNK_BLOCKS_UPDATE, // 0x0F
    COMMAND_SUGGESTIONS, // 0x10
    COMMANDS, // 0x11
    CONTAINER_ACK, // 0x12
    CONTAINER_CLOSE, // 0x13
    CONTAINER_SET_CONTENT, // 0x14
    CONTAINER_SET_DATA, // 0x15
    CONTAINER_SET_SLOT, // 0x16
    COOLDOWN, // 0x17
    CUSTOM_PAYLOAD, // 0x18
    CUSTOM_SOUND, // 0x19
    DISCONNECT, // 0x1A
    ENTITY_EVENT, // 0x1B
    EXPLODE, // 0x1C
    FORGET_LEVEL_CHUNK, // 0x1D
    GAME_EVENT, // 0x1E
    HORSE_SCREEN_OPEN, // 0x1F
    KEEP_ALIVE, // 0x20
    LEVEL_CHUNK, // 0x21
    LEVEL_EVENT, // 0x22
    LEVEL_PARTICLES, // 0x23
    LIGHT_UPDATE, // 0x24
    LOGIN, // 0x25
    MAP_ITEM_DATA, // 0x26
    MERCHANT_OFFERS, // 0x27
    MOVE_ENTITY_POS, // 0x28
    MOVE_ENTITY_POS_ROT, // 0x29
    MOVE_ENTITY_ROT, // 0x2A
    MOVE_ENTITY, // 0x2B
    MOVE_VEHICLE, // 0x2C
    OPEN_BOOK, // 0x2D
    OPEN_SCREEN, // 0x2E
    OPEN_SIGN_EDITOR, // 0x2F
    PLACE_GHOST_RECIPE, // 0x30
    PLAYER_ABILITIES, // 0x31
    PLAYER_COMBAT, // 0x32
    PLAYER_INFO, // 0x33
    PLAYER_LOOK_AT, // 0x34
    PLAYER_POSITION, // 0x35
    RECIPE, // 0x36
    REMOVE_ENTITIES, // 0x37
    REMOVE_MOB_EFFECT, // 0x38
    RESOURCE_PACK, // 0x39
    RESPAWN, // 0x3A
    ROTATE_HEAD, // 0x3B
    SELECT_ADVANCEMENTS_TAB, // 0x3C
    SET_BORDER, // 0x3D
    SET_CAMERA, // 0x3E
    SET_CARRIED_ITEM, // 0x3F
    SET_CHUNK_CACHE_CENTER, // 0x40
    SET_CHUNK_CACHE_RADIUS, // 0x41
    SET_DEFAULT_SPAWN_POSITION, // 0x42
    SET_DISPLAY_OBJECTIVE, // 0x43
    SET_ENTITY_DATA, // 0x44
    SET_ENTITY_LINK, // 0x45
    SET_ENTITY_MOTION, // 0x46
    SET_EQUIPMENT, // 0x47
    SET_EXPERIENCE, // 0x48
    SET_HEALTH, // 0x49
    SET_OBJECTIVE, // 0x4A
    SET_PASSENGERS, // 0x4B
    SET_PLAYER_TEAM, // 0x4C
    SET_SCORE, // 0x4D
    SET_TIME, // 0x4E
    SET_TITLES, // 0x4F
    SOUND_ENTITY, // 0x50
    SOUND, // 0x51
    STOP_SOUND, // 0x52
    TAB_LIST, // 0x53
    TAG_QUERY, // 0x54
    TAKE_ITEM_ENTITY, // 0x55
    TELEPORT_ENTITY, // 0x56
    UPDATE_ADVANCEMENTS, // 0x57
    UPDATE_ATTRIBUTES, // 0x58
    UPDATE_MOB_EFFECT, // 0x59
    UPDATE_RECIPES, // 0x5A
    UPDATE_TAGS; // 0x5B

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
