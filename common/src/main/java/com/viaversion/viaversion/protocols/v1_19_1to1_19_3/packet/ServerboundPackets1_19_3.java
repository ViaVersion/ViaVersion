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
package com.viaversion.viaversion.protocols.v1_19_1to1_19_3.packet;

import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;

public enum ServerboundPackets1_19_3 implements ServerboundPacketType {

    ACCEPT_TELEPORTATION, // 0x00
    BLOCK_ENTITY_TAG_QUERY, // 0x01
    CHANGE_DIFFICULTY, // 0x02
    CHAT_ACK, // 0x03
    CHAT_COMMAND, // 0x04
    CHAT, // 0x05
    CLIENT_COMMAND, // 0x06
    CLIENT_INFORMATION, // 0x07
    COMMAND_SUGGESTION, // 0x08
    CONTAINER_BUTTON_CLICK, // 0x09
    CONTAINER_CLICK, // 0x0A
    CONTAINER_CLOSE, // 0x0B
    CUSTOM_PAYLOAD, // 0x0C
    EDIT_BOOK, // 0x0D
    ENTITY_TAG_QUERY, // 0x0E
    INTERACT, // 0x0F
    JIGSAW_GENERATE, // 0x10
    KEEP_ALIVE, // 0x11
    LOCK_DIFFICULTY, // 0x12
    MOVE_PLAYER_POS, // 0x13
    MOVE_PLAYER_POS_ROT, // 0x14
    MOVE_PLAYER_ROT, // 0x15
    MOVE_PLAYER_STATUS_ONLY, // 0x16
    MOVE_VEHICLE, // 0x17
    PADDLE_BOAT, // 0x18
    PICK_ITEM, // 0x19
    PLACE_RECIPE, // 0x1A
    PLAYER_ABILITIES, // 0x1B
    PLAYER_ACTION, // 0x1C
    PLAYER_COMMAND, // 0x1D
    PLAYER_INPUT, // 0x1E
    PONG, // 0x1F
    CHAT_SESSION_UPDATE, // 0x20
    RECIPE_BOOK_CHANGE_SETTINGS, // 0x21
    RECIPE_BOOK_SEEN_RECIPE, // 0x22
    RENAME_ITEM, // 0x23
    RESOURCE_PACK, // 0x24
    SEEN_ADVANCEMENTS, // 0x25
    SELECT_TRADE, // 0x26
    SET_BEACON, // 0x27
    SET_CARRIED_ITEM, // 0x28
    SET_COMMAND_BLOCK, // 0x29
    SET_COMMAND_MINECART, // 0x2A
    SET_CREATIVE_MODE_SLOT, // 0x2B
    SET_JIGSAW_BLOCK, // 0x2C
    SET_STRUCTURE_BLOCK, // 0x2D
    SIGN_UPDATE, // 0x2E
    SWING, // 0x2F
    TELEPORT_TO_ENTITY, // 0x30
    USE_ITEM_ON, // 0x31
    USE_ITEM; // 0x32

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
