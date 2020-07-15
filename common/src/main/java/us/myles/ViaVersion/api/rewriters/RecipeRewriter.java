package us.myles.ViaVersion.api.rewriters;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.protocol.ClientboundPacketType;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;

import java.util.HashMap;
import java.util.Map;

public abstract class RecipeRewriter {

    protected final Protocol protocol;
    protected final ItemRewriter.RewriteFunction rewriter;
    protected final Map<String, RecipeConsumer> recipeHandlers = new HashMap<>();

    protected RecipeRewriter(Protocol protocol, ItemRewriter.RewriteFunction rewriter) {
        this.protocol = protocol;
        this.rewriter = rewriter;
    }

    public void handle(PacketWrapper wrapper, String type) throws Exception {
        RecipeConsumer handler = recipeHandlers.get(type);
        if (handler != null) {
            handler.accept(wrapper);
        }
    }

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

    @FunctionalInterface
    public interface RecipeConsumer {

        void accept(PacketWrapper wrapper) throws Exception;
    }
}
