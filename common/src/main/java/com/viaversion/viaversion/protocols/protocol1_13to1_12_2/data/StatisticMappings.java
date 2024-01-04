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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data;

import java.util.HashMap;
import java.util.Map;

public class StatisticMappings {

    public static final Map<String, Integer> CUSTOM_STATS = new HashMap<>();

    static {
        CUSTOM_STATS.put("stat.leaveGame", 0);
        CUSTOM_STATS.put("stat.playOneMinute", 1);
        CUSTOM_STATS.put("stat.timeSinceDeath", 2);
        CUSTOM_STATS.put("stat.sneakTime", 4);
        CUSTOM_STATS.put("stat.walkOneCm", 5);
        CUSTOM_STATS.put("stat.crouchOneCm", 6);
        CUSTOM_STATS.put("stat.sprintOneCm", 7);
        CUSTOM_STATS.put("stat.swimOneCm", 18);
        CUSTOM_STATS.put("stat.fallOneCm", 9);
        CUSTOM_STATS.put("stat.climbOneCm", 10);
        CUSTOM_STATS.put("stat.flyOneCm", 11);
        CUSTOM_STATS.put("stat.diveOneCm", 12);
        CUSTOM_STATS.put("stat.minecartOneCm", 13);
        CUSTOM_STATS.put("stat.boatOneCm", 14);
        CUSTOM_STATS.put("stat.pigOneCm", 15);
        CUSTOM_STATS.put("stat.horseOneCm", 16);
        CUSTOM_STATS.put("stat.aviateOneCm", 17);
        CUSTOM_STATS.put("stat.jump", 19);
        CUSTOM_STATS.put("stat.drop", 20);
        CUSTOM_STATS.put("stat.damageDealt", 21);
        CUSTOM_STATS.put("stat.damageTaken", 22);
        CUSTOM_STATS.put("stat.deaths", 23);
        CUSTOM_STATS.put("stat.mobKills", 24);
        CUSTOM_STATS.put("stat.animalsBred", 25);
        CUSTOM_STATS.put("stat.playerKills", 26);
        CUSTOM_STATS.put("stat.fishCaught", 27);
        CUSTOM_STATS.put("stat.talkedToVillager", 28);
        CUSTOM_STATS.put("stat.tradedWithVillager", 29);
        CUSTOM_STATS.put("stat.cakeSlicesEaten", 30);
        CUSTOM_STATS.put("stat.cauldronFilled", 31);
        CUSTOM_STATS.put("stat.cauldronUsed", 32);
        CUSTOM_STATS.put("stat.armorCleaned", 33);
        CUSTOM_STATS.put("stat.bannerCleaned", 34);
        CUSTOM_STATS.put("stat.brewingstandInter", 35);
        CUSTOM_STATS.put("stat.beaconInteraction", 36);
        CUSTOM_STATS.put("stat.dropperInspected", 37);
        CUSTOM_STATS.put("stat.hopperInspected", 38);
        CUSTOM_STATS.put("stat.dispenserInspecte", 39);
        CUSTOM_STATS.put("stat.noteblockPlayed", 40);
        CUSTOM_STATS.put("stat.noteblockTuned", 41);
        CUSTOM_STATS.put("stat.flowerPotted", 42);
        CUSTOM_STATS.put("stat.trappedChestTriggered", 43);
        CUSTOM_STATS.put("stat.enderchestOpened", 44);
        CUSTOM_STATS.put("stat.itemEnchanted", 45);
        CUSTOM_STATS.put("stat.recordPlayed", 46);
        CUSTOM_STATS.put("stat.furnaceInteraction", 47);
        CUSTOM_STATS.put("stat.craftingTableInteraction", 48);
        CUSTOM_STATS.put("stat.chestOpened", 49);
        CUSTOM_STATS.put("stat.sleepInBed", 50);
        CUSTOM_STATS.put("stat.shulkerBoxOpened", 51);
        CUSTOM_STATS.put("achievement.openInventory", -1);
        CUSTOM_STATS.put("achievement.mineWood", -1);
        CUSTOM_STATS.put("achievement.buildWorkBench", -1);
        CUSTOM_STATS.put("achievement.buildPickaxe", -1);
        CUSTOM_STATS.put("achievement.buildFurnace", -1);
        CUSTOM_STATS.put("achievement.acquireIron", -1);
        CUSTOM_STATS.put("achievement.buildHoe", -1);
        CUSTOM_STATS.put("achievement.makeBread", -1);
        CUSTOM_STATS.put("achievement.bakeCake", -1);
        CUSTOM_STATS.put("achievement.buildBetterPickaxe", -1);
        CUSTOM_STATS.put("achievement.cookFish", -1);
        CUSTOM_STATS.put("achievement.onARail", -1);
        CUSTOM_STATS.put("achievement.buildSword", -1);
        CUSTOM_STATS.put("achievement.killEnemy", -1);
        CUSTOM_STATS.put("achievement.killCow", -1);
        CUSTOM_STATS.put("achievement.flyPig", -1);
        CUSTOM_STATS.put("achievement.snipeSkeleton", -1);
        CUSTOM_STATS.put("achievement.diamonds", -1);
        CUSTOM_STATS.put("achievement.diamondsToYou", -1);
        CUSTOM_STATS.put("achievement.portal", -1);
        CUSTOM_STATS.put("achievement.ghast", -1);
        CUSTOM_STATS.put("achievement.blazeRod", -1);
        CUSTOM_STATS.put("achievement.potion", -1);
        CUSTOM_STATS.put("achievement.theEnd", -1);
        CUSTOM_STATS.put("achievement.theEnd2", -1);
        CUSTOM_STATS.put("achievement.enchantments", -1);
        CUSTOM_STATS.put("achievement.overkill", -1);
        CUSTOM_STATS.put("achievement.bookcase", -1);
        CUSTOM_STATS.put("achievement.breedCow", -1);
        CUSTOM_STATS.put("achievement.spawnWither", -1);
        CUSTOM_STATS.put("achievement.killWither", -1);
        CUSTOM_STATS.put("achievement.fullBeacon", -1);
        CUSTOM_STATS.put("achievement.exploreAllBiomes", -1);
        CUSTOM_STATS.put("achievement.overpowered", -1);
    }

}
