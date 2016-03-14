package us.myles.ViaVersion2.api.protocol1_9to1_8.storage;

import com.google.common.collect.Sets;
import lombok.Getter;
import us.myles.ViaVersion2.api.data.StoredObject;

import java.util.Set;

@Getter
public class ClientChunks extends StoredObject {
    private final Set<Long> loadedChunks = Sets.newConcurrentHashSet();
    private final Set<Long> bulkChunks = Sets.newConcurrentHashSet();
}
