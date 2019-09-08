package us.myles.ViaVersion.bungee.handlers;

import lombok.SneakyThrows;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.score.Team;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.md_5.bungee.protocol.packet.Handshake;
import net.md_5.bungee.protocol.packet.PluginMessage;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.ExternalJoinGameListener;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.protocol.ProtocolPipeline;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.bungee.service.ProtocolDetectorService;
import us.myles.ViaVersion.bungee.storage.BungeeStorage;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.EntityIdProvider;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.EntityTracker;
import us.myles.ViaVersion.util.InvokeUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class BungeeServerHandler implements Listener {
    private static final MethodHandle getHandshake;
    private static final MethodHandle getRelayMessages;
    private static final MethodHandle getEntityMap;
    private static final MethodHandle setVersion;
    private static final MethodHandle entityRewrite;
    private static final MethodHandle channelWrapper;

    static {
        try {
            MethodHandles.Lookup lookup = InvokeUtil.lookup();
            Class<?> initialHandlerClass = Class.forName("net.md_5.bungee.connection.InitialHandler");
            getHandshake = lookup.findVirtual(initialHandlerClass, "getHandshake", MethodType.methodType(Handshake.class));
            getRelayMessages = lookup.findVirtual(initialHandlerClass, "getRelayMessages", MethodType.methodType(List.class));
            Class<?> entityMapClass = Class.forName("net.md_5.bungee.entitymap.EntityMap");
            getEntityMap = lookup.findStatic(entityMapClass, "getEntityMap", MethodType.methodType(entityMapClass, Integer.TYPE));
            Class<?> channelWrapperClass = Class.forName("net.md_5.bungee.netty.ChannelWrapper");
            setVersion = lookup.findVirtual(channelWrapperClass, "setVersion", MethodType.methodType(Void.TYPE, Integer.TYPE));
            Class<?> userConnectionClass = Class.forName("net.md_5.bungee.UserConnection");
            channelWrapper = lookup.findGetter(userConnectionClass, "ch", channelWrapperClass);
            entityRewrite = lookup.findSetter(userConnectionClass, "entityRewrite", entityMapClass);
        } catch (NoSuchFieldException | NoSuchMethodException | ClassNotFoundException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // Set the handshake version every time someone connects to any server
    @EventHandler
    @SneakyThrows
    public void onServerConnect(ServerConnectEvent e) {
        UserConnection user = Via.getManager().getConnection(e.getPlayer().getUniqueId());
        if (user == null) return;
        if (!user.has(BungeeStorage.class)) {
            user.put(new BungeeStorage(user, e.getPlayer()));
        }

        int protocolId = ProtocolDetectorService.getProtocolId(e.getTarget().getName());
        List<Pair<Integer, Protocol>> protocols = ProtocolRegistry.getProtocolPath(user.get(ProtocolInfo.class).getProtocolVersion(), protocolId);

        // Check if ViaVersion can support that version
        Handshake handshake = (Handshake) getHandshake.invoke(e.getPlayer().getPendingConnection());
        handshake.setProtocolVersion(protocols == null ? user.get(ProtocolInfo.class).getProtocolVersion() : protocolId);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerConnected(ServerConnectedEvent e) {
        try {
            checkServerChange(e, Via.getManager().getConnection(e.getPlayer().getUniqueId()));
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerSwitch(ServerSwitchEvent e) {
        // Update entity id
        UserConnection userConnection = Via.getManager().getConnection(e.getPlayer().getUniqueId());
        if (userConnection == null) return;
        int playerId;
        try {
            playerId = Via.getManager().getProviders().get(EntityIdProvider.class).getEntityId(userConnection);
        } catch (Exception ex) {
            return; // Ignored
        }
        for (StoredObject storedObject : userConnection.getStoredObjects().values()) {
            if (storedObject instanceof ExternalJoinGameListener) {
                ((ExternalJoinGameListener) storedObject).onExternalJoinGame(playerId);
            }
        }
    }

    @SneakyThrows
    public void checkServerChange(ServerConnectedEvent e, UserConnection user) {
        if (user == null) return;
        // Auto-team handling
        // Handle server/version change
        if (user.has(BungeeStorage.class)) {
            BungeeStorage storage = user.get(BungeeStorage.class);
            ProxiedPlayer player = storage.getPlayer();

            if (e.getServer() != null) {
                if (!e.getServer().getInfo().getName().equals(storage.getCurrentServer())) {
                    // Clear auto-team
                    EntityTracker oldEntityTracker = user.get(EntityTracker.class);
                    if (oldEntityTracker != null) {
                        if (oldEntityTracker.isAutoTeam() && oldEntityTracker.isTeamExists()) {
                            oldEntityTracker.sendTeamPacket(false, true);
                        }
                    }

                    String serverName = e.getServer().getInfo().getName();

                    storage.setCurrentServer(serverName);

                    int protocolId = ProtocolDetectorService.getProtocolId(serverName);

                    if (protocolId <= ProtocolVersion.v1_8.getId()) { // 1.8 doesn't have BossBar packet
                        if (storage.getBossbar() != null) {
                            for (UUID uuid : storage.getBossbar()) {
                                PacketWrapper wrapper = new PacketWrapper(0x0C, null, user);
                                wrapper.write(Type.UUID, uuid);
                                wrapper.write(Type.VAR_INT, 1); // remove
                                wrapper.send(Protocol1_9To1_8.class, true, true);
                            }
                            storage.getBossbar().clear();
                        }
                    }

                    ProtocolInfo info = user.get(ProtocolInfo.class);
                    int previousServerProtocol = info.getServerProtocolVersion();

                    // Refresh the pipes
                    List<Pair<Integer, Protocol>> protocols = ProtocolRegistry.getProtocolPath(info.getProtocolVersion(), protocolId);
                    ProtocolPipeline pipeline = user.get(ProtocolInfo.class).getPipeline();
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

                    // Workaround 1.13 server change
                    Object relayMessages = getRelayMessages.invoke(e.getPlayer().getPendingConnection());
                    for (Object message : (List) relayMessages) {
                        PluginMessage plMsg = (PluginMessage) message;
                        String channel = plMsg.getTag();
                        int id1_13 = ProtocolVersion.v1_13.getId();
                        if (previousServerProtocol != -1) {
                            String oldChannel = channel;
                            if (previousServerProtocol < id1_13 && protocolId >= id1_13) {
                                channel = InventoryPackets.getNewPluginChannelId(channel);
                                if (channel == null) {
                                    throw new RuntimeException(oldChannel + " found in relayMessages");
                                }
                                if (channel.equals("minecraft:register")) {
                                    plMsg.setData(Arrays.stream(new String(plMsg.getData(), StandardCharsets.UTF_8).split("\0"))
                                            .map(InventoryPackets::getNewPluginChannelId)
                                            .filter(Objects::nonNull)
                                            .collect(Collectors.joining("\0")).getBytes(StandardCharsets.UTF_8));
                                }
                            } else if (previousServerProtocol >= id1_13 && protocolId < id1_13) {
                                channel = InventoryPackets.getOldPluginChannelId(channel);
                                if (channel == null) {
                                    throw new RuntimeException(oldChannel + " found in relayMessages");
                                }
                                if (channel.equals("REGISTER")) {
                                    plMsg.setData(Arrays.stream(new String(plMsg.getData(), StandardCharsets.UTF_8).split("\0"))
                                            .map(InventoryPackets::getOldPluginChannelId)
                                            .filter(Objects::nonNull)
                                            .collect(Collectors.joining("\0")).getBytes(StandardCharsets.UTF_8));
                                }
                            }
                        }
                        plMsg.setTag(channel);
                    }

                    user.put(info);
                    user.put(storage);

                    user.setActive(protocols != null);

                    // Init all protocols TODO check if this can get moved up to the previous for loop, and doesn't require the pipeline to already exist.
                    for (Protocol protocol : pipeline.pipes()) {
                        protocol.init(user);
                    }

                    EntityTracker newTracker = user.get(EntityTracker.class);
                    if (newTracker != null) {
                        if (Via.getConfig().isAutoTeam()) {
                            String currentTeam = null;
                            for (Team team : player.getScoreboard().getTeams()) {
                                if (team.getPlayers().contains(info.getUsername())) {
                                    currentTeam = team.getName();

                                }
                            }

                            // Reinitialize auto-team
                            newTracker.setAutoTeam(true);
                            if (currentTeam == null) {
                                // Send auto-team as it was cleared above
                                newTracker.sendTeamPacket(true, true);
                                newTracker.setCurrentTeam("viaversion");
                            } else {
                                // Auto-team will be sent when bungee send remove packet
                                newTracker.setAutoTeam(Via.getConfig().isAutoTeam());
                                newTracker.setCurrentTeam(currentTeam);
                            }
                        }
                    }

                    Object wrapper = channelWrapper.invoke(player);
                    setVersion.invoke(wrapper, protocolId);
                    Object entityMap = getEntityMap.invoke(protocolId);
                    entityRewrite.invoke(player, entityMap);
                }
            }
        }
    }
}
