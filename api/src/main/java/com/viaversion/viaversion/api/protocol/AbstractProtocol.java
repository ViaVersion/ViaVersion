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
package com.viaversion.viaversion.api.protocol;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.exception.CancelException;
import com.viaversion.viaversion.exception.InformativeException;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public abstract class AbstractProtocol<C1 extends ClientboundPacketType, C2 extends ClientboundPacketType, S1 extends ServerboundPacketType, S2 extends ServerboundPacketType>
        implements Protocol<C1, C2, S1, S2> {
    private final Map<Packet, ProtocolPacket> serverbound = new HashMap<>();
    private final Map<Packet, ProtocolPacket> clientbound = new HashMap<>();
    private final Map<Class<?>, Object> storedObjects = new HashMap<>(); // currently only used for MetadataRewriters
    protected final Class<C1> oldClientboundPacketEnum;
    protected final Class<C2> newClientboundPacketEnum;
    protected final Class<S1> oldServerboundPacketEnum;
    protected final Class<S2> newServerboundPacketEnum;
    private boolean initialized;

    protected AbstractProtocol() {
        this(null, null, null, null);
    }

    /**
     * Creates a protocol with automated id mapping if the respective enums are not null.
     */
    protected AbstractProtocol(@Nullable Class<C1> oldClientboundPacketEnum, @Nullable Class<C2> clientboundPacketEnum,
                               @Nullable Class<S1> oldServerboundPacketEnum, @Nullable Class<S2> serverboundPacketEnum) {
        this.oldClientboundPacketEnum = oldClientboundPacketEnum;
        this.newClientboundPacketEnum = clientboundPacketEnum;
        this.oldServerboundPacketEnum = oldServerboundPacketEnum;
        this.newServerboundPacketEnum = serverboundPacketEnum;
    }

    @Override
    public final void initialize() {
        Preconditions.checkArgument(!initialized);
        initialized = true;

        registerPackets();

        // Register the rest of the ids with no handlers if necessary
        if (oldClientboundPacketEnum != null && newClientboundPacketEnum != null
                && oldClientboundPacketEnum != newClientboundPacketEnum) {
            registerClientboundChannelIdChanges();
        }
        if (oldServerboundPacketEnum != null && newServerboundPacketEnum != null
                && oldServerboundPacketEnum != newServerboundPacketEnum) {
            registerServerboundChannelIdChanges();
        }
    }

    protected void registerClientboundChannelIdChanges() {
        ClientboundPacketType[] newConstants = newClientboundPacketEnum.getEnumConstants();
        Map<String, ClientboundPacketType> newClientboundPackets = new HashMap<>(newConstants.length);
        for (ClientboundPacketType newConstant : newConstants) {
            newClientboundPackets.put(newConstant.getName(), newConstant);
        }

        for (ClientboundPacketType packet : oldClientboundPacketEnum.getEnumConstants()) {
            ClientboundPacketType mappedPacket = newClientboundPackets.get(packet.getName());
            int oldId = packet.getId();
            if (mappedPacket == null) {
                // Packet doesn't exist on new client
                Preconditions.checkArgument(hasRegisteredClientbound(State.PLAY, oldId),
                        "Packet " + packet + " in " + getClass().getSimpleName() + " has no mapping - it needs to be manually cancelled or remapped!");
                continue;
            }

            int newId = mappedPacket.getId();
            if (!hasRegisteredClientbound(State.PLAY, oldId)) {
                registerClientbound(State.PLAY, oldId, newId);
            }
        }
    }

    protected void registerServerboundChannelIdChanges() {
        ServerboundPacketType[] oldConstants = oldServerboundPacketEnum.getEnumConstants();
        Map<String, ServerboundPacketType> oldServerboundConstants = new HashMap<>(oldConstants.length);
        for (ServerboundPacketType oldConstant : oldConstants) {
            oldServerboundConstants.put(oldConstant.getName(), oldConstant);
        }

        for (ServerboundPacketType packet : newServerboundPacketEnum.getEnumConstants()) {
            ServerboundPacketType mappedPacket = oldServerboundConstants.get(packet.getName());
            int newId = packet.getId();
            if (mappedPacket == null) {
                // Packet doesn't exist on old server
                Preconditions.checkArgument(hasRegisteredServerbound(State.PLAY, newId),
                        "Packet " + packet + " in " + getClass().getSimpleName() + " has no mapping - it needs to be manually cancelled or remapped!");
                continue;
            }

            int oldId = mappedPacket.getId();
            if (!hasRegisteredServerbound(State.PLAY, newId)) {
                registerServerbound(State.PLAY, oldId, newId);
            }
        }
    }

    /**
     * Register the packets for this protocol. To be overriden.
     */
    protected void registerPackets() {
    }

    @Override
    public final void loadMappingData() {
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

    protected void addEntityTracker(UserConnection connection, EntityTracker tracker) {
        connection.addEntityTracker(this.getClass(), tracker);
    }

    @Override
    public void registerServerbound(State state, int oldPacketID, int newPacketID, PacketRemapper packetRemapper, boolean override) {
        ProtocolPacket protocolPacket = new ProtocolPacket(state, oldPacketID, newPacketID, packetRemapper);
        Packet packet = new Packet(state, newPacketID);
        if (!override && serverbound.containsKey(packet)) {
            Via.getPlatform().getLogger().log(Level.WARNING, packet + " already registered!" +
                    " If this override is intentional, set override to true. Stacktrace: ", new Exception());
        }
        serverbound.put(packet, protocolPacket);
    }

    @Override
    public void cancelServerbound(State state, int oldPacketID, int newPacketID) {
        registerServerbound(state, oldPacketID, newPacketID, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(PacketWrapper::cancel);
            }
        });
    }

    @Override
    public void cancelClientbound(State state, int oldPacketID, int newPacketID) {
        registerClientbound(state, oldPacketID, newPacketID, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(PacketWrapper::cancel);
            }
        });
    }

    @Override
    public void registerClientbound(State state, int oldPacketID, int newPacketID, PacketRemapper packetRemapper, boolean override) {
        ProtocolPacket protocolPacket = new ProtocolPacket(state, oldPacketID, newPacketID, packetRemapper);
        Packet packet = new Packet(state, oldPacketID);
        if (!override && clientbound.containsKey(packet)) {
            Via.getPlatform().getLogger().log(Level.WARNING, packet + " already registered!" +
                    " If override is intentional, set override to true. Stacktrace: ", new Exception());
        }
        clientbound.put(packet, protocolPacket);
    }


    @Override
    public void registerClientbound(C1 packetType, @Nullable PacketRemapper packetRemapper) {
        checkPacketType(packetType, packetType.getClass() == oldClientboundPacketEnum);

        ClientboundPacketType mappedPacket = oldClientboundPacketEnum == newClientboundPacketEnum ? packetType
                : Arrays.stream(newClientboundPacketEnum.getEnumConstants()).filter(en -> en.getName().equals(packetType.getName())).findAny().orElse(null);
        Preconditions.checkNotNull(mappedPacket, "Packet type " + packetType + " in " + packetType.getClass().getSimpleName() + " could not be automatically mapped!");

        int oldId = packetType.getId();
        int newId = mappedPacket.getId();
        registerClientbound(State.PLAY, oldId, newId, packetRemapper);
    }

    @Override
    public void registerClientbound(C1 packetType, @Nullable C2 mappedPacketType, @Nullable PacketRemapper packetRemapper) {
        checkPacketType(packetType, packetType.getClass() == oldClientboundPacketEnum);
        checkPacketType(mappedPacketType, mappedPacketType == null || mappedPacketType.getClass() == newClientboundPacketEnum);

        registerClientbound(State.PLAY, packetType.getId(), mappedPacketType != null ? mappedPacketType.getId() : -1, packetRemapper);
    }

    @Override
    public void cancelClientbound(C1 packetType) {
        cancelClientbound(State.PLAY, packetType.getId(), packetType.getId());
    }

    @Override
    public void registerServerbound(S2 packetType, @Nullable PacketRemapper packetRemapper) {
        checkPacketType(packetType, packetType.getClass() == newServerboundPacketEnum);

        ServerboundPacketType mappedPacket = oldServerboundPacketEnum == newServerboundPacketEnum ? packetType
                : Arrays.stream(oldServerboundPacketEnum.getEnumConstants()).filter(en -> en.getName().equals(packetType.getName())).findAny().orElse(null);
        Preconditions.checkNotNull(mappedPacket, "Packet type " + packetType + " in " + packetType.getClass().getSimpleName() + " could not be automatically mapped!");

        int oldId = mappedPacket.getId();
        int newId = packetType.getId();
        registerServerbound(State.PLAY, oldId, newId, packetRemapper);
    }

    @Override
    public void registerServerbound(S2 packetType, @Nullable S1 mappedPacketType, @Nullable PacketRemapper packetRemapper) {
        checkPacketType(packetType, packetType.getClass() == newServerboundPacketEnum);
        checkPacketType(mappedPacketType, mappedPacketType == null || mappedPacketType.getClass() == oldServerboundPacketEnum);

        registerServerbound(State.PLAY, mappedPacketType != null ? mappedPacketType.getId() : -1, packetType.getId(), packetRemapper);
    }

    @Override
    public void cancelServerbound(S2 packetType) {
        Preconditions.checkArgument(packetType.getClass() == newServerboundPacketEnum);
        cancelServerbound(State.PLAY, -1, packetType.getId());
    }


    @Override
    public boolean hasRegisteredClientbound(State state, int oldPacketID) {
        Packet packet = new Packet(state, oldPacketID);
        return clientbound.containsKey(packet);
    }

    @Override
    public boolean hasRegisteredServerbound(State state, int newPacketId) {
        Packet packet = new Packet(state, newPacketId);
        return serverbound.containsKey(packet);
    }

    @Override
    public void transform(Direction direction, State state, PacketWrapper packetWrapper) throws Exception {
        Packet statePacket = new Packet(state, packetWrapper.getId());
        Map<Packet, ProtocolPacket> packetMap = (direction == Direction.CLIENTBOUND ? clientbound : serverbound);
        ProtocolPacket protocolPacket = packetMap.get(statePacket);
        if (protocolPacket == null) {
            return;
        }

        // Write packet id
        int oldId = packetWrapper.getId();
        int newId = direction == Direction.CLIENTBOUND ? protocolPacket.getNewID() : protocolPacket.getOldID();
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

        Class<? extends PacketType> packetTypeClass = state == State.PLAY ? (direction == Direction.CLIENTBOUND ? oldClientboundPacketEnum : newServerboundPacketEnum) : null;
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

    @Override
    public @Nullable <T> T get(Class<T> objectClass) {
        return (T) storedObjects.get(objectClass);
    }

    @Override
    public void put(Object object) {
        storedObjects.put(object.getClass(), object);
    }

    @Override
    public boolean hasMappingDataToLoad() {
        return getMappingData() != null;
    }

    @Override
    public String toString() {
        return "Protocol:" + getClass().getSimpleName();
    }

    public static final class Packet {
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

    public static final class ProtocolPacket {
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
