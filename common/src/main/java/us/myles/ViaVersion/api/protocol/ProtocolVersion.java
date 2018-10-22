package us.myles.ViaVersion.api.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.*;

@AllArgsConstructor
@Getter
public class ProtocolVersion {
    private static final Map<Integer, ProtocolVersion> versions = new HashMap<>();
    private static final List<ProtocolVersion> versionList = new ArrayList<>();

    public static final ProtocolVersion v1_4_6;
    public static final ProtocolVersion v1_5_1;
    public static final ProtocolVersion v1_5_2;
    public static final ProtocolVersion v_1_6_1;
    public static final ProtocolVersion v_1_6_2;
    public static final ProtocolVersion v_1_6_3;
    public static final ProtocolVersion v_1_6_4;
    public static final ProtocolVersion v1_7_1;
    public static final ProtocolVersion v1_7_6;
    public static final ProtocolVersion v1_8;
    public static final ProtocolVersion v1_9;
    public static final ProtocolVersion v1_9_1;
    public static final ProtocolVersion v1_9_2;
    public static final ProtocolVersion v1_9_3;
    public static final ProtocolVersion v1_10;
    public static final ProtocolVersion v1_11;
    public static final ProtocolVersion v1_11_1;
    public static final ProtocolVersion v1_12;
    public static final ProtocolVersion v1_12_1;
    public static final ProtocolVersion v1_12_2;
    public static final ProtocolVersion v1_13;
    public static final ProtocolVersion v1_13_1;
    public static final ProtocolVersion v1_13_2;
    public static final ProtocolVersion unknown;

    private final int id;
    private final String name;

    static {
        // Before netty rewrite
        register(v1_4_6 = new ProtocolVersion(51, "1.4.6"));
        register(v1_5_1 = new ProtocolVersion(60, "1.5.1"));
        register(v1_5_2 = new ProtocolVersion(61, "1.5.2"));
        register(v_1_6_1 = new ProtocolVersion(73, "1.6.1"));
        register(v_1_6_2 = new ProtocolVersion(74, "1.6.2"));
        register(v_1_6_3 = new ProtocolVersion(77, "1.6.3"));
        register(v_1_6_4 = new ProtocolVersion(78, "1.6.4"));
        // After netty rewrite
        register(v1_7_1 = new ProtocolVersion(4, "1.7-1.7.5"));
        register(v1_7_6 = new ProtocolVersion(5, "1.7.6-1.7.10"));
        register(v1_8 = new ProtocolVersion(47, "1.8.x"));
        register(v1_9 = new ProtocolVersion(107, "1.9"));
        register(v1_9_1 = new ProtocolVersion(108, "1.9.1"));
        register(v1_9_2 = new ProtocolVersion(109, "1.9.2"));
        register(v1_9_3 = new ProtocolVersion(110, "1.9.3/4"));
        register(v1_10 = new ProtocolVersion(210, "1.10"));
        register(v1_11 = new ProtocolVersion(315, "1.11"));
        register(v1_11_1 = new ProtocolVersion(316, "1.11.1"));
        register(v1_12 = new ProtocolVersion(335, "1.12"));
        register(v1_12_1 = new ProtocolVersion(338, "1.12.1"));
        register(v1_12_2 = new ProtocolVersion(340, "1.12.2"));
        register(v1_13 = new ProtocolVersion(393, "1.13"));
        register(v1_13_1 = new ProtocolVersion(401, "1.13.1"));
        register(v1_13_2 = new ProtocolVersion(404, "1.13.2"));
        register(unknown = new ProtocolVersion(-1, "UNKNOWN"));
    }

    public static void register(@NonNull ProtocolVersion protocol) {
        versions.put(protocol.getId(), protocol);
        versionList.add(protocol);
    }

    public static boolean isRegistered(int id) {
        return versions.containsKey(id);
    }

    public static ProtocolVersion getProtocol(int id) {
        ProtocolVersion protocolVersion = versions.get(id);
        if (protocolVersion != null) {
            return protocolVersion;
        } else {
            return new ProtocolVersion(id, "Unknown (" + id + ")");
        }
    }

    public static int getIndex(ProtocolVersion version) {
        return versionList.indexOf(version);
    }

    public static List<ProtocolVersion> getProtocols() {
        return Collections.unmodifiableList(new ArrayList<>(versions.values()));
    }

    public static ProtocolVersion getClosest(String protocol) {
        for (ProtocolVersion version : versions.values()) {
            if (version.getName().equals(protocol))
                return version;
            if (version.getName().equals(protocol + ".x"))
                return version;
            String[] parts = version.getName().split("-");
            for (String part : parts) {
                if (part.equalsIgnoreCase(protocol)) {
                    return version;
                }
                if (part.equals(protocol + ".x"))
                    return version;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProtocolVersion that = (ProtocolVersion) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("%s(%d)", this.getName(), this.getId());
    }
}
