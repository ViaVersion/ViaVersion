package us.myles.ViaVersion.api.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;
import us.myles.ViaVersion.CancelException;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.packets.Direction;
import us.myles.ViaVersion.packets.State;

import java.util.HashMap;
import java.util.Map;

public abstract class Protocol {
    private Map<Pair<State, Integer>, ProtocolPacket> incoming = new HashMap<>();
    private Map<Pair<State, Integer>, ProtocolPacket> outgoing = new HashMap<>();

    public Protocol() {
        registerPackets();
        registerListeners();
    }

    protected void registerListeners() {

    }

    protected abstract void registerPackets();

    public abstract void init(UserConnection userConnection);

    public void registerIncoming(State state, int oldPacketID, int newPacketID) {
        registerIncoming(state, oldPacketID, newPacketID, null);
    }

    public void registerIncoming(State state, int oldPacketID, int newPacketID, PacketRemapper packetRemapper) {
        ProtocolPacket protocolPacket = new ProtocolPacket(state, oldPacketID, newPacketID, packetRemapper);
        incoming.put(new Pair<>(state, newPacketID), protocolPacket);
    }

    public void registerOutgoing(State state, int oldPacketID, int newPacketID) {
        registerOutgoing(state, oldPacketID, newPacketID, null);
    }

    public void registerOutgoing(State state, int oldPacketID, int newPacketID, PacketRemapper packetRemapper) {
        ProtocolPacket protocolPacket = new ProtocolPacket(state, oldPacketID, newPacketID, packetRemapper);
        outgoing.put(new Pair<>(state, oldPacketID), protocolPacket);
    }

    public void transform(Direction direction, State state, PacketWrapper packetWrapper) throws Exception {
        Pair<State, Integer> statePacket = new Pair<>(state, packetWrapper.getId());
        Map<Pair<State, Integer>, ProtocolPacket> packetMap = (direction == Direction.OUTGOING ? outgoing : incoming);
        ProtocolPacket protocolPacket;
        if (packetMap.containsKey(statePacket)) {
            protocolPacket = packetMap.get(statePacket);
        } else {
            return;
        }
        // write packet id
        int newID = direction == Direction.OUTGOING ? protocolPacket.getNewID() : protocolPacket.getOldID();
        packetWrapper.setId(newID);
        // remap
        if (protocolPacket.getRemapper() != null) {
            protocolPacket.getRemapper().remap(packetWrapper);
            if (packetWrapper.isCancelled())
                throw new CancelException();
        }
    }

    @Override
    public String toString() {
        return "Protocol:" + getClass().getSimpleName();
    }

    @AllArgsConstructor
    @Getter
    class ProtocolPacket {
        State state;
        int oldID;
        int newID;
        PacketRemapper remapper;
    }
}
