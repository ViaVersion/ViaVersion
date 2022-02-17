/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2022 ViaVersion and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.viaversion.viaversion.api.type.types.minecraft;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.ParticleMappings;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.Particle;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public class ParticleType extends Type<Particle> {

    private final Int2ObjectMap<ParticleReader> readers;

    public ParticleType(final Int2ObjectMap<ParticleReader> readers) {
        super("Particle", Particle.class);
        this.readers = readers;
    }

    public ParticleType() {
        this(new Int2ObjectArrayMap<>());
    }

    public ParticleTypeFiller filler(final Protocol<?, ?, ?, ?> protocol) {
        return filler(protocol, true);
    }

    public ParticleTypeFiller filler(final Protocol<?, ?, ?, ?> protocol, final boolean useMappedNames) {
        return this.new ParticleTypeFiller(protocol, useMappedNames);
    }

    @Override
    public void write(final ByteBuf buffer, final Particle object) throws Exception {
        Type.VAR_INT.writePrimitive(buffer, object.getId());
        for (final Particle.ParticleData data : object.getArguments()) {
            data.getType().write(buffer, data.getValue());
        }
    }

    @Override
    public Particle read(final ByteBuf buffer) throws Exception {
        final int type = Type.VAR_INT.readPrimitive(buffer);
        final Particle particle = new Particle(type);

        final ParticleReader reader = readers.get(type);
        if (reader != null) {
            reader.read(buffer, particle);
        }
        return particle;
    }

    public static ParticleReader itemHandler(final Type<Item> itemType) {
        return (buf, particle) -> particle.add(itemType, itemType.read(buf));
    }

    public static final class Readers {

        public static final ParticleReader BLOCK = (buf, particle) -> {
            particle.add(Type.VAR_INT, Type.VAR_INT.readPrimitive(buf)); // Flat Block
        };
        public static final ParticleReader ITEM = itemHandler(Type.FLAT_ITEM);
        public static final ParticleReader VAR_INT_ITEM = itemHandler(Type.FLAT_VAR_INT_ITEM);
        public static final ParticleReader DUST = (buf, particle) -> {
            particle.add(Type.FLOAT, Type.FLOAT.readPrimitive(buf)); // Red 0-1
            particle.add(Type.FLOAT, Type.FLOAT.readPrimitive(buf)); // Green 0-1
            particle.add(Type.FLOAT, Type.FLOAT.readPrimitive(buf)); // Blue 0-1
            particle.add(Type.FLOAT, Type.FLOAT.readPrimitive(buf)); // Scale 0.01-4
        };
        public static final ParticleReader DUST_TRANSITION = (buf, particle) -> {
            particle.add(Type.FLOAT, Type.FLOAT.readPrimitive(buf)); // Red 0-1
            particle.add(Type.FLOAT, Type.FLOAT.readPrimitive(buf)); // Green 0-1
            particle.add(Type.FLOAT, Type.FLOAT.readPrimitive(buf)); // Blue 0-1
            particle.add(Type.FLOAT, Type.FLOAT.readPrimitive(buf)); // Scale 0.01-4
            particle.add(Type.FLOAT, Type.FLOAT.readPrimitive(buf)); // Red
            particle.add(Type.FLOAT, Type.FLOAT.readPrimitive(buf)); // Green
            particle.add(Type.FLOAT, Type.FLOAT.readPrimitive(buf)); // Blue
        };
        public static final ParticleReader VIBRATION = (buf, particle) -> {
            particle.add(Type.POSITION1_14, Type.POSITION1_14.read(buf)); // From block pos

            String resourceLocation = Type.STRING.read(buf);
            particle.add(Type.STRING, resourceLocation);
            if (resourceLocation.startsWith("minecraft:")) {
                resourceLocation = resourceLocation.substring(10);
            }

            if (resourceLocation.equals("block")) {
                particle.add(Type.POSITION1_14, Type.POSITION1_14.read(buf)); // Target block pos
            } else if (resourceLocation.equals("entity")) {
                particle.add(Type.VAR_INT, Type.VAR_INT.readPrimitive(buf)); // Target entity
            } else {
                Via.getPlatform().getLogger().warning("Unknown vibration path position source type: " + resourceLocation);
            }
            particle.add(Type.VAR_INT, Type.VAR_INT.readPrimitive(buf)); // Arrival in ticks
        };
        public static final ParticleReader SCULK_CHARGE = (buf, particle) -> {
            particle.add(Type.FLOAT, Type.FLOAT.readPrimitive(buf)); // Roll
        };
        public static final ParticleReader SHRIEK = (buf, particle) -> {
            particle.add(Type.VAR_INT, Type.VAR_INT.readPrimitive(buf)); // Delay
        };
    }

    public final class ParticleTypeFiller {

        private final ParticleMappings mappings;
        private final boolean useMappedNames;

        private ParticleTypeFiller(final Protocol<?, ?, ?, ?> protocol, final boolean useMappedNames) {
            this.mappings = protocol.getMappingData().getParticleMappings();
            this.useMappedNames = useMappedNames;
        }

        public ParticleTypeFiller reader(final String identifier, final ParticleReader reader) {
            readers.put(useMappedNames ? mappings.mappedId(identifier) : mappings.id(identifier), reader);
            return this;
        }

        public ParticleTypeFiller reader(final int id, final ParticleReader reader) {
            readers.put(id, reader);
            return this;
        }
    }

    @FunctionalInterface
    public interface ParticleReader {

        /**
         * Reads particle data from the buffer and adds it to the particle data.
         *
         * @param buf      buffer
         * @param particle particle
         * @throws Exception if an error occurs during buffer reading
         */
        void read(ByteBuf buf, Particle particle) throws Exception;
    }
}
