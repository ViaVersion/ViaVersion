package us.myles.ViaVersion.api.type.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.type.PartialType;

public class CustomByteType extends PartialType<byte[], Integer> {

    public CustomByteType(Integer param) {
        super(param, byte[].class);
    }

    @Override
    public byte[] read(ByteBuf byteBuf, Integer integer) throws Exception {
        if (byteBuf.readableBytes() < integer) throw new RuntimeException("Readable bytes does not match expected!");

        byte[] byteArray = new byte[integer];
        byteBuf.readBytes(byteArray);

        return byteArray;
    }

    @Override
    public void write(ByteBuf byteBuf, Integer integer, byte[] bytes) throws Exception {
        byteBuf.writeBytes(bytes);
    }
}