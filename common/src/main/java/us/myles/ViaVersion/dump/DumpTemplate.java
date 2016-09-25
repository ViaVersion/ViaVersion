package us.myles.ViaVersion.dump;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class DumpTemplate {
    private VersionInfo versionInfo;
    private Map<String, Object> configuration;
    private JsonObject platformDump;
}
