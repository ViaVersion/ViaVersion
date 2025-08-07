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
package com.viaversion.viaversion.protocols.v1_8to1_9.packet;

import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;

public enum ServerboundPackets1_8 implements ServerboundPacketType {

    KEEP_ALIVE,
    CHAT,
    INTERACT,
    MOVE_PLAYER_STATUS_ONLY,
    MOVE_PLAYER_POS,
    MOVE_PLAYER_ROT,
    MOVE_PLAYER_POS_ROT,
    PLAYER_ACTION,
    USE_ITEM_ON,
    SET_CARRIED_ITEM,
    SWING,
    PLAYER_COMMAND,
    PLAYER_INPUT,
    CONTAINER_CLOSE,
    CONTAINER_CLICK,
    CONTAINER_ACK,
    SET_CREATIVE_MODE_SLOT,
    CONTAINER_BUTTON_CLICK,
    SIGN_UPDATE,
    PLAYER_ABILITIES,
    COMMAND_SUGGESTION,
    CLIENT_INFORMATION,
    CLIENT_COMMAND,
    CUSTOM_PAYLOAD,
    TELEPORT_TO_ENTITY,
    RESOURCE_PACK;

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}
