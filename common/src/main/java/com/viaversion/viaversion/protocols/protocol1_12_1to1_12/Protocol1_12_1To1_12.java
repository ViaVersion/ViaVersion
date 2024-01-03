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
package com.viaversion.viaversion.protocols.protocol1_12_1to1_12;

import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.protocols.protocol1_12to1_11_1.ClientboundPackets1_12;
import com.viaversion.viaversion.protocols.protocol1_12to1_11_1.ServerboundPackets1_12;

public class Protocol1_12_1To1_12 extends AbstractProtocol<ClientboundPackets1_12, ClientboundPackets1_12_1, ServerboundPackets1_12, ServerboundPackets1_12_1> {

    public Protocol1_12_1To1_12() {
        super(ClientboundPackets1_12.class, ClientboundPackets1_12_1.class, ServerboundPackets1_12.class, ServerboundPackets1_12_1.class);
    }

    @Override
    protected void registerPackets() {
        cancelServerbound(ServerboundPackets1_12_1.CRAFT_RECIPE_REQUEST);
    }
}
