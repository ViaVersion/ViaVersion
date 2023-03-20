/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_16to1_15_2.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.viaversion.viaversion.protocols.protocol1_15to1_14_4.ClientboundPackets1_15;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.Protocol1_16To1_15_2;
import com.viaversion.viaversion.rewriter.ComponentRewriter;
import java.util.HashMap;
import java.util.Map;

public class TranslationMappings extends ComponentRewriter<ClientboundPackets1_15> {
    private final Map<String, String> mappings = new HashMap<>();

    public TranslationMappings(Protocol1_16To1_15_2 protocol) {
        super(protocol);
        mappings.put("attribute.name.generic.armorToughness", "attribute.name.generic.armor_toughness");
        mappings.put("attribute.name.generic.attackDamage", "attribute.name.generic.attack_damage");
        mappings.put("attribute.name.generic.attackSpeed", "attribute.name.generic.attack_speed");
        mappings.put("attribute.name.generic.followRange", "attribute.name.generic.follow_range");
        mappings.put("attribute.name.generic.knockbackResistance", "attribute.name.generic.knockback_resistance");
        mappings.put("attribute.name.generic.maxHealth", "attribute.name.generic.max_health");
        mappings.put("attribute.name.generic.movementSpeed", "attribute.name.generic.movement_speed");
        mappings.put("attribute.name.horse.jumpStrength", "attribute.name.horse.jump_strength");
        mappings.put("attribute.name.zombie.spawnReinforcements", "attribute.name.zombie.spawn_reinforcements");
        mappings.put("block.minecraft.banner", "Banner");
        mappings.put("block.minecraft.wall_banner", "Wall Banner");
        mappings.put("block.minecraft.bed", "Bed");
        mappings.put("block.minecraft.bed.not_valid", "block.minecraft.spawn.not_valid");
        mappings.put("block.minecraft.bed.set_spawn", "block.minecraft.set_spawn");
        mappings.put("block.minecraft.flowing_water", "Flowing Water");
        mappings.put("block.minecraft.flowing_lava", "Flowing Lava");
        mappings.put("block.minecraft.two_turtle_eggs", "Two Turtle Eggs");
        mappings.put("block.minecraft.three_turtle_eggs", "Three Turtle Eggs");
        mappings.put("block.minecraft.four_turtle_eggs", "Four Turtle Eggs");
        mappings.put("item.minecraft.skeleton_skull", "block.minecraft.skeleton_skull");
        mappings.put("item.minecraft.wither_skeleton_skull", "block.minecraft.skeleton_wall_skull");
        mappings.put("item.minecraft.zombie_head", "block.minecraft.zombie_head");
        mappings.put("item.minecraft.creeper_head", "block.minecraft.creeper_head");
        mappings.put("item.minecraft.dragon_head", "block.minecraft.dragon_head");
        mappings.put("entity.minecraft.zombie_pigman", "Zombie Pigman");
        mappings.put("item.minecraft.zombie_pigman_spawn_egg", "Zombie Pigman Spawn Egg");
        mappings.put("death.fell.accident.water", "%1$s fell out of the water");
        mappings.put("death.attack.netherBed.message", "death.attack.badRespawnPoint.message");
        mappings.put("death.attack.netherBed.link", "death.attack.badRespawnPoint.link");
        mappings.put("advancements.husbandry.break_diamond_hoe.title", "Serious Dedication");
        mappings.put("advancements.husbandry.break_diamond_hoe.description", "Completely use up a diamond hoe, and then reevaluate your life choices");
        mappings.put("biome.minecraft.nether", "Nether");
        mappings.put("key.swapHands", "key.swapOffhand");
    }

    @Override
    public void processText(JsonElement element) {
        super.processText(element);
        if (element == null || !element.isJsonObject()) return;

        // Score components no longer contain value fields
        JsonObject object = element.getAsJsonObject();
        JsonObject score = object.getAsJsonObject("score");
        if (score == null || object.has("text")) return;

        JsonPrimitive value = score.getAsJsonPrimitive("value");
        if (value != null) {
            object.remove("score");
            object.add("text", value);
        }
    }

    @Override
    protected void handleTranslate(JsonObject object, String translate) {
        // A few keys were removed - manually set the text of relevant ones
        String mappedTranslation = mappings.get(translate);
        if (mappedTranslation != null) {
            object.addProperty("translate", mappedTranslation);
        }
    }
}
