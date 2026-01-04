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
package com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet;

public enum ServerboundPackets1_20_5 implements ServerboundPacket1_20_5 {

    ACCEPT_TELEPORTATION, // 0x00
    BLOCK_ENTITY_TAG_QUERY, // 0x01
    CHANGE_DIFFICULTY, // 0x02
    CHAT_ACK, // 0x03
    CHAT_COMMAND, // 0x04
    CHAT_COMMAND_SIGNED, // 0x05
    CHAT, // 0x06
    CHAT_SESSION_UPDATE, // 0x07
    CHUNK_BATCH_RECEIVED, // 0x08
    CLIENT_COMMAND, // 0x09
    CLIENT_INFORMATION, // 0x0A
    COMMAND_SUGGESTION, // 0x0B
    CONFIGURATION_ACKNOWLEDGED, // 0x0C
    CONTAINER_BUTTON_CLICK, // 0x0D
    CONTAINER_CLICK, // 0x0E
    CONTAINER_CLOSE, // 0x0F
    CONTAINER_SLOT_STATE_CHANGED, // 0x10
    COOKIE_RESPONSE, // 0x11
    CUSTOM_PAYLOAD, // 0x12
    DEBUG_SAMPLE_SUBSCRIPTION, // 0x13
    EDIT_BOOK, // 0x14
    ENTITY_TAG_QUERY, // 0x15
    INTERACT, // 0x16
    JIGSAW_GENERATE, // 0x17
    KEEP_ALIVE, // 0x18
    LOCK_DIFFICULTY, // 0x19
    MOVE_PLAYER_POS, // 0x1A
    MOVE_PLAYER_POS_ROT, // 0x1B
    MOVE_PLAYER_ROT, // 0x1C
    MOVE_PLAYER_STATUS_ONLY, // 0x1D
    MOVE_VEHICLE, // 0x1E
    PADDLE_BOAT, // 0x1F
    PICK_ITEM, // 0x20
    PING_REQUEST, // 0x21
    PLACE_RECIPE, // 0x22
    PLAYER_ABILITIES, // 0x23
    PLAYER_ACTION, // 0x24
    PLAYER_COMMAND, // 0x25
    PLAYER_INPUT, // 0x26
    PONG, // 0x27
    RECIPE_BOOK_CHANGE_SETTINGS, // 0x28
    RECIPE_BOOK_SEEN_RECIPE, // 0x29
    RENAME_ITEM, // 0x2A
    RESOURCE_PACK, // 0x2B
    SEEN_ADVANCEMENTS, // 0x2C
    SELECT_TRADE, // 0x2D
    SET_BEACON, // 0x2E
    SET_CARRIED_ITEM, // 0x2F
    SET_COMMAND_BLOCK, // 0x30
    SET_COMMAND_MINECART, // 0x31
    SET_CREATIVE_MODE_SLOT, // 0x32
    SET_JIGSAW_BLOCK, // 0x33
    SET_STRUCTURE_BLOCK, // 0x34
    SIGN_UPDATE, // 0x35
    SWING, // 0x36
    TELEPORT_TO_ENTITY, // 0x37
    USE_ITEM_ON, // 0x38
    USE_ITEM; // 0x39

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
