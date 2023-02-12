/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2023 ViaVersion and contributors
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
package com.viaversion.viaversion.common.protocol.remapper;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.AbstractProtocol.ProtocolPacket;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.common.dummy.DummyInitializer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ProtocolHandlersTest {

    private static final Logger LOGGER = Logger.getGlobal();
    private static final boolean WARN = false;

    @BeforeAll
    static void init() {
        DummyInitializer.init();
    }

    @Test
    void testHandlersSize() {
        if (!WARN) {
            return;
        }

        for (final Protocol<?, ?, ?, ?> protocol : Via.getManager().getProtocolManager().getProtocols()) {
            if (!(protocol instanceof AbstractProtocol)) {
                continue;
            }

            final AbstractProtocol<?, ?, ?, ?> abstractProtocol = (AbstractProtocol<?, ?, ?, ?>) protocol;
            final List<ProtocolPacket> protocolMappings = new ArrayList<>(abstractProtocol.getClientbound().values());
            protocolMappings.addAll(abstractProtocol.getServerbound().values());

            for (final ProtocolPacket protocolMapping : protocolMappings) {
                if (!(protocolMapping.getRemapper() instanceof PacketHandlers)) {
                    continue;
                }

                final PacketHandlers packetHandlers = (PacketHandlers) protocolMapping.getRemapper();
                if (packetHandlers.handlersSize() == 0) {
                    LOGGER.warning("PacketHandlers instance has no handlers: " + protocolMapping + " in " + protocol.getClass().getSimpleName());
                } else if (packetHandlers.handlersSize() == 1) {
                    LOGGER.warning("PacketHandlers instance only has a single handler; consider using a PacketHandler lambda instead of extending PacketHandlers: "
                            + protocolMapping + " in " + protocol.getClass().getSimpleName());
                }
            }
        }
    }
}
