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
package com.viaversion.viaversion.api.protocol.base;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.AbstractSimpleProtocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

/**
 * Abstract protocol class handling packet transformation between two protocol versions.
 * Clientbound and serverbount packet types can be set to enforce correct usage of them.
 *
 * @param <C1> old clientbound packet types
 * @param <C2> new clientbound packet types
 * @param <S1> old serverbound packet types
 * @param <S2> new serverbound packet types
 * @see AbstractSimpleProtocol for a helper class if you do not want to define any of the types above
 */
public interface Protocol<C1 extends ClientboundPacketType, C2 extends ClientboundPacketType, S1 extends ServerboundPacketType, S2 extends ServerboundPacketType> {

    /**
     * Should this protocol filter an object packet from this class.
     * Default: false
     *
     * @param packetClass The class of the current input
     * @return True if it should handle the filtering
     */
    default boolean isFiltered(Class packetClass) {
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
    void filterPacket(UserConnection info, Object packet, List output) throws Exception;

    /**
     * Loads the mappingdata.
     */
    void loadMappingData();

    /**
     * Handle protocol registration phase, use this to register providers / tasks.
     * <p>
     * To be overridden if needed.
     *
     * @param providers The current providers
     */
    void register(ViaProviders providers);

    /**
     * Initialise a user for this protocol setting up objects.
     * /!\ WARNING - May be called more than once in a single {@link UserConnection}
     * <p>
     * To be overridden if needed.
     *
     * @param userConnection The user to initialise
     */
    void init(UserConnection userConnection);

    /**
     * Register an incoming packet, with simple id transformation.
     *
     * @param state       The state which the packet is sent in.
     * @param oldPacketID The old packet ID
     * @param newPacketID The new packet ID
     */
    void registerIncoming(State state, int oldPacketID, int newPacketID);

    /**
     * Register an incoming packet, with id transformation and remapper.
     *
     * @param state          The state which the packet is sent in.
     * @param oldPacketID    The old packet ID
     * @param newPacketID    The new packet ID
     * @param packetRemapper The remapper to use for the packet
     */
    void registerIncoming(State state, int oldPacketID, int newPacketID, PacketRemapper packetRemapper);

    void registerIncoming(State state, int oldPacketID, int newPacketID, PacketRemapper packetRemapper, boolean override);

    void cancelIncoming(State state, int oldPacketID, int newPacketID);

    void cancelIncoming(State state, int newPacketID);

    /**
     * Register an outgoing packet, with simple id transformation.
     *
     * @param state       The state which the packet is sent in.
     * @param oldPacketID The old packet ID
     * @param newPacketID The new packet ID
     */
    void registerOutgoing(State state, int oldPacketID, int newPacketID);

    /**
     * Register an outgoing packet, with id transformation and remapper.
     *
     * @param state          The state which the packet is sent in.
     * @param oldPacketID    The old packet ID
     * @param newPacketID    The new packet ID
     * @param packetRemapper The remapper to use for the packet
     */
    void registerOutgoing(State state, int oldPacketID, int newPacketID, PacketRemapper packetRemapper);

    void cancelOutgoing(State state, int oldPacketID, int newPacketID);

    void cancelOutgoing(State state, int oldPacketID);

    void registerOutgoing(State state, int oldPacketID, int newPacketID, PacketRemapper packetRemapper, boolean override);

    /**
     * Registers an outgoing protocol and automatically maps it to the new id.
     *
     * @param packetType     clientbound packet type the server sends
     * @param packetRemapper remapper
     */
    void registerOutgoing(C1 packetType, @Nullable PacketRemapper packetRemapper);

    /**
     * Registers an outgoing protocol.
     *
     * @param packetType       clientbound packet type the server initially sends
     * @param mappedPacketType clientbound packet type after transforming for the client
     * @param packetRemapper   remapper
     */
    void registerOutgoing(C1 packetType, C2 mappedPacketType, @Nullable PacketRemapper packetRemapper);

    /**
     * Maps a packet type to another packet type without a packet handler.
     * Note that this should not be called for simple channel mappings of the same packet; this is already done automatically.
     *
     * @param packetType       clientbound packet type the server initially sends
     * @param mappedPacketType clientbound packet type after transforming for the client
     */
    void registerOutgoing(C1 packetType, C2 mappedPacketType);

    void cancelOutgoing(C1 packetType);

    /**
     * Registers an incoming protocol and automatically maps it to the server's id.
     *
     * @param packetType     serverbound packet type the client sends
     * @param packetRemapper remapper
     */
    void registerIncoming(S2 packetType, @Nullable PacketRemapper packetRemapper);

    /**
     * Registers an incoming protocol.
     *
     * @param packetType       serverbound packet type initially sent by the client
     * @param mappedPacketType serverbound packet type after transforming for the server
     * @param packetRemapper   remapper
     */
    void registerIncoming(S2 packetType, S1 mappedPacketType, @Nullable PacketRemapper packetRemapper);

    void cancelIncoming(S2 packetType);

    /**
     * Checks if an outgoing packet has already been registered.
     *
     * @param state       state which the packet is sent in
     * @param oldPacketID old packet ID
     * @return true if already registered
     */
    boolean hasRegisteredOutgoing(State state, int oldPacketID);

    /**
     * Checks if an incoming packet has already been registered.
     *
     * @param state       state which the packet is sent in
     * @param newPacketId packet ID
     * @return true if already registered
     */
    boolean hasRegisteredIncoming(State state, int newPacketId);

    /**
     * Transform a packet using this protocol
     *
     * @param direction     The direction the packet is going in
     * @param state         The current protocol state
     * @param packetWrapper The packet wrapper to transform
     * @throws Exception Throws exception if it fails to transform
     */
    void transform(Direction direction, State state, PacketWrapper packetWrapper) throws Exception;

    @Nullable <T> T get(Class<T> objectClass);

    void put(Object object);

    /**
     * Returns true if this Protocol's {@link #loadMappingData()} method should be called.
     * <p>
     * This does *not* necessarily mean that {@link #getMappingData()} is non-null, since this may be
     * overriden, depending on special cases.
     *
     * @return true if this Protocol's {@link #loadMappingData()} method should be called
     */
    boolean hasMappingDataToLoad();

    default @Nullable MappingData getMappingData() {
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
