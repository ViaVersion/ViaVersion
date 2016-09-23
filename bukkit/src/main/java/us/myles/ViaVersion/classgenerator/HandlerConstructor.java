package us.myles.ViaVersion.classgenerator;

import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import us.myles.ViaVersion.api.data.UserConnection;

public interface HandlerConstructor {
    public MessageToByteEncoder newEncodeHandler(UserConnection info, MessageToByteEncoder minecraftEncoder);

    public ByteToMessageDecoder newDecodeHandler(UserConnection info, ByteToMessageDecoder minecraftDecoder);
}
