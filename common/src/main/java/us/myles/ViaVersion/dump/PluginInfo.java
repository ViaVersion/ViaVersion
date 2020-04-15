package us.myles.ViaVersion.dump;

import java.util.List;

public class PluginInfo {
    private final boolean enabled;
    private final String name;
    private final String version;
    private final String main;
    private final List<String> authors;

    public PluginInfo(boolean enabled, String name, String version, String main, List<String> authors) {
        this.enabled = enabled;
        this.name = name;
        this.version = version;
        this.main = main;
        this.authors = authors;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getMain() {
        return main;
    }

    public List<String> getAuthors() {
        return authors;
    }
}
