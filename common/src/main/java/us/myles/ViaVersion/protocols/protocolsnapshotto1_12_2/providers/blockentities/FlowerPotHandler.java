package us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.providers.blockentities;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.providers.BlockEntityProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FlowerPotHandler implements BlockEntityProvider.BlockEntityHandler {
    private static final Map<Pair<String, Integer>, Integer> flowers = new ConcurrentHashMap<>();

    static {
        register("minecraft:air",            0, 5247);
        register("minecraft:sapling",        0, 5248);
        register("minecraft:sapling",        1, 5249);
        register("minecraft:sapling",        2, 5250);
        register("minecraft:sapling",        3, 5251);
        register("minecraft:sapling",        4, 5252);
        register("minecraft:sapling",        5, 5253);
        register("minecraft:tallgrass",      2, 5254);
        register("minecraft:yellow_flower",  0, 5255);
        register("minecraft:red_flower",     0, 5256);
        register("minecraft:red_flower",     1, 5257);
        register("minecraft:red_flower",     2, 5258);
        register("minecraft:red_flower",     3, 5259);
        register("minecraft:red_flower",     4, 5260);
        register("minecraft:red_flower",     5, 5261);
        register("minecraft:red_flower",     6, 5262);
        register("minecraft:red_flower",     7, 5263);
        register("minecraft:red_flower",     8, 5264);
        register("minecraft:red_mushroom",   0, 5265);
        register("minecraft:brown_mushroom", 0, 5266);
        register("minecraft:deadbush",       0, 5267);
        register("minecraft:cactus",         0, 5268);

    }

    public static void register(String identifier, int blockData, int newId) {
        flowers.put(new Pair<>(identifier, blockData), newId);
    }

    @Override
    public int transform(UserConnection user, CompoundTag tag) {
        String item = (String) tag.get("Item").getValue();
        int data = (int) tag.get("Data").getValue();

        Pair<String, Integer> pair = new Pair<>(item, data);

        if (flowers.containsKey(pair)) {
            return flowers.get(pair);
        } else {
            System.out.println("Could not find flowerpot content " + item + " for " + tag);
        }

        return -1;
    }
}
