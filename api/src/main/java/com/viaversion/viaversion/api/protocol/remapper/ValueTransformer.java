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

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.exception.InformativeException;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class ValueTransformer<T1, T2> implements ValueWriter<T1> {
    private final Type<T1> inputType;
    private final Type<T2> outputType;

    protected ValueTransformer(@Nullable Type<T1> inputType, Type<T2> outputType) {
        this.inputType = inputType;
        this.outputType = outputType;
    }

    protected ValueTransformer(Type<T2> outputType) {
        this(null, outputType);
    }

    /**
     * Transform a value from one type to another
     *
     * @param wrapper    The current packet
     * @param inputValue The input value
     * @return The value to write to the wrapper
     * @throws Exception Throws exception if it fails to transform a value
     */
    public abstract T2 transform(PacketWrapper wrapper, T1 inputValue) throws Exception;

    @Override
    public void write(PacketWrapper writer, T1 inputValue) throws Exception {
        try {
            writer.write(outputType, transform(writer, inputValue));
        } catch (InformativeException e) {
            e.addSource(this.getClass());
            throw e;
        }
    }

    public @Nullable Type<T1> getInputType() {
        return inputType;
    }

    public Type<T2> getOutputType() {
        return outputType;
    }
}
