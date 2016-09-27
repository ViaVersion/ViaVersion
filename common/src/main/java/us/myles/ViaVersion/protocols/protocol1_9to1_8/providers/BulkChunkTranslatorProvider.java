package us.myles.ViaVersion.protocols.protocol1_9to1_8.providers;

import us.myles.ViaVersion.api.platform.providers.Provider;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.ClientChunks;

import java.util.Arrays;
import java.util.List;

public class BulkChunkTranslatorProvider implements Provider {
    /**
     * Transforms a Bulk Chunk packet into Chunk Packets
     *
     * @param packet       The NMS Packet
     * @param clientChunks The ClientChunks object for the current player
     * @return A List of all the output packets
     */
    public List<Object> transformMapChunkBulk(Object packet, ClientChunks clientChunks) {
        return Arrays.asList(packet);
    }

    /**
     * Check if a packet of a class should be filtered
     *
     * @param packet NMS Packet
     * @return True if it should be filtered into transformmapChunkBulk
     */
    public boolean isFiltered(Class<?> packet) {
        return false;
    }
}
