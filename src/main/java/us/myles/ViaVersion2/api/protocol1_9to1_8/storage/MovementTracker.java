package us.myles.ViaVersion2.api.protocol1_9to1_8.storage;

import lombok.Getter;
import us.myles.ViaVersion2.api.data.StoredObject;

public class MovementTracker extends StoredObject{
    private static final long IDLE_PACKET_DELAY = 50L; // Update every 50ms (20tps)
    private static final long IDLE_PACKET_LIMIT = 20; // Max 20 ticks behind
    @Getter
    private long nextIdlePacket = 0L;

    public void incrementIdlePacket() {
        // Notify of next update
        // Allow a maximum lag spike of 1 second (20 ticks/updates)
        this.nextIdlePacket = Math.max(nextIdlePacket + IDLE_PACKET_DELAY, System.currentTimeMillis() - IDLE_PACKET_DELAY * IDLE_PACKET_LIMIT);
    }
}
