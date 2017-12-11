package us.myles.ViaVersion.api.type.types.minecraft;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.minecraft.BlockChangeRecord;
import us.myles.ViaVersion.api.type.Type;

public class BlockChangeRecordType extends Type<BlockChangeRecord> {
    public BlockChangeRecordType() {
        super(BlockChangeRecord.class);
    }

    @Override
    public BlockChangeRecord read(ByteBuf buffer) throws Exception {
        short horizontal = Type.UNSIGNED_BYTE.read(buffer);
        short y = Type.UNSIGNED_BYTE.read(buffer);
        int blockId = Type.VAR_INT.read(buffer);

        return new BlockChangeRecord(horizontal, y, blockId);
    }

    @Override
    public void write(ByteBuf buffer, BlockChangeRecord object) throws Exception {
        Type.UNSIGNED_BYTE.write(buffer, object.getHorizontal());
        Type.UNSIGNED_BYTE.write(buffer, object.getY());
        Type.VAR_INT.write(buffer, object.getBlockId());
    }
}
