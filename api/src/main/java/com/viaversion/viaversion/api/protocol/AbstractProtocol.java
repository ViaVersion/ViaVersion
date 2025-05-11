/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
import com.viaversion.viaversion.api.data.item.ItemHasher;
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
import com.viaversion.viaversion.api.rewriter.MappingDataListener;
import com.viaversion.viaversion.api.rewriter.Rewriter;
import com.viaversion.viaversion.exception.CancelException;
import com.viaversion.viaversion.exception.InformativeException;
import com.viaversion.viaversion.util.ProtocolLogger;
import com.viaversion.viaversion.util.ProtocolUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.viaversion.viaversion.util.ProtocolUtil.packetTypeMap;

/**
 * Abstract protocol class to handle packet transformation between two protocol versions.
 *
 * @param <CU> unmapped clientbound packet type
 * @param <CM> mapped clientbound packet type
 * @param <SM> mapped serverbound packet type
 * @param <SU> unmapped serverbound packet type
 */
public abstract class AbstractProtocol<CU extends ClientboundPacketType, CM extends ClientboundPacketType,
    SM extends ServerboundPacketType, SU extends ServerboundPacketType> implements Protocol<CU, CM, SM, SU> {
    protected final Class<CU> unmappedClientboundPacketType;
    protected final Class<CM> mappedClientboundPacketType;
    protected final Class<SM> mappedServerboundPacketType;
    protected final Class<SU> unmappedServerboundPacketType;
    protected final PacketTypesProvider<CU, CM, SM, SU> packetTypesProvider;
    protected final PacketMappings clientboundMappings;
    protected final PacketMappings serverboundMappings;
    private final Map<Class<?>, Object> storedObjects = new HashMap<>();
    private boolean initialized;
    private ProtocolLogger logger;

    @Deprecated
    protected AbstractProtocol() {
        this(null, null, null, null);
    }

    /**
     * Creates a protocol with automated id mapping if the respective packet type classes are not null.
     * They are also required to track the CONFIGURATION state.
     */
    protected AbstractProtocol(@Nullable Class<CU> unmappedClientboundPacketType, @Nullable Class<CM> mappedClientboundPacketType,
                               @Nullable Class<SM> mappedServerboundPacketType, @Nullable Class<SU> unmappedServerboundPacketType) {
        this.unmappedClientboundPacketType = unmappedClientboundPacketType;
        this.mappedClientboundPacketType = mappedClientboundPacketType;
        this.mappedServerboundPacketType = mappedServerboundPacketType;
        this.unmappedServerboundPacketType = unmappedServerboundPacketType;
        this.packetTypesProvider = createPacketTypesProvider();
        this.clientboundMappings = createClientboundPacketMappings();
        this.serverboundMappings = createServerboundPacketMappings();
    }

    @Override
    public final void initialize() {
        Preconditions.checkArgument(!initialized, "Protocol has already been initialized");
        initialized = true;

        // Create logger if protocol does not have one
        if (getLogger() == null) {
            logger = new ProtocolLogger(getClass());
        }
        registerPackets();
        registerConfigurationChangeHandlers();

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

    protected void registerConfigurationChangeHandlers() {
        // Register handlers for protocol state switching
        // Assuming ids will change too often, it is cleaner to register them here instead of the base protocols,
        // even if there will be multiple of these handlers
        final SU configurationAcknowledgedPacket = configurationAcknowledgedPacket();
        if (configurationAcknowledgedPacket != null) {
            appendServerbound(configurationAcknowledgedPacket, setClientStateHandler(State.CONFIGURATION));
        }

        final CU startConfigurationPacket = startConfigurationPacket();
        if (startConfigurationPacket != null) {
            appendClientbound(startConfigurationPacket, setServerStateHandler(State.CONFIGURATION));
        }

        final SU finishConfigurationPacket = serverboundFinishConfigurationPacket();
        if (finishConfigurationPacket != null) {
            appendServerbound(finishConfigurationPacket, setClientStateHandler(State.PLAY));
        }

        final CU clientboundFinishConfigurationPacket = clientboundFinishConfigurationPacket();
        if (clientboundFinishConfigurationPacket != null) {
            appendClientbound(clientboundFinishConfigurationPacket, setServerStateHandler(State.PLAY));
        }
    }

    @Override
    public void appendClientbound(final CU type, final PacketHandler handler) {
        final PacketMapping mapping = clientboundMappings.mappedPacket(type.state(), type.getId());
        if (mapping != null) {
            mapping.appendHandler(handler);
        } else {
            registerClientbound(type, handler);
        }
    }

    @Override
    public void appendServerbound(final SU type, final PacketHandler handler) {
        final PacketMapping mapping = serverboundMappings.mappedPacket(type.state(), type.getId());
        if (mapping != null) {
            mapping.appendHandler(handler);
        } else {
            registerServerbound(type, handler);
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
            PacketTypeMap<U> unmappedTypes = unmappedPacketTypes.get(entry.getKey());
            for (U unmappedType : unmappedTypes.types()) {
                M mappedType = mappedTypes.typeByName(unmappedType.getName());
                if (mappedType == null) {
                    // No mapped packet of the same name exists
                    Preconditions.checkArgument(registeredPredicate.test(unmappedType), "Packet %s in %s has no mapping - it needs to be manually cancelled or remapped", unmappedType, getClass());
                    continue;
                }

                // Register if no custom handler exists and ids are different
                if (unmappedType.getId() != mappedType.getId() && !registeredPredicate.test(unmappedType)) {
                    registerConsumer.accept(unmappedType, mappedType);

                }
            }
        }
    }

    public void registerFinishConfiguration(final CU packetType, final PacketHandler handler) {
        registerClientbound(packetType, wrapper -> {
            // TODO Temporary solution to handle the finish configuration packet already having changed our tracked protocol state in a previous handler
            wrapper.user().getProtocolInfo().setServerState(State.CONFIGURATION);
            handler.handle(wrapper);
        });
    }

    @Override
    public final void loadMappingData() {
        getMappingData().load();
        onMappingDataLoaded();
    }

    /**
     * Register the packets for this protocol. To be overridden.
     */
    protected void registerPackets() {
        callRegister(getEntityRewriter());
        callRegister(getItemRewriter());
    }

    /**
     * Called after {@link #loadMappingData()} is called; load extra mapping data for the protocol.
     * <p>
     * To be overridden if needed.
     */
    protected void onMappingDataLoaded() {
        callOnMappingDataLoaded(getEntityRewriter());
        callOnMappingDataLoaded(getItemRewriter());
        callOnMappingDataLoaded(getTagRewriter());
    }

    private void callRegister(@Nullable Rewriter<?> rewriter) {
        if (rewriter != null) {
            rewriter.register();
        }
    }

    private void callOnMappingDataLoaded(@Nullable MappingDataListener rewriter) {
        if (rewriter != null) {
            rewriter.onMappingDataLoaded();
        }
    }

    protected void addEntityTracker(UserConnection connection, EntityTracker tracker) {
        connection.addEntityTracker(this.getClass(), tracker);
    }

    protected void addItemHasher(UserConnection connection, ItemHasher hasher) {
        connection.addItemHasher(this.getClass(), hasher);
    }

    protected PacketTypesProvider<CU, CM, SM, SU> createPacketTypesProvider() {
        return new SimplePacketTypesProvider<>(
            packetTypeMap(unmappedClientboundPacketType, unmappedClientboundPacketType),
            packetTypeMap(mappedClientboundPacketType, mappedClientboundPacketType),
            packetTypeMap(mappedServerboundPacketType, mappedServerboundPacketType),
            packetTypeMap(unmappedServerboundPacketType, unmappedServerboundPacketType)
        );
    }

    protected PacketMappings createClientboundPacketMappings() {
        return PacketMappings.arrayMappings();
    }

    protected PacketMappings createServerboundPacketMappings() {
        return PacketMappings.arrayMappings();
    }

    protected @Nullable SU configurationAcknowledgedPacket() {
        return packetTypesProvider.unmappedServerboundType(State.PLAY, "CONFIGURATION_ACKNOWLEDGED");
    }

    protected @Nullable CU startConfigurationPacket() {
        return packetTypesProvider.unmappedClientboundType(State.PLAY, "START_CONFIGURATION");
    }

    protected @Nullable SU serverboundFinishConfigurationPacket() {
        return packetTypesProvider.unmappedServerboundType(State.CONFIGURATION, "FINISH_CONFIGURATION");
    }

    protected @Nullable CU clientboundFinishConfigurationPacket() {
        return packetTypesProvider.unmappedClientboundType(State.CONFIGURATION, "FINISH_CONFIGURATION");
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
    public void registerClientbound(CU packetType, @Nullable PacketHandler handler) {
        PacketTypeMap<CM> mappedPacketTypes = packetTypesProvider.mappedClientboundPacketTypes().get(packetType.state());
        CM mappedPacketType = mappedPacketType(packetType, mappedPacketTypes, unmappedClientboundPacketType, mappedClientboundPacketType);
        registerClientbound(packetType, mappedPacketType, handler);
    }

    @Override
    public void registerClientbound(CU packetType, @Nullable CM mappedPacketType, @Nullable PacketHandler handler, boolean override) {
        register(clientboundMappings, packetType, mappedPacketType, unmappedClientboundPacketType, mappedClientboundPacketType, handler, override);
    }

    @Override
    public void cancelClientbound(CU packetType) {
        registerClientbound(packetType, null, PacketWrapper::cancel);
    }

    @Override
    public void registerServerbound(SU packetType, @Nullable PacketHandler handler) {
        PacketTypeMap<SM> mappedPacketTypes = packetTypesProvider.mappedServerboundPacketTypes().get(packetType.state());
        SM mappedPacketType = mappedPacketType(packetType, mappedPacketTypes, unmappedServerboundPacketType, mappedServerboundPacketType);
        registerServerbound(packetType, mappedPacketType, handler);
    }

    @Override
    public void registerServerbound(SU packetType, @Nullable SM mappedPacketType, @Nullable PacketHandler handler, boolean override) {
        register(serverboundMappings, packetType, mappedPacketType, unmappedServerboundPacketType, mappedServerboundPacketType, handler, override);
    }

    @Override
    public void cancelServerbound(SU packetType) {
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
            getLogger().log(Level.WARNING, packetType + " already registered!" +
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
    public void transform(Direction direction, State state, PacketWrapper packetWrapper) throws InformativeException, CancelException {
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
            } catch (InformativeException e) {
                e.addSource(handler.getClass());
                printRemapError(direction, state, unmappedId, packetWrapper.getId(), e);
                throw e;
            } catch (Exception e) {
                // Wrap other exceptions during packet handling
                InformativeException ex = new InformativeException(e);
                ex.addSource(handler.getClass());
                printRemapError(direction, state, unmappedId, packetWrapper.getId(), ex);
                throw ex;
            }

            if (packetWrapper.isCancelled()) {
                throw CancelException.generate();
            }
        }
    }

    @Override
    public ProtocolLogger getLogger() {
        return logger;
    }

    private void printRemapError(Direction direction, State state, int unmappedPacketId, int mappedPacketId, InformativeException e) {
        // Don't print errors during handshake/login/status
        if (state != State.PLAY && direction == Direction.SERVERBOUND && !Via.getManager().debugHandler().enabled()) {
            e.setShouldBePrinted(false);
            return;
        }

        PacketType packetType = direction == Direction.CLIENTBOUND
            ? packetTypesProvider.unmappedClientboundType(state, unmappedPacketId)
            : packetTypesProvider.unmappedServerboundType(state, unmappedPacketId);
        if (packetType != null) {
            Via.getPlatform().getLogger().warning("ERROR IN " + getClass().getSimpleName() + " IN REMAP OF " + packetType + " (" + ProtocolUtil.toNiceHex(unmappedPacketId) + ")");
        } else {
            Via.getPlatform().getLogger().warning("ERROR IN " + getClass().getSimpleName()
                + " IN REMAP OF " + state + " " + ProtocolUtil.toNiceHex(unmappedPacketId) + "->" + ProtocolUtil.toNiceHex(mappedPacketId));
        }
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

    private PacketHandler setClientStateHandler(final State state) {
        return wrapper -> wrapper.user().getProtocolInfo().setClientState(state);
    }

    private PacketHandler setServerStateHandler(final State state) {
        return wrapper -> wrapper.user().getProtocolInfo().setServerState(state);
    }

    @Override
    public final PacketTypesProvider<CU, CM, SM, SU> getPacketTypesProvider() {
        return packetTypesProvider;
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
}
