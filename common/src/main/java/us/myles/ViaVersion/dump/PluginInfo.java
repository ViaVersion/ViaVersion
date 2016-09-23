package us.myles.ViaVersion.dump;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PluginInfo {
    private boolean enabled;
    private String name;
    private String version;
    private String main;
    private List<String> authors;
}
