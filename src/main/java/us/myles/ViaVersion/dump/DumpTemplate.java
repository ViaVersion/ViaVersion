package us.myles.ViaVersion.dump;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DumpTemplate {
    private VersionInfo versions;
    private List<PluginInfo> plugins;
}
