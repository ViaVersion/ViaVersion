package us.myles.ViaVersion.protocols.protocol1_11to1_10.storage;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

import java.util.Set;

@Getter
public class FMLTracker extends StoredObject {
    @Setter
    private boolean clientHandshakeComplete;
    @Setter
    private boolean serverHandshakeComplete;
    private Set<Integer> removedSoundIds = Sets.newConcurrentHashSet();

    public FMLTracker(UserConnection user) {
        super(user);
    }

    public boolean isHandshakeComplete() {
        return clientHandshakeComplete && serverHandshakeComplete;
    }

    public void reset() {
        clientHandshakeComplete = false;
        serverHandshakeComplete = false;
        removedSoundIds.clear();
    }
}
