package us.myles.ViaVersion.api.type;

import io.netty.buffer.ByteBuf;

public abstract class PartialType<T, X> extends Type<T> {
    private final X param;

    public PartialType(X param, Class<T> type) {
        super(type);
        this.param = param;
    }

    public abstract T read(ByteBuf buffer, X param) throws Exception;

    public abstract void write(ByteBuf buffer, X param, T object) throws Exception;

    @Override
    public T read(ByteBuf buffer) throws Exception {
        return read(buffer, this.param);
    }

    @Override
    public void write(ByteBuf buffer, T object) throws Exception {
        write(buffer, this.param, object);
    }
}
