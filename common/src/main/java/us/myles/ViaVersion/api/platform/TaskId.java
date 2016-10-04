package us.myles.ViaVersion.api.platform;

public interface TaskId {
    /**
     * Returns the actual object represented by this TaskId
     * Null if task cannot be cancelled.
     *
     * @return Platform based Object (don't assume)
     */
    Object getObject();
}
