/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
package com.viaversion.viaversion.sponge.providers;

import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.MovementTransmitterProvider;

import java.lang.reflect.Field;

public class SpongeViaMovementTransmitter extends MovementTransmitterProvider {
    // Used for packet mode
    private Object idlePacket;
    private Object idlePacket2;

    public SpongeViaMovementTransmitter() {
        Class<?> idlePacketClass;
        try {
            idlePacketClass = Class.forName("net.minecraft.network.play.client.C03PacketPlayer");
        } catch (ClassNotFoundException e) {
            return; // We'll hope this is 1.9.4+
        }
        try {
            idlePacket = idlePacketClass.newInstance();
            idlePacket2 = idlePacketClass.newInstance();

            Field flying = idlePacketClass.getDeclaredField("field_149474_g");
            flying.setAccessible(true);

            flying.set(idlePacket2, true);
        } catch (NoSuchFieldException | InstantiationException | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException("Couldn't make player idle packet, help!", e);
        }
    }

    @Override
    public Object getFlyingPacket() {
        if (idlePacket == null)
            throw new NullPointerException("Could not locate flying packet");
        return idlePacket2;
    }

    @Override
    public Object getGroundPacket() {
        if (idlePacket == null)
            throw new NullPointerException("Could not locate flying packet");
        return idlePacket;
    }
}
