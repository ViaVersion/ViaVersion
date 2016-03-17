package us.myles.ViaVersion2.api.type.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion2.api.type.Type;
import us.myles.ViaVersion2.api.type.TypeConverter;

public class IntType extends Type<Integer> implements TypeConverter<Integer>{
    public IntType() {
        super(Integer.class);
    }

    @Override
    public Integer read(ByteBuf buffer) {
        return buffer.readInt();
    }

    @Override
    public void write(ByteBuf buffer, Integer object) {
        buffer.writeInt(object);
    }

    @Override
    public Integer from(Object o) {
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        if(o instanceof Boolean){
            return ((Boolean)o) == true ? 1 : 0;
        }
        return (Integer) o;
    }
}
