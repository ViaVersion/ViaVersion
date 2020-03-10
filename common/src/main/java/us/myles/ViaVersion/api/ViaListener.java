package us.myles.ViaVersion.api;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;

import java.util.UUID;

public abstract class ViaListener {
    private final Class<? extends Protocol> requiredPipeline;
    private boolean registered;

    public ViaListener(Class<? extends Protocol> requiredPipeline) {
        this.requiredPipeline = requiredPipeline;
    }

    /**
     * Get the UserConnection from an UUID
     *
     * @param uuid UUID object
     * @return The UserConnection
     */
    protected UserConnection getUserConnection(UUID uuid) {
        return Via.getManager().getConnection(uuid);
    }

    /**
     * Checks if the UUID is on the selected pipe
     *
     * @param uuid UUID Object
     * @return True if on pipe
     */
    protected boolean isOnPipe(UUID uuid) {
        UserConnection userConnection = getUserConnection(uuid);
        return userConnection != null &&
                (requiredPipeline == null || userConnection.get(ProtocolInfo.class).getPipeline().contains(requiredPipeline));
    }

    /**
     * Register the event
     */
    public abstract void register();

    protected Class<? extends Protocol> getRequiredPipeline() {
        return requiredPipeline;
    }

    protected boolean isRegistered() {
        return registered;
    }

    protected void setRegistered(boolean registered) {
        this.registered = registered;
    }
}
