package us.myles.ViaVersion.api.rewriters;

import us.myles.ViaVersion.api.minecraft.BlockChangeRecord;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;

// If any of these methods become outdated, just create a new rewriter overriding the methods
public class BlockRewriter {
    private final Protocol protocol;
    private final IdRewriteFunction blockStateRewriter;
    private final IdRewriteFunction blockRewriter;
    private final Type<Position> positionType;

    public BlockRewriter(Protocol protocol, Type<Position> positionType, IdRewriteFunction blockStateRewriter, IdRewriteFunction blockRewriter) {
        this.protocol = protocol;
        this.positionType = positionType;
        this.blockStateRewriter = blockStateRewriter;
        this.blockRewriter = blockRewriter;
    }

    public void registerBlockAction(int oldPacketId, int newPacketId) {
        protocol.registerOutgoing(State.PLAY, oldPacketId, newPacketId, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(positionType); // Location
                map(Type.UNSIGNED_BYTE); // Action id
                map(Type.UNSIGNED_BYTE); // Action param
                map(Type.VAR_INT); // Block id - /!\ NOT BLOCK STATE
                handler(wrapper -> wrapper.set(Type.VAR_INT, 0, blockRewriter.rewrite(wrapper.get(Type.VAR_INT, 0))));
            }
        });
    }

    public void registerBlockChange(int oldPacketId, int newPacketId) {
        protocol.registerOutgoing(State.PLAY, oldPacketId, newPacketId, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(positionType);
                map(Type.VAR_INT);
                handler(wrapper -> wrapper.set(Type.VAR_INT, 0, blockStateRewriter.rewrite(wrapper.get(Type.VAR_INT, 0))));
            }
        });
    }

    public void registerMultiBlockChange(int oldPacketId, int newPacketId) {
        protocol.registerOutgoing(State.PLAY, oldPacketId, newPacketId, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // 0 - Chunk X
                map(Type.INT); // 1 - Chunk Z
                map(Type.BLOCK_CHANGE_RECORD_ARRAY); // 2 - Records
                handler(wrapper -> {
                    for (BlockChangeRecord record : wrapper.get(Type.BLOCK_CHANGE_RECORD_ARRAY, 0)) {
                        record.setBlockId(blockStateRewriter.rewrite(record.getBlockId()));
                    }
                });
            }
        });
    }

    public void registerAcknowledgePlayerDigging(int oldPacketId, int newPacketId) {
        // Same exact handler
        registerBlockChange(oldPacketId, newPacketId);
    }

    public void registerEffect(int oldPacketId, int newPacketId, int playRecordId, int blockBreakId, IdRewriteFunction itemIdRewriteFunction) {
        protocol.registerOutgoing(State.PLAY, oldPacketId, newPacketId, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // Effect Id
                map(positionType); // Location
                map(Type.INT); // Data
                handler(wrapper -> {
                    int id = wrapper.get(Type.INT, 0);
                    int data = wrapper.get(Type.INT, 1);
                    if (id == playRecordId) { // Play record
                        wrapper.set(Type.INT, 1, itemIdRewriteFunction.rewrite(data));
                    } else if (id == blockBreakId) { // Block break + block break sound
                        wrapper.set(Type.INT, 1, blockStateRewriter.rewrite(data));
                    }
                });
            }
        });
    }

    public void registerSpawnParticle(Type<?> coordType, int oldPacketId, int newPacketId, int blockId, int fallingDustId, int itemId,
                                      ItemRewriter.RewriteFunction itemRewriteFunction, Type<Item> itemType) {
        registerSpawnParticle(coordType, oldPacketId, newPacketId, blockId, fallingDustId, itemId, null, itemRewriteFunction, itemType);
    }

    public void registerSpawnParticle(Type<?> coordType, int oldPacketId, int newPacketId, int blockId, int fallingDustId, int itemId,
                                      IdRewriteFunction particleRewriteFunction, ItemRewriter.RewriteFunction itemRewriteFunction, Type<Item> itemType) {
        protocol.registerOutgoing(State.PLAY, oldPacketId, newPacketId, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // 0 - Particle ID
                map(Type.BOOLEAN); // 1 - Long Distance
                map(coordType); // 2 - X
                map(coordType); // 3 - Y
                map(coordType); // 4 - Z
                map(Type.FLOAT); // 5 - Offset X
                map(Type.FLOAT); // 6 - Offset Y
                map(Type.FLOAT); // 7 - Offset Z
                map(Type.FLOAT); // 8 - Particle Data
                map(Type.INT); // 9 - Particle Count
                handler(wrapper -> {
                    int id = wrapper.get(Type.INT, 0);
                    if (id == -1) return;
                    if (id == blockId || id == fallingDustId) {
                        int data = wrapper.passthrough(Type.VAR_INT);
                        wrapper.set(Type.VAR_INT, 0, blockStateRewriter.rewrite(data));
                    } else if (id == itemId) {
                        // Has to be like this, until we make *everything* object oriented inside of each protocol :(
                        itemRewriteFunction.rewrite(wrapper.passthrough(itemType));
                    }

                    if (particleRewriteFunction != null) {
                        int newId = particleRewriteFunction.rewrite(id);
                        if (newId != id) {
                            wrapper.set(Type.INT, 0, newId);
                        }
                    }
                });
            }
        });
    }
}
