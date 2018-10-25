package us.myles.ViaVersion.protocols.protocol1_9to1_8.chunks;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import lombok.Getter;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;

import java.util.ArrayList;
import java.util.List;

public class Chunk1_9to1_8 extends Chunk {
    @Getter
    private boolean unloadPacket = false;

    public Chunk1_9to1_8(int x, int z, boolean groundUp, int bitmask, ChunkSection[] sections, byte[] biomeData, List<CompoundTag> blockEntities) {
        super(x, z, groundUp, bitmask, sections, biomeData, blockEntities);
    }

    /**
     * Chunk unload.
     *
     * @param x coord
     * @param z coord
     */
    public Chunk1_9to1_8(int x, int z) {
        this(x, z, true, 0, new ChunkSection[16], null, new ArrayList<CompoundTag>());
        this.unloadPacket = true;
    }

    /**
     * Does this chunks have biome data
     *
     * @return True if the chunks has biome data
     */
    public boolean hasBiomeData() {
        return biomeData != null && groundUp;
    }

    @Override
    public boolean isBiomeData() {
        return biomeData != null;
    }
}
