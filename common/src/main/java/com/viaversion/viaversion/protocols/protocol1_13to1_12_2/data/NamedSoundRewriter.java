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
import java.util.Locale;
import java.util.Map;

public class NamedSoundRewriter {
    private static final Map<String, String> oldToNew = new HashMap<>();

    static {
        // Extracted from Minecraft Wiki
        oldToNew.put("block.cloth.break", "block.wool.break");
        oldToNew.put("block.cloth.fall", "block.wool.fall");
        oldToNew.put("block.cloth.hit", "block.wool.hit");
        oldToNew.put("block.cloth.place", "block.wool.place");
        oldToNew.put("block.cloth.step", "block.wool.step");
        oldToNew.put("block.enderchest.close", "block.ender_chest.close");
        oldToNew.put("block.enderchest.open", "block.ender_chest.open");
        oldToNew.put("block.metal_pressureplate.click_off", "block.metal_pressure_plate.click_off");
        oldToNew.put("block.metal_pressureplate.click_on", "block.metal_pressure_plate.click_on");
        oldToNew.put("block.note.basedrum", "block.note_block.basedrum");
        oldToNew.put("block.note.bass", "block.note_block.bass");
        oldToNew.put("block.note.bell", "block.note_block.bell");
        oldToNew.put("block.note.chime", "block.note_block.chime");
        oldToNew.put("block.note.flute", "block.note_block.flute");
        oldToNew.put("block.note.guitar", "block.note_block.guitar");
        oldToNew.put("block.note.harp", "block.note_block.harp");
        oldToNew.put("block.note.hat", "block.note_block.hat");
        oldToNew.put("block.note.pling", "block.note_block.pling");
        oldToNew.put("block.note.snare", "block.note_block.snare");
        oldToNew.put("block.note.xylophone", "block.note_block.xylophone");
        oldToNew.put("block.slime.break", "block.slime_block.break");
        oldToNew.put("block.slime.fall", "block.slime_block.fall");
        oldToNew.put("block.slime.hit", "block.slime_block.hit");
        oldToNew.put("block.slime.place", "block.slime_block.place");
        oldToNew.put("block.slime.step", "block.slime_block.step");
        oldToNew.put("block.stone_pressureplate.click_off", "block.stone_pressure_plate.click_off");
        oldToNew.put("block.stone_pressureplate.click_on", "block.stone_pressure_plate.click_on");
        oldToNew.put("block.waterlily.place", "block.lily_pad.place");
        oldToNew.put("block.wood_pressureplate.click_off", "block.wooden_pressure_plate.click_off");
        oldToNew.put("block.wood_button.click_on", "block.wooden_button.click_on");
        oldToNew.put("block.wood_button.click_off", "block.wooden_button.click_off");
        oldToNew.put("block.wood_pressureplate.click_on", "block.wooden_pressure_plate.click_on");
        oldToNew.put("entity.armorstand.break", "entity.armor_stand.break");
        oldToNew.put("entity.armorstand.fall", "entity.armor_stand.fall");
        oldToNew.put("entity.armorstand.hit", "entity.armor_stand.hit");
        oldToNew.put("entity.armorstand.place", "entity.armor_stand.place");
        oldToNew.put("entity.bobber.retrieve", "entity.fishing_bobber.retrieve");
        oldToNew.put("entity.bobber.splash", "entity.fishing_bobber.splash");
        oldToNew.put("entity.bobber.throw", "entity.fishing_bobber.throw");
        oldToNew.put("entity.enderdragon.ambient", "entity.ender_dragon.ambient");
        oldToNew.put("entity.enderdragon.death", "entity.ender_dragon.death");
        oldToNew.put("entity.enderdragon.flap", "entity.ender_dragon.flap");
        oldToNew.put("entity.enderdragon.growl", "entity.ender_dragon.growl");
        oldToNew.put("entity.enderdragon.hurt", "entity.ender_dragon.hurt");
        oldToNew.put("entity.enderdragon.shoot", "entity.ender_dragon.shoot");
        oldToNew.put("entity.enderdragon_fireball.explode", "entity.dragon_fireball.explode");
        oldToNew.put("entity.endereye.death", "entity.ender_eye.death");
        oldToNew.put("entity.endereye.launch", "entity.ender_eye.launch");
        oldToNew.put("entity.endermen.ambient", "entity.enderman.ambient");
        oldToNew.put("entity.endermen.death", "entity.enderman.death");
        oldToNew.put("entity.endermen.hurt", "entity.enderman.hurt");
        oldToNew.put("entity.endermen.scream", "entity.enderman.scream");
        oldToNew.put("entity.endermen.stare", "entity.enderman.stare");
        oldToNew.put("entity.endermen.teleport", "entity.enderman.teleport");
        oldToNew.put("entity.enderpearl.throw", "entity.ender_pearl.throw");
        oldToNew.put("entity.evocation_illager.ambient", "entity.evoker.ambient");
        oldToNew.put("entity.evocation_illager.cast_spell", "entity.evoker.cast_spell");
        oldToNew.put("entity.evocation_illager.death", "entity.evoker.death");
        oldToNew.put("entity.evocation_illager.hurt", "entity.evoker.hurt");
        oldToNew.put("entity.evocation_illager.prepare_attack", "entity.evoker.prepare_attack");
        oldToNew.put("entity.evocation_illager.prepare_summon", "entity.evoker.prepare_summon");
        oldToNew.put("entity.evocation_illager.prepare_wololo", "entity.evoker.prepare_wololo");
        oldToNew.put("entity.firework.blast", "entity.firework_rocket.blast");
        oldToNew.put("entity.firework.blast_far", "entity.firework_rocket.blast_far");
        oldToNew.put("entity.firework.large_blast", "entity.firework_rocket.large_blast");
        oldToNew.put("entity.firework.large_blast_far", "entity.firework_rocket.large_blast_far");
        oldToNew.put("entity.firework.launch", "entity.firework_rocket.launch");
        oldToNew.put("entity.firework.shoot", "entity.firework_rocket.shoot");
        oldToNew.put("entity.firework.twinkle", "entity.firework_rocket.twinkle");
        oldToNew.put("entity.firework.twinkle_far", "entity.firework_rocket.twinkle_far");
        oldToNew.put("entity.illusion_illager.ambient", "entity.illusioner.ambient");
        oldToNew.put("entity.illusion_illager.cast_spell", "entity.illusioner.cast_spell");
        oldToNew.put("entity.illusion_illager.death", "entity.illusioner.death");
        oldToNew.put("entity.illusion_illager.hurt", "entity.illusioner.hurt");
        oldToNew.put("entity.illusion_illager.mirror_move", "entity.illusioner.mirror_move");
        oldToNew.put("entity.illusion_illager.prepare_blindness", "entity.illusioner.prepare_blindness");
        oldToNew.put("entity.illusion_illager.prepare_mirror", "entity.illusioner.prepare_mirror");
        oldToNew.put("entity.irongolem.attack", "entity.iron_golem.attack");
        oldToNew.put("entity.irongolem.death", "entity.iron_golem.death");
        oldToNew.put("entity.irongolem.hurt", "entity.iron_golem.hurt");
        oldToNew.put("entity.irongolem.step", "entity.iron_golem.step");
        oldToNew.put("entity.itemframe.add_item", "entity.item_frame.add_item");
        oldToNew.put("entity.itemframe.break", "entity.item_frame.break");
        oldToNew.put("entity.itemframe.place", "entity.item_frame.place");
        oldToNew.put("entity.itemframe.remove_item", "entity.item_frame.remove_item");
        oldToNew.put("entity.itemframe.rotate_item", "entity.item_frame.rotate_item");
        oldToNew.put("entity.leashknot.break", "entity.leash_knot.break");
        oldToNew.put("entity.leashknot.place", "entity.leash_knot.place");
        oldToNew.put("entity.lightning.impact", "entity.lightning_bolt.impact");
        oldToNew.put("entity.lightning.thunder", "entity.lightning_bolt.thunder");
        oldToNew.put("entity.lingeringpotion.throw", "entity.lingering_potion.throw");
        oldToNew.put("entity.magmacube.death", "entity.magma_cube.death");
        oldToNew.put("entity.magmacube.hurt", "entity.magma_cube.hurt");
        oldToNew.put("entity.magmacube.jump", "entity.magma_cube.jump");
        oldToNew.put("entity.magmacube.squish", "entity.magma_cube.squish");
        oldToNew.put("entity.parrot.imitate.enderdragon", "entity.parrot.imitate.ender_dragon");
        oldToNew.put("entity.parrot.imitate.evocation_illager", "entity.parrot.imitate.evoker");
        oldToNew.put("entity.parrot.imitate.illusion_illager", "entity.parrot.imitate.illusioner");
        oldToNew.put("entity.parrot.imitate.magmacube", "entity.parrot.imitate.magma_cube");
        oldToNew.put("entity.parrot.imitate.vindication_illager", "entity.parrot.imitate.vindicator");
        oldToNew.put("entity.player.splash.highspeed", "entity.player.splash.high_speed");
        oldToNew.put("entity.polar_bear.baby_ambient", "entity.polar_bear.ambient_baby");
        oldToNew.put("entity.small_magmacube.death", "entity.magma_cube.death_small");
        oldToNew.put("entity.small_magmacube.hurt", "entity.magma_cube.hurt_small");
        oldToNew.put("entity.small_magmacube.squish", "entity.magma_cube.squish_small");
        oldToNew.put("entity.small_slime.death", "entity.slime.death_small");
        oldToNew.put("entity.small_slime.hurt", "entity.slime.hurt_small");
        oldToNew.put("entity.small_slime.jump", "entity.slime.jump_small");
        oldToNew.put("entity.small_slime.squish", "entity.slime.squish_small");
        oldToNew.put("entity.snowman.ambient", "entity.snow_golem.ambient");
        oldToNew.put("entity.snowman.death", "entity.snow_golem.death");
        oldToNew.put("entity.snowman.hurt", "entity.snow_golem.hurt");
        oldToNew.put("entity.snowman.shoot", "entity.snow_golem.shoot");
        oldToNew.put("entity.villager.trading", "entity.villager.trade");
        oldToNew.put("entity.vindication_illager.ambient", "entity.vindicator.ambient");
        oldToNew.put("entity.vindication_illager.death", "entity.vindicator.death");
        oldToNew.put("entity.vindication_illager.hurt", "entity.vindicator.hurt");
        oldToNew.put("entity.zombie.attack_door_wood", "entity.zombie.attack_wooden_door");
        oldToNew.put("entity.zombie.break_door_wood", "entity.zombie.break_wooden_door");
        oldToNew.put("entity.zombie_pig.ambient", "entity.zombie_pigman.ambient");
        oldToNew.put("entity.zombie_pig.angry", "entity.zombie_pigman.angry");
        oldToNew.put("entity.zombie_pig.death", "entity.zombie_pigman.death");
        oldToNew.put("entity.zombie_pig.hurt", "entity.zombie_pigman.hurt");
        oldToNew.put("record.11", "music_disc.11");
        oldToNew.put("record.13", "music_disc.13");
        oldToNew.put("record.blocks", "music_disc.blocks");
        oldToNew.put("record.cat", "music_disc.cat");
        oldToNew.put("record.chirp", "music_disc.chirp");
        oldToNew.put("record.far", "music_disc.far");
        oldToNew.put("record.mall", "music_disc.mall");
        oldToNew.put("record.mellohi", "music_disc.mellohi");
        oldToNew.put("record.stal", "music_disc.stal");
        oldToNew.put("record.strad", "music_disc.strad");
        oldToNew.put("record.wait", "music_disc.wait");
        oldToNew.put("record.ward", "music_disc.ward");
    }

    public static String getNewId(String old) {
        String newId = oldToNew.get(old);
        return newId != null ? newId : old.toLowerCase(Locale.ROOT);
    }
}
