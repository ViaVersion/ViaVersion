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

import com.google.common.annotations.Beta;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.rewriter.EntityRewriter;
import com.viaversion.viaversion.api.rewriter.ItemRewriter;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Abstract protocol class handling packet transformation between two protocol versions.
 * Clientbound and serverbount packet types can be set to enforce correct usage of them.
 *
 * @param <CU> unmapped clientbound packet types
 * @param <CM> mapped clientbound packet types
 * @param <SM> mapped serverbound packet types
 * @param <SU> unmapped serverbound packet types
 * @see SimpleProtocol for a helper class if you do not need to define any of the types above
 */
public interface Protocol<CU extends ClientboundPacketType, CM extends ClientboundPacketType, SM extends ServerboundPacketType, SU extends ServerboundPacketType> {

    default void registerServerbound(State state, int unmappedPacketId, int mappedPacketId) {
        registerServerbound(state, unmappedPacketId, mappedPacketId, (PacketHandler) null);
    }

    default void registerServerbound(State state, int unmappedPacketId, int mappedPacketId, PacketHandler handler) {
        registerServerbound(state, unmappedPacketId, mappedPacketId, handler, false);
    }

    /**
     * Registers a serverbound packet, with id transformation and remapper.
     *
     * @param state            state which the packet is sent in.
     * @param unmappedPacketId unmapped packet id
     * @param mappedPacketId   mapped packet id
     * @param handler          packet handler
     * @param override         whether an existing mapper should be overridden
     * @see #registerServerbound(ServerboundPacketType, ServerboundPacketType, PacketHandler, boolean)
     */
    void registerServerbound(State state, int unmappedPacketId, int mappedPacketId, PacketHandler handler, boolean override);

    void cancelServerbound(State state, int mappedPacketId);

    default void registerClientbound(State state, int unmappedPacketId, int mappedPacketId) {
        registerClientbound(state, unmappedPacketId, mappedPacketId, (PacketHandler) null);
    }

    default void registerClientbound(State state, int unmappedPacketId, int mappedPacketId, PacketHandler handler) {
        registerClientbound(state, unmappedPacketId, mappedPacketId, handler, false);
    }

    void cancelClientbound(State state, int unmappedPacketId);

    /**
     * Registers a clientbound packet, with id transformation and remapper.
     *
     * @param state            state which the packet is sent in.
     * @param unmappedPacketId unmapped packet id
     * @param mappedPacketId   mapped packet id
     * @param handler          packet handler
     * @param override         whether an existing mapper should be overridden
     * @see #registerClientbound(ClientboundPacketType, ClientboundPacketType, PacketHandler, boolean)
     */
    void registerClientbound(State state, int unmappedPacketId, int mappedPacketId, PacketHandler handler, boolean override);

    // ---------------------------------------------------------------------------------------

    /**
     * Registers a clientbound protocol and automatically maps it to the new id.
     *
     * @param packetType clientbound packet type the server sends
     * @param handler    packet handler
     */
    void registerClientbound(CU packetType, @Nullable PacketHandler handler);

    /**
     * Maps a packet type to another packet type without a packet handler.
     * Note that this should not be called for simple channel mappings of the same packet; this is already done automatically.
     *
     * @param packetType       clientbound packet type the server initially sends
     * @param mappedPacketType clientbound packet type after transforming for the client
     */
    default void registerClientbound(CU packetType, @Nullable CM mappedPacketType) {
        registerClientbound(packetType, mappedPacketType, (PacketHandler) null);
    }

    /**
     * Registers a clientbound packet mapping.
     *
     * @param packetType       clientbound packet type the server initially sends
     * @param mappedPacketType clientbound packet type after transforming for the client
     * @param handler          packet handler
     */
    default void registerClientbound(CU packetType, @Nullable CM mappedPacketType, @Nullable PacketHandler handler) {
        registerClientbound(packetType, mappedPacketType, handler, false);
    }

    /**
     * Registers a clientbound packet mapping.
     *
     * @param packetType       clientbound packet type the server initially sends
     * @param mappedPacketType clientbound packet type after transforming for the client
     * @param handler          packet handler
     * @param override         whether an existing mapping should be overridden if present
     */
    void registerClientbound(CU packetType, @Nullable CM mappedPacketType, @Nullable PacketHandler handler, boolean override);

    /**
     * Cancels any clientbound packets from the given type.
     *
     * @param packetType clientbound packet type to cancel
     */
    void cancelClientbound(CU packetType);

    /**
     * Maps a packet type to another packet type without a packet handler.
     * Note that this should not be called for simple channel mappings of the same packet; this is already done automatically.
     *
     * @param packetType       serverbound packet type the client initially sends
     * @param mappedPacketType serverbound packet type after transforming for the client
     */
    default void registerServerbound(SU packetType, @Nullable SM mappedPacketType) {
        registerServerbound(packetType, mappedPacketType, (PacketHandler) null);
    }

    /**
     * Registers a serverbound protocol and automatically maps it to the server's id.
     *
     * @param packetType serverbound packet type the client sends
     * @param handler    packet handler
     */
    void registerServerbound(SU packetType, @Nullable PacketHandler handler);

    /**
     * Registers a serverbound protocol.
     *
     * @param packetType       serverbound packet type initially sent by the client
     * @param mappedPacketType serverbound packet type after transforming for the server
     * @param handler          packet handler
     */
    default void registerServerbound(SU packetType, @Nullable SM mappedPacketType, @Nullable PacketHandler handler) {
        registerServerbound(packetType, mappedPacketType, handler, false);
    }

    /**
     * Registers a serverbound packet mapping.
     *
     * @param packetType       serverbound packet type initially sent by the client
     * @param mappedPacketType serverbound packet type after transforming for the server
     * @param handler          packet handler
     * @param override         whether an existing mapping should be overridden if present
     */
    void registerServerbound(SU packetType, @Nullable SM mappedPacketType, @Nullable PacketHandler handler, boolean override);

    /**
     * Cancels any serverbound packets from the given type.
     *
     * @param packetType serverbound packet type to cancel
     */
    void cancelServerbound(SU packetType);

    /**
     * Checks if a clientbound packet has already been registered.
     *
     * @param packetType clientbound packet type
     * @return true if already registered
     */
    default boolean hasRegisteredClientbound(CU packetType) {
        return hasRegisteredClientbound(packetType.state(), packetType.getId());
    }

    /**
     * Checks if a serverbound packet has already been registered.
     *
     * @param packetType serverbound packet type
     * @return true if already registered
     */
    default boolean hasRegisteredServerbound(SU packetType) {
        return hasRegisteredServerbound(packetType.state(), packetType.getId());
    }

    /**
     * Checks if a clientbound packet has already been registered.
     *
     * @param state            state which the packet is sent in
     * @param unmappedPacketId unmapped packet id
     * @return true if already registered
     */
    boolean hasRegisteredClientbound(State state, int unmappedPacketId);

    /**
     * Checks if a serverbound packet has already been registered.
     *
     * @param state            state which the packet is sent in
     * @param unmappedPacketId mapped packet id
     * @return true if already registered
     */
    boolean hasRegisteredServerbound(State state, int unmappedPacketId);

    /**
     * Transform a packet using this protocol
     *
     * @param direction     The direction the packet is going in
     * @param state         The current protocol state
     * @param packetWrapper The packet wrapper to transform
     * @throws Exception Throws exception if it fails to transform
     */
    void transform(Direction direction, State state, PacketWrapper packetWrapper) throws Exception;

    /**
     * Returns a packet type provider for this protocol to get packet types by id.
     * Depending on the Protocol, not every state may be populated.
     *
     * @return the packet types provider
     */
    @Beta
    PacketTypesProvider<CU, CM, SM, SU> getPacketTypesProvider();

    /**
     * Returns a cached object by the given type if present.
     *
     * @param objectClass class of the object to get
     * @param <T>         type
     * @return object if present, else null
     */
    @Nullable <T> T get(Class<T> objectClass);

    /**
     * Caches an object, retrievable by using {@link #get(Class)}.
     *
     * @param object object to cache
     */
    void put(Object object);

    /**
     * Called with {@link ProtocolManager#registerProtocol} to register packet handlers and automatic packet id remapping.
     *
     * @throws IllegalArgumentException if this method has already been called
     */
    void initialize();

    /**
     * Returns true if this Protocol's {@link #loadMappingData()} method should be called.
     * <p>
     * This does *not* necessarily mean that {@link #getMappingData()} is non-null, since this may be
     * overriden, depending on special cases.
     *
     * @return true if this Protocol's {@link #loadMappingData()} method should be called
     */
    default boolean hasMappingDataToLoad() {
        return getMappingData() != null;
    }

    /**
     * Loads the protocol's mapping data.
     *
     * @throws NullPointerException if this protocol has no mapping data
     */
    void loadMappingData();

    /**
     * Handle protocol registration phase, use this to register providers / tasks.
     * <p>
     * To be overridden if needed.
     *
     * @param providers The current providers
     */
    default void register(ViaProviders providers) {
    }

    /**
     * Initialise a user for this protocol setting up objects.
     * /!\ WARNING - May be called more than once in a single {@link UserConnection}
     * <p>
     * To be overridden if needed.
     *
     * @param connection user to initialise
     */
    default void init(UserConnection connection) {
    }

    /**
     * Returns the protocol's mapping data if present.
     *
     * @return mapping data if present
     */
    default @Nullable MappingData getMappingData() {
        return null;
    }

    /**
     * Returns the protocol's entity rewriter if present.
     *
     * @return entity rewriter
     */
    default @Nullable EntityRewriter<?> getEntityRewriter() {
        return null;
    }

    /**
     * Returns the protocol's item rewriter if present.
     *
     * @return item rewriter
     */
    default @Nullable ItemRewriter<?> getItemRewriter() {
        return null;
    }

    /**
     * Returns whether this protocol is a base protocol.
     *
     * @return whether this represents a base protocol
     */
    default boolean isBaseProtocol() {
        return false;
    }

    // ---------------------------------------------------------

    /**
     * @deprecated use {@link #cancelServerbound(State, int)}
     */
    @Deprecated/*(forRemoval = true)*/
    default void cancelServerbound(State state, int unmappedPacketId, int mappedPacketId) {
        cancelServerbound(state, unmappedPacketId);
    }

    /**
     * @deprecated use {@link #cancelClientbound(State, int)}
     */
    @Deprecated/*(forRemoval = true)*/
    default void cancelClientbound(State state, int unmappedPacketId, int mappedPacketId) {
        cancelClientbound(state, unmappedPacketId);
    }

    @Deprecated/*(forRemoval = true)*/
    default void registerClientbound(State state, int unmappedPacketId, int mappedPacketId, PacketRemapper packetRemapper) {
        registerClientbound(state, unmappedPacketId, mappedPacketId, packetRemapper.asPacketHandler(), false);
    }

    @Deprecated/*(forRemoval = true)*/
    default void registerClientbound(State state, int unmappedPacketId, int mappedPacketId, PacketRemapper packetRemapper, boolean override) {
        registerClientbound(state, unmappedPacketId, mappedPacketId, packetRemapper.asPacketHandler(), override);
    }

    @Deprecated/*(forRemoval = true)*/
    default void registerClientbound(CU packetType, @Nullable PacketRemapper packetRemapper) {
        registerClientbound(packetType, packetRemapper.asPacketHandler());
    }

    @Deprecated/*(forRemoval = true)*/
    default void registerClientbound(CU packetType, @Nullable CM mappedPacketType, @Nullable PacketRemapper packetRemapper) {
        registerClientbound(packetType, mappedPacketType, packetRemapper.asPacketHandler(), false);
    }

    @Deprecated/*(forRemoval = true)*/
    default void registerClientbound(CU packetType, @Nullable CM mappedPacketType, @Nullable PacketRemapper packetRemapper, boolean override) {
        registerClientbound(packetType, mappedPacketType, packetRemapper.asPacketHandler(), override);
    }

    @Deprecated/*(forRemoval = true)*/
    default void registerServerbound(State state, int unmappedPacketId, int mappedPacketId, PacketRemapper packetRemapper) {
        registerServerbound(state, unmappedPacketId, mappedPacketId, packetRemapper.asPacketHandler(), false);
    }

    @Deprecated/*(forRemoval = true)*/
    default void registerServerbound(State state, int unmappedPacketId, int mappedPacketId, PacketRemapper packetRemapper, boolean override) {
        registerServerbound(state, unmappedPacketId, mappedPacketId, packetRemapper.asPacketHandler(), override);
    }

    @Deprecated/*(forRemoval = true)*/
    default void registerServerbound(SU packetType, @Nullable PacketRemapper packetRemapper) {
        registerServerbound(packetType, packetRemapper.asPacketHandler());
    }

    @Deprecated/*(forRemoval = true)*/
    default void registerServerbound(SU packetType, @Nullable SM mappedPacketType, @Nullable PacketRemapper packetRemapper) {
        registerServerbound(packetType, mappedPacketType, packetRemapper.asPacketHandler(), false);
    }

    @Deprecated/*(forRemoval = true)*/
    default void registerServerbound(SU packetType, @Nullable SM mappedPacketType, @Nullable PacketRemapper packetRemapper, boolean override) {
        registerServerbound(packetType, mappedPacketType, packetRemapper.asPacketHandler(), override);
    }
}
