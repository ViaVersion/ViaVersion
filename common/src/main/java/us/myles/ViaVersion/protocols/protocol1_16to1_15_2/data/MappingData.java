package us.myles.ViaVersion.protocols.protocol1_16to1_15_2.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.MappingDataLoader;
import us.myles.ViaVersion.api.data.Mappings;

import java.util.HashMap;
import java.util.Map;

public class MappingData {
    public static BiMap<Integer, Integer> oldToNewItems = HashBiMap.create();
    public static Map<String, String> attributeMappings = new HashMap<>();
    public static Mappings blockMappings;
    public static Mappings blockStateMappings;
    public static Mappings soundMappings;

    public static void init() {
        Via.getPlatform().getLogger().info("Loading 1.15 -> 1.16 mappings...");
        JsonObject diffmapping = MappingDataLoader.loadData("mappingdiff-1.15to1.16.json");
        JsonObject mapping1_15 = MappingDataLoader.loadData("mapping-1.15.json", true);
        JsonObject mapping1_16 = MappingDataLoader.loadData("mapping-1.16.json", true);

        blockStateMappings = new Mappings(mapping1_15.getAsJsonObject("blockstates"), mapping1_16.getAsJsonObject("blockstates"), diffmapping.getAsJsonObject("blockstates"));
        blockMappings = new Mappings(mapping1_15.getAsJsonObject("blocks"), mapping1_16.getAsJsonObject("blocks"));
        MappingDataLoader.mapIdentifiers(oldToNewItems, mapping1_15.getAsJsonObject("items"), mapping1_16.getAsJsonObject("items"), diffmapping.getAsJsonObject("items"));
        soundMappings = new Mappings(mapping1_15.getAsJsonArray("sounds"), mapping1_16.getAsJsonArray("sounds"), diffmapping.getAsJsonObject("sounds"));

        attributeMappings.put("generic.maxHealth", "minecraft:generic.max_health");
        attributeMappings.put("zombie.spawnReinforcements", "minecraft:zombie.spawn_reinforcements");
        attributeMappings.put("horse.jumpStrength", "minecraft:horse.jump_strength");
        attributeMappings.put("generic.followRange", "minecraft:generic.follow_range");
        attributeMappings.put("generic.knockbackResistance", "minecraft:generic.knockback_resistance");
        attributeMappings.put("generic.movementSpeed", "minecraft:generic.movement_speed");
        attributeMappings.put("generic.flyingSpeed", "minecraft:generic.flying_speed");
        attributeMappings.put("generic.attackDamage", "minecraft:generic.attack_damage");
        attributeMappings.put("generic.attackKnockback", "minecraft:generic.attack_knockback");
        attributeMappings.put("generic.attackSpeed", "minecraft:generic.attack_speed");
        attributeMappings.put("generic.armorToughness", "minecraft:generic.armor_toughness");
    }
}
