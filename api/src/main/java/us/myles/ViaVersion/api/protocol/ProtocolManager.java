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

import com.google.common.collect.Range;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.CompletableFuture;

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
     * @return protocol if present
     */
    @Nullable
    Protocol getProtocol(Class<? extends Protocol> protocolClass);

    Protocol getBaseProtocol();

    Protocol getBaseProtocol(int serverVersion);

    boolean isBaseProtocol(Protocol protocol);

    /**
     * Register a protocol.
     *
     * @param protocol  protocol to register
     * @param supported supported client versions
     * @param output    output server version the protocol converts to
     */
    void registerProtocol(Protocol protocol, ProtocolVersion supported, ProtocolVersion output);

    /**
     * Register a protocol.
     *
     * @param protocol  protocol to register
     * @param supported supported client versions
     * @param output    output server version the protocol converts to
     */
    void registerProtocol(Protocol protocol, List<Integer> supported, int output);

    /**
     * Registers a base protocol. Base Protocols registered later have higher priority.
     * Only base protocol will always be added to pipeline.
     *
     * @param baseProtocol       base protocol to register
     * @param supportedProtocols versions supported by the base protocol
     */
    void registerBaseProtocol(Protocol baseProtocol, Range<Integer> supportedProtocols);

    /**
     * Calculates a path from a client version to server version.
     *
     * @param clientVersion input client version
     * @param serverVersion desired output server version
     * @return path it generated, null if not supported
     */
    @Nullable
    List<ProtocolPathEntry> getProtocolPath(int clientVersion, int serverVersion);

    /**
     * Returns the maximum protocol path size applied to {@link #getProtocolPath(int, int)}.
     *
     * @return maximum protocol path size
     */
    int getMaxProtocolPathSize();

    /**
     * Sets the maximum protocol path size applied to {@link #getProtocolPath(int, int)}.
     * Its default it 50.
     *
     * @param maxProtocolPathSize maximum protocol path size
     */
    void setMaxProtocolPathSize(int maxProtocolPathSize);

    /**
     * Returns the versions compatible with the server.
     *
     * @return sorted, immutable set of supported versions
     */
    SortedSet<Integer> getSupportedVersions();

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
    void completeMappingDataLoading(Class<? extends Protocol> protocolClass) throws Exception;

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
    @Nullable
    CompletableFuture<Void> getMappingLoaderFuture(Class<? extends Protocol> protocolClass);
}
