package us.myles.ViaVersion.api.type.types.minecraft;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.type.Type;

public class ItemType extends Type<Item> {
    public ItemType() {
        super(Item.class);
    }

    @Override
    public Item read(ByteBuf buffer) throws Exception {
        short id = buffer.readShort();
        if (id < 0) {
            return null;
        } else {
            Item item = new Item();
            item.setId(id);
            item.setAmount(buffer.readByte());
            item.setData(buffer.readShort());
            item.setTag(Type.NBT.read(buffer));
            return item;
        }
    }

    @Override
    public void write(ByteBuf buffer, Item object) throws Exception {
        if (object == null) {
            buffer.writeShort(-1);
        } else {
            buffer.writeShort(object.getId());
            buffer.writeByte(object.getAmount());
            buffer.writeShort(object.getData());
            Type.NBT.write(buffer, object.getTag());
        }
    }
}
