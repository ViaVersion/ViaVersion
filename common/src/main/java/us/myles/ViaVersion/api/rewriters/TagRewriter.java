package us.myles.ViaVersion.api.rewriters;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.Nullable;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.MappingData;
import us.myles.ViaVersion.api.protocol.ClientboundPacketType;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;

import java.util.ArrayList;
import java.util.List;

public class TagRewriter {
    private static final int[] EMPTY_ARRAY = {};
    private final Protocol protocol;
    private final IdRewriteFunction entityRewriter;
    private final List<TagData> newBlockTags = new ArrayList<>();
    private final List<TagData> newItemTags = new ArrayList<>();
    private final List<TagData> newEntityTags = new ArrayList<>();
    // add fluid tag list if needed at some point

    public TagRewriter(Protocol protocol, @Nullable IdRewriteFunction entityRewriter) {
        this.protocol = protocol;
        this.entityRewriter = entityRewriter;
    }

    /**
     * Adds an empty tag (since the client crashes if a checked tag is not registered.)
     */
    public void addEmptyTag(RegistryType tagType, String id) {
        getNewTags(tagType).add(new TagData(id, EMPTY_ARRAY));
    }

    public void addEmptyTags(RegistryType tagType, String... ids) {
        List<TagData> tagList = getNewTags(tagType);
        for (String id : ids) {
            tagList.add(new TagData(id, EMPTY_ARRAY));
        }
    }

    public void addTag(RegistryType tagType, String id, int... oldIds) {
        List<TagData> newTags = getNewTags(tagType);
        IdRewriteFunction rewriteFunction = getRewriter(tagType);
        for (int i = 0; i < oldIds.length; i++) {
            int oldId = oldIds[i];
            oldIds[i] = rewriteFunction.rewrite(oldId);
        }
        newTags.add(new TagData(id, oldIds));
    }

    public void register(ClientboundPacketType packetType) {
        protocol.registerOutgoing(packetType, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    MappingData mappingData = protocol.getMappingData();
                    handle(wrapper, id -> mappingData != null ? mappingData.getNewBlockId(id) : null, newBlockTags);
                    handle(wrapper, id -> mappingData != null ? mappingData.getNewItemId(id) : null, newItemTags);

                    if (entityRewriter == null && newEntityTags.isEmpty()) return;

                    int fluidTagsSize = wrapper.passthrough(Type.VAR_INT);
                    for (int i = 0; i < fluidTagsSize; i++) {
                        wrapper.passthrough(Type.STRING);
                        wrapper.passthrough(Type.VAR_INT_ARRAY_PRIMITIVE);
                    }

                    handle(wrapper, entityRewriter, newEntityTags);
                });
            }
        });
    }

    private void handle(PacketWrapper wrapper, IdRewriteFunction rewriteFunction, List<TagData> newTags) throws Exception {
        int tagsSize = wrapper.read(Type.VAR_INT);
        wrapper.write(Type.VAR_INT, newTags != null ? tagsSize + newTags.size() : tagsSize); // add new tags count

        for (int i = 0; i < tagsSize; i++) {
            wrapper.passthrough(Type.STRING);
            int[] ids = wrapper.read(Type.VAR_INT_ARRAY_PRIMITIVE);
            if (rewriteFunction != null) {
                // Map ids and filter out new blocks
                IntList idList = new IntArrayList(ids.length);
                for (int id : ids) {
                    int mappedId = rewriteFunction.rewrite(id);
                    if (mappedId != -1) {
                        idList.add(mappedId);
                    }
                }

                wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, idList.toArray(EMPTY_ARRAY));
            } else {
                // Write the original array
                wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, ids);
            }
        }

        // Send new tags if present
        if (newTags != null && !newTags.isEmpty()) {
            for (TagData tag : newTags) {
                wrapper.write(Type.STRING, tag.identifier);
                wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, tag.entries);
            }
        }
    }

    private List<TagData> getNewTags(RegistryType tagType) {
        switch (tagType) {
            case BLOCK:
                return newBlockTags;
            case ITEM:
                return newItemTags;
            case ENTITY:
                return newEntityTags;
            case FLUID:
            default:
                return null;
        }
    }

    @Nullable
    private IdRewriteFunction getRewriter(RegistryType tagType) {
        switch (tagType) {
            case BLOCK:
                return protocol.getMappingData().getBlockMappings() != null ? id -> protocol.getMappingData().getNewBlockId(id) : null;
            case ITEM:
                return protocol.getMappingData().getItemMappings() != null ? id -> protocol.getMappingData().getNewItemId(id) : null;
            case ENTITY:
                return entityRewriter;
            case FLUID:
            default:
                return null;
        }
    }

    private static final class TagData {
        private final String identifier;
        private final int[] entries;

        private TagData(String identifier, int[] entries) {
            this.identifier = identifier;
            this.entries = entries;
        }
    }
}
