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
package com.viaversion.viaversion.protocols.v1_20_3to1_20_5.rewriter;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.ParticleMappings;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundPacket1_20_3;
import com.viaversion.viaversion.rewriter.ParticleRewriter;

public final class ParticleRewriter1_20_5 extends ParticleRewriter<ClientboundPacket1_20_3> {

    public ParticleRewriter1_20_5(final Protocol<ClientboundPacket1_20_3, ?, ?, ?> protocol) {
        super(protocol);
    }

    @Override
    public void rewriteParticle(final UserConnection connection, final Particle particle) {
        super.rewriteParticle(connection, particle);

        final ParticleMappings particleMappings = protocol.getMappingData().getParticleMappings();
        if (particle.id() == particleMappings.mappedId("entity_effect")) {
            particle.add(Types.INT, 0); // Default color, changed in the area effect handler
        } else if (particle.id() == particleMappings.mappedId("item")) {
            final Particle.ParticleData<Item> data = particle.getArgument(0);
            if (data.getValue().isEmpty()) {
                data.setValue(new StructuredItem(1, 1));
            }
        }
    }
}
