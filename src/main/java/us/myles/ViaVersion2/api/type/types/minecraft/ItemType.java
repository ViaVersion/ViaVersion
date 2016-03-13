package us.myles.ViaVersion2.api.type.types.minecraft;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion2.api.item.Item;
import us.myles.ViaVersion2.api.type.Type;

// TODO: Implement this class
public class ItemType extends Type<Item> {
    public ItemType() {
        super(Item.class);
    }

    @Override
    public Item read(ByteBuf buffer) {
        return null;
    }

    @Override
    public void write(ByteBuf buffer, Item object) {

    }
}
