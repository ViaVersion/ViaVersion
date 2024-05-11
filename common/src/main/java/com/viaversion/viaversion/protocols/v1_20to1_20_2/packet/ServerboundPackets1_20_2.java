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
package com.viaversion.viaversion.protocols.v1_20to1_20_2.packet;

public enum ServerboundPackets1_20_2 implements ServerboundPacket1_20_2 {

    ACCEPT_TELEPORTATION, // 0x00
    BLOCK_ENTITY_TAG_QUERY, // 0x01
    CHANGE_DIFFICULTY, // 0x02
    CHAT_ACK, // 0x03
    CHAT_COMMAND, // 0x04
    CHAT, // 0x05
    CHAT_SESSION_UPDATE, // 0x06
    CHUNK_BATCH_RECEIVED, // 0x07
    CLIENT_COMMAND, // 0x08
    CLIENT_INFORMATION, // 0x09
    COMMAND_SUGGESTION, // 0x0A
    CONFIGURATION_ACKNOWLEDGED, // 0x0B
    CONTAINER_BUTTON_CLICK, // 0x0C
    CONTAINER_CLICK, // 0x0D
    CONTAINER_CLOSE, // 0x0E
    CUSTOM_PAYLOAD, // 0x0F
    EDIT_BOOK, // 0x10
    ENTITY_TAG_QUERY, // 0x11
    INTERACT, // 0x12
    JIGSAW_GENERATE, // 0x13
    KEEP_ALIVE, // 0x14
    LOCK_DIFFICULTY, // 0x15
    MOVE_PLAYER_POS, // 0x16
    MOVE_PLAYER_POS_ROT, // 0x17
    MOVE_PLAYER_ROT, // 0x18
    MOVE_PLAYER_STATUS_ONLY, // 0x19
    MOVE_VEHICLE, // 0x1A
    PADDLE_BOAT, // 0x1B
    PICK_ITEM, // 0x1C
    PING_REQUEST, // 0x1D
    PLACE_RECIPE, // 0x1E
    PLAYER_ABILITIES, // 0x1F
    PLAYER_ACTION, // 0x20
    PLAYER_COMMAND, // 0x21
    PLAYER_INPUT, // 0x22
    PONG, // 0x23
    RECIPE_BOOK_CHANGE_SETTINGS, // 0x24
    RECIPE_BOOK_SEEN_RECIPE, // 0x25
    RENAME_ITEM, // 0x26
    RESOURCE_PACK, // 0x27
    SEEN_ADVANCEMENTS, // 0x28
    SELECT_TRADE, // 0x29
    SET_BEACON, // 0x2A
    SET_CARRIED_ITEM, // 0x2B
    SET_COMMAND_BLOCK, // 0x2C
    SET_COMMAND_MINECART, // 0x2D
    SET_CREATIVE_MODE_SLOT, // 0x2E
    SET_JIGSAW_BLOCK, // 0x2F
    SET_STRUCTURE_BLOCK, // 0x30
    SIGN_UPDATE, // 0x31
    SWING, // 0x32
    TELEPORT_TO_ENTITY, // 0x33
    USE_ITEM_ON, // 0x34
    USE_ITEM; // 0x35

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
