package us.myles.ViaVersion.api.protocol;

import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.packets.Direction;
import us.myles.ViaVersion.packets.PacketType;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.base.BaseProtocol;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

public class ProtocolPipeline extends Protocol {
    LinkedList<Protocol> protocolList;
    private UserConnection userConnection;

    public ProtocolPipeline(UserConnection userConnection) {
        super();
        init(userConnection);
    }

    @Override
    protected void registerPackets() {
        protocolList = new LinkedList<>();
        // This is a pipeline so we register basic pipes
        protocolList.addLast(ProtocolRegistry.BASE_PROTOCOL);
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
     * This will call the .init method.
     *
     * @param protocol The protocol to add to the end
     */
    public void add(Protocol protocol) {
        if (protocolList != null) {
            protocolList.addLast(protocol);
            protocol.init(userConnection);
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

        for (Protocol protocol : protocols) { // Copy to prevent from removal.
            protocol.transform(direction, state, packetWrapper);
            // Reset the reader for the packetWrapper (So it can be recycled across packets)
            packetWrapper.resetReader();
        }
        super.transform(direction, state, packetWrapper);

        if (ViaVersion.getInstance().isDebug()) {
            // Debug packet
            String packet = "UNKNOWN";

            // For 1.8/1.9 server version, eventually we'll probably get an API for this...
            if (ProtocolRegistry.SERVER_PROTOCOL >= ProtocolVersion.v1_8.getId() &&
                    ProtocolRegistry.SERVER_PROTOCOL <= ProtocolVersion.v1_9_2.getId()) {

                PacketType type;
                if (ProtocolRegistry.SERVER_PROTOCOL == ProtocolVersion.v1_8.getId()) {
                    if (direction == Direction.INCOMING) {
                        type = PacketType.findNewPacket(state, direction, originalID);
                    } else {
                        type = PacketType.findOldPacket(state, direction, originalID);
                    }
                } else {
                    if (direction == Direction.INCOMING) {
                        type = PacketType.findOldPacket(state, direction, originalID);
                    } else {
                        type = PacketType.findNewPacket(state, direction, originalID);
                    }
                }

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
            String name = packet + "[" + userConnection.get(ProtocolInfo.class).getProtocolVersion() + "]";
            ViaVersionPlugin plugin = (ViaVersionPlugin) ViaVersion.getInstance();
            plugin.getLogger().log(Level.INFO, "{0}: {1} {2} -> {3} [{4}]",
                    new Object[]{
                            direction,
                            state,
                            originalID,
                            packetWrapper.getId(),
                            name
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
}
