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
package com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet;

import com.viaversion.viaversion.api.protocol.packet.State;

public enum ServerboundConfigurationPackets1_21_6 implements ServerboundPacket1_21_6 {

    CLIENT_INFORMATION, // 0x00
    COOKIE_RESPONSE, // 0x01
    CUSTOM_PAYLOAD, // 0x02
    FINISH_CONFIGURATION, // 0x03
    KEEP_ALIVE, // 0x04
    PONG, // 0x05
    RESOURCE_PACK, // 0x06
    SELECT_KNOWN_PACKS, // 0x07
    CUSTOM_CLICK_ACTION; // 0x08

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public State state() {
        return State.CONFIGURATION;
    }
}
