package us.myles.ViaVersion.protocols.protocol1_16to1_15_2.data;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.rewriters.ItemRewriter;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data.RecipeRewriter1_14;

public class RecipeRewriter1_16 extends RecipeRewriter1_14 {

    public RecipeRewriter1_16(Protocol protocol, ItemRewriter.RewriteFunction rewriter) {
        super(protocol, rewriter);
        recipeHandlers.put("smithing", this::handleSmithing);
    }

    public void handleSmithing(PacketWrapper wrapper) throws Exception {
        Item[] baseIngredients = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT);
        for (Item item : baseIngredients) {
            rewriter.rewrite(item);
        }
        Item[] ingredients = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT);
        for (Item item : ingredients) {
            rewriter.rewrite(item);
        }
        rewriter.rewrite(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Result
    }
}
