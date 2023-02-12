/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.exception.CancelException;
import com.viaversion.viaversion.exception.InformativeException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class AbstractProtocol<C1 extends ClientboundPacketType, C2 extends ClientboundPacketType, S1 extends ServerboundPacketType, S2 extends ServerboundPacketType>
        implements Protocol<C1, C2, S1, S2> {
    private final Map<Packet, ProtocolPacket> serverbound = new HashMap<>();
    private final Map<Packet, ProtocolPacket> clientbound = new HashMap<>();
    private final Map<Class<?>, Object> storedObjects = new HashMap<>(); // currently only used for MetadataRewriters
    protected final Class<C1> unmappedClientboundPacketType;
    protected final Class<C2> mappedClientboundPacketType;
    protected final Class<S1> mappedServerboundPacketType;
    protected final Class<S2> unmappedServerboundPacketType;
    private boolean initialized;

    protected AbstractProtocol() {
        this(null, null, null, null);
    }

    /**
     * Creates a protocol with automated id mapping if the respective packet type classes are not null.
     */
    protected AbstractProtocol(@Nullable Class<C1> unmappedClientboundPacketType, @Nullable Class<C2> mappedClientboundPacketType,
                               @Nullable Class<S1> mappedServerboundPacketType, @Nullable Class<S2> unmappedServerboundPacketType) {
        this.unmappedClientboundPacketType = unmappedClientboundPacketType;
        this.mappedClientboundPacketType = mappedClientboundPacketType;
        this.mappedServerboundPacketType = mappedServerboundPacketType;
        this.unmappedServerboundPacketType = unmappedServerboundPacketType;
    }

    @Override
    public final void initialize() {
        Preconditions.checkArgument(!initialized);
        initialized = true;

        registerPackets();

        // Register the rest of the ids with no handlers if necessary
        if (unmappedClientboundPacketType != null && mappedClientboundPacketType != null
                && unmappedClientboundPacketType != mappedClientboundPacketType) {
            registerClientboundChannelIdChanges();
        }
        if (mappedServerboundPacketType != null && unmappedServerboundPacketType != null
                && mappedServerboundPacketType != unmappedServerboundPacketType) {
            registerServerboundChannelIdChanges();
        }
    }

    protected void registerClientboundChannelIdChanges() {
        C2[] newConstants = mappedClientboundPacketType.getEnumConstants();
        Map<String, C2> newClientboundPackets = new HashMap<>(newConstants.length);
        for (C2 newConstant : newConstants) {
            newClientboundPackets.put(newConstant.getName(), newConstant);
        }

        for (C1 packet : unmappedClientboundPacketType.getEnumConstants()) {
            C2 mappedPacket = newClientboundPackets.get(packet.getName());
            if (mappedPacket == null) {
                // Packet doesn't exist on new client
                Preconditions.checkArgument(hasRegisteredClientbound(packet),
                        "Packet " + packet + " in " + getClass().getSimpleName() + " has no mapping - it needs to be manually cancelled or remapped!");
                continue;
            }

            if (!hasRegisteredClientbound(packet)) {
                registerClientbound(packet, mappedPacket);
            }
        }
    }

    protected void registerServerboundChannelIdChanges() {
        S1[] oldConstants = mappedServerboundPacketType.getEnumConstants();
        Map<String, S1> oldServerboundConstants = new HashMap<>(oldConstants.length);
        for (S1 oldConstant : oldConstants) {
            oldServerboundConstants.put(oldConstant.getName(), oldConstant);
        }

        for (S2 packet : unmappedServerboundPacketType.getEnumConstants()) {
            S1 mappedPacket = oldServerboundConstants.get(packet.getName());
            if (mappedPacket == null) {
                // Packet doesn't exist on old server
                Preconditions.checkArgument(hasRegisteredServerbound(packet),
                        "Packet " + packet + " in " + getClass().getSimpleName() + " has no mapping - it needs to be manually cancelled or remapped!");
                continue;
            }

            if (!hasRegisteredServerbound(packet)) {
                registerServerbound(packet, mappedPacket);
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
    public void registerServerbound(State state, int unmappedPacketId, int mappedPacketId, PacketHandler handler, boolean override) {
        ProtocolPacket protocolPacket = new ProtocolPacket(state, unmappedPacketId, mappedPacketId, handler);
        Packet packet = new Packet(state, mappedPacketId);
        if (!override && serverbound.containsKey(packet)) {
            Via.getPlatform().getLogger().log(Level.WARNING, packet + " already registered!" +
                    " If this override is intentional, set override to true. Stacktrace: ", new Exception());
        }
        serverbound.put(packet, protocolPacket);
    }

    @Override
    public void cancelServerbound(State state, int unmappedPacketId, int mappedPacketId) {
        registerServerbound(state, unmappedPacketId, mappedPacketId, PacketWrapper::cancel);
    }

    @Override
    public void cancelServerbound(State state, int mappedPacketId) {
        cancelServerbound(state, -1, mappedPacketId);
    }

    @Override
    public void cancelClientbound(State state, int unmappedPacketId, int mappedPacketId) {
        registerClientbound(state, unmappedPacketId, mappedPacketId, PacketWrapper::cancel);
    }

    @Override
    public void cancelClientbound(State state, int unmappedPacketId) {
        cancelClientbound(state, unmappedPacketId, -1);
    }

    @Override
    public void registerClientbound(State state, int unmappedPacketId, int mappedPacketId, PacketHandler handler, boolean override) {
        ProtocolPacket protocolPacket = new ProtocolPacket(state, unmappedPacketId, mappedPacketId, handler);
        Packet packet = new Packet(state, unmappedPacketId);
        if (!override && clientbound.containsKey(packet)) {
            Via.getPlatform().getLogger().log(Level.WARNING, packet + " already registered!" +
                    " If override is intentional, set override to true. Stacktrace: ", new Exception());
        }
        clientbound.put(packet, protocolPacket);
    }


    @Override
    public void registerClientbound(C1 packetType, @Nullable PacketHandler handler) {
        checkPacketType(packetType, unmappedClientboundPacketType == null || packetType.getClass() == unmappedClientboundPacketType);

        //noinspection unchecked
        C2 mappedPacket = unmappedClientboundPacketType == mappedClientboundPacketType ? (C2) packetType
                : Arrays.stream(mappedClientboundPacketType.getEnumConstants()).filter(en -> en.getName().equals(packetType.getName())).findAny().orElse(null);
        Preconditions.checkNotNull(mappedPacket, "Packet type " + packetType + " in " + packetType.getClass().getSimpleName() + " could not be automatically mapped!");

        registerClientbound(packetType, mappedPacket, handler);
    }

    @Override
    public void registerClientbound(C1 packetType, @Nullable C2 mappedPacketType, @Nullable PacketHandler handler, boolean override) {
        register(clientbound, packetType, mappedPacketType, unmappedClientboundPacketType, mappedClientboundPacketType, handler, override);
    }

    @Override
    public void cancelClientbound(C1 packetType) {
        registerClientbound(packetType, null, PacketWrapper::cancel);
    }

    @Override
    public void registerServerbound(S2 packetType, @Nullable PacketHandler handler) {
        checkPacketType(packetType, unmappedServerboundPacketType == null || packetType.getClass() == unmappedServerboundPacketType);

        //noinspection unchecked
        S1 mappedPacket = mappedServerboundPacketType == unmappedServerboundPacketType ? (S1) packetType
                : Arrays.stream(mappedServerboundPacketType.getEnumConstants()).filter(en -> en.getName().equals(packetType.getName())).findAny().orElse(null);
        Preconditions.checkNotNull(mappedPacket, "Packet type " + packetType + " in " + packetType.getClass().getSimpleName() + " could not be automatically mapped!");

        registerServerbound(packetType, mappedPacket, handler);
    }

    @Override
    public void registerServerbound(S2 packetType, @Nullable S1 mappedPacketType, @Nullable PacketHandler handler, boolean override) {
        register(serverbound, packetType, mappedPacketType, unmappedServerboundPacketType, mappedServerboundPacketType, handler, override);
    }

    @Override
    public void cancelServerbound(S2 packetType) {
        registerServerbound(packetType, null, PacketWrapper::cancel);
    }

    private void register(Map<Packet, ProtocolPacket> packetMap, PacketType packetType, @Nullable PacketType mappedPacketType,
                          Class<? extends PacketType> unmappedPacketEnum, Class<? extends PacketType> mappedPacketEnum,
                          @Nullable PacketHandler handler, boolean override) {
        checkPacketType(packetType, unmappedPacketEnum == null || packetType.getClass() == unmappedPacketEnum);
        checkPacketType(mappedPacketType, mappedPacketType == null || mappedPacketEnum == null || mappedPacketType.getClass() == mappedPacketEnum);
        Preconditions.checkArgument(mappedPacketType == null || packetType.state() == mappedPacketType.state(), "Packet type state does not match mapped packet type state");

        ProtocolPacket protocolPacket = new ProtocolPacket(packetType.state(), packetType, mappedPacketType, handler);
        Packet packet = new Packet(packetType.state(), packetType.getId());
        if (!override && packetMap.containsKey(packet)) {
            Via.getPlatform().getLogger().log(Level.WARNING, packet + " already registered!" +
                    " If override is intentional, set override to true. Stacktrace: ", new Exception());
        }
        packetMap.put(packet, protocolPacket);
    }

    @Override
    public boolean hasRegisteredClientbound(C1 packetType) {
        return hasRegisteredClientbound(packetType.state(), packetType.getId());
    }

    @Override
    public boolean hasRegisteredServerbound(S2 packetType) {
        return hasRegisteredServerbound(packetType.state(), packetType.getId());
    }

    @Override
    public boolean hasRegisteredClientbound(State state, int unmappedPacketId) {
        Packet packet = new Packet(state, unmappedPacketId);
        return clientbound.containsKey(packet);
    }

    @Override
    public boolean hasRegisteredServerbound(State state, int unmappedPacketId) {
        Packet packet = new Packet(state, unmappedPacketId);
        return serverbound.containsKey(packet);
    }

    @Override
    public void transform(Direction direction, State state, PacketWrapper packetWrapper) throws Exception {
        Packet statePacket = new Packet(state, packetWrapper.getId());
        Map<Packet, ProtocolPacket> packetMap = direction == Direction.CLIENTBOUND ? clientbound : serverbound;
        ProtocolPacket protocolPacket = packetMap.get(statePacket);
        if (protocolPacket == null) {
            return;
        }

        // Write packet id
        int unmappedId = packetWrapper.getId();
        if (protocolPacket.isMappedOverTypes()) {
            packetWrapper.setPacketType(protocolPacket.getMappedPacketType());
        } else {
            int mappedId = direction == Direction.CLIENTBOUND ? protocolPacket.getNewId() : protocolPacket.getOldId();
            if (unmappedId != mappedId) {
                //noinspection deprecation
                packetWrapper.setId(mappedId);
            }
        }

        PacketHandler handler = protocolPacket.getRemapper();
        if (handler != null) {
            try {
                handler.handle(packetWrapper);
            } catch (CancelException e) {
                // Pass through CancelExceptions
                throw e;
            } catch (InformativeException e) {
                // Catch InformativeExceptions
                e.addSource(handler.getClass());
                throwRemapError(direction, state, unmappedId, packetWrapper.getId(), e);
                return;
            } catch (Exception e) {
                // Wrap other exceptions during packet handling
                InformativeException ex = new InformativeException(e);
                ex.addSource(handler.getClass());
                throwRemapError(direction, state, unmappedId, packetWrapper.getId(), ex);
                return;
            }

            if (packetWrapper.isCancelled()) {
                throw CancelException.generate();
            }
        }
    }

    private void throwRemapError(Direction direction, State state, int unmappedPacketId, int mappedPacketId, InformativeException e) throws InformativeException {
        // Don't print errors during handshake/login/status
        if (state != State.PLAY && direction == Direction.SERVERBOUND && !Via.getManager().debugHandler().enabled()) {
            e.setShouldBePrinted(false);
            throw e;
        }

        Class<? extends PacketType> packetTypeClass = state == State.PLAY ? (direction == Direction.CLIENTBOUND ? unmappedClientboundPacketType : unmappedServerboundPacketType) : null;
        if (packetTypeClass != null) {
            PacketType[] enumConstants = packetTypeClass.getEnumConstants();
            PacketType packetType = unmappedPacketId < enumConstants.length && unmappedPacketId >= 0 ? enumConstants[unmappedPacketId] : null;
            Via.getPlatform().getLogger().warning("ERROR IN " + getClass().getSimpleName() + " IN REMAP OF " + packetType + " (" + toNiceHex(unmappedPacketId) + ")");
        } else {
            Via.getPlatform().getLogger().warning("ERROR IN " + getClass().getSimpleName()
                    + " IN REMAP OF " + toNiceHex(unmappedPacketId) + "->" + toNiceHex(mappedPacketId));
        }
        throw e;
    }

    public static String toNiceHex(int id) {
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
            throw new IllegalArgumentException("Packet type " + packetType + " in " + packetType.getClass().getSimpleName() + " is taken from the wrong packet type class");
        }
    }

    @Override
    public @Nullable <T> T get(Class<T> objectClass) {
        //noinspection unchecked
        return (T) storedObjects.get(objectClass);
    }

    @Override
    public void put(Object object) {
        storedObjects.put(object.getClass(), object);
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
        private final int oldId;
        private final int newId;
        private final PacketType unmappedPacketType;
        private final PacketType mappedPacketType;
        private final PacketHandler handler;

        @Deprecated
        public ProtocolPacket(State state, int oldId, int newId, @Nullable PacketHandler handler) {
            this.state = state;
            this.oldId = oldId;
            this.newId = newId;
            this.handler = handler;
            this.unmappedPacketType = null;
            this.mappedPacketType = null;
        }

        public ProtocolPacket(State state, PacketType unmappedPacketType, @Nullable PacketType mappedPacketType, @Nullable PacketHandler handler) {
            this.state = state;
            this.unmappedPacketType = unmappedPacketType;
            if (unmappedPacketType.direction() == Direction.CLIENTBOUND) {
                this.oldId = unmappedPacketType.getId();
                this.newId = mappedPacketType != null ? mappedPacketType.getId() : -1;
            } else {
                // Serverbound switcheroo in old vs. new id caused issues and was counterintuitive
                this.oldId = mappedPacketType != null ? mappedPacketType.getId() : -1;
                this.newId = unmappedPacketType.getId();
            }
            this.mappedPacketType = mappedPacketType;
            this.handler = handler;
        }

        public State getState() {
            return state;
        }

        @Deprecated
        public int getOldId() {
            return oldId;
        }

        @Deprecated
        public int getNewId() {
            return newId;
        }

        /**
         * Returns the unmapped packet type, or null if mapped over ids.
         * This is NOT the same as calling {@link #getOldId()} (think of unmapped vs. old in 1.17→1.16).
         *
         * @return unmapped packet type, or null if mapped over ids
         */
        @Nullable
        public PacketType getUnmappedPacketType() {
            return unmappedPacketType;
        }

        /**
         * Returns the mapped packet type, or null if mapped over ids or mapped to no packet type.
         * This is NOT the same as calling {@link #getNewId()} (think of mapped vs. new in 1.17→1.16).
         *
         * @return new packet type, or null if mapped over ids or mapped to no packet type
         */
        @Nullable
        public PacketType getMappedPacketType() {
            return mappedPacketType;
        }

        public boolean isMappedOverTypes() {
            return unmappedPacketType != null;
        }

        @Nullable
        public PacketHandler getRemapper() {
            return handler;
        }

        @Override
        public String toString() {
            return "ProtocolPacket{" +
                    "state=" + state +
                    ", oldId=" + oldId +
                    ", newId=" + newId +
                    ", unmappedPacketType=" + unmappedPacketType +
                    ", mappedPacketType=" + mappedPacketType +
                    ", handler=" + handler +
                    '}';
        }
    }

    public Map<Packet, ProtocolPacket> getClientbound() {
        return clientbound;
    }

    public Map<Packet, ProtocolPacket> getServerbound() {
        return serverbound;
    }
}
