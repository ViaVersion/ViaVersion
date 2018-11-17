package us.myles.ViaVersion.common.test.type;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import us.myles.ViaVersion.api.type.Type;

public class StringTypeTest {
    @Test
    public void testStringWrite() throws Exception {
        // Write
        final ByteBuf buf = Unpooled.buffer();
        Type.STRING.write(buf, "\uD83E\uDDFD"); // Sponge Emoji
        Assertions.assertEquals(ByteBufUtil.hexDump(buf), "04f09fa7bd");
    }

    @Test
    public void testStringRead() throws Exception {
        // Write
        final ByteBuf buf = Unpooled.buffer();
        Type.STRING.write(buf, new String(new char[Short.MAX_VALUE]));
        Assertions.assertEquals(Type.STRING.read(buf), new String(new char[Short.MAX_VALUE]));

        Type.STRING.write(buf, new String(new char[Short.MAX_VALUE]).replace("\0", "ç"));
        Assertions.assertEquals(Type.STRING.read(buf), new String(new char[Short.MAX_VALUE]).replace("\0", "ç"));

        Type.STRING.write(buf, new String(new char[Short.MAX_VALUE / 2]).replace("\0", "\uD83E\uDDFD"));
        Assertions.assertEquals(Type.STRING.read(buf), new String(new char[Short.MAX_VALUE / 2]).replace("\0", "\uD83E\uDDFD"));
    }

    @Test
    public void testStringReadOverflowException() throws Exception {
        // Read exception
        final ByteBuf buf = Unpooled.buffer();
        Type.VAR_INT.write(buf, (Short.MAX_VALUE + 1) * 4);
        for (int i = 0; i < Short.MAX_VALUE / 2 + 1; i++) {
            buf.writeBytes(new byte[]{0x04, (byte) 0xf0, (byte) 0x9f, (byte) 0xa7, (byte) 0xbd}); // Sponge emoji
        }
        Assertions.assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                Type.STRING.read(buf);
            }
        });
    }

    @Test
    public void testStringWriteOverflowException() {
        // Write exceptions
        final ByteBuf buf = Unpooled.buffer();
        Assertions.assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                Type.STRING.write(buf, new String(new char[Short.MAX_VALUE / 2 + 1]).replace("\0", "\uD83E\uDDFD"));
            }
        });

        Assertions.assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                Type.STRING.write(buf, new String(new char[Short.MAX_VALUE + 1]));
            }
        });
    }
}
