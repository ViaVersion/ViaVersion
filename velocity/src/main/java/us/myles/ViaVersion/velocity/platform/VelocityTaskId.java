package us.myles.ViaVersion.velocity.platform;

import com.velocitypowered.api.scheduler.ScheduledTask;
import us.myles.ViaVersion.api.platform.TaskId;

public class VelocityTaskId implements TaskId {
    private final ScheduledTask object;

    public VelocityTaskId(ScheduledTask object) {
        this.object = object;
    }

    @Override
    public ScheduledTask getObject() {
        return object;
    }
}
