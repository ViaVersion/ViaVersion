package us.myles.ViaVersion.api.rewriters;

import us.myles.ViaVersion.api.protocol.Protocol;

public abstract class Rewriter<T extends Protocol> {
    protected final T protocol;

    protected Rewriter(T protocol) {
        this.protocol = protocol;
    }

    public T getProtocol() {
        return protocol;
    }
}
