package us.myles.ViaVersion.protocols.protocol1_11to1_10.storage;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

import java.util.Set;

public class FMLTracker extends StoredObject {
    // todo better check
    @Getter
    @Setter
    private boolean handshakeComplete;
    @Getter
    private Set<Integer> removedSoundIds = Sets.newConcurrentHashSet();
    public FMLTracker(UserConnection user) {
        super(user);
    }
    public void reset() {
        handshakeComplete = false;
    }
}
