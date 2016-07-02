package us.myles.ViaVersion.protocols.protocol1_9_1_2to1_9_3_4;

import io.netty.buffer.ByteBuf;
import org.spacehq.opennbt.tag.builtin.CompoundTag;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.minecraft.BaseChunkType;

import java.util.ArrayList;
import java.util.List;

public class Chunk1_9_3_4Type extends BaseChunkType {
    public Chunk1_9_3_4Type() {
        super("1.9.3 Chunk");
    }

    @Override
    public Chunk read(ByteBuf input) throws Exception {
        int chunkX = input.readInt();
        int chunkZ = input.readInt();

        boolean groundUp = input.readBoolean();
        int primaryBitmask = Type.VAR_INT.read(input);
        int size = Type.VAR_INT.read(input);

        byte[] sections = new byte[size];
        input.readBytes(sections);

        int blockEntities = Type.VAR_INT.read(input);
        List<CompoundTag> nbtData = new ArrayList<>();
        for (int i = 0; i < blockEntities; i++) {
            nbtData.add(Type.NBT.read(input));
        }
        return new Chunk1_9_3_4(chunkX, chunkZ, groundUp, primaryBitmask, new ChunkSection1_9_3_4[] {new ChunkSection1_9_3_4(sections)}, nbtData);
    }

    @Override
    public void write(ByteBuf buffer, Chunk input) throws Exception {
        if (!(input instanceof Chunk1_9_3_4))
            throw new Exception("Tried to send the wrong chunk type from 1.9.3-4 chunk: " + input.getClass());
        Chunk1_9_3_4 chunk = (Chunk1_9_3_4) input;

        buffer.writeInt(chunk.getX());
        buffer.writeInt(chunk.getZ());

        buffer.writeBoolean(chunk.isGroundUp());
        Type.VAR_INT.write(buffer, chunk.getBitmask());

        Type.VAR_INT.write(buffer, chunk.getSections()[0].getData().length);
        buffer.writeBytes(chunk.getSections()[0].getData());

        // no block entities as it's earlier
    }
}
