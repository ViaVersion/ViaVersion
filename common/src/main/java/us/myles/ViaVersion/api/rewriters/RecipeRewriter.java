package us.myles.ViaVersion.api.rewriters;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.protocol.ClientboundPacketType;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;

public abstract class RecipeRewriter {

    protected final Protocol protocol;
    protected final ItemRewriter.RewriteFunction rewriter;

    protected RecipeRewriter(Protocol protocol, ItemRewriter.RewriteFunction rewriter) {
        this.protocol = protocol;
        this.rewriter = rewriter;
    }

    public abstract void handle(PacketWrapper wrapper, String type) throws Exception;

    public void registerDefaultHandler(ClientboundPacketType packetType) {
        protocol.registerOutgoing(packetType, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    int size = wrapper.passthrough(Type.VAR_INT);
                    for (int i = 0; i < size; i++) {
                        String type = wrapper.passthrough(Type.STRING).replace("minecraft:", "");
                        String id = wrapper.passthrough(Type.STRING); // Recipe Identifier
                        handle(wrapper, type);
                    }
                });
            }
        });
    }
}
