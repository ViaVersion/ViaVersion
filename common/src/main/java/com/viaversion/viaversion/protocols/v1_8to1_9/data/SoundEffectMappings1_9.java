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
package com.viaversion.viaversion.protocols.v1_8to1_9.data;

import java.util.HashMap;
import java.util.Map;

public enum SoundEffectMappings1_9 {

    MOB_HORSE_ZOMBIE_IDLE("mob.horse.zombie.idle", "entity.zombie_horse.ambient", SoundCategories1_8.NEUTRAL),
    NOTE_SNARE("note.snare", "block.note.snare", SoundCategories1_8.RECORD),
    RANDOM_WOOD_CLICK("random.wood_click", "block.wood_button.click_on", SoundCategories1_8.BLOCK),
    DIG_GRAVEL("dig.gravel", "block.gravel.break", SoundCategories1_8.BLOCK),
    RANDOM_BOWHIT("random.bowhit", "entity.arrow.hit", SoundCategories1_8.NEUTRAL),
    DIG_GLASS("dig.glass", "block.glass.break", SoundCategories1_8.BLOCK),
    MOB_ZOMBIE_SAY("mob.zombie.say", "entity.zombie.ambient", SoundCategories1_8.HOSTILE),
    MOB_PIG_DEATH("mob.pig.death", "entity.pig.death", SoundCategories1_8.NEUTRAL),
    MOB_HORSE_DONKEY_HIT("mob.horse.donkey.hit", "entity.donkey.hurt", SoundCategories1_8.NEUTRAL),
    GAME_NEUTRAL_SWIM("game.neutral.swim", "entity.player.swim", SoundCategories1_8.NEUTRAL),
    GAME_PLAYER_SWIM("game.player.swim", "entity.player.swim", SoundCategories1_8.PLAYER),
    MOB_ENDERMEN_IDLE("mob.endermen.idle", "entity.endermen.ambient", SoundCategories1_8.HOSTILE),
    PORTAL_PORTAL("portal.portal", "block.portal.ambient", SoundCategories1_8.BLOCK),
    RANDOM_FIZZ("random.fizz", "entity.generic.extinguish_fire", SoundCategories1_8.BLOCK),
    NOTE_HARP("note.harp", "block.note.harp", SoundCategories1_8.RECORD),
    STEP_SNOW("step.snow", "block.snow.step", SoundCategories1_8.NEUTRAL),
    RANDOM_SUCCESSFUL_HIT("random.successful_hit", "entity.arrow.hit_player", SoundCategories1_8.PLAYER),
    MOB_ZOMBIEPIG_ZPIGHURT("mob.zombiepig.zpighurt", "entity.zombie_pig.hurt", SoundCategories1_8.HOSTILE),
    MOB_WOLF_HOWL("mob.wolf.howl", "entity.wolf.howl", SoundCategories1_8.NEUTRAL),
    FIREWORKS_LAUNCH("fireworks.launch", "entity.firework.launch", SoundCategories1_8.AMBIENT),
    MOB_COW_HURT("mob.cow.hurt", "entity.cow.hurt", SoundCategories1_8.NEUTRAL),
    FIREWORKS_LARGEBLAST("fireworks.largeBlast", "entity.firework.large_blast", SoundCategories1_8.AMBIENT),
    MOB_BLAZE_HIT("mob.blaze.hit", "entity.blaze.hurt", SoundCategories1_8.HOSTILE),
    MOB_VILLAGER_DEATH("mob.villager.death", "entity.villager.death", SoundCategories1_8.NEUTRAL),
    MOB_BLAZE_DEATH("mob.blaze.death", "entity.blaze.death", SoundCategories1_8.HOSTILE),
    MOB_HORSE_ZOMBIE_DEATH("mob.horse.zombie.death", "entity.zombie_horse.death", SoundCategories1_8.NEUTRAL),
    MOB_SILVERFISH_KILL("mob.silverfish.kill", "entity.silverfish.death", SoundCategories1_8.HOSTILE),
    MOB_WOLF_PANTING("mob.wolf.panting", "entity.wolf.pant", SoundCategories1_8.NEUTRAL),
    NOTE_BASS("note.bass", "block.note.bass", SoundCategories1_8.RECORD),
    DIG_STONE("dig.stone", "block.stone.break", SoundCategories1_8.BLOCK),
    MOB_ENDERMEN_STARE("mob.endermen.stare", "entity.endermen.stare", SoundCategories1_8.HOSTILE),
    GAME_PLAYER_SWIM_SPLASH("game.player.swim.splash", "entity.generic.splash", SoundCategories1_8.BLOCK),
    MOB_SLIME_SMALL("mob.slime.small", "entity.small_slime.hurt", SoundCategories1_8.HOSTILE),
    MOB_GHAST_DEATH("mob.ghast.death", "entity.ghast.death", SoundCategories1_8.HOSTILE),
    MOB_GUARDIAN_ATTACK("mob.guardian.attack", "entity.guardian.attack", SoundCategories1_8.HOSTILE),
    RANDOM_CLICK("random.click", "block.stone_button.click_on", SoundCategories1_8.BLOCK),
    MOB_ZOMBIEPIG_ZPIG("mob.zombiepig.zpig", "entity.zombie_pig.ambient", SoundCategories1_8.HOSTILE),
    GAME_PLAYER_DIE("game.player.die", "entity.player.death", SoundCategories1_8.PLAYER),
    FIREWORKS_TWINKLE_FAR("fireworks.twinkle_far", "entity.firework.twinkle_far", SoundCategories1_8.AMBIENT),
    MOB_GUARDIAN_LAND_IDLE("mob.guardian.land.idle", "entity.guardian.ambient_land", SoundCategories1_8.HOSTILE),
    DIG_GRASS("dig.grass", "block.grass.break", SoundCategories1_8.BLOCK),
    MOB_SKELETON_STEP("mob.skeleton.step", "entity.skeleton.step", SoundCategories1_8.HOSTILE),
    MOB_WITHER_DEATH("mob.wither.death", "entity.wither.death", SoundCategories1_8.HOSTILE),
    MOB_WOLF_HURT("mob.wolf.hurt", "entity.wolf.hurt", SoundCategories1_8.NEUTRAL),
    MOB_HORSE_LEATHER("mob.horse.leather", "entity.horse.saddle", SoundCategories1_8.NEUTRAL),
    MOB_BAT_LOOP("mob.bat.loop", "entity.bat.loop", SoundCategories1_8.NEUTRAL),
    MOB_GHAST_SCREAM("mob.ghast.scream", "entity.ghast.hurt", SoundCategories1_8.HOSTILE),
    GAME_PLAYER_HURT("game.player.hurt", "entity.player.hurt", SoundCategories1_8.PLAYER),
    GAME_NEUTRAL_DIE("game.neutral.die", "entity.player.death", SoundCategories1_8.NEUTRAL),
    MOB_CREEPER_DEATH("mob.creeper.death", "entity.creeper.death", SoundCategories1_8.HOSTILE),
    MOB_HORSE_GALLOP("mob.horse.gallop", "entity.horse.gallop", SoundCategories1_8.NEUTRAL),
    MOB_WITHER_SPAWN("mob.wither.spawn", "entity.wither.spawn", SoundCategories1_8.HOSTILE),
    MOB_ENDERMEN_HIT("mob.endermen.hit", "entity.endermen.hurt", SoundCategories1_8.HOSTILE),
    MOB_CREEPER_SAY("mob.creeper.say", "entity.creeper.hurt", SoundCategories1_8.HOSTILE),
    MOB_HORSE_WOOD("mob.horse.wood", "entity.horse.step_wood", SoundCategories1_8.NEUTRAL),
    MOB_ZOMBIE_UNFECT("mob.zombie.unfect", "entity.zombie_villager.converted", SoundCategories1_8.HOSTILE),
    RANDOM_ANVIL_USE("random.anvil_use", "block.anvil.use", SoundCategories1_8.BLOCK),
    RANDOM_CHESTCLOSED("random.chestclosed", "block.chest.close", SoundCategories1_8.BLOCK),
    MOB_SHEEP_SHEAR("mob.sheep.shear", "entity.sheep.shear", SoundCategories1_8.NEUTRAL),
    RANDOM_POP("random.pop", "entity.item.pickup", SoundCategories1_8.PLAYER),
    MOB_BAT_DEATH("mob.bat.death", "entity.bat.death", SoundCategories1_8.NEUTRAL),
    DIG_WOOD("dig.wood", "block.wood.break", SoundCategories1_8.BLOCK),
    MOB_HORSE_DONKEY_DEATH("mob.horse.donkey.death", "entity.donkey.death", SoundCategories1_8.NEUTRAL),
    FIREWORKS_BLAST("fireworks.blast", "entity.firework.blast", SoundCategories1_8.AMBIENT),
    MOB_ZOMBIEPIG_ZPIGANGRY("mob.zombiepig.zpigangry", "entity.zombie_pig.angry", SoundCategories1_8.HOSTILE),
    GAME_HOSTILE_SWIM("game.hostile.swim", "entity.player.swim", SoundCategories1_8.HOSTILE),
    MOB_GUARDIAN_FLOP("mob.guardian.flop", "entity.guardian.flop", SoundCategories1_8.HOSTILE),
    MOB_VILLAGER_YES("mob.villager.yes", "entity.villager.yes", SoundCategories1_8.NEUTRAL),
    MOB_GHAST_CHARGE("mob.ghast.charge", "entity.ghast.warn", SoundCategories1_8.HOSTILE),
    CREEPER_PRIMED("creeper.primed", "entity.creeper.primed", SoundCategories1_8.HOSTILE),
    DIG_SAND("dig.sand", "block.sand.break", SoundCategories1_8.BLOCK),
    MOB_CHICKEN_SAY("mob.chicken.say", "entity.chicken.ambient", SoundCategories1_8.NEUTRAL),
    RANDOM_DOOR_CLOSE("random.door_close", "block.wooden_door.close", SoundCategories1_8.BLOCK),
    MOB_GUARDIAN_ELDER_DEATH("mob.guardian.elder.death", "entity.elder_guardian.death", SoundCategories1_8.HOSTILE),
    FIREWORKS_TWINKLE("fireworks.twinkle", "entity.firework.twinkle", SoundCategories1_8.AMBIENT),
    MOB_HORSE_SKELETON_DEATH("mob.horse.skeleton.death", "entity.skeleton_horse.death", SoundCategories1_8.NEUTRAL),
    AMBIENT_WEATHER_RAIN("ambient.weather.rain", "weather.rain", SoundCategories1_8.WEATHER),
    PORTAL_TRIGGER("portal.trigger", "block.portal.trigger", SoundCategories1_8.BLOCK),
    RANDOM_CHESTOPEN("random.chestopen", "block.chest.open", SoundCategories1_8.BLOCK),
    MOB_HORSE_LAND("mob.horse.land", "entity.horse.land", SoundCategories1_8.NEUTRAL),
    MOB_SILVERFISH_STEP("mob.silverfish.step", "entity.silverfish.step", SoundCategories1_8.HOSTILE),
    MOB_BAT_TAKEOFF("mob.bat.takeoff", "entity.bat.takeoff", SoundCategories1_8.NEUTRAL),
    MOB_VILLAGER_NO("mob.villager.no", "entity.villager.no", SoundCategories1_8.NEUTRAL),
    GAME_HOSTILE_HURT_FALL_BIG("game.hostile.hurt.fall.big", "entity.hostile.big_fall", SoundCategories1_8.HOSTILE),
    MOB_IRONGOLEM_WALK("mob.irongolem.walk", "entity.irongolem.step", SoundCategories1_8.NEUTRAL),
    NOTE_HAT("note.hat", "block.note.hat", SoundCategories1_8.RECORD),
    MOB_ZOMBIE_METAL("mob.zombie.metal", "entity.zombie.attack_iron_door", SoundCategories1_8.HOSTILE),
    MOB_VILLAGER_HAGGLE("mob.villager.haggle", "entity.villager.trading", SoundCategories1_8.NEUTRAL),
    MOB_GHAST_FIREBALL("mob.ghast.fireball", "entity.ghast.shoot", SoundCategories1_8.HOSTILE),
    MOB_IRONGOLEM_DEATH("mob.irongolem.death", "entity.irongolem.death", SoundCategories1_8.NEUTRAL),
    RANDOM_BREAK("random.break", "entity.item.break", SoundCategories1_8.PLAYER),
    MOB_ZOMBIE_REMEDY("mob.zombie.remedy", "entity.zombie_villager.cure", SoundCategories1_8.HOSTILE),
    RANDOM_BOW("random.bow", "entity.arrow.shoot", SoundCategories1_8.NEUTRAL),
    MOB_VILLAGER_IDLE("mob.villager.idle", "entity.villager.ambient", SoundCategories1_8.NEUTRAL),
    STEP_CLOTH("step.cloth", "block.cloth.step", SoundCategories1_8.NEUTRAL),
    MOB_SILVERFISH_HIT("mob.silverfish.hit", "entity.silverfish.hurt", SoundCategories1_8.HOSTILE),
    LIQUID_LAVA("liquid.lava", "block.lava.ambient", SoundCategories1_8.BLOCK),
    GAME_NEUTRAL_HURT_FALL_BIG("game.neutral.hurt.fall.big", "entity.hostile.big_fall", SoundCategories1_8.NEUTRAL),
    FIRE_FIRE("fire.fire", "block.fire.ambient", SoundCategories1_8.BLOCK),
    MOB_ZOMBIE_WOOD("mob.zombie.wood", "entity.zombie.attack_door_wood", SoundCategories1_8.HOSTILE),
    MOB_CHICKEN_STEP("mob.chicken.step", "entity.chicken.step", SoundCategories1_8.NEUTRAL),
    MOB_GUARDIAN_LAND_HIT("mob.guardian.land.hit", "entity.guardian.hurt_land", SoundCategories1_8.HOSTILE),
    MOB_CHICKEN_PLOP("mob.chicken.plop", "entity.chicken.egg", SoundCategories1_8.NEUTRAL),
    MOB_ENDERDRAGON_WINGS("mob.enderdragon.wings", "entity.enderdragon.flap", SoundCategories1_8.HOSTILE),
    STEP_GRASS("step.grass", "block.grass.step", SoundCategories1_8.NEUTRAL),
    MOB_HORSE_BREATHE("mob.horse.breathe", "entity.horse.breathe", SoundCategories1_8.NEUTRAL),
    GAME_PLAYER_HURT_FALL_BIG("game.player.hurt.fall.big", "entity.hostile.big_fall", SoundCategories1_8.PLAYER),
    MOB_HORSE_DONKEY_IDLE("mob.horse.donkey.idle", "entity.donkey.ambient", SoundCategories1_8.NEUTRAL),
    MOB_SPIDER_STEP("mob.spider.step", "entity.spider.step", SoundCategories1_8.HOSTILE),
    GAME_NEUTRAL_HURT("game.neutral.hurt", "entity.player.death", SoundCategories1_8.NEUTRAL),
    MOB_COW_SAY("mob.cow.say", "entity.cow.ambient", SoundCategories1_8.NEUTRAL),
    MOB_HORSE_JUMP("mob.horse.jump", "entity.horse.jump", SoundCategories1_8.NEUTRAL),
    MOB_HORSE_SOFT("mob.horse.soft", "entity.horse.step", SoundCategories1_8.NEUTRAL),
    GAME_NEUTRAL_SWIM_SPLASH("game.neutral.swim.splash", "entity.generic.splash", SoundCategories1_8.NEUTRAL),
    MOB_GUARDIAN_HIT("mob.guardian.hit", "entity.guardian.hurt", SoundCategories1_8.HOSTILE),
    MOB_ENDERDRAGON_END("mob.enderdragon.end", "entity.enderdragon.death", SoundCategories1_8.HOSTILE),
    MOB_ZOMBIE_STEP("mob.zombie.step", "entity.zombie.step", SoundCategories1_8.HOSTILE),
    MOB_ENDERDRAGON_GROWL("mob.enderdragon.growl", "entity.enderdragon.growl", SoundCategories1_8.HOSTILE),
    MOB_WOLF_SHAKE("mob.wolf.shake", "entity.wolf.shake", SoundCategories1_8.NEUTRAL),
    MOB_ENDERMEN_DEATH("mob.endermen.death", "entity.endermen.death", SoundCategories1_8.HOSTILE),
    RANDOM_ANVIL_LAND("random.anvil_land", "block.anvil.land", SoundCategories1_8.BLOCK),
    GAME_HOSTILE_HURT("game.hostile.hurt", "entity.player.death", SoundCategories1_8.HOSTILE),
    MINECART_INSIDE("minecart.inside", "entity.minecart.inside", SoundCategories1_8.PLAYER),
    MOB_SLIME_BIG("mob.slime.big", "entity.slime.squish", SoundCategories1_8.HOSTILE),
    LIQUID_WATER("liquid.water", "block.water.ambient", SoundCategories1_8.BLOCK),
    MOB_PIG_SAY("mob.pig.say", "entity.pig.ambient", SoundCategories1_8.NEUTRAL),
    MOB_WITHER_SHOOT("mob.wither.shoot", "entity.wither.shoot", SoundCategories1_8.HOSTILE),
    ITEM_FIRECHARGE_USE("item.fireCharge.use", "entity.blaze.shoot", SoundCategories1_8.BLOCK),
    STEP_SAND("step.sand", "block.sand.step", SoundCategories1_8.NEUTRAL),
    MOB_IRONGOLEM_HIT("mob.irongolem.hit", "entity.irongolem.hurt", SoundCategories1_8.NEUTRAL),
    MOB_HORSE_DEATH("mob.horse.death", "entity.horse.death", SoundCategories1_8.NEUTRAL),
    MOB_BAT_HURT("mob.bat.hurt", "entity.bat.hurt", SoundCategories1_8.NEUTRAL),
    MOB_GHAST_AFFECTIONATE_SCREAM("mob.ghast.affectionate_scream", "entity.ghast.scream", SoundCategories1_8.HOSTILE),
    MOB_GUARDIAN_ELDER_IDLE("mob.guardian.elder.idle", "entity.elder_guardian.ambient", SoundCategories1_8.HOSTILE),
    MOB_ZOMBIEPIG_ZPIGDEATH("mob.zombiepig.zpigdeath", "entity.zombie_pig.death", SoundCategories1_8.HOSTILE),
    AMBIENT_WEATHER_THUNDER("ambient.weather.thunder", "entity.lightning.thunder", SoundCategories1_8.WEATHER),
    MINECART_BASE("minecart.base", "entity.minecart.riding", SoundCategories1_8.NEUTRAL),
    STEP_LADDER("step.ladder", "block.ladder.step", SoundCategories1_8.NEUTRAL),
    MOB_HORSE_DONKEY_ANGRY("mob.horse.donkey.angry", "entity.donkey.angry", SoundCategories1_8.NEUTRAL),
    AMBIENT_CAVE_CAVE("ambient.cave.cave", "ambient.cave", SoundCategories1_8.AMBIENT),
    FIREWORKS_BLAST_FAR("fireworks.blast_far", "entity.firework.blast_far", SoundCategories1_8.AMBIENT),
    GAME_NEUTRAL_HURT_FALL_SMALL("game.neutral.hurt.fall.small", "entity.generic.small_fall", SoundCategories1_8.NEUTRAL),
    GAME_HOSTILE_SWIM_SPLASH("game.hostile.swim.splash", "entity.generic.splash", SoundCategories1_8.HOSTILE),
    RANDOM_DRINK("random.drink", "entity.generic.drink", SoundCategories1_8.PLAYER),
    GAME_HOSTILE_DIE("game.hostile.die", "entity.player.death", SoundCategories1_8.HOSTILE),
    MOB_CAT_HISS("mob.cat.hiss", "entity.cat.hiss", SoundCategories1_8.NEUTRAL),
    NOTE_BD("note.bd", "block.note.basedrum", SoundCategories1_8.RECORD),
    MOB_SPIDER_SAY("mob.spider.say", "entity.spider.ambient", SoundCategories1_8.HOSTILE),
    STEP_STONE("step.stone", "block.stone.step", SoundCategories1_8.NEUTRAL, true), //Is used for glass placement sound
    RANDOM_LEVELUP("random.levelup", "entity.player.levelup", SoundCategories1_8.PLAYER),
    LIQUID_LAVAPOP("liquid.lavapop", "block.lava.pop", SoundCategories1_8.BLOCK),
    MOB_SHEEP_SAY("mob.sheep.say", "entity.sheep.ambient", SoundCategories1_8.NEUTRAL),
    MOB_SKELETON_SAY("mob.skeleton.say", "entity.skeleton.ambient", SoundCategories1_8.HOSTILE),
    MOB_BLAZE_BREATHE("mob.blaze.breathe", "entity.blaze.ambient", SoundCategories1_8.HOSTILE),
    MOB_BAT_IDLE("mob.bat.idle", "entity.bat.ambient", SoundCategories1_8.NEUTRAL),
    MOB_MAGMACUBE_BIG("mob.magmacube.big", "entity.magmacube.squish", SoundCategories1_8.HOSTILE),
    MOB_HORSE_IDLE("mob.horse.idle", "entity.horse.ambient", SoundCategories1_8.NEUTRAL),
    GAME_HOSTILE_HURT_FALL_SMALL("game.hostile.hurt.fall.small", "entity.generic.small_fall", SoundCategories1_8.HOSTILE),
    MOB_HORSE_ZOMBIE_HIT("mob.horse.zombie.hit", "entity.zombie_horse.hurt", SoundCategories1_8.NEUTRAL),
    MOB_IRONGOLEM_THROW("mob.irongolem.throw", "entity.irongolem.attack", SoundCategories1_8.NEUTRAL),
    DIG_CLOTH("dig.cloth", "block.cloth.break", SoundCategories1_8.BLOCK),
    STEP_GRAVEL("step.gravel", "block.gravel.step", SoundCategories1_8.NEUTRAL),
    MOB_SILVERFISH_SAY("mob.silverfish.say", "entity.silverfish.ambient", SoundCategories1_8.HOSTILE),
    MOB_CAT_PURR("mob.cat.purr", "entity.cat.purr", SoundCategories1_8.NEUTRAL),
    MOB_ZOMBIE_INFECT("mob.zombie.infect", "entity.zombie.infect", SoundCategories1_8.HOSTILE),
    RANDOM_EAT("random.eat", "entity.generic.eat", SoundCategories1_8.PLAYER),
    MOB_WOLF_BARK("mob.wolf.bark", "entity.wolf.ambient", SoundCategories1_8.NEUTRAL),
    GAME_TNT_PRIMED("game.tnt.primed", "entity.creeper.primed", SoundCategories1_8.BLOCK),
    MOB_SHEEP_STEP("mob.sheep.step", "entity.sheep.step", SoundCategories1_8.NEUTRAL),
    MOB_ZOMBIE_DEATH("mob.zombie.death", "entity.zombie.death", SoundCategories1_8.HOSTILE),
    RANDOM_DOOR_OPEN("random.door_open", "block.wooden_door.open", SoundCategories1_8.BLOCK),
    MOB_ENDERMEN_PORTAL("mob.endermen.portal", "entity.endermen.teleport", SoundCategories1_8.HOSTILE),
    MOB_HORSE_ANGRY("mob.horse.angry", "entity.horse.angry", SoundCategories1_8.NEUTRAL),
    MOB_WOLF_GROWL("mob.wolf.growl", "entity.wolf.growl", SoundCategories1_8.NEUTRAL),
    DIG_SNOW("dig.snow", "block.snow.break", SoundCategories1_8.BLOCK),
    TILE_PISTON_OUT("tile.piston.out", "block.piston.extend", SoundCategories1_8.BLOCK),
    RANDOM_BURP("random.burp", "entity.player.burp", SoundCategories1_8.PLAYER),
    MOB_COW_STEP("mob.cow.step", "entity.cow.step", SoundCategories1_8.NEUTRAL),
    MOB_WITHER_HURT("mob.wither.hurt", "entity.wither.hurt", SoundCategories1_8.HOSTILE),
    MOB_GUARDIAN_LAND_DEATH("mob.guardian.land.death", "entity.elder_guardian.death_land", SoundCategories1_8.HOSTILE),
    MOB_CHICKEN_HURT("mob.chicken.hurt", "entity.chicken.hurt", SoundCategories1_8.NEUTRAL),
    MOB_WOLF_STEP("mob.wolf.step", "entity.wolf.step", SoundCategories1_8.NEUTRAL),
    MOB_WOLF_DEATH("mob.wolf.death", "entity.wolf.death", SoundCategories1_8.NEUTRAL),
    MOB_WOLF_WHINE("mob.wolf.whine", "entity.wolf.whine", SoundCategories1_8.NEUTRAL),
    NOTE_PLING("note.pling", "block.note.pling", SoundCategories1_8.RECORD),
    GAME_PLAYER_HURT_FALL_SMALL("game.player.hurt.fall.small", "entity.generic.small_fall", SoundCategories1_8.PLAYER),
    MOB_CAT_PURREOW("mob.cat.purreow", "entity.cat.purreow", SoundCategories1_8.NEUTRAL),
    FIREWORKS_LARGEBLAST_FAR("fireworks.largeBlast_far", "entity.firework.large_blast_far", SoundCategories1_8.AMBIENT),
    MOB_SKELETON_HURT("mob.skeleton.hurt", "entity.skeleton.hurt", SoundCategories1_8.HOSTILE),
    MOB_SPIDER_DEATH("mob.spider.death", "entity.spider.death", SoundCategories1_8.HOSTILE),
    RANDOM_ANVIL_BREAK("random.anvil_break", "block.anvil.destroy", SoundCategories1_8.BLOCK),
    MOB_WITHER_IDLE("mob.wither.idle", "entity.wither.ambient", SoundCategories1_8.HOSTILE),
    MOB_GUARDIAN_ELDER_HIT("mob.guardian.elder.hit", "entity.elder_guardian.hurt", SoundCategories1_8.HOSTILE),
    MOB_ENDERMEN_SCREAM("mob.endermen.scream", "entity.endermen.scream", SoundCategories1_8.HOSTILE),
    MOB_CAT_HITT("mob.cat.hitt", "entity.cat.hurt", SoundCategories1_8.NEUTRAL),
    MOB_MAGMACUBE_SMALL("mob.magmacube.small", "entity.small_magmacube.squish", SoundCategories1_8.HOSTILE),
    FIRE_IGNITE("fire.ignite", "item.flintandsteel.use", SoundCategories1_8.BLOCK, true),
    MOB_ENDERDRAGON_HIT("mob.enderdragon.hit", "entity.enderdragon.hurt", SoundCategories1_8.HOSTILE),
    MOB_ZOMBIE_HURT("mob.zombie.hurt", "entity.zombie.hurt", SoundCategories1_8.HOSTILE),
    RANDOM_EXPLODE("random.explode", "entity.generic.explode", SoundCategories1_8.BLOCK),
    MOB_SLIME_ATTACK("mob.slime.attack", "entity.slime.attack", SoundCategories1_8.HOSTILE),
    MOB_MAGMACUBE_JUMP("mob.magmacube.jump", "entity.magmacube.jump", SoundCategories1_8.HOSTILE),
    RANDOM_SPLASH("random.splash", "entity.bobber.splash", SoundCategories1_8.PLAYER),
    MOB_HORSE_SKELETON_HIT("mob.horse.skeleton.hit", "entity.skeleton_horse.hurt", SoundCategories1_8.NEUTRAL),
    MOB_GHAST_MOAN("mob.ghast.moan", "entity.ghast.ambient", SoundCategories1_8.HOSTILE),
    MOB_GUARDIAN_CURSE("mob.guardian.curse", "entity.elder_guardian.curse", SoundCategories1_8.HOSTILE),
    GAME_POTION_SMASH("game.potion.smash", "block.glass.break", SoundCategories1_8.NEUTRAL),
    NOTE_BASSATTACK("note.bassattack", "block.note.bass", SoundCategories1_8.RECORD),
    GUI_BUTTON_PRESS("gui.button.press", "block.wood_pressureplate.click_on", SoundCategories1_8.MASTER),
    RANDOM_ORB("random.orb", "entity.experience_orb.pickup", SoundCategories1_8.PLAYER),
    MOB_ZOMBIE_WOODBREAK("mob.zombie.woodbreak", "entity.zombie.break_door_wood", SoundCategories1_8.HOSTILE),
    MOB_HORSE_ARMOR("mob.horse.armor", "entity.horse.armor", SoundCategories1_8.NEUTRAL),
    TILE_PISTON_IN("tile.piston.in", "block.piston.contract", SoundCategories1_8.BLOCK),
    MOB_CAT_MEOW("mob.cat.meow", "entity.cat.ambient", SoundCategories1_8.NEUTRAL),
    MOB_PIG_STEP("mob.pig.step", "entity.pig.step", SoundCategories1_8.NEUTRAL),
    STEP_WOOD("step.wood", "block.wood.step", SoundCategories1_8.NEUTRAL),
    PORTAL_TRAVEL("portal.travel", "block.portal.travel", SoundCategories1_8.PLAYER),
    MOB_GUARDIAN_DEATH("mob.guardian.death", "entity.guardian.death", SoundCategories1_8.HOSTILE),
    MOB_SKELETON_DEATH("mob.skeleton.death", "entity.skeleton.death", SoundCategories1_8.HOSTILE),
    MOB_HORSE_HIT("mob.horse.hit", "entity.horse.hurt", SoundCategories1_8.NEUTRAL),
    MOB_VILLAGER_HIT("mob.villager.hit", "entity.villager.hurt", SoundCategories1_8.NEUTRAL),
    MOB_HORSE_SKELETON_IDLE("mob.horse.skeleton.idle", "entity.skeleton_horse.ambient", SoundCategories1_8.NEUTRAL),
    RECORDS_CHIRP("records.chirp", "record.chirp", SoundCategories1_8.RECORD),
    MOB_RABBIT_HURT("mob.rabbit.hurt", "entity.rabbit.hurt", SoundCategories1_8.NEUTRAL),
    RECORDS_STAL("records.stal", "record.stal", SoundCategories1_8.RECORD),
    MUSIC_GAME_NETHER("music.game.nether", "music.nether", SoundCategories1_8.MUSIC),
    MUSIC_MENU("music.menu", "music.menu", SoundCategories1_8.MUSIC),
    RECORDS_MELLOHI("records.mellohi", "record.mellohi", SoundCategories1_8.RECORD),
    RECORDS_CAT("records.cat", "record.cat", SoundCategories1_8.RECORD),
    RECORDS_FAR("records.far", "record.far", SoundCategories1_8.RECORD),
    MUSIC_GAME_END_DRAGON("music.game.end.dragon", "music.dragon", SoundCategories1_8.MUSIC),
    MOB_RABBIT_DEATH("mob.rabbit.death", "entity.rabbit.death", SoundCategories1_8.NEUTRAL),
    MOB_RABBIT_IDLE("mob.rabbit.idle", "entity.rabbit.ambient", SoundCategories1_8.NEUTRAL),
    MUSIC_GAME_END("music.game.end", "music.end", SoundCategories1_8.MUSIC),
    MUSIC_GAME("music.game", "music.game", SoundCategories1_8.MUSIC),
    MOB_GUARDIAN_IDLE("mob.guardian.idle", "entity.elder_guardian.ambient", SoundCategories1_8.HOSTILE),
    RECORDS_WARD("records.ward", "record.ward", SoundCategories1_8.RECORD),
    RECORDS_13("records.13", "record.13", SoundCategories1_8.RECORD),
    MOB_RABBIT_HOP("mob.rabbit.hop", "entity.rabbit.jump", SoundCategories1_8.NEUTRAL),
    RECORDS_STRAD("records.strad", "record.strad", SoundCategories1_8.RECORD),
    RECORDS_11("records.11", "record.11", SoundCategories1_8.RECORD),
    RECORDS_MALL("records.mall", "record.mall", SoundCategories1_8.RECORD),
    RECORDS_BLOCKS("records.blocks", "record.blocks", SoundCategories1_8.RECORD),
    RECORDS_WAIT("records.wait", "record.wait", SoundCategories1_8.RECORD),
    MUSIC_GAME_END_CREDITS("music.game.end.credits", "music.credits", SoundCategories1_8.MUSIC),
    MUSIC_GAME_CREATIVE("music.game.creative", "music.creative", SoundCategories1_8.MUSIC);

    private final String name;
    private final String newName;
    private final SoundCategories1_8 category;
    private final boolean breakSound;

    private static final Map<String, SoundEffectMappings1_9> effects;

    static {
        effects = new HashMap<>();
        for (SoundEffectMappings1_9 e : SoundEffectMappings1_9.values()) {
            effects.put(e.getName(), e);
        }
    }

    SoundEffectMappings1_9(String name, String newName, SoundCategories1_8 category) {
        this.category = category;
        this.newName = newName;
        this.name = name;
        this.breakSound = name.startsWith("dig.");
    }

    SoundEffectMappings1_9(String name, String newName, SoundCategories1_8 category, boolean shouldIgnore) {
        this.category = category;
        this.newName = newName;
        this.name = name;
        this.breakSound = name.startsWith("dig.") || shouldIgnore;
    }

    public static SoundEffectMappings1_9 getByName(String name) {
        return effects.get(name);
    }

    public String getName() {
        return name;
    }

    public String getNewName() {
        return newName;
    }

    public SoundCategories1_8 getCategory() {
        return category;
    }

    public boolean isBreakSound() {
        return breakSound;
    }
}
