package us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.types;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.minecraft.Environment;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import us.myles.ViaVersion.api.type.PartialType;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.minecraft.BaseChunkType;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_10to1_9_3.Protocol1_10To1_9_3_4;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.chunks.Chunk1_9_1_2;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.chunks.ChunkSection1_9_1_2;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

import java.util.ArrayList;
import java.util.BitSet;

public class Chunk1_9_1_2Type extends PartialType<Chunk, ClientWorld> {

    public Chunk1_9_1_2Type(ClientWorld clientWorld) {
        super(clientWorld, Chunk.class);
    }

    @Override
    public Chunk read(ByteBuf input, ClientWorld world) throws Exception {
        boolean replacePistons = world.getUser().get(ProtocolInfo.class).getPipeline().contains(Protocol1_10To1_9_3_4.class) && Via.getConfig().isReplacePistons();
        int replacementId = Via.getConfig().getPistonReplacementId();

        int chunkX = input.readInt();
        int chunkZ = input.readInt();

        boolean groundUp = input.readBoolean();
        int primaryBitmask = Type.VAR_INT.read(input);
        int size = Type.VAR_INT.read(input);

        BitSet usedSections = new BitSet(16);
        ChunkSection1_9_1_2[] sections = new ChunkSection1_9_1_2[16];
        // Calculate section count from bitmask
        for (int i = 0; i < 16; i++) {
            if ((primaryBitmask & (1 << i)) != 0) {
                usedSections.set(i);
            }
        }

        // Read sections
        for (int i = 0; i < 16; i++) {
            if (!usedSections.get(i)) continue; // Section not set
            ChunkSection1_9_1_2 section = new ChunkSection1_9_1_2();
            sections[i] = section;
            section.readBlocks(input);
            section.readBlockLight(input);
            if (world.getEnvironment() == Environment.NORMAL) {
                section.readSkyLight(input);
            }
            if (replacePistons) {
                if (section.getPalette().contains(36)) {
                    section.getPalette().set(section.getPalette().indexOf(36), replacementId);
                }
            }
        }

        byte[] biomeData = groundUp ? new byte[256] : null;
        if (groundUp) {
            input.readBytes(biomeData);
        }

        return new Chunk1_9_1_2(chunkX, chunkZ, groundUp, primaryBitmask, sections, biomeData, new ArrayList<CompoundTag>());
    }

    @Override
    public void write(ByteBuf output, ClientWorld world, Chunk chunk) throws Exception {
        output.writeInt(chunk.getX());
        output.writeInt(chunk.getZ());

        output.writeBoolean(chunk.isGroundUp());
        Type.VAR_INT.write(output, chunk.getBitmask());

        ByteBuf buf = output.alloc().buffer();
        for (int i = 0; i < 16; i++) {
            ChunkSection section = chunk.getSections()[i];
            if (section == null) continue; // Section not set
            section.writeBlocks(buf);
            section.writeBlockLight(buf);

            if (!section.hasSkyLight()) continue; // No sky light, we're done here.
            section.writeSkyLight(buf);

        }
        buf.readerIndex(0);
        Type.VAR_INT.write(output, buf.readableBytes() + (chunk.isBiomeData() ? 256 : 0));
        output.writeBytes(buf);
        buf.release(); // release buffer

        // Write biome data
        if (chunk.isBiomeData()) {
            output.writeBytes(chunk.getBiomeData());
        }
    }

    @Override
    public Class<? extends Type> getBaseClass() {
        return BaseChunkType.class;
    }
}
