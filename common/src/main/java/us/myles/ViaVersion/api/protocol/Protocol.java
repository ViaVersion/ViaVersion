package us.myles.ViaVersion.api.protocol;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.platform.providers.ViaProviders;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.exception.CancelException;
import us.myles.ViaVersion.packets.Direction;
import us.myles.ViaVersion.packets.State;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public abstract class Protocol {
    private final Map<Pair<State, Integer>, ProtocolPacket> incoming = new HashMap<>();
    private final Map<Pair<State, Integer>, ProtocolPacket> outgoing = new HashMap<>();

    private final Map<Class, Object> storedObjects = new ConcurrentHashMap<>();

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
        registerIncoming(state, oldPacketID, newPacketID, packetRemapper, false);
    }

    public void registerIncoming(State state, int oldPacketID, int newPacketID, PacketRemapper packetRemapper, boolean override) {
        ProtocolPacket protocolPacket = new ProtocolPacket(state, oldPacketID, newPacketID, packetRemapper);
        Pair<State, Integer> pair = new Pair<>(state, newPacketID);
        if (!override && incoming.containsKey(pair)) {
            Via.getPlatform().getLogger().log(Level.WARNING, pair + " already registered!" +
                    " If this override is intentional, set override to true. Stacktrace: ", new Exception());
        }
        incoming.put(pair, protocolPacket);
    }

    public void cancelIncoming(State state, int oldPacketID, int newPacketID) {
        registerIncoming(state, oldPacketID, newPacketID, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(PacketWrapper::cancel);
            }
        });
    }

    public void cancelIncoming(State state, int newPacketID) {
        cancelIncoming(state, -1, newPacketID);
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
        registerOutgoing(state, oldPacketID, newPacketID, packetRemapper, false);
    }

    public void registerOutgoing(State state, int oldPacketID, int newPacketID, PacketRemapper packetRemapper, boolean override) {
        ProtocolPacket protocolPacket = new ProtocolPacket(state, oldPacketID, newPacketID, packetRemapper);
        Pair<State, Integer> pair = new Pair<>(state, oldPacketID);
        if (!override && outgoing.containsKey(pair)) {
            Via.getPlatform().getLogger().log(Level.WARNING, pair + " already registered!" +
                    " If override is intentional, set override to true. Stacktrace: ", new Exception());
        }
        outgoing.put(pair, protocolPacket);
    }

    public void cancelOutgoing(State state, int oldPacketID, int newPacketID) {
        registerOutgoing(state, oldPacketID, newPacketID, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(PacketWrapper::cancel);
            }
        });
    }

    public void cancelOutgoing(State state, int oldPacketID) {
        cancelOutgoing(state, oldPacketID, -1);
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
            if (packetWrapper.isCancelled()) {
                throw Via.getManager().isDebug() ? new CancelException() : CancelException.CACHED;
            }
        }
    }

    public <T> T get(Class<T> objectClass) {
        return (T) storedObjects.get(objectClass);
    }

    public void put(Object object) {
        storedObjects.put(object.getClass(), object);
    }

    @Override
    public String toString() {
        return "Protocol:" + getClass().getSimpleName();
    }

    public static class ProtocolPacket {
        private final State state;
        private final int oldID;
        private final int newID;
        private final PacketRemapper remapper;

        public ProtocolPacket(State state, int oldID, int newID, PacketRemapper remapper) {
            this.state = state;
            this.oldID = oldID;
            this.newID = newID;
            this.remapper = remapper;
        }

        public State getState() {
            return state;
        }

        public int getOldID() {
            return oldID;
        }

        public int getNewID() {
            return newID;
        }

        public PacketRemapper getRemapper() {
            return remapper;
        }
    }
}
