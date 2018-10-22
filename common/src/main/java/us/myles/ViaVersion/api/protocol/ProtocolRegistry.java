package us.myles.ViaVersion.api.protocol;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.protocols.base.BaseProtocol;
import us.myles.ViaVersion.protocols.base.BaseProtocol1_7;
import us.myles.ViaVersion.protocols.protocol1_10to1_9_3.Protocol1_10To1_9_3_4;
import us.myles.ViaVersion.protocols.protocol1_11_1to1_11.Protocol1_11_1To1_11;
import us.myles.ViaVersion.protocols.protocol1_11to1_10.Protocol1_11To1_10;
import us.myles.ViaVersion.protocols.protocol1_12_1to1_12.Protocol1_12_1TO1_12;
import us.myles.ViaVersion.protocols.protocol1_12_2to1_12_1.Protocol1_12_2TO1_12_1;
import us.myles.ViaVersion.protocols.protocol1_12to1_11_1.Protocol1_12To1_11_1;
import us.myles.ViaVersion.protocols.protocol1_13_2to1_13_1.Protocol1_13_2To1_13_1;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;
import us.myles.ViaVersion.protocols.protocol1_9_1_2to1_9_3_4.Protocol1_9_1_2TO1_9_3_4;
import us.myles.ViaVersion.protocols.protocol1_9_1to1_9.Protocol1_9_1TO1_9;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.Protocol1_9_3TO1_9_1_2;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9TO1_8;
import us.myles.ViaVersion.protocols.protocol1_9to1_9_1.Protocol1_9TO1_9_1;
import us.myles.ViaVersion.protocols.protocol1_13_1to1_13.Protocol1_13_1To1_13;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ProtocolRegistry {
    public static final Protocol BASE_PROTOCOL = new BaseProtocol();
    public static int SERVER_PROTOCOL = -1;
    // Input Version -> Output Version & Protocol (Allows fast lookup)
    private static final Map<Integer, Map<Integer, Protocol>> registryMap = new ConcurrentHashMap<>();
    private static final Map<Pair<Integer, Integer>, List<Pair<Integer, Protocol>>> pathCache = new ConcurrentHashMap<>();
    private static final List<Protocol> registerList = Lists.newCopyOnWriteArrayList();
    private static final Set<Integer> supportedVersions = Sets.newConcurrentHashSet();
    private static final List<Pair<Range<Integer>, Protocol>> baseProtocols = Lists.newCopyOnWriteArrayList();

    static {
        // Base Protocol
        registerBaseProtocol(BASE_PROTOCOL, Range.lessThan(Integer.MIN_VALUE));
        registerBaseProtocol(new BaseProtocol1_7(), Range.<Integer>all());

        // Register built in protocols
        registerProtocol(new Protocol1_9TO1_8(), Collections.singletonList(ProtocolVersion.v1_9.getId()), ProtocolVersion.v1_8.getId());
        registerProtocol(new Protocol1_9_1TO1_9(), Arrays.asList(ProtocolVersion.v1_9_1.getId(), ProtocolVersion.v1_9_2.getId()), ProtocolVersion.v1_9.getId());
        registerProtocol(new Protocol1_9_3TO1_9_1_2(), Collections.singletonList(ProtocolVersion.v1_9_3.getId()), ProtocolVersion.v1_9_2.getId());
        // Only supported for 1.9.4 server to 1.9 (nothing else)
        registerProtocol(new Protocol1_9TO1_9_1(), Collections.singletonList(ProtocolVersion.v1_9.getId()), ProtocolVersion.v1_9_2.getId());
        registerProtocol(new Protocol1_9_1_2TO1_9_3_4(), Arrays.asList(ProtocolVersion.v1_9_1.getId(), ProtocolVersion.v1_9_2.getId()), ProtocolVersion.v1_9_3.getId());
        registerProtocol(new Protocol1_10To1_9_3_4(), Collections.singletonList(ProtocolVersion.v1_10.getId()), ProtocolVersion.v1_9_3.getId());

        registerProtocol(new Protocol1_11To1_10(), Collections.singletonList(ProtocolVersion.v1_11.getId()), ProtocolVersion.v1_10.getId());
        registerProtocol(new Protocol1_11_1To1_11(), Collections.singletonList(ProtocolVersion.v1_11_1.getId()), ProtocolVersion.v1_11.getId());

        registerProtocol(new Protocol1_12To1_11_1(), Collections.singletonList(ProtocolVersion.v1_12.getId()), ProtocolVersion.v1_11_1.getId());
        registerProtocol(new Protocol1_12_1TO1_12(), Collections.singletonList(ProtocolVersion.v1_12_1.getId()), ProtocolVersion.v1_12.getId());
        registerProtocol(new Protocol1_12_2TO1_12_1(), Collections.singletonList(ProtocolVersion.v1_12_2.getId()), ProtocolVersion.v1_12_1.getId());

        registerProtocol(new Protocol1_13To1_12_2(), Collections.singletonList(ProtocolVersion.v1_13.getId()), ProtocolVersion.v1_12_2.getId());
        registerProtocol(new Protocol1_13_1To1_13(),  Arrays.asList(ProtocolVersion.v1_13_1.getId()), ProtocolVersion.v1_13.getId());
        registerProtocol(new Protocol1_13_2To1_13_1(),  Arrays.asList(ProtocolVersion.v1_13_2.getId()), ProtocolVersion.v1_13_1.getId());
    }

    /**
     * Register a protocol
     *
     * @param protocol  The protocol to register.
     * @param supported Supported client versions.
     * @param output    The output server version it converts to.
     */
    public static void registerProtocol(Protocol protocol, List<Integer> supported, Integer output) {
        // Clear cache as this may make new routes.
        if (pathCache.size() > 0)
            pathCache.clear();

        for (Integer version : supported) {
            if (!registryMap.containsKey(version)) {
                registryMap.put(version, new HashMap<Integer, Protocol>());
            }

            registryMap.get(version).put(output, protocol);
        }

        if (Via.getPlatform().isPluginEnabled()) {
            protocol.registerListeners();
            protocol.register(Via.getManager().getProviders());
            refreshVersions();
        } else {
            registerList.add(protocol);
        }
    }

    /**
     * Registers a base protocol.
     * Base Protocols registered later have higher priority
     * Only one base protocol will be added to pipeline
     *
     * @param baseProtocol Base Protocol to register
     * @param supportedProtocols Versions that baseProtocol supports
     */
    public static void registerBaseProtocol(Protocol baseProtocol, Range<Integer> supportedProtocols) {
        baseProtocols.add(new Pair<>(supportedProtocols, baseProtocol));
        if (Via.getPlatform().isPluginEnabled()) {
            baseProtocol.registerListeners();
            baseProtocol.register(Via.getManager().getProviders());
            refreshVersions();
        } else {
            registerList.add(baseProtocol);
        }
    }

    public static void refreshVersions() {
        supportedVersions.clear();

        supportedVersions.add(ProtocolRegistry.SERVER_PROTOCOL);
        for (ProtocolVersion versions : ProtocolVersion.getProtocols()) {
            List<Pair<Integer, Protocol>> paths = getProtocolPath(versions.getId(), ProtocolRegistry.SERVER_PROTOCOL);
            if (paths == null) continue;
            supportedVersions.add(versions.getId());
            for (Pair<Integer, Protocol> path : paths)
                supportedVersions.add(path.getKey());
        }
    }

    /**
     * Get the versions compatible with the server.
     *
     * @return Read-only set of the versions.
     */
    public static SortedSet<Integer> getSupportedVersions() {
        return Collections.unmodifiableSortedSet(new TreeSet<>(supportedVersions));
    }

    /**
     * Check if this plugin is useful to the server.
     *
     * @return True if there is a useful pipe
     */
    public static boolean isWorkingPipe() {
        for (Map<Integer, Protocol> maps : registryMap.values()) {
            if (maps.containsKey(SERVER_PROTOCOL)) return true;
        }
        return false; // No destination for protocol
    }

    /**
     * Called when the server is enabled, to register any non registered listeners.
     */
    public static void onServerLoaded() {
        for (Protocol protocol : registerList) {
            protocol.registerListeners();
            protocol.register(Via.getManager().getProviders());
        }
        registerList.clear();
    }

    /**
     * Calculate a path to get from an input protocol to the servers protocol.
     *
     * @param current       The current items in the path
     * @param clientVersion The current input version
     * @param serverVersion The desired output version
     * @return The path which has been generated, null if failed.
     */
    private static List<Pair<Integer, Protocol>> getProtocolPath(List<Pair<Integer, Protocol>> current, int clientVersion, int serverVersion) {
        if (clientVersion == serverVersion) return null; // We're already there
        if (current.size() > 50) return null; // Fail safe, protocol too complicated.

        // First check if there is any protocols for this
        Map<Integer, Protocol> inputMap = registryMap.get(clientVersion);
        if (inputMap == null) {
            return null; // Not supported
        }
        // Next check there isn't an obvious path
        Protocol protocol = inputMap.get(serverVersion);
        if (protocol != null) {
            current.add(new Pair<>(serverVersion, protocol));
            return current; // Easy solution
        }
        // There might be a more advanced solution... So we'll see if any of the others can get us there
        List<Pair<Integer, Protocol>> shortest = null;

        for (Map.Entry<Integer, Protocol> entry : inputMap.entrySet()) {
            // Ensure it wasn't caught by the other loop
            if (!entry.getKey().equals(serverVersion)) {
                Pair<Integer, Protocol> pair = new Pair<>(entry.getKey(), entry.getValue());
                // Ensure no recursion
                if (!current.contains(pair)) {
                    // Create a copy
                    List<Pair<Integer, Protocol>> newCurrent = new ArrayList<>(current);
                    newCurrent.add(pair);
                    // Calculate the rest of the protocol using the current
                    newCurrent = getProtocolPath(newCurrent, entry.getKey(), serverVersion);
                    if (newCurrent != null) {
                        // If it's shorter then choose it
                        if (shortest == null || shortest.size() > newCurrent.size()) {
                            shortest = newCurrent;
                        }
                    }
                }
            }
        }

        return shortest; // null if none found
    }

    /**
     * Calculate a path from a client version to server version
     *
     * @param clientVersion The input client version
     * @param serverVersion The desired output server version
     * @return The path it generated, null if it failed.
     */
    public static List<Pair<Integer, Protocol>> getProtocolPath(int clientVersion, int serverVersion) {
        Pair<Integer, Integer> protocolKey = new Pair<>(clientVersion, serverVersion);
        // Check cache
        List<Pair<Integer, Protocol>> protocolList = pathCache.get(protocolKey);
        if (protocolList != null) {
            return protocolList;
        }
        // Generate path
        List<Pair<Integer, Protocol>> outputPath = getProtocolPath(new ArrayList<Pair<Integer, Protocol>>(), clientVersion, serverVersion);
        // If it found a path, cache it.
        if (outputPath != null) {
            pathCache.put(protocolKey, outputPath);
        }
        return outputPath;
    }

    public static Protocol getBaseProtocol(int serverVersion) {
        for (Pair<Range<Integer>, Protocol> rangeProtocol : Lists.reverse(baseProtocols)) {
            if (rangeProtocol.getKey().contains(serverVersion)) {
                return rangeProtocol.getValue();
            }
        }
        throw new IllegalStateException("No Base Protocol for " + serverVersion);
    }

    public static boolean isBaseProtocol(Protocol protocol) {
        for (Pair<Range<Integer>, Protocol> p : baseProtocols) {
            if (p.getValue() == protocol) {
                return true;
            }
        }
        return false;
    }

}
