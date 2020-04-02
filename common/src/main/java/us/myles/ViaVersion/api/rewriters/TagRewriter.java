package us.myles.ViaVersion.api.rewriters;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;

import java.util.ArrayList;
import java.util.List;

public class TagRewriter {
    public static final int[] EMPTY_ARRAY = {};
    private final Protocol protocol;
    private final IdRewriteFunction blockRewriter;
    private final IdRewriteFunction itemRewriter;
    private final IdRewriteFunction entityRewriter;
    private final List<TagData> newBlockTags = new ArrayList<>();
    // add item, fluid, or entity tag lists if needed at some point

    public TagRewriter(Protocol protocol, IdRewriteFunction blockRewriter, IdRewriteFunction itemRewriter, IdRewriteFunction entityRewriter) {
        this.protocol = protocol;
        this.blockRewriter = blockRewriter;
        this.itemRewriter = itemRewriter;
        this.entityRewriter = entityRewriter;
    }

    public void addEmptyBlockTag(String id) {
        newBlockTags.add(new TagData(id, EMPTY_ARRAY));
    }

    public void addBlockTag(String id, int... oldBlockIds) {
        for (int i = 0; i < oldBlockIds.length; i++) {
            int oldBlockId = oldBlockIds[i];
            oldBlockIds[i] = blockRewriter.rewrite(oldBlockId);
        }
        newBlockTags.add(new TagData(id, oldBlockIds));
    }

    public void register(int oldId, int newId) {
        protocol.registerOutgoing(State.PLAY, oldId, newId, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    handle(wrapper, blockRewriter, newBlockTags);
                    handle(wrapper, itemRewriter, null);

                    if (entityRewriter == null) return;

                    int fluidTagsSize = wrapper.passthrough(Type.VAR_INT);
                    for (int i = 0; i < fluidTagsSize; i++) {
                        wrapper.passthrough(Type.STRING);
                        wrapper.passthrough(Type.VAR_INT_ARRAY_PRIMITIVE);
                    }

                    handle(wrapper, entityRewriter, null);
                });
            }
        });
    }

    private void handle(PacketWrapper wrapper, IdRewriteFunction rewriteFunction, List<TagData> newTags) throws Exception {
        int tagsSize = wrapper.read(Type.VAR_INT);
        wrapper.write(Type.VAR_INT, newTags != null ? tagsSize + newTags.size() : tagsSize); // add new tags count

        for (int i = 0; i < tagsSize; i++) {
            wrapper.passthrough(Type.STRING);
            int[] ids = wrapper.passthrough(Type.VAR_INT_ARRAY_PRIMITIVE);
            if (rewriteFunction != null) {
                for (int j = 0; j < ids.length; j++) {
                    ids[j] = rewriteFunction.rewrite(ids[j]);
                }
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

    private static final class TagData {
        private final String identifier;
        private final int[] entries;

        private TagData(String identifier, int[] entries) {
            this.identifier = identifier;
            this.entries = entries;
        }
    }
}
