/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
import com.viaversion.viaversion.protocols.v1_12_2to1_13.rewriter.ItemPacketRewriter1_13;
import com.viaversion.viaversion.protocols.v1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.providers.EntityIdProvider;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.EntityTracker1_9;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.score.Team;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.packet.Handshake;
import net.md_5.bungee.protocol.packet.PluginMessage;

// All of this is madness
public class BungeeServerHandler implements Listener {
    private static final MethodHandle GET_HANDSHAKE;
    private static final MethodHandle GET_REGISTERED_CHANNELS;
    private static final MethodHandle GET_BRAND_MESSAGE;
    private static final MethodHandle GET_ENTITY_MAP;
    private static final MethodHandle SET_VERSION;
    private static final MethodHandle SET_ENTITY_REWRITE;
    private static final MethodHandle GET_CHANNEL_WRAPPER;

    static {
        try {
            final MethodHandles.Lookup lookup = MethodHandles.lookup();
            final Class<?> initialHandlerClass = Class.forName("net.md_5.bungee.connection.InitialHandler");
            GET_HANDSHAKE = lookup.findVirtual(initialHandlerClass, "getHandshake", MethodType.methodType(Handshake.class));
            GET_REGISTERED_CHANNELS = lookup.findVirtual(initialHandlerClass, "getRegisteredChannels", MethodType.methodType(Set.class));
            GET_BRAND_MESSAGE = lookup.findVirtual(initialHandlerClass, "getBrandMessage", MethodType.methodType(PluginMessage.class));

            final Class<?> entityMapClass = Class.forName("net.md_5.bungee.entitymap.EntityMap");
            final Class<?> channelWrapperClass = Class.forName("net.md_5.bungee.netty.ChannelWrapper");
            GET_ENTITY_MAP = lookup.findStatic(entityMapClass, "getEntityMap", MethodType.methodType(entityMapClass, int.class));
            SET_VERSION = lookup.findVirtual(channelWrapperClass, "setVersion", MethodType.methodType(void.class, int.class));

            final Class<?> userConnectionClass = Class.forName("net.md_5.bungee.UserConnection");
            final MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(userConnectionClass, lookup);
            GET_CHANNEL_WRAPPER = privateLookup.findGetter(userConnectionClass, "ch", channelWrapperClass);
            SET_ENTITY_REWRITE = privateLookup.findSetter(userConnectionClass, "entityRewrite", entityMapClass);
        } catch (final ReflectiveOperationException e) {
            Via.getPlatform().getLogger().severe("Error initializing BungeeServerHandler, try updating BungeeCord or ViaVersion!");
            throw new RuntimeException(e);
        }
    }

    // Set the handshake version every time someone connects to any server
    @EventHandler(priority = 120)
    public void onServerConnect(ServerConnectEvent event) {
        if (event.isCancelled()) {
            return;
        }

        UserConnection user = Via.getManager().getConnectionManager().getConnectedClient(event.getPlayer().getUniqueId());
        if (user == null) {
            return;
        }

        if (!user.has(BungeeStorage.class)) {
            user.put(new BungeeStorage(event.getPlayer()));
        }

        ProtocolVersion serverProtocolVersion = Via.proxyPlatform().protocolDetectorService().serverProtocolVersion(event.getTarget().getName());
        ProtocolVersion clientProtocolVersion = user.getProtocolInfo().protocolVersion();
        List<ProtocolPathEntry> protocols = Via.getManager().getProtocolManager().getProtocolPath(clientProtocolVersion, serverProtocolVersion);

        // Check if ViaVersion can support that version
        try {
            Handshake handshake = (Handshake) GET_HANDSHAKE.invoke(event.getPlayer().getPendingConnection());
            handshake.setProtocolVersion(protocols == null ? clientProtocolVersion.getVersion() : serverProtocolVersion.getVersion());
        } catch (Throwable e) {
            Via.getPlatform().getLogger().log(Level.SEVERE, "Error setting handshake version", e);
        }
    }

    @EventHandler(priority = -120)
    public void onServerConnected(ServerConnectedEvent event) {
        try {
            checkServerChange(event, Via.getManager().getConnectionManager().getConnectedClient(event.getPlayer().getUniqueId()));
        } catch (Throwable e) {
            Via.getPlatform().getLogger().log(Level.SEVERE, "Failed to handle server switch", e);
        }
    }

    @EventHandler(priority = -120)
    public void onServerSwitch(ServerSwitchEvent event) {
        // Update entity id
        UserConnection userConnection = Via.getManager().getConnectionManager().getConnectedClient(event.getPlayer().getUniqueId());
        if (userConnection == null) {
            return;
        }

        int playerId;
        try {
            playerId = Via.getManager().getProviders().get(EntityIdProvider.class).getEntityId(userConnection);
        } catch (Exception ignored) {
            return;
        }

        for (EntityTracker tracker : userConnection.getEntityTrackers()) {
            tracker.setClientEntityId(playerId);
        }

        // For ViaRewind
        for (StorableObject object : userConnection.getStoredObjects().values()) {
            if (object instanceof ClientEntityIdChangeListener listener) {
                listener.setClientEntityId(playerId);
            }
        }
    }

    public void checkServerChange(ServerConnectedEvent event, UserConnection user) throws Throwable {
        if (user == null) {
            return;
        }

        BungeeStorage storage = user.get(BungeeStorage.class);
        if (storage == null) {
            return;
        }

        Server server = event.getServer();
        if (server == null || server.getInfo().getName().equals(storage.getCurrentServer())) {
            return;
        }


        // Clear auto-team
        EntityTracker1_9 oldEntityTracker = user.getEntityTracker(Protocol1_8To1_9.class);
        if (oldEntityTracker != null && oldEntityTracker.isAutoTeam() && oldEntityTracker.isTeamExists()) {
            oldEntityTracker.sendTeamPacket(false, true);
        }

        String serverName = server.getInfo().getName();
        storage.setCurrentServer(serverName);
        ProtocolVersion serverProtocolVersion = Via.proxyPlatform().protocolDetectorService().serverProtocolVersion(serverName);
        if (serverProtocolVersion.olderThanOrEqualTo(ProtocolVersion.v1_8) && storage.getBossbar() != null) { // 1.8 doesn't have BossBar packet
            // This ensures we can encode it properly as only the 1.9 protocol is currently implemented.
            if (user.getProtocolInfo().getPipeline().contains(Protocol1_8To1_9.class)) {
                for (UUID uuid : storage.getBossbar()) {
                    PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_9.BOSSBAR, null, user);
                    wrapper.write(Type.UUID, uuid);
                    wrapper.write(Type.VAR_INT, 1); // remove
                    wrapper.send(Protocol1_8To1_9.class);
                }
            }
            storage.getBossbar().clear();
        }

        ProtocolInfo info = user.getProtocolInfo();
        ProtocolVersion previousServerProtocol = info.serverProtocolVersion();

        // Refresh the pipes
        List<ProtocolPathEntry> protocolPath = Via.getManager().getProtocolManager().getProtocolPath(info.protocolVersion(), serverProtocolVersion);
        ProtocolPipeline pipeline = user.getProtocolInfo().getPipeline();
        user.clearStoredObjects(true);
        pipeline.cleanPipes();
        if (protocolPath != null) {
            info.setServerProtocolVersion(serverProtocolVersion);
            pipeline.add(protocolPath.stream().map(ProtocolPathEntry::protocol).collect(Collectors.toList()));
        } else {
            // TODO Check Bungee Supported Protocols? *shrugs*
            serverProtocolVersion = info.protocolVersion();
            info.setServerProtocolVersion(serverProtocolVersion);
        }

        // Add version-specific base Protocol
        pipeline.add(Via.getManager().getProtocolManager().getBaseProtocol(serverProtocolVersion));

        // Workaround 1.13 server change
        boolean toNewId = previousServerProtocol.olderThan(ProtocolVersion.v1_13) && serverProtocolVersion.newerThanOrEqualTo(ProtocolVersion.v1_13);
        boolean toOldId = previousServerProtocol.newerThanOrEqualTo(ProtocolVersion.v1_13) && serverProtocolVersion.olderThan(ProtocolVersion.v1_13);
        if (previousServerProtocol.isKnown() && (toNewId || toOldId)) {
            Collection<String> registeredChannels = (Collection<String>) GET_REGISTERED_CHANNELS.invoke(event.getPlayer().getPendingConnection());
            if (!registeredChannels.isEmpty()) {
                Collection<String> newChannels = new HashSet<>();
                for (Iterator<String> iterator = registeredChannels.iterator(); iterator.hasNext(); ) {
                    String channel = iterator.next();
                    String oldChannel = channel;
                    if (toNewId) {
                        channel = ItemPacketRewriter1_13.getNewPluginChannelId(channel);
                    } else {
                        channel = ItemPacketRewriter1_13.getOldPluginChannelId(channel);
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

            PluginMessage brandMessage = (PluginMessage) GET_BRAND_MESSAGE.invoke(event.getPlayer().getPendingConnection());
            if (brandMessage != null) {
                String channel = brandMessage.getTag();
                if (toNewId) {
                    channel = ItemPacketRewriter1_13.getNewPluginChannelId(channel);
                } else {
                    channel = ItemPacketRewriter1_13.getOldPluginChannelId(channel);
                }
                if (channel != null) {
                    brandMessage.setTag(channel);
                }
            }
        }

        user.put(storage);

        user.setActive(protocolPath != null);

        ProxiedPlayer player = storage.getPlayer();
        EntityTracker1_9 newTracker = user.getEntityTracker(Protocol1_8To1_9.class);
        if (newTracker != null && Via.getConfig().isAutoTeam()) {
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

        Object wrapper = GET_CHANNEL_WRAPPER.invoke(player);
        SET_VERSION.invoke(wrapper, serverProtocolVersion.getVersion());

        Object entityMap = GET_ENTITY_MAP.invoke(serverProtocolVersion.getVersion());
        SET_ENTITY_REWRITE.invoke(player, entityMap);
    }
}
