package us.myles.ViaVersion.api.protocol;

import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.protocols.protocol1_9_1to1_9.Protocol1_9_1TO1_9;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9TO1_8;

import java.util.*;

public class ProtocolRegistry {
    public static int SERVER_PROTOCOL = -1;
    // Input Version -> Output Version & Protocol (Allows fast lookup)
    private static Map<Integer, Map<Integer, Protocol>> registryMap = new HashMap<>();
    private static Map<Pair<Integer, Integer>, List<Pair<Integer, Protocol>>> pathCache = new HashMap<>();

    static {
        // Register built in protocols
        registerProtocol(new Protocol1_9TO1_8(), Collections.singletonList(ProtocolVersion.v1_9.getId()), ProtocolVersion.v1_8.getId());
        registerProtocol(new Protocol1_9_1TO1_9(), Arrays.asList(ProtocolVersion.v1_9_1.getId(), ProtocolVersion.v1_9_2.getId()), ProtocolVersion.v1_9.getId());
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
     * Calculate a path to get from an input protocol to the servers protocol.
     *
     * @param current       The current items in the path
     * @param clientVersion The current input version
     * @param serverVersion The desired output version
     * @return The path which has been generated, null if failed.
     */
    private static List<Pair<Integer, Protocol>> getProtocolPath(List<Pair<Integer, Protocol>> current, int clientVersion, int serverVersion) {
        if (current.size() > 50) return null; // Fail safe, protocol too complicated.

        // First check if there is any protocols for this
        if (!registryMap.containsKey(clientVersion)) {
            return null; // Not supported
        }
        // Next check there isn't an obvious path
        Map<Integer, Protocol> inputMap = registryMap.get(clientVersion);
        if (inputMap.containsKey(serverVersion)) {
            current.add(new Pair<>(serverVersion, inputMap.get(serverVersion)));
            return current; // Easy solution
        }
        // There might be a more advanced solution... So we'll see if any of the others can get us there
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
                        return newCurrent;
                    }
                }
            }
        }

        return null;
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
        if (pathCache.containsKey(protocolKey)) {
            return pathCache.get(protocolKey);
        }
        // Generate path
        List<Pair<Integer, Protocol>> outputPath = getProtocolPath(new ArrayList<Pair<Integer, Protocol>>(), clientVersion, serverVersion);
        // If it found a path, cache it.
        if (outputPath != null) {
            pathCache.put(protocolKey, outputPath);
        }
        return outputPath;
    }
}
