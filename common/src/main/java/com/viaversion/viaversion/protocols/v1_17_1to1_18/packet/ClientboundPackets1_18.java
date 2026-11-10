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
package com.viaversion.viaversion.protocols.v1_17_1to1_18.packet;

import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;

public enum ClientboundPackets1_18 implements ClientboundPacketType {

    ADD_ENTITY, // 0x00
    ADD_EXPERIENCE_ORB, // 0x01
    ADD_MOB, // 0x02
    ADD_PAINTING, // 0x03
    ADD_PLAYER, // 0x04
    ADD_VIBRATION_SIGNAL, // 0x05
    ANIMATE, // 0x06
    AWARD_STATS, // 0x07
    BLOCK_BREAK_ACK, // 0x08
    BLOCK_DESTRUCTION, // 0x09
    BLOCK_ENTITY_DATA, // 0x0A
    BLOCK_EVENT, // 0x0B
    BLOCK_UPDATE, // 0x0C
    BOSS_EVENT, // 0x0D
    CHANGE_DIFFICULTY, // 0x0E
    CHAT, // 0x0F
    CLEAR_TITLES, // 0x10
    COMMAND_SUGGESTIONS, // 0x11
    COMMANDS, // 0x12
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
    INITIALIZE_BORDER, // 0x20
    KEEP_ALIVE, // 0x21
    LEVEL_CHUNK_WITH_LIGHT, // 0x22
    LEVEL_EVENT, // 0x23
    LEVEL_PARTICLES, // 0x24
    LIGHT_UPDATE, // 0x25
    LOGIN, // 0x26
    MAP_ITEM_DATA, // 0x27
    MERCHANT_OFFERS, // 0x28
    MOVE_ENTITY_POS, // 0x29
    MOVE_ENTITY_POS_ROT, // 0x2A
    MOVE_ENTITY_ROT, // 0x2B
    MOVE_VEHICLE, // 0x2C
    OPEN_BOOK, // 0x2D
    OPEN_SCREEN, // 0x2E
    OPEN_SIGN_EDITOR, // 0x2F
    PING, // 0x30
    PLACE_GHOST_RECIPE, // 0x31
    PLAYER_ABILITIES, // 0x32
    PLAYER_COMBAT_END, // 0x33
    PLAYER_COMBAT_ENTER, // 0x34
    PLAYER_COMBAT_KILL, // 0x35
    PLAYER_INFO, // 0x36
    PLAYER_LOOK_AT, // 0x37
    PLAYER_POSITION, // 0x38
    RECIPE, // 0x39
    REMOVE_ENTITIES, // 0x3A
    REMOVE_MOB_EFFECT, // 0x3B
    RESOURCE_PACK, // 0x3C
    RESPAWN, // 0x3D
    ROTATE_HEAD, // 0x3E
    SECTION_BLOCKS_UPDATE, // 0x3F
    SELECT_ADVANCEMENTS_TAB, // 0x40
    SET_ACTION_BAR_TEXT, // 0x41
    SET_BORDER_CENTER, // 0x42
    SET_BORDER_LERP_SIZE, // 0x43
    SET_BORDER_SIZE, // 0x44
    SET_BORDER_WARNING_DELAY, // 0x45
    SET_BORDER_WARNING_DISTANCE, // 0x46
    SET_CAMERA, // 0x47
    SET_CARRIED_ITEM, // 0x48
    SET_CHUNK_CACHE_CENTER, // 0x49
    SET_CHUNK_CACHE_RADIUS, // 0x4A
    SET_DEFAULT_SPAWN_POSITION, // 0x4B
    SET_DISPLAY_OBJECTIVE, // 0x4C
    SET_ENTITY_DATA, // 0x4D
    SET_ENTITY_LINK, // 0x4E
    SET_ENTITY_MOTION, // 0x4F
    SET_EQUIPMENT, // 0x50
    SET_EXPERIENCE, // 0x51
    SET_HEALTH, // 0x52
    SET_OBJECTIVE, // 0x53
    SET_PASSENGERS, // 0x54
    SET_PLAYER_TEAM, // 0x55
    SET_SCORE, // 0x56
    SET_SIMULATION_DISTANCE, // 0x57
    SET_SUBTITLE_TEXT, // 0x58
    SET_TIME, // 0x59
    SET_TITLE_TEXT, // 0x5A
    SET_TITLES_ANIMATION, // 0x5B
    SOUND_ENTITY, // 0x5C
    SOUND, // 0x5D
    STOP_SOUND, // 0x5E
    TAB_LIST, // 0x5F
    TAG_QUERY, // 0x60
    TAKE_ITEM_ENTITY, // 0x61
    TELEPORT_ENTITY, // 0x62
    UPDATE_ADVANCEMENTS, // 0x63
    UPDATE_ATTRIBUTES, // 0x64
    UPDATE_MOB_EFFECT, // 0x65
    UPDATE_RECIPES, // 0x66
    UPDATE_TAGS; // 0x67

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
