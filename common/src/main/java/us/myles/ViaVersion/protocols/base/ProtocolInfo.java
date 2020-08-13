package us.myles.ViaVersion.protocols.base;

import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.ProtocolPipeline;
import us.myles.ViaVersion.packets.State;

import java.util.UUID;

public class ProtocolInfo extends StoredObject {
    private State state = State.HANDSHAKE;
    private int protocolVersion = -1;
    private int serverProtocolVersion = -1;
    private String username;
    private UUID uuid;
    private ProtocolPipeline pipeline;

    public ProtocolInfo(UserConnection user) {
        super(user);
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public int getServerProtocolVersion() {
        return serverProtocolVersion;
    }

    public void setServerProtocolVersion(int serverProtocolVersion) {
        this.serverProtocolVersion = serverProtocolVersion;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public ProtocolPipeline getPipeline() {
        return pipeline;
    }

    public void setPipeline(ProtocolPipeline pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public String toString() {
        return "ProtocolInfo{" +
                "state=" + state +
                ", protocolVersion=" + protocolVersion +
                ", serverProtocolVersion=" + serverProtocolVersion +
                ", username='" + username + '\'' +
                ", uuid=" + uuid +
                '}';
    }
}
