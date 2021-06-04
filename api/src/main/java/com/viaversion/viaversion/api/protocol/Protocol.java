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

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.rewriter.EntityRewriter;
import com.viaversion.viaversion.api.rewriter.ItemRewriter;
import org.checkerframework.checker.nullness.qual.Nullable;

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
public interface Protocol<C1 extends ClientboundPacketType, C2 extends ClientboundPacketType, S1 extends ServerboundPacketType, S2 extends ServerboundPacketType> {

    /**
     * Register a serverbound packet, with simple id transformation.
     *
     * @param state       The state which the packet is sent in.
     * @param oldPacketID The old packet ID
     * @param newPacketID The new packet ID
     */
    default void registerServerbound(State state, int oldPacketID, int newPacketID) {
        registerServerbound(state, oldPacketID, newPacketID, null);
    }

    /**
     * Register a serverbound packet, with id transformation and remapper.
     *
     * @param state          The state which the packet is sent in.
     * @param oldPacketID    The old packet ID
     * @param newPacketID    The new packet ID
     * @param packetRemapper The remapper to use for the packet
     */
    default void registerServerbound(State state, int oldPacketID, int newPacketID, PacketRemapper packetRemapper) {
        registerServerbound(state, oldPacketID, newPacketID, packetRemapper, false);
    }

    void registerServerbound(State state, int oldPacketID, int newPacketID, PacketRemapper packetRemapper, boolean override);

    void cancelServerbound(State state, int oldPacketID, int newPacketID);

    default void cancelServerbound(State state, int newPacketID) {
        cancelServerbound(state, -1, newPacketID);
    }

    /**
     * Register a clientbound packet, with simple id transformation.
     *
     * @param state       The state which the packet is sent in.
     * @param oldPacketID The old packet ID
     * @param newPacketID The new packet ID
     */
    default void registerClientbound(State state, int oldPacketID, int newPacketID) {
        registerClientbound(state, oldPacketID, newPacketID, null);
    }

    /**
     * Register a clientbound packet, with id transformation and remapper.
     *
     * @param state          The state which the packet is sent in.
     * @param oldPacketID    The old packet ID
     * @param newPacketID    The new packet ID
     * @param packetRemapper The remapper to use for the packet
     */
    default void registerClientbound(State state, int oldPacketID, int newPacketID, PacketRemapper packetRemapper) {
        registerClientbound(state, oldPacketID, newPacketID, packetRemapper, false);
    }

    void cancelClientbound(State state, int oldPacketID, int newPacketID);

    default void cancelClientbound(State state, int oldPacketID) {
        cancelClientbound(state, oldPacketID, -1);
    }

    void registerClientbound(State state, int oldPacketID, int newPacketID, PacketRemapper packetRemapper, boolean override);

    /**
     * Registers a clientbound protocol and automatically maps it to the new id.
     *
     * @param packetType     clientbound packet type the server sends
     * @param packetRemapper remapper
     */
    void registerClientbound(C1 packetType, @Nullable PacketRemapper packetRemapper);

    /**
     * Registers a clientbound protocol.
     *
     * @param packetType       clientbound packet type the server initially sends
     * @param mappedPacketType clientbound packet type after transforming for the client
     * @param packetRemapper   remapper
     */
    void registerClientbound(C1 packetType, C2 mappedPacketType, @Nullable PacketRemapper packetRemapper);

    /**
     * Maps a packet type to another packet type without a packet handler.
     * Note that this should not be called for simple channel mappings of the same packet; this is already done automatically.
     *
     * @param packetType       clientbound packet type the server initially sends
     * @param mappedPacketType clientbound packet type after transforming for the client
     */
    default void registerClientbound(C1 packetType, @Nullable C2 mappedPacketType) {
        registerClientbound(packetType, mappedPacketType, null);
    }

    /**
     * Cancels any clientbound packets from the given type.
     *
     * @param packetType clientbound packet type to cancel
     */
    void cancelClientbound(C1 packetType);

    /**
     * Registers a serverbound protocol and automatically maps it to the server's id.
     *
     * @param packetType     serverbound packet type the client sends
     * @param packetRemapper remapper
     */
    void registerServerbound(S2 packetType, @Nullable PacketRemapper packetRemapper);

    /**
     * Registers a serverbound protocol.
     *
     * @param packetType       serverbound packet type initially sent by the client
     * @param mappedPacketType serverbound packet type after transforming for the server
     * @param packetRemapper   remapper
     */
    void registerServerbound(S2 packetType, @Nullable S1 mappedPacketType, @Nullable PacketRemapper packetRemapper);

    /**
     * Cancels any serverbound packets from the given type.
     *
     * @param packetType serverbound packet type to cancel
     */
    void cancelServerbound(S2 packetType);

    /**
     * Checks if a clientbound packet has already been registered.
     *
     * @param state       state which the packet is sent in
     * @param oldPacketID old packet ID
     * @return true if already registered
     */
    boolean hasRegisteredClientbound(State state, int oldPacketID);

    /**
     * Checks if a serverbound packet has already been registered.
     *
     * @param state       state which the packet is sent in
     * @param newPacketId packet ID
     * @return true if already registered
     */
    boolean hasRegisteredServerbound(State state, int newPacketId);

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
    boolean hasMappingDataToLoad();

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
     * @param userConnection The user to initialise
     */
    default void init(UserConnection userConnection) {
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
    default @Nullable EntityRewriter getEntityRewriter() {
        return null;
    }

    /**
     * Returns the protocol's item rewriter if present.
     *
     * @return item rewriter
     */
    default @Nullable ItemRewriter getItemRewriter() {
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
}
