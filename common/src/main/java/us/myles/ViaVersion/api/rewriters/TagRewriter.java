package us.myles.ViaVersion.api.rewriters;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.Nullable;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.MappingData;
import us.myles.ViaVersion.api.protocol.ClientboundPacketType;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class TagRewriter {
    private static final int[] EMPTY_ARRAY = {};
    private final Protocol protocol;
    private final IdRewriteFunction entityRewriter;
    private final Map<RegistryType, List<TagData>> newTags = new EnumMap<>(RegistryType.class);

    public TagRewriter(Protocol protocol, @Nullable IdRewriteFunction entityRewriter) {
        this.protocol = protocol;
        this.entityRewriter = entityRewriter;
    }

    /**
     * Adds an empty tag (since the client crashes if a checked tag is not registered.)
     */
    public void addEmptyTag(RegistryType tagType, String id) {
        getOrComputeNewTags(tagType).add(new TagData(id, EMPTY_ARRAY));
    }

    public void addEmptyTags(RegistryType tagType, String... ids) {
        List<TagData> tagList = getOrComputeNewTags(tagType);
        for (String id : ids) {
            tagList.add(new TagData(id, EMPTY_ARRAY));
        }
    }

    public void addTag(RegistryType tagType, String id, int... oldIds) {
        List<TagData> newTags = getOrComputeNewTags(tagType);
        IdRewriteFunction rewriteFunction = getRewriter(tagType);
        for (int i = 0; i < oldIds.length; i++) {
            int oldId = oldIds[i];
            oldIds[i] = rewriteFunction.rewrite(oldId);
        }
        newTags.add(new TagData(id, oldIds));
    }

    /**
     * @param packetType    packet type
     * @param readUntilType read and process the types until (including) the given registry type
     */
    public void register(ClientboundPacketType packetType, @Nullable RegistryType readUntilType) {
        protocol.registerOutgoing(packetType, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(getHandler(readUntilType));
            }
        });
    }

    public PacketHandler getHandler(@Nullable RegistryType readUntilType) {
        return wrapper -> {
            for (RegistryType type : RegistryType.getValues()) {
                handle(wrapper, getRewriter(type), getNewTags(type));

                // Stop iterating
                if (type == readUntilType) {
                    break;
                }
            }
        };
    }

    public void handle(PacketWrapper wrapper, @Nullable IdRewriteFunction rewriteFunction, List<TagData> newTags) throws Exception {
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
        if (newTags != null) {
            for (TagData tag : newTags) {
                wrapper.write(Type.STRING, tag.identifier);
                wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, tag.entries);
            }
        }
    }

    @Nullable
    public List<TagData> getNewTags(RegistryType tagType) {
        return newTags.get(tagType);
    }

    public List<TagData> getOrComputeNewTags(RegistryType tagType) {
        return newTags.computeIfAbsent(tagType, type -> new ArrayList<>());
    }

    @Nullable
    public IdRewriteFunction getRewriter(RegistryType tagType) {
        MappingData mappingData = protocol.getMappingData();
        switch (tagType) {
            case BLOCK:
                return mappingData != null && mappingData.getBlockMappings() != null ? mappingData::getNewBlockId : null;
            case ITEM:
                return mappingData != null && mappingData.getItemMappings() != null ? mappingData::getNewItemId : null;
            case ENTITY:
                return entityRewriter;
            case FLUID:
            case GAME_EVENT:
            default:
                return null;
        }
    }

    public static final class TagData {
        private final String identifier;
        private final int[] entries;

        public TagData(String identifier, int[] entries) {
            this.identifier = identifier;
            this.entries = entries;
        }

        public String getIdentifier() {
            return identifier;
        }

        public int[] getEntries() {
            return entries;
        }
    }
}
