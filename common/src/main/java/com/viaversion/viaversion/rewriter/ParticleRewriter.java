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
package com.viaversion.viaversion.rewriter;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.ParticleMappings;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
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
        this.protocol = protocol;
        this.particleType = protocol.types() != null ? protocol.types().particle() : null;
        this.mappedParticleType = protocol.mappedTypes() != null ? protocol.mappedTypes().particle() : null;
    }

    public void registerLevelParticles1_13(C packetType, Type<?> coordType) {
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
                handler(levelParticlesHandler1_13(Types.INT));
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
                handler(levelParticlesHandler1_13(Types.VAR_INT));
            }
        });
    }

    public PacketHandler levelParticlesHandler1_13(Type<Integer> idType) {
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

    public void registerLevelParticles1_20_5(final C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.BOOLEAN); // Override limiter
            wrapper.passthrough(Types.DOUBLE); // X
            wrapper.passthrough(Types.DOUBLE); // Y
            wrapper.passthrough(Types.DOUBLE); // Z
            wrapper.passthrough(Types.FLOAT); // Offset X
            wrapper.passthrough(Types.FLOAT); // Offset Y
            wrapper.passthrough(Types.FLOAT); // Offset Z
            wrapper.passthrough(Types.FLOAT); // Particle Data
            wrapper.passthrough(Types.INT); // Particle Count

            final Particle particle = wrapper.passthroughAndMap(particleType, mappedParticleType);
            rewriteParticle(wrapper.user(), particle);
        });
    }

    public void registerLevelParticles1_21_4(final C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.BOOLEAN); // Override limiter
            wrapper.passthrough(Types.BOOLEAN); // Always show
            wrapper.passthrough(Types.DOUBLE); // X
            wrapper.passthrough(Types.DOUBLE); // Y
            wrapper.passthrough(Types.DOUBLE); // Z
            wrapper.passthrough(Types.FLOAT); // Offset X
            wrapper.passthrough(Types.FLOAT); // Offset Y
            wrapper.passthrough(Types.FLOAT); // Offset Z
            wrapper.passthrough(Types.FLOAT); // Particle Data
            wrapper.passthrough(Types.INT); // Particle Count

            final Particle particle = wrapper.passthroughAndMap(particleType, mappedParticleType);
            rewriteParticle(wrapper.user(), particle);
        });
    }

    public void registerExplode1_20_5(final C packetType) {
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

    public void registerExplode1_21_2(final C packetType) {
        final SoundRewriter<C> soundRewriter = new SoundRewriter<>(protocol);
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.DOUBLE); // X
            wrapper.passthrough(Types.DOUBLE); // Y
            wrapper.passthrough(Types.DOUBLE); // Z
            if (wrapper.passthrough(Types.BOOLEAN)) {
                wrapper.passthrough(Types.DOUBLE); // Knockback X
                wrapper.passthrough(Types.DOUBLE); // Knockback Y
                wrapper.passthrough(Types.DOUBLE); // Knockback Z
            }

            passthroughParticle(wrapper); // Explosion particle
            soundRewriter.soundHolderHandler().handle(wrapper);
        });
    }

    public void registerExplode1_21_9(final C packetType) {
        final SoundRewriter<C> soundRewriter = new SoundRewriter<>(protocol);
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.DOUBLE); // X
            wrapper.passthrough(Types.DOUBLE); // Y
            wrapper.passthrough(Types.DOUBLE); // Z
            wrapper.passthrough(Types.FLOAT); // Power
            wrapper.passthrough(Types.INT); // Block count
            if (wrapper.passthrough(Types.BOOLEAN)) {
                wrapper.passthrough(Types.DOUBLE); // Knockback X
                wrapper.passthrough(Types.DOUBLE); // Knockback Y
                wrapper.passthrough(Types.DOUBLE); // Knockback Z
            }

            passthroughParticle(wrapper); // Explosion particle
            soundRewriter.soundHolderHandler().handle(wrapper);

            final int blockParticles = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < blockParticles; i++) {
                passthroughParticle(wrapper);
                wrapper.passthrough(Types.FLOAT); // Scaling
                wrapper.passthrough(Types.FLOAT); // Speed
                wrapper.passthrough(Types.VAR_INT); // Weight
            }
        });
    }

    public Particle passthroughParticle(final PacketWrapper wrapper) {
        final Particle particle = wrapper.read(particleType);
        wrapper.write(mappedParticleType, particle);
        rewriteParticle(wrapper.user(), particle);
        return particle;
    }

    @Override
    public void rewriteParticle(final UserConnection connection, final Particle particle) {
        final ParticleMappings mappings = protocol.getMappingData().getParticleMappings();
        final ItemRewriter<?> itemRewriter = protocol.getItemRewriter();
        final int id = particle.id();
        if (mappings.isBlockParticle(id)) {
            final Particle.ParticleData<Integer> data = particle.getArgument(0);
            data.setValue(protocol.getMappingData().getNewBlockStateId(data.getValue()));
        } else if (mappings.isItemParticle(id) && itemRewriter != null) {
            final Particle.ParticleData<Item> data = particle.getArgument(0);
            final Item item = itemRewriter.handleItemToClient(connection, data.getValue());
            if (itemRewriter.mappedItemType() != null && itemRewriter.itemType() != itemRewriter.mappedItemType()) {
                // Replace the type
                particle.set(0, itemRewriter.mappedItemType(), item);
            } else {
                data.setValue(item);
            }
        }

        particle.setId(protocol.getMappingData().getNewParticleId(id));
    }

    public Type<Particle> particleType() {
        return particleType;
    }

    public Type<Particle> mappedParticleType() {
        return mappedParticleType;
    }
}
