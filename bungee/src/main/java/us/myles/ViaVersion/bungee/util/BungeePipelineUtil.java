package us.myles.ViaVersion.bungee.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.MinecraftDecoder;
import net.md_5.bungee.protocol.MinecraftEncoder;
import us.myles.ViaVersion.Bungee;
import us.myles.ViaVersion.util.PipelineUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class BungeePipelineUtil {
    private static Method DECODE_METHOD;
    private static Method ENCODE_METHOD;

    static {
        try {
            DECODE_METHOD = MinecraftDecoder.class.getDeclaredMethod("decode", ChannelHandlerContext.class, ByteBuf.class, List.class);
            DECODE_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            System.out.println("Netty issue?");
        }
        try {
            ENCODE_METHOD = MinecraftEncoder.class.getDeclaredMethod("encode", ChannelHandlerContext.class, DefinedPacket.class, ByteBuf.class);
            ENCODE_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            System.out.println("Netty issue?");
        }
    }
    public static List<Object> callDecode(MessageToMessageDecoder decoder, ChannelHandlerContext ctx, Object input) throws InvocationTargetException {
        List<Object> output = new ArrayList<>();
        try {
            BungeePipelineUtil.DECODE_METHOD.invoke(decoder, ctx, input, output);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return output;
    }

    public static void callEncode(MessageToByteEncoder encoder, ChannelHandlerContext ctx, Object msg, ByteBuf output) throws InvocationTargetException {
        try {
            BungeePipelineUtil.ENCODE_METHOD.invoke(encoder, ctx, msg, output);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
