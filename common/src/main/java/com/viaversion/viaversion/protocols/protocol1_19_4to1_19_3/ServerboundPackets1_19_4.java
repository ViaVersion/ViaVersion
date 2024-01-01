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
package com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3;

import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;

public enum ServerboundPackets1_19_4 implements ServerboundPacketType {

    TELEPORT_CONFIRM, // 0x00
    QUERY_BLOCK_NBT, // 0x01
    SET_DIFFICULTY, // 0x02
    CHAT_ACK, // 0x03
    CHAT_COMMAND, // 0x04
    CHAT_MESSAGE, // 0x05
    CHAT_SESSION_UPDATE, // 0x06
    CLIENT_STATUS, // 0x07
    CLIENT_SETTINGS, // 0x08
    TAB_COMPLETE, // 0x09
    CLICK_WINDOW_BUTTON, // 0x0A
    CLICK_WINDOW, // 0x0B
    CLOSE_WINDOW, // 0x0C
    PLUGIN_MESSAGE, // 0x0D
    EDIT_BOOK, // 0x0E
    ENTITY_NBT_REQUEST, // 0x0F
    INTERACT_ENTITY, // 0x10
    GENERATE_JIGSAW, // 0x11
    KEEP_ALIVE, // 0x12
    LOCK_DIFFICULTY, // 0x13
    PLAYER_POSITION, // 0x14
    PLAYER_POSITION_AND_ROTATION, // 0x15
    PLAYER_ROTATION, // 0x16
    PLAYER_MOVEMENT, // 0x17
    VEHICLE_MOVE, // 0x18
    STEER_BOAT, // 0x19
    PICK_ITEM, // 0x1A
    CRAFT_RECIPE_REQUEST, // 0x1B
    PLAYER_ABILITIES, // 0x1C
    PLAYER_DIGGING, // 0x1D
    ENTITY_ACTION, // 0x1E
    STEER_VEHICLE, // 0x1F
    PONG, // 0x20
    RECIPE_BOOK_DATA, // 0x21
    SEEN_RECIPE, // 0x22
    RENAME_ITEM, // 0x23
    RESOURCE_PACK_STATUS, // 0x24
    ADVANCEMENT_TAB, // 0x25
    SELECT_TRADE, // 0x26
    SET_BEACON_EFFECT, // 0x27
    HELD_ITEM_CHANGE, // 0x28
    UPDATE_COMMAND_BLOCK, // 0x29
    UPDATE_COMMAND_BLOCK_MINECART, // 0x2A
    CREATIVE_INVENTORY_ACTION, // 0x2B
    UPDATE_JIGSAW_BLOCK, // 0x2C
    UPDATE_STRUCTURE_BLOCK, // 0x2D
    UPDATE_SIGN, // 0x2E
    ANIMATION, // 0x2F
    SPECTATE, // 0x30
    PLAYER_BLOCK_PLACEMENT, // 0x31
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
