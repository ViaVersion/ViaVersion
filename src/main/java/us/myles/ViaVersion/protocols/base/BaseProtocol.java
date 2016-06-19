package us.myles.ViaVersion.protocols.base;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.protocol.ProtocolPipeline;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.Direction;
import us.myles.ViaVersion.packets.State;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class BaseProtocol extends Protocol {

    @Override
    protected void registerPackets() {
        /* Outgoing Packets */
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
                            JSONObject json = (JSONObject) new JSONParser().parse(originalStatus);
                            JSONObject version = (JSONObject) json.get("version");
                            if (ViaVersion.getConfig().isSendSupportedVersions())
                                version.put("supportedVersions", ViaVersion.getInstance().getSupportedVersions());
                            if (ProtocolRegistry.SERVER_PROTOCOL == -1) {
                                Long original = (Long) version.get("protocol");
                                ProtocolRegistry.SERVER_PROTOCOL = original.intValue();
                            }
                            List<Pair<Integer, Protocol>> protocols = ProtocolRegistry.getProtocolPath(info.getProtocolVersion(), ProtocolRegistry.SERVER_PROTOCOL);
                            if (protocols != null) {
                                if ((long) version.get("protocol") != 9999) //Fix serverlistplus
                                    version.put("protocol", info.getProtocolVersion());
                            } else {
                                // not compatible :(, *plays very sad violin*
                                wrapper.user().setActive(false);
                            }
                            wrapper.set(Type.STRING, 0, json.toJSONString()); // Update value
                        } catch (ParseException e) {
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
                        UUID uuid = UUID.fromString(wrapper.get(Type.STRING, 0));
                        info.setUuid(uuid);
                        info.setUsername(wrapper.get(Type.STRING, 1));
                        // Add to ported clients
                        ((ViaVersionPlugin) ViaVersion.getInstance()).addPortedClient(wrapper.user());
                        if (ViaVersion.getInstance().isDebug()) {
                            // Print out the route to console
                            ((ViaVersionPlugin) ViaVersion.getInstance()).getLogger().log(Level.INFO, "{0} logged in with protocol {1}, Route: {2}",
                                    new Object[]{
                                            wrapper.get(Type.STRING, 1),
                                            info.getProtocolVersion(),
                                            StringUtils.join(info.getPipeline().pipes(), ", ")
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
                        // Choose the pipe
                        List<Pair<Integer, Protocol>> protocols = ProtocolRegistry.getProtocolPath(info.getProtocolVersion(), ProtocolRegistry.SERVER_PROTOCOL);
                        ProtocolPipeline pipeline = wrapper.user().get(ProtocolInfo.class).getPipeline();
                        if (protocols != null) {
                            for (Pair<Integer, Protocol> prot : protocols) {
                                pipeline.add(prot.getValue());
                            }
                            wrapper.set(Type.VAR_INT, 0, ProtocolRegistry.SERVER_PROTOCOL);
                        } else {
                            if (state == 2) {
                                // not compatible :(, *plays very sad violin*
                                wrapper.user().setActive(false);
                            }
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

        registerIncoming(State.LOGIN, 0x00, 0x00); // Login Start Packet
        registerIncoming(State.LOGIN, 0x01, 0x01); // Encryption Response Packet
    }

    @Override
    public void init(UserConnection userConnection) {
        // Nothing gets added, ProtocolPipeline handles ProtocolInfo
    }

    @Override
    protected void registerListeners() {
        final ViaVersionPlugin plugin = (ViaVersionPlugin) Bukkit.getPluginManager().getPlugin("ViaVersion");

        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent e) {
                plugin.removePortedClient(e.getPlayer().getUniqueId());
            }
        }, plugin);
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
}
