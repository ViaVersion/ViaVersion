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
package com.viaversion.viaversion.protocols.v1_21_2to1_21_4.rewriter;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPacket1_21_2;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import java.util.concurrent.ThreadLocalRandom;

public final class ParticleRewriter1_21_4 extends ParticleRewriter<ClientboundPacket1_21_2> {

    public ParticleRewriter1_21_4(final Protocol<ClientboundPacket1_21_2, ?, ?, ?> protocol) {
        super(protocol);
    }

    @Override
    public void rewriteParticle(final UserConnection connection, final Particle particle) {
        super.rewriteParticle(connection, particle);

        final String identifier = protocol.getMappingData().getParticleMappings().mappedIdentifier(particle.id());
        if ("minecraft:trail".equals(identifier)) {
            // Duration
            particle.add(Types.VAR_INT, ThreadLocalRandom.current().nextInt(40) + 10);
        }
    }
}
