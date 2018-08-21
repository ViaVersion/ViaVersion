package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.blockentities;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.BlockEntityProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FlowerPotHandler implements BlockEntityProvider.BlockEntityHandler {
    private static final Map<Pair<String, Byte>, Integer> flowers = new ConcurrentHashMap<>();
    private static final Map<Pair<Byte, Byte>, Integer> flowersNumberId = new ConcurrentHashMap<>();

    static {
        register("minecraft:air", (byte) 0, (byte) 0, 5265);
        register("minecraft:sapling", (byte) 6, (byte) 0, 5266);
        register("minecraft:sapling", (byte) 6, (byte) 1, 5267);
        register("minecraft:sapling", (byte) 6, (byte) 2, 5268);
        register("minecraft:sapling", (byte) 6, (byte) 3, 5269);
        register("minecraft:sapling", (byte) 6, (byte) 4, 5270);
        register("minecraft:sapling", (byte) 6, (byte) 5, 5271);
        register("minecraft:tallgrass", (byte) 31, (byte) 2, 5272);
        register("minecraft:yellow_flower", (byte) 37, (byte) 0, 5273);
        register("minecraft:red_flower", (byte) 38, (byte) 0, 5274);
        register("minecraft:red_flower", (byte) 38, (byte) 1, 5275);
        register("minecraft:red_flower", (byte) 38, (byte) 2, 5276);
        register("minecraft:red_flower", (byte) 38, (byte) 3, 5277);
        register("minecraft:red_flower", (byte) 38, (byte) 4, 5278);
        register("minecraft:red_flower", (byte) 38, (byte) 5, 5279);
        register("minecraft:red_flower", (byte) 38, (byte) 6, 5280);
        register("minecraft:red_flower", (byte) 38, (byte) 7, 5281);
        register("minecraft:red_flower", (byte) 38, (byte) 8, 5282);
        register("minecraft:red_mushroom", (byte) 40, (byte) 0, 5283);
        register("minecraft:brown_mushroom", (byte) 39, (byte) 0, 5284);
        register("minecraft:deadbush", (byte) 32, (byte) 0, 5285);
        register("minecraft:cactus", (byte) 81, (byte) 0, 5286);

    }

    public static void register(String identifier, byte numbericBlockId, byte blockData, int newId) {
        flowers.put(new Pair<>(identifier, blockData), newId);
        flowersNumberId.put(new Pair<>(numbericBlockId, blockData), newId);
    }

    @Override
    public int transform(UserConnection user, CompoundTag tag) {
        Object item = tag.get("Item").getValue();
        byte data = ((Number) tag.get("Data").getValue()).byteValue();

        Pair<?, Byte> pair = item instanceof Number
                ? new Pair<>(((Number) item).byteValue(), data)
                : new Pair<>((String) item, data);

        // Return air on empty string
        if (item instanceof String && ((String) item).isEmpty())
            return 5265;
        else if (flowers.containsKey(pair)) {
            return flowers.get(pair);
        } else if (flowersNumberId.containsKey(pair)) {
            return flowersNumberId.get(pair);
        } else {
            if (!Via.getConfig().isSuppress1_13ConversionErrors() || Via.getManager().isDebug()) {
                Via.getPlatform().getLogger().warning("Could not find flowerpot content " + item + " for " + tag);
            }
        }

        return -1;
    }
}
