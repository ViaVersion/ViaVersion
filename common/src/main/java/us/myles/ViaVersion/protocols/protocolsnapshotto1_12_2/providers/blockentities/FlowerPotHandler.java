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
        register("minecraft:air", 0, 4466);
        register("minecraft:sapling", 0, 4467);
        register("minecraft:sapling", 1, 4468);
        register("minecraft:sapling", 2, 4469);
        register("minecraft:sapling", 3, 4470);
        register("minecraft:sapling", 4, 4471);
        register("minecraft:sapling", 5, 4472);
        register("minecraft:tallgrass", 2, 4473);
        register("minecraft:yellow_flower", 0, 4474);
        register("minecraft:red_flower", 0, 4475);
        register("minecraft:red_flower", 1, 4476);
        register("minecraft:red_flower", 2, 4477);
        register("minecraft:red_flower", 3, 4478);
        register("minecraft:red_flower", 4, 4479);
        register("minecraft:red_flower", 5, 4480);
        register("minecraft:red_flower", 6, 4481);
        register("minecraft:red_flower", 7, 4482);
        register("minecraft:red_flower", 8, 4483);
        register("minecraft:red_mushroom", 0, 4484);
        register("minecraft:brown_mushroom", 0, 4485);
        register("minecraft:deadbush", 0, 4486);
        register("minecraft:cactus", 0, 4487);

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
