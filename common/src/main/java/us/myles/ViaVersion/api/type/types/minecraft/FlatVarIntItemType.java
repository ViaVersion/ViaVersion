package us.myles.ViaVersion.api.type.types.minecraft;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.type.Type;

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
            item.setIdentifier(Type.VAR_INT.read(buffer));
            item.setAmount(buffer.readByte());
            item.setTag(Type.NBT.read(buffer));
            return item;
        }
    }

    @Override
    public void write(ByteBuf buffer, Item object) throws Exception {
        if (object == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            Type.VAR_INT.write(buffer, object.getIdentifier());
            buffer.writeByte(object.getAmount());
            Type.NBT.write(buffer, object.getTag());
        }
    }
}
