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
package com.viaversion.viaversion.protocols.v1_12_2to1_13.packet;

import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;

public enum ServerboundPackets1_13 implements ServerboundPacketType {

    ACCEPT_TELEPORTATION, // 0x00
    BLOCK_ENTITY_TAG_QUERY, // 0x01
    CHAT, // 0x02
    CLIENT_COMMAND, // 0x03
    CLIENT_INFORMATION, // 0x04
    COMMAND_SUGGESTION, // 0x05
    CONTAINER_ACK, // 0x06
    CONTAINER_BUTTON_CLICK, // 0x07
    CONTAINER_CLICK, // 0x08
    CONTAINER_CLOSE, // 0x09
    CUSTOM_PAYLOAD, // 0x0A
    EDIT_BOOK, // 0x0B
    ENTITY_TAG_QUERY, // 0x0C
    INTERACT, // 0x0D
    KEEP_ALIVE, // 0x0E
    MOVE_PLAYER_STATUS_ONLY, // 0x0F
    MOVE_PLAYER_POS, // 0x10
    MOVE_PLAYER_POS_ROT, // 0x11
    MOVE_PLAYER_ROT, // 0x12
    MOVE_VEHICLE, // 0x13
    PADDLE_BOAT, // 0x14
    PICK_ITEM, // 0x15
    PLACE_RECIPE, // 0x16
    PLAYER_ABILITIES, // 0x17
    PLAYER_ACTION, // 0x18
    PLAYER_COMMAND, // 0x19
    PLAYER_INPUT, // 0x1A
    RECIPE_BOOK_UPDATE, // 0x1B
    RENAME_ITEM, // 0x1C
    RESOURCE_PACK, // 0x1D
    SEEN_ADVANCEMENTS, // 0x1E
    SELECT_TRADE, // 0x1F
    SET_BEACON, // 0x20
    SET_CARRIED_ITEM, // 0x21
    SET_COMMAND_BLOCK, // 0x22
    SET_COMMAND_MINECART, // 0x23
    SET_CREATIVE_MODE_SLOT, // 0x24
    SET_STRUCTURE_BLOCK, // 0x25
    SIGN_UPDATE, // 0x26
    SWING, // 0x27
    TELEPORT_TO_ENTITY, // 0x28
    USE_ITEM_ON, // 0x29
    USE_ITEM; // 0x2A

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
