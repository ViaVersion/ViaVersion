package us.myles.ViaVersion.api.protocol;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.platform.ViaPlatform;
import us.myles.ViaVersion.packets.Direction;
import us.myles.ViaVersion.packets.PacketType;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

public class ProtocolPipeline extends Protocol {
    List<Protocol> protocolList;
    private UserConnection userConnection;

    public ProtocolPipeline(UserConnection userConnection) {
        super();
        init(userConnection);
    }

    @Override
    protected void registerPackets() {
        protocolList = new CopyOnWriteArrayList<>();
        // This is a pipeline so we register basic pipes
        protocolList.add(ProtocolRegistry.BASE_PROTOCOL);
    }

    @Override
    public void init(UserConnection userConnection) {
        this.userConnection = userConnection;

        ProtocolInfo protocolInfo = new ProtocolInfo(userConnection);
        protocolInfo.setPipeline(this);

        userConnection.put(protocolInfo);

        /* Init through all our pipes */
        for (Protocol protocol : protocolList) {
            protocol.init(userConnection);
        }
    }

    /**
     * Add a protocol to the current pipeline
     * This will call the Protocol#init method.
     *
     * @param protocol The protocol to add to the end
     */
    public void add(Protocol protocol) {
        if (protocolList != null) {
            protocolList.add(protocol);
            protocol.init(userConnection);
            // Move BaseProtocol to end, so the login packets can be modified by other protocols
            protocolList.remove(ProtocolRegistry.BASE_PROTOCOL);
            protocolList.add(ProtocolRegistry.BASE_PROTOCOL);
        } else {
            throw new NullPointerException("Tried to add protocol to early");
        }
    }

    @Override
    public void transform(Direction direction, State state, PacketWrapper packetWrapper) throws Exception {
        int originalID = packetWrapper.getId();
        List<Protocol> protocols = new ArrayList<>(protocolList);
        // Other way if outgoing
        if (direction == Direction.OUTGOING)
            Collections.reverse(protocols);

        // Apply protocols
        packetWrapper.apply(direction, state, 0, protocols);
        super.transform(direction, state, packetWrapper);

        if (Via.getManager().isDebug()) {
            // Debug packet
            String packet = "UNKNOWN";


            int serverProtocol = userConnection.get(ProtocolInfo.class).getServerProtocolVersion();
            int clientProtocol = userConnection.get(ProtocolInfo.class).getProtocolVersion();

            // For 1.8/1.9 server version, eventually we'll probably get an API for this...
            if (serverProtocol >= ProtocolVersion.v1_8.getId() &&
                    serverProtocol <= ProtocolVersion.v1_9_3.getId()) {
                PacketType type;
                if (serverProtocol <= ProtocolVersion.v1_8.getId()) {
                    if (direction == Direction.INCOMING) {
                        type = PacketType.findNewPacket(state, direction, originalID);
                    } else {
                        type = PacketType.findOldPacket(state, direction, originalID);
                    }
                } else {
                    type = PacketType.findNewPacket(state, direction, originalID);
                }
                if (type != null) {
                    // Filter :) This would be not hard coded too, sorry :(
                    if (type == PacketType.PLAY_CHUNK_DATA) return;
                    if (type == PacketType.PLAY_TIME_UPDATE) return;
                    if (type == PacketType.PLAY_KEEP_ALIVE) return;
                    if (type == PacketType.PLAY_KEEP_ALIVE_REQUEST) return;
                    if (type == PacketType.PLAY_ENTITY_LOOK_MOVE) return;
                    if (type == PacketType.PLAY_ENTITY_LOOK) return;
                    if (type == PacketType.PLAY_ENTITY_RELATIVE_MOVE) return;
                    if (type == PacketType.PLAY_PLAYER_POSITION_LOOK_REQUEST) return;
                    if (type == PacketType.PLAY_PLAYER_LOOK_REQUEST) return;
                    if (type == PacketType.PLAY_PLAYER_POSITION_REQUEST) return;

                    packet = type.name();
                }
            }
            String name = packet + "[" + clientProtocol + "]";
            ViaPlatform platform = Via.getPlatform();

            String actualUsername = packetWrapper.user().get(ProtocolInfo.class).getUsername();
            String username = actualUsername != null ? actualUsername + " " : "";

            platform.getLogger().log(Level.INFO, "{0}{1}: {2} {3} -> {4} [{5}] Value: {6}",
                    new Object[]{
                            username,
                            direction,
                            state,
                            originalID,
                            packetWrapper.getId(),
                            name,
                            packetWrapper
                    });
        }
    }

    /**
     * Check if the pipeline contains a protocol
     *
     * @param pipeClass The class to check
     * @return True if the protocol class is in the pipeline
     */
    public boolean contains(Class<? extends Protocol> pipeClass) {
        for (Protocol protocol : protocolList) {
            if (protocol.getClass().equals(pipeClass)) return true;
        }
        return false;
    }

    /**
     * Use the pipeline to filter a NMS packet
     *
     * @param o    The NMS packet object
     * @param list The output list to write to
     * @return If it should not write the input object to te list.
     * @throws Exception If it failed to convert / packet cancelld.
     */
    public boolean filter(Object o, List list) throws Exception {
        for (Protocol protocol : protocolList) {
            if (protocol.isFiltered(o.getClass())) {
                protocol.filterPacket(userConnection, o, list);
                return true;
            }
        }

        return false;
    }

    public List<Protocol> pipes() {
        return protocolList;
    }

    public void cleanPipes() {
        pipes().clear();
        registerPackets();
    }
}
