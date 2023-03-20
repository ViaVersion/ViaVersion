/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.bungee.handlers;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.ClientEntityIdChangeListener;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.ProtocolPathEntry;
import com.viaversion.viaversion.api.protocol.ProtocolPipeline;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.bungee.storage.BungeeStorage;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.packets.InventoryPackets;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.EntityIdProvider;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.storage.EntityTracker1_9;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.score.Team;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.packet.PluginMessage;

public class BungeeServerHandler implements Listener {
    private static Method getHandshake;
    private static Method getRegisteredChannels;
    private static Method getBrandMessage;
    private static Method setProtocol;
    private static Method getEntityMap = null;
    private static Method setVersion = null;
    private static Field entityRewrite = null;
    private static Field channelWrapper = null;

    static {
        try {
            getHandshake = Class.forName("net.md_5.bungee.connection.InitialHandler").getDeclaredMethod("getHandshake");
            getRegisteredChannels = Class.forName("net.md_5.bungee.connection.InitialHandler").getDeclaredMethod("getRegisteredChannels");
            getBrandMessage = Class.forName("net.md_5.bungee.connection.InitialHandler").getDeclaredMethod("getBrandMessage");
            setProtocol = Class.forName("net.md_5.bungee.protocol.packet.Handshake").getDeclaredMethod("setProtocolVersion", int.class);
            getEntityMap = Class.forName("net.md_5.bungee.entitymap.EntityMap").getDeclaredMethod("getEntityMap", int.class);
            setVersion = Class.forName("net.md_5.bungee.netty.ChannelWrapper").getDeclaredMethod("setVersion", int.class);
            channelWrapper = Class.forName("net.md_5.bungee.UserConnection").getDeclaredField("ch");
            channelWrapper.setAccessible(true);
            entityRewrite = Class.forName("net.md_5.bungee.UserConnection").getDeclaredField("entityRewrite");
            entityRewrite.setAccessible(true);
        } catch (Exception e) {
            Via.getPlatform().getLogger().severe("Error initializing BungeeServerHandler, try updating BungeeCord or ViaVersion!");
            e.printStackTrace();
        }
    }

    // Set the handshake version every time someone connects to any server
    @EventHandler(priority = 120)
    public void onServerConnect(ServerConnectEvent e) {
        if (e.isCancelled()) {
            return;
        }

        UserConnection user = Via.getManager().getConnectionManager().getConnectedClient(e.getPlayer().getUniqueId());
        if (user == null) return;
        if (!user.has(BungeeStorage.class)) {
            user.put(new BungeeStorage(e.getPlayer()));
        }

        int protocolId = Via.proxyPlatform().protocolDetectorService().serverProtocolVersion(e.getTarget().getName());
        List<ProtocolPathEntry> protocols = Via.getManager().getProtocolManager().getProtocolPath(user.getProtocolInfo().getProtocolVersion(), protocolId);

        // Check if ViaVersion can support that version
        try {
            //Object pendingConnection = getPendingConnection.invoke(e.getPlayer());
            Object handshake = getHandshake.invoke(e.getPlayer().getPendingConnection());
            setProtocol.invoke(handshake, protocols == null ? user.getProtocolInfo().getProtocolVersion() : protocolId);
        } catch (InvocationTargetException | IllegalAccessException e1) {
            e1.printStackTrace();
        }
    }

    @EventHandler(priority = -120)
    public void onServerConnected(ServerConnectedEvent e) {
        try {
            checkServerChange(e, Via.getManager().getConnectionManager().getConnectedClient(e.getPlayer().getUniqueId()));
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @EventHandler(priority = -120)
    public void onServerSwitch(ServerSwitchEvent e) {
        // Update entity id
        UserConnection userConnection = Via.getManager().getConnectionManager().getConnectedClient(e.getPlayer().getUniqueId());
        if (userConnection == null) return;
        int playerId;
        try {
            playerId = Via.getManager().getProviders().get(EntityIdProvider.class).getEntityId(userConnection);
        } catch (Exception ex) {
            return; // Ignored
        }

        for (EntityTracker tracker : userConnection.getEntityTrackers()) {
            tracker.setClientEntityId(playerId);
        }

        // For ViaRewind
        for (StorableObject object : userConnection.getStoredObjects().values()) {
            if (object instanceof ClientEntityIdChangeListener) {
                ((ClientEntityIdChangeListener) object).setClientEntityId(playerId);
            }
        }
    }

    public void checkServerChange(ServerConnectedEvent e, UserConnection user) throws Exception {
        if (user == null) return;
        // Auto-team handling
        // Handle server/version change
        if (user.has(BungeeStorage.class)) {
            BungeeStorage storage = user.get(BungeeStorage.class);
            ProxiedPlayer player = storage.getPlayer();

            if (e.getServer() != null) {
                if (!e.getServer().getInfo().getName().equals(storage.getCurrentServer())) {
                    // Clear auto-team
                    EntityTracker1_9 oldEntityTracker = user.getEntityTracker(Protocol1_9To1_8.class);
                    if (oldEntityTracker != null) {
                        if (oldEntityTracker.isAutoTeam() && oldEntityTracker.isTeamExists()) {
                            oldEntityTracker.sendTeamPacket(false, true);
                        }
                    }

                    String serverName = e.getServer().getInfo().getName();

                    storage.setCurrentServer(serverName);

                    int protocolId = Via.proxyPlatform().protocolDetectorService().serverProtocolVersion(serverName);

                    if (protocolId <= ProtocolVersion.v1_8.getVersion()) { // 1.8 doesn't have BossBar packet
                        if (storage.getBossbar() != null) {
                            // This ensures we can encode it properly as only the 1.9 protocol is currently implemented.
                            if (user.getProtocolInfo().getPipeline().contains(Protocol1_9To1_8.class)) {
                                for (UUID uuid : storage.getBossbar()) {
                                    PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_9.BOSSBAR, null, user);
                                    wrapper.write(Type.UUID, uuid);
                                    wrapper.write(Type.VAR_INT, 1); // remove
                                    wrapper.send(Protocol1_9To1_8.class);
                                }
                            }
                            storage.getBossbar().clear();
                        }
                    }

                    ProtocolInfo info = user.getProtocolInfo();
                    int previousServerProtocol = info.getServerProtocolVersion();

                    // Refresh the pipes
                    List<ProtocolPathEntry> protocolPath = Via.getManager().getProtocolManager().getProtocolPath(info.getProtocolVersion(), protocolId);
                    ProtocolPipeline pipeline = user.getProtocolInfo().getPipeline();
                    user.clearStoredObjects(true);
                    pipeline.cleanPipes();
                    if (protocolPath == null) {
                        // TODO Check Bungee Supported Protocols? *shrugs*
                        protocolId = info.getProtocolVersion();
                    } else {
                        List<Protocol> protocols = new ArrayList<>(protocolPath.size());
                        for (ProtocolPathEntry entry : protocolPath) {
                            protocols.add(entry.protocol());
                        }
                        pipeline.add(protocols);
                    }

                    info.setServerProtocolVersion(protocolId);
                    // Add version-specific base Protocol
                    pipeline.add(Via.getManager().getProtocolManager().getBaseProtocol(protocolId));

                    // Workaround 1.13 server change
                    int id1_13 = ProtocolVersion.v1_13.getVersion();
                    boolean toNewId = previousServerProtocol < id1_13 && protocolId >= id1_13;
                    boolean toOldId = previousServerProtocol >= id1_13 && protocolId < id1_13;
                    if (previousServerProtocol != -1 && (toNewId || toOldId)) {
                        Collection<String> registeredChannels = (Collection<String>) getRegisteredChannels.invoke(e.getPlayer().getPendingConnection());
                        if (!registeredChannels.isEmpty()) {
                            Collection<String> newChannels = new HashSet<>();
                            for (Iterator<String> iterator = registeredChannels.iterator(); iterator.hasNext(); ) {
                                String channel = iterator.next();
                                String oldChannel = channel;
                                if (toNewId) {
                                    channel = InventoryPackets.getNewPluginChannelId(channel);
                                } else {
                                    channel = InventoryPackets.getOldPluginChannelId(channel);
                                }
                                if (channel == null) {
                                    iterator.remove();
                                    continue;
                                }
                                if (!oldChannel.equals(channel)) {
                                    iterator.remove();
                                    newChannels.add(channel);
                                }
                            }
                            registeredChannels.addAll(newChannels);
                        }
                        PluginMessage brandMessage = (PluginMessage) getBrandMessage.invoke(e.getPlayer().getPendingConnection());
                        if (brandMessage != null) {
                            String channel = brandMessage.getTag();
                            if (toNewId) {
                                channel = InventoryPackets.getNewPluginChannelId(channel);
                            } else {
                                channel = InventoryPackets.getOldPluginChannelId(channel);
                            }
                            if (channel != null) {
                                brandMessage.setTag(channel);
                            }
                        }
                    }

                    user.put(storage);

                    user.setActive(protocolPath != null);

                    // Init all protocols TODO check if this can get moved up to the previous for loop, and doesn't require the pipeline to already exist.
                    for (Protocol protocol : pipeline.pipes()) {
                        protocol.init(user);
                    }

                    EntityTracker1_9 newTracker = user.getEntityTracker(Protocol1_9To1_8.class);
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

                    Object wrapper = channelWrapper.get(player);
                    setVersion.invoke(wrapper, protocolId);

                    Object entityMap = getEntityMap.invoke(null, protocolId);
                    entityRewrite.set(player, entityMap);
                }
            }
        }
    }
}
