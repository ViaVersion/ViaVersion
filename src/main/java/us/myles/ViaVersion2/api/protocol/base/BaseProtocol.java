package us.myles.ViaVersion2.api.protocol.base;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.util.PacketUtil;
import us.myles.ViaVersion2.api.PacketWrapper;
import us.myles.ViaVersion2.api.data.UserConnection;
import us.myles.ViaVersion2.api.protocol.Protocol;
import us.myles.ViaVersion2.api.remapper.PacketHandler;
import us.myles.ViaVersion2.api.remapper.PacketRemapper;
import us.myles.ViaVersion2.api.type.Type;

import java.util.UUID;

public class BaseProtocol extends Protocol {

    @Override
    protected void registerPackets() {
        /* Outgoing Packets */
        registerOutgoing(State.STATUS, 0x00, 0x00, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING);
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) {
                        // TODO: Actually make this show compatible versions
                        ProtocolInfo info = wrapper.user().get(ProtocolInfo.class);
                        String originalStatus = wrapper.get(Type.STRING, 0);
                        try {
                            JSONObject json = (JSONObject) new JSONParser().parse(originalStatus);
                            JSONObject version = (JSONObject) json.get("version");
                            version.put("protocol", info.getProtocolVersion());

                            wrapper.set(Type.STRING, 0, json.toJSONString()); // Update value
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }); // Status Response Packet
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
                    public void handle(PacketWrapper wrapper) {
                        ProtocolInfo info = wrapper.user().get(ProtocolInfo.class);
                        info.setState(State.PLAY);
                        UUID uuid = UUID.fromString(wrapper.get(Type.STRING, 0));
                        info.setUuid(uuid);
                        info.setUsername(wrapper.get(Type.STRING, 1));
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
                    public void handle(PacketWrapper wrapper) {
                        int protVer = wrapper.get(Type.VAR_INT, 0);
                        ProtocolInfo info = wrapper.user().get(ProtocolInfo.class);
                        info.setProtocolVersion(protVer);
                        // TODO: Choose the right pipe

                        // Change state
                        int state = wrapper.get(Type.VAR_INT, 1);
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
}
