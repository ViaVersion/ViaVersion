package us.myles.ViaVersion.api.type.types;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.type.Type;

public class StringType extends Type<String> {
    // String#length() (used to limit the string in Minecraft source code) uses char[]#length
    private static final int maxJavaCharUtf8Length = Character.toString(Character.MAX_VALUE)
            .getBytes(Charsets.UTF_8).length;

    public StringType() {
        super(String.class);
    }

    @Override
    public String read(ByteBuf buffer) throws Exception {
        int len = Type.VAR_INT.read(buffer);

        Preconditions.checkArgument(len <= Short.MAX_VALUE * maxJavaCharUtf8Length,
                "Cannot receive string longer than Short.MAX_VALUE * "  + maxJavaCharUtf8Length + " bytes (got %s bytes)", len);

        byte[] b = new byte[len];
        buffer.readBytes(b);
        String string = new String(b, Charsets.UTF_8);
        Preconditions.checkArgument(string.length() <= Short.MAX_VALUE,
                "Cannot receive string longer than Short.MAX_VALUE characters (got %s bytes)", string.length());

        return string;
    }

    @Override
    public void write(ByteBuf buffer, String object) throws Exception {
        Preconditions.checkArgument(object.length() <= Short.MAX_VALUE, "Cannot send string longer than Short.MAX_VALUE (got %s characters)", object.length());

        byte[] b = object.getBytes(Charsets.UTF_8);
        Type.VAR_INT.write(buffer, b.length);
        buffer.writeBytes(b);
    }
}
