/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.Particle;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public abstract class AbstractParticleType extends Type<Particle> {

    protected final Int2ObjectMap<ParticleReader> readers = new Int2ObjectOpenHashMap<>();

    protected AbstractParticleType() {
        super("Particle", Particle.class);
    }

    @Override
    public void write(ByteBuf buffer, Particle object) throws Exception {
        Type.VAR_INT.writePrimitive(buffer, object.getId());
        for (Particle.ParticleData data : object.getArguments()) {
            data.getType().write(buffer, data.getValue());
        }
    }

    @Override
    public Particle read(ByteBuf buffer) throws Exception {
        int type = Type.VAR_INT.readPrimitive(buffer);
        Particle particle = new Particle(type);

        ParticleReader reader = readers.get(type);
        if (reader != null) {
            reader.read(buffer, particle);
        }
        return particle;
    }

    protected ParticleReader blockHandler() {
        return (buf, particle) -> {
            particle.getArguments().add(new Particle.ParticleData(Type.VAR_INT, Type.VAR_INT.readPrimitive(buf))); // Flat Block
        };
    }

    protected ParticleReader itemHandler(Type<Item> itemType) {
        return (buf, particle) -> {
            particle.getArguments().add(new Particle.ParticleData(itemType, itemType.read(buf))); // Flat item;
        };
    }

    protected ParticleReader dustHandler() {
        return (buf, particle) -> {
            particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.readPrimitive(buf))); // Red 0 - 1
            particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.readPrimitive(buf))); // Green 0 - 1
            particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.readPrimitive(buf))); // Blue 0 - 1
            particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.readPrimitive(buf))); // Scale 0.01 - 4
        };
    }

    protected ParticleReader dustTransitionHandler() {
        return (buf, particle) -> {
            particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.readPrimitive(buf))); // Red 0 - 1
            particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.readPrimitive(buf))); // Green 0 - 1
            particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.readPrimitive(buf))); // Blue 0 - 1
            particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.readPrimitive(buf))); // Scale 0.01 - 4
            particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.readPrimitive(buf))); // Red
            particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.readPrimitive(buf))); // Green
            particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.readPrimitive(buf))); // Blue
        };
    }

    protected ParticleReader vibrationHandler(Type<Position> positionType) {
        return (buf, particle) -> {
            particle.getArguments().add(new Particle.ParticleData(positionType, positionType.read(buf))); // From block pos
            String resourceLocation = Type.STRING.read(buf);
            if (resourceLocation.equals("block")) {
                particle.getArguments().add(new Particle.ParticleData(positionType, positionType.read(buf))); // Target block pos
            } else if (resourceLocation.equals("entity")) {
                particle.getArguments().add(new Particle.ParticleData(Type.VAR_INT, Type.VAR_INT.readPrimitive(buf))); // Target entity
            } else {
                Via.getPlatform().getLogger().warning("Unknown vibration path position source type: " + resourceLocation);
            }
            particle.getArguments().add(new Particle.ParticleData(Type.VAR_INT, Type.VAR_INT.readPrimitive(buf))); // Arrival in ticks
        };
    }

    @FunctionalInterface
    public interface ParticleReader {

        void read(ByteBuf buf, Particle particle) throws Exception;
    }
}
