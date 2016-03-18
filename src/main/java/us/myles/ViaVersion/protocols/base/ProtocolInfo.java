package us.myles.ViaVersion.protocols.base;

import lombok.Getter;
import lombok.Setter;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.ProtocolPipeline;
import us.myles.ViaVersion.packets.State;

import java.util.UUID;

@Getter
@Setter
public class ProtocolInfo extends StoredObject {
    private State state = State.HANDSHAKE;
    private int protocolVersion = -1;
    private String username;
    private UUID uuid;
    private ProtocolPipeline pipeline;

    public ProtocolInfo(UserConnection user) {
        super(user);
    }
}
