package us.myles.ViaVersion.protocols.base;

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

import java.util.List;

public class BaseProtocol extends Protocol {

    @Override
    protected void registerPackets() {
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

                        wrapper.user().getChannel().attr(Via.getPlatform().getVersionAttributeKey()).set(protVer);

                        ProtocolInfo info = wrapper.user().get(ProtocolInfo.class);
                        info.setProtocolVersion(protVer);
                        // Ensure the server has a version provider
                        if (Via.getManager().getProviders().get(VersionProvider.class) == null) {
                            wrapper.user().setActive(false);
                            return;
                        }
                        // Choose the pipe
                        int protocol = Via.getManager().getProviders().get(VersionProvider.class).getServerProtocol(wrapper.user());
                        info.setServerProtocolVersion(protocol);
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

                        // Add Base Protocol
                        pipeline.add(ProtocolRegistry.getBaseProtocol(protocol));

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
            if (packetWrapper.getId() != 0) {
                packetWrapper.user().setActive(false);
            }
        }
    }
}
