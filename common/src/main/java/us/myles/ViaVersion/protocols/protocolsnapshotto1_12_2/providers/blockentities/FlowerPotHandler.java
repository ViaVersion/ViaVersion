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
        register("minecraft:air", 0, 4487);
        register("minecraft:sapling", 0, 4488);
        register("minecraft:sapling", 1, 4489);
        register("minecraft:sapling", 2, 4490);
        register("minecraft:sapling", 3, 4491);
        register("minecraft:sapling", 4, 4492);
        register("minecraft:sapling", 5, 4493);
        register("minecraft:tallgrass", 2, 4494);
        register("minecraft:yellow_flower", 0, 4495);
        register("minecraft:red_flower", 0, 4496);
        register("minecraft:red_flower", 1, 4497);
        register("minecraft:red_flower", 2, 4498);
        register("minecraft:red_flower", 3, 4499);
        register("minecraft:red_flower", 4, 4500);
        register("minecraft:red_flower", 5, 4501);
        register("minecraft:red_flower", 6, 4502);
        register("minecraft:red_flower", 7, 4503);
        register("minecraft:red_flower", 8, 4504);
        register("minecraft:red_mushroom", 0, 4505);
        register("minecraft:brown_mushroom", 0, 4506);
        register("minecraft:deadbush", 0, 4507);
        register("minecraft:cactus", 0, 4508);

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
