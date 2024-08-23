/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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

import com.google.common.collect.Range;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.VersionedPacketTransformer;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.ServerProtocolVersion;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface ProtocolManager {

    /**
     * Returns the server protocol version. Its methods will return -1 if not set yet.
     *
     * @return server protocol version
     */
    ServerProtocolVersion getServerProtocolVersion();

    /**
     * Returns a protocol instance by its class.
     *
     * @param protocolClass class of the protocol
     * @param <T>           protocol
     * @return protocol if present
     */
    @Nullable <T extends Protocol> T getProtocol(Class<T> protocolClass);

    /**
     * Returns a protocol transforming packets for server version to the given client version.
     *
     * @param clientVersion client protocol version
     * @param serverVersion server protocol version
     * @return protocol if present, else null
     */
    @Nullable Protocol getProtocol(ProtocolVersion clientVersion, ProtocolVersion serverVersion);

    /**
     * Returns the base protocol handling serverbound handshake packets.
     *
     * @return base protocol
     */
    Protocol getBaseProtocol();

    /**
     * Returns the base protocols for a specific server and client protocol version.
     * The standard base protocols deal with status and login packets for userconnection initialization.
     *
     * @param clientVersion client protocol version
     * @param serverVersion server protocol version
     * @return base protocols for the given server and client protocol version
     */
    List<Protocol> getBaseProtocols(@Nullable ProtocolVersion clientVersion, @Nullable ProtocolVersion serverVersion);

    /**
     * Returns an immutable collection of registered protocols.
     *
     * @return immutable collection of registered protocols
     */
    Collection<Protocol<?, ?, ?, ?>> getProtocols();

    /**
     * @deprecated use Protocol#isBaseProtocol()
     */
    @Deprecated(forRemoval = true)
    default boolean isBaseProtocol(Protocol protocol) {
        return protocol.isBaseProtocol();
    }

    /**
     * Register and initializes a protocol.
     *
     * @param protocol      protocol to register
     * @param clientVersion supported client protocol versions
     * @param serverVersion output server protocol version the protocol converts to
     * @throws IllegalArgumentException if the client protocol version is equal to the server protocol version
     */
    void registerProtocol(Protocol protocol, ProtocolVersion clientVersion, ProtocolVersion serverVersion);

    /**
     * Register and initializes protocol.
     *
     * @param protocol               protocol to register
     * @param supportedClientVersion supported client protocol versions
     * @param serverVersion          output server protocol version the protocol converts to
     * @throws IllegalArgumentException if a supported client protocol version is equal to the server protocol version
     */
    void registerProtocol(Protocol protocol, List<ProtocolVersion> supportedClientVersion, ProtocolVersion serverVersion);

    /**
     * Registers and initializes a base protocol. Base Protocols registered later have higher priority.
     * Only base protocol will always be added to pipeline.
     *
     * @param direction          direction of the base protocol
     * @param baseProtocol       base protocol to register
     * @param supportedProtocols protocol versions supported by the base protocol
     * @throws IllegalArgumentException if the protocol is not a base protocol as given by {@link Protocol#isBaseProtocol()}
     */
    void registerBaseProtocol(Direction direction, Protocol baseProtocol, Range<ProtocolVersion> supportedProtocols);

    /**
     * Calculates and returns the protocol path from a client protocol version to server protocol version.
     * Returns null if no path could be found or the path length exceeds the value given by {@link #getMaxProtocolPathSize()}.
     *
     * @param clientVersion input client protocol version
     * @param serverVersion desired output server protocol version
     * @return path generated, or null if not supported or the length exceeds {@link #getMaxProtocolPathSize()}
     */
    @Nullable List<ProtocolPathEntry> getProtocolPath(ProtocolVersion clientVersion, ProtocolVersion serverVersion);

    @Deprecated
    default @Nullable List<ProtocolPathEntry> getProtocolPath(int clientVersion, int serverVersion) {
        return getProtocolPath(ProtocolVersion.getProtocol(clientVersion), ProtocolVersion.getProtocol(serverVersion));
    }

    /**
     * Returns a versioned packet transformer to transform and send packets from a given base version to any client version supported by Via.
     * The used packet types have to match the given protocol version.
     * <p>
     * It is important the correct packet type classes are passed. The ViaVersion given packet type enums
     * are found in the common module. Examples for correct invocations are:
     * <pre>
     * createPacketTransformer(ProtocolVersion.v1_17_1, ClientboundPackets1_17_1.class, ServerboundPackets1_17.class);
     * createPacketTransformer(ProtocolVersion.v1_12_2, ClientboundPackets1_12_1.class, ServerboundPackets1_12_1.class);
     * createPacketTransformer(ProtocolVersion.v1_8, ClientboundPackets1_8.class, ServerboundPackets1_8.class);
     * </pre>
     * If only clientbound <b>or</b> serverbound packets are used, the other class can be passed as null, see:
     * <pre>
     * VersionedPacketTransformer&lt;?, ServerboundHandshakePackets&gt; creator
     *     = createPacketTransformer(ProtocolVersion.v1_17_1, null, ServerboundHandshakePackets.class);
     * </pre>
     *
     * @param inputVersion            input protocol version
     * @param clientboundPacketsClass clientbound packets class, or null if no clientbound packets will be sent or transformed with this
     * @param serverboundPacketsClass serverbound packets class, or null if no serverbound packets will be sent or transformed with this
     * @return versioned packet transformer to transform and send packets from a given protocol version
     * @throws IllegalArgumentException if either of the packet classes are the base {@link ClientboundPacketType} or {@link ServerboundPacketType} interfaces
     * @throws IllegalArgumentException if both packet classes are null
     */
    <C extends ClientboundPacketType,
        S extends ServerboundPacketType
        > VersionedPacketTransformer<C, S> createPacketTransformer(ProtocolVersion inputVersion,
                                                                   @Nullable Class<C> clientboundPacketsClass,
                                                                   @Nullable Class<S> serverboundPacketsClass);

    /**
     * Sets the max delta the path calculation allows the distance to the target protocol version to increase.
     * <p>
     * If set to 0, protocol paths will have to come closer to the target protocol version with every entry,
     * never going farther away from it (1→5→10 and 1→11→10 are ok, 1→20→10 is not ok).
     * If set to -1, no distance checks will be applied (1→20→10 is ok).
     *
     * @param maxPathDeltaIncrease the max delta the path calculation allows the distance to the target protocol version to increase
     */
    void setMaxPathDeltaIncrease(int maxPathDeltaIncrease);

    /**
     * Returns the max delta the path calculation allows the distance to the target protocol version to increase. 0 by default.
     * <p>
     * In practice, a value of 0 means a path will never go to a protocol version that puts it farther from the desired
     * server protocol version, even if a path existed.
     * If this is set to -1, *all* possible paths will be checked until a fitting one is found.
     * <p>
     * Negative examples if this returns 0:
     * <ul>
     *     A possible path from 3 to 5 in order of 3→10→5 will be dismissed.
     *     A possible path from 5 to 3 in order of 5→0→3 will be dismissed.
     * </ul>
     * <p>
     * Negative examples if this returns -1:
     * <ul>
     *     While searching for a path from 3 to 5, 3→2→1 could be checked first before 3→4→5 is found.
     *     While searching for a path from 5 to 3, 5→6→7 could be checked first before 5→4→3 is found.
     * </ul>
     *
     * @return max delta the path calculation allows the distance to the target protocol version to increase
     */
    int getMaxPathDeltaIncrease();

    /**
     * Returns the maximum protocol path size applied to {@link #getProtocolPath(ProtocolVersion, ProtocolVersion)}.
     *
     * @return maximum protocol path size
     */
    int getMaxProtocolPathSize();

    /**
     * Sets the maximum protocol path size applied to {@link #getProtocolPath(ProtocolVersion, ProtocolVersion)}.
     * Its default is 50.
     *
     * @param maxProtocolPathSize maximum protocol path size
     */
    void setMaxProtocolPathSize(int maxProtocolPathSize);

    /**
     * Returns the protocol versions compatible with the server.
     *
     * @return sorted, immutable set of supported protocol versions
     */
    SortedSet<ProtocolVersion> getSupportedVersions();

    /**
     * Check if this plugin is useful to the server.
     *
     * @return true if there is a useful pipe
     */
    boolean isWorkingPipe();

    /**
     * Ensure that mapping data for that protocol has already been loaded, completes it otherwise.
     *
     * @param protocolClass protocol class
     */
    void completeMappingDataLoading(Class<? extends Protocol> protocolClass);

    /**
     * Shuts down the executor and uncaches mappings if all futures have been completed.
     *
     * @return true if the executor has now been shut down
     */
    boolean checkForMappingCompletion();

    /**
     * Executes the given runnable asynchronously, adding a {@link CompletableFuture}
     * to the list of data to load bound to their protocols.
     *
     * @param protocolClass protocol class
     * @param runnable      runnable to be executed asynchronously
     */
    void addMappingLoaderFuture(Class<? extends Protocol> protocolClass, Runnable runnable);

    /**
     * Executes the given runnable asynchronously after the other protocol has finished its data loading,
     * adding a {@link CompletableFuture} to the list of data to load bound to their protocols.
     *
     * @param protocolClass protocol class
     * @param dependsOn     class of the protocol that the data loading depends on
     * @param runnable      runnable to be executed asynchronously
     */
    void addMappingLoaderFuture(Class<? extends Protocol> protocolClass, Class<? extends Protocol> dependsOn, Runnable runnable);

    /**
     * Returns the data loading future bound to the protocol, or null if all loading is complete.
     * The future may or may not have already been completed.
     *
     * @param protocolClass protocol class
     * @return data loading future bound to the protocol, or null if all loading is complete
     */
    @Nullable CompletableFuture<Void> getMappingLoaderFuture(Class<? extends Protocol> protocolClass);

    /**
     * Creates a new packet wrapper instance.
     *
     * @param packetType packet type, or null if none should be written to the packet (raw id = -1)
     * @param buf        input buffer
     * @param connection user connection
     * @return new packet wrapper instance
     * @see PacketWrapper#create(PacketType, ByteBuf, UserConnection)
     */
    PacketWrapper createPacketWrapper(@Nullable PacketType packetType, @Nullable ByteBuf buf, UserConnection connection);

    /**
     * Creates a new packet wrapper instance.
     *
     * @param packetId   packet id
     * @param buf        input buffer
     * @param connection user connection
     * @return new packet wrapper instance
     * @deprecated magic id; prefer using {@link #createPacketWrapper(PacketType, ByteBuf, UserConnection)}
     */
    @Deprecated
    PacketWrapper createPacketWrapper(int packetId, @Nullable ByteBuf buf, UserConnection connection);

    /**
     * Returns whether the mappings have been loaded and the mapping loader executor shutdown.
     *
     * @return whether the mappings have been loaded
     */
    boolean hasLoadedMappings();
}
