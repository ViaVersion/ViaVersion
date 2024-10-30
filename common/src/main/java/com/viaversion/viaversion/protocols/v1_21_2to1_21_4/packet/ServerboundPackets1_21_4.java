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
package com.viaversion.viaversion.protocols.v1_21_2to1_21_4.packet;

public enum ServerboundPackets1_21_4 implements ServerboundPacket1_21_4 {

    ACCEPT_TELEPORTATION, // 0x00
    BLOCK_ENTITY_TAG_QUERY, // 0x01
    BUNDLE_ITEM_SELECTED, // 0x02
    CHANGE_DIFFICULTY, // 0x03
    CHAT_ACK, // 0x04
    CHAT_COMMAND, // 0x05
    CHAT_COMMAND_SIGNED, // 0x06
    CHAT, // 0x07
    CHAT_SESSION_UPDATE, // 0x08
    CHUNK_BATCH_RECEIVED, // 0x09
    CLIENT_COMMAND, // 0x0A
    CLIENT_TICK_END, // 0x0B
    CLIENT_INFORMATION, // 0x0C
    COMMAND_SUGGESTION, // 0x0D
    CONFIGURATION_ACKNOWLEDGED, // 0x0E
    CONTAINER_BUTTON_CLICK, // 0x0F
    CONTAINER_CLICK, // 0x10
    CONTAINER_CLOSE, // 0x11
    CONTAINER_SLOT_STATE_CHANGED, // 0x12
    COOKIE_RESPONSE, // 0x13
    CUSTOM_PAYLOAD, // 0x14
    DEBUG_SAMPLE_SUBSCRIPTION, // 0x15
    EDIT_BOOK, // 0x16
    ENTITY_TAG_QUERY, // 0x17
    INTERACT, // 0x18
    JIGSAW_GENERATE, // 0x19
    KEEP_ALIVE, // 0x1A
    LOCK_DIFFICULTY, // 0x1B
    MOVE_PLAYER_POS, // 0x1C
    MOVE_PLAYER_POS_ROT, // 0x1D
    MOVE_PLAYER_ROT, // 0x1E
    MOVE_PLAYER_STATUS_ONLY, // 0x1F
    MOVE_VEHICLE, // 0x20
    PADDLE_BOAT, // 0x21
    PICK_ITEM_FROM_BLOCK, // 0x22
    PICK_ITEM_FROM_ENTITY, // 0x22
    PING_REQUEST, // 0x23
    PLACE_RECIPE, // 0x24
    PLAYER_ABILITIES, // 0x25
    PLAYER_ACTION, // 0x26
    PLAYER_COMMAND, // 0x27
    PLAYER_INPUT, // 0x28
    PONG, // 0x29
    RECIPE_BOOK_CHANGE_SETTINGS, // 0x2A
    RECIPE_BOOK_SEEN_RECIPE, // 0x2B
    RENAME_ITEM, // 0x2C
    RESOURCE_PACK, // 0x2D
    SEEN_ADVANCEMENTS, // 0x2E
    SELECT_TRADE, // 0x2F
    SET_BEACON, // 0x30
    SET_CARRIED_ITEM, // 0x31
    SET_COMMAND_BLOCK, // 0x32
    SET_COMMAND_MINECART, // 0x33
    SET_CREATIVE_MODE_SLOT, // 0x34
    SET_JIGSAW_BLOCK, // 0x35
    SET_STRUCTURE_BLOCK, // 0x36
    SIGN_UPDATE, // 0x37
    SWING, // 0x38
    TELEPORT_TO_ENTITY, // 0x39
    USE_ITEM_ON, // 0x3A
    USE_ITEM; // 0x3B

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
