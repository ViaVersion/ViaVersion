package us.myles.ViaVersion.protocols.protocol1_9to1_8.storage;

import com.github.steveice10.opennbt.tag.builtin.ByteTag;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.Setter;
import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandBlockStorage extends StoredObject {
    private Map<Pair<Integer, Integer>, Map<Position, CompoundTag>> storedCommandBlocks = new ConcurrentHashMap<>();
    @Setter
    @Getter
    private boolean permissions = false;

    public CommandBlockStorage(UserConnection user) {
        super(user);
    }

    public void unloadChunk(int x, int z) {
        Pair<Integer, Integer> chunkPos = new Pair<>(x, z);
        storedCommandBlocks.remove(chunkPos);
    }

    public void addOrUpdateBlock(Position position, CompoundTag tag) {
        Pair<Integer, Integer> chunkPos = getChunkCoords(position);

        if (!storedCommandBlocks.containsKey(chunkPos))
            storedCommandBlocks.put(chunkPos, new ConcurrentHashMap<Position, CompoundTag>());

        Map<Position, CompoundTag> blocks = storedCommandBlocks.get(chunkPos);

        if (blocks.containsKey(position))
            if (blocks.get(position).equals(tag))
                return;

        blocks.put(position, tag);
    }

    private Pair<Integer, Integer> getChunkCoords(Position position) {
        int chunkX = (int) Math.floor(position.getX() / 16);
        int chunkZ = (int) Math.floor(position.getZ() / 16);

        return new Pair<>(chunkX, chunkZ);
    }

    public Optional<CompoundTag> getCommandBlock(Position position) {
        Pair<Integer, Integer> chunkCoords = getChunkCoords(position);

        Map<Position, CompoundTag> blocks = storedCommandBlocks.get(chunkCoords);
        if (blocks == null)
            return Optional.absent();

        CompoundTag tag = blocks.get(position);
        if (tag == null)
            return Optional.absent();

        tag = tag.clone();
        tag.put(new ByteTag("powered", (byte) 0));
        tag.put(new ByteTag("auto", (byte) 0));
        tag.put(new ByteTag("conditionMet", (byte) 0));
        return Optional.of(tag);
    }

    public void unloadChunks() {
        storedCommandBlocks.clear();
    }
}
