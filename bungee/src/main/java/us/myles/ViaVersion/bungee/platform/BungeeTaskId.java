package us.myles.ViaVersion.bungee.platform;

import lombok.AllArgsConstructor;
import lombok.Getter;
import us.myles.ViaVersion.api.platform.TaskId;

@Getter
@AllArgsConstructor
public class BungeeTaskId implements TaskId {
    private Integer object;
}
