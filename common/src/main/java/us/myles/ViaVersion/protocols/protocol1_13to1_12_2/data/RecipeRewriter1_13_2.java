package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.rewriters.ItemRewriter;
import us.myles.ViaVersion.api.rewriters.RecipeRewriter;
import us.myles.ViaVersion.api.type.Type;

/**
 * For 1.13.2, not 1.13 (1.13 reads recipe type and id in swapped order)!
 */
public class RecipeRewriter1_13_2 extends RecipeRewriter {

    public RecipeRewriter1_13_2(Protocol protocol, ItemRewriter.RewriteFunction rewriter) {
        super(protocol, rewriter);
        recipeHandlers.put("crafting_shapeless", this::handleCraftingShapeless);
        recipeHandlers.put("crafting_shaped", this::handleCraftingShaped);
        recipeHandlers.put("smelting", this::handleSmelting);
    }

    public void handleSmelting(PacketWrapper wrapper) throws Exception {
        wrapper.passthrough(Type.STRING); // Group
        Item[] items = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT); // Ingredients
        for (Item item : items) {
            rewriter.rewrite(item);
        }

        rewriter.rewrite(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Result

        wrapper.passthrough(Type.FLOAT); // EXP
        wrapper.passthrough(Type.VAR_INT); // Cooking time
    }

    public void handleCraftingShaped(PacketWrapper wrapper) throws Exception {
        int ingredientsNo = wrapper.passthrough(Type.VAR_INT) * wrapper.passthrough(Type.VAR_INT);
        wrapper.passthrough(Type.STRING); // Group
        for (int j = 0; j < ingredientsNo; j++) {
            Item[] items = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT); // Ingredients
            for (Item item : items) {
                rewriter.rewrite(item);
            }
        }
        rewriter.rewrite(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Result
    }

    public void handleCraftingShapeless(PacketWrapper wrapper) throws Exception {
        wrapper.passthrough(Type.STRING); // Group
        int ingredientsNo = wrapper.passthrough(Type.VAR_INT);
        for (int j = 0; j < ingredientsNo; j++) {
            Item[] items = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT); // Ingredients
            for (Item item : items) {
                rewriter.rewrite(item);
            }
        }
        rewriter.rewrite(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Result
    }
}
