package us.myles.ViaVersion2.api.protocol.base;

import lombok.Getter;
import lombok.Setter;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion2.api.data.StoredObject;

import java.util.UUID;

@Getter
@Setter
public class ProtocolInfo extends StoredObject{
    private State state = State.HANDSHAKE;
    private int protocolVersion = -1;
    private String username;
    private UUID uuid;
}
