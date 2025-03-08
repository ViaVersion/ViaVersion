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
package com.viaversion.viaversion.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class PipelineUtil {
    private static final MethodHandle DECODE_METHOD = privateHandleUnchecked(ByteToMessageDecoder.class, "decode", ChannelHandlerContext.class, ByteBuf.class, List.class);
    private static final MethodHandle ENCODE_METHOD = privateHandleUnchecked(MessageToByteEncoder.class, "encode", ChannelHandlerContext.class, Object.class, ByteBuf.class);
    private static final MethodHandle MTM_DECODE = privateHandleUnchecked(MessageToMessageDecoder.class, "decode", ChannelHandlerContext.class, Object.class, List.class);

    /**
     * Calls the decode method on a netty ByteToMessageDecoder.
     *
     * @param decoder decoder
     * @param ctx     current context
     * @param input   packet to decode
     * @return a list of the decoders output
     * @throws Exception                 propagated exception from the underlying method if thrown
     * @throws InvocationTargetException wrapped throwable if a given throwable is not an exception or error
     */
    public static List<Object> callDecode(ByteToMessageDecoder decoder, ChannelHandlerContext ctx, Object input) throws Exception {
        List<Object> output = new ArrayList<>();
        try {
            PipelineUtil.DECODE_METHOD.invoke(decoder, ctx, input, output);
        } catch (Exception | Error e) {
            throw e; // Directly propagate exceptions/errors
        } catch (Throwable t) {
            throw new InvocationTargetException(t);
        }
        return output;
    }

    /**
     * Calls the encode method on a netty MessageToByteEncoder.
     *
     * @param encoder encoder
     * @param ctx     current context
     * @param msg     packet to encode
     * @throws Exception                 propagated exception from the underlying method if thrown
     * @throws InvocationTargetException wrapped throwable if a given throwable is not an exception or error
     */
    public static void callEncode(MessageToByteEncoder encoder, ChannelHandlerContext ctx, Object msg, ByteBuf output) throws Exception {
        try {
            PipelineUtil.ENCODE_METHOD.invoke(encoder, ctx, msg, output);
        } catch (Exception | Error e) {
            throw e; // Directly propagate exceptions/errors
        } catch (Throwable t) {
            throw new InvocationTargetException(t);
        }
    }

    /**
     * Calls the decode method on a netty MessageToMessageDecoder.
     *
     * @param decoder decoder
     * @param ctx     current context
     * @param msg     packet to decode
     * @return a list of the decoders output
     * @throws Exception                 propagated exception from the underlying method if thrown
     * @throws InvocationTargetException wrapped throwable if a given throwable is not an exception or error
     */
    public static List<Object> callDecode(MessageToMessageDecoder decoder, ChannelHandlerContext ctx, Object msg) throws Exception {
        List<Object> output = new ArrayList<>();
        try {
            MTM_DECODE.invoke(decoder, ctx, msg, output);
        } catch (Exception | Error e) {
            throw e;
        } catch (Throwable t) {
            throw new InvocationTargetException(t);
        }
        return output;
    }

    /**
     * Check if a stack trace contains a certain exception
     *
     * @param t The throwable
     * @param c The exception to look for
     * @return True if the stack trace contained it as its cause or if t is an instance of c.
     */
    public static boolean containsCause(Throwable t, Class<?> c) {
        do {
            if (c.isAssignableFrom(t.getClass())) {
                return true;
            }

            t = t.getCause();
        } while (t != null);
        return false;
    }

    /**
     * Check if a stack trace contains a certain exception and returns it if present.
     *
     * @param t throwable
     * @param c the exception to look for
     * @return contained exception of t if present
     */
    public static <T> @Nullable T getCause(Throwable t, Class<T> c) {
        while (t != null) {
            if (c.isAssignableFrom(t.getClass())) {
                //noinspection unchecked
                return (T) t;
            }

            t = t.getCause();
        }
        return null;
    }

    /**
     * Get the context for the channel handler before a certain name.
     *
     * @param name     The name of the channel handler
     * @param pipeline The pipeline to target
     * @return The ChannelHandler before the one requested.
     */
    public static @Nullable ChannelHandlerContext getContextBefore(String name, ChannelPipeline pipeline) {
        boolean mark = false;
        for (String s : pipeline.names()) {
            if (mark) {
                return pipeline.context(pipeline.get(s));
            }
            if (s.equalsIgnoreCase(name)) {
                mark = true;
            }
        }
        return null;
    }

    public static @Nullable ChannelHandlerContext getPreviousContext(String name, ChannelPipeline pipeline) {
        String previous = null;
        for (String entry : pipeline.toMap().keySet()) {
            if (entry.equals(name)) {
                return pipeline.context(previous);
            }
            previous = entry;
        }
        return null;
    }

    private static MethodHandle privateHandle(final Class<?> clazz, final String method, final Class<?>... parameterTypes) throws NoSuchMethodException, IllegalAccessException {
        final Method decodeMethod = clazz.getDeclaredMethod(method, parameterTypes);
        decodeMethod.setAccessible(true);
        return MethodHandles.lookup().unreflect(decodeMethod);
    }

    private static MethodHandle privateHandleUnchecked(final Class<?> clazz, final String method, final Class<?>... args) {
        try {
            return privateHandle(clazz, method, args);
        } catch (final NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
