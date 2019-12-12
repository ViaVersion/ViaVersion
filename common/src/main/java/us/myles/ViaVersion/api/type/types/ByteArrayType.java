package us.myles.ViaVersion.api.type.types;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.type.Type;

public class ByteArrayType extends Type<byte[]> {
    public ByteArrayType() {
        super("Byte Array", byte[].class);
    }

    @Override
    public byte[] read(ByteBuf buffer) throws Exception {
        int len = Type.VAR_INT.read(buffer);

        Preconditions.checkArgument(buffer.isReadable(len), "Could not read %s bytes from buffer (have %s)", len, buffer.readableBytes());
        byte[] data = new byte[len];
        buffer.readBytes(data);
        return data;
    }

    @Override
    public void write(ByteBuf buffer, byte[] object) throws Exception {
        Type.VAR_INT.write(buffer, object.length);
        buffer.writeBytes(object);
    }
}
