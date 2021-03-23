/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package us.myles.ViaVersion.api.protocol;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.MappingData;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.platform.providers.ViaProviders;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.exception.CancelException;
import us.myles.ViaVersion.exception.InformativeException;
import us.myles.ViaVersion.packets.Direction;
import us.myles.ViaVersion.packets.State;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Abstract protocol class handling packet transformation between two protocol versions.
 * Clientbound and serverbount packet types can be set to enforce correct usage of them.
 *
 * @param <C1> old clientbound packet types
 * @param <C2> new clientbound packet types
 * @param <S1> old serverbound packet types
 * @param <S2> new serverbound packet types
 * @see SimpleProtocol for a helper class if you do not want to define any of the types above
 */
public abstract class Protocol<C1 extends ClientboundPacketType, C2 extends ClientboundPacketType, S1 extends ServerboundPacketType, S2 extends ServerboundPacketType> {
    private final Map<Packet, ProtocolPacket> incoming = new HashMap<>();
    private final Map<Packet, ProtocolPacket> outgoing = new HashMap<>();
    private final Map<Class, Object> storedObjects = new HashMap<>(); // currently only used for MetadataRewriters
    protected final Class<C1> oldClientboundPacketEnum;
    protected final Class<C2> newClientboundPacketEnum;
    protected final Class<S1> oldServerboundPacketEnum;
    protected final Class<S2> newServerboundPacketEnum;

    protected Protocol() {
        this(null, null, null, null);
    }

    /**
     * Creates a protocol with automated id mapping if the respective enums are not null.
     */
    protected Protocol(@Nullable Class<C1> oldClientboundPacketEnum, @Nullable Class<C2> clientboundPacketEnum,
                       @Nullable Class<S1> oldServerboundPacketEnum, @Nullable Class<S2> serverboundPacketEnum) {
        this.oldClientboundPacketEnum = oldClientboundPacketEnum;
        this.newClientboundPacketEnum = clientboundPacketEnum;
        this.oldServerboundPacketEnum = oldServerboundPacketEnum;
        this.newServerboundPacketEnum = serverboundPacketEnum;
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
                        "Packet " + packet + " in " + getClass().getSimpleName() + " has no mapping - it needs to be manually cancelled or remapped!");
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
                        "Packet " + packet + " in " + getClass().getSimpleName() + " has no mapping - it needs to be manually cancelled or remapped!");
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
     * Register the packets for this protocol. To be overriden.
     */
    protected void registerPackets() {
    }

    /**
     * Loads the mappingdata.
     */
    protected final void loadMappingData() {
        getMappingData().load();
        onMappingDataLoaded();
    }

    /**
     * Called after {@link #loadMappingData()} is called; load extra mapping data for the protocol.
     * <p>
     * To be overridden if needed.
     */
    protected void onMappingDataLoaded() {
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
     * @param packetType     clientbound packet type the server sends
     * @param packetRemapper remapper
     */
    public void registerOutgoing(C1 packetType, @Nullable PacketRemapper packetRemapper) {
        checkPacketType(packetType, packetType.getClass() == oldClientboundPacketEnum);

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
     * @param packetType       clientbound packet type the server initially sends
     * @param mappedPacketType clientbound packet type after transforming for the client
     * @param packetRemapper   remapper
     */
    public void registerOutgoing(C1 packetType, @Nullable C2 mappedPacketType, @Nullable PacketRemapper packetRemapper) {
        checkPacketType(packetType, packetType.getClass() == oldClientboundPacketEnum);
        checkPacketType(mappedPacketType, mappedPacketType == null || mappedPacketType.getClass() == newClientboundPacketEnum);

        registerOutgoing(State.PLAY, packetType.ordinal(), mappedPacketType != null ? mappedPacketType.ordinal() : -1, packetRemapper);
    }

    /**
     * Maps a packet type to another packet type without a packet handler.
     * Note that this should not be called for simple channel mappings of the same packet; this is already done automatically.
     *
     * @param packetType       clientbound packet type the server initially sends
     * @param mappedPacketType clientbound packet type after transforming for the client
     */
    public void registerOutgoing(C1 packetType, @Nullable C2 mappedPacketType) {
        registerOutgoing(packetType, mappedPacketType, null);
    }

    public void cancelOutgoing(C1 packetType) {
        cancelOutgoing(State.PLAY, packetType.ordinal(), packetType.ordinal());
    }

    /**
     * Registers an incoming protocol and automatically maps it to the server's id.
     *
     * @param packetType     serverbound packet type the client sends
     * @param packetRemapper remapper
     */
    public void registerIncoming(S2 packetType, @Nullable PacketRemapper packetRemapper) {
        checkPacketType(packetType, packetType.getClass() == newServerboundPacketEnum);

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
     * @param packetType       serverbound packet type initially sent by the client
     * @param mappedPacketType serverbound packet type after transforming for the server
     * @param packetRemapper   remapper
     */
    public void registerIncoming(S2 packetType, @Nullable S1 mappedPacketType, @Nullable PacketRemapper packetRemapper) {
        checkPacketType(packetType, packetType.getClass() == newServerboundPacketEnum);
        checkPacketType(mappedPacketType, mappedPacketType == null || mappedPacketType.getClass() == oldServerboundPacketEnum);

        registerIncoming(State.PLAY, mappedPacketType != null ? mappedPacketType.ordinal() : -1, packetType.ordinal(), packetRemapper);
    }

    public void cancelIncoming(S2 packetType) {
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

        // Write packet id
        int oldId = packetWrapper.getId();
        int newId = direction == Direction.OUTGOING ? protocolPacket.getNewID() : protocolPacket.getOldID();
        packetWrapper.setId(newId);

        PacketRemapper remapper = protocolPacket.getRemapper();
        if (remapper != null) {
            try {
                remapper.remap(packetWrapper);
            } catch (InformativeException e) { // Catch InformativeExceptions, pass through CancelExceptions
                throwRemapError(direction, state, oldId, newId, e);
                return;
            }

            if (packetWrapper.isCancelled()) {
                throw CancelException.generate();
            }
        }
    }

    private void throwRemapError(Direction direction, State state, int oldId, int newId, InformativeException e) throws InformativeException {
        // Don't print errors during handshake
        if (state == State.HANDSHAKE) {
            throw e;
        }

        Class<? extends PacketType> packetTypeClass = state == State.PLAY ? (direction == Direction.OUTGOING ? oldClientboundPacketEnum : newServerboundPacketEnum) : null;
        if (packetTypeClass != null) {
            PacketType[] enumConstants = packetTypeClass.getEnumConstants();
            PacketType packetType = oldId < enumConstants.length && oldId >= 0 ? enumConstants[oldId] : null;
            Via.getPlatform().getLogger().warning("ERROR IN " + getClass().getSimpleName() + " IN REMAP OF " + packetType + " (" + toNiceHex(oldId) + ")");
        } else {
            Via.getPlatform().getLogger().warning("ERROR IN " + getClass().getSimpleName()
                    + " IN REMAP OF " + toNiceHex(oldId) + "->" + toNiceHex(newId));
        }
        throw e;
    }

    private String toNiceHex(int id) {
        String hex = Integer.toHexString(id).toUpperCase();
        return (hex.length() == 1 ? "0x0" : "0x") + hex;
    }

    /**
     * @param packetType packet type
     * @param isValid    expression to check the packet's validity
     * @throws IllegalArgumentException if the given expression is not met
     */
    private void checkPacketType(PacketType packetType, boolean isValid) {
        if (!isValid) {
            throw new IllegalArgumentException("Packet type " + packetType + " in " + packetType.getClass().getSimpleName() + " is taken from the wrong enum");
        }
    }

    @Nullable
    public <T> T get(Class<T> objectClass) {
        return (T) storedObjects.get(objectClass);
    }

    public void put(Object object) {
        storedObjects.put(object.getClass(), object);
    }

    /**
     * Returns true if this Protocol's {@link #loadMappingData()} method should be called.
     * <p>
     * This does *not* necessarily mean that {@link #getMappingData()} is non-null, since this may be
     * overriden, depending on special cases.
     *
     * @return true if this Protocol's {@link #loadMappingData()} method should be called
     */
    public boolean hasMappingDataToLoad() {
        return getMappingData() != null;
    }

    @Nullable
    public MappingData getMappingData() {
        return null; // Let the protocols hold the mappings to still have easy, static singleton access there
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

        public ProtocolPacket(State state, int oldID, int newID, @Nullable PacketRemapper remapper) {
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

        @Nullable
        public PacketRemapper getRemapper() {
            return remapper;
        }
    }
}
