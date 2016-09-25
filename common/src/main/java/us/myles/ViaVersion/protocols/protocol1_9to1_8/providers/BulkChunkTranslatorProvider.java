package us.myles.ViaVersion.protocols.protocol1_9to1_8.providers;

import us.myles.ViaVersion.api.platform.providers.Provider;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.ClientChunks;

import java.util.Arrays;
import java.util.List;

public class BulkChunkTranslatorProvider implements Provider {
    public List<Object> transformMapChunkBulk(Object packet, ClientChunks clientChunks) {
        return Arrays.asList(packet);
    }

    public boolean isEnabled() {
        return false;
    }
}
