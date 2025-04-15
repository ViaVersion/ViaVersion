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
package com.viaversion.viaversion.api.minecraft;

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.IdHolder;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;

public final class Particle implements IdHolder, Copyable {
    private final List<ParticleData<?>> arguments = new ArrayList<>(4);
    private int id;

    public Particle(final int id) {
        this.id = id;
    }

    @Override
    public int id() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public <T> ParticleData<T> getArgument(final int index) {
        //noinspection unchecked
        return (ParticleData<T>) arguments.get(index);
    }

    public <T> ParticleData<T> removeArgument(final int index) {
        //noinspection unchecked
        return (ParticleData<T>) arguments.remove(index);
    }

    public List<ParticleData<?>> getArguments() {
        return arguments;
    }

    public <T> void add(final Type<T> type, final T value) {
        arguments.add(new ParticleData<>(type, value));
    }

    public <T> void add(final int index, final Type<T> type, final T value) {
        arguments.add(index, new ParticleData<>(type, value));
    }

    public <T> void set(final int index, final Type<T> type, final T value) {
        arguments.set(index, new ParticleData<>(type, value));
    }

    @Override
    public Particle copy() {
        final Particle particle = new Particle(id);
        for (ParticleData<?> argument : arguments) {
            particle.arguments.add(argument.copy());
        }
        return particle;
    }

    @Override
    public String toString() {
        return "Particle{" +
            "arguments=" + arguments +
            ", id=" + id +
            '}';
    }

    public static final class ParticleData<T> implements Copyable {
        private final Type<T> type;
        private T value;

        public ParticleData(final Type<T> type, final T value) {
            this.type = type;
            this.value = value;
        }

        public Type<T> getType() {
            return type;
        }

        public T getValue() {
            return value;
        }

        public void setValue(final T value) {
            this.value = value;
        }

        public void write(final ByteBuf buf) {
            type.write(buf, value);
        }

        public void write(final PacketWrapper wrapper) {
            wrapper.write(type, value);
        }

        @Override
        public ParticleData<T> copy() {
            return new ParticleData<>(type, Copyable.copy(value));
        }

        @Override
        public String toString() {
            return "ParticleData{" +
                "type=" + type +
                ", value=" + value +
                '}';
        }
    }
}
