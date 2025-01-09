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
package com.viaversion.viaversion.protocols.v1_13_2to1_14.packet;

import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;

public enum ServerboundPackets1_14 implements ServerboundPacketType {

    ACCEPT_TELEPORTATION, // 0x00
    BLOCK_ENTITY_TAG_QUERY, // 0x01
    CHANGE_DIFFICULTY, // 0x02
    CHAT, // 0x03
    CLIENT_COMMAND, // 0x04
    CLIENT_INFORMATION, // 0x05
    COMMAND_SUGGESTION, // 0x06
    CONTAINER_ACK, // 0x07
    CONTAINER_BUTTON_CLICK, // 0x08
    CONTAINER_CLICK, // 0x09
    CONTAINER_CLOSE, // 0x0A
    CUSTOM_PAYLOAD, // 0x0B
    EDIT_BOOK, // 0x0C
    ENTITY_TAG_QUERY, // 0x0D
    INTERACT, // 0x0E
    KEEP_ALIVE, // 0x0F
    LOCK_DIFFICULTY, // 0x10
    MOVE_PLAYER_POS, // 0x11
    MOVE_PLAYER_POS_ROT, // 0x12
    MOVE_PLAYER_ROT, // 0x13
    MOVE_PLAYER_STATUS_ONLY, // 0x14
    MOVE_VEHICLE, // 0x15
    PADDLE_BOAT, // 0x16
    PICK_ITEM, // 0x17
    PLACE_RECIPE, // 0x18
    PLAYER_ABILITIES, // 0x19
    PLAYER_ACTION, // 0x1A
    PLAYER_COMMAND, // 0x1B
    PLAYER_INPUT, // 0x1C
    RECIPE_BOOK_UPDATE, // 0x1D
    RENAME_ITEM, // 0x1E
    RESOURCE_PACK, // 0x1F
    SEEN_ADVANCEMENTS, // 0x20
    SELECT_TRADE, // 0x21
    SET_BEACON, // 0x22
    SET_CARRIED_ITEM, // 0x23
    SET_COMMAND_BLOCK, // 0x24
    SET_COMMAND_MINECART, // 0x25
    SET_CREATIVE_MODE_SLOT, // 0x26
    SET_JIGSAW_BLOCK, // 0x27
    SET_STRUCTURE_BLOCK, // 0x28
    SIGN_UPDATE, // 0x29
    SWING, // 0x2A
    TELEPORT_TO_ENTITY, // 0x2B
    USE_ITEM_ON, // 0x2C
    USE_ITEM; // 0x2D

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
