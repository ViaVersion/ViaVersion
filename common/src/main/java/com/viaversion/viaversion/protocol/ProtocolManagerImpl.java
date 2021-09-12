/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.VersionedPacketTransformer;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.ServerProtocolVersion;
import com.viaversion.viaversion.protocol.packet.PacketWrapperImpl;
import com.viaversion.viaversion.protocol.packet.VersionedPacketTransformerImpl;
import com.viaversion.viaversion.protocols.base.BaseProtocol;
import com.viaversion.viaversion.protocols.base.BaseProtocol1_16;
import com.viaversion.viaversion.protocols.base.BaseProtocol1_7;
import com.viaversion.viaversion.protocols.protocol1_10to1_9_3.Protocol1_10To1_9_3_4;
import com.viaversion.viaversion.protocols.protocol1_11_1to1_11.Protocol1_11_1To1_11;
import com.viaversion.viaversion.protocols.protocol1_11to1_10.Protocol1_11To1_10;
import com.viaversion.viaversion.protocols.protocol1_12_1to1_12.Protocol1_12_1To1_12;
import com.viaversion.viaversion.protocols.protocol1_12_2to1_12_1.Protocol1_12_2To1_12_1;
import com.viaversion.viaversion.protocols.protocol1_12to1_11_1.Protocol1_12To1_11_1;
import com.viaversion.viaversion.protocols.protocol1_13_1to1_13.Protocol1_13_1To1_13;
import com.viaversion.viaversion.protocols.protocol1_13_2to1_13_1.Protocol1_13_2To1_13_1;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;
import com.viaversion.viaversion.protocols.protocol1_14_1to1_14.Protocol1_14_1To1_14;
import com.viaversion.viaversion.protocols.protocol1_14_2to1_14_1.Protocol1_14_2To1_14_1;
import com.viaversion.viaversion.protocols.protocol1_14_3to1_14_2.Protocol1_14_3To1_14_2;
import com.viaversion.viaversion.protocols.protocol1_14_4to1_14_3.Protocol1_14_4To1_14_3;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.Protocol1_14To1_13_2;
import com.viaversion.viaversion.protocols.protocol1_15_1to1_15.Protocol1_15_1To1_15;
import com.viaversion.viaversion.protocols.protocol1_15_2to1_15_1.Protocol1_15_2To1_15_1;
import com.viaversion.viaversion.protocols.protocol1_15to1_14_4.Protocol1_15To1_14_4;
import com.viaversion.viaversion.protocols.protocol1_16_1to1_16.Protocol1_16_1To1_16;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.Protocol1_16_2To1_16_1;
import com.viaversion.viaversion.protocols.protocol1_16_3to1_16_2.Protocol1_16_3To1_16_2;
import com.viaversion.viaversion.protocols.protocol1_16_4to1_16_3.Protocol1_16_4To1_16_3;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.Protocol1_16To1_15_2;
import com.viaversion.viaversion.protocols.protocol1_17_1to1_17.Protocol1_17_1To1_17;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.Protocol1_17To1_16_4;
import com.viaversion.viaversion.protocols.protocol1_9_1_2to1_9_3_4.Protocol1_9_1_2To1_9_3_4;
import com.viaversion.viaversion.protocols.protocol1_9_1to1_9.Protocol1_9_1To1_9;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.Protocol1_9_3To1_9_1_2;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_9_1.Protocol1_9To1_9_1;
import com.viaversion.viaversion.util.Pair;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import org.checkerframework.checker.nullness.qual.Nullable;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;

public class ProtocolManagerImpl implements ProtocolManager {
    private static final Protocol BASE_PROTOCOL = new BaseProtocol();

    // Input Version -> Output Version & Protocol (Allows fast lookup)
    private final Int2ObjectMap<Int2ObjectMap<ProtocolSupplier>> registryMap = new Int2ObjectOpenHashMap<>(32);
    private final Map<Class<? extends Protocol>, ProtocolSupplier> protocols = new HashMap<>();
    private final Map<ProtocolPathKey, List<ProtocolPathEntry>> pathCache = new ConcurrentHashMap<>();
    private final Set<Integer> supportedVersions = new HashSet<>();
    private final List<Pair<Range<Integer>, Protocol>> baseProtocols = Lists.newCopyOnWriteArrayList();
    private final List<Protocol> registerList = new ArrayList<>();

    private final ReadWriteLock mappingLoaderLock = new ReentrantReadWriteLock();
    private final Map<Class<? extends Protocol>, CompletableFuture<Void>> mappingLoaderFutures = new HashMap<>();
    private final ThreadPoolExecutor mappingLoaderExecutor;

    private ServerProtocolVersion serverProtocolVersion = new ServerProtocolVersionSingleton(-1);
    private boolean onlyCheckLoweringPathEntries = true;
    private int maxProtocolPathSize = 50;

    public ProtocolManagerImpl() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("Via-Mappingloader-%d").build();
        mappingLoaderExecutor = new ThreadPoolExecutor(4, 16, 5L, TimeUnit.SECONDS, new SynchronousQueue<>(), threadFactory);
        mappingLoaderExecutor.allowCoreThreadTimeOut(true);
    }

    public void registerProtocols() {
        // Base Protocol
        registerBaseProtocol(BASE_PROTOCOL, Range.lessThan(Integer.MIN_VALUE));
        registerBaseProtocol(new BaseProtocol1_7(), Range.lessThan(ProtocolVersion.v1_16.getVersion()));
        registerBaseProtocol(new BaseProtocol1_16(), Range.atLeast(ProtocolVersion.v1_16.getVersion()));

        registerProtocolSupplier(Protocol1_9To1_8.class, Protocol1_9To1_8::new, ProtocolVersion.v1_9, ProtocolVersion.v1_8);
        registerProtocolSupplier(Protocol1_9_1To1_9.class, Protocol1_9_1To1_9::new, Arrays.asList(ProtocolVersion.v1_9_1.getVersion(), ProtocolVersion.v1_9_2.getVersion()), ProtocolVersion.v1_9.getVersion());
        registerProtocolSupplier(Protocol1_9_3To1_9_1_2.class, Protocol1_9_3To1_9_1_2::new, ProtocolVersion.v1_9_3, ProtocolVersion.v1_9_2);

        registerProtocolSupplier(Protocol1_9To1_9_1.class, Protocol1_9To1_9_1::new, ProtocolVersion.v1_9, ProtocolVersion.v1_9_1);
        registerProtocolSupplier(Protocol1_9_1_2To1_9_3_4.class, Protocol1_9_1_2To1_9_3_4::new, Arrays.asList(ProtocolVersion.v1_9_1.getVersion(), ProtocolVersion.v1_9_2.getVersion()), ProtocolVersion.v1_9_3.getVersion());
        registerProtocolSupplier(Protocol1_10To1_9_3_4.class, Protocol1_10To1_9_3_4::new, ProtocolVersion.v1_10, ProtocolVersion.v1_9_3);

        registerProtocolSupplier(Protocol1_11To1_10.class, Protocol1_11To1_10::new, ProtocolVersion.v1_11, ProtocolVersion.v1_10);
        registerProtocolSupplier(Protocol1_11_1To1_11.class, Protocol1_11_1To1_11::new, ProtocolVersion.v1_11_1, ProtocolVersion.v1_11);

        registerProtocolSupplier(Protocol1_12To1_11_1.class, Protocol1_12To1_11_1::new, ProtocolVersion.v1_12, ProtocolVersion.v1_11_1);
        registerProtocolSupplier(Protocol1_12_1To1_12.class, Protocol1_12_1To1_12::new, ProtocolVersion.v1_12_1, ProtocolVersion.v1_12);
        registerProtocolSupplier(Protocol1_12_2To1_12_1.class, Protocol1_12_2To1_12_1::new, ProtocolVersion.v1_12_2, ProtocolVersion.v1_12_1);

        registerProtocolSupplier(Protocol1_13To1_12_2.class, Protocol1_13To1_12_2::new, ProtocolVersion.v1_13, ProtocolVersion.v1_12_2);
        registerProtocolSupplier(Protocol1_13_1To1_13.class, Protocol1_13_1To1_13::new, ProtocolVersion.v1_13_1, ProtocolVersion.v1_13);
        registerProtocolSupplier(Protocol1_13_2To1_13_1.class, Protocol1_13_2To1_13_1::new, ProtocolVersion.v1_13_2, ProtocolVersion.v1_13_1);

        registerProtocolSupplier(Protocol1_14To1_13_2.class, Protocol1_14To1_13_2::new, ProtocolVersion.v1_14, ProtocolVersion.v1_13_2);
        registerProtocolSupplier(Protocol1_14_1To1_14.class, Protocol1_14_1To1_14::new, ProtocolVersion.v1_14_1, ProtocolVersion.v1_14);
        registerProtocolSupplier(Protocol1_14_2To1_14_1.class, Protocol1_14_2To1_14_1::new, ProtocolVersion.v1_14_2, ProtocolVersion.v1_14_1);
        registerProtocolSupplier(Protocol1_14_3To1_14_2.class, Protocol1_14_3To1_14_2::new, ProtocolVersion.v1_14_3, ProtocolVersion.v1_14_2);
        registerProtocolSupplier(Protocol1_14_4To1_14_3.class, Protocol1_14_4To1_14_3::new, ProtocolVersion.v1_14_4, ProtocolVersion.v1_14_3);

        registerProtocolSupplier(Protocol1_15To1_14_4.class, Protocol1_15To1_14_4::new, ProtocolVersion.v1_15, ProtocolVersion.v1_14_4);
        registerProtocolSupplier(Protocol1_15_1To1_15.class, Protocol1_15_1To1_15::new, ProtocolVersion.v1_15_1, ProtocolVersion.v1_15);
        registerProtocolSupplier(Protocol1_15_2To1_15_1.class, Protocol1_15_2To1_15_1::new, ProtocolVersion.v1_15_2, ProtocolVersion.v1_15_1);

        registerProtocolSupplier(Protocol1_16To1_15_2.class, Protocol1_16To1_15_2::new, ProtocolVersion.v1_16, ProtocolVersion.v1_15_2);
        registerProtocolSupplier(Protocol1_16_1To1_16.class, Protocol1_16_1To1_16::new, ProtocolVersion.v1_16_1, ProtocolVersion.v1_16);
        registerProtocolSupplier(Protocol1_16_2To1_16_1.class, Protocol1_16_2To1_16_1::new, ProtocolVersion.v1_16_2, ProtocolVersion.v1_16_1);
        registerProtocolSupplier(Protocol1_16_3To1_16_2.class, Protocol1_16_3To1_16_2::new, ProtocolVersion.v1_16_3, ProtocolVersion.v1_16_2);
        registerProtocolSupplier(Protocol1_16_4To1_16_3.class, Protocol1_16_4To1_16_3::new, ProtocolVersion.v1_16_4, ProtocolVersion.v1_16_3);

        registerProtocolSupplier(Protocol1_17To1_16_4.class, Protocol1_17To1_16_4::new, ProtocolVersion.v1_17, ProtocolVersion.v1_16_4);
        registerProtocolSupplier(Protocol1_17_1To1_17.class, Protocol1_17_1To1_17::new, ProtocolVersion.v1_17_1, ProtocolVersion.v1_17);
    }

    @Override
    public void registerProtocol(Protocol protocol, ProtocolVersion clientVersion, ProtocolVersion serverVersion) {
        registerProtocol(protocol, Collections.singletonList(clientVersion.getVersion()), serverVersion.getVersion());
    }

    @Override
    public void registerProtocol(Protocol protocol, List<Integer> supportedClientVersion, int serverVersion) {
        registerProtocolSupplier(protocol.getClass(), new DirectProtocolSupplier(protocol), supportedClientVersion, serverVersion);
    }

    @Override
    public void registerProtocolSupplier(Class<? extends Protocol> protocolClass, Supplier<Protocol> provider, ProtocolVersion clientVersion, ProtocolVersion serverVersion) {
        registerProtocolSupplier(protocolClass, provider, Collections.singletonList(clientVersion.getVersion()), serverVersion.getVersion());
    }

    @Override
    public void registerProtocolSupplier(Class<? extends Protocol> protocolClass, Supplier<Protocol> provider, List<Integer> supportedClientVersion, int serverVersion) {
        registerProtocolSupplier(protocolClass, new CachedProtocolSupplier(provider), supportedClientVersion, serverVersion);
    }

    private void registerProtocolSupplier(Class<? extends Protocol> protocolClass, ProtocolSupplier provider, List<Integer> supportedClientVersion, int serverVersion) {
        // Clear cache as this may make new routes.
        if (!pathCache.isEmpty()) {
            pathCache.clear();
        }

        protocols.put(protocolClass, provider);

        for (int clientVersion : supportedClientVersion) {
            // Throw an error if supported client version = server version
            Preconditions.checkArgument(clientVersion != serverVersion);

            Int2ObjectMap<ProtocolSupplier> protocolMap = registryMap.computeIfAbsent(clientVersion, s -> new Int2ObjectOpenHashMap<>(2));
            protocolMap.put(serverVersion, provider);
        }
    }

    @Override
    public void registerBaseProtocol(Protocol baseProtocol, Range<Integer> supportedProtocols) {
        Preconditions.checkArgument(baseProtocol.isBaseProtocol(), "Protocol is not a base protocol");
        baseProtocol.initialize();

        baseProtocols.add(new Pair<>(supportedProtocols, baseProtocol));
        registerProtocol(baseProtocol);
    }

    private void registerProtocol(Protocol protocol) {
        if (Via.getPlatform().isPluginEnabled()) {
            protocol.register(Via.getManager().getProviders());
            refreshVersions();
        } else {
            registerList.add(protocol);
        }
    }

    public void refreshVersions() {
        supportedVersions.clear();
        supportedVersions.add(serverProtocolVersion.lowestSupportedVersion());
        supportedVersions.addAll(registryMap.keySet()); // Assuming all registered versions lead to a supported server version
    }

    @Override
    public @Nullable List<ProtocolPathEntry> getProtocolPath(int clientVersion, int serverVersion) {
        if (clientVersion == serverVersion) return null; // Nothing to do!

        // Check cache
        ProtocolPathKey protocolKey = new ProtocolPathKeyImpl(clientVersion, serverVersion);
        List<ProtocolPathEntry> protocolList = pathCache.get(protocolKey);
        if (protocolList != null) {
            return protocolList;
        }

        // Calculate path
        Int2ObjectSortedMap<ProtocolSupplier> outputPath = getProtocolPath(new Int2ObjectLinkedOpenHashMap<>(), clientVersion, serverVersion);
        if (outputPath == null) {
            return null;
        }

        List<ProtocolPathEntry> path = new ArrayList<>(outputPath.size());
        for (Int2ObjectMap.Entry<ProtocolSupplier> entry : outputPath.int2ObjectEntrySet()) {
            path.add(new ProtocolPathEntryImpl(entry.getIntKey(), entry.getValue().get()));
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
    private @Nullable Int2ObjectSortedMap<ProtocolSupplier> getProtocolPath(Int2ObjectSortedMap<ProtocolSupplier> current, int clientVersion, int serverVersion) {
        if (current.size() > maxProtocolPathSize) return null; // Fail-safe, protocol too complicated.

        // First, check if there is any protocols for this
        Int2ObjectMap<ProtocolSupplier> toServerProtocolMap = registryMap.get(clientVersion);
        if (toServerProtocolMap == null) {
            return null; // Not supported
        }

        // Next, check if there is a direct, single Protocol path
        ProtocolSupplier protocol = toServerProtocolMap.get(serverVersion);
        if (protocol != null) {
            current.put(serverVersion, protocol);
            return current; // Easy solution
        }

        // There might be a more advanced solution... So we'll see if any of the others can get us there
        Int2ObjectSortedMap<ProtocolSupplier> shortest = null;
        for (Int2ObjectMap.Entry<ProtocolSupplier> entry : toServerProtocolMap.int2ObjectEntrySet()) {
            // Ensure we don't go back to already contained versions
            int translatedToVersion = entry.getIntKey();
            if (current.containsKey(translatedToVersion)) continue;

            // Check if the new version is farther away than the current client version
            if (onlyCheckLoweringPathEntries && Math.abs(serverVersion - translatedToVersion) > Math.abs(serverVersion - clientVersion)) {
                continue;
            }

            // Create a copy
            Int2ObjectSortedMap<ProtocolSupplier> newCurrent = new Int2ObjectLinkedOpenHashMap<>(current);
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
        return (T) protocols.get(protocolClass).get();
    }

    @Override
    public @Nullable Protocol getProtocol(int clientVersion, int serverVersion) {
        Int2ObjectMap<ProtocolSupplier> map = registryMap.get(clientVersion);
        return map != null ? map.get(serverVersion).get() : null;
    }

    @Override
    public Protocol getBaseProtocol(int serverVersion) {
        for (Pair<Range<Integer>, Protocol> rangeProtocol : Lists.reverse(baseProtocols)) {
            if (rangeProtocol.getKey().contains(serverVersion)) {
                return rangeProtocol.getValue();
            }
        }
        throw new IllegalStateException("No Base Protocol for " + serverVersion);
    }

    @Override
    public ServerProtocolVersion getServerProtocolVersion() {
        return serverProtocolVersion;
    }

    public void setServerProtocol(ServerProtocolVersion serverProtocolVersion) {
        this.serverProtocolVersion = serverProtocolVersion;
        //noinspection deprecation
        ProtocolRegistry.SERVER_PROTOCOL = serverProtocolVersion.lowestSupportedVersion();
    }

    @Override
    public boolean isWorkingPipe() {
        for (Int2ObjectMap<ProtocolSupplier> map : registryMap.values()) {
            for (int protocolVersion : serverProtocolVersion.supportedVersions()) {
                if (map.containsKey(protocolVersion)) {
                    return true;
                }
            }
        }
        return false; // No destination for protocol
    }

    @Override
    public SortedSet<Integer> getSupportedVersions() {
        return Collections.unmodifiableSortedSet(new TreeSet<>(supportedVersions));
    }

    @Override
    public void setOnlyCheckLoweringPathEntries(boolean onlyCheckLoweringPathEntries) {
        this.onlyCheckLoweringPathEntries = onlyCheckLoweringPathEntries;
    }

    @Override
    public boolean onlyCheckLoweringPathEntries() {
        return onlyCheckLoweringPathEntries;
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
    public void completeMappingDataLoading(Class<? extends Protocol> protocolClass) throws Exception {
        CompletableFuture<Void> future = getMappingLoaderFuture(protocolClass);
        if (future != null) {
            // Wait for completion
            future.get();
        }
    }

    @Override
    public boolean checkForMappingCompletion() {
        mappingLoaderLock.readLock().lock();
        try {
            for (CompletableFuture<Void> future : mappingLoaderFutures.values()) {
                // Return if any future hasn't completed yet
                if (!future.isDone()) {
                    return false;
                }
            }

            // Clear cached json files
            if (MappingDataLoader.isCacheJsonMappings()) {
                MappingDataLoader.getMappingsCache().clear();
            }
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
        CompletableFuture<Void> future = internalGetMappingLoaderFuture(protocolClass);
        if (future != null) {
            return future;
        }

        //Force loading
        protocols.get(protocolClass).get();
        return internalGetMappingLoaderFuture(protocolClass);
    }

    private CompletableFuture<Void> internalGetMappingLoaderFuture(Class<? extends Protocol> protocolClass) {
        mappingLoaderLock.readLock().lock();
        try {
            return mappingLoaderFutures.get(protocolClass);
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

    /**
     * Called when the server is enabled, to register any non-registered listeners.
     */
    public void onServerLoaded() {
        for (Protocol protocol : registerList) {
            protocol.register(Via.getManager().getProviders());
        }
        registerList.clear();
    }

    private Function<Throwable, Void> mappingLoaderThrowable(Class<? extends Protocol> protocolClass) {
        return throwable -> {
            Via.getPlatform().getLogger().severe("Error during mapping loading of " + protocolClass.getSimpleName());
            throwable.printStackTrace();
            return null;
        };
    }


    private abstract class ProtocolSupplier implements Supplier<Protocol> {

        protected void init(Protocol protocol) {
            // Register the protocol's handlers
            protocol.initialize();
            registerProtocol(protocol);

            if (protocol.hasMappingDataToLoad()) {
                // Submit mapping data loading
                addMappingLoaderFuture(protocol.getClass(), protocol::loadMappingData);
                try {
                    completeMappingDataLoading(protocol.getClass());
                } catch (Exception e) {
                    Via.getPlatform().getLogger().severe("Could not wait for mapping loading of " + protocol.getClass().getSimpleName());
                    e.printStackTrace();
                }
            }
        }
    }


    private final class CachedProtocolSupplier extends ProtocolSupplier {
        private final Supplier<Protocol> provider;
        private Protocol protocol;

        public CachedProtocolSupplier(Supplier<Protocol> provider) {
            this.provider = provider;
        }

        @Override
        public Protocol get() {
            synchronized (provider) {
                if (protocol != null) {
                    return protocol;
                }

                protocol = provider.get();
                init(protocol);
            }
            return protocol;
        }
    }


    private final class DirectProtocolSupplier extends ProtocolSupplier {
        private final Protocol protocol;

        private DirectProtocolSupplier(Protocol protocol) {
            this.protocol = protocol;
            init(protocol);
        }


        @Override
        public Protocol get() {
            return protocol;
        }
    }
}
