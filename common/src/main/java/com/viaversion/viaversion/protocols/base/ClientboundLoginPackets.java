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
package com.viaversion.viaversion.protocols.base;

import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.protocols.base.packet.BaseClientboundPacket;

public enum ClientboundLoginPackets implements BaseClientboundPacket {
    LOGIN_DISCONNECT, // 0x00
    HELLO, // 0x01
    GAME_PROFILE, // 0x02
    LOGIN_COMPRESSION, // 0x03
    CUSTOM_QUERY, // 0x04
    COOKIE_REQUEST; // 0x05

    @Override
    public final int getId() {
        return ordinal();
    }

    @Override
    public final String getName() {
        return name();
    }

    @Override
    public final State state() {
        return State.LOGIN;
    }
}
