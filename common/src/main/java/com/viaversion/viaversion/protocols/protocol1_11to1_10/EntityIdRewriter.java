/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.protocols.protocol1_11to1_10;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.viaversion.viaversion.api.minecraft.item.Item;

public class EntityIdRewriter {
    private static final BiMap<String, String> oldToNewNames = HashBiMap.create();

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
        toClient(tag, false);
    }

    public static void toClient(CompoundTag tag, boolean backwards) {
        Tag idTag = tag.get("id");
        if (idTag instanceof StringTag) {
            StringTag id = (StringTag) idTag;
            String newName = backwards ? oldToNewNames.inverse().get(id.getValue()) : oldToNewNames.get(id.getValue());
            if (newName != null) {
                id.setValue(newName);
            }
        }
    }

    public static void toClientSpawner(CompoundTag tag) {
        toClientSpawner(tag, false);
    }

    public static void toClientSpawner(CompoundTag tag, boolean backwards) {
        if (tag == null) return;

        Tag spawnDataTag = tag.get("SpawnData");
        if (spawnDataTag != null) {
            toClient((CompoundTag) spawnDataTag, backwards);
        }
    }

    public static void toClientItem(Item item) {
        toClientItem(item, false);
    }

    public static void toClientItem(Item item, boolean backwards) {
        if (hasEntityTag(item)) {
            toClient(item.tag().get("EntityTag"), backwards);
        }
        if (item != null && item.amount() <= 0) item.setAmount(1);
    }

    public static void toServerItem(Item item) {
        toServerItem(item, false);
    }

    public static void toServerItem(Item item, boolean backwards) {
        if (!hasEntityTag(item)) return;

        CompoundTag entityTag = item.tag().get("EntityTag");
        Tag idTag = entityTag.get("id");
        if (idTag instanceof StringTag) {
            StringTag id = (StringTag) idTag;
            String newName = backwards ? oldToNewNames.get(id.getValue()) : oldToNewNames.inverse().get(id.getValue());
            if (newName != null) {
                id.setValue(newName);
            }
        }
    }

    private static boolean hasEntityTag(Item item) {
        if (item == null || item.identifier() != 383) return false; // Monster Egg

        CompoundTag tag = item.tag();
        if (tag == null) return false;

        Tag entityTag = tag.get("EntityTag");
        return entityTag instanceof CompoundTag && ((CompoundTag) entityTag).get("id") instanceof StringTag;
    }
}
