package us.myles.ViaVersion.protocols.base;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.platform.providers.ViaProviders;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.protocol.ProtocolPipeline;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;
import us.myles.ViaVersion.api.protocol.SimpleProtocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.Direction;
import us.myles.ViaVersion.packets.State;

import java.util.List;

public class BaseProtocol extends SimpleProtocol {

    @Override
    protected void registerPackets() {
        /* Incoming Packets */

        // Handshake Packet
        registerIncoming(State.HANDSHAKE, 0x00, 0x00, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    int protocolVersion = wrapper.passthrough(Type.VAR_INT);
                    wrapper.passthrough(Type.STRING); // Server Address
                    wrapper.passthrough(Type.UNSIGNED_SHORT); // Server Port
                    int state = wrapper.passthrough(Type.VAR_INT);

                    ProtocolInfo info = wrapper.user().getProtocolInfo();
                    info.setProtocolVersion(protocolVersion);
                    // Ensure the server has a version provider
                    if (Via.getManager().getProviders().get(VersionProvider.class) == null) {
                        wrapper.user().setActive(false);
                        return;
                    }

                    // Choose the pipe
                    int serverProtocol = Via.getManager().getProviders().get(VersionProvider.class).getServerProtocol(wrapper.user());
                    info.setServerProtocolVersion(serverProtocol);
                    List<Pair<Integer, Protocol>> protocols = null;

                    // Only allow newer clients or (1.9.2 on 1.9.4 server if the server supports it)
                    if (info.getProtocolVersion() >= serverProtocol || Via.getPlatform().isOldClientsAllowed()) {
                        protocols = ProtocolRegistry.getProtocolPath(info.getProtocolVersion(), serverProtocol);
                    }

                    ProtocolPipeline pipeline = wrapper.user().getProtocolInfo().getPipeline();
                    if (protocols != null) {
                        for (Pair<Integer, Protocol> prot : protocols) {
                            pipeline.add(prot.getValue());
                            // Ensure mapping data has already been loaded
                            ProtocolRegistry.completeMappingDataLoading(prot.getValue().getClass());
                        }

                        // Set the original snapshot version if present
                        ProtocolVersion protocol = ProtocolVersion.getProtocol(serverProtocol);
                        wrapper.set(Type.VAR_INT, 0, protocol.getOriginalId());
                    }

                    // Add Base Protocol
                    pipeline.add(ProtocolRegistry.getBaseProtocol(serverProtocol));

                    // Change state
                    if (state == 1) {
                        info.setState(State.STATUS);
                    }
                    if (state == 2) {
                        info.setState(State.LOGIN);
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
