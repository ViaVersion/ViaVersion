package us.myles.ViaVersion;

import org.jetbrains.annotations.Nullable;
import us.myles.ViaVersion.api.command.ViaVersionCommand;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.platform.ViaConnectionManager;
import us.myles.ViaVersion.api.platform.ViaInjector;
import us.myles.ViaVersion.api.platform.ViaPlatform;
import us.myles.ViaVersion.api.platform.ViaPlatformLoader;
import us.myles.ViaVersion.api.platform.providers.ViaProviders;
import us.myles.ViaVersion.api.protocol.Protocol;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface ViaManager {

    Set<UserConnection> getConnections();

    /**
     * @deprecated use getConnectedClients()
     */
    @Deprecated
    Map<UUID, UserConnection> getPortedPlayers();

    Map<UUID, UserConnection> getConnectedClients();

    UUID getConnectedClientId(UserConnection conn);

    /**
     * @see ViaConnectionManager#isClientConnected(UUID)
     */
    boolean isClientConnected(UUID player);

    void handleLoginSuccess(UserConnection info);

    ViaPlatform<?> getPlatform();

    ViaProviders getProviders();

    boolean isDebug();

    void setDebug(boolean debug);

    ViaInjector getInjector();

    ViaVersionCommand getCommandHandler();

    ViaPlatformLoader getLoader();

    /**
     * Returns a mutable set of self-added subplatform version strings.
     * This set is expanded by the subplatform itself (e.g. ViaBackwards), and may not contain all running ones.
     *
     * @return mutable set of subplatform versions
     */
    Set<String> getSubPlatforms();

    /**
     * @see ViaConnectionManager#getConnectedClient(UUID)
     */
    @Nullable
    UserConnection getConnection(UUID playerUUID);

    /**
     * Adds a runnable to be executed when ViaVersion has finished its init before the full server load.
     *
     * @param runnable runnable to be executed
     */
    void addEnableListener(Runnable runnable);

    Protocol getBaseProtocol();

    boolean isBaseProtocol(Protocol protocol);
}
