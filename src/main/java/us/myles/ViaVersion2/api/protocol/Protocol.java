package us.myles.ViaVersion2.api.protocol;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import us.myles.ViaVersion.packets.Direction;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion2.api.PacketWrapper;
import us.myles.ViaVersion2.api.data.UserConnection;
import us.myles.ViaVersion2.api.protocol.base.BaseProtocol;
import us.myles.ViaVersion2.api.remapper.PacketRemapper;
import us.myles.ViaVersion2.api.type.Type;
import us.myles.ViaVersion2.api.util.Pair;

import java.util.HashMap;
import java.util.Map;

public abstract class Protocol {
    public static final Protocol BASE_PROTOCOL = new BaseProtocol();

    private Map<Pair<State, Integer>, ProtocolPacket> incoming = new HashMap<>();
    private Map<Pair<State, Integer>, ProtocolPacket> outgoing = new HashMap<>();

    public Protocol() {
        registerPackets();
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

    public void transform(Direction direction, State state, int packetID, PacketWrapper packetWrapper, ByteBuf output) {
        Pair<State, Integer> statePacket = new Pair<>(state, packetID);
        Map<Pair<State, Integer>, ProtocolPacket> packetMap = (direction == Direction.OUTGOING ? outgoing : incoming);
        ProtocolPacket protocolPacket;
        if (packetMap.containsKey(statePacket)) {
            protocolPacket = packetMap.get(statePacket);
        } else {
            System.out.println("Packet not found: " + packetID);
            // simply translate
            Type.VAR_INT.write(output, packetID);
            // pass through
            packetWrapper.writeRemaining(output);
            return;
        }
        // write packet id
        Type.VAR_INT.write(output, direction == Direction.OUTGOING ? protocolPacket.getNewID() : protocolPacket.getOldID());
        // remap
        if (protocolPacket.getRemapper() != null) {
            protocolPacket.getRemapper().remap(packetWrapper);
            // write to output
            packetWrapper.writeToBuffer(output);
        }
        // pass through
        packetWrapper.writeRemaining(output);
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
