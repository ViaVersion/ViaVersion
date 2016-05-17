package us.myles.ViaVersion.protocols.protocol1_9_1_2to1_9_3;

import io.netty.buffer.ByteBuf;
import org.spacehq.opennbt.tag.builtin.CompoundTag;
import us.myles.ViaVersion.api.type.Type;

import java.util.ArrayList;
import java.util.List;

public class Chunk1_9_3Type extends Type<Chunk1_9_3> {
    public Chunk1_9_3Type() {
        super("1.9.3 Chunk", Chunk1_9_3.class);
    }

    @Override
    public Chunk1_9_3 read(ByteBuf input) throws Exception {
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
        return new Chunk1_9_3(chunkX, chunkZ, groundUp, primaryBitmask, sections, nbtData);
    }

    @Override
    public void write(ByteBuf buffer, Chunk1_9_3 chunk) throws Exception {
        buffer.writeInt(chunk.getX());
        buffer.writeInt(chunk.getZ());

        buffer.writeBoolean(chunk.isGroundUp());
        Type.VAR_INT.write(buffer, chunk.getBitmask());

        Type.VAR_INT.write(buffer, chunk.getSections().length);
        buffer.writeBytes(chunk.getSections());

        // no block entities as it's earlier
    }
}
