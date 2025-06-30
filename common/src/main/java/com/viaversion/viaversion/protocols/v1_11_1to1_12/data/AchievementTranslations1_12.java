/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_11_1to1_12.data;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.HashSet;
import java.util.Set;

public class AchievementTranslations1_12 {

    private static final Object2ObjectOpenHashMap<String, String> ACHIEVEMENTS = new Object2ObjectOpenHashMap<>(150);
    private static final Set<String> SPECIAL_ACHIEVEMENTS = new HashSet<>(10);

    static {
        add("chat.type.achievement", "%s has just earned the achievement %s");
        add("chat.type.achievement.taken", "%s has lost the achievement %s");
        add("stats.tooltip.type.achievement", "Achievement");
        add("stats.tooltip.type.statistic", "Statistic");
        add("stat.generalButton", "General");
        add("stat.blocksButton", "Blocks");
        add("stat.itemsButton", "Items");
        add("stat.mobsButton", "Mobs");
        add("stat.used", "Times Used");
        add("stat.mined", "Times Mined");
        add("stat.depleted", "Times Depleted");
        add("stat.crafted", "Times Crafted");
        add("stat.entityKills", "You killed %s %s");
        add("stat.entityKilledBy", "%s killed you %s time(s)");
        add("stat.entityKills.none", "You have never killed %s");
        add("stat.entityKilledBy.none", "You have never been killed by %s");
        add("stat.startGame", "Times Played");
        add("stat.createWorld", "Worlds Created");
        add("stat.loadWorld", "Saves Loaded");
        add("stat.joinMultiplayer", "Multiplayer Joins");
        add("stat.leaveGame", "Games Quit");
        add("stat.playOneMinute", "Minutes Played");
        add("stat.timeSinceDeath", "Since Last Death");
        add("stat.sneakTime", "Sneak Time");
        add("stat.walkOneCm", "Distance Walked");
        add("stat.crouchOneCm", "Distance Crouched");
        add("stat.sprintOneCm", "Distance Sprinted");
        add("stat.fallOneCm", "Distance Fallen");
        add("stat.swimOneCm", "Distance Swum");
        add("stat.flyOneCm", "Distance Flown");
        add("stat.climbOneCm", "Distance Climbed");
        add("stat.diveOneCm", "Distance Dove");
        add("stat.minecartOneCm", "Distance by Minecart");
        add("stat.boatOneCm", "Distance by Boat");
        add("stat.pigOneCm", "Distance by Pig");
        add("stat.horseOneCm", "Distance by Horse");
        add("stat.aviateOneCm", "Distance by Elytra");
        add("stat.jump", "Jumps");
        add("stat.drop", "Items Dropped");
        add("stat.dropped", "Dropped");
        add("stat.pickup", "Picked Up");
        add("stat.damageDealt", "Damage Dealt");
        add("stat.damageTaken", "Damage Taken");
        add("stat.deaths", "Number of Deaths");
        add("stat.mobKills", "Mob Kills");
        add("stat.animalsBred", "Animals Bred");
        add("stat.playerKills", "Player Kills");
        add("stat.fishCaught", "Fish Caught");
        add("stat.treasureFished", "Treasure Fished");
        add("stat.junkFished", "Junk Fished");
        add("stat.talkedToVillager", "Talked to Villagers");
        add("stat.tradedWithVillager", "Traded with Villagers");
        add("stat.cakeSlicesEaten", "Cake Slices Eaten");
        add("stat.cauldronFilled", "Cauldrons Filled");
        add("stat.cauldronUsed", "Water Taken from Cauldron");
        add("stat.armorCleaned", "Armor Pieces Cleaned");
        add("stat.bannerCleaned", "Banners Cleaned");
        add("stat.brewingstandInteraction", "Interactions with Brewing Stand");
        add("stat.beaconInteraction", "Interactions with Beacon");
        add("stat.dropperInspected", "Droppers Searched");
        add("stat.hopperInspected", "Hoppers Searched");
        add("stat.dispenserInspected", "Dispensers Searched");
        add("stat.noteblockPlayed", "Note Blocks Played");
        add("stat.noteblockTuned", "Note Blocks Tuned");
        add("stat.flowerPotted", "Plants Potted");
        add("stat.trappedChestTriggered", "Trapped Chests Triggered");
        add("stat.enderchestOpened", "Ender Chests Opened");
        add("stat.itemEnchanted", "Items Enchanted");
        add("stat.recordPlayed", "Records Played");
        add("stat.furnaceInteraction", "Interactions with Furnace");
        add("stat.workbenchInteraction", "Interactions with Crafting Table");
        add("stat.chestOpened", "Chests Opened");
        add("stat.shulkerBoxOpened", "Shulker Boxes Opened");
        add("stat.sleepInBed", "Times Slept in a Bed");
        add("stat.mineBlock", "%1$s Mined");
        add("stat.craftItem", "%1$s Crafted");
        add("stat.useItem", "%1$s Used");
        add("stat.breakItem", "%1$s Depleted");
        add("achievement.get", "Achievement get!");
        add("achievement.taken", "Taken!");
        add("achievement.unknown", "???");
        add("achievement.requires", "Requires '%1$s'");
        add("achievement.openInventory", "Taking Inventory");
        add("achievement.openInventory.desc", "Press 'E' to open your inventory");
        add("achievement.mineWood", "Getting Wood");
        add("achievement.mineWood.desc", "Attack a tree until a block of wood pops out");
        add("achievement.buildWorkBench", "Benchmarking");
        add("achievement.buildWorkBench.desc", "Craft a workbench with four blocks of planks");
        add("achievement.buildPickaxe", "Time to Mine!");
        add("achievement.buildPickaxe.desc", "Use planks and sticks to make a pickaxe");
        add("achievement.buildFurnace", "Hot Topic");
        add("achievement.buildFurnace.desc", "Construct a furnace out of eight cobblestone blocks");
        add("achievement.acquireIron", "Acquire Hardware");
        add("achievement.acquireIron.desc", "Smelt an iron ingot");
        add("achievement.buildHoe", "Time to Farm!");
        add("achievement.buildHoe.desc", "Use planks and sticks to make a hoe");
        add("achievement.makeBread", "Bake Bread");
        add("achievement.makeBread.desc", "Turn wheat into bread");
        add("achievement.bakeCake", "The Lie");
        add("achievement.bakeCake.desc", "Wheat, sugar, milk and eggs!");
        add("achievement.buildBetterPickaxe", "Getting an Upgrade");
        add("achievement.buildBetterPickaxe.desc", "Construct a better pickaxe");
        addSpecial("achievement.overpowered", "Overpowered");
        add("achievement.overpowered.desc", "Eat a Notch apple");
        add("achievement.cookFish", "Delicious Fish");
        add("achievement.cookFish.desc", "Catch and cook fish!");
        addSpecial("achievement.onARail", "On A Rail");
        add("achievement.onARail.desc", "Travel by minecart at least 1 km from where you started");
        add("achievement.buildSword", "Time to Strike!");
        add("achievement.buildSword.desc", "Use planks and sticks to make a sword");
        add("achievement.killEnemy", "Monster Hunter");
        add("achievement.killEnemy.desc", "Attack and destroy a monster");
        add("achievement.killCow", "Cow Tipper");
        add("achievement.killCow.desc", "Harvest some leather");
        add("achievement.breedCow", "Repopulation");
        add("achievement.breedCow.desc", "Breed two cows with wheat");
        addSpecial("achievement.flyPig", "When Pigs Fly");
        add("achievement.flyPig.desc", "Fly a pig off a cliff");
        addSpecial("achievement.snipeSkeleton", "Sniper Duel");
        add("achievement.snipeSkeleton.desc", "Kill a skeleton with an arrow from more than 50 meters");
        add("achievement.diamonds", "DIAMONDS!");
        add("achievement.diamonds.desc", "Acquire diamonds with your iron tools");
        add("achievement.diamondsToYou", "Diamonds to you!");
        add("achievement.diamondsToYou.desc", "Throw diamonds at another player");
        add("achievement.portal", "We Need to Go Deeper");
        add("achievement.portal.desc", "Build a portal to the Nether");
        addSpecial("achievement.ghast", "Return to Sender");
        add("achievement.ghast.desc", "Destroy a Ghast with a fireball");
        add("achievement.blazeRod", "Into Fire");
        add("achievement.blazeRod.desc", "Relieve a Blaze of its rod");
        add("achievement.potion", "Local Brewery");
        add("achievement.potion.desc", "Brew a potion");
        addSpecial("achievement.theEnd", "The End?");
        add("achievement.theEnd.desc", "Locate the End");
        addSpecial("achievement.theEnd2", "The End.");
        add("achievement.theEnd2.desc", "Defeat the Ender Dragon");
        add("achievement.spawnWither", "The Beginning?");
        add("achievement.spawnWither.desc", "Spawn the Wither");
        add("achievement.killWither", "The Beginning.");
        add("achievement.killWither.desc", "Kill the Wither");
        addSpecial("achievement.fullBeacon", "Beaconator");
        add("achievement.fullBeacon.desc", "Create a full beacon");
        addSpecial("achievement.exploreAllBiomes", "Adventuring Time");
        add("achievement.exploreAllBiomes.desc", "Discover all biomes");
        add("achievement.enchantments", "Enchanter");
        add("achievement.enchantments.desc", "Use a book, obsidian and diamonds to construct an enchantment table");
        addSpecial("achievement.overkill", "Overkill");
        add("achievement.overkill.desc", "Deal nine hearts of damage in a single hit");
        add("achievement.bookcase", "Librarian");
        add("achievement.bookcase.desc", "Build some bookshelves to improve your enchantment table");
        add("commands.achievement.usage", "/achievement <give|take> <name|*> [player]");
        add("commands.achievement.give.success.all", "Successfully given all achievements to %s");
        add("commands.achievement.take.success.all", "Successfully taken all achievements from %s");
        add("commands.achievement.unknownAchievement", "Unknown achievement '%s'");
        add("commands.achievement.take.success.one", "Successfully taken the achievement %s from %s");
        add("commands.achievement.dontHave", "Player %s doesn't have achievement %s");
        add("commands.achievement.alreadyHave", "Player %s already has achievement %s");
        add("commands.achievement.give.success.one", "Successfully given %s the achievement %s");
    }

    private static void add(String key, String value) {
        ACHIEVEMENTS.put(key, value);
    }

    private static void addSpecial(String key, String value) {
        add(key, value);
        SPECIAL_ACHIEVEMENTS.add(key);
    }

    public static String get(String key) {
        return ACHIEVEMENTS.get(key);
    }

    public static boolean isSpecial(String key) {
        return SPECIAL_ACHIEVEMENTS.contains(key);
    }

}
