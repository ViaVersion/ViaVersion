/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.base;

import com.google.common.base.Joiner;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.protocol.AbstractSimpleProtocol;
import com.viaversion.viaversion.api.protocol.ProtocolPathEntry;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.VersionProvider;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocol.ProtocolManagerImpl;
import com.viaversion.viaversion.protocol.ServerProtocolVersionSingleton;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.util.ChatColorUtil;
import com.viaversion.viaversion.util.GsonUtil;
import io.netty.channel.ChannelFuture;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class BaseProtocol1_7 extends AbstractSimpleProtocol {

    @Override
    protected void registerPackets() {
        /* Outgoing Packets */

        // Status Response Packet
        registerClientbound(State.STATUS, 0x00, 0x00, new PacketRemapper() { // Status Response Packet
            @Override
            public void registerMap() {
                map(Type.STRING);
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        ProtocolInfo info = wrapper.user().getProtocolInfo();
                        String originalStatus = wrapper.get(Type.STRING, 0);
                        try {
                            JsonElement json = GsonUtil.getGson().fromJson(originalStatus, JsonElement.class);
                            JsonObject version;
                            int protocolVersion = 0; // Unknown!

                            if (json.isJsonObject()) {
                                if (json.getAsJsonObject().has("version")) {
                                    version = json.getAsJsonObject().get("version").getAsJsonObject();
                                    if (version.has("protocol")) {
                                        protocolVersion = ((Long) version.get("protocol").getAsLong()).intValue();
                                    }
                                } else {
                                    json.getAsJsonObject().add("version", version = new JsonObject());
                                }
                            } else {
                                // Format properly
                                json = new JsonObject();
                                json.getAsJsonObject().add("version", version = new JsonObject());
                            }

                            if (Via.getConfig().isSendSupportedVersions()) { // Send supported versions
                                version.add("supportedVersions", GsonUtil.getGson().toJsonTree(Via.getAPI().getSupportedVersions()));
                            }

                            if (!Via.getAPI().getServerVersion().isKnown()) { // Set the Server protocol if the detection on startup failed
                                ProtocolManagerImpl protocolManager = (ProtocolManagerImpl) Via.getManager().getProtocolManager();
                                protocolManager.setServerProtocol(new ServerProtocolVersionSingleton(ProtocolVersion.getProtocol(protocolVersion).getVersion()));
                            }

                            // Ensure the server has a version provider
                            VersionProvider versionProvider = Via.getManager().getProviders().get(VersionProvider.class);
                            if (versionProvider == null) {
                                wrapper.user().setActive(false);
                                return;
                            }

                            int closestServerProtocol = versionProvider.getClosestServerProtocol(wrapper.user());
                            List<ProtocolPathEntry> protocols = null;
                            if (info.getProtocolVersion() >= closestServerProtocol || Via.getPlatform().isOldClientsAllowed()) {
                                protocols = Via.getManager().getProtocolManager().getProtocolPath(info.getProtocolVersion(), closestServerProtocol);
                            }

                            if (protocols != null) {
                                if (protocolVersion == closestServerProtocol || protocolVersion == 0) { // Fix ServerListPlus
                                    ProtocolVersion prot = ProtocolVersion.getProtocol(info.getProtocolVersion());
                                    version.addProperty("protocol", prot.getOriginalVersion());
                                }
                            } else {
                                // not compatible :(, *plays very sad violin*
                                wrapper.user().setActive(false);
                            }

                            if (Via.getConfig().getBlockedProtocols().contains(info.getProtocolVersion())) {
                                version.addProperty("protocol", -1); // Show blocked versions as outdated
                            }

                            wrapper.set(Type.STRING, 0, GsonUtil.getGson().toJson(json)); // Update value
                        } catch (JsonParseException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        registerClientbound(State.STATUS, 0x01, 0x01); // Status Pong Packet

        registerClientbound(State.LOGIN, 0x00, 0x00); // Login Disconnect Packet
        registerClientbound(State.LOGIN, 0x01, 0x01); // Encryption Request Packet

        // Login Success Packet
        registerClientbound(State.LOGIN, 0x02, 0x02, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        ProtocolInfo info = wrapper.user().getProtocolInfo();
                        info.setState(State.PLAY);

                        UUID uuid = passthroughLoginUUID(wrapper);
                        info.setUuid(uuid);

                        String username = wrapper.passthrough(Type.STRING);
                        info.setUsername(username);
                        // Add to ported clients
                        Via.getManager().getConnectionManager().onLoginSuccess(wrapper.user());

                        if (!info.getPipeline().hasNonBaseProtocols()) { // Only base protocol
                            wrapper.user().setActive(false);
                        }

                        if (Via.getManager().isDebug()) {
                            // Print out the route to console
                            Via.getPlatform().getLogger().log(Level.INFO, "{0} logged in with protocol {1}, Route: {2}",
                                    new Object[]{
                                            username,
                                            info.getProtocolVersion(),
                                            Joiner.on(", ").join(info.getPipeline().pipes(), ", ")
                                    });
                        }
                    }
                });
            }
        });

        registerClientbound(State.LOGIN, 0x03, 0x03); // Login Set Compression Packet
        registerServerbound(State.LOGIN, 0x04, 0x04); // Plugin Request (1.13)

        /* Incoming Packets */

        registerServerbound(State.STATUS, 0x00, 0x00); // Status Request Packet
        registerServerbound(State.STATUS, 0x01, 0x01); // Status Ping Packet

        // Login Start Packet
        registerServerbound(State.LOGIN, 0x00, 0x00, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(final PacketWrapper wrapper) throws Exception {
                        int protocol = wrapper.user().getProtocolInfo().getProtocolVersion();
                        if (Via.getConfig().getBlockedProtocols().contains(protocol)) {
                            if (!wrapper.user().getChannel().isOpen()) return;
                            if (!wrapper.user().shouldApplyBlockProtocol()) return;

                            PacketWrapper disconnectPacket = PacketWrapper.create(0x00, null, wrapper.user()); // Disconnect Packet
                            Protocol1_9To1_8.FIX_JSON.write(disconnectPacket, ChatColorUtil.translateAlternateColorCodes(Via.getConfig().getBlockedDisconnectMsg()));
                            wrapper.cancel(); // cancel current

                            // Send and close
                            ChannelFuture future = disconnectPacket.sendFuture(BaseProtocol.class);
                            future.addListener(f -> wrapper.user().getChannel().close());
                        }
                    }
                });
            }
        }); // Login Start Packet
        registerServerbound(State.LOGIN, 0x01, 0x01); // Encryption Response Packet
        registerServerbound(State.LOGIN, 0x02, 0x02); // Plugin Response (1.13)
    }

    @Override
    public boolean isBaseProtocol() {
        return true;
    }

    public static String addDashes(String trimmedUUID) {
        StringBuilder idBuff = new StringBuilder(trimmedUUID);
        idBuff.insert(20, '-');
        idBuff.insert(16, '-');
        idBuff.insert(12, '-');
        idBuff.insert(8, '-');
        return idBuff.toString();
    }

    protected UUID passthroughLoginUUID(PacketWrapper wrapper) throws Exception {
        String uuidString = wrapper.passthrough(Type.STRING);
        if (uuidString.length() == 32) { // Trimmed UUIDs are 32 characters
            // Trimmed
            uuidString = addDashes(uuidString);
        }
        return UUID.fromString(uuidString);
    }
}
