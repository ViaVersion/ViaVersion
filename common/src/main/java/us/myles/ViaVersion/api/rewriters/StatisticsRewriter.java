package us.myles.ViaVersion.api.rewriters;

import org.jetbrains.annotations.Nullable;
import us.myles.ViaVersion.api.protocol.ClientboundPacketType;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;

public class StatisticsRewriter {
    private final Protocol protocol;
    private final IdRewriteFunction blockRewriter;
    private final IdRewriteFunction itemRewriter;
    private final IdRewriteFunction entityRewriter;
    private final IdRewriteFunction categoryIdRewriter;

    public StatisticsRewriter(Protocol protocol,
                              @Nullable IdRewriteFunction blockRewriter, @Nullable IdRewriteFunction itemRewriter, @Nullable IdRewriteFunction entityRewriter,
                              @Nullable IdRewriteFunction categoryIdRewriter) {
        this.protocol = protocol;
        this.blockRewriter = blockRewriter;
        this.itemRewriter = itemRewriter;
        this.entityRewriter = entityRewriter;
        this.categoryIdRewriter = categoryIdRewriter;
    }

    public StatisticsRewriter(Protocol protocol, @Nullable IdRewriteFunction blockRewriter, @Nullable IdRewriteFunction itemRewriter, @Nullable IdRewriteFunction entityRewriter) {
        this(protocol, blockRewriter, itemRewriter, entityRewriter, null);
    }

    public void register(ClientboundPacketType packetType) {
        protocol.registerOutgoing(packetType, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    int size = wrapper.passthrough(Type.VAR_INT);
                    int newSize = size;
                    for (int i = 0; i < size; i++) {
                        int categoryId = wrapper.read(Type.VAR_INT);
                        int statisticId = wrapper.read(Type.VAR_INT);
                        int value = wrapper.read(Type.VAR_INT);
                        // Rewrite category id
                        if (categoryIdRewriter != null) {
                            categoryId = categoryIdRewriter.rewrite(categoryId);
                            if (categoryId == -1) {
                                // Remove entry
                                newSize--;
                                continue;
                            }
                        }

                        wrapper.write(Type.VAR_INT, categoryId);

                        RegistryType type = getRegistryTypeForStatistic(categoryId);
                        IdRewriteFunction statisticsRewriter;
                        // Rewrite the block/item/entity id
                        if (type != null && (statisticsRewriter = getRewriter(type)) != null) {
                            wrapper.write(Type.VAR_INT, statisticsRewriter.rewrite(statisticId));
                        } else {
                            wrapper.write(Type.VAR_INT, statisticId);
                        }

                        wrapper.write(Type.VAR_INT, value);
                    }

                    if (newSize != size) {
                        wrapper.set(Type.VAR_INT, 0, newSize);
                    }
                });
            }
        });
    }

    @Nullable
    protected IdRewriteFunction getRewriter(RegistryType type) {
        switch (type) {
            case BLOCK:
                return blockRewriter;
            case ITEM:
                return itemRewriter;
            case ENTITY:
                return entityRewriter;
        }
        throw new IllegalArgumentException("Unknown registry type in statistics packet: " + type);
    }

    @Nullable
    public RegistryType getRegistryTypeForStatistic(int statisticsId) {
        switch (statisticsId) {
            case 0:
                return RegistryType.BLOCK;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return RegistryType.ITEM;
            case 6:
            case 7:
                return RegistryType.ENTITY;
            default:
                return null;
        }
    }
}
