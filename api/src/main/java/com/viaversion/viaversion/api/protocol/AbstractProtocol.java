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
import com.viaversion.viaversion.api.protocol.packet.mapping.PacketMapping;
import com.viaversion.viaversion.api.protocol.packet.mapping.PacketMappings;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypeMap;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.exception.CancelException;
import com.viaversion.viaversion.exception.InformativeException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Abstract protocol class to handle packet transformation between two protocol versions.
 *
 * @param <C1> unmapped clientbound packet type
 * @param <C2> mapped clientbound packet type
 * @param <S1> mapped serverbound packet type
 * @param <S2> unmapped serverbound packet type
 */
public abstract class AbstractProtocol<C1 extends ClientboundPacketType, C2 extends ClientboundPacketType,
        S1 extends ServerboundPacketType, S2 extends ServerboundPacketType> implements Protocol<C1, C2, S1, S2> {
    protected final Class<C1> unmappedClientboundPacketType;
    protected final Class<C2> mappedClientboundPacketType;
    protected final Class<S1> mappedServerboundPacketType;
    protected final Class<S2> unmappedServerboundPacketType;
    private final PacketTypesProvider<C1, C2, S1, S2> packetTypesProvider;
    private final PacketMappings serverboundMappings = PacketMappings.arrayMappings();
    private final PacketMappings clientboundMappings = PacketMappings.arrayMappings();
    private final Map<Class<?>, Object> storedObjects = new HashMap<>();
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
        this.packetTypesProvider = createPacketTypesProvider();
    }

    @Override
    public final void initialize() {
        Preconditions.checkArgument(!initialized, "Protocol has already been initialized");
        initialized = true;

        registerPackets();

        // Register the rest of the ids with no handlers if necessary
        if (unmappedClientboundPacketType != null && mappedClientboundPacketType != null
                && unmappedClientboundPacketType != mappedClientboundPacketType) {
            registerPacketIdChanges(
                    packetTypesProvider.unmappedClientboundPacketTypes(),
                    packetTypesProvider.mappedClientboundPacketTypes(),
                    this::hasRegisteredClientbound,
                    this::registerClientbound
            );
        }
        if (mappedServerboundPacketType != null && unmappedServerboundPacketType != null &&
                mappedServerboundPacketType != unmappedServerboundPacketType) {
            registerPacketIdChanges(
                    packetTypesProvider.unmappedServerboundPacketTypes(),
                    packetTypesProvider.mappedServerboundPacketTypes(),
                    this::hasRegisteredServerbound,
                    this::registerServerbound
            );
        }
    }

    private <U extends PacketType, M extends PacketType> void registerPacketIdChanges(
            Map<State, PacketTypeMap<U>> unmappedPacketTypes,
            Map<State, PacketTypeMap<M>> mappedPacketTypes,
            Predicate<U> registeredPredicate,
            BiConsumer<U, M> registerConsumer
    ) {
        for (Map.Entry<State, PacketTypeMap<M>> entry : mappedPacketTypes.entrySet()) {
            PacketTypeMap<M> mappedTypes = entry.getValue();
            for (U unmappedType : unmappedPacketTypes.get(entry.getKey()).types()) {
                M mappedType = mappedTypes.typeByName(unmappedType.getName());
                if (mappedType == null) {
                    // No mapped packet of the same name exists
                    Preconditions.checkArgument(registeredPredicate.test(unmappedType),
                            "Packet %s in %s has no mapping - it needs to be manually cancelled or remapped", unmappedType, getClass());
                    continue;
                }

                // Register if no custom handler exists and ids are different
                if (unmappedType.getId() != mappedType.getId() && !registeredPredicate.test(unmappedType)) {
                    registerConsumer.accept(unmappedType, mappedType);
                }
            }
        }
    }

    @Override
    public final void loadMappingData() {
        getMappingData().load();
        onMappingDataLoaded();
    }

    /**
     * Register the packets for this protocol. To be overriden.
     */
    protected void registerPackets() {
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

    protected PacketTypesProvider<C1, C2, S1, S2> createPacketTypesProvider() {
        return new SimplePacketTypesProvider<>(
                packetTypeMap(unmappedClientboundPacketType),
                packetTypeMap(mappedClientboundPacketType),
                packetTypeMap(mappedServerboundPacketType),
                packetTypeMap(unmappedServerboundPacketType)
        );
    }

    private <P extends PacketType> Map<State, PacketTypeMap<P>> packetTypeMap(Class<P> packetTypeClass) {
        if (packetTypeClass != null) {
            Map<State, PacketTypeMap<P>> map = new EnumMap<>(State.class);
            map.put(State.PLAY, PacketTypeMap.of(packetTypeClass));
            return map;
        }
        return Collections.emptyMap();
    }

    // ---------------------------------------------------------------------------------

    @Override
    public void registerServerbound(State state, int unmappedPacketId, int mappedPacketId, PacketHandler handler, boolean override) {
        Preconditions.checkArgument(unmappedPacketId != -1, "Unmapped packet id cannot be -1");
        PacketMapping packetMapping = PacketMapping.of(mappedPacketId, handler);
        if (!override && serverboundMappings.hasMapping(state, unmappedPacketId)) {
            Via.getPlatform().getLogger().log(Level.WARNING, unmappedPacketId + " already registered!" +
                    " If this override is intentional, set override to true. Stacktrace: ", new Exception());
        }
        serverboundMappings.addMapping(state, unmappedPacketId, packetMapping);
    }

    @Override
    public void cancelServerbound(State state, int unmappedPacketId) {
        registerServerbound(state, unmappedPacketId, unmappedPacketId, PacketWrapper::cancel);
    }

    @Override
    public void registerClientbound(State state, int unmappedPacketId, int mappedPacketId, PacketHandler handler, boolean override) {
        Preconditions.checkArgument(unmappedPacketId != -1, "Unmapped packet id cannot be -1");
        PacketMapping packetMapping = PacketMapping.of(mappedPacketId, handler);
        if (!override && clientboundMappings.hasMapping(state, unmappedPacketId)) {
            Via.getPlatform().getLogger().log(Level.WARNING, unmappedPacketId + " already registered!" +
                    " If override is intentional, set override to true. Stacktrace: ", new Exception());
        }
        clientboundMappings.addMapping(state, unmappedPacketId, packetMapping);
    }

    @Override
    public void cancelClientbound(State state, int unmappedPacketId) {
        registerClientbound(state, unmappedPacketId, unmappedPacketId, PacketWrapper::cancel);
    }

    // ---------------------------------------------------------------------------------

    @Override
    public void registerClientbound(C1 packetType, @Nullable PacketHandler handler) {
        PacketTypeMap<C2> mappedPacketTypes = packetTypesProvider.mappedClientboundPacketTypes().get(packetType.state());
        C2 mappedPacketType = mappedPacketType(packetType, mappedPacketTypes, unmappedClientboundPacketType, mappedClientboundPacketType);
        registerClientbound(packetType, mappedPacketType, handler);
    }

    @Override
    public void registerClientbound(C1 packetType, @Nullable C2 mappedPacketType, @Nullable PacketHandler handler, boolean override) {
        register(clientboundMappings, packetType, mappedPacketType, unmappedClientboundPacketType, mappedClientboundPacketType, handler, override);
    }

    @Override
    public void cancelClientbound(C1 packetType) {
        registerClientbound(packetType, null, PacketWrapper::cancel);
    }

    @Override
    public void registerServerbound(S2 packetType, @Nullable PacketHandler handler) {
        PacketTypeMap<S1> mappedPacketTypes = packetTypesProvider.mappedServerboundPacketTypes().get(packetType.state());
        S1 mappedPacketType = mappedPacketType(packetType, mappedPacketTypes, unmappedServerboundPacketType, mappedServerboundPacketType);
        registerServerbound(packetType, mappedPacketType, handler);
    }

    @Override
    public void registerServerbound(S2 packetType, @Nullable S1 mappedPacketType, @Nullable PacketHandler handler, boolean override) {
        register(serverboundMappings, packetType, mappedPacketType, unmappedServerboundPacketType, mappedServerboundPacketType, handler, override);
    }

    @Override
    public void cancelServerbound(S2 packetType) {
        registerServerbound(packetType, null, PacketWrapper::cancel);
    }

    private void register(PacketMappings packetMappings, PacketType packetType, @Nullable PacketType mappedPacketType,
                          Class<? extends PacketType> unmappedPacketClass, Class<? extends PacketType> mappedPacketClass,
                          @Nullable PacketHandler handler, boolean override) {
        checkPacketType(packetType, unmappedPacketClass == null || unmappedPacketClass.isInstance(packetType));
        if (mappedPacketType != null) {
            checkPacketType(mappedPacketType, mappedPacketClass == null || mappedPacketClass.isInstance(mappedPacketType));
            Preconditions.checkArgument(packetType.state() == mappedPacketType.state(),
                    "Packet type state does not match mapped packet type state");
            Preconditions.checkArgument(packetType.direction() == mappedPacketType.direction(),
                    "Packet type direction does not match mapped packet type state");
        }

        PacketMapping packetMapping = PacketMapping.of(mappedPacketType, handler);
        if (!override && packetMappings.hasMapping(packetType)) {
            Via.getPlatform().getLogger().log(Level.WARNING, packetType + " already registered!" +
                    " If override is intentional, set override to true. Stacktrace: ", new Exception());
        }
        packetMappings.addMapping(packetType, packetMapping);
    }

    private static <U extends PacketType, M extends PacketType> M mappedPacketType(U packetType, PacketTypeMap<M> mappedTypes, Class<U> unmappedPacketTypeClass, Class<M> mappedPacketTypeClass) {
        Preconditions.checkNotNull(packetType);
        checkPacketType(packetType, unmappedPacketTypeClass == null || unmappedPacketTypeClass.isInstance(packetType));
        if (unmappedPacketTypeClass == mappedPacketTypeClass) {
            //noinspection unchecked
            return (M) packetType;
        }

        Preconditions.checkNotNull(mappedTypes, "Mapped packet types not provided for state %s of type class %s", packetType.state(), mappedPacketTypeClass);
        M mappedType = mappedTypes.typeByName(packetType.getName());
        if (mappedType != null) {
            return mappedType;
        }
        throw new IllegalArgumentException("Packet type " + packetType + " in " + packetType.getClass().getSimpleName() + " could not be automatically mapped!");
    }

    @Override
    public boolean hasRegisteredClientbound(State state, int unmappedPacketId) {
        return clientboundMappings.hasMapping(state, unmappedPacketId);
    }

    @Override
    public boolean hasRegisteredServerbound(State state, int unmappedPacketId) {
        return serverboundMappings.hasMapping(state, unmappedPacketId);
    }

    @Override
    public void transform(Direction direction, State state, PacketWrapper packetWrapper) throws Exception {
        PacketMappings mappings = direction == Direction.CLIENTBOUND ? clientboundMappings : serverboundMappings;
        int unmappedId = packetWrapper.getId();
        PacketMapping packetMapping = mappings.mappedPacket(state, unmappedId);
        if (packetMapping == null) {
            return;
        }

        // Change packet id and apply remapping
        packetMapping.applyType(packetWrapper);
        PacketHandler handler = packetMapping.handler();
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

        PacketType packetType = direction == Direction.CLIENTBOUND ? unmappedClientboundPacketType(state, unmappedPacketId) : unmappedServerboundPacketType(state, unmappedPacketId);
        if (packetType != null) {
            Via.getPlatform().getLogger().warning("ERROR IN " + getClass().getSimpleName() + " IN REMAP OF " + packetType + " (" + toNiceHex(unmappedPacketId) + ")");
        } else {
            Via.getPlatform().getLogger().warning("ERROR IN " + getClass().getSimpleName()
                    + " IN REMAP OF " + toNiceHex(unmappedPacketId) + "->" + toNiceHex(mappedPacketId));
        }
        throw e;
    }

    private @Nullable C1 unmappedClientboundPacketType(final State state, final int packetId) {
        PacketTypeMap<C1> map = packetTypesProvider.unmappedClientboundPacketTypes().get(state);
        return map != null ? map.typeById(packetId) : null;
    }

    private @Nullable S2 unmappedServerboundPacketType(final State state, final int packetId) {
        PacketTypeMap<S2> map = packetTypesProvider.unmappedServerboundPacketTypes().get(state);
        return map != null ? map.typeById(packetId) : null;
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
    private static void checkPacketType(PacketType packetType, boolean isValid) {
        if (!isValid) {
            throw new IllegalArgumentException("Packet type " + packetType + " in " + packetType.getClass().getSimpleName() + " is taken from the wrong packet types class");
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

    public PacketTypesProvider<C1, C2, S1, S2> packetTypesProvider() {
        return packetTypesProvider;
    }

    @Override
    public String toString() {
        return "Protocol:" + getClass().getSimpleName();
    }
}
