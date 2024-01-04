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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2;

import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;

public enum ServerboundPackets1_13 implements ServerboundPacketType {

    TELEPORT_CONFIRM, // 0x00
    QUERY_BLOCK_NBT, // 0x01
    CHAT_MESSAGE, // 0x02
    CLIENT_STATUS, // 0x03
    CLIENT_SETTINGS, // 0x04
    TAB_COMPLETE, // 0x05
    WINDOW_CONFIRMATION, // 0x06
    CLICK_WINDOW_BUTTON, // 0x07
    CLICK_WINDOW, // 0x08
    CLOSE_WINDOW, // 0x09
    PLUGIN_MESSAGE, // 0x0A
    EDIT_BOOK, // 0x0B
    ENTITY_NBT_REQUEST, // 0x0C
    INTERACT_ENTITY, // 0x0D
    KEEP_ALIVE, // 0x0E
    PLAYER_MOVEMENT, // 0x0F
    PLAYER_POSITION, // 0x10
    PLAYER_POSITION_AND_ROTATION, // 0x11
    PLAYER_ROTATION, // 0x12
    VEHICLE_MOVE, // 0x13
    STEER_BOAT, // 0x14
    PICK_ITEM, // 0x15
    CRAFT_RECIPE_REQUEST, // 0x16
    PLAYER_ABILITIES, // 0x17
    PLAYER_DIGGING, // 0x18
    ENTITY_ACTION, // 0x19
    STEER_VEHICLE, // 0x1A
    RECIPE_BOOK_DATA, // 0x1B
    RENAME_ITEM, // 0x1C
    RESOURCE_PACK_STATUS, // 0x1D
    ADVANCEMENT_TAB, // 0x1E
    SELECT_TRADE, // 0x1F
    SET_BEACON_EFFECT, // 0x20
    HELD_ITEM_CHANGE, // 0x21
    UPDATE_COMMAND_BLOCK, // 0x22
    UPDATE_COMMAND_BLOCK_MINECART, // 0x23
    CREATIVE_INVENTORY_ACTION, // 0x24
    UPDATE_STRUCTURE_BLOCK, // 0x25
    UPDATE_SIGN, // 0x26
    ANIMATION, // 0x27
    SPECTATE, // 0x28
    PLAYER_BLOCK_PLACEMENT, // 0x29
    USE_ITEM; // 0x2A

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
