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
package com.viaversion.viaversion.protocol.shared_registration.def;

import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocol.shared_registration.RegistrationContext;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_14_4to1_15.packet.ClientboundPackets1_15;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.packet.ClientboundPackets1_19;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import org.checkerframework.checker.nullness.qual.Nullable;

final class ParticleRegistrations {

    static <CU extends ClientboundPacketType> void registerParticlePackets1_13(final RegistrationContext<CU, ?> ctx, final ParticleRewriter<CU> pr) {
        ctx.clientbound(ClientboundPackets1_13.LEVEL_PARTICLES, type -> pr.registerLevelParticles1_13(type, Types.FLOAT));
    }

    static <CU extends ClientboundPacketType> void registerParticlePackets1_15_2(final RegistrationContext<CU, ?> ctx, final ParticleRewriter<CU> pr) {
        ctx.clientbound(ClientboundPackets1_15.LEVEL_PARTICLES, type -> pr.registerLevelParticles1_13(type, Types.DOUBLE));
    }

    static <CU extends ClientboundPacketType> void registerParticlePackets1_19(final RegistrationContext<CU, ?> ctx, final ParticleRewriter<CU> pr) {
        ctx.clientbound(ClientboundPackets1_19.LEVEL_PARTICLES, pr::registerLevelParticles1_19);
    }

    static <CU extends ClientboundPacketType> void registerParticlePackets1_20(final RegistrationContext<CU, ?> ctx, final ParticleRewriter<CU> pr) {
        ctx.clientbound(ClientboundPackets1_19.LEVEL_PARTICLES, pr::registerLevelParticles1_19);
    }

    static <CU extends ClientboundPacketType> void registerParticlePackets1_20_5(final RegistrationContext<CU, ?> ctx, final ParticleRewriter<CU> pr) {
        ctx.clientbound(ClientboundPackets1_20_5.LEVEL_PARTICLES, pr::registerLevelParticles1_20_5);
        ctx.clientbound(ClientboundPackets1_20_5.EXPLODE, pr::registerExplode1_20_5);
    }

    static <CU extends ClientboundPacketType> void registerParticlePackets1_21_2(final RegistrationContext<CU, ?> ctx, final ParticleRewriter<CU> pr) {
        ctx.clientbound(ClientboundPackets1_21_2.LEVEL_PARTICLES, pr::registerLevelParticles1_20_5);
        ctx.clientbound(ClientboundPackets1_21_2.EXPLODE, pr::registerExplode1_21_2);
    }

    static <CU extends ClientboundPacketType> void registerParticlePackets1_21_4(final RegistrationContext<CU, ?> ctx, final ParticleRewriter<CU> pr) {
        ctx.clientbound(ClientboundPackets1_21_2.LEVEL_PARTICLES, pr::registerLevelParticles1_21_4);
        ctx.clientbound(ClientboundPackets1_21_2.EXPLODE, pr::registerExplode1_21_2);
    }

    static <CU extends ClientboundPacketType> void registerParticlePackets1_21_9(final RegistrationContext<CU, ?> ctx, final ParticleRewriter<CU> pr) {
        ctx.clientbound(ClientboundPackets1_21_9.LEVEL_PARTICLES, pr::registerLevelParticles1_21_4);
        ctx.clientbound(ClientboundPackets1_21_9.EXPLODE, pr::registerExplode1_21_9);
    }

    static @Nullable <CU extends ClientboundPacketType, SU extends ServerboundPacketType> ParticleRewriter<CU> particle(final RegistrationContext<CU, SU> ctx) {
        return (ParticleRewriter<CU>) ctx.protocol().getParticleRewriter();
    }
}
