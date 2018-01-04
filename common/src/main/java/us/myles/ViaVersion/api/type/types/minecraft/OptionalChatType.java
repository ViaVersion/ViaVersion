package us.myles.ViaVersion.api.type.types.minecraft;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.type.Type;

public class OptionalChatType extends Type<String> {
    public OptionalChatType() {
        super(String.class);
    }

    @Override
    public String read(ByteBuf buffer) throws Exception {
        boolean present = buffer.readBoolean();
        if (!present) return null;
        return Type.STRING.read(buffer);
    }

    @Override
    public void write(ByteBuf buffer, String object) throws Exception {
        if (object == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            Type.STRING.write(buffer, object);
        }
    }
}