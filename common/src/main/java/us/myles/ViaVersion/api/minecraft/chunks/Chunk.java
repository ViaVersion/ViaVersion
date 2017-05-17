package us.myles.ViaVersion.api.minecraft.chunks;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;

import java.util.List;

public interface Chunk {
    int getX();

    int getZ();

    ChunkSection[] getSections();

    boolean isGroundUp();

    boolean isBiomeData();

    byte[] getBiomeData();

    int getBitmask();

    List<CompoundTag> getBlockEntities();
}
