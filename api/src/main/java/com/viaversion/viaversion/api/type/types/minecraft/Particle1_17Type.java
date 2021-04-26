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
package us.myles.ViaVersion.api.type.types.minecraft;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.Particle;

public class Particle1_17Type extends Type<Particle> {

    public Particle1_17Type() {
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

        switch (type) {
            case 3: // Block
            case 24: // Falling dust
                particle.getArguments().add(new Particle.ParticleData(Type.VAR_INT, Type.VAR_INT.readPrimitive(buffer))); // Flat Block
                break;
            case 14: // Dust
            case 15: // Dust transition
                particle.getArguments().add(new Particle.ParticleData(Type.DOUBLE, Type.DOUBLE.readPrimitive(buffer))); // Red 0 - 1
                particle.getArguments().add(new Particle.ParticleData(Type.DOUBLE, Type.DOUBLE.readPrimitive(buffer))); // Green 0 - 1
                particle.getArguments().add(new Particle.ParticleData(Type.DOUBLE, Type.DOUBLE.readPrimitive(buffer))); // Blue 0 - 1
                particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.readPrimitive(buffer))); // Scale 0.01 - 4
                if (type == 15) {
                    // Transition to color
                    particle.getArguments().add(new Particle.ParticleData(Type.DOUBLE, Type.DOUBLE.readPrimitive(buffer))); // Red
                    particle.getArguments().add(new Particle.ParticleData(Type.DOUBLE, Type.DOUBLE.readPrimitive(buffer))); // Green
                    particle.getArguments().add(new Particle.ParticleData(Type.DOUBLE, Type.DOUBLE.readPrimitive(buffer))); // Blue
                }
                break;
            case 33: // Item
                particle.getArguments().add(new Particle.ParticleData(Type.FLAT_VAR_INT_ITEM, Type.FLAT_VAR_INT_ITEM.read(buffer))); // Flat item
                break;
            case 36: // Vibration path
                particle.getArguments().add(new Particle.ParticleData(Type.POSITION1_14, Type.POSITION1_14.read(buffer))); // From block pos
                String resourceLocation = Type.STRING.read(buffer);
                if (resourceLocation.equals("block")) {
                    particle.getArguments().add(new Particle.ParticleData(Type.POSITION1_14, Type.POSITION1_14.read(buffer))); // Target block pos
                } else if (resourceLocation.equals("entity")) {
                    particle.getArguments().add(new Particle.ParticleData(Type.VAR_INT, Type.VAR_INT.readPrimitive(buffer))); // Target entity
                } else {
                    Via.getPlatform().getLogger().warning("Unknown vibration path position source type: " + resourceLocation);
                }
                particle.getArguments().add(new Particle.ParticleData(Type.VAR_INT, Type.VAR_INT.readPrimitive(buffer))); // Arrival in ticks
        }
        return particle;
    }
}

