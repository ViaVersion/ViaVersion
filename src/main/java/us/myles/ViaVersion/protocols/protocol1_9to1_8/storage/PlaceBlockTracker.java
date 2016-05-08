package us.myles.ViaVersion.protocols.protocol1_9to1_8.storage;

import lombok.Getter;
import lombok.Setter;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;

@Getter
public class PlaceBlockTracker extends StoredObject {
    private long lastPlaceTimestamp = System.currentTimeMillis();
    @Setter
    private Position lastPlacedPosition = null;

    public PlaceBlockTracker(UserConnection user) {
        super(user);
    }

    public boolean isExpired(int ms) {
        return System.currentTimeMillis() > (lastPlaceTimestamp + ms);
    }

    public void updateTime() {
        lastPlaceTimestamp = System.currentTimeMillis();
    }
}
