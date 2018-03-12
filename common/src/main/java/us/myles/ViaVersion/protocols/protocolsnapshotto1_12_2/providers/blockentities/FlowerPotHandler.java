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
        register("minecraft:air", 0, 5175);
        register("minecraft:sapling", 0, 5176);
        register("minecraft:sapling", 1, 5177);
        register("minecraft:sapling", 2, 5178);
        register("minecraft:sapling", 3, 5179);
        register("minecraft:sapling", 4, 5180);
        register("minecraft:sapling", 5, 5181);
        register("minecraft:tallgrass", 2, 5182);
        register("minecraft:yellow_flower", 0, 5183);
        register("minecraft:red_flower", 0, 5184);
        register("minecraft:red_flower", 1, 5185);
        register("minecraft:red_flower", 2, 5186);
        register("minecraft:red_flower", 3, 5187);
        register("minecraft:red_flower", 4, 5188);
        register("minecraft:red_flower", 5, 5189);
        register("minecraft:red_flower", 6, 5190);
        register("minecraft:red_flower", 7, 5191);
        register("minecraft:red_flower", 8, 5192);
        register("minecraft:red_mushroom", 0, 5193);
        register("minecraft:brown_mushroom", 0, 5194);
        register("minecraft:deadbush", 0, 5195);
        register("minecraft:cactus", 0, 5196);

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
