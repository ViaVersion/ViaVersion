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
package com.viaversion.viaversion.protocols.v1_21_11to26_1.packet;

public enum ServerboundPackets26_1 implements ServerboundPacket26_1 {

    ACCEPT_TELEPORTATION, // 0x00
    ATTACK, // 0x01
    BLOCK_ENTITY_TAG_QUERY, // 0x02
    BUNDLE_ITEM_SELECTED, // 0x03
    CHANGE_DIFFICULTY, // 0x04
    CHANGE_GAME_MODE, // 0x05
    CHAT_ACK, // 0x06
    CHAT_COMMAND, // 0x07
    CHAT_COMMAND_SIGNED, // 0x08
    CHAT, // 0x09
    CHAT_SESSION_UPDATE, // 0x0A
    CHUNK_BATCH_RECEIVED, // 0x0B
    CLIENT_COMMAND, // 0x0C
    CLIENT_TICK_END, // 0x0D
    CLIENT_INFORMATION, // 0x0E
    COMMAND_SUGGESTION, // 0x0F
    CONFIGURATION_ACKNOWLEDGED, // 0x10
    CONTAINER_BUTTON_CLICK, // 0x11
    CONTAINER_CLICK, // 0x12
    CONTAINER_CLOSE, // 0x13
    CONTAINER_SLOT_STATE_CHANGED, // 0x14
    COOKIE_RESPONSE, // 0x15
    CUSTOM_PAYLOAD, // 0x16
    DEBUG_SAMPLE_SUBSCRIPTION, // 0x17
    EDIT_BOOK, // 0x18
    ENTITY_TAG_QUERY, // 0x19
    INTERACT, // 0x1A
    JIGSAW_GENERATE, // 0x1B
    KEEP_ALIVE, // 0x1C
    LOCK_DIFFICULTY, // 0x1D
    MOVE_PLAYER_POS, // 0x1E
    MOVE_PLAYER_POS_ROT, // 0x1F
    MOVE_PLAYER_ROT, // 0x20
    MOVE_PLAYER_STATUS_ONLY, // 0x21
    MOVE_VEHICLE, // 0x22
    PADDLE_BOAT, // 0x23
    PICK_ITEM_FROM_BLOCK, // 0x24
    PICK_ITEM_FROM_ENTITY, // 0x25
    PING_REQUEST, // 0x26
    PLACE_RECIPE, // 0x27
    PLAYER_ABILITIES, // 0x28
    PLAYER_ACTION, // 0x29
    PLAYER_COMMAND, // 0x2A
    PLAYER_INPUT, // 0x2B
    PLAYER_LOADED, // 0x2C
    PONG, // 0x2D
    RECIPE_BOOK_CHANGE_SETTINGS, // 0x2E
    RECIPE_BOOK_SEEN_RECIPE, // 0x2F
    RENAME_ITEM, // 0x30
    RESOURCE_PACK, // 0x31
    SEEN_ADVANCEMENTS, // 0x32
    SELECT_TRADE, // 0x33
    SET_BEACON, // 0x34
    SET_CARRIED_ITEM, // 0x35
    SET_COMMAND_BLOCK, // 0x36
    SET_COMMAND_MINECART, // 0x37
    SET_CREATIVE_MODE_SLOT, // 0x38
    SET_GAME_RULE, // 0x39
    SET_JIGSAW_BLOCK, // 0x3A
    SET_STRUCTURE_BLOCK, // 0x3B
    SET_TEST_BLOCK, // 0x3C
    SIGN_UPDATE, // 0x3D
    SPECTATE_ENTITY, // 0x3E
    SWING, // 0x3F
    TELEPORT_TO_ENTITY, // 0x40
    TEST_INSTANCE_BLOCK_ACTION, // 0x41
    USE_ITEM_ON, // 0x42
    USE_ITEM, // 0x43
    CUSTOM_CLICK_ACTION; // 0x44

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
