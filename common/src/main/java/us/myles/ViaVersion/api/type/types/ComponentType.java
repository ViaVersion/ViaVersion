package us.myles.ViaVersion.api.type.types;

import com.google.gson.JsonElement;
import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.util.GsonUtil;

public class ComponentType extends Type<JsonElement> {
    private static final StringType STRING_TAG = new StringType(262144);

    public ComponentType() {
        super(JsonElement.class);
    }

    @Override
    public JsonElement read(ByteBuf buffer) throws Exception {
        return GsonUtil.getJsonParser().parse(STRING_TAG.read(buffer));
    }

    @Override
    public void write(ByteBuf buffer, JsonElement object) throws Exception {
        STRING_TAG.write(buffer, object.toString());
    }
}
