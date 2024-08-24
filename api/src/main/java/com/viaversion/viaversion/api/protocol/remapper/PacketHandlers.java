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
package com.viaversion.viaversion.api.protocol.remapper;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.exception.InformativeException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;

public abstract class PacketHandlers implements PacketHandler {
    private final List<PacketHandler> packetHandlers = new ArrayList<>();

    protected PacketHandlers() {
        register();
    }

    static PacketHandler fromRemapper(List<PacketHandler> valueRemappers) {
        final PacketHandlers handlers = new PacketHandlers() {
            @Override
            public void register() {
            }
        };
        handlers.packetHandlers.addAll(valueRemappers);
        return handlers;
    }

    /**
     * Reads and writes the given type.
     *
     * @param type type to map
     */
    public <T> void map(Type<T> type) {
        handler(wrapper -> wrapper.passthrough(type));
    }

    /**
     * Reads the first given type and writes the second given type.
     *
     * @param oldType old type
     * @param newType new type
     */
    public void map(Type<?> oldType, Type<?> newType) {
        handler(wrapper -> wrapper.passthroughAndMap(oldType, newType));
    }

    /**
     * Maps a type from an old type to a transformed new type.
     *
     * @param <T1>        old value type
     * @param <T2>        new value type
     * @param oldType     old type
     * @param newType     new type
     * @param transformer transformer to produce the new type
     */
    public <T1, T2> void map(Type<T1> oldType, Type<T2> newType, Function<T1, T2> transformer) {
        map(oldType, new ValueTransformer<>(newType) {
            @Override
            public T2 transform(PacketWrapper wrapper, T1 inputValue) {
                return transformer.apply(inputValue);
            }
        });
    }

    /**
     * Maps a type from an old type to a transformed new type based on their input type.
     *
     * @param <T1>        old value type
     * @param <T2>        new value type
     * @param transformer transformer to produce the new type
     */
    public <T1, T2> void map(ValueTransformer<T1, T2> transformer) {
        if (transformer.getInputType() == null) {
            throw new IllegalArgumentException("Use map(Type<T1>, ValueTransformer<T1, T2>) for value transformers without specified input type!");
        }
        map(transformer.getInputType(), transformer);
    }

    /**
     * Maps a type from an old type to a transformed new type.
     *
     * @param <T1>        old value type
     * @param <T2>        new value type
     * @param oldType     old type
     * @param transformer transformer to produce the new type
     */
    public <T1, T2> void map(Type<T1> oldType, ValueTransformer<T1, T2> transformer) {
        map(new TypeRemapper<>(oldType), transformer);
    }

    /**
     * Maps a type using a basic ValueReader to a ValueWriter.
     *
     * @param inputReader  reader to read with
     * @param outputWriter writer to write with
     * @param <T>          read/write type
     */
    public <T> void map(ValueReader<T> inputReader, ValueWriter<T> outputWriter) {
        handler(wrapper -> outputWriter.write(wrapper, inputReader.read(wrapper)));
    }

    /**
     * Adds a packet handler.
     *
     * @param handler packet handler
     */
    public void handler(PacketHandler handler) {
        packetHandlers.add(handler);
    }

    /**
     * Adds a packet handler which will suppress any exceptions thrown by the handler.
     *
     * @param handler packet handler
     */
    public void handlerSoftFail(PacketHandler handler) {
        packetHandlers.add(h -> {
            try {
                handler.handle(h);
            } catch (Exception e) {
                if (!Via.getConfig().isSuppressConversionWarnings()) {
                    Via.getPlatform().getLogger().log(Level.WARNING, "Failed to handle packet", e);
                }
                h.cancel();
            }
        });
    }

    /**
     * Writes a value.
     *
     * @param type  type to write
     * @param value value to write
     */
    public <T> void create(Type<T> type, T value) {
        handler(wrapper -> wrapper.write(type, value));
    }

    /**
     * Reads (and thus removes) the given type.
     *
     * @param type type to read
     */
    public void read(Type<?> type) {
        handler(wrapper -> wrapper.read(type));
    }

    /**
     * Registers the handlers for this packet.
     */
    protected abstract void register();

    @Override
    public final void handle(PacketWrapper wrapper) throws InformativeException {
        for (PacketHandler handler : packetHandlers) {
            handler.handle(wrapper);
        }
    }

    public int handlersSize() {
        return packetHandlers.size();
    }
}
