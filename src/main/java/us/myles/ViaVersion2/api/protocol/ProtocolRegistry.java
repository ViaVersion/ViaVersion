package us.myles.ViaVersion2.api.protocol;

import us.myles.ViaVersion2.api.protocol1_9_1to1_9.Protocol1_9_1TO1_9;
import us.myles.ViaVersion2.api.protocol1_9to1_8.Protocol1_9TO1_8;
import us.myles.ViaVersion2.api.util.Pair;

import java.util.*;

public class ProtocolRegistry {
    // Input Version -> Output Version & Protocol (Allows fast lookup)
    private static Map<Integer, Map<Integer, Protocol>> registryMap = new HashMap<>();

    public static int SERVER_PROTOCOL = -1;

    static {
        // Register built in protocols
        registerProtocol(new Protocol1_9TO1_8(), Arrays.asList(ProtocolVersion.V1_9), ProtocolVersion.V1_8);
        registerProtocol(new Protocol1_9_1TO1_9(), Arrays.asList(ProtocolVersion.V1_9_1_PRE2), ProtocolVersion.V1_9);
    }

    public static void registerProtocol(Protocol protocol, List<Integer> supported, Integer output) {
        for (Integer version : supported) {
            if (!registryMap.containsKey(version)) {
                registryMap.put(version, new HashMap<Integer, Protocol>());
            }

            registryMap.get(version).put(output, protocol);
        }
    }

    public static boolean isWorkingPipe() {
        for (Map<Integer, Protocol> maps : registryMap.values()) {
            if (maps.containsKey(SERVER_PROTOCOL)) return true;
        }
        return false; // No destination for protocol
    }

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

    public static List<Pair<Integer, Protocol>> getProtocolPath(int clientVersion, int serverVersion) {
        return getProtocolPath(new ArrayList<Pair<Integer, Protocol>>(), clientVersion, serverVersion);
    }
}
