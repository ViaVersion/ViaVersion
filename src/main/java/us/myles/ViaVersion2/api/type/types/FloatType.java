package us.myles.ViaVersion2.api.type.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion2.api.type.Type;
import us.myles.ViaVersion2.api.type.TypeConverter;

public class FloatType extends Type<Float> implements TypeConverter<Float>{
    public FloatType() {
        super(Float.class);
    }

    @Override
    public Float read(ByteBuf buffer) {
        return buffer.readFloat();
    }

    @Override
    public void write(ByteBuf buffer, Float object) {
        buffer.writeFloat(object);
    }


    @Override
    public Float from(Object o) {
        if (o instanceof Number) {
            return ((Number) o).floatValue();
        }
        if(o instanceof Boolean){
            return ((Boolean)o) == true ? 1F : 0;
        }
        return (Float) o;
    }
}
