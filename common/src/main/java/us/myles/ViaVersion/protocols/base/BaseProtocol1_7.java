package us.myles.ViaVersion.protocols.base;

import com.google.common.base.Joiner;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.netty.channel.ChannelFuture;
import net.md_5.bungee.api.ChatColor;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import us.myles.ViaVersion.util.GsonUtil;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class BaseProtocol1_7 extends Protocol {

    @Override
    protected void registerPackets() {
        /* Outgoing Packets */

        // Status Response Packet
        registerOutgoing(State.STATUS, 0x00, 0x00, new PacketRemapper() { // Status Response Packet
            @Override
            public void registerMap() {
                map(Type.STRING);
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        ProtocolInfo info = wrapper.user().get(ProtocolInfo.class);
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
                                    version = new JsonObject();
                                    json.getAsJsonObject().add("version", version);
                                }
                            } else {
                                // Format properly
                                json = new JsonObject();
                                version = new JsonObject();
                                json.getAsJsonObject().add("version", version);
                            }
                            if (Via.getConfig().isSendSupportedVersions()) //Send supported versions
                                version.add("supportedVersions", GsonUtil.getGson().toJsonTree(Via.getAPI().getSupportedVersions()));

                            if (ProtocolRegistry.SERVER_PROTOCOL == -1) // Set the Server protocol if the detection on startup failed
                                ProtocolRegistry.SERVER_PROTOCOL = protocolVersion;
                            // Ensure the server has a version provider
                            if (Via.getManager().getProviders().get(VersionProvider.class) == null) {
                                wrapper.user().setActive(false);
                                return;
                            }
                            int protocol = Via.getManager().getProviders().get(VersionProvider.class).getServerProtocol(wrapper.user());
                            List<Pair<Integer, Protocol>> protocols = null;

                            // Only allow newer clients or (1.9.2 on 1.9.4 server if the server supports it)
                            if (info.getProtocolVersion() >= protocol || Via.getPlatform().isOldClientsAllowed()) {
                                protocols = ProtocolRegistry.getProtocolPath(info.getProtocolVersion(), protocol);
                            }

                            if (protocols != null) {
                                if (protocolVersion == protocol || protocolVersion == 0) {
                                    //Fix ServerListPlus
                                    version.addProperty("protocol", info.getProtocolVersion());
                                }
                            } else {
                                // not compatible :(, *plays very sad violin*
                                wrapper.user().setActive(false);
                            }

                            if (Via.getConfig().getBlockedProtocols().contains(info.getProtocolVersion()))
                                version.addProperty("protocol", -1); // Show blocked versions as outdated

                            wrapper.set(Type.STRING, 0, GsonUtil.getGson().toJson(json)); // Update value
                        } catch (JsonParseException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        registerOutgoing(State.STATUS, 0x01, 0x01); // Status Pong Packet

        registerOutgoing(State.LOGIN, 0x00, 0x00); // Login Disconnect Packet
        registerOutgoing(State.LOGIN, 0x01, 0x01); // Encryption Request Packet

        // Login Success Packet
        registerOutgoing(State.LOGIN, 0x02, 0x02, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        ProtocolInfo info = wrapper.user().get(ProtocolInfo.class);
                        info.setState(State.PLAY);

                        if (info.getServerProtocolVersion() >= ProtocolVersion.v1_16.getId()) {
                            // 1.16+ uses int arrays
                            UUID uuid = wrapper.passthrough(Type.UUID_INT_ARRAY);
                            info.setUuid(uuid);
                        } else {
                            // Save other info
                            String stringUUID = wrapper.passthrough(Type.STRING);
                            if (stringUUID.length() == 32) { // Trimmed UUIDs are 32 characters
                                // Trimmed
                                stringUUID = addDashes(stringUUID);
                            }
                            UUID uuid = UUID.fromString(stringUUID);
                            info.setUuid(uuid);
                        }

                        String username = wrapper.passthrough(Type.STRING);
                        info.setUsername(username);
                        // Add to ported clients
                        Via.getManager().addPortedClient(wrapper.user());

                        if (info.getPipeline().pipes().size() == 2
                                && info.getPipeline().pipes().get(1).getClass() == BaseProtocol1_7.class
                                && info.getPipeline().pipes().get(0).getClass() == BaseProtocol.class) // Only base protocol
                            wrapper.user().setActive(false);

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

        registerOutgoing(State.LOGIN, 0x03, 0x03); // Login Set Compression Packet
        registerIncoming(State.LOGIN, 0x04, 0x04); // Plugin Request (1.13)

        /* Incoming Packets */

        registerIncoming(State.STATUS, 0x00, 0x00); // Status Request Packet
        registerIncoming(State.STATUS, 0x01, 0x01); // Status Ping Packet

        // Login Start Packet
        registerIncoming(State.LOGIN, 0x00, 0x00, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(final PacketWrapper wrapper) throws Exception {
                        int protocol = wrapper.user().get(ProtocolInfo.class).getProtocolVersion();
                        if (Via.getConfig().getBlockedProtocols().contains(protocol)) {
                            if (!wrapper.user().getChannel().isOpen()) return;

                            PacketWrapper disconnectPacket = new PacketWrapper(0x00, null, wrapper.user()); // Disconnect Packet
                            Protocol1_9To1_8.FIX_JSON.write(disconnectPacket, ChatColor.translateAlternateColorCodes('&', Via.getConfig().getBlockedDisconnectMsg()));
                            wrapper.cancel(); // cancel current

                            // Send and close
                            ChannelFuture future = disconnectPacket.sendFuture(BaseProtocol.class);
                            future.addListener(f -> wrapper.user().getChannel().close());
                        }
                    }
                });
            }
        }); // Login Start Packet
        registerIncoming(State.LOGIN, 0x01, 0x01); // Encryption Response Packet
        registerIncoming(State.LOGIN, 0x02, 0x02); // Plugin Response (1.13)
    }

    @Override
    public void init(UserConnection userConnection) {

    }

    public static String addDashes(String trimmedUUID) {
        StringBuilder idBuff = new StringBuilder(trimmedUUID);
        idBuff.insert(20, '-');
        idBuff.insert(16, '-');
        idBuff.insert(12, '-');
        idBuff.insert(8, '-');
        return idBuff.toString();
    }
}
