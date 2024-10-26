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
package com.viaversion.viaversion.rewriter;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.ParticleMappings;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.rewriter.ItemRewriter;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;

public class ParticleRewriter<C extends ClientboundPacketType> implements com.viaversion.viaversion.api.rewriter.ParticleRewriter {

    protected final Protocol<C, ?, ?, ?> protocol;
    private final Type<Particle> particleType;
    private final Type<Particle> mappedParticleType;

    public ParticleRewriter(final Protocol<C, ?, ?, ?> protocol) {
        this(protocol, null, null);
    }

    public ParticleRewriter(final Protocol<C, ?, ?, ?> protocol, Type<Particle> particleType, Type<Particle> mappedParticleType) {
        this.protocol = protocol;
        this.particleType = particleType;
        this.mappedParticleType = mappedParticleType;
    }

    public void registerLevelParticles(C packetType, Type<?> coordType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // 0 - Particle ID
                map(Types.BOOLEAN); // 1 - Long Distance
                map(coordType); // 2 - X
                map(coordType); // 3 - Y
                map(coordType); // 4 - Z
                map(Types.FLOAT); // 5 - Offset X
                map(Types.FLOAT); // 6 - Offset Y
                map(Types.FLOAT); // 7 - Offset Z
                map(Types.FLOAT); // 8 - Particle Data
                map(Types.INT); // 9 - Particle Count
                handler(levelParticlesHandler());
            }
        });
    }

    public void registerLevelParticles1_19(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Particle ID
                map(Types.BOOLEAN); // 1 - Long Distance
                map(Types.DOUBLE); // 2 - X
                map(Types.DOUBLE); // 3 - Y
                map(Types.DOUBLE); // 4 - Z
                map(Types.FLOAT); // 5 - Offset X
                map(Types.FLOAT); // 6 - Offset Y
                map(Types.FLOAT); // 7 - Offset Z
                map(Types.FLOAT); // 8 - Particle Data
                map(Types.INT); // 9 - Particle Count
                handler(levelParticlesHandler(Types.VAR_INT));
            }
        });
    }

    public void registerLevelParticles1_20_5(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.BOOLEAN); // Long Distance
                map(Types.DOUBLE); // X
                map(Types.DOUBLE); // Y
                map(Types.DOUBLE); // Z
                map(Types.FLOAT); // Offset X
                map(Types.FLOAT); // Offset Y
                map(Types.FLOAT); // Offset Z
                map(Types.FLOAT); // Particle Data
                map(Types.INT); // Particle Count
                handler(wrapper -> {
                    final Particle particle = wrapper.passthroughAndMap(particleType, mappedParticleType);
                    rewriteParticle(wrapper.user(), particle);
                });
            }
        });
    }

    public void registerExplosion(C packetType) {
        final SoundRewriter<C> soundRewriter = new SoundRewriter<>(protocol);
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.DOUBLE); // X
            wrapper.passthrough(Types.DOUBLE); // Y
            wrapper.passthrough(Types.DOUBLE); // Z
            wrapper.passthrough(Types.FLOAT); // Power
            final int blocks = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < blocks; i++) {
                wrapper.passthrough(Types.BYTE); // Relative X
                wrapper.passthrough(Types.BYTE); // Relative Y
                wrapper.passthrough(Types.BYTE); // Relative Z
            }
            wrapper.passthrough(Types.FLOAT); // Knockback X
            wrapper.passthrough(Types.FLOAT); // Knockback Y
            wrapper.passthrough(Types.FLOAT); // Knockback Z
            wrapper.passthrough(Types.VAR_INT); // Block interaction type

            final Particle smallExplosionParticle = wrapper.passthroughAndMap(particleType, mappedParticleType);
            final Particle largeExplosionParticle = wrapper.passthroughAndMap(particleType, mappedParticleType);
            rewriteParticle(wrapper.user(), smallExplosionParticle);
            rewriteParticle(wrapper.user(), largeExplosionParticle);

            soundRewriter.soundHolderHandler().handle(wrapper);
        });
    }

    public void registerExplosion1_21_2(C packetType) {
        final SoundRewriter<C> soundRewriter = new SoundRewriter<>(protocol);
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.DOUBLE); // X
            wrapper.passthrough(Types.DOUBLE); // Y
            wrapper.passthrough(Types.DOUBLE); // Z
            wrapper.passthrough(Types.DOUBLE); // Knockback X
            wrapper.passthrough(Types.DOUBLE); // Knockback Y
            wrapper.passthrough(Types.DOUBLE); // Knockback Z

            final Particle explosionParticle = wrapper.read(particleType);
            wrapper.write(mappedParticleType, explosionParticle);
            rewriteParticle(wrapper.user(), explosionParticle);

            soundRewriter.soundHolderHandler().handle(wrapper);
        });
    }

    public PacketHandler levelParticlesHandler() {
        return levelParticlesHandler(Types.INT);
    }

    public PacketHandler levelParticlesHandler(Type<Integer> idType) {
        return wrapper -> {
            int id = wrapper.get(idType, 0);
            if (id == -1) {
                return;
            }

            ParticleMappings mappings = protocol.getMappingData().getParticleMappings();
            if (mappings.isBlockParticle(id)) {
                int data = wrapper.read(Types.VAR_INT);
                wrapper.write(Types.VAR_INT, protocol.getMappingData().getNewBlockStateId(data));
            } else if (mappings.isItemParticle(id)) {
                ItemRewriter<?> itemRewriter = protocol.getItemRewriter();
                final Item item = wrapper.read(itemRewriter.itemType());
                wrapper.write(itemRewriter.mappedItemType(), itemRewriter.handleItemToClient(wrapper.user(), item));
            }

            int mappedId = protocol.getMappingData().getNewParticleId(id);
            if (mappedId != id) {
                wrapper.set(idType, 0, mappedId);
            }
        };
    }

    @Override
    public void rewriteParticle(final UserConnection connection, final Particle particle) {
        ParticleMappings mappings = protocol.getMappingData().getParticleMappings();
        ItemRewriter<?> itemRewriter = protocol.getItemRewriter();
        int id = particle.id();
        if (mappings.isBlockParticle(id)) {
            Particle.ParticleData<Integer> data = particle.getArgument(0);
            data.setValue(protocol.getMappingData().getNewBlockStateId(data.getValue()));
        } else if (mappings.isItemParticle(id) && itemRewriter != null) {
            Particle.ParticleData<Item> data = particle.getArgument(0);
            Item item = itemRewriter.handleItemToClient(connection, data.getValue());
            if (itemRewriter.mappedItemType() != null && itemRewriter.itemType() != itemRewriter.mappedItemType()) {
                // Replace the type
                particle.set(0, itemRewriter.mappedItemType(), item);
            } else {
                data.setValue(item);
            }
        }

        particle.setId(protocol.getMappingData().getNewParticleId(id));
    }
}
