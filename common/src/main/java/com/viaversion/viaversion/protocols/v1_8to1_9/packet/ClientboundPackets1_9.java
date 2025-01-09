/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_8to1_9.packet;

import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;

public enum ClientboundPackets1_9 implements ClientboundPacketType {

    ADD_ENTITY, // 0x00
    ADD_EXPERIENCE_ORB, // 0x01
    ADD_GLOBAL_ENTITY, // 0x02
    ADD_MOB, // 0x03
    ADD_PAINTING, // 0x04
    ADD_PLAYER, // 0x05
    ANIMATE, // 0x06
    AWARD_STATS, // 0x07
    BLOCK_DESTRUCTION, // 0x08
    BLOCK_ENTITY_DATA, // 0x09
    BLOCK_EVENT, // 0x0A
    BLOCK_UPDATE, // 0x0B
    BOSS_EVENT, // 0x0C
    CHANGE_DIFFICULTY, // 0x0D
    COMMAND_SUGGESTIONS, // 0x0E
    CHAT, // 0x0F
    CHUNK_BLOCKS_UPDATE, // 0x10
    CONTAINER_ACK, // 0x11
    CONTAINER_CLOSE, // 0x12
    OPEN_SCREEN, // 0x13
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
    KEEP_ALIVE, // 0x1F
    LEVEL_CHUNK, // 0x20
    LEVEL_EVENT, // 0x21
    LEVEL_PARTICLES, // 0x22
    LOGIN, // 0x23
    MAP_ITEM_DATA, // 0x24
    MOVE_ENTITY_POS, // 0x25
    MOVE_ENTITY_POS_ROT, // 0x26
    MOVE_ENTITY_ROT, // 0x27
    MOVE_ENTITY, // 0x28
    MOVE_VEHICLE, // 0x29
    OPEN_SIGN_EDITOR, // 0x2A
    PLAYER_ABILITIES, // 0x2B
    PLAYER_COMBAT, // 0x2C
    PLAYER_INFO, // 0x2D
    PLAYER_POSITION, // 0x2E
    PLAYER_SLEEP, // 0x2F
    REMOVE_ENTITIES, // 0x30
    REMOVE_MOB_EFFECT, // 0x31
    RESOURCE_PACK, // 0x32
    RESPAWN, // 0x33
    ROTATE_HEAD, // 0x34
    SET_BORDER, // 0x35
    SET_CAMERA, // 0x36
    SET_CARRIED_ITEM, // 0x37
    SET_DISPLAY_OBJECTIVE, // 0x38
    SET_ENTITY_DATA, // 0x39
    SET_ENTITY_LINK, // 0x3A
    SET_ENTITY_MOTION, // 0x3B
    SET_EQUIPPED_ITEM, // 0x3C
    SET_EXPERIENCE, // 0x3D
    SET_HEALTH, // 0x3E
    SET_OBJECTIVE, // 0x3F
    SET_PASSENGERS, // 0x40
    SET_PLAYER_TEAM, // 0x41
    SET_SCORE, // 0x42
    SET_DEFAULT_SPAWN_POSITION, // 0x43
    SET_TIME, // 0x44
    SET_TITLES, // 0x45
    UPDATE_SIGN, // 0x46
    SOUND, // 0x47
    TAB_LIST, // 0x48
    TAKE_ITEM_ENTITY, // 0x49
    TELEPORT_ENTITY, // 0x4A
    UPDATE_ATTRIBUTES, // 0x4B
    UPDATE_MOB_EFFECT; // 0x4C

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
