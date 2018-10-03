package us.myles.ViaVersion.api.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.platform.providers.ViaProviders;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.exception.CancelException;
import us.myles.ViaVersion.packets.Direction;
import us.myles.ViaVersion.packets.State;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Protocol {
    private final Map<Pair<State, Integer>, ProtocolPacket> incoming = new HashMap<>();
    private final Map<Pair<State, Integer>, ProtocolPacket> outgoing = new HashMap<>();

    public Protocol() {
        registerPackets();
    }

    /**
     * Should this protocol filter an object packet from this class.
     * Default: false
     *
     * @param packetClass The class of the current input
     * @return True if it should handle the filtering
     */
    public boolean isFiltered(Class packetClass) {
        return false;
    }

    /**
     * Filter a packet into the output
     *
     * @param info   The current user connection
     * @param packet The input packet as an object (NMS)
     * @param output The list to put the object into.
     * @throws Exception Throws exception if cancelled / error.
     */
    protected void filterPacket(UserConnection info, Object packet, List output) throws Exception {
        output.add(packet);
    }

    /**
     * Register listeners for this protocol
     *
     * @deprecated No longer used as listeners are registered in {@link us.myles.ViaVersion.api.platform.ViaPlatformLoader#load}
     */
    @Deprecated
    protected void registerListeners() {

    }

    /**
     * Handle protocol registration phase, use this to register providers / tasks.
     *
     * @param providers The current providers
     */
    protected void register(ViaProviders providers) {

    }

    /**
     * Register the packets for this protocol
     */
    protected abstract void registerPackets();

    /**
     * Initialise a user for this protocol setting up objects.
     * /!\ WARNING - May be called more than once in a single {@link UserConnection}
     *
     * @param userConnection The user to initialise
     */
    public abstract void init(UserConnection userConnection);

    /**
     * Register an incoming packet, with simple id transformation.
     *
     * @param state       The state which the packet is sent in.
     * @param oldPacketID The old packet ID
     * @param newPacketID The new packet ID
     */
    public void registerIncoming(State state, int oldPacketID, int newPacketID) {
        registerIncoming(state, oldPacketID, newPacketID, null);
    }

    /**
     * Register an incoming packet, with id transformation and remapper.
     *
     * @param state          The state which the packet is sent in.
     * @param oldPacketID    The old packet ID
     * @param newPacketID    The new packet ID
     * @param packetRemapper The remapper to use for the packet
     */
    public void registerIncoming(State state, int oldPacketID, int newPacketID, PacketRemapper packetRemapper) {
        ProtocolPacket protocolPacket = new ProtocolPacket(state, oldPacketID, newPacketID, packetRemapper);
        incoming.put(new Pair<>(state, newPacketID), protocolPacket);
    }

    /**
     * Register an outgoing packet, with simple id transformation.
     *
     * @param state       The state which the packet is sent in.
     * @param oldPacketID The old packet ID
     * @param newPacketID The new packet ID
     */
    public void registerOutgoing(State state, int oldPacketID, int newPacketID) {
        registerOutgoing(state, oldPacketID, newPacketID, null);
    }

    /**
     * Register an outgoing packet, with id transformation and remapper.
     *
     * @param state          The state which the packet is sent in.
     * @param oldPacketID    The old packet ID
     * @param newPacketID    The new packet ID
     * @param packetRemapper The remapper to use for the packet
     */
    public void registerOutgoing(State state, int oldPacketID, int newPacketID, PacketRemapper packetRemapper) {
        ProtocolPacket protocolPacket = new ProtocolPacket(state, oldPacketID, newPacketID, packetRemapper);
        outgoing.put(new Pair<>(state, oldPacketID), protocolPacket);
    }

    /**
     * Transform a packet using this protocol
     *
     * @param direction     The direction the packet is going in
     * @param state         The current protocol state
     * @param packetWrapper The packet wrapper to transform
     * @throws Exception Throws exception if it fails to transform
     */
    public void transform(Direction direction, State state, PacketWrapper packetWrapper) throws Exception {
        Pair<State, Integer> statePacket = new Pair<>(state, packetWrapper.getId());
        Map<Pair<State, Integer>, ProtocolPacket> packetMap = (direction == Direction.OUTGOING ? outgoing : incoming);
        ProtocolPacket protocolPacket = packetMap.get(statePacket);
        if (protocolPacket == null) {
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
