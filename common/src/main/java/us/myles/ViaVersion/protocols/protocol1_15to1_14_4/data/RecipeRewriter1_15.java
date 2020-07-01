package us.myles.ViaVersion.protocols.protocol1_15to1_14_4.data;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.rewriters.ItemRewriter;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data.RecipeRewriter1_14;

public class RecipeRewriter1_15 extends RecipeRewriter1_14 {

    public RecipeRewriter1_15(Protocol protocol, ItemRewriter.RewriteFunction rewriter) {
        super(protocol, rewriter);
    }

    @Override
    public void handle(PacketWrapper wrapper, String type) throws Exception {
        switch (type) {
            case "crafting_shapeless":
                handleCraftingShapeless(wrapper);
                break;
            case "crafting_shaped":
                handleCraftingShaped(wrapper);
                break;
            case "blasting": // new
            case "smoking": // new
            case "campfire_cooking": // new
            case "smelting":
                handleSmelting(wrapper);
                break;
            case "stonecutting": // new
                handleStonecutting(wrapper);
                break;
        }
    }

    public void handleStonecutting(PacketWrapper wrapper) throws Exception {
        wrapper.passthrough(Type.STRING);
        Item[] items = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT); // Ingredients
        for (Item item : items) {
            rewriter.rewrite(item);
        }

        rewriter.rewrite(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Result
    }
}
