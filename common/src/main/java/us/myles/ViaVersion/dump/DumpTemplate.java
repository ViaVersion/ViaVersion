package us.myles.ViaVersion.dump;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class DumpTemplate {
    private VersionInfo versionInfo;
    private Map<String, Object> configuration;
    private List<PluginInfo> plugins;
}
