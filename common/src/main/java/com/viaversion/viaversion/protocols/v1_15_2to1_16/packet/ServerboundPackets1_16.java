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

import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;

public enum ServerboundPackets1_16 implements ServerboundPacketType {

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
    JIGSAW_GENERATE, // 0x0F
    KEEP_ALIVE, // 0x10
    LOCK_DIFFICULTY, // 0x11
    MOVE_PLAYER_POS, // 0x12
    MOVE_PLAYER_POS_ROT, // 0x13
    MOVE_PLAYER_ROT, // 0x14
    MOVE_PLAYER_STATUS_ONLY, // 0x15
    MOVE_VEHICLE, // 0x16
    PADDLE_BOAT, // 0x17
    PICK_ITEM, // 0x18
    PLACE_RECIPE, // 0x19
    PLAYER_ABILITIES, // 0x1A
    PLAYER_ACTION, // 0x1B
    PLAYER_COMMAND, // 0x1C
    PLAYER_INPUT, // 0x1D
    RECIPE_BOOK_UPDATE, // 0x1E
    RENAME_ITEM, // 0x1F
    RESOURCE_PACK, // 0x20
    SEEN_ADVANCEMENTS, // 0x21
    SELECT_TRADE, // 0x22
    SET_BEACON, // 0x23
    SET_CARRIED_ITEM, // 0x24
    SET_COMMAND_BLOCK, // 0x25
    SET_COMMAND_MINECART, // 0x26
    SET_CREATIVE_MODE_SLOT, // 0x27
    SET_JIGSAW_BLOCK, // 0x28
    SET_STRUCTURE_BLOCK, // 0x29
    SIGN_UPDATE, // 0x2A
    SWING, // 0x2B
    TELEPORT_TO_ENTITY, // 0x2C
    USE_ITEM_ON, // 0x2D
    USE_ITEM; // 0x2E

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
