/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
import com.viaversion.viaversion.api.type.types.version.Types1_20_5;
import com.viaversion.viaversion.util.Key;
import io.netty.buffer.ByteBuf;

public class ParticleType extends DynamicType<Particle> {

    public ParticleType() {
        super(Particle.class);
    }

    @Override
    public void write(final ByteBuf buffer, final Particle object) throws Exception {
        Type.VAR_INT.writePrimitive(buffer, object.id());
        for (final Particle.ParticleData<?> data : object.getArguments()) {
            data.write(buffer);
        }
    }

    @Override
    public Particle read(final ByteBuf buffer) throws Exception {
        final int type = Type.VAR_INT.readPrimitive(buffer);
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
            particle.add(Type.VAR_INT, Type.VAR_INT.readPrimitive(buf)); // Flat Block
        };
        public static final DataReader<Particle> ITEM1_13 = itemHandler(Type.ITEM1_13);
        public static final DataReader<Particle> ITEM1_13_2 = itemHandler(Type.ITEM1_13_2);
        public static final DataReader<Particle> ITEM1_20_2 = itemHandler(Type.ITEM1_20_2);
        public static final DataReader<Particle> ITEM1_20_5 = itemHandler(Types1_20_5.ITEM);
        public static final DataReader<Particle> DUST = (buf, particle) -> {
            particle.add(Type.FLOAT, Type.FLOAT.readPrimitive(buf)); // Red 0-1
            particle.add(Type.FLOAT, Type.FLOAT.readPrimitive(buf)); // Green 0-1
            particle.add(Type.FLOAT, Type.FLOAT.readPrimitive(buf)); // Blue 0-1
            particle.add(Type.FLOAT, Type.FLOAT.readPrimitive(buf)); // Scale 0.01-4
        };
        public static final DataReader<Particle> DUST_TRANSITION = (buf, particle) -> {
            particle.add(Type.FLOAT, Type.FLOAT.readPrimitive(buf)); // Red 0-1
            particle.add(Type.FLOAT, Type.FLOAT.readPrimitive(buf)); // Green 0-1
            particle.add(Type.FLOAT, Type.FLOAT.readPrimitive(buf)); // Blue 0-1
            particle.add(Type.FLOAT, Type.FLOAT.readPrimitive(buf)); // Scale 0.01-4 (moved to the end as of 24w03a / 1.20.5)
            particle.add(Type.FLOAT, Type.FLOAT.readPrimitive(buf)); // Red
            particle.add(Type.FLOAT, Type.FLOAT.readPrimitive(buf)); // Green
            particle.add(Type.FLOAT, Type.FLOAT.readPrimitive(buf)); // Blue
        };
        public static final DataReader<Particle> VIBRATION = (buf, particle) -> {
            particle.add(Type.POSITION1_14, Type.POSITION1_14.read(buf)); // From block pos

            String resourceLocation = Type.STRING.read(buf);
            particle.add(Type.STRING, resourceLocation);

            resourceLocation = Key.stripMinecraftNamespace(resourceLocation);
            if (resourceLocation.equals("block")) {
                particle.add(Type.POSITION1_14, Type.POSITION1_14.read(buf)); // Target block pos
            } else if (resourceLocation.equals("entity")) {
                particle.add(Type.VAR_INT, Type.VAR_INT.readPrimitive(buf)); // Target entity
            } else {
                Via.getPlatform().getLogger().warning("Unknown vibration path position source type: " + resourceLocation);
            }
            particle.add(Type.VAR_INT, Type.VAR_INT.readPrimitive(buf)); // Arrival in ticks
        };
        public static final DataReader<Particle> VIBRATION1_19 = (buf, particle) -> {
            String resourceLocation = Type.STRING.read(buf);
            particle.add(Type.STRING, resourceLocation);

            resourceLocation = Key.stripMinecraftNamespace(resourceLocation);
            if (resourceLocation.equals("block")) {
                particle.add(Type.POSITION1_14, Type.POSITION1_14.read(buf)); // Target block pos
            } else if (resourceLocation.equals("entity")) {
                particle.add(Type.VAR_INT, Type.VAR_INT.readPrimitive(buf)); // Target entity
                particle.add(Type.FLOAT, Type.FLOAT.readPrimitive(buf)); // Y offset
            } else {
                Via.getPlatform().getLogger().warning("Unknown vibration path position source type: " + resourceLocation);
            }
            particle.add(Type.VAR_INT, Type.VAR_INT.readPrimitive(buf)); // Arrival in ticks
        };
        public static final DataReader<Particle> VIBRATION1_20_3 = (buf, particle) -> {
            final int sourceTypeId = Type.VAR_INT.readPrimitive(buf);
            particle.add(Type.VAR_INT, sourceTypeId);
            if (sourceTypeId == 0) { // Block
                particle.add(Type.POSITION1_14, Type.POSITION1_14.read(buf)); // Target block pos
            } else if (sourceTypeId == 1) { // Entity
                particle.add(Type.VAR_INT, Type.VAR_INT.readPrimitive(buf)); // Target entity
                particle.add(Type.FLOAT, Type.FLOAT.readPrimitive(buf)); // Y offset
            } else {
                Via.getPlatform().getLogger().warning("Unknown vibration path position source type: " + sourceTypeId);
            }
            particle.add(Type.VAR_INT, Type.VAR_INT.readPrimitive(buf)); // Arrival in ticks
        };
        public static final DataReader<Particle> SCULK_CHARGE = (buf, particle) -> {
            particle.add(Type.FLOAT, Type.FLOAT.readPrimitive(buf)); // Roll
        };
        public static final DataReader<Particle> SHRIEK = (buf, particle) -> {
            particle.add(Type.VAR_INT, Type.VAR_INT.readPrimitive(buf)); // Delay
        };
        public static final DataReader<Particle> COLOR = (buf, particle) -> particle.add(Type.INT, buf.readInt());
    }
}
