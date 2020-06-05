package us.myles.ViaVersion.api.protocol;

import com.google.common.base.Preconditions;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.platform.providers.ViaProviders;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.exception.CancelException;
import us.myles.ViaVersion.packets.Direction;
import us.myles.ViaVersion.packets.State;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public abstract class Protocol {
    private final Map<Packet, ProtocolPacket> incoming = new HashMap<>();
    private final Map<Packet, ProtocolPacket> outgoing = new HashMap<>();
    private final Map<Class, Object> storedObjects = new HashMap<>(); // currently only used for MetadataRewriters
    protected final Class<? extends ClientboundPacketType> oldClientboundPacketEnum;
    protected final Class<? extends ClientboundPacketType> newClientboundPacketEnum;
    protected final Class<? extends ServerboundPacketType> oldServerboundPacketEnum;
    protected final Class<? extends ServerboundPacketType> newServerboundPacketEnum;
    protected final boolean hasMappingDataToLoad;

    protected Protocol() {
        this(null, null, null, null, false);
    }

    protected Protocol(boolean hasMappingDataToLoad) {
        this(null, null, null, null, hasMappingDataToLoad);
    }

    /**
     * Creates a protocol with automated id mapping if the respective enums are not null.
     */
    protected Protocol(Class<? extends ClientboundPacketType> oldClientboundPacketEnum, Class<? extends ClientboundPacketType> clientboundPacketEnum,
                       Class<? extends ServerboundPacketType> oldServerboundPacketEnum, Class<? extends ServerboundPacketType> serverboundPacketEnum) {
        this(oldClientboundPacketEnum, clientboundPacketEnum, oldServerboundPacketEnum, serverboundPacketEnum, false);
    }

    /**
     * Creates a protocol with automated id mapping if the respective enums are not null.
     *
     * @param hasMappingDataToLoad whether an async executor should call the {@Link #loadMappingData} method
     */
    protected Protocol(Class<? extends ClientboundPacketType> oldClientboundPacketEnum, Class<? extends ClientboundPacketType> clientboundPacketEnum,
                       Class<? extends ServerboundPacketType> oldServerboundPacketEnum, Class<? extends ServerboundPacketType> serverboundPacketEnum, boolean hasMappingDataToLoad) {
        this.oldClientboundPacketEnum = oldClientboundPacketEnum;
        this.newClientboundPacketEnum = clientboundPacketEnum;
        this.oldServerboundPacketEnum = oldServerboundPacketEnum;
        this.newServerboundPacketEnum = serverboundPacketEnum;
        this.hasMappingDataToLoad = hasMappingDataToLoad;
        registerPackets();

        // Register the rest of the ids with no handlers if necessary
        if (oldClientboundPacketEnum != null && clientboundPacketEnum != null
                && oldClientboundPacketEnum != clientboundPacketEnum) {
            registerOutgoingChannelIdChanges();
        }
        if (oldServerboundPacketEnum != null && serverboundPacketEnum != null
                && oldServerboundPacketEnum != serverboundPacketEnum) {
            registerIncomingChannelIdChanges();
        }
    }

    protected void registerOutgoingChannelIdChanges() {
        ClientboundPacketType[] newConstants = newClientboundPacketEnum.getEnumConstants();
        Map<String, ClientboundPacketType> newClientboundPackets = new HashMap<>(newConstants.length);
        for (ClientboundPacketType newConstant : newConstants) {
            newClientboundPackets.put(newConstant.name(), newConstant);
        }

        for (ClientboundPacketType packet : oldClientboundPacketEnum.getEnumConstants()) {
            ClientboundPacketType mappedPacket = newClientboundPackets.get(packet.name());
            int oldId = packet.ordinal();
            if (mappedPacket == null) {
                // Packet doesn't exist on new client
                Preconditions.checkArgument(hasRegisteredOutgoing(State.PLAY, oldId),
                        "Packet " + mappedPacket + " in " + getClass().getSimpleName() + " has no mapping - it needs to be manually cancelled or remapped!");
                continue;
            }

            int newId = mappedPacket.ordinal();
            if (!hasRegisteredOutgoing(State.PLAY, oldId)) {
                registerOutgoing(State.PLAY, oldId, newId);
            }
        }
    }

    protected void registerIncomingChannelIdChanges() {
        ServerboundPacketType[] oldConstants = oldServerboundPacketEnum.getEnumConstants();
        Map<String, ServerboundPacketType> oldServerboundConstants = new HashMap<>(oldConstants.length);
        for (ServerboundPacketType oldConstant : oldConstants) {
            oldServerboundConstants.put(oldConstant.name(), oldConstant);
        }

        for (ServerboundPacketType packet : newServerboundPacketEnum.getEnumConstants()) {
            ServerboundPacketType mappedPacket = oldServerboundConstants.get(packet.name());
            int newId = packet.ordinal();
            if (mappedPacket == null) {
                // Packet doesn't exist on old server
                Preconditions.checkArgument(hasRegisteredIncoming(State.PLAY, newId),
                        "Packet " + mappedPacket + " in " + getClass().getSimpleName() + " has no mapping - it needs to be manually cancelled or remapped!");
                continue;
            }

            int oldId = mappedPacket.ordinal();
            if (!hasRegisteredIncoming(State.PLAY, newId)) {
                registerIncoming(State.PLAY, oldId, newId);
            }
        }
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
     * Register the packets for this protocol.
     */
    protected abstract void registerPackets();

    /**
     * Load mapping data for the protocol.
     * <p>
     * To be overridden if needed.
     */
    protected void loadMappingData() {
    }

    /**
     * Handle protocol registration phase, use this to register providers / tasks.
     * <p>
     * To be overridden if needed.
     *
     * @param providers The current providers
     */
    protected void register(ViaProviders providers) {
    }

    /**
     * Initialise a user for this protocol setting up objects.
     * /!\ WARNING - May be called more than once in a single {@link UserConnection}
     * <p>
     * To be overridden if needed.
     *
     * @param userConnection The user to initialise
     */
    public void init(UserConnection userConnection) {
    }

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
        Packet packet = new Packet(state, newPacketID);
        if (!override && incoming.containsKey(packet)) {
            Via.getPlatform().getLogger().log(Level.WARNING, packet + " already registered!" +
                    " If this override is intentional, set override to true. Stacktrace: ", new Exception());
        }
        incoming.put(packet, protocolPacket);
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


    public void registerOutgoing(State state, int oldPacketID, int newPacketID, PacketRemapper packetRemapper, boolean override) {
        ProtocolPacket protocolPacket = new ProtocolPacket(state, oldPacketID, newPacketID, packetRemapper);
        Packet packet = new Packet(state, oldPacketID);
        if (!override && outgoing.containsKey(packet)) {
            Via.getPlatform().getLogger().log(Level.WARNING, packet + " already registered!" +
                    " If override is intentional, set override to true. Stacktrace: ", new Exception());
        }
        outgoing.put(packet, protocolPacket);
    }

    /**
     * Registers an outgoing protocol and automatically maps it to the new id.
     *
     * @param packetType     packet type the server sends
     * @param packetRemapper remapper
     */
    public void registerOutgoing(ClientboundPacketType packetType, PacketRemapper packetRemapper) {
        Preconditions.checkArgument(packetType.getClass() == oldClientboundPacketEnum);

        ClientboundPacketType mappedPacket = oldClientboundPacketEnum == newClientboundPacketEnum ? packetType
                : Arrays.stream(newClientboundPacketEnum.getEnumConstants()).filter(en -> en.name().equals(packetType.name())).findAny().orElse(null);
        Preconditions.checkNotNull(mappedPacket, "Packet type " + packetType + " in " + packetType.getClass().getSimpleName() + " could not be automatically mapped!");

        int oldId = packetType.ordinal();
        int newId = mappedPacket.ordinal();
        registerOutgoing(State.PLAY, oldId, newId, packetRemapper);
    }

    /**
     * Registers an outgoing protocol.
     *
     * @param packetType       packet type the server initially sends
     * @param mappedPacketType packet type after transforming for the client
     * @param packetRemapper   remapper
     */
    public void registerOutgoing(ClientboundPacketType packetType, ClientboundPacketType mappedPacketType, PacketRemapper packetRemapper) {
        Preconditions.checkArgument(packetType.getClass() == oldClientboundPacketEnum);
        Preconditions.checkArgument(mappedPacketType == null || mappedPacketType.getClass() == newClientboundPacketEnum);
        registerOutgoing(State.PLAY, packetType.ordinal(), mappedPacketType != null ? mappedPacketType.ordinal() : -1, packetRemapper);
    }

    public void registerOutgoing(ClientboundPacketType oldPacketType, ClientboundPacketType newPacketType) {
        registerOutgoing(oldPacketType, newPacketType, null);
    }

    public void cancelOutgoing(ClientboundPacketType packetType) {
        Preconditions.checkArgument(packetType.getClass() == oldClientboundPacketEnum);
        cancelOutgoing(State.PLAY, packetType.ordinal(), packetType.ordinal());
    }

    /**
     * Registers an incoming protocol and automatically maps it to the server's id.
     *
     * @param packetType     packet type the client sends
     * @param packetRemapper remapper
     */
    public void registerIncoming(ServerboundPacketType packetType, PacketRemapper packetRemapper) {
        Preconditions.checkArgument(packetType.getClass() == newServerboundPacketEnum);

        ServerboundPacketType mappedPacket = oldServerboundPacketEnum == newServerboundPacketEnum ? packetType
                : Arrays.stream(oldServerboundPacketEnum.getEnumConstants()).filter(en -> en.name().equals(packetType.name())).findAny().orElse(null);
        Preconditions.checkNotNull(mappedPacket, "Packet type " + packetType + " in " + packetType.getClass().getSimpleName() + " could not be automatically mapped!");

        int oldId = mappedPacket.ordinal();
        int newId = packetType.ordinal();
        registerIncoming(State.PLAY, oldId, newId, packetRemapper);
    }

    /**
     * Registers an incoming protocol.
     *
     * @param packetType       packet type initially sent by the client
     * @param mappedPacketType packet type after transforming for the server
     * @param packetRemapper   remapper
     */
    public void registerIncoming(ServerboundPacketType packetType, ServerboundPacketType mappedPacketType, PacketRemapper packetRemapper) {
        Preconditions.checkArgument(packetType.getClass() == newServerboundPacketEnum);
        Preconditions.checkArgument(mappedPacketType == null || mappedPacketType.getClass() == oldServerboundPacketEnum);
        registerIncoming(State.PLAY, mappedPacketType != null ? mappedPacketType.ordinal() : -1, packetType.ordinal(), packetRemapper);
    }

    public void cancelIncoming(ServerboundPacketType packetType) {
        Preconditions.checkArgument(packetType.getClass() == newServerboundPacketEnum);
        cancelIncoming(State.PLAY, -1, packetType.ordinal());
    }


    /**
     * Checks if an outgoing packet has already been registered.
     *
     * @param state       state which the packet is sent in
     * @param oldPacketID old packet ID
     * @return true if already registered
     */
    public boolean hasRegisteredOutgoing(State state, int oldPacketID) {
        Packet packet = new Packet(state, oldPacketID);
        return outgoing.containsKey(packet);
    }

    /**
     * Checks if an incoming packet has already been registered.
     *
     * @param state       state which the packet is sent in
     * @param newPacketId packet ID
     * @return true if already registered
     */
    public boolean hasRegisteredIncoming(State state, int newPacketId) {
        Packet packet = new Packet(state, newPacketId);
        return incoming.containsKey(packet);
    }

    public boolean hasMappingDataToLoad() {
        return hasMappingDataToLoad;
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
        Packet statePacket = new Packet(state, packetWrapper.getId());
        Map<Packet, ProtocolPacket> packetMap = (direction == Direction.OUTGOING ? outgoing : incoming);
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

    public static class Packet {
        private final State state;
        private final int packetId;

        public Packet(State state, int packetId) {
            this.state = state;
            this.packetId = packetId;
        }

        public State getState() {
            return state;
        }

        public int getPacketId() {
            return packetId;
        }

        @Override
        public String toString() {
            return "Packet{" + "state=" + state + ", packetId=" + packetId + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Packet that = (Packet) o;
            return packetId == that.packetId && state == that.state;
        }

        @Override
        public int hashCode() {
            int result = state != null ? state.hashCode() : 0;
            result = 31 * result + packetId;
            return result;
        }
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
