package us.myles.ViaVersion.util;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import org.spacehq.opennbt.NBTIO;
import org.spacehq.opennbt.tag.builtin.CompoundTag;
import us.myles.ViaVersion.chunks.MagicBitSet;
import us.myles.ViaVersion.chunks.PacketChunk;
import us.myles.ViaVersion.chunks.PacketChunkData;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;

public class PacketUtil {
    private static Method DECODE_METHOD;
    private static Method ENCODE_METHOD;

    static {
        try {
            DECODE_METHOD = ByteToMessageDecoder.class.getDeclaredMethod("decode", ChannelHandlerContext.class, ByteBuf.class, List.class);
            DECODE_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            System.out.println("Netty issue?");
        }
        try {
            ENCODE_METHOD = MessageToByteEncoder.class.getDeclaredMethod("encode", ChannelHandlerContext.class, Object.class, ByteBuf.class);
            ENCODE_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            System.out.println("Netty issue?");
        }
    }

    public static CompoundTag readNBT(ByteBuf input) throws IOException {
        int readerIndex = input.readerIndex();
        byte b = input.readByte();
        if (b == 0) {
            return null;
        } else {
            input.readerIndex(readerIndex);
            return (CompoundTag) NBTIO.readTag(new DataInputStream(new ByteBufInputStream(input)));
        }
    }

    public static void writeNBT(ByteBuf output, CompoundTag tag) throws IOException {
        if (tag == null) {
            output.writeByte(0);
        } else {
            NBTIO.writeTag(new DataOutputStream(new ByteBufOutputStream(output)), tag);
        }
    }

    public static List<Object> callDecode(ByteToMessageDecoder decoder, ChannelHandlerContext ctx, Object input) {
        List<Object> output = new ArrayList<Object>();
        try {
            PacketUtil.DECODE_METHOD.invoke(decoder, ctx, input, output);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return output;
    }

    public static void callEncode(MessageToByteEncoder encoder, ChannelHandlerContext ctx, Object msg, ByteBuf output) {
        try {
            PacketUtil.ENCODE_METHOD.invoke(encoder, ctx, msg, output);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static ByteBuf decompress(ChannelHandlerContext ctx, ByteBuf msg) {
        ByteToMessageDecoder x = (ByteToMessageDecoder) ctx.pipeline().get("decompress");
        List<Object> output = callDecode(x, ctx, msg);
        return output.size() == 0 ? null : (ByteBuf) output.get(0);
    }

    public static ByteBuf compress(ChannelHandlerContext ctx, ByteBuf msg) {
        MessageToByteEncoder x = (MessageToByteEncoder) ctx.pipeline().get("compress");
        ByteBuf output = ctx.alloc().buffer();
        callEncode(x, ctx, msg, output);
        return output;
    }

    /* I take no credit, these are taken from BungeeCord */
    // https://github.com/SpigotMC/BungeeCord/blob/master/protocol/src/main/java/net/md_5/bungee/protocol/DefinedPacket.java
    public static void writeString(String s, ByteBuf buf) {
        Preconditions.checkArgument(s.length() <= Short.MAX_VALUE, "Cannot send string longer than Short.MAX_VALUE (got %s characters)", s.length());

        byte[] b = s.getBytes(Charsets.UTF_8);
        writeVarInt(b.length, buf);
        buf.writeBytes(b);
    }

    public static String readString(ByteBuf buf) {
        int len = readVarInt(buf);
        Preconditions.checkArgument(len <= Short.MAX_VALUE, "Cannot receive string longer than Short.MAX_VALUE (got %s characters)", len);

        byte[] b = new byte[len];
        buf.readBytes(b);

        return new String(b, Charsets.UTF_8);
    }

    public static void writeArrayLegacy(byte[] b, ByteBuf buf, boolean allowExtended) {
        // (Integer.MAX_VALUE & 0x1FFF9A ) = 2097050 - Forge's current upper limit
        if (allowExtended) {
            Preconditions.checkArgument(b.length <= (Integer.MAX_VALUE & 0x1FFF9A), "Cannot send array longer than 2097050 (got %s bytes)", b.length);
        } else {
            Preconditions.checkArgument(b.length <= Short.MAX_VALUE, "Cannot send array longer than Short.MAX_VALUE (got %s bytes)", b.length);
        }
        // Write a 2 or 3 byte number that represents the length of the packet. (3 byte "shorts" for Forge only)
        // No vanilla packet should give a 3 byte packet, this method will still retain vanilla behaviour.
        writeVarShort(buf, b.length);
        buf.writeBytes(b);
    }

    public static byte[] readArrayLegacy(ByteBuf buf) {
        // Read in a 2 or 3 byte number that represents the length of the packet. (3 byte "shorts" for Forge only)
        // No vanilla packet should give a 3 byte packet, this method will still retain vanilla behaviour.
        int len = readVarShort(buf);

        // (Integer.MAX_VALUE & 0x1FFF9A ) = 2097050 - Forge's current upper limit
        Preconditions.checkArgument(len <= (Integer.MAX_VALUE & 0x1FFF9A), "Cannot receive array longer than 2097050 (got %s bytes)", len);

        byte[] ret = new byte[len];
        buf.readBytes(ret);
        return ret;
    }

    public static void writeArray(byte[] b, ByteBuf buf) {
        writeVarInt(b.length, buf);
        buf.writeBytes(b);
    }

    public static byte[] readArray(ByteBuf buf) {
        byte[] ret = new byte[readVarInt(buf)];
        buf.readBytes(ret);
        return ret;
    }

    public static void writeStringArray(List<String> s, ByteBuf buf) {
        writeVarInt(s.size(), buf);
        for (String str : s) {
            writeString(str, buf);
        }
    }

    public static List<String> readStringArray(ByteBuf buf) {
        int len = readVarInt(buf);
        List<String> ret = new ArrayList<String>(len);
        for (int i = 0; i < len; i++) {
            ret.add(readString(buf));
        }
        return ret;
    }

    public static int readVarInt(ByteBuf input) {
        return readVarInt(input, 5);
    }

    public static int readVarInt(ByteBuf input, int maxBytes) {
        int out = 0;
        int bytes = 0;
        byte in;
        while (true) {
            in = input.readByte();

            out |= (in & 0x7F) << (bytes++ * 7);

            if (bytes > maxBytes) {
                throw new RuntimeException("VarInt too big");
            }

            if ((in & 0x80) != 0x80) {
                break;
            }
        }

        return out;
    }

    public static void writeVarInt(int value, ByteBuf output) {
        int part;
        while (true) {
            part = value & 0x7F;

            value >>>= 7;
            if (value != 0) {
                part |= 0x80;
            }

            output.writeByte(part);

            if (value == 0) {
                break;
            }
        }
    }

    public static void writeVarIntArray(List<Integer> integers, ByteBuf output) {
        writeVarInt(integers.size(), output);
        for (Integer i : integers) {
            writeVarInt(i, output);
        }
    }

    public static int readVarShort(ByteBuf buf) {
        int low = buf.readUnsignedShort();
        int high = 0;
        if ((low & 0x8000) != 0) {
            low = low & 0x7FFF;
            high = buf.readUnsignedByte();
        }
        return ((high & 0xFF) << 15) | low;
    }

    public static void writeVarShort(ByteBuf buf, int toWrite) {
        int low = toWrite & 0x7FFF;
        int high = (toWrite & 0x7F8000) >> 15;
        if (high != 0) {
            low = low | 0x8000;
        }
        buf.writeShort(low);
        if (high != 0) {
            buf.writeByte(high);
        }
    }

    public static void writeUUID(UUID value, ByteBuf output) {
        output.writeLong(value.getMostSignificantBits());
        output.writeLong(value.getLeastSignificantBits());
    }

    public static UUID readUUID(ByteBuf input) {
        return new UUID(input.readLong(), input.readLong());
    }

    public static void writeLongs(long[] data, ByteBuf output) {
        for (int index = 0; index < data.length; index++) {
            output.writeLong(data[index]);
        }
    }

    public static long[] readLongs(int amount, ByteBuf output) {
        long data[] = new long[amount];
        for (int index = 0; index < amount; index++) {
            data[index] = output.readLong();
        }

        return data;
    }

    // This method is based on one from here
    // https://github.com/Steveice10/MCProtocolLib/blob/master/src/main/java/org/spacehq/mc/protocol/util/NetUtil.java
    public static PacketChunk readChunkData(boolean isFullChunk, int bitmask, byte[] input) {
        PacketChunkData[] chunks = new PacketChunkData[16];
        boolean sky = false;
        int expected = 0;
        int position = 0;
        ShortBuffer blockData = ByteBuffer.wrap(input).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();

        for (int i = 0; i < chunks.length; i++) {
            if ((bitmask & 1 << i) != 0) {
                expected += (4096 * 2) + 2048;
            }
        }
        // If there is more data, then there must be skylights :D
        if (input.length > expected) {
            sky = true;
        }

        // Read block data
        for (int i = 0; i < chunks.length; i++) {
            if ((bitmask & 1 << i) != 0) {
                chunks[i] = new PacketChunkData(sky);
                blockData.position(position / 2);
                blockData.get(chunks[i].getBlocks(), 0, 4096);
                position = position + (4096 * 2);
            } else {
                chunks[i] = new PacketChunkData(sky);
            }
        }
        // Read blocklight data
        for (int i = 0; i < chunks.length; i++) {
            if ((bitmask & 1 << i) != 0) {
                System.arraycopy(input, position, chunks[i].getBlockLight(), 0, 2048);
                position = position + 2048;
            }
        }
        // Read skylight data
        if (sky) {
            for (int i = 0; i < chunks.length; i++) {
                if ((bitmask & 1 << i) != 0) {
                    System.arraycopy(input, position, chunks[i].getSkyLight(), 0, 2048);
                    position = position + 2048;
                }
            }
        }

        byte[] biomeData = null;

        if (isFullChunk) {
            // yay im a full chunk meaning i know my biomes, mommy get the camera!
            biomeData = new byte[256];
            System.arraycopy(input, position, biomeData, 0, 256);
        }
        return new PacketChunk(chunks, biomeData);
    }

    public static void writeNewChunk(ByteBuf buffer, PacketChunkData chunk) {
        // Bits Per Block (We use 0, cause we're not gonna write a palette ;) )
        buffer.writeByte(0);
        // No Palette nor length :D

        // Data Array Length
        byte[] blockData = convertBlockArray(chunk.getBlocks());
        writeVarInt(blockData.length / 8, buffer); // Notchian is divide by 8

        buffer.writeBytes(blockData);
        // Block Light
        buffer.writeBytes(chunk.getBlockLight());
        // If has skylight, write it
        if (chunk.getSkyLight() != null) {
            buffer.writeBytes(chunk.getSkyLight());
        }
    }

    private static byte[] convertBlockArray(short[] blocks) {
        //  block ID for the first 9 bits, and the block damage value for the last 4 bits
        byte[] output = new byte[6664]; // (16 * 16 * 16 * 13) / 8 :) (plus some for padding ...)
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (short block : blocks) {
            int blockID = block >> 4; // (Needs to be to 9 bits)
            int data = block & 0xF; // 8 bits

        }
        return null; // todo: finish
    }

    private static BitSet append(BitSet base, int index, MagicBitSet toAdd) {
        int length = index;
        for (int i = 0; i < toAdd.getTrueLength(); i++) {
            base.set(length + i, toAdd.get(i));
        }
        return base;
    }

    private static MagicBitSet getPaddedBitSet(int value, int bitSize) {
        MagicBitSet output = new MagicBitSet(bitSize);
        BitSet temp = BitSet.valueOf(new long[]{value});
        for (int i = 0; i < bitSize; i++) {
            output.set(i, false);
        }
        int toShift = bitSize - temp.length();
        for (int i = 0; i < temp.length(); i++) {
            output.set(toShift + i, temp.get(i));
        }
        return output;
    }

    public static long[] readBlockPosition(ByteBuf buf) {
        long val = buf.readLong();
        long x = (val >> 38); // signed
        long y = (val >> 26) & 0xfff; // unsigned
        // this shifting madness is used to preserve sign
        long z = (val << 38) >> 38; // signed
        return new long[]{x, y, z};
    }

    public static void writeBlockPosition(ByteBuf buf, long x, long y, long z) {
        buf.writeLong(((x & 0x3ffffff) << 38) | ((y & 0xfff) << 26) | (z & 0x3ffffff));
    }

    public static int[] readVarInts(int amount, ByteBuf input) {
        int data[] = new int[amount];
        for (int index = 0; index < amount; index++) {
            data[index] = PacketUtil.readVarInt(input);
        }

        return data;
    }

    public static boolean containsCause(Throwable t, Class<? extends Throwable> c) {
        while (t != null) {
            t = t.getCause();
            if (t != null)
                if (c.isAssignableFrom(t.getClass())) return true;
        }
        return false;
    }
}
