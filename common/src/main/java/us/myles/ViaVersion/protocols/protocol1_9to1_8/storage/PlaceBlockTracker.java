package us.myles.ViaVersion.protocols.protocol1_9to1_8.storage;

import lombok.Getter;
import lombok.Setter;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;

@Getter
public class PlaceBlockTracker extends StoredObject {
    private long lastPlaceTimestamp = 0;
    @Setter
    private Position lastPlacedPosition = null;

    public PlaceBlockTracker(UserConnection user) {
        super(user);
    }

    /**
     * Check if a certain amount of time has passed
     *
     * @param ms The amount of time in MS
     * @return True if it has passed
     */
    public boolean isExpired(int ms) {
        return System.currentTimeMillis() > (lastPlaceTimestamp + ms);
    }

    /**
     * Set the last place time to the current time
     */
    public void updateTime() {
        lastPlaceTimestamp = System.currentTimeMillis();
    }
}
