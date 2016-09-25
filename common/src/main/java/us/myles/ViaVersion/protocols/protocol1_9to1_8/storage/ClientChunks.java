package us.myles.ViaVersion.protocols.protocol1_9to1_8.storage;

import com.google.common.collect.Sets;
import lombok.Getter;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.BulkChunkTranslatorProvider;

import java.util.List;
import java.util.Set;

@Getter
public class ClientChunks extends StoredObject {


    private final Set<Long> loadedChunks = Sets.newConcurrentHashSet();
    private final Set<Long> bulkChunks = Sets.newConcurrentHashSet();

    public ClientChunks(UserConnection user) {
        super(user);
    }

    public static long toLong(int msw, int lsw) {
        return ((long) msw << 32) + lsw - -2147483648L;
    }

    public List<Object> transformMapChunkBulk(Object packet) {
        return Via.getManager().getProviders().get(BulkChunkTranslatorProvider.class).transformMapChunkBulk(packet, this);
    }
}
