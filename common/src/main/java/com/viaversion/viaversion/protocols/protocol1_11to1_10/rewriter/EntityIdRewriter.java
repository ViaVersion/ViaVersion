/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_11to1_10.rewriter;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.util.Key;

public class EntityIdRewriter {
    private static final BiMap<String, String> oldToNewNames = HashBiMap.create();

    static {
        rewrite("AreaEffectCloud", "area_effect_cloud");
        rewrite("ArmorStand", "armor_stand");
        rewrite("Arrow", "arrow");
        rewrite("Bat", "bat");
        rewrite("Blaze", "blaze");
        rewrite("Boat", "boat");
        rewrite("CaveSpider", "cave_spider");
        rewrite("Chicken", "chicken");
        rewrite("Cow", "cow");
        rewrite("Creeper", "creeper");
        rewrite("Donkey", "donkey");
        rewrite("DragonFireball", "dragon_fireball");
        rewrite("ElderGuardian", "elder_guardian");
        rewrite("EnderCrystal", "ender_crystal");
        rewrite("EnderDragon", "ender_dragon");
        rewrite("Enderman", "enderman");
        rewrite("Endermite", "endermite");
        rewrite("EntityHorse", "horse");
        rewrite("EyeOfEnderSignal", "eye_of_ender_signal");
        rewrite("FallingSand", "falling_block");
        rewrite("Fireball", "fireball");
        rewrite("FireworksRocketEntity", "fireworks_rocket");
        rewrite("Ghast", "ghast");
        rewrite("Giant", "giant");
        rewrite("Guardian", "guardian");
        rewrite("Husk", "husk");
        rewrite("Item", "item");
        rewrite("ItemFrame", "item_frame");
        rewrite("LavaSlime", "magma_cube");
        rewrite("LeashKnot", "leash_knot");
        rewrite("MinecartChest", "chest_minecart");
        rewrite("MinecartCommandBlock", "commandblock_minecart");
        rewrite("MinecartFurnace", "furnace_minecart");
        rewrite("MinecartHopper", "hopper_minecart");
        rewrite("MinecartRideable", "minecart");
        rewrite("MinecartSpawner", "spawner_minecart");
        rewrite("MinecartTNT", "tnt_minecart");
        rewrite("Mule", "mule");
        rewrite("MushroomCow", "mooshroom");
        rewrite("Ozelot", "ocelot");
        rewrite("Painting", "painting");
        rewrite("Pig", "pig");
        rewrite("PigZombie", "zombie_pigman");
        rewrite("PolarBear", "polar_bear");
        rewrite("PrimedTnt", "tnt");
        rewrite("Rabbit", "rabbit");
        rewrite("Sheep", "sheep");
        rewrite("Shulker", "shulker");
        rewrite("ShulkerBullet", "shulker_bullet");
        rewrite("Silverfish", "silverfish");
        rewrite("Skeleton", "skeleton");
        rewrite("SkeletonHorse", "skeleton_horse");
        rewrite("Slime", "slime");
        rewrite("SmallFireball", "small_fireball");
        rewrite("Snowball", "snowball");
        rewrite("SnowMan", "snowman");
        rewrite("SpectralArrow", "spectral_arrow");
        rewrite("Spider", "spider");
        rewrite("Squid", "squid");
        rewrite("Stray", "stray");
        rewrite("ThrownEgg", "egg");
        rewrite("ThrownEnderpearl", "ender_pearl");
        rewrite("ThrownExpBottle", "xp_bottle");
        rewrite("ThrownPotion", "potion");
        rewrite("Villager", "villager");
        rewrite("VillagerGolem", "villager_golem");
        rewrite("Witch", "witch");
        rewrite("WitherBoss", "wither");
        rewrite("WitherSkeleton", "wither_skeleton");
        rewrite("WitherSkull", "wither_skull");
        rewrite("Wolf", "wolf");
        rewrite("XPOrb", "xp_orb");
        rewrite("Zombie", "zombie");
        rewrite("ZombieHorse", "zombie_horse");
        rewrite("ZombieVillager", "zombie_villager");
    }

    private static void rewrite(String oldName, String newName) {
        oldToNewNames.put(oldName, Key.namespaced(newName));
    }

    public static void toClient(CompoundTag tag) {
        toClient(tag, false);
    }

    public static void toClient(CompoundTag tag, boolean backwards) {
        StringTag idTag = tag.getStringTag("id");
        if (idTag != null) {
            String newName = backwards ? oldToNewNames.inverse().get(idTag.getValue()) : oldToNewNames.get(idTag.getValue());
            if (newName != null) {
                idTag.setValue(newName);
            }
        }
    }

    public static void toClientSpawner(CompoundTag tag) {
        toClientSpawner(tag, false);
    }

    public static void toClientSpawner(CompoundTag tag, boolean backwards) {
        if (tag == null) return;

        CompoundTag spawnDataTag = tag.getCompoundTag("SpawnData");
        if (spawnDataTag != null) {
            toClient(spawnDataTag, backwards);
        }
    }

    public static void toClientItem(Item item) {
        toClientItem(item, false);
    }

    public static void toClientItem(Item item, boolean backwards) {
        if (hasEntityTag(item)) {
            toClient(item.tag().getCompoundTag("EntityTag"), backwards);
        }
        if (item != null && item.amount() <= 0) item.setAmount(1);
    }

    public static void toServerItem(Item item) {
        toServerItem(item, false);
    }

    public static void toServerItem(Item item, boolean backwards) {
        if (!hasEntityTag(item)) return;

        CompoundTag entityTag = item.tag().getCompoundTag("EntityTag");
        StringTag idTag = entityTag.getStringTag("id");
        if (idTag != null) {
            String newName = backwards ? oldToNewNames.get(idTag.getValue()) : oldToNewNames.inverse().get(idTag.getValue());
            if (newName != null) {
                idTag.setValue(newName);
            }
        }
    }

    private static boolean hasEntityTag(Item item) {
        if (item == null || item.identifier() != 383) return false; // Monster Egg

        CompoundTag tag = item.tag();
        if (tag == null) return false;

        CompoundTag entityTag = tag.getCompoundTag("EntityTag");
        return entityTag != null && entityTag.getStringTag("id") != null;
    }
}
