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
package com.viaversion.viaversion.protocols.protocol1_9to1_8.sounds;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum SoundEffect {

    MOB_HORSE_ZOMBIE_IDLE("mob.horse.zombie.idle", "entity.zombie_horse.ambient", SoundCategory.NEUTRAL),
    NOTE_SNARE("note.snare", "block.note.snare", SoundCategory.RECORD),
    RANDOM_WOOD_CLICK("random.wood_click", "block.wood_button.click_on", SoundCategory.BLOCK),
    DIG_GRAVEL("dig.gravel", "block.gravel.place", SoundCategory.BLOCK),
    RANDOM_BOWHIT("random.bowhit", "block.tripwire.detach", SoundCategory.NEUTRAL),
    DIG_GLASS("dig.glass", "block.glass.break", SoundCategory.BLOCK),
    MOB_ZOMBIE_SAY("mob.zombie.say", "entity.zombie.ambient", SoundCategory.HOSTILE),
    MOB_PIG_DEATH("mob.pig.death", "entity.pig.death", SoundCategory.NEUTRAL),
    MOB_HORSE_DONKEY_HIT("mob.horse.donkey.hit", "entity.donkey.hurt", SoundCategory.NEUTRAL),
    GAME_NEUTRAL_SWIM("game.neutral.swim", "entity.player.swim", SoundCategory.NEUTRAL),
    GAME_PLAYER_SWIM("game.player.swim", "entity.player.swim", SoundCategory.PLAYER),
    MOB_ENDERMEN_IDLE("mob.endermen.idle", "entity.endermen.ambient", SoundCategory.HOSTILE),
    PORTAL_PORTAL("portal.portal", "block.portal.ambient", SoundCategory.BLOCK),
    RANDOM_FIZZ("random.fizz", "entity.generic.extinguish_fire", SoundCategory.BLOCK),
    NOTE_HARP("note.harp", "block.note.harp", SoundCategory.RECORD),
    STEP_SNOW("step.snow", "block.snow.step", SoundCategory.NEUTRAL),
    RANDOM_SUCCESSFUL_HIT("random.successful_hit", "entity.arrow.hit_player", SoundCategory.PLAYER),
    MOB_ZOMBIEPIG_ZPIGHURT("mob.zombiepig.zpighurt", "entity.zombie_pig.hurt", SoundCategory.HOSTILE),
    MOB_WOLF_HOWL("mob.wolf.howl", "entity.wolf.howl", SoundCategory.NEUTRAL),
    FIREWORKS_LAUNCH("fireworks.launch", "entity.firework.launch", SoundCategory.AMBIENT),
    MOB_COW_HURT("mob.cow.hurt", "entity.cow.death", SoundCategory.NEUTRAL),
    FIREWORKS_LARGEBLAST("fireworks.largeBlast", "entity.firework.large_blast", SoundCategory.AMBIENT),
    MOB_BLAZE_HIT("mob.blaze.hit", "entity.blaze.hurt", SoundCategory.HOSTILE),
    MOB_VILLAGER_DEATH("mob.villager.death", "entity.villager.death", SoundCategory.NEUTRAL),
    MOB_BLAZE_DEATH("mob.blaze.death", "entity.blaze.death", SoundCategory.HOSTILE),
    MOB_HORSE_ZOMBIE_DEATH("mob.horse.zombie.death", "entity.zombie_horse.death", SoundCategory.NEUTRAL),
    MOB_SILVERFISH_KILL("mob.silverfish.kill", "entity.endermite.death", SoundCategory.HOSTILE),
    MOB_WOLF_PANTING("mob.wolf.panting", "entity.wolf.pant", SoundCategory.NEUTRAL),
    NOTE_BASS("note.bass", "block.note.bass", SoundCategory.RECORD),
    DIG_STONE("dig.stone", "block.glass.place", SoundCategory.BLOCK),
    MOB_ENDERMEN_STARE("mob.endermen.stare", "entity.endermen.stare", SoundCategory.HOSTILE),
    GAME_PLAYER_SWIM_SPLASH("game.player.swim.splash", "entity.generic.splash", SoundCategory.BLOCK),
    MOB_SLIME_SMALL("mob.slime.small", "block.slime.hit", SoundCategory.HOSTILE),
    MOB_GHAST_DEATH("mob.ghast.death", "entity.ghast.death", SoundCategory.HOSTILE),
    MOB_GUARDIAN_ATTACK("mob.guardian.attack", "entity.guardian.attack", SoundCategory.HOSTILE),
    RANDOM_CLICK("random.click", "block.wood_pressureplate.click_on", SoundCategory.BLOCK),
    MOB_ZOMBIEPIG_ZPIG("mob.zombiepig.zpig", "entity.zombie_pig.ambient", SoundCategory.HOSTILE),
    GAME_PLAYER_DIE("game.player.die", "entity.player.death", SoundCategory.PLAYER),
    FIREWORKS_TWINKLE_FAR("fireworks.twinkle_far", "entity.firework.twinkle_far", SoundCategory.AMBIENT),
    MOB_GUARDIAN_LAND_IDLE("mob.guardian.land.idle", "entity.guardian.ambient_land", SoundCategory.HOSTILE),
    DIG_GRASS("dig.grass", "block.grass.place", SoundCategory.BLOCK),
    MOB_SKELETON_STEP("mob.skeleton.step", "entity.skeleton.step", SoundCategory.HOSTILE),
    MOB_WITHER_DEATH("mob.wither.death", "entity.wither.death", SoundCategory.HOSTILE),
    MOB_WOLF_HURT("mob.wolf.hurt", "entity.wolf.hurt", SoundCategory.NEUTRAL),
    MOB_HORSE_LEATHER("mob.horse.leather", "entity.horse.saddle", SoundCategory.NEUTRAL),
    MOB_BAT_LOOP("mob.bat.loop", "entity.bat.loop", SoundCategory.NEUTRAL),
    MOB_GHAST_SCREAM("mob.ghast.scream", "entity.ghast.hurt", SoundCategory.HOSTILE),
    GAME_PLAYER_HURT("game.player.hurt", "entity.player.death", SoundCategory.PLAYER),
    GAME_NEUTRAL_DIE("game.neutral.die", "entity.player.death", SoundCategory.NEUTRAL),
    MOB_CREEPER_DEATH("mob.creeper.death", "entity.creeper.death", SoundCategory.HOSTILE),
    MOB_HORSE_GALLOP("mob.horse.gallop", "entity.horse.gallop", SoundCategory.NEUTRAL),
    MOB_WITHER_SPAWN("mob.wither.spawn", "entity.wither.spawn", SoundCategory.HOSTILE),
    MOB_ENDERMEN_HIT("mob.endermen.hit", "entity.endermen.hurt", SoundCategory.HOSTILE),
    MOB_CREEPER_SAY("mob.creeper.say", "entity.creeper.hurt", SoundCategory.HOSTILE),
    MOB_HORSE_WOOD("mob.horse.wood", "entity.horse.step_wood", SoundCategory.NEUTRAL),
    MOB_ZOMBIE_UNFECT("mob.zombie.unfect", "entity.zombie_villager.converted", SoundCategory.HOSTILE),
    RANDOM_ANVIL_USE("random.anvil_use", "block.anvil.use", SoundCategory.BLOCK),
    RANDOM_CHESTCLOSED("random.chestclosed", "block.chest.close", SoundCategory.BLOCK),
    MOB_SHEEP_SHEAR("mob.sheep.shear", "entity.sheep.shear", SoundCategory.NEUTRAL),
    RANDOM_POP("random.pop", "entity.item.pickup", SoundCategory.PLAYER),
    MOB_BAT_DEATH("mob.bat.death", "entity.bat.death", SoundCategory.NEUTRAL),
    DIG_WOOD("dig.wood", "block.ladder.break", SoundCategory.BLOCK),
    MOB_HORSE_DONKEY_DEATH("mob.horse.donkey.death", "entity.donkey.death", SoundCategory.NEUTRAL),
    FIREWORKS_BLAST("fireworks.blast", "entity.firework.blast", SoundCategory.AMBIENT),
    MOB_ZOMBIEPIG_ZPIGANGRY("mob.zombiepig.zpigangry", "entity.zombie_pig.angry", SoundCategory.HOSTILE),
    GAME_HOSTILE_SWIM("game.hostile.swim", "entity.player.swim", SoundCategory.HOSTILE),
    MOB_GUARDIAN_FLOP("mob.guardian.flop", "entity.guardian.flop", SoundCategory.HOSTILE),
    MOB_VILLAGER_YES("mob.villager.yes", "entity.villager.yes", SoundCategory.NEUTRAL),
    MOB_GHAST_CHARGE("mob.ghast.charge", "entity.ghast.warn", SoundCategory.HOSTILE),
    CREEPER_PRIMED("creeper.primed", "entity.creeper.primed", SoundCategory.HOSTILE),
    DIG_SAND("dig.sand", "block.sand.break", SoundCategory.BLOCK),
    MOB_CHICKEN_SAY("mob.chicken.say", "entity.chicken.ambient", SoundCategory.NEUTRAL),
    RANDOM_DOOR_CLOSE("random.door_close", "block.wooden_door.close", SoundCategory.BLOCK),
    MOB_GUARDIAN_ELDER_DEATH("mob.guardian.elder.death", "entity.elder_guardian.death", SoundCategory.HOSTILE),
    FIREWORKS_TWINKLE("fireworks.twinkle", "entity.firework.twinkle", SoundCategory.AMBIENT),
    MOB_HORSE_SKELETON_DEATH("mob.horse.skeleton.death", "entity.skeleton_horse.death", SoundCategory.NEUTRAL),
    AMBIENT_WEATHER_RAIN("ambient.weather.rain", "weather.rain.above", SoundCategory.WEATHER),
    PORTAL_TRIGGER("portal.trigger", "block.portal.trigger", SoundCategory.BLOCK),
    RANDOM_CHESTOPEN("random.chestopen", "block.chest.open", SoundCategory.BLOCK),
    MOB_HORSE_LAND("mob.horse.land", "entity.horse.land", SoundCategory.NEUTRAL),
    MOB_SILVERFISH_STEP("mob.silverfish.step", "entity.silverfish.step", SoundCategory.HOSTILE),
    MOB_BAT_TAKEOFF("mob.bat.takeoff", "entity.bat.takeoff", SoundCategory.NEUTRAL),
    MOB_VILLAGER_NO("mob.villager.no", "entity.villager.no", SoundCategory.NEUTRAL),
    GAME_HOSTILE_HURT_FALL_BIG("game.hostile.hurt.fall.big", "entity.hostile.big_fall", SoundCategory.HOSTILE),
    MOB_IRONGOLEM_WALK("mob.irongolem.walk", "entity.irongolem.step", SoundCategory.NEUTRAL),
    NOTE_HAT("note.hat", "block.note.hat", SoundCategory.RECORD),
    MOB_ZOMBIE_METAL("mob.zombie.metal", "entity.zombie.attack_iron_door", SoundCategory.HOSTILE),
    MOB_VILLAGER_HAGGLE("mob.villager.haggle", "entity.villager.trading", SoundCategory.NEUTRAL),
    MOB_GHAST_FIREBALL("mob.ghast.fireball", "entity.blaze.shoot", SoundCategory.HOSTILE),
    MOB_IRONGOLEM_DEATH("mob.irongolem.death", "entity.irongolem.death", SoundCategory.NEUTRAL),
    RANDOM_BREAK("random.break", "item.shield.break", SoundCategory.PLAYER),
    MOB_ZOMBIE_REMEDY("mob.zombie.remedy", "entity.zombie_villager.cure", SoundCategory.HOSTILE),
    RANDOM_BOW("random.bow", "entity.splash_potion.throw", SoundCategory.NEUTRAL),
    MOB_VILLAGER_IDLE("mob.villager.idle", "entity.villager.ambient", SoundCategory.NEUTRAL),
    STEP_CLOTH("step.cloth", "block.cloth.fall", SoundCategory.NEUTRAL),
    MOB_SILVERFISH_HIT("mob.silverfish.hit", "entity.endermite.hurt", SoundCategory.HOSTILE),
    LIQUID_LAVA("liquid.lava", "block.lava.ambient", SoundCategory.BLOCK),
    GAME_NEUTRAL_HURT_FALL_BIG("game.neutral.hurt.fall.big", "entity.hostile.big_fall", SoundCategory.NEUTRAL),
    FIRE_FIRE("fire.fire", "block.fire.ambient", SoundCategory.BLOCK),
    MOB_ZOMBIE_WOOD("mob.zombie.wood", "entity.zombie.attack_door_wood", SoundCategory.HOSTILE),
    MOB_CHICKEN_STEP("mob.chicken.step", "entity.chicken.step", SoundCategory.NEUTRAL),
    MOB_GUARDIAN_LAND_HIT("mob.guardian.land.hit", "entity.guardian.hurt_land", SoundCategory.HOSTILE),
    MOB_CHICKEN_PLOP("mob.chicken.plop", "entity.donkey.chest", SoundCategory.NEUTRAL),
    MOB_ENDERDRAGON_WINGS("mob.enderdragon.wings", "entity.enderdragon.flap", SoundCategory.HOSTILE),
    STEP_GRASS("step.grass", "block.grass.hit", SoundCategory.NEUTRAL),
    MOB_HORSE_BREATHE("mob.horse.breathe", "entity.horse.breathe", SoundCategory.NEUTRAL),
    GAME_PLAYER_HURT_FALL_BIG("game.player.hurt.fall.big", "entity.hostile.big_fall", SoundCategory.PLAYER),
    MOB_HORSE_DONKEY_IDLE("mob.horse.donkey.idle", "entity.donkey.ambient", SoundCategory.NEUTRAL),
    MOB_SPIDER_STEP("mob.spider.step", "entity.spider.step", SoundCategory.HOSTILE),
    GAME_NEUTRAL_HURT("game.neutral.hurt", "entity.player.death", SoundCategory.NEUTRAL),
    MOB_COW_SAY("mob.cow.say", "entity.cow.ambient", SoundCategory.NEUTRAL),
    MOB_HORSE_JUMP("mob.horse.jump", "entity.horse.jump", SoundCategory.NEUTRAL),
    MOB_HORSE_SOFT("mob.horse.soft", "entity.horse.step", SoundCategory.NEUTRAL),
    GAME_NEUTRAL_SWIM_SPLASH("game.neutral.swim.splash", "entity.generic.splash", SoundCategory.NEUTRAL),
    MOB_GUARDIAN_HIT("mob.guardian.hit", "entity.guardian.hurt", SoundCategory.HOSTILE),
    MOB_ENDERDRAGON_END("mob.enderdragon.end", "entity.enderdragon.death", SoundCategory.HOSTILE),
    MOB_ZOMBIE_STEP("mob.zombie.step", "entity.zombie.step", SoundCategory.HOSTILE),
    MOB_ENDERDRAGON_GROWL("mob.enderdragon.growl", "entity.enderdragon.growl", SoundCategory.HOSTILE),
    MOB_WOLF_SHAKE("mob.wolf.shake", "entity.wolf.shake", SoundCategory.NEUTRAL),
    MOB_ENDERMEN_DEATH("mob.endermen.death", "entity.endermen.death", SoundCategory.HOSTILE),
    RANDOM_ANVIL_LAND("random.anvil_land", "block.anvil.land", SoundCategory.BLOCK),
    GAME_HOSTILE_HURT("game.hostile.hurt", "entity.player.death", SoundCategory.HOSTILE),
    MINECART_INSIDE("minecart.inside", "entity.minecart.inside", SoundCategory.PLAYER),
    MOB_SLIME_BIG("mob.slime.big", "entity.slime.death", SoundCategory.HOSTILE),
    LIQUID_WATER("liquid.water", "block.water.ambient", SoundCategory.BLOCK),
    MOB_PIG_SAY("mob.pig.say", "entity.pig.ambient", SoundCategory.NEUTRAL),
    MOB_WITHER_SHOOT("mob.wither.shoot", "entity.wither.shoot", SoundCategory.HOSTILE),
    ITEM_FIRECHARGE_USE("item.fireCharge.use", "entity.blaze.shoot", SoundCategory.BLOCK),
    STEP_SAND("step.sand", "block.sand.fall", SoundCategory.NEUTRAL),
    MOB_IRONGOLEM_HIT("mob.irongolem.hit", "entity.irongolem.hurt", SoundCategory.NEUTRAL),
    MOB_HORSE_DEATH("mob.horse.death", "entity.horse.death", SoundCategory.NEUTRAL),
    MOB_BAT_HURT("mob.bat.hurt", "entity.bat.hurt", SoundCategory.NEUTRAL),
    MOB_GHAST_AFFECTIONATE_SCREAM("mob.ghast.affectionate_scream", "entity.ghast.scream", SoundCategory.HOSTILE),
    MOB_GUARDIAN_ELDER_IDLE("mob.guardian.elder.idle", "entity.elder_guardian.ambient", SoundCategory.HOSTILE),
    MOB_ZOMBIEPIG_ZPIGDEATH("mob.zombiepig.zpigdeath", "entity.zombie_pig.death", SoundCategory.HOSTILE),
    AMBIENT_WEATHER_THUNDER("ambient.weather.thunder", "entity.lightning.thunder", SoundCategory.WEATHER),
    MINECART_BASE("minecart.base", "entity.minecart.riding", SoundCategory.NEUTRAL),
    STEP_LADDER("step.ladder", "block.ladder.hit", SoundCategory.NEUTRAL),
    MOB_HORSE_DONKEY_ANGRY("mob.horse.donkey.angry", "entity.donkey.angry", SoundCategory.NEUTRAL),
    AMBIENT_CAVE_CAVE("ambient.cave.cave", "ambient.cave", SoundCategory.AMBIENT),
    FIREWORKS_BLAST_FAR("fireworks.blast_far", "entity.firework.blast_far", SoundCategory.AMBIENT),
    GAME_NEUTRAL_HURT_FALL_SMALL("game.neutral.hurt.fall.small", "entity.generic.small_fall", SoundCategory.NEUTRAL),
    GAME_HOSTILE_SWIM_SPLASH("game.hostile.swim.splash", "entity.generic.splash", SoundCategory.HOSTILE),
    RANDOM_DRINK("random.drink", "entity.generic.drink", SoundCategory.PLAYER),
    GAME_HOSTILE_DIE("game.hostile.die", "entity.player.death", SoundCategory.HOSTILE),
    MOB_CAT_HISS("mob.cat.hiss", "entity.cat.hiss", SoundCategory.NEUTRAL),
    NOTE_BD("note.bd", "block.note.basedrum", SoundCategory.RECORD),
    MOB_SPIDER_SAY("mob.spider.say", "entity.spider.hurt", SoundCategory.HOSTILE),
    STEP_STONE("step.stone", "block.anvil.hit", SoundCategory.NEUTRAL, true), //Is used for glass placement sound
    RANDOM_LEVELUP("random.levelup", "entity.player.levelup", SoundCategory.PLAYER),
    LIQUID_LAVAPOP("liquid.lavapop", "block.lava.pop", SoundCategory.BLOCK),
    MOB_SHEEP_SAY("mob.sheep.say", "entity.sheep.ambient", SoundCategory.NEUTRAL),
    MOB_SKELETON_SAY("mob.skeleton.say", "entity.skeleton.ambient", SoundCategory.HOSTILE),
    MOB_BLAZE_BREATHE("mob.blaze.breathe", "entity.blaze.ambient", SoundCategory.HOSTILE),
    MOB_BAT_IDLE("mob.bat.idle", "entity.bat.ambient", SoundCategory.NEUTRAL),
    MOB_MAGMACUBE_BIG("mob.magmacube.big", "entity.magmacube.squish", SoundCategory.HOSTILE),
    MOB_HORSE_IDLE("mob.horse.idle", "entity.horse.ambient", SoundCategory.NEUTRAL),
    GAME_HOSTILE_HURT_FALL_SMALL("game.hostile.hurt.fall.small", "entity.generic.small_fall", SoundCategory.HOSTILE),
    MOB_HORSE_ZOMBIE_HIT("mob.horse.zombie.hit", "entity.zombie_horse.hurt", SoundCategory.NEUTRAL),
    MOB_IRONGOLEM_THROW("mob.irongolem.throw", "entity.irongolem.attack", SoundCategory.NEUTRAL),
    DIG_CLOTH("dig.cloth", "block.cloth.place", SoundCategory.BLOCK),
    STEP_GRAVEL("step.gravel", "block.gravel.hit", SoundCategory.NEUTRAL),
    MOB_SILVERFISH_SAY("mob.silverfish.say", "entity.silverfish.ambient", SoundCategory.HOSTILE),
    MOB_CAT_PURR("mob.cat.purr", "entity.cat.purr", SoundCategory.NEUTRAL),
    MOB_ZOMBIE_INFECT("mob.zombie.infect", "entity.zombie.infect", SoundCategory.HOSTILE),
    RANDOM_EAT("random.eat", "entity.generic.eat", SoundCategory.PLAYER),
    MOB_WOLF_BARK("mob.wolf.bark", "entity.wolf.ambient", SoundCategory.NEUTRAL),
    GAME_TNT_PRIMED("game.tnt.primed", "entity.creeper.primed", SoundCategory.BLOCK),
    MOB_SHEEP_STEP("mob.sheep.step", "entity.sheep.step", SoundCategory.NEUTRAL),
    MOB_ZOMBIE_DEATH("mob.zombie.death", "entity.zombie.death", SoundCategory.HOSTILE),
    RANDOM_DOOR_OPEN("random.door_open", "block.wooden_door.open", SoundCategory.BLOCK),
    MOB_ENDERMEN_PORTAL("mob.endermen.portal", "entity.endermen.teleport", SoundCategory.HOSTILE),
    MOB_HORSE_ANGRY("mob.horse.angry", "entity.horse.angry", SoundCategory.NEUTRAL),
    MOB_WOLF_GROWL("mob.wolf.growl", "entity.wolf.growl", SoundCategory.NEUTRAL),
    DIG_SNOW("dig.snow", "block.snow.place", SoundCategory.BLOCK),
    TILE_PISTON_OUT("tile.piston.out", "block.piston.extend", SoundCategory.BLOCK),
    RANDOM_BURP("random.burp", "entity.player.burp", SoundCategory.PLAYER),
    MOB_COW_STEP("mob.cow.step", "entity.cow.step", SoundCategory.NEUTRAL),
    MOB_WITHER_HURT("mob.wither.hurt", "entity.wither.hurt", SoundCategory.HOSTILE),
    MOB_GUARDIAN_LAND_DEATH("mob.guardian.land.death", "entity.elder_guardian.death_land", SoundCategory.HOSTILE),
    MOB_CHICKEN_HURT("mob.chicken.hurt", "entity.chicken.death", SoundCategory.NEUTRAL),
    MOB_WOLF_STEP("mob.wolf.step", "entity.wolf.step", SoundCategory.NEUTRAL),
    MOB_WOLF_DEATH("mob.wolf.death", "entity.wolf.death", SoundCategory.NEUTRAL),
    MOB_WOLF_WHINE("mob.wolf.whine", "entity.wolf.whine", SoundCategory.NEUTRAL),
    NOTE_PLING("note.pling", "block.note.pling", SoundCategory.RECORD),
    GAME_PLAYER_HURT_FALL_SMALL("game.player.hurt.fall.small", "entity.generic.small_fall", SoundCategory.PLAYER),
    MOB_CAT_PURREOW("mob.cat.purreow", "entity.cat.purreow", SoundCategory.NEUTRAL),
    FIREWORKS_LARGEBLAST_FAR("fireworks.largeBlast_far", "entity.firework.large_blast_far", SoundCategory.AMBIENT),
    MOB_SKELETON_HURT("mob.skeleton.hurt", "entity.skeleton.hurt", SoundCategory.HOSTILE),
    MOB_SPIDER_DEATH("mob.spider.death", "entity.spider.death", SoundCategory.HOSTILE),
    RANDOM_ANVIL_BREAK("random.anvil_break", "block.anvil.destroy", SoundCategory.BLOCK),
    MOB_WITHER_IDLE("mob.wither.idle", "entity.wither.ambient", SoundCategory.HOSTILE),
    MOB_GUARDIAN_ELDER_HIT("mob.guardian.elder.hit", "entity.elder_guardian.hurt", SoundCategory.HOSTILE),
    MOB_ENDERMEN_SCREAM("mob.endermen.scream", "entity.endermen.scream", SoundCategory.HOSTILE),
    MOB_CAT_HITT("mob.cat.hitt", "entity.cat.hurt", SoundCategory.NEUTRAL),
    MOB_MAGMACUBE_SMALL("mob.magmacube.small", "entity.small_magmacube.squish", SoundCategory.HOSTILE),
    FIRE_IGNITE("fire.ignite", "item.flintandsteel.use", SoundCategory.BLOCK, true),
    MOB_ENDERDRAGON_HIT("mob.enderdragon.hit", "entity.enderdragon.hurt", SoundCategory.HOSTILE),
    MOB_ZOMBIE_HURT("mob.zombie.hurt", "entity.zombie.hurt", SoundCategory.HOSTILE),
    RANDOM_EXPLODE("random.explode", "block.end_gateway.spawn", SoundCategory.BLOCK),
    MOB_SLIME_ATTACK("mob.slime.attack", "entity.slime.attack", SoundCategory.HOSTILE),
    MOB_MAGMACUBE_JUMP("mob.magmacube.jump", "entity.magmacube.jump", SoundCategory.HOSTILE),
    RANDOM_SPLASH("random.splash", "entity.bobber.splash", SoundCategory.PLAYER),
    MOB_HORSE_SKELETON_HIT("mob.horse.skeleton.hit", "entity.skeleton_horse.hurt", SoundCategory.NEUTRAL),
    MOB_GHAST_MOAN("mob.ghast.moan", "entity.ghast.ambient", SoundCategory.HOSTILE),
    MOB_GUARDIAN_CURSE("mob.guardian.curse", "entity.elder_guardian.curse", SoundCategory.HOSTILE),
    GAME_POTION_SMASH("game.potion.smash", "block.glass.break", SoundCategory.NEUTRAL),
    NOTE_BASSATTACK("note.bassattack", "block.note.bass", SoundCategory.RECORD),
    GUI_BUTTON_PRESS("gui.button.press", "block.wood_pressureplate.click_on", SoundCategory.MASTER),
    RANDOM_ORB("random.orb", "entity.experience_orb.pickup", SoundCategory.PLAYER),
    MOB_ZOMBIE_WOODBREAK("mob.zombie.woodbreak", "entity.zombie.break_door_wood", SoundCategory.HOSTILE),
    MOB_HORSE_ARMOR("mob.horse.armor", "entity.horse.armor", SoundCategory.NEUTRAL),
    TILE_PISTON_IN("tile.piston.in", "block.piston.contract", SoundCategory.BLOCK),
    MOB_CAT_MEOW("mob.cat.meow", "entity.cat.ambient", SoundCategory.NEUTRAL),
    MOB_PIG_STEP("mob.pig.step", "entity.pig.step", SoundCategory.NEUTRAL),
    STEP_WOOD("step.wood", "block.wood.step", SoundCategory.NEUTRAL),
    PORTAL_TRAVEL("portal.travel", "block.portal.travel", SoundCategory.PLAYER),
    MOB_GUARDIAN_DEATH("mob.guardian.death", "entity.guardian.death", SoundCategory.HOSTILE),
    MOB_SKELETON_DEATH("mob.skeleton.death", "entity.skeleton.death", SoundCategory.HOSTILE),
    MOB_HORSE_HIT("mob.horse.hit", "entity.horse.hurt", SoundCategory.NEUTRAL),
    MOB_VILLAGER_HIT("mob.villager.hit", "entity.villager.hurt", SoundCategory.NEUTRAL),
    MOB_HORSE_SKELETON_IDLE("mob.horse.skeleton.idle", "entity.skeleton_horse.ambient", SoundCategory.NEUTRAL),
    RECORDS_CHIRP("records.chirp", "record.chirp", SoundCategory.RECORD),
    MOB_RABBIT_HURT("mob.rabbit.hurt", "entity.rabbit.hurt", SoundCategory.NEUTRAL),
    RECORDS_STAL("records.stal", "record.stal", SoundCategory.RECORD),
    MUSIC_GAME_NETHER("music.game.nether", "music.nether", SoundCategory.MUSIC),
    MUSIC_MENU("music.menu", "music.menu", SoundCategory.MUSIC),
    RECORDS_MELLOHI("records.mellohi", "record.mellohi", SoundCategory.RECORD),
    RECORDS_CAT("records.cat", "record.cat", SoundCategory.RECORD),
    RECORDS_FAR("records.far", "record.far", SoundCategory.RECORD),
    MUSIC_GAME_END_DRAGON("music.game.end.dragon", "music.dragon", SoundCategory.MUSIC),
    MOB_RABBIT_DEATH("mob.rabbit.death", "entity.rabbit.death", SoundCategory.NEUTRAL),
    MOB_RABBIT_IDLE("mob.rabbit.idle", "entity.rabbit.ambient", SoundCategory.NEUTRAL),
    MUSIC_GAME_END("music.game.end", "music.end", SoundCategory.MUSIC),
    MUSIC_GAME("music.game", "music.game", SoundCategory.MUSIC),
    MOB_GUARDIAN_IDLE("mob.guardian.idle", "entity.elder_guardian.ambient", SoundCategory.HOSTILE),
    RECORDS_WARD("records.ward", "record.ward", SoundCategory.RECORD),
    RECORDS_13("records.13", "record.13", SoundCategory.RECORD),
    MOB_RABBIT_HOP("mob.rabbit.hop", "entity.rabbit.jump", SoundCategory.NEUTRAL),
    RECORDS_STRAD("records.strad", "record.strad", SoundCategory.RECORD),
    RECORDS_11("records.11", "record.11", SoundCategory.RECORD),
    RECORDS_MALL("records.mall", "record.mall", SoundCategory.RECORD),
    RECORDS_BLOCKS("records.blocks", "record.blocks", SoundCategory.RECORD),
    RECORDS_WAIT("records.wait", "record.wait", SoundCategory.RECORD),
    MUSIC_GAME_END_CREDITS("music.game.end.credits", "music.credits", SoundCategory.MUSIC),
    MUSIC_GAME_CREATIVE("music.game.creative", "music.creative", SoundCategory.MUSIC);

    private final String name;
    private final String newName;
    private final SoundCategory category;
    private final boolean breakSound;

    private static final Map<String, SoundEffect> effects;

    static {
        effects = new HashMap<>();
        for (SoundEffect e : SoundEffect.values()) {
            effects.put(e.getName(), e);
        }
    }

    SoundEffect(String name, String newName, SoundCategory category) {
        this.category = category;
        this.newName = newName;
        this.name = name;
        this.breakSound = name.startsWith("dig.");
    }

    SoundEffect(String name, String newName, SoundCategory category, boolean shouldIgnore) {
        this.category = category;
        this.newName = newName;
        this.name = name;
        this.breakSound = name.startsWith("dig.") || shouldIgnore;
    }

    public static SoundEffect getByName(String name) {
        return effects.get(name);
    }

    public String getName() {
        return name;
    }

    public String getNewName() {
        return newName;
    }

    public SoundCategory getCategory() {
        return category;
    }

    public boolean isBreakSound() {
        return breakSound;
    }
}
