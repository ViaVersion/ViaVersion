package us.myles.ViaVersion.protocols.protocol1_12to1_11_1;

import us.myles.ViaVersion.api.minecraft.item.Item;

public class BedRewriter {

    public static void toClientItem(Item item) {
        if (item == null) return;
        if (item.getIdentifier() == 355 && item.getData() == 0) {
            item.setData((short) 14);
        }
    }

    public static void toServerItem(Item item) {
        if (item == null) return;
        if (item.getIdentifier() == 355 && item.getData() == 14) {
            item.setData((short) 0);
        }
    }
}
