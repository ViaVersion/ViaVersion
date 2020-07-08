package us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.packets;

import us.myles.ViaVersion.api.minecraft.BlockChangeRecord;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.rewriters.BlockRewriter;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.Protocol1_16_2To1_16_1;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.types.Chunk1_16_2Type;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.ClientboundPackets1_16;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.types.Chunk1_16Type;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

public class WorldPackets {

    public static void register(Protocol protocol) {
        BlockRewriter blockRewriter = new BlockRewriter(protocol, Type.POSITION1_14, Protocol1_16_2To1_16_1::getNewBlockStateId, Protocol1_16_2To1_16_1::getNewBlockId);

        blockRewriter.registerBlockAction(ClientboundPackets1_16.BLOCK_ACTION);
        blockRewriter.registerBlockChange(ClientboundPackets1_16.BLOCK_CHANGE);
        blockRewriter.registerAcknowledgePlayerDigging(ClientboundPackets1_16.ACKNOWLEDGE_PLAYER_DIGGING);

        protocol.registerOutgoing(ClientboundPackets1_16.CHUNK_DATA, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                    Chunk chunk = wrapper.read(new Chunk1_16Type(clientWorld));
                    wrapper.write(new Chunk1_16_2Type(clientWorld), chunk);

                    for (int s = 0; s < 16; s++) {
                        ChunkSection section = chunk.getSections()[s];
                        if (section == null) continue;
                        for (int i = 0; i < section.getPaletteSize(); i++) {
                            int old = section.getPaletteEntry(i);
                            section.setPaletteEntry(i, Protocol1_16_2To1_16_1.getNewBlockStateId(old));
                        }
                    }
                });
            }
        });

        protocol.registerOutgoing(ClientboundPackets1_16.MULTI_BLOCK_CHANGE, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    wrapper.cancel();

                    int chunkX = wrapper.read(Type.INT);
                    int chunkZ = wrapper.read(Type.INT);
                    wrapper.write(Type.LONG, asLong(chunkX, 0, chunkZ)); //TODO

                    BlockChangeRecord[] blockChangeRecord = wrapper.read(Type.BLOCK_CHANGE_RECORD_ARRAY);
                    wrapper.write(Type.VAR_LONG_BLOCK_CHANGE_RECORD_ARRAY, blockChangeRecord);
                    for (BlockChangeRecord record : blockChangeRecord) {
                        record.setBlockId(Protocol1_16_2To1_16_1.getNewBlockId(record.getBlockId()));
                    }
                });
            }
        });

        blockRewriter.registerEffect(ClientboundPackets1_16.EFFECT, 1010, 2001, InventoryPackets::getNewItemId);
        blockRewriter.registerSpawnParticle(ClientboundPackets1_16.SPAWN_PARTICLE, 3, 23, 34,
                null, InventoryPackets::toClient, Type.FLAT_VAR_INT_ITEM, Type.DOUBLE);
    }

    //TODO to chunk coordinates
    public static int x(final long long1) {
        return (int) (long1 >> 42);
    }

    public static int y(final long long1) {
        return (int) (long1 << 44 >> 44);
    }

    public static int z(final long long1) {
        return (int) (long1 << 22 >> 42);
    }


    public static long asLong(final int x, final int y, final int z) {
        long long4 = 0L;
        long4 |= (x & 0x3FFFFFL) << 42;
        long4 |= (y & 0xFFFFFL);
        long4 |= (z & 0x3FFFFFL) << 20;
        return long4;
    }
}
