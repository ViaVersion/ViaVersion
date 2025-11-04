/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.api.type.types.misc;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Key;
import io.netty.buffer.ByteBuf;

public class ParticleType extends DynamicType<Particle> {

    public ParticleType() {
        super(Particle.class);
    }

    @Override
    public void write(final ByteBuf buffer, final Particle object) {
        Types.VAR_INT.writePrimitive(buffer, object.id());
        for (final Particle.ParticleData<?> data : object.getArguments()) {
            data.write(buffer);
        }
    }

    @Override
    public Particle read(final ByteBuf buffer) {
        final int type = Types.VAR_INT.readPrimitive(buffer);
        final Particle particle = new Particle(type);
        readData(buffer, particle);
        return particle;
    }

    @Override
    protected FullMappings mappings(final Protocol<?, ?, ?, ?> protocol) {
        return protocol.getMappingData().getParticleMappings();
    }

    public static DataReader<Particle> itemHandler(final Type<Item> itemType) {
        return (buf, particle) -> particle.add(itemType, itemType.read(buf));
    }

    public static final class Readers {

        public static final DataReader<Particle> BLOCK = (buf, particle) -> {
            particle.add(Types.VAR_INT, Types.VAR_INT.readPrimitive(buf)); // Flat Block
        };
        public static final DataReader<Particle> ITEM1_13 = itemHandler(Types.ITEM1_13);
        public static final DataReader<Particle> ITEM1_13_2 = itemHandler(Types.ITEM1_13_2);
        public static final DataReader<Particle> DUST = (buf, particle) -> {
            particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf)); // Red 0-1
            particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf)); // Green 0-1
            particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf)); // Blue 0-1
            particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf)); // Scale 0.01-4
        };
        public static final DataReader<Particle> DUST1_21_2 = (buf, particle) -> {
            particle.add(Types.INT, Types.INT.readPrimitive(buf)); // RGB
            particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf)); // Scale 0.01-4
        };
        public static final DataReader<Particle> DUST_TRANSITION = (buf, particle) -> {
            particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf)); // Red 0-1
            particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf)); // Green 0-1
            particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf)); // Blue 0-1
            particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf)); // Scale 0.01-4 (moved to the end as of 24w03a / 1.20.5)
            particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf)); // Red
            particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf)); // Green
            particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf)); // Blue
        };
        public static final DataReader<Particle> DUST_TRANSITION1_21_2 = (buf, particle) -> {
            particle.add(Types.INT, Types.INT.readPrimitive(buf)); // From RGB
            particle.add(Types.INT, Types.INT.readPrimitive(buf)); // To RGB
            particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf)); // Scale 0.01-4
        };
        public static final DataReader<Particle> VIBRATION = (buf, particle) -> {
            particle.add(Types.BLOCK_POSITION1_14, Types.BLOCK_POSITION1_14.read(buf)); // From block pos

            String identifier = Types.STRING.read(buf);
            particle.add(Types.STRING, identifier);

            identifier = Key.stripMinecraftNamespace(identifier);
            if (identifier.equals("block")) {
                particle.add(Types.BLOCK_POSITION1_14, Types.BLOCK_POSITION1_14.read(buf)); // Target block pos
            } else if (identifier.equals("entity")) {
                particle.add(Types.VAR_INT, Types.VAR_INT.readPrimitive(buf)); // Target entity
            } else {
                Via.getPlatform().getLogger().warning("Unknown vibration path position source type: " + identifier);
            }
            particle.add(Types.VAR_INT, Types.VAR_INT.readPrimitive(buf)); // Arrival in ticks
        };
        public static final DataReader<Particle> VIBRATION1_19 = (buf, particle) -> {
            String identifier = Types.STRING.read(buf);
            particle.add(Types.STRING, identifier);

            identifier = Key.stripMinecraftNamespace(identifier);
            if (identifier.equals("block")) {
                particle.add(Types.BLOCK_POSITION1_14, Types.BLOCK_POSITION1_14.read(buf)); // Target block pos
            } else if (identifier.equals("entity")) {
                particle.add(Types.VAR_INT, Types.VAR_INT.readPrimitive(buf)); // Target entity
                particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf)); // Y offset
            } else {
                Via.getPlatform().getLogger().warning("Unknown vibration path position source type: " + identifier);
            }
            particle.add(Types.VAR_INT, Types.VAR_INT.readPrimitive(buf)); // Arrival in ticks
        };
        public static final DataReader<Particle> VIBRATION1_20_3 = (buf, particle) -> {
            final int sourceTypeId = Types.VAR_INT.readPrimitive(buf);
            particle.add(Types.VAR_INT, sourceTypeId);
            if (sourceTypeId == 0) { // Block
                particle.add(Types.BLOCK_POSITION1_14, Types.BLOCK_POSITION1_14.read(buf)); // Target block pos
            } else if (sourceTypeId == 1) { // Entity
                particle.add(Types.VAR_INT, Types.VAR_INT.readPrimitive(buf)); // Target entity
                particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf)); // Y offset
            } else {
                Via.getPlatform().getLogger().warning("Unknown vibration path position source type: " + sourceTypeId);
            }
            particle.add(Types.VAR_INT, Types.VAR_INT.readPrimitive(buf)); // Arrival in ticks
        };
        public static final DataReader<Particle> SCULK_CHARGE = (buf, particle) -> {
            particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf)); // Roll
        };
        public static final DataReader<Particle> SHRIEK = (buf, particle) -> {
            particle.add(Types.VAR_INT, Types.VAR_INT.readPrimitive(buf)); // Delay
        };
        public static final DataReader<Particle> COLOR = (buf, particle) -> {
            particle.add(Types.INT, Types.INT.readPrimitive(buf));
        };
        public static final DataReader<Particle> TRAIL1_21_2 = (buf, particle) -> {
            particle.add(Types.DOUBLE, Types.DOUBLE.readPrimitive(buf)); // Target X
            particle.add(Types.DOUBLE, Types.DOUBLE.readPrimitive(buf)); // Target Y
            particle.add(Types.DOUBLE, Types.DOUBLE.readPrimitive(buf)); // Target Z
            particle.add(Types.INT, Types.INT.readPrimitive(buf)); // Color
        };
        public static final DataReader<Particle> TRAIL1_21_4 = (buf, particle) -> {
            particle.add(Types.DOUBLE, Types.DOUBLE.readPrimitive(buf)); // Target X
            particle.add(Types.DOUBLE, Types.DOUBLE.readPrimitive(buf)); // Target Y
            particle.add(Types.DOUBLE, Types.DOUBLE.readPrimitive(buf)); // Target Z
            particle.add(Types.INT, Types.INT.readPrimitive(buf)); // Color
            particle.add(Types.VAR_INT, Types.VAR_INT.readPrimitive(buf)); // Duration
        };
        public static final DataReader<Particle> POWER = (buf, particle) -> {
            particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf));
        };
        public static final DataReader<Particle> SPELL = (buf, particle) -> {
            particle.add(Types.INT, Types.INT.readPrimitive(buf)); // Color
            particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf)); // Power
        };

        public static DataReader<Particle> item(Type<Item> item) {
            return (buf, particle) -> particle.add(item, item.read(buf));
        }
    }
}
