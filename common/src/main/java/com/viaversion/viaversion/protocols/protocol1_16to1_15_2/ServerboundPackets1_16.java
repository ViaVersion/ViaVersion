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
package com.viaversion.viaversion.protocols.protocol1_16to1_15_2;

import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;

public enum ServerboundPackets1_16 implements ServerboundPacketType {

    TELEPORT_CONFIRM, // 0x00
    QUERY_BLOCK_NBT, // 0x01
    SET_DIFFICULTY, // 0x02
    CHAT_MESSAGE, // 0x03
    CLIENT_STATUS, // 0x04
    CLIENT_SETTINGS, // 0x05
    TAB_COMPLETE, // 0x06
    WINDOW_CONFIRMATION, // 0x07
    CLICK_WINDOW_BUTTON, // 0x08
    CLICK_WINDOW, // 0x09
    CLOSE_WINDOW, // 0x0A
    PLUGIN_MESSAGE, // 0x0B
    EDIT_BOOK, // 0x0C
    ENTITY_NBT_REQUEST, // 0x0D
    INTERACT_ENTITY, // 0x0E
    GENERATE_JIGSAW, // 0x0F
    KEEP_ALIVE, // 0x10
    LOCK_DIFFICULTY, // 0x11
    PLAYER_POSITION, // 0x12
    PLAYER_POSITION_AND_ROTATION, // 0x13
    PLAYER_ROTATION, // 0x14
    PLAYER_MOVEMENT, // 0x15
    VEHICLE_MOVE, // 0x16
    STEER_BOAT, // 0x17
    PICK_ITEM, // 0x18
    CRAFT_RECIPE_REQUEST, // 0x19
    PLAYER_ABILITIES, // 0x1A
    PLAYER_DIGGING, // 0x1B
    ENTITY_ACTION, // 0x1C
    STEER_VEHICLE, // 0x1D
    RECIPE_BOOK_DATA, // 0x1E
    RENAME_ITEM, // 0x1F
    RESOURCE_PACK_STATUS, // 0x20
    ADVANCEMENT_TAB, // 0x21
    SELECT_TRADE, // 0x22
    SET_BEACON_EFFECT, // 0x23
    HELD_ITEM_CHANGE, // 0x24
    UPDATE_COMMAND_BLOCK, // 0x25
    UPDATE_COMMAND_BLOCK_MINECART, // 0x26
    CREATIVE_INVENTORY_ACTION, // 0x27
    UPDATE_JIGSAW_BLOCK, // 0x28
    UPDATE_STRUCTURE_BLOCK, // 0x29
    UPDATE_SIGN, // 0x2A
    ANIMATION, // 0x2B
    SPECTATE, // 0x2C
    PLAYER_BLOCK_PLACEMENT, // 0x2D
    USE_ITEM; // 0x2E

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
