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
package com.viaversion.viaversion.protocols.base;

import com.google.common.base.Joiner;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.ProtocolPathEntry;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.VersionProvider;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocol.ProtocolManagerImpl;
import com.viaversion.viaversion.protocol.ServerProtocolVersionSingleton;
import com.viaversion.viaversion.protocols.base.packet.BaseClientboundPacket;
import com.viaversion.viaversion.protocols.base.packet.BasePacketTypesProvider;
import com.viaversion.viaversion.protocols.base.packet.BaseServerboundPacket;
import com.viaversion.viaversion.util.ChatColorUtil;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.GsonUtil;
import io.netty.channel.ChannelFuture;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class BaseProtocol1_7 extends AbstractProtocol<BaseClientboundPacket, BaseClientboundPacket, BaseServerboundPacket, BaseServerboundPacket> {

    public BaseProtocol1_7() {
        super(BaseClientboundPacket.class, BaseClientboundPacket.class, BaseServerboundPacket.class, BaseServerboundPacket.class);
    }

    @Override
    protected void registerPackets() {
        registerClientbound(ClientboundStatusPackets.STATUS_RESPONSE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING);
                handler(wrapper -> {
                    ProtocolInfo info = wrapper.user().getProtocolInfo();
                    String originalStatus = wrapper.get(Type.STRING, 0);
                    try {
                        JsonElement json = GsonUtil.getGson().fromJson(originalStatus, JsonElement.class);
                        JsonObject version;
                        int protocol = 0; // Unknown!

                        if (json.isJsonObject()) {
                            if (json.getAsJsonObject().has("version")) {
                                version = json.getAsJsonObject().get("version").getAsJsonObject();
                                if (version.has("protocol")) {
                                    protocol = ((Long) version.get("protocol").getAsLong()).intValue();
                                }
                            } else {
                                json.getAsJsonObject().add("version", version = new JsonObject());
                            }
                        } else {
                            // Format properly
                            json = new JsonObject();
                            json.getAsJsonObject().add("version", version = new JsonObject());
                        }

                        final ProtocolVersion protocolVersion = ProtocolVersion.getProtocol(protocol);

                        if (Via.getConfig().isSendSupportedVersions()) { // Send supported versions
                            version.add("supportedVersions", GsonUtil.getGson().toJsonTree(Via.getAPI().getSupportedVersions()));
                        }

                        if (!Via.getAPI().getServerVersion().isKnown()) { // Set the Server protocol if the detection on startup failed
                            ProtocolManagerImpl protocolManager = (ProtocolManagerImpl) Via.getManager().getProtocolManager();
                            protocolManager.setServerProtocol(new ServerProtocolVersionSingleton(protocolVersion));
                        }

                        // Ensure the server has a version provider
                        VersionProvider versionProvider = Via.getManager().getProviders().get(VersionProvider.class);
                        if (versionProvider == null) {
                            wrapper.user().setActive(false);
                            return;
                        }

                        ProtocolVersion closestServerProtocol = versionProvider.getClosestServerProtocol(wrapper.user());
                        List<ProtocolPathEntry> protocols = null;
                        if (info.protocolVersion().newerThanOrEqualTo(closestServerProtocol) || Via.getPlatform().isOldClientsAllowed()) {
                            protocols = Via.getManager().getProtocolManager()
                                .getProtocolPath(info.protocolVersion(), closestServerProtocol);
                        }

                        if (protocols != null) {
                            if (protocolVersion.equalTo(closestServerProtocol) || protocolVersion.getVersion() == 0) { // Fix ServerListPlus
                                version.addProperty("protocol", info.protocolVersion().getOriginalVersion());
                            }
                        } else {
                            // not compatible :(, *plays very sad violin*
                            wrapper.user().setActive(false);
                        }

                        if (Via.getConfig().blockedProtocolVersions().contains(info.protocolVersion())) {
                            version.addProperty("protocol", -1); // Show blocked versions as outdated
                        }

                        wrapper.set(Type.STRING, 0, GsonUtil.getGson().toJson(json)); // Update value
                    } catch (JsonParseException e) {
                        Via.getPlatform().getLogger().log(Level.SEVERE, "Error handling StatusResponse", e);
                    }
                });
            }
        });

        // Login Success Packet
        registerClientbound(ClientboundLoginPackets.GAME_PROFILE, wrapper -> {
            ProtocolInfo info = wrapper.user().getProtocolInfo();
            if (info.protocolVersion().olderThan(ProtocolVersion.v1_20_2)) { // On 1.20.2+, wait for the login ack
                info.setState(State.PLAY);
            }

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
                                info.protocolVersion().getName(),
                                Joiner.on(", ").join(info.getPipeline().pipes(), ", ")
                        });
            }
        });

        // Login Start Packet
        registerServerbound(ServerboundLoginPackets.HELLO, wrapper -> {
            final UserConnection user = wrapper.user();
            final ProtocolVersion protocol = user.getProtocolInfo().protocolVersion();
            if (Via.getConfig().blockedProtocolVersions().contains(protocol)) {
                if (!user.getChannel().isOpen() || !user.shouldApplyBlockProtocol()) {
                    return;
                }

                wrapper.cancel(); // cancel current

                final String disconnectMessage = ChatColorUtil.translateAlternateColorCodes(Via.getConfig().getBlockedDisconnectMsg());
                final PacketWrapper disconnectPacket = PacketWrapper.create(ClientboundLoginPackets.LOGIN_DISCONNECT, user);
                disconnectPacket.write(Type.COMPONENT, ComponentUtil.plainToJson(disconnectMessage));

                // Send and close
                final ChannelFuture future = disconnectPacket.sendFuture(null);
                future.addListener(f -> user.getChannel().close());
            }
        });

        registerServerbound(ServerboundLoginPackets.LOGIN_ACKNOWLEDGED, wrapper -> {
            final ProtocolInfo info = wrapper.user().getProtocolInfo();
            info.setState(State.CONFIGURATION);
        });
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

    @Override
    protected PacketTypesProvider<BaseClientboundPacket, BaseClientboundPacket, BaseServerboundPacket, BaseServerboundPacket> createPacketTypesProvider() {
        return BasePacketTypesProvider.INSTANCE;
    }
}
