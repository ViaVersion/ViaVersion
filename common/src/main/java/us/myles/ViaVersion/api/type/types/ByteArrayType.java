package us.myles.ViaVersion.api.type.types;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.type.Type;

public class ByteArrayType extends Type<byte[]> {
    public ByteArrayType() {
        super(byte[].class);
    }

    @Override
    public void write(ByteBuf buffer, byte[] object) throws Exception {
        Type.VAR_INT.write(buffer, object.length);
        buffer.writeBytes(object);
    }

    @Override
    public byte[] read(ByteBuf buffer) throws Exception {
        int length = Type.VAR_INT.read(buffer);
        Preconditions.checkArgument(buffer.isReadable(length), "Length is fewer than readable bytes");
        byte[] array = new byte[length];
        buffer.readBytes(array);
        return array;
    }
}
