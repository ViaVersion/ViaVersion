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
package com.viaversion.viaversion.protocols.v1_8to1_9.packet;

import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;

public enum ServerboundPackets1_8 implements ServerboundPacketType {

    KEEP_ALIVE, // 0x00
    CHAT, // 0x01
    INTERACT, // 0x02
    MOVE_PLAYER_STATUS_ONLY, // 0x03
    MOVE_PLAYER_POS, // 0x04
    MOVE_PLAYER_ROT, // 0x05
    MOVE_PLAYER_POS_ROT, // 0x06
    PLAYER_ACTION, // 0x07
    USE_ITEM_ON, // 0x08
    SET_CARRIED_ITEM, // 0x09
    SWING, // 0x0A
    PLAYER_COMMAND, // 0x0B
    PLAYER_INPUT, // 0x0C
    CONTAINER_CLOSE, // 0x0D
    CONTAINER_CLICK, // 0x0E
    CONTAINER_ACK, // 0x0F
    SET_CREATIVE_MODE_SLOT, // 0x10
    CONTAINER_BUTTON_CLICK, // 0x11
    SIGN_UPDATE, // 0x12
    PLAYER_ABILITIES, // 0x13
    COMMAND_SUGGESTION, // 0x14
    CLIENT_INFORMATION, // 0x15
    CLIENT_COMMAND, // 0x16
    CUSTOM_PAYLOAD, // 0x17
    TELEPORT_TO_ENTITY, // 0x18
    RESOURCE_PACK; // 0x19

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
