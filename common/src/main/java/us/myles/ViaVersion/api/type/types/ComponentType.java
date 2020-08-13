package us.myles.ViaVersion.api.type.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.util.GsonUtil;

public class ComponentType extends Type<JsonElement> {
    private static final StringType STRING_TAG = new StringType(262144);

    public ComponentType() {
        super(JsonElement.class);
    }

    @Override
    public JsonElement read(ByteBuf buffer) throws Exception {
        String s = STRING_TAG.read(buffer);
        try {
            return GsonUtil.getJsonParser().parse(s);
        } catch (JsonSyntaxException e) {
            Via.getPlatform().getLogger().severe("Error when trying to parse json: " + s);
            throw e;
        }
    }

    @Override
    public void write(ByteBuf buffer, JsonElement object) throws Exception {
        STRING_TAG.write(buffer, object.toString());
    }
}
