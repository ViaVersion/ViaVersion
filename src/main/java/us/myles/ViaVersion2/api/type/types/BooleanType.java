package us.myles.ViaVersion2.api.type.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion2.api.type.Type;
import us.myles.ViaVersion2.api.type.TypeConverter;

public class BooleanType extends Type<Boolean> implements TypeConverter<Boolean>{
    public BooleanType() {
        super(Boolean.class);
    }

    @Override
    public Boolean read(ByteBuf buffer) {
        return buffer.readBoolean();
    }

    @Override
    public void write(ByteBuf buffer, Boolean object) {
        buffer.writeBoolean(object);
    }


    @Override
    public Boolean from(Object o) {
        if (o instanceof Number) {
            return ((Number) o).intValue() == 1;
        }
        return (Boolean) o;
    }
}
