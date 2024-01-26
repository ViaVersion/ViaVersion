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
package com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet;

public enum ServerboundPackets1_20_3 implements ServerboundPacket1_20_3 {

    TELEPORT_CONFIRM, // 0x00
    QUERY_BLOCK_NBT, // 0x01
    SET_DIFFICULTY, // 0x02
    CHAT_ACK, // 0x03
    CHAT_COMMAND, // 0x04
    CHAT_MESSAGE, // 0x05
    CHAT_SESSION_UPDATE, // 0x06
    CHUNK_BATCH_RECEIVED, // 0x07
    CLIENT_STATUS, // 0x08
    CLIENT_SETTINGS, // 0x09
    TAB_COMPLETE, // 0x0A
    CONFIGURATION_ACKNOWLEDGED, // 0x0B
    CLICK_WINDOW_BUTTON, // 0x0C
    CLICK_WINDOW, // 0x0D
    CLOSE_WINDOW, // 0x0E
    CONTAINER_SLOT_STATE_CHANGED, // 0x0F
    PLUGIN_MESSAGE, // 0x10
    EDIT_BOOK, // 0x11
    ENTITY_NBT_REQUEST, // 0x12
    INTERACT_ENTITY, // 0x13
    GENERATE_JIGSAW, // 0x14
    KEEP_ALIVE, // 0x15
    LOCK_DIFFICULTY, // 0x16
    PLAYER_POSITION, // 0x17
    PLAYER_POSITION_AND_ROTATION, // 0x18
    PLAYER_ROTATION, // 0x19
    PLAYER_MOVEMENT, // 0x1A
    VEHICLE_MOVE, // 0x1B
    STEER_BOAT, // 0x1C
    PICK_ITEM, // 0x1D
    PING_REQUEST, // 0x1E
    CRAFT_RECIPE_REQUEST, // 0x1F
    PLAYER_ABILITIES, // 0x20
    PLAYER_DIGGING, // 0x21
    ENTITY_ACTION, // 0x22
    STEER_VEHICLE, // 0x23
    PONG, // 0x24
    RECIPE_BOOK_DATA, // 0x25
    SEEN_RECIPE, // 0x26
    RENAME_ITEM, // 0x27
    RESOURCE_PACK_STATUS, // 0x28
    ADVANCEMENT_TAB, // 0x29
    SELECT_TRADE, // 0x2A
    SET_BEACON_EFFECT, // 0x2B
    HELD_ITEM_CHANGE, // 0x2C
    UPDATE_COMMAND_BLOCK, // 0x2D
    UPDATE_COMMAND_BLOCK_MINECART, // 0x2E
    CREATIVE_INVENTORY_ACTION, // 0x2F
    UPDATE_JIGSAW_BLOCK, // 0x30
    UPDATE_STRUCTURE_BLOCK, // 0x31
    UPDATE_SIGN, // 0x32
    ANIMATION, // 0x33
    SPECTATE, // 0x34
    PLAYER_BLOCK_PLACEMENT, // 0x35
    USE_ITEM; // 0x36

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
