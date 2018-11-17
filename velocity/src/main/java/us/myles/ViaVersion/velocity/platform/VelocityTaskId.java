package us.myles.ViaVersion.velocity.platform;

import com.velocitypowered.api.scheduler.ScheduledTask;
import lombok.AllArgsConstructor;
import lombok.Getter;
import us.myles.ViaVersion.api.platform.TaskId;

@Getter
@AllArgsConstructor
public class VelocityTaskId implements TaskId {
    private ScheduledTask object;
}
