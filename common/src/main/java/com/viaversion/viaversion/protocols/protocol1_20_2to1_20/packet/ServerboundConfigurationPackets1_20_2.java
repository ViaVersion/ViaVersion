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
package com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet;

import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ServerboundPacket1_20_3;

public enum ServerboundConfigurationPackets1_20_2 implements ServerboundPacket1_20_2, ServerboundPacket1_20_3 {

    CLIENT_INFORMATION, // 0x00
    CUSTOM_PAYLOAD, // 0x01
    FINISH_CONFIGURATION, // 0x02
    KEEP_ALIVE, // 0x03
    PONG, // 0x04
    RESOURCE_PACK; // 0x05

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
