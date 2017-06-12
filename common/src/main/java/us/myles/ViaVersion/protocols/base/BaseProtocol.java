package us.myles.ViaVersion.protocols.base;

import com.google.common.base.Joiner;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.md_5.bungee.api.ChatColor;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.platform.providers.ViaProviders;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.protocol.ProtocolPipeline;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.Direction;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9TO1_8;
import us.myles.ViaVersion.util.GsonUtil;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class BaseProtocol extends Protocol {

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
                                if (protocolVersion != 9999) {
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
                map(Type.STRING); // 0 - UUID as String
                map(Type.STRING); // 1 - Player Username
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        ProtocolInfo info = wrapper.user().get(ProtocolInfo.class);
                        info.setState(State.PLAY);
                        // Save other info
                        String stringUUID = wrapper.get(Type.STRING, 0);
                        if (stringUUID.length() == 32) { // Trimmed UUIDs are 32 characters
                            // Trimmed
                            stringUUID = addDashes(stringUUID);
                        }
                        UUID uuid = UUID.fromString(stringUUID);
                        info.setUuid(uuid);
                        info.setUsername(wrapper.get(Type.STRING, 1));
                        // Add to ported clients
                        Via.getManager().addPortedClient(wrapper.user());

                        if (info.getPipeline().pipes().size() == 1 && info.getPipeline().pipes().get(0).getClass() == BaseProtocol.class) // Only base protocol
                            wrapper.user().setActive(false);

                        if (Via.getManager().isDebug()) {
                            // Print out the route to console
                            Via.getPlatform().getLogger().log(Level.INFO, "{0} logged in with protocol {1}, Route: {2}",
                                    new Object[]{
                                            wrapper.get(Type.STRING, 1),
                                            info.getProtocolVersion(),
                                            Joiner.on(", ").join(info.getPipeline().pipes(), ", ")
                                    });
                        }
                    }
                });
            }
        });

        registerOutgoing(State.LOGIN, 0x03, 0x03); // Login Set Compression Packet
        /* Incoming Packets */

        // Handshake Packet
        registerIncoming(State.HANDSHAKE, 0x00, 0x00, new PacketRemapper() {
            @Override
            public void registerMap() {
                // select right protocol
                map(Type.VAR_INT); // 0 - Client Protocol Version
                map(Type.STRING); // 1 - Server Address
                map(Type.UNSIGNED_SHORT); // 2 - Server Port
                map(Type.VAR_INT); // 3 - Next State
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int protVer = wrapper.get(Type.VAR_INT, 0);
                        int state = wrapper.get(Type.VAR_INT, 1);

                        ProtocolInfo info = wrapper.user().get(ProtocolInfo.class);
                        info.setProtocolVersion(protVer);
                        // Ensure the server has a version provider
                        if (Via.getManager().getProviders().get(VersionProvider.class) == null) {
                            wrapper.user().setActive(false);
                            return;
                        }
                        // Choose the pipe
                        int protocol = Via.getManager().getProviders().get(VersionProvider.class).getServerProtocol(wrapper.user());
                        List<Pair<Integer, Protocol>> protocols = null;

                        // Only allow newer clients or (1.9.2 on 1.9.4 server if the server supports it)
                        if (info.getProtocolVersion() >= protocol || Via.getPlatform().isOldClientsAllowed()) {
                            protocols = ProtocolRegistry.getProtocolPath(info.getProtocolVersion(), protocol);
                        }

                        ProtocolPipeline pipeline = wrapper.user().get(ProtocolInfo.class).getPipeline();
                        if (protocols != null) {
                            for (Pair<Integer, Protocol> prot : protocols) {
                                pipeline.add(prot.getValue());
                            }
                            wrapper.set(Type.VAR_INT, 0, protocol);
                        }

                        // Change state
                        if (state == 1) {
                            info.setState(State.STATUS);
                        }
                        if (state == 2) {
                            info.setState(State.LOGIN);
                        }
                    }
                });
            }
        });
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
                            Protocol1_9TO1_8.FIX_JSON.write(disconnectPacket, ChatColor.translateAlternateColorCodes('&', Via.getConfig().getBlockedDisconnectMsg()));
                            wrapper.cancel(); // cancel current

                            // Send and close
                            ChannelFuture future = disconnectPacket.sendFuture(BaseProtocol.class);
                            future.addListener(new GenericFutureListener<Future<? super Void>>() {
                                @Override
                                public void operationComplete(Future<? super Void> future) throws Exception {
                                    wrapper.user().getChannel().close();
                                }
                            });
                        }
                    }
                });
            }
        }); // Login Start Packet
        registerIncoming(State.LOGIN, 0x01, 0x01); // Encryption Response Packet
    }

    @Override
    public void init(UserConnection userConnection) {
        // Nothing gets added, ProtocolPipeline handles ProtocolInfo
    }

    @Override
    protected void register(ViaProviders providers) {
        providers.register(VersionProvider.class, new VersionProvider());
    }

    @Override
    public void transform(Direction direction, State state, PacketWrapper packetWrapper) throws Exception {
        super.transform(direction, state, packetWrapper);
        if (direction == Direction.INCOMING && state == State.HANDSHAKE) {
            // Disable if it isn't a handshake packet.
            if (packetWrapper.getId() > 0) {
                packetWrapper.user().setActive(false);
            }
        }
    }

    public static String addDashes(String trimmedUUID) {
        StringBuffer idBuff = new StringBuffer(trimmedUUID);
        idBuff.insert(20, '-');
        idBuff.insert(16, '-');
        idBuff.insert(12, '-');
        idBuff.insert(8, '-');
        return idBuff.toString();
    }
}
