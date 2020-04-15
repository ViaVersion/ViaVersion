package us.myles.ViaVersion.dump;

import java.util.Set;

public class VersionInfo {
    private final String javaVersion;
    private final String operatingSystem;
    private final int serverProtocol;
    private final Set<Integer> enabledProtocols;
    private final String platformName;
    private final String platformVersion;
    private final String pluginVersion;

    public VersionInfo(String javaVersion, String operatingSystem, int serverProtocol, Set<Integer> enabledProtocols, String platformName, String platformVersion, String pluginVersion) {
        this.javaVersion = javaVersion;
        this.operatingSystem = operatingSystem;
        this.serverProtocol = serverProtocol;
        this.enabledProtocols = enabledProtocols;
        this.platformName = platformName;
        this.platformVersion = platformVersion;
        this.pluginVersion = pluginVersion;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public int getServerProtocol() {
        return serverProtocol;
    }

    public Set<Integer> getEnabledProtocols() {
        return enabledProtocols;
    }

    public String getPlatformName() {
        return platformName;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }
}

