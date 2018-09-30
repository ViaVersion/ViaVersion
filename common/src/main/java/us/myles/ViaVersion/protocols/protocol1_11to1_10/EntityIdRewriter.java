package us.myles.ViaVersion.protocols.protocol1_11to1_10;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import us.myles.ViaVersion.api.minecraft.item.Item;

public class EntityIdRewriter {
    private static BiMap<String, String> oldToNewNames = HashBiMap.create();

    static {
        oldToNewNames.put("AreaEffectCloud", "minecraft:area_effect_cloud");
        oldToNewNames.put("ArmorStand", "minecraft:armor_stand");
        oldToNewNames.put("Arrow", "minecraft:arrow");
        oldToNewNames.put("Bat", "minecraft:bat");
        oldToNewNames.put("Blaze", "minecraft:blaze");
        oldToNewNames.put("Boat", "minecraft:boat");
        oldToNewNames.put("CaveSpider", "minecraft:cave_spider");
        oldToNewNames.put("Chicken", "minecraft:chicken");
        oldToNewNames.put("Cow", "minecraft:cow");
        oldToNewNames.put("Creeper", "minecraft:creeper");
        oldToNewNames.put("Donkey", "minecraft:donkey");
        oldToNewNames.put("DragonFireball", "minecraft:dragon_fireball");
        oldToNewNames.put("ElderGuardian", "minecraft:elder_guardian");
        oldToNewNames.put("EnderCrystal", "minecraft:ender_crystal");
        oldToNewNames.put("EnderDragon", "minecraft:ender_dragon");
        oldToNewNames.put("Enderman", "minecraft:enderman");
        oldToNewNames.put("Endermite", "minecraft:endermite");
        oldToNewNames.put("EntityHorse", "minecraft:horse");
        oldToNewNames.put("EyeOfEnderSignal", "minecraft:eye_of_ender_signal");
        oldToNewNames.put("FallingSand", "minecraft:falling_block");
        oldToNewNames.put("Fireball", "minecraft:fireball");
        oldToNewNames.put("FireworksRocketEntity", "minecraft:fireworks_rocket");
        oldToNewNames.put("Ghast", "minecraft:ghast");
        oldToNewNames.put("Giant", "minecraft:giant");
        oldToNewNames.put("Guardian", "minecraft:guardian");
        oldToNewNames.put("Husk", "minecraft:husk");
        oldToNewNames.put("Item", "minecraft:item");
        oldToNewNames.put("ItemFrame", "minecraft:item_frame");
        oldToNewNames.put("LavaSlime", "minecraft:magma_cube");
        oldToNewNames.put("LeashKnot", "minecraft:leash_knot");
        oldToNewNames.put("MinecartChest", "minecraft:chest_minecart");
        oldToNewNames.put("MinecartCommandBlock", "minecraft:commandblock_minecart");
        oldToNewNames.put("MinecartFurnace", "minecraft:furnace_minecart");
        oldToNewNames.put("MinecartHopper", "minecraft:hopper_minecart");
        oldToNewNames.put("MinecartRideable", "minecraft:minecart");
        oldToNewNames.put("MinecartSpawner", "minecraft:spawner_minecart");
        oldToNewNames.put("MinecartTNT", "minecraft:tnt_minecart");
        oldToNewNames.put("Mule", "minecraft:mule");
        oldToNewNames.put("MushroomCow", "minecraft:mooshroom");
        oldToNewNames.put("Ozelot", "minecraft:ocelot");
        oldToNewNames.put("Painting", "minecraft:painting");
        oldToNewNames.put("Pig", "minecraft:pig");
        oldToNewNames.put("PigZombie", "minecraft:zombie_pigman");
        oldToNewNames.put("PolarBear", "minecraft:polar_bear");
        oldToNewNames.put("PrimedTnt", "minecraft:tnt");
        oldToNewNames.put("Rabbit", "minecraft:rabbit");
        oldToNewNames.put("Sheep", "minecraft:sheep");
        oldToNewNames.put("Shulker", "minecraft:shulker");
        oldToNewNames.put("ShulkerBullet", "minecraft:shulker_bullet");
        oldToNewNames.put("Silverfish", "minecraft:silverfish");
        oldToNewNames.put("Skeleton", "minecraft:skeleton");
        oldToNewNames.put("SkeletonHorse", "minecraft:skeleton_horse");
        oldToNewNames.put("Slime", "minecraft:slime");
        oldToNewNames.put("SmallFireball", "minecraft:small_fireball");
        oldToNewNames.put("Snowball", "minecraft:snowball");
        oldToNewNames.put("SnowMan", "minecraft:snowman");
        oldToNewNames.put("SpectralArrow", "minecraft:spectral_arrow");
        oldToNewNames.put("Spider", "minecraft:spider");
        oldToNewNames.put("Squid", "minecraft:squid");
        oldToNewNames.put("Stray", "minecraft:stray");
        oldToNewNames.put("ThrownEgg", "minecraft:egg");
        oldToNewNames.put("ThrownEnderpearl", "minecraft:ender_pearl");
        oldToNewNames.put("ThrownExpBottle", "minecraft:xp_bottle");
        oldToNewNames.put("ThrownPotion", "minecraft:potion");
        oldToNewNames.put("Villager", "minecraft:villager");
        oldToNewNames.put("VillagerGolem", "minecraft:villager_golem");
        oldToNewNames.put("Witch", "minecraft:witch");
        oldToNewNames.put("WitherBoss", "minecraft:wither");
        oldToNewNames.put("WitherSkeleton", "minecraft:wither_skeleton");
        oldToNewNames.put("WitherSkull", "minecraft:wither_skull");
        oldToNewNames.put("Wolf", "minecraft:wolf");
        oldToNewNames.put("XPOrb", "minecraft:xp_orb");
        oldToNewNames.put("Zombie", "minecraft:zombie");
        oldToNewNames.put("ZombieHorse", "minecraft:zombie_horse");
        oldToNewNames.put("ZombieVillager", "minecraft:zombie_villager");
    }

    public static void toClient(CompoundTag tag) {
        if (tag.get("id") instanceof StringTag) {
            StringTag id = tag.get("id");
            String newName = oldToNewNames.get(id.getValue());
            if (newName != null) {
                id.setValue(newName);
            }
        }
    }

    public static void toClientSpawner(CompoundTag tag) {
        if (tag != null && tag.contains("SpawnData")) {
            CompoundTag spawnData = tag.get("SpawnData");
            if (spawnData != null && spawnData.contains("id"))
                toClient(spawnData);
        }
    }

    public static void toClientItem(Item item) {
        if (hasEntityTag(item)) {
            CompoundTag entityTag = item.getTag().get("EntityTag");
            toClient(entityTag);
        }
        if (item != null && item.getAmount() <= 0) item.setAmount((byte) 1);
    }

    public static void toServerItem(Item item) {
        if (hasEntityTag(item)) {
            CompoundTag entityTag = item.getTag().get("EntityTag");
            if (entityTag.get("id") instanceof StringTag) {
                StringTag id = entityTag.get("id");
                String newName = oldToNewNames.inverse().get(id.getValue());
                if (newName != null) {
                    id.setValue(newName);
                }
            }
        }
    }

    private static boolean hasEntityTag(Item item) {
        if (item != null && item.getId() == 383) { // Monster Egg
            CompoundTag tag = item.getTag();
            if (tag != null && tag.contains("EntityTag") && tag.get("EntityTag") instanceof CompoundTag) {
                if (((CompoundTag) tag.get("EntityTag")).get("id") instanceof StringTag) {
                    return true;
                }
            }
        }
        return false;
    }
}
