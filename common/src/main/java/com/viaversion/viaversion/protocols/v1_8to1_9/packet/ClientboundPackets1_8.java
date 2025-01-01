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

public enum ClientboundPackets1_8 implements ClientboundPacketType {

    KEEP_ALIVE, // 0x00
    LOGIN, // 0x01
    CHAT, // 0x02
    SET_TIME, // 0x03
    SET_EQUIPPED_ITEM, // 0x04
    SET_DEFAULT_SPAWN_POSITION, // 0x05
    SET_HEALTH, // 0x06
    RESPAWN, // 0x07
    PLAYER_POSITION, // 0x08
    SET_CARRIED_ITEM, // 0x09
    PLAYER_SLEEP, // 0x0A
    ANIMATE, // 0x0B
    ADD_PLAYER, // 0x0C
    TAKE_ITEM_ENTITY, // 0x0D
    ADD_ENTITY, // 0x0E
    ADD_MOB, // 0x0F
    ADD_PAINTING, // 0x10
    ADD_EXPERIENCE_ORB, // 0x11
    SET_ENTITY_MOTION, // 0x12
    REMOVE_ENTITIES, // 0x13
    MOVE_ENTITY, // 0x14
    MOVE_ENTITY_POS, // 0x15
    MOVE_ENTITY_ROT, // 0x16
    MOVE_ENTITY_POS_ROT, // 0x17
    TELEPORT_ENTITY, // 0x18
    ROTATE_HEAD, // 0x19
    ENTITY_EVENT, // 0x1A
    SET_ENTITY_LINK, // 0x1B
    SET_ENTITY_DATA, // 0x1C
    UPDATE_MOB_EFFECT, // 0x1D
    REMOVE_MOB_EFFECT, // 0x1E
    SET_EXPERIENCE, // 0x1F
    UPDATE_ATTRIBUTES, // 0x20
    LEVEL_CHUNK, // 0x21
    CHUNK_BLOCKS_UPDATE, // 0x22
    BLOCK_UPDATE, // 0x23
    BLOCK_EVENT, // 0x24
    BLOCK_DESTRUCTION, // 0x25
    MAP_BULK_CHUNK, // 0x26
    EXPLODE, // 0x27
    LEVEL_EVENT, // 0x28
    CUSTOM_SOUND, // 0x29
    LEVEL_PARTICLES, // 0x2A
    GAME_EVENT, // 0x2B
    ADD_GLOBAL_ENTITY, // 0x2C
    OPEN_SCREEN, // 0x2D
    CONTAINER_CLOSE, // 0x2E
    CONTAINER_SET_SLOT, // 0x2F
    CONTAINER_SET_CONTENT, // 0x30
    CONTAINER_SET_DATA, // 0x31
    CONTAINER_ACK, // 0x32
    UPDATE_SIGN, // 0x33
    MAP_ITEM_DATA, // 0x34
    BLOCK_ENTITY_DATA, // 0x35
    OPEN_SIGN_EDITOR, // 0x36
    AWARD_STATS, // 0x37
    PLAYER_INFO, // 0x38
    PLAYER_ABILITIES, // 0x39
    COMMAND_SUGGESTIONS, // 0x3A
    SET_OBJECTIVE, // 0x3B
    SET_SCORE, // 0x3C
    SET_DISPLAY_OBJECTIVE, // 0x3D
    SET_PLAYER_TEAM, // 0x3E
    CUSTOM_PAYLOAD, // 0x3F
    DISCONNECT, // 0x40
    CHANGE_DIFFICULTY, // 0x41
    PLAYER_COMBAT, // 0x42
    SET_CAMERA, // 0x43
    SET_BORDER, // 0x44
    SET_TITLES, // 0x45
    SET_COMPRESSION, // 0x46
    TAB_LIST, // 0x47
    RESOURCE_PACK, // 0x48
    UPDATE_ENTITY_NBT; // 0x49

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
