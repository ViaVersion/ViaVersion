package us.myles.ViaVersion.common.test.type;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.type.Type;

public class ItemTypeTest {
    @Test
    public void testEmptyItemRead() throws Exception {
        // Test empty item read
        Assertions.assertNull(Type.ITEM.read(Unpooled.wrappedBuffer(new byte[]{-1, -1})));
        Assertions.assertNull(Type.FLAT_ITEM.read(Unpooled.wrappedBuffer(new byte[]{-1, -1})));
        Assertions.assertNull(Type.FLAT_VAR_INT_ITEM.read(Unpooled.wrappedBuffer(new byte[]{0})));
    }

    @Test
    public void testNormalItemRead() throws Exception {

        // Test item read
        Assertions.assertEquals(
                new Item((int) Short.MAX_VALUE, (byte) -128, (short) 257, null),
                Type.ITEM.read(Unpooled.wrappedBuffer(new byte[]{
                        127, -1,
                        -128,
                        1, 1,
                        0
                }))
        );
        Assertions.assertEquals(
                new Item(420, (byte) 53, (short) 0, null),
                Type.FLAT_ITEM.read(Unpooled.wrappedBuffer(new byte[]{
                        1, (byte) 164,
                        53,
                        0
                }))
        );
        Assertions.assertEquals(
                new Item(268435456, (byte) 127, (short) 0, null),
                Type.FLAT_VAR_INT_ITEM.read(Unpooled.wrappedBuffer(new byte[]{
                        1,
                        -128, -128, -128, -128, 1,
                        127,
                        0
                }))
        );
    }

    @Test
    public void testEmptyItemWrite() throws Exception {
        ByteBuf buf = Unpooled.buffer();

        // Test item empty write
        Type.ITEM.write(buf, null);
        Assertions.assertArrayEquals(toBytes(buf), new byte[]{-1, -1});
        Type.FLAT_ITEM.write(buf, null);
        Assertions.assertArrayEquals(toBytes(buf), new byte[]{-1, -1});
        Type.FLAT_VAR_INT_ITEM.write(buf, null);
        Assertions.assertArrayEquals(toBytes(buf), new byte[]{0});
    }

    @Test
    public void testNormalItemWrite() throws Exception {
        ByteBuf buf = Unpooled.buffer();

        // Test item write
        Type.ITEM.write(buf, new Item((int) Short.MAX_VALUE, (byte) -128, (short) 257, null));
        Assertions.assertArrayEquals(toBytes(buf), new byte[]{
                127, -1,
                -128,
                1, 1,
                0
        });
        Type.FLAT_ITEM.write(buf, new Item(420, (byte) 53, (short) 0, null));
        Assertions.assertArrayEquals(toBytes(buf), new byte[]{
                1, (byte) 164,
                53,
                0
        });
        Type.FLAT_VAR_INT_ITEM.write(buf, new Item(268435456, (byte) 127, (short) 0, null));
        Assertions.assertArrayEquals(toBytes(buf), new byte[]{
                1,
                -128, -128, -128, -128, 1,
                127,
                0
        });
    }

    private byte[] toBytes(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        return bytes;
    }
}
