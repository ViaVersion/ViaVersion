package us.myles.ViaVersion2.api.type;

import io.netty.buffer.ByteBuf;

public abstract class PartialType<T, X> extends Type<T> {
    private final X param;

    public PartialType(X param, Class<T> type) {
        super(type);
        this.param = param;
    }

    public abstract T read(ByteBuf buffer, X param);

    public abstract void write(ByteBuf buffer, X param, T object);

    @Override
    public T read(ByteBuf buffer) {
        return read(buffer, this.param);
    }

    @Override
    public void write(ByteBuf buffer, T object) {
        write(buffer, this.param, object);
    }
}
