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
package com.viaversion.viaversion.protocols.v1_21to1_21_2.rewriter;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_21;
import com.viaversion.viaversion.api.type.types.version.Types1_21_2;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPacket1_21;
import com.viaversion.viaversion.rewriter.ParticleRewriter;

public final class ParticleRewriter1_21_2 extends ParticleRewriter<ClientboundPacket1_21> {

    public ParticleRewriter1_21_2(final Protocol<ClientboundPacket1_21, ?, ?, ?> protocol) {
        super(protocol, Types1_21.PARTICLE, Types1_21_2.PARTICLE);
    }

    private void floatsToARGB(final Particle particle, final int fromIndex) {
        final Particle.ParticleData<Float> r = particle.removeArgument(fromIndex);
        final Particle.ParticleData<Float> g = particle.removeArgument(fromIndex);
        final Particle.ParticleData<Float> b = particle.removeArgument(fromIndex);
        final int rgb = 255 << 24 | (int) (r.getValue() * 255) << 16 | (int) (g.getValue() * 255) << 8 | (int) (b.getValue() * 255);
        particle.add(fromIndex, Types.INT, rgb);
    }

    @Override
    public void rewriteParticle(final UserConnection connection, final Particle particle) {
        super.rewriteParticle(connection, particle);
        
        final String identifier = protocol.getMappingData().getParticleMappings().mappedIdentifier(particle.id());
        if ("minecraft:dust_color_transition".equals(identifier)) {
            floatsToARGB(particle, 0);
            floatsToARGB(particle, 1);
        } else if ("minecraft:dust".equals(identifier)) {
            floatsToARGB(particle, 0);
        }
    }
}
