package us.myles.ViaVersion.api.rewriters;

import us.myles.ViaVersion.api.protocol.ClientboundPacketType;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;

public class SoundRewriter {
    protected final Protocol protocol;
    // Can't hold the mappings instance here since it's loaded later
    protected final IdRewriteFunction idRewriter;

    public SoundRewriter(Protocol protocol, IdRewriteFunction idRewriter) {
        this.protocol = protocol;
        this.idRewriter = idRewriter;
    }

    // The same for entity sound effect
    public void registerSound(int oldId, int newId) {
        protocol.registerOutgoing(State.PLAY, oldId, newId, getRemapper());
    }

    public void registerSound(ClientboundPacketType packetType) {
        protocol.registerOutgoing(packetType, getRemapper());
    }

    protected PacketRemapper getRemapper() {
        return new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // Sound Id
                handler(wrapper -> {
                    int soundId = wrapper.get(Type.VAR_INT, 0);
                    int mappedId = idRewriter.rewrite(soundId);
                    if (mappedId == -1) {
                        wrapper.cancel();
                    } else if (soundId != mappedId) {
                        wrapper.set(Type.VAR_INT, 0, mappedId);
                    }
                });
            }
        };
    }
}
