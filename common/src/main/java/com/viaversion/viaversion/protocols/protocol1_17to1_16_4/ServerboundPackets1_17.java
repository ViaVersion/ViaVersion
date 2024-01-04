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
package com.viaversion.viaversion.protocols.protocol1_17to1_16_4;

import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;

public enum ServerboundPackets1_17 implements ServerboundPacketType {

    TELEPORT_CONFIRM, // 0x00
    QUERY_BLOCK_NBT, // 0x01
    SET_DIFFICULTY, // 0x02
    CHAT_MESSAGE, // 0x03
    CLIENT_STATUS, // 0x04
    CLIENT_SETTINGS, // 0x05
    TAB_COMPLETE, // 0x06
    CLICK_WINDOW_BUTTON, // 0x07
    CLICK_WINDOW, // 0x08
    CLOSE_WINDOW, // 0x09
    PLUGIN_MESSAGE, // 0x0A
    EDIT_BOOK, // 0x0B
    ENTITY_NBT_REQUEST, // 0x0C
    INTERACT_ENTITY, // 0x0D
    GENERATE_JIGSAW, // 0x0E
    KEEP_ALIVE, // 0x0F
    LOCK_DIFFICULTY, // 0x10
    PLAYER_POSITION, // 0x11
    PLAYER_POSITION_AND_ROTATION, // 0x12
    PLAYER_ROTATION, // 0x13
    PLAYER_MOVEMENT, // 0x14
    VEHICLE_MOVE, // 0x15
    STEER_BOAT, // 0x16
    PICK_ITEM, // 0x17
    CRAFT_RECIPE_REQUEST, // 0x18
    PLAYER_ABILITIES, // 0x19
    PLAYER_DIGGING, // 0x1A
    ENTITY_ACTION, // 0x1B
    STEER_VEHICLE, // 0x1C
    PONG, // 0x1D
    RECIPE_BOOK_DATA, // 0x1E
    SEEN_RECIPE, // 0x1F
    RENAME_ITEM, // 0x20
    RESOURCE_PACK_STATUS, // 0x21
    ADVANCEMENT_TAB, // 0x22
    SELECT_TRADE, // 0x23
    SET_BEACON_EFFECT, // 0x24
    HELD_ITEM_CHANGE, // 0x25
    UPDATE_COMMAND_BLOCK, // 0x26
    UPDATE_COMMAND_BLOCK_MINECART, // 0x27
    CREATIVE_INVENTORY_ACTION, // 0x28
    UPDATE_JIGSAW_BLOCK, // 0x29
    UPDATE_STRUCTURE_BLOCK, // 0x2A
    UPDATE_SIGN, // 0x2B
    ANIMATION, // 0x2C
    SPECTATE, // 0x2D
    PLAYER_BLOCK_PLACEMENT, // 0x2E
    USE_ITEM; // 0x2F

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
