package us.myles.ViaVersion.protocols.protocol1_16to1_15_2.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;

public class MappingData extends us.myles.ViaVersion.api.data.MappingData {
    private final BiMap<String, String> attributeMappings = HashBiMap.create();

    public MappingData() {
        super("1.15", "1.16", true);
    }

    @Override
    protected void loadExtras(JsonObject oldMappings, JsonObject newMappings, JsonObject diffMappings) {
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

    public BiMap<String, String> getAttributeMappings() {
        return attributeMappings;
    }
}
