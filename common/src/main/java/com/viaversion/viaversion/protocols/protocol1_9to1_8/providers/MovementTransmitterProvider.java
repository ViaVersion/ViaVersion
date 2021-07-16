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
package com.viaversion.viaversion.protocols.protocol1_9to1_8.providers;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.providers.Provider;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_8.ServerboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.storage.MovementTracker;
import com.viaversion.viaversion.util.PipelineUtil;
import io.netty.channel.ChannelHandlerContext;

public class MovementTransmitterProvider implements Provider {
    public void sendPlayer(UserConnection userConnection) {
        if (userConnection.getProtocolInfo().getState() == State.PLAY) {
            PacketWrapper wrapper = PacketWrapper.create(ServerboundPackets1_8.PLAYER_MOVEMENT.getId(), null, userConnection);
            wrapper.write(Type.BOOLEAN, userConnection.get(MovementTracker.class).isGround());
            try {
                wrapper.sendToServer(Protocol1_9To1_8.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            userConnection.get(MovementTracker.class).incrementIdlePacket();
        }
    }

    public abstract static class InternalsBased extends MovementTransmitterProvider {
        public abstract Object getFlyingPacket();

        public abstract Object getGroundPacket();

        public void sendPlayer(UserConnection userConnection) {
            // Old method using packet pipeline.
            ChannelHandlerContext context = PipelineUtil.getContextBefore("decoder", userConnection.getChannel().pipeline());
            if (context != null) {
                if (userConnection.get(MovementTracker.class).isGround()) {
                    context.fireChannelRead(getGroundPacket());
                } else {
                    context.fireChannelRead(getFlyingPacket());
                }
                userConnection.get(MovementTracker.class).incrementIdlePacket();
            }
        }
    }
}
