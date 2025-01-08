/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.protocol;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingDataLoader;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.ProtocolManager;
import com.viaversion.viaversion.api.protocol.ProtocolPathEntry;
import com.viaversion.viaversion.api.protocol.ProtocolPathKey;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.VersionedPacketTransformer;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.ServerProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.VersionType;
import com.viaversion.viaversion.protocol.packet.PacketWrapperImpl;
import com.viaversion.viaversion.protocol.packet.VersionedPacketTransformerImpl;
import com.viaversion.viaversion.protocols.base.InitialBaseProtocol;
import com.viaversion.viaversion.protocols.base.v1_16.ClientboundBaseProtocol1_16;
import com.viaversion.viaversion.protocols.base.v1_7.ClientboundBaseProtocol1_7;
import com.viaversion.viaversion.protocols.base.v1_7.ServerboundBaseProtocol1_7;
import com.viaversion.viaversion.protocols.v1_10to1_11.Protocol1_10To1_11;
import com.viaversion.viaversion.protocols.v1_11_1to1_12.Protocol1_11_1To1_12;
import com.viaversion.viaversion.protocols.v1_11to1_11_1.Protocol1_11To1_11_1;
import com.viaversion.viaversion.protocols.v1_12_1to1_12_2.Protocol1_12_1To1_12_2;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.Protocol1_12_2To1_13;
import com.viaversion.viaversion.protocols.v1_12to1_12_1.Protocol1_12To1_12_1;
import com.viaversion.viaversion.protocols.v1_13_1to1_13_2.Protocol1_13_1To1_13_2;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.Protocol1_13_2To1_14;
import com.viaversion.viaversion.protocols.v1_13to1_13_1.Protocol1_13To1_13_1;
import com.viaversion.viaversion.protocols.v1_14_1to1_14_2.Protocol1_14_1To1_14_2;
import com.viaversion.viaversion.protocols.v1_14_2to1_14_3.Protocol1_14_2To1_14_3;
import com.viaversion.viaversion.protocols.v1_14_3to1_14_4.Protocol1_14_3To1_14_4;
import com.viaversion.viaversion.protocols.v1_14_4to1_15.Protocol1_14_4To1_15;
import com.viaversion.viaversion.protocols.v1_14to1_14_1.Protocol1_14To1_14_1;
import com.viaversion.viaversion.protocols.v1_15_1to1_15_2.Protocol1_15_1To1_15_2;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.Protocol1_15_2To1_16;
import com.viaversion.viaversion.protocols.v1_15to1_15_1.Protocol1_15To1_15_1;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.Protocol1_16_1To1_16_2;
import com.viaversion.viaversion.protocols.v1_16_2to1_16_3.Protocol1_16_2To1_16_3;
import com.viaversion.viaversion.protocols.v1_16_3to1_16_4.Protocol1_16_3To1_16_4;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.Protocol1_16_4To1_17;
import com.viaversion.viaversion.protocols.v1_16to1_16_1.Protocol1_16To1_16_1;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.Protocol1_17_1To1_18;
import com.viaversion.viaversion.protocols.v1_17to1_17_1.Protocol1_17To1_17_1;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.Protocol1_18_2To1_19;
import com.viaversion.viaversion.protocols.v1_18to1_18_2.Protocol1_18To1_18_2;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.Protocol1_19_1To1_19_3;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.Protocol1_19_3To1_19_4;
import com.viaversion.viaversion.protocols.v1_19_4to1_20.Protocol1_19_4To1_20;
import com.viaversion.viaversion.protocols.v1_19to1_19_1.Protocol1_19To1_19_1;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.Protocol1_20_2To1_20_3;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.Protocol1_20_3To1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.Protocol1_20_5To1_21;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.Protocol1_20To1_20_2;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.Protocol1_21_2To1_21_4;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.Protocol1_21_4To1_21_5;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.Protocol1_21To1_21_2;
import com.viaversion.viaversion.protocols.v1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.Protocol1_9_1To1_9_3;
import com.viaversion.viaversion.protocols.v1_9_3to1_10.Protocol1_9_3To1_10;
import com.viaversion.viaversion.protocols.v1_9to1_9_1.Protocol1_9To1_9_1;
import com.viaversion.viaversion.util.Pair;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ProtocolManagerImpl implements ProtocolManager {
    private static final Protocol BASE_PROTOCOL = new InitialBaseProtocol();

    // Input Version -> Output Version & Protocol (Allows fast lookup)
    private final Object2ObjectMap<ProtocolVersion, Object2ObjectMap<ProtocolVersion, Protocol>> registryMap = new Object2ObjectOpenHashMap<>(32);
    private final Map<Class<? extends Protocol>, Protocol<?, ?, ?, ?>> protocols = new HashMap<>(64);
    private final Map<ProtocolPathKey, List<ProtocolPathEntry>> pathCache = new ConcurrentHashMap<>();
    private final Set<ProtocolVersion> supportedVersions = new HashSet<>();
    private final List<Pair<Range<ProtocolVersion>, Protocol>> serverboundBaseProtocols = Lists.newCopyOnWriteArrayList();
    private final List<Pair<Range<ProtocolVersion>, Protocol>> clientboundBaseProtocols = Lists.newCopyOnWriteArrayList();

    private final ReadWriteLock mappingLoaderLock = new ReentrantReadWriteLock();
    private Map<Class<? extends Protocol>, CompletableFuture<Void>> mappingLoaderFutures = new HashMap<>();
    private ThreadPoolExecutor mappingLoaderExecutor;
    private boolean mappingsLoaded;

    private ServerProtocolVersion serverProtocolVersion = new ServerProtocolVersionSingleton(ProtocolVersion.unknown);
    private int maxPathDeltaIncrease; // Only allow lowering path entries by default
    private int maxProtocolPathSize = 50;

    public ProtocolManagerImpl() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("Via-Mappingloader-%d").build();
        mappingLoaderExecutor = new ThreadPoolExecutor(12, Integer.MAX_VALUE, 30L, TimeUnit.SECONDS, new SynchronousQueue<>(), threadFactory);
        mappingLoaderExecutor.allowCoreThreadTimeOut(true);
    }

    public void registerProtocols() {
        // Base Protocol
        BASE_PROTOCOL.initialize();
        BASE_PROTOCOL.register(Via.getManager().getProviders());
        registerBaseProtocol(Direction.CLIENTBOUND, new ClientboundBaseProtocol1_7(), Range.closedOpen(ProtocolVersion.v1_7_2, ProtocolVersion.v1_16));
        registerBaseProtocol(Direction.CLIENTBOUND, new ClientboundBaseProtocol1_16(), Range.atLeast(ProtocolVersion.v1_16));
        registerBaseProtocol(Direction.SERVERBOUND, new ServerboundBaseProtocol1_7(), Range.atLeast(ProtocolVersion.v1_7_2));

        registerProtocol(new Protocol1_8To1_9(), ProtocolVersion.v1_9, ProtocolVersion.v1_8);
        registerProtocol(new Protocol1_9To1_9_1(), Arrays.asList(ProtocolVersion.v1_9_1, ProtocolVersion.v1_9_2), ProtocolVersion.v1_9);
        registerProtocol(new Protocol1_9_1To1_9_3(), ProtocolVersion.v1_9_3, ProtocolVersion.v1_9_2);

        registerProtocol(new Protocol1_9_3To1_10(), ProtocolVersion.v1_10, ProtocolVersion.v1_9_3);

        registerProtocol(new Protocol1_10To1_11(), ProtocolVersion.v1_11, ProtocolVersion.v1_10);
        registerProtocol(new Protocol1_11To1_11_1(), ProtocolVersion.v1_11_1, ProtocolVersion.v1_11);

        registerProtocol(new Protocol1_11_1To1_12(), ProtocolVersion.v1_12, ProtocolVersion.v1_11_1);
        registerProtocol(new Protocol1_12To1_12_1(), ProtocolVersion.v1_12_1, ProtocolVersion.v1_12);
        registerProtocol(new Protocol1_12_1To1_12_2(), ProtocolVersion.v1_12_2, ProtocolVersion.v1_12_1);

        registerProtocol(new Protocol1_12_2To1_13(), ProtocolVersion.v1_13, ProtocolVersion.v1_12_2);
        registerProtocol(new Protocol1_13To1_13_1(), ProtocolVersion.v1_13_1, ProtocolVersion.v1_13);
        registerProtocol(new Protocol1_13_1To1_13_2(), ProtocolVersion.v1_13_2, ProtocolVersion.v1_13_1);

        registerProtocol(new Protocol1_13_2To1_14(), ProtocolVersion.v1_14, ProtocolVersion.v1_13_2);
        registerProtocol(new Protocol1_14To1_14_1(), ProtocolVersion.v1_14_1, ProtocolVersion.v1_14);
        registerProtocol(new Protocol1_14_1To1_14_2(), ProtocolVersion.v1_14_2, ProtocolVersion.v1_14_1);
        registerProtocol(new Protocol1_14_2To1_14_3(), ProtocolVersion.v1_14_3, ProtocolVersion.v1_14_2);
        registerProtocol(new Protocol1_14_3To1_14_4(), ProtocolVersion.v1_14_4, ProtocolVersion.v1_14_3);

        registerProtocol(new Protocol1_14_4To1_15(), ProtocolVersion.v1_15, ProtocolVersion.v1_14_4);
        registerProtocol(new Protocol1_15To1_15_1(), ProtocolVersion.v1_15_1, ProtocolVersion.v1_15);
        registerProtocol(new Protocol1_15_1To1_15_2(), ProtocolVersion.v1_15_2, ProtocolVersion.v1_15_1);

        registerProtocol(new Protocol1_15_2To1_16(), ProtocolVersion.v1_16, ProtocolVersion.v1_15_2);
        registerProtocol(new Protocol1_16To1_16_1(), ProtocolVersion.v1_16_1, ProtocolVersion.v1_16);
        registerProtocol(new Protocol1_16_1To1_16_2(), ProtocolVersion.v1_16_2, ProtocolVersion.v1_16_1);
        registerProtocol(new Protocol1_16_2To1_16_3(), ProtocolVersion.v1_16_3, ProtocolVersion.v1_16_2);
        registerProtocol(new Protocol1_16_3To1_16_4(), ProtocolVersion.v1_16_4, ProtocolVersion.v1_16_3);

        registerProtocol(new Protocol1_16_4To1_17(), ProtocolVersion.v1_17, ProtocolVersion.v1_16_4);
        registerProtocol(new Protocol1_17To1_17_1(), ProtocolVersion.v1_17_1, ProtocolVersion.v1_17);

        registerProtocol(new Protocol1_17_1To1_18(), ProtocolVersion.v1_18, ProtocolVersion.v1_17_1);
        registerProtocol(new Protocol1_18To1_18_2(), ProtocolVersion.v1_18_2, ProtocolVersion.v1_18);

        registerProtocol(new Protocol1_18_2To1_19(), ProtocolVersion.v1_19, ProtocolVersion.v1_18_2);
        registerProtocol(new Protocol1_19To1_19_1(), ProtocolVersion.v1_19_1, ProtocolVersion.v1_19);
        registerProtocol(new Protocol1_19_1To1_19_3(), ProtocolVersion.v1_19_3, ProtocolVersion.v1_19_1);
        registerProtocol(new Protocol1_19_3To1_19_4(), ProtocolVersion.v1_19_4, ProtocolVersion.v1_19_3);

        registerProtocol(new Protocol1_19_4To1_20(), ProtocolVersion.v1_20, ProtocolVersion.v1_19_4);
        registerProtocol(new Protocol1_20To1_20_2(), ProtocolVersion.v1_20_2, ProtocolVersion.v1_20);
        registerProtocol(new Protocol1_20_2To1_20_3(), ProtocolVersion.v1_20_3, ProtocolVersion.v1_20_2);
        registerProtocol(new Protocol1_20_3To1_20_5(), ProtocolVersion.v1_20_5, ProtocolVersion.v1_20_3);

        registerProtocol(new Protocol1_20_5To1_21(), ProtocolVersion.v1_21, ProtocolVersion.v1_20_5);
        registerProtocol(new Protocol1_21To1_21_2(), ProtocolVersion.v1_21_2, ProtocolVersion.v1_21);
        registerProtocol(new Protocol1_21_2To1_21_4(), ProtocolVersion.v1_21_4, ProtocolVersion.v1_21_2);
        registerProtocol(new Protocol1_21_4To1_21_5(), ProtocolVersion.v1_21_5, ProtocolVersion.v1_21_4);
    }

    @Override
    public void registerProtocol(Protocol protocol, ProtocolVersion clientVersion, ProtocolVersion serverVersion) {
        registerProtocol(protocol, Collections.singletonList(clientVersion), serverVersion);
    }

    @Override
    public void registerProtocol(Protocol protocol, List<ProtocolVersion> supportedClientVersion, ProtocolVersion serverVersion) {
        // Register the protocol's handlers
        protocol.initialize();

        // Clear cache as this may make new routes.
        if (!pathCache.isEmpty()) {
            pathCache.clear();
        }

        protocols.put(protocol.getClass(), protocol);

        for (ProtocolVersion clientVersion : supportedClientVersion) {
            // Throw an error if supported client version = server version
            Preconditions.checkArgument(!clientVersion.equals(serverVersion));

            Object2ObjectMap<ProtocolVersion, Protocol> protocolMap = registryMap.computeIfAbsent(clientVersion, s -> new Object2ObjectOpenHashMap<>(2));
            protocolMap.put(serverVersion, protocol);
        }

        protocol.register(Via.getManager().getProviders());
        if (Via.getManager().isInitialized()) {
            refreshVersions();
        }

        if (protocol.hasMappingDataToLoad()) {
            if (mappingLoaderExecutor != null) {
                // Submit mapping data loading
                addMappingLoaderFuture(protocol.getClass(), protocol::loadMappingData);
            } else {
                // Late protocol adding - just do it on the current thread
                protocol.loadMappingData();
            }
        }
    }

    @Override
    public void registerBaseProtocol(Direction direction, Protocol baseProtocol, Range<ProtocolVersion> supportedProtocols) {
        Preconditions.checkArgument(baseProtocol.isBaseProtocol(), "Protocol is not a base protocol");
        final ProtocolVersion lower = supportedProtocols.hasLowerBound() ? supportedProtocols.lowerEndpoint() : null;
        final ProtocolVersion upper = supportedProtocols.hasUpperBound() ? supportedProtocols.upperEndpoint() : null;
        Preconditions.checkArgument(lower == null || lower.getVersionType() != VersionType.SPECIAL, "Base protocol versions cannot contain a special version");
        Preconditions.checkArgument(upper == null || upper.getVersionType() != VersionType.SPECIAL, "Base protocol versions cannot contain a special version");

        baseProtocol.initialize();

        if (direction == Direction.SERVERBOUND) {
            serverboundBaseProtocols.add(new Pair<>(supportedProtocols, baseProtocol));
        } else {
            clientboundBaseProtocols.add(new Pair<>(supportedProtocols, baseProtocol));
        }
        baseProtocol.register(Via.getManager().getProviders());
        if (Via.getManager().isInitialized()) {
            refreshVersions();
        }
    }

    public void refreshVersions() {
        supportedVersions.clear();

        supportedVersions.add(serverProtocolVersion.lowestSupportedProtocolVersion());
        for (ProtocolVersion version : ProtocolVersion.getProtocols()) {
            List<ProtocolPathEntry> protocolPath = getProtocolPath(version, serverProtocolVersion.lowestSupportedProtocolVersion());
            if (protocolPath == null) continue;

            supportedVersions.add(version);
            for (ProtocolPathEntry pathEntry : protocolPath) {
                supportedVersions.add(pathEntry.outputProtocolVersion());
            }
        }
    }

    @Override
    public @Nullable List<ProtocolPathEntry> getProtocolPath(ProtocolVersion clientVersion, ProtocolVersion serverVersion) {
        if (clientVersion == serverVersion) return null; // Nothing to do!

        // Check cache
        ProtocolPathKey protocolKey = new ProtocolPathKeyImpl(clientVersion, serverVersion);
        List<ProtocolPathEntry> protocolList = pathCache.get(protocolKey);
        if (protocolList != null) {
            return protocolList.isEmpty() ? null : protocolList;
        }

        // Calculate path
        Object2ObjectSortedMap<ProtocolVersion, Protocol> outputPath = getProtocolPath(new Object2ObjectLinkedOpenHashMap<>(), clientVersion, serverVersion);
        if (outputPath == null) {
            // Also cache that there is no path
            pathCache.put(protocolKey, List.of());
            return null;
        }

        List<ProtocolPathEntry> path = new ArrayList<>(outputPath.size());
        for (Map.Entry<ProtocolVersion, Protocol> entry : outputPath.entrySet()) {
            path.add(new ProtocolPathEntryImpl(entry.getKey(), entry.getValue()));
        }
        pathCache.put(protocolKey, path);
        return path;
    }

    @Override
    public <C extends ClientboundPacketType,
        S extends ServerboundPacketType
        > VersionedPacketTransformer<C, S> createPacketTransformer(ProtocolVersion inputVersion,
                                                                   @Nullable Class<C> clientboundPacketsClass,
                                                                   @Nullable Class<S> serverboundPacketsClass) {
        Preconditions.checkArgument(clientboundPacketsClass != ClientboundPacketType.class && serverboundPacketsClass != ServerboundPacketType.class);
        return new VersionedPacketTransformerImpl<>(inputVersion, clientboundPacketsClass, serverboundPacketsClass);
    }

    /**
     * Calculates a path to get from an input protocol to the server's protocol.
     *
     * @param current       current items in the path
     * @param clientVersion current input version
     * @param serverVersion desired output version
     * @return path that has been generated, null if failed
     */
    private @Nullable Object2ObjectSortedMap<ProtocolVersion, Protocol> getProtocolPath(Object2ObjectSortedMap<ProtocolVersion, Protocol> current, ProtocolVersion clientVersion, ProtocolVersion serverVersion) {
        if (current.size() > maxProtocolPathSize) return null; // Fail-safe, protocol too complicated.

        // First, check if there is any protocols for this
        Object2ObjectMap<ProtocolVersion, Protocol> toServerProtocolMap = registryMap.get(clientVersion);
        if (toServerProtocolMap == null) {
            return null; // Not supported
        }

        // Next, check if there is a direct, single Protocol path
        Protocol protocol = toServerProtocolMap.get(serverVersion);
        if (protocol != null) {
            current.put(serverVersion, protocol);
            return current; // Easy solution
        }

        // There might be a more advanced solution... So we'll see if any of the others can get us there
        Object2ObjectSortedMap<ProtocolVersion, Protocol> shortest = null;
        for (Map.Entry<ProtocolVersion, Protocol> entry : toServerProtocolMap.entrySet()) {
            // Ensure we don't go back to already contained versions
            ProtocolVersion translatedToVersion = entry.getKey();
            if (current.containsKey(translatedToVersion)) continue;

            // Check if the new version is farther away than the current client version
            if (maxPathDeltaIncrease != -1 && translatedToVersion.getVersionType() == clientVersion.getVersionType()) {
                final int delta = Math.abs(serverVersion.getVersion() - translatedToVersion.getVersion()) - Math.abs(serverVersion.getVersion() - clientVersion.getVersion());
                if (delta > maxPathDeltaIncrease) {
                    continue;
                }
            }

            // Create a copy
            Object2ObjectSortedMap<ProtocolVersion, Protocol> newCurrent = new Object2ObjectLinkedOpenHashMap<>(current);
            newCurrent.put(translatedToVersion, entry.getValue());

            // Calculate the rest of the protocol starting from translatedToVersion and take the shortest
            newCurrent = getProtocolPath(newCurrent, translatedToVersion, serverVersion);
            if (newCurrent != null && (shortest == null || newCurrent.size() < shortest.size())) {
                shortest = newCurrent;
            }
        }

        return shortest; // null if none found
    }

    @Override
    public @Nullable <T extends Protocol> T getProtocol(Class<T> protocolClass) {
        return (T) protocols.get(protocolClass);
    }

    @Override
    public @Nullable Protocol getProtocol(ProtocolVersion clientVersion, ProtocolVersion serverVersion) {
        Object2ObjectMap<ProtocolVersion, Protocol> map = registryMap.get(clientVersion);
        return map != null ? map.get(serverVersion) : null;
    }

    @Override
    public List<Protocol> getBaseProtocols(@Nullable ProtocolVersion clientVersion, @Nullable ProtocolVersion serverVersion) {
        final List<Protocol> list = new ArrayList<>();
        if (clientVersion != null) {
            for (Pair<Range<ProtocolVersion>, Protocol> rangeProtocol : serverboundBaseProtocols) {
                if (rangeProtocol.key().contains(clientVersion)) {
                    list.add(rangeProtocol.value());
                }
            }
        }
        if (serverVersion != null) {
            for (Pair<Range<ProtocolVersion>, Protocol> rangeProtocol : clientboundBaseProtocols) {
                if (rangeProtocol.key().contains(serverVersion)) {
                    list.add(rangeProtocol.value());
                }
            }
        }
        return list;
    }

    @Override
    public Collection<Protocol<?, ?, ?, ?>> getProtocols() {
        return Collections.unmodifiableCollection(protocols.values());
    }

    @Override
    public ServerProtocolVersion getServerProtocolVersion() {
        return serverProtocolVersion;
    }

    public void setServerProtocol(ServerProtocolVersion serverProtocolVersion) {
        this.serverProtocolVersion = serverProtocolVersion;
    }

    @Override
    public boolean isWorkingPipe() {
        for (Object2ObjectMap<ProtocolVersion, Protocol> map : registryMap.values()) {
            for (ProtocolVersion protocolVersion : serverProtocolVersion.supportedProtocolVersions()) {
                if (map.containsKey(protocolVersion)) {
                    return true;
                }
            }
        }
        return false; // No destination for protocol
    }

    @Override
    public SortedSet<ProtocolVersion> getSupportedVersions() {
        return Collections.unmodifiableSortedSet(new TreeSet<>(supportedVersions));
    }

    @Override
    public void setMaxPathDeltaIncrease(final int maxPathDeltaIncrease) {
        this.maxPathDeltaIncrease = Math.max(-1, maxPathDeltaIncrease);
    }

    @Override
    public int getMaxPathDeltaIncrease() {
        return maxPathDeltaIncrease;
    }

    @Override
    public int getMaxProtocolPathSize() {
        return maxProtocolPathSize;
    }

    @Override
    public void setMaxProtocolPathSize(int maxProtocolPathSize) {
        this.maxProtocolPathSize = maxProtocolPathSize;
    }

    @Override
    public Protocol getBaseProtocol() {
        return BASE_PROTOCOL;
    }

    @Override
    public void completeMappingDataLoading(Class<? extends Protocol> protocolClass) {
        if (mappingsLoaded) return;

        CompletableFuture<Void> future = getMappingLoaderFuture(protocolClass);
        if (future != null) {
            // Wait for completion
            future.join();
        }
    }

    @Override
    public boolean checkForMappingCompletion() {
        mappingLoaderLock.readLock().lock();
        try {
            if (mappingsLoaded) {
                return false;
            }

            for (CompletableFuture<Void> future : mappingLoaderFutures.values()) {
                // Return if any future hasn't completed yet
                if (!future.isDone()) {
                    return false;
                }
            }

            shutdownLoaderExecutor();
            return true;
        } finally {
            mappingLoaderLock.readLock().unlock();
        }
    }

    @Override
    public void addMappingLoaderFuture(Class<? extends Protocol> protocolClass, Runnable runnable) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(runnable, mappingLoaderExecutor).exceptionally(mappingLoaderThrowable(protocolClass));

        mappingLoaderLock.writeLock().lock();
        try {
            mappingLoaderFutures.put(protocolClass, future);
        } finally {
            mappingLoaderLock.writeLock().unlock();
        }
    }

    @Override
    public void addMappingLoaderFuture(Class<? extends Protocol> protocolClass, Class<? extends Protocol> dependsOn, Runnable runnable) {
        CompletableFuture<Void> future = getMappingLoaderFuture(dependsOn)
            .whenCompleteAsync((v, throwable) -> runnable.run(), mappingLoaderExecutor).exceptionally(mappingLoaderThrowable(protocolClass));

        mappingLoaderLock.writeLock().lock();
        try {
            mappingLoaderFutures.put(protocolClass, future);
        } finally {
            mappingLoaderLock.writeLock().unlock();
        }
    }

    @Override
    public @Nullable CompletableFuture<Void> getMappingLoaderFuture(Class<? extends Protocol> protocolClass) {
        mappingLoaderLock.readLock().lock();
        try {
            return mappingsLoaded ? null : mappingLoaderFutures.get(protocolClass);
        } finally {
            mappingLoaderLock.readLock().unlock();
        }
    }

    @Override
    public PacketWrapper createPacketWrapper(@Nullable PacketType packetType, @Nullable ByteBuf buf, UserConnection connection) {
        return new PacketWrapperImpl(packetType, buf, connection);
    }

    @Override
    @Deprecated
    public PacketWrapper createPacketWrapper(int packetId, @Nullable ByteBuf buf, UserConnection connection) {
        return new PacketWrapperImpl(packetId, buf, connection);
    }

    @Override
    public boolean hasLoadedMappings() {
        return mappingsLoaded;
    }

    public void shutdownLoaderExecutor() {
        Preconditions.checkArgument(!mappingsLoaded);

        // If this log message is missing, something is wrong
        Via.getPlatform().getLogger().info("Finished mapping loading, shutting down loader executor.");
        mappingsLoaded = true;
        mappingLoaderExecutor.shutdown();
        mappingLoaderExecutor = null;
        mappingLoaderFutures.clear();
        mappingLoaderFutures = null;

        // Clear cached mapping files
        MappingDataLoader.INSTANCE.clearCache();
    }

    private Function<Throwable, Void> mappingLoaderThrowable(Class<? extends Protocol> protocolClass) {
        return throwable -> {
            Via.getPlatform().getLogger().log(Level.SEVERE, "Error during mapping loading of " + protocolClass.getSimpleName(), throwable);
            return null;
        };
    }
}
