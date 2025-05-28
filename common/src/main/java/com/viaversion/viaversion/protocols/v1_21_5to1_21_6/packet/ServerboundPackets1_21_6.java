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
package com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet;

public enum ServerboundPackets1_21_6 implements ServerboundPacket1_21_6 {

    ACCEPT_TELEPORTATION, // 0x00
    BLOCK_ENTITY_TAG_QUERY, // 0x01
    BUNDLE_ITEM_SELECTED, // 0x02
    CHANGE_DIFFICULTY, // 0x03
    CHANGE_GAME_MODE, // 0x03
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
    PICK_ITEM_FROM_ENTITY, // 0x23
    PING_REQUEST, // 0x24
    PLACE_RECIPE, // 0x25
    PLAYER_ABILITIES, // 0x26
    PLAYER_ACTION, // 0x27
    PLAYER_COMMAND, // 0x28
    PLAYER_INPUT, // 0x29
    PLAYER_LOADED, // 0x2A
    PONG, // 0x2B
    RECIPE_BOOK_CHANGE_SETTINGS, // 0x2C
    RECIPE_BOOK_SEEN_RECIPE, // 0x2D
    RENAME_ITEM, // 0x2E
    RESOURCE_PACK, // 0x2F
    SEEN_ADVANCEMENTS, // 0x30
    SELECT_TRADE, // 0x31
    SET_BEACON, // 0x32
    SET_CARRIED_ITEM, // 0x33
    SET_COMMAND_BLOCK, // 0x34
    SET_COMMAND_MINECART, // 0x35
    SET_CREATIVE_MODE_SLOT, // 0x36
    SET_JIGSAW_BLOCK, // 0x37
    SET_STRUCTURE_BLOCK, // 0x38
    SET_TEST_BLOCK, // 0x39
    SIGN_UPDATE, // 0x3A
    SWING, // 0x3B
    TELEPORT_TO_ENTITY, // 0x3C
    TEST_INSTANCE_BLOCK_ACTION, // 0x3D
    USE_ITEM_ON, // 0x3E
    USE_ITEM, // 0x3F
    CUSTOM_CLICK_ACTION; // 0x40

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
