package us.myles.ViaVersion.velocity.handlers;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.protocol.ProtocolPipeline;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import us.myles.ViaVersion.velocity.service.ProtocolDetectorService;
import us.myles.ViaVersion.velocity.storage.VelocityStorage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VelocityServerHandler {
    private static Method setProtocolVersion;
    private static Method setNextProtocolVersion;
    private static Method getMinecraftConnection;
    private static Method getNextProtocolVersion;
    private static Method getKnownChannels;

    static {
        try {
            setProtocolVersion = Class.forName("com.velocitypowered.proxy.connection.MinecraftConnection")
                    .getDeclaredMethod("setProtocolVersion", ProtocolVersion.class);
            setNextProtocolVersion = Class.forName("com.velocitypowered.proxy.connection.MinecraftConnection")
                    .getDeclaredMethod("setNextProtocolVersion", ProtocolVersion.class);
            Class<?> connectedPlayer = Class.forName("com.velocitypowered.proxy.connection.client.ConnectedPlayer");
            getMinecraftConnection = connectedPlayer.getDeclaredMethod("getMinecraftConnection");
            getNextProtocolVersion = Class.forName("com.velocitypowered.proxy.connection.MinecraftConnection")
                    .getDeclaredMethod("getNextProtocolVersion");
            getKnownChannels = connectedPlayer.getDeclaredMethod("getKnownChannels");
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void preServerConnect(ServerPreConnectEvent e) {
        try {
            UserConnection user = Via.getManager().getConnection(e.getPlayer().getUniqueId());
            if (user == null) return;
            if (!user.has(VelocityStorage.class)) {
                user.put(new VelocityStorage(user, e.getPlayer()));
            }

            int protocolId = ProtocolDetectorService.getProtocolId(e.getOriginalServer().getServerInfo().getName());
            List<Pair<Integer, Protocol>> protocols = ProtocolRegistry.getProtocolPath(user.getProtocolInfo().getProtocolVersion(), protocolId);

            // Check if ViaVersion can support that version
            Object connection = getMinecraftConnection.invoke(e.getPlayer());
            setNextProtocolVersion.invoke(connection, ProtocolVersion.getProtocolVersion(protocols == null
                    ? user.getProtocolInfo().getProtocolVersion()
                    : protocolId));

        } catch (IllegalAccessException | InvocationTargetException e1) {
            e1.printStackTrace();
        }
    }

    @Subscribe(order = PostOrder.LATE)
    public void connectedEvent(ServerConnectedEvent e) {
        UserConnection user = Via.getManager().getConnection(e.getPlayer().getUniqueId());
        CompletableFuture.runAsync(() -> {
            try {
                checkServerChange(e, Via.getManager().getConnection(e.getPlayer().getUniqueId()));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }, user.getChannel().eventLoop()).join();
    }

    public void checkServerChange(ServerConnectedEvent e, UserConnection user) throws Exception {
        if (user == null) return;
        // Handle server/version change
        if (user.has(VelocityStorage.class)) {
            VelocityStorage storage = user.get(VelocityStorage.class);

            if (e.getServer() != null) {
                if (!e.getServer().getServerInfo().getName().equals(storage.getCurrentServer())) {
                    String serverName = e.getServer().getServerInfo().getName();

                    storage.setCurrentServer(serverName);

                    int protocolId = ProtocolDetectorService.getProtocolId(serverName);

                    if (protocolId <= ProtocolVersion.MINECRAFT_1_8.getProtocol()) { // 1.8 doesn't have BossBar packet
                        if (storage.getBossbar() != null) {
                            // TODO: Verify whether this packet needs to be sent when 1.8 -> 1.9 protocol isn't present in the pipeline
                            // This ensures we can encode it properly as only the 1.9 protocol is currently implemented.
                            if (user.getProtocolInfo().getPipeline().contains(Protocol1_9To1_8.class)) {
                                for (UUID uuid : storage.getBossbar()) {
                                    PacketWrapper wrapper = new PacketWrapper(0x0C, null, user);
                                    wrapper.write(Type.UUID, uuid);
                                    wrapper.write(Type.VAR_INT, 1); // remove
                                    wrapper.send(Protocol1_9To1_8.class, true, true);
                                }
                            }
                            storage.getBossbar().clear();
                        }
                    }

                    ProtocolInfo info = user.getProtocolInfo();
                    int previousServerProtocol = info.getServerProtocolVersion();

                    // Refresh the pipes
                    List<Pair<Integer, Protocol>> protocols = ProtocolRegistry.getProtocolPath(info.getProtocolVersion(), protocolId);
                    ProtocolPipeline pipeline = info.getPipeline();
                    user.clearStoredObjects();
                    pipeline.cleanPipes();
                    if (protocols == null) {
                        // TODO Check Bungee Supported Protocols? *shrugs*
                        protocolId = info.getProtocolVersion();
                    } else {
                        for (Pair<Integer, Protocol> prot : protocols) {
                            pipeline.add(prot.getValue());
                        }
                    }

                    info.setServerProtocolVersion(protocolId);
                    // Add version-specific base Protocol
                    pipeline.add(ProtocolRegistry.getBaseProtocol(protocolId));

                    Collection<String> knownChannels = (Collection<String>) getKnownChannels.invoke(e.getPlayer());
                    if (previousServerProtocol != -1) {
                        int id1_13 = ProtocolVersion.MINECRAFT_1_13.getProtocol();
                        if (previousServerProtocol < id1_13 && protocolId >= id1_13) {
                            List<String> newChannels = new ArrayList<>();
                            for (String oldChannel : knownChannels) {
                                String transformed = InventoryPackets.getNewPluginChannelId(oldChannel);
                                if (transformed != null) {
                                    newChannels.add(transformed);
                                }
                            }
                            knownChannels.clear();
                            knownChannels.addAll(newChannels);
                        } else if (previousServerProtocol >= id1_13 && protocolId < id1_13) {
                            List<String> newChannels = new ArrayList<>();
                            for (String oldChannel : knownChannels) {
                                String transformed = InventoryPackets.getOldPluginChannelId(oldChannel);
                                if (transformed != null) {
                                    newChannels.add(transformed);
                                }
                            }
                            knownChannels.clear();
                            knownChannels.addAll(newChannels);
                        }
                    }

                    user.put(info);
                    user.put(storage);

                    user.setActive(protocols != null);

                    // Init all protocols TODO check if this can get moved up to the previous for loop, and doesn't require the pipeline to already exist.
                    for (Protocol protocol : pipeline.pipes()) {
                        protocol.init(user);
                    }

                    Object connection = getMinecraftConnection.invoke(e.getPlayer());
                    ProtocolVersion version = (ProtocolVersion) getNextProtocolVersion.invoke(connection);
                    setProtocolVersion.invoke(connection, version);
                }
            }
        }
    }
}
