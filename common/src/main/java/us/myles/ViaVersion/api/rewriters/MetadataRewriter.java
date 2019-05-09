package us.myles.ViaVersion.api.rewriters;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.IEntityType;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public abstract class MetadataRewriter<T extends IEntityType> {

    public final void handleMetadata(int entityId, T type, List<Metadata> metadatas, UserConnection connection) {
        Map<Integer, Metadata> metadataMap = new HashMap<>(metadatas.size());
        for (Metadata metadata : metadatas) {
            metadataMap.put(metadata.getId(), metadata);
        }

        metadataMap = Collections.unmodifiableMap(metadataMap);

        for (Metadata metadata : new ArrayList<>(metadatas)) {
            int oldId = metadata.getId();
            try {
                handleMetadata(entityId, type, metadata, metadatas, metadataMap, connection);
            } catch (Exception e) {
                metadatas.remove(metadata);
                if (!Via.getConfig().isSuppressMetadataErrors() || Via.getManager().isDebug()) {
                    Logger logger = Via.getPlatform().getLogger();

                    logger.warning("An error occurred with entity metadata handler");
                    logger.warning("This is most likely down to one of your plugins sending bad datawatchers. Please test if this occurs without any plugins except ViaVersion before reporting it on GitHub");
                    logger.warning("Also make sure that all your plugins are compatible with your server version.");
                    logger.warning("Entity type: " + type);
                    logger.warning("Metadata: " + metadata);
                    e.printStackTrace();
                }
            }
        }
    }

    protected void handleMetadata(int entityId, T type, Metadata metadata, List<Metadata> metadatas, UserConnection connection) throws Exception {}

    protected void handleMetadata(int entityId, T type, Metadata metadata, List<Metadata> metadatas, Map<Integer, Metadata> metadataMap, UserConnection connection) throws Exception {
        handleMetadata(entityId, type, metadata, metadatas, connection);
    }
}
