package us.myles.ViaVersion.protocols.protocol1_9to1_8.storage;

import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;

public class PlaceBlockTracker extends StoredObject {
    private long lastPlaceTimestamp = 0;
    private Position lastPlacedPosition = null;
    private short x = -1, y = -1, z = -1;

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

    /**
     * Checks if the place clicked the same block again
     *
     * @param lastPlacedPosition the last placed position
     * @param x the x position of the crosshair
     * @param y the y position of the crosshair
     * @param z the z position of the crosshair
     * @return True if the block is the same as the stored block
     */
    public boolean isSame(Position lastPlacedPosition, short x, short y, short z) {
        return lastPlacedPosition.equals(this.lastPlacedPosition)
                && x == this.x
                && y == this.y
                && z == this.z;
    }

    /**
     * Sets the last placed block position and face
     *
     * @param lastPlacedPosition the block position the player just placed a block at
     * @param x the x position of the crosshair
     * @param y the y position of the crosshair
     * @param z the z position of the crosshair
     */
    public void setLastPlacedPosition(Position lastPlacedPosition, short x, short y, short z) {
        this.lastPlacedPosition = lastPlacedPosition;
        this.x = x;
        this.y = y;
        this.z = z;
    }

}
