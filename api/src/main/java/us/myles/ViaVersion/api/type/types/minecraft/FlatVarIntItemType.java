package us.myles.ViaVersion.api.type.types.minecraft;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.minecraft.item.Item;

public class FlatVarIntItemType extends BaseItemType {
    public FlatVarIntItemType() {
        super("FlatVarIntItem");
    }

    @Override
    public Item read(ByteBuf buffer) throws Exception {
        boolean present = buffer.readBoolean();
        if (!present) {
            return null;
        } else {
            Item item = new Item();
            item.setIdentifier(VAR_INT.readPrimitive(buffer));
            item.setAmount(buffer.readByte());
            item.setTag(NBT.read(buffer));
            return item;
        }
    }

    @Override
    public void write(ByteBuf buffer, Item object) throws Exception {
        if (object == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            VAR_INT.writePrimitive(buffer, object.getIdentifier());
            buffer.writeByte(object.getAmount());
            NBT.write(buffer, object.getTag());
        }
    }
}
