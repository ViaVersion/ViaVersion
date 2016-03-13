package us.myles.ViaVersion2.api.type.types.minecraft;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion2.api.item.Item;
import us.myles.ViaVersion2.api.type.Type;

public class ItemArrayType extends Type<Item[]> {

    public ItemArrayType() {
        super("Item Array", Item[].class);
    }

    @Override
    public Item[] read(ByteBuf buffer) {
        int amount = Type.SHORT.read(buffer);
        Item[] array = new Item[amount];
        for (int i = 0; i < amount; i++) {
            array[i] = Type.ITEM.read(buffer);
        }
        return array;
    }

    @Override
    public void write(ByteBuf buffer, Item[] object) {
        Type.VAR_INT.write(buffer, object.length);
        for (Item o : object) {
            Type.ITEM.write(buffer, o);
        }
    }
}