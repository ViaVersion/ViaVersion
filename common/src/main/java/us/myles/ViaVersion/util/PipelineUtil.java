package us.myles.ViaVersion.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class PipelineUtil {
    private static Method DECODE_METHOD;
    private static Method ENCODE_METHOD;

    static {
        try {
            DECODE_METHOD = ByteToMessageDecoder.class.getDeclaredMethod("decode", ChannelHandlerContext.class, ByteBuf.class, List.class);
            DECODE_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        try {
            ENCODE_METHOD = MessageToByteEncoder.class.getDeclaredMethod("encode", ChannelHandlerContext.class, Object.class, ByteBuf.class);
            ENCODE_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * Call the decode method on a netty ByteToMessageDecoder
     *
     * @param decoder The decoder
     * @param ctx     The current context
     * @param input   The packet to decode
     * @return A list of the decoders output
     * @throws InvocationTargetException If an exception happens while executing
     */
    public static List<Object> callDecode(ByteToMessageDecoder decoder, ChannelHandlerContext ctx, Object input) throws InvocationTargetException {
        List<Object> output = new ArrayList<>();
        try {
            PipelineUtil.DECODE_METHOD.invoke(decoder, ctx, input, output);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return output;
    }

    /**
     * Call the encode method on a netty MessageToByteEncoder
     *
     * @param encoder The encoder
     * @param ctx     The current context
     * @param msg     The packet to encode
     * @param output  The bytebuf to write the output to
     * @throws InvocationTargetException If an exception happens while executing
     */
    public static void callEncode(MessageToByteEncoder encoder, ChannelHandlerContext ctx, Object msg, ByteBuf output) throws InvocationTargetException {
        try {
            PipelineUtil.ENCODE_METHOD.invoke(encoder, ctx, msg, output);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if a stack trace contains a certain exception
     *
     * @param t The throwable
     * @param c The exception to look for
     * @return True if the stack trace contained it as its cause.
     */
    public static boolean containsCause(Throwable t, Class<? extends Throwable> c) {
        while (t != null) {
            t = t.getCause();
            if (t != null)
                if (c.isAssignableFrom(t.getClass())) return true;
        }
        return false;
    }

    /**
     * Get the context for a the channel handler before a certain name.
     *
     * @param name     The name of the channel handler
     * @param pipeline The pipeline to target
     * @return The ChannelHandler before the one requested.
     */
    public static ChannelHandlerContext getContextBefore(String name, ChannelPipeline pipeline) {
        boolean mark = false;
        for (String s : pipeline.names()) {
            if (mark) {
                return pipeline.context(pipeline.get(s));
            }
            if (s.equalsIgnoreCase(name))
                mark = true;
        }
        return null;
    }

    public static ChannelHandlerContext getPreviousContext(String name, ChannelPipeline pipeline) {
        String previous = null;
        for (String entry : pipeline.toMap().keySet()) {
            if (entry.equals(name)) {
                return pipeline.context(previous);
            }
            previous = entry;
        }
        return null;
    }
}
