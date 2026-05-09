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
package com.viaversion.viaversion.protocol.shared_registration.def.base;

import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.rewriter.RegistryDataRewriter;
import com.viaversion.viaversion.protocol.shared_registration.PacketBound;
import com.viaversion.viaversion.protocol.shared_registration.RegistrationContext;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ClientboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ClientboundPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ServerboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ServerboundPackets1_20_2;

public final class ConfigurationRegistrations {

    public static <CU extends ClientboundPacketType, SU extends ServerboundPacketType> void registerConfigurationStateSwitching(final RegistrationContext<CU, SU> ctx) {
        ctx.serverbound(ServerboundPackets1_20_2.CONFIGURATION_ACKNOWLEDGED,
            packetType -> ctx.protocol().registerServerbound(packetType, wrapper -> wrapper.user().getProtocolInfo().setClientState(State.CONFIGURATION)),
            PacketBound.ADDED_AT_MIN
        );

        ctx.clientbound(ClientboundPackets1_20_2.START_CONFIGURATION, packetType -> ctx.protocol().registerClientbound(packetType, wrapper -> {
            wrapper.user().getProtocolInfo().setServerState(State.CONFIGURATION);

            // Mimic client behaviour by resetting tracked entities when configuration starts.
            final EntityTracker tracker = wrapper.user().getEntityTracker(ctx.protocol().getClass());
            if (tracker != null) {
                tracker.clear();
            }
        }), PacketBound.ADDED_AT_MIN);

        ctx.serverbound(ServerboundConfigurationPackets1_20_2.FINISH_CONFIGURATION,
            packetType -> ctx.protocol().registerServerbound(packetType, wrapper -> wrapper.user().getProtocolInfo().setClientState(State.PLAY)),
            PacketBound.ADDED_AT_MIN
        );

        ctx.clientbound(ClientboundConfigurationPackets1_20_2.FINISH_CONFIGURATION, packetType -> ctx.protocol().registerClientbound(packetType, wrapper -> {
                final RegistryDataRewriter registryDataRewriter = ctx.protocol().getRegistryDataRewriter();
                if (registryDataRewriter != null) {
                    registryDataRewriter.sendMissingRegistries(wrapper.user());
                }
                wrapper.user().getProtocolInfo().setServerState(State.PLAY);
            }),
            PacketBound.ADDED_AT_MIN
        );
    }
}

