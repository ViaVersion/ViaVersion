package us.myles.ViaVersion.api.protocol;

import lombok.Data;
import lombok.NonNull;

import java.util.*;

@Data
public class ProtocolVersion {
    private static final Map<Integer, ProtocolVersion> versions = new HashMap<>();

    public static final ProtocolVersion v1_7_1;
    public static final ProtocolVersion v1_7_6;
    public static final ProtocolVersion v1_8;
    public static final ProtocolVersion v1_9;
    public static final ProtocolVersion v1_9_1;
    public static final ProtocolVersion v1_9_2;

    private final int id;
    private final String name;

    static {
        register(v1_7_1 = new ProtocolVersion(4, "1.7-1.7.5"));
        register(v1_7_6 = new ProtocolVersion(5, "1.7.6-1.7.10"));
        register(v1_8 = new ProtocolVersion(47, "1.8.x"));
        register(v1_9 = new ProtocolVersion(107, "1.9"));
        register(v1_9_1 = new ProtocolVersion(108, "1.9.1"));
        register(v1_9_2 = new ProtocolVersion(109, "1.9.2"));
    }

    public static void register(@NonNull ProtocolVersion protocol) {
        versions.put(protocol.getId(), protocol);
    }

    public static boolean isRegistered(int id) {
        return versions.containsKey(id);
    }

    public static ProtocolVersion getProtocol(int id) {
        return versions.get(id);
    }

    public static List<ProtocolVersion> getProtocols() {
        return Collections.unmodifiableList(new ArrayList<ProtocolVersion>(versions.values()));
    }
}
