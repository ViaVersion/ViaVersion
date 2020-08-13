package us.myles.ViaVersion.bukkit.platform;

import us.myles.ViaVersion.api.platform.TaskId;

public class BukkitTaskId implements TaskId {
    private final Integer object;

    public BukkitTaskId(Integer object) {
        this.object = object;
    }

    @Override
    public Integer getObject() {
        return object;
    }
}
