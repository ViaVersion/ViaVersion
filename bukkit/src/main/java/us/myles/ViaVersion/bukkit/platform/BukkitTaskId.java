package us.myles.ViaVersion.bukkit.platform;

import lombok.AllArgsConstructor;
import lombok.Getter;
import us.myles.ViaVersion.api.platform.TaskId;

@Getter
@AllArgsConstructor
public class BukkitTaskId implements TaskId {
    private Integer object;
}
