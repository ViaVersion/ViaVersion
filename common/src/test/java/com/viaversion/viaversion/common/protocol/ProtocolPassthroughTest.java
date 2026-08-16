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
package com.viaversion.viaversion.common.protocol;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.common.PlatformTestBase;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.exception.CancelException;
import com.viaversion.viaversion.exception.InformativeException;
import com.viaversion.viaversion.protocol.AbstractSimpleProtocol;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import com.viaversion.viaversion.protocols.base.InitialBaseProtocol;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.Protocol1_20To1_20_2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ProtocolPassthroughTest extends PlatformTestBase {

    @Test
    void mappingOnlyProtocolsMaySkip() {
        Assertions.assertTrue(new MappingOnlyProtocol().maySkipUnregisteredPackets());
    }

    @Test
    void transformOverrideBlocksSkip() {
        Assertions.assertFalse(new CatchAllProtocol().maySkipUnregisteredPackets());
        Assertions.assertFalse(new Protocol1_20To1_20_2().maySkipUnregisteredPackets());
    }

    @Test
    void handshakeBaseProtocolAllowsSkip() {
        Assertions.assertTrue(new InitialBaseProtocol().maySkipUnregisteredPackets());
    }

    @Test
    void pipelineSkipsUnregisteredIdentityPackets() {
        final UserConnection connection = new UserConnectionImpl(null);
        new ProtocolPipelineImpl(connection);
        final ProtocolPipelineImpl pipeline = (ProtocolPipelineImpl) connection.getProtocolInfo().getPipeline();
        final MappingOnlyProtocol protocol = new MappingOnlyProtocol();
        protocol.registerClientbound(State.PLAY, 5, 5, wrapper -> {
        });
        pipeline.add(protocol);

        Assertions.assertFalse(pipeline.canPassthroughPacket(Direction.CLIENTBOUND, State.PLAY, 5));
        Assertions.assertTrue(pipeline.canPassthroughPacket(Direction.CLIENTBOUND, State.PLAY, 42));
        Assertions.assertFalse(pipeline.canPassthroughPacket(Direction.SERVERBOUND, State.HANDSHAKE, 0));
    }

    @Test
    void pipelineDoesNotSkipWhenTransformIsOverridden() {
        final UserConnection connection = new UserConnectionImpl(null);
        new ProtocolPipelineImpl(connection);
        final ProtocolPipelineImpl pipeline = (ProtocolPipelineImpl) connection.getProtocolInfo().getPipeline();
        pipeline.add(new CatchAllProtocol());

        Assertions.assertFalse(pipeline.canPassthroughPacket(Direction.CLIENTBOUND, State.PLAY, 42));
    }

    private static final class MappingOnlyProtocol extends AbstractSimpleProtocol {
    }

    private static final class CatchAllProtocol extends AbstractSimpleProtocol {
        @Override
        public void transform(final Direction direction, final State state, final PacketWrapper packetWrapper)
            throws InformativeException, CancelException {
            super.transform(direction, state, packetWrapper);
        }
    }
}
