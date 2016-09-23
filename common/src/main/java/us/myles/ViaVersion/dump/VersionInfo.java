package us.myles.ViaVersion.dump;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class VersionInfo {
    private String version;
    private String bukkitVersion;
    private String javaVersion;
    private String operatingSystem;
    private int serverProtocol;
    private Set<Integer> enabledPipelines;
}

