package us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.packets;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.minecraft.BlockChangeRecord;
import us.myles.ViaVersion.api.minecraft.BlockChangeRecord1_16_2;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.rewriters.BlockRewriter;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.ClientboundPackets1_16_2;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.Protocol1_16_2To1_16_1;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.types.Chunk1_16_2Type;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.ClientboundPackets1_16;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.types.Chunk1_16Type;

import java.util.ArrayList;
import java.util.List;

public class WorldPackets {

    private static final BlockChangeRecord[] EMPTY_RECORDS = new BlockChangeRecord[0];

    public static void register(Protocol protocol) {
        BlockRewriter blockRewriter = new BlockRewriter(protocol, Type.POSITION1_14);

        blockRewriter.registerBlockAction(ClientboundPackets1_16.BLOCK_ACTION);
        blockRewriter.registerBlockChange(ClientboundPackets1_16.BLOCK_CHANGE);
        blockRewriter.registerAcknowledgePlayerDigging(ClientboundPackets1_16.ACKNOWLEDGE_PLAYER_DIGGING);

        protocol.registerOutgoing(ClientboundPackets1_16.CHUNK_DATA, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    Chunk chunk = wrapper.read(new Chunk1_16Type());
                    wrapper.write(new Chunk1_16_2Type(), chunk);

                    for (int s = 0; s < chunk.getSections().length; s++) {
                        ChunkSection section = chunk.getSections()[s];
                        if (section == null) continue;
                        for (int i = 0; i < section.getPaletteSize(); i++) {
                            int old = section.getPaletteEntry(i);
                            section.setPaletteEntry(i, protocol.getMappingData().getNewBlockStateId(old));
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

                    long chunkPosition = 0;
                    chunkPosition |= (chunkX & 0x3FFFFFL) << 42;
                    chunkPosition |= (chunkZ & 0x3FFFFFL) << 20;

                    List<BlockChangeRecord>[] sectionRecords = new List[16];
                    BlockChangeRecord[] blockChangeRecord = wrapper.read(Type.BLOCK_CHANGE_RECORD_ARRAY);
                    for (BlockChangeRecord record : blockChangeRecord) {
                        int chunkY = record.getY() >> 4;
                        List<BlockChangeRecord> list = sectionRecords[chunkY];
                        if (list == null) {
                            sectionRecords[chunkY] = (list = new ArrayList<>());
                        }

                        // Absolute y -> relative chunk section y
                        int blockId = protocol.getMappingData().getNewBlockStateId(record.getBlockId());
                        list.add(new BlockChangeRecord1_16_2(record.getSectionX(), record.getSectionY(), record.getSectionZ(), blockId));
                    }

                    // Now send separate packets for the different chunk sections
                    for (int chunkY = 0; chunkY < sectionRecords.length; chunkY++) {
                        List<BlockChangeRecord> sectionRecord = sectionRecords[chunkY];
                        if (sectionRecord == null) continue;

                        PacketWrapper newPacket = wrapper.create(ClientboundPackets1_16_2.MULTI_BLOCK_CHANGE);
                        newPacket.write(Type.LONG, chunkPosition | (chunkY & 0xFFFFFL));
                        newPacket.write(Type.BOOLEAN, false); // Ignore light updates
                        newPacket.write(Type.VAR_LONG_BLOCK_CHANGE_RECORD_ARRAY, sectionRecord.toArray(EMPTY_RECORDS));
                        newPacket.send(Protocol1_16_2To1_16_1.class, true, true);
                    }
                });
            }
        });

        blockRewriter.registerEffect(ClientboundPackets1_16.EFFECT, 1010, 2001);
    }
}
