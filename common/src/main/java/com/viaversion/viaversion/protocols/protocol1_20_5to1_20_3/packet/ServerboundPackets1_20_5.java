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
package com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.packet;

public enum ServerboundPackets1_20_5 implements ServerboundPacket1_20_5 {

    TELEPORT_CONFIRM, // 0x00
    QUERY_BLOCK_NBT, // 0x01
    SET_DIFFICULTY, // 0x02
    CHAT_ACK, // 0x03
    CHAT_COMMAND, // 0x04
    CHAT_COMMAND_SIGNED, // 0x05
    CHAT_MESSAGE, // 0x06
    CHAT_SESSION_UPDATE, // 0x07
    CHUNK_BATCH_RECEIVED, // 0x08
    CLIENT_STATUS, // 0x09
    CLIENT_SETTINGS, // 0x0A
    TAB_COMPLETE, // 0x0B
    CONFIGURATION_ACKNOWLEDGED, // 0x0C
    CLICK_WINDOW_BUTTON, // 0x0D
    CLICK_WINDOW, // 0x0E
    CLOSE_WINDOW, // 0x0F
    CONTAINER_SLOT_STATE_CHANGED, // 0x10
    COOKIE_RESPONSE, // 0x11
    PLUGIN_MESSAGE, // 0x12
    DEBUG_SAMPLE_SUBSCRIPTION, // 0x13
    EDIT_BOOK, // 0x14
    ENTITY_NBT_REQUEST, // 0x15
    INTERACT_ENTITY, // 0x16
    GENERATE_JIGSAW, // 0x17
    KEEP_ALIVE, // 0x18
    LOCK_DIFFICULTY, // 0x19
    PLAYER_POSITION, // 0x1A
    PLAYER_POSITION_AND_ROTATION, // 0x1B
    PLAYER_ROTATION, // 0x1C
    PLAYER_MOVEMENT, // 0x1D
    VEHICLE_MOVE, // 0x1E
    STEER_BOAT, // 0x1F
    PICK_ITEM, // 0x20
    PING_REQUEST, // 0x21
    CRAFT_RECIPE_REQUEST, // 0x22
    PLAYER_ABILITIES, // 0x23
    PLAYER_DIGGING, // 0x24
    ENTITY_ACTION, // 0x25
    STEER_VEHICLE, // 0x26
    PONG, // 0x27
    RECIPE_BOOK_DATA, // 0x28
    SEEN_RECIPE, // 0x29
    RENAME_ITEM, // 0x2A
    RESOURCE_PACK_STATUS, // 0x2B
    ADVANCEMENT_TAB, // 0x2C
    SELECT_TRADE, // 0x2D
    SET_BEACON_EFFECT, // 0x2E
    HELD_ITEM_CHANGE, // 0x2F
    UPDATE_COMMAND_BLOCK, // 0x30
    UPDATE_COMMAND_BLOCK_MINECART, // 0x31
    CREATIVE_INVENTORY_ACTION, // 0x32
    UPDATE_JIGSAW_BLOCK, // 0x33
    UPDATE_STRUCTURE_BLOCK, // 0x34
    UPDATE_SIGN, // 0x35
    ANIMATION, // 0x36
    SPECTATE, // 0x37
    PLAYER_BLOCK_PLACEMENT, // 0x38
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
