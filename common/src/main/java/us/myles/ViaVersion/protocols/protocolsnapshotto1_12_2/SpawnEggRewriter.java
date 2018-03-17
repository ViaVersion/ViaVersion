package us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import us.myles.ViaVersion.api.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;

public class SpawnEggRewriter {
    private static Map<String,Integer> entityToEggId = new HashMap<>();

    static {
        entityToEggId.put("minecraft:bat",620);
        entityToEggId.put("minecraft:blaze",621);
        entityToEggId.put("minecraft:cave_spider",622);
        entityToEggId.put("minecraft:chicken",623);
        entityToEggId.put("minecraft:cow",624);
        entityToEggId.put("minecraft:creeper",625);
        entityToEggId.put("minecraft:donkey",626);
        // drowned
        entityToEggId.put("minecraft:elder_guardian",628);
        entityToEggId.put("minecraft:enderman",629);
        entityToEggId.put("minecraft:endermite",630);
        entityToEggId.put("minecraft:evocation_illager",631);
        entityToEggId.put("minecraft:ghast",632);
        entityToEggId.put("minecraft:guardian",633);
        entityToEggId.put("minecraft:horse",634);
        entityToEggId.put("minecraft:husk",635);
        entityToEggId.put("minecraft:llama",636);
        entityToEggId.put("minecraft:magma_cube",637);
        entityToEggId.put("minecraft:mooshroom",638);
        entityToEggId.put("minecraft:mule",639);
        entityToEggId.put("minecraft:ocelot",640);
        entityToEggId.put("minecraft:parrot",641);
        entityToEggId.put("minecraft:pig",642);
        // phantom
        entityToEggId.put("minecraft:polar_bear",644);
        entityToEggId.put("minecraft:rabbit",645);
        entityToEggId.put("minecraft:sheep",646);
        entityToEggId.put("minecraft:shulker",647);
        entityToEggId.put("minecraft:silverfish",648);
        entityToEggId.put("minecraft:skeleton",649);
        entityToEggId.put("minecraft:skeleton_horse",650);
        entityToEggId.put("minecraft:slime",651);
        entityToEggId.put("minecraft:spider",652);
        entityToEggId.put("minecraft:squid",653);
        entityToEggId.put("minecraft:stray",654);
        entityToEggId.put("minecraft:turtle",655);
        entityToEggId.put("minecraft:vex",656);
        entityToEggId.put("minecraft:villager",657);
        entityToEggId.put("minecraft:vindication_illager",658);
        entityToEggId.put("minecraft:witch",659);
        entityToEggId.put("minecraft:wither_skeleton",660);
        entityToEggId.put("minecraft:wolf",661);
        entityToEggId.put("minecraft:zombie",662);
        entityToEggId.put("minecraft:zombie_horse",663);
        entityToEggId.put("minecraft:zombie_pigman",664);
        entityToEggId.put("minecraft:zombie_villager",665);
    }

    public static boolean toClient(Item item) {
        if (item != null && item.getId() == 383) {
            if (item.getTag() != null) {
                if ((item.getTag().get("EntityTag")) instanceof CompoundTag) {
                    CompoundTag entityTag = item.getTag().get("EntityTag");
                    if (entityTag.get("id") instanceof StringTag){
                        StringTag id = entityTag.get("id");
                        item.setId((short) (int) entityToEggId.get(id.getValue()));
                        return true;
                    } else {
                        System.err.println("Failed to get EntityTag");
                    }
                } else {
                    System.err.println("Failed to get EntityTag from spawn egg");
                }
            } else {
                System.err.println("Failed to get spawn egg: egg doesn't contains NBT tag");
            }
        }
        return false;
    }

    public static boolean toServer(Item item) {
        String entityID = firstKey(entityToEggId, (int) item.getId());
        if (item != null && entityID != null){
            item.setId((short) 383);
            item.setData((short) 0);
            CompoundTag tag = item.getTag();
            if (tag == null) {
                item.setTag(tag = new CompoundTag("tag"));
            }
            CompoundTag entityTag = new CompoundTag("EntityTag");
            tag.put(entityTag);
            entityTag.put(new StringTag("id", entityID));
            return true;
        }
        return false;
    }

    public static <A, B> A firstKey(Map<A, B> map, B value) {
        for (Map.Entry<A, B> entry : map.entrySet()){
            if (entry.getValue().equals(value)){
                return entry.getKey();
            }
        }
        return null;
    }
}
