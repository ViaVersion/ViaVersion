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
package com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet;

import com.viaversion.viaversion.api.protocol.packet.State;

public enum ClientboundConfigurationPackets1_21_9 implements ClientboundPacket1_21_9 {

    COOKIE_REQUEST, // 0x00
    CUSTOM_PAYLOAD, // 0x01
    DISCONNECT, // 0x02
    FINISH_CONFIGURATION, // 0x03
    KEEP_ALIVE, // 0x04
    PING, // 0x05
    RESET_CHAT, // 0x06
    REGISTRY_DATA, // 0x07
    RESOURCE_PACK_POP, // 0x08
    RESOURCE_PACK_PUSH, // 0x09
    STORE_COOKIE, // 0x0A
    TRANSFER, // 0x0B
    UPDATE_ENABLED_FEATURES, // 0x0C
    UPDATE_TAGS, // 0x0D
    SELECT_KNOWN_PACKS, // 0x0E
    CUSTOM_REPORT_DETAILS, // 0x0F
    SERVER_LINKS, // 0x10
    CLEAR_DIALOG, // 0x11
    SHOW_DIALOG, // 0x12
    CODE_OF_CONDUCT; // 0x13

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
