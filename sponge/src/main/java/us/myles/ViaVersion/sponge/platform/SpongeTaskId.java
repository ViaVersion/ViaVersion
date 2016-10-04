package us.myles.ViaVersion.sponge.platform;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.spongepowered.api.scheduler.Task;
import us.myles.ViaVersion.api.platform.TaskId;

@Getter
@AllArgsConstructor
public class SpongeTaskId implements TaskId {
    private Task object;
}
