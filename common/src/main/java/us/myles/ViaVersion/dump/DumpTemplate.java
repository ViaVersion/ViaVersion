package us.myles.ViaVersion.dump;

import com.google.gson.JsonObject;

import java.util.Map;

public class DumpTemplate {
    private final VersionInfo versionInfo;
    private final Map<String, Object> configuration;
    private final JsonObject platformDump;
    private final JsonObject injectionDump;

    public DumpTemplate(VersionInfo versionInfo, Map<String, Object> configuration, JsonObject platformDump, JsonObject injectionDump) {
        this.versionInfo = versionInfo;
        this.configuration = configuration;
        this.platformDump = platformDump;
        this.injectionDump = injectionDump;
    }

    public VersionInfo getVersionInfo() {
        return versionInfo;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public JsonObject getPlatformDump() {
        return platformDump;
    }

    public JsonObject getInjectionDump() {
        return injectionDump;
    }
}
