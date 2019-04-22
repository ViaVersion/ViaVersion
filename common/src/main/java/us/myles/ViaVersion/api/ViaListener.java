package us.myles.ViaVersion.api;

import lombok.*;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;

import java.util.UUID;

@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
@RequiredArgsConstructor
public abstract class ViaListener {
    private final Class<? extends Protocol> requiredPipeline;
    private boolean registered = false;

    /**
     * Get the UserConnection from an UUID
     *
     * @param uuid UUID object
     * @return The UserConnection
     */
    protected UserConnection getUserConnection(@NonNull UUID uuid) {
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
}
