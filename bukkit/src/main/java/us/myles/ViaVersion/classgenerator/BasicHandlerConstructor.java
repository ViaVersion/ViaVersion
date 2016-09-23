package us.myles.ViaVersion.classgenerator;

import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.handlers.ViaDecodeHandler;
import us.myles.ViaVersion.handlers.ViaEncodeHandler;

public class BasicHandlerConstructor implements HandlerConstructor {
    @Override
    public ViaEncodeHandler newEncodeHandler(UserConnection info, MessageToByteEncoder minecraftEncoder) {
        return new ViaEncodeHandler(info, minecraftEncoder);
    }

    @Override
    public ViaDecodeHandler newDecodeHandler(UserConnection info, ByteToMessageDecoder minecraftDecoder) {
        return new ViaDecodeHandler(info, minecraftDecoder);
    }
}
