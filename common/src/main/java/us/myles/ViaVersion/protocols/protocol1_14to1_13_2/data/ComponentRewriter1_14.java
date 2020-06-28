package us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data;

import com.google.gson.JsonObject;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.ComponentRewriter1_13;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets.InventoryPackets;

public class ComponentRewriter1_14 extends ComponentRewriter1_13 {

    public ComponentRewriter1_14(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected void handleItem(Item item) {
        InventoryPackets.toClient(item);
    }

    @Override
    protected void handleTranslate(JsonObject object, String translate) {
        // Nothing
    }
}
