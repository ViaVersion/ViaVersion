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
package us.myles.ViaVersion.protocols.protocol1_16to1_15_2.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.rewriters.ComponentRewriter;

import java.util.HashMap;
import java.util.Map;

public class TranslationMappings extends ComponentRewriter {
    private final Map<String, String> mappings = new HashMap<>();

    public TranslationMappings(Protocol protocol) {
        super(protocol);
        mappings.put("block.minecraft.flowing_water", "Flowing Water");
        mappings.put("block.minecraft.flowing_lava", "Flowing Lava");
        mappings.put("block.minecraft.bed", "Bed");
        mappings.put("block.minecraft.bed.not_valid", "Your home bed was missing or obstructed");
        mappings.put("block.minecraft.bed.set_spawn", "Respawn point set");
        mappings.put("block.minecraft.two_turtle_eggs", "Two Turtle Eggs");
        mappings.put("block.minecraft.three_turtle_eggs", "Three Turtle Eggs");
        mappings.put("block.minecraft.four_turtle_eggs", "Four Turtle Eggs");
        mappings.put("block.minecraft.banner", "Banner");
        mappings.put("block.minecraft.wall_banner", "Wall Banner");
        mappings.put("item.minecraft.zombie_pigman_spawn_egg", "Zombie Pigman Spawn Egg");
        mappings.put("item.minecraft.skeleton_skull", "Skeleton Skull");
        mappings.put("item.minecraft.wither_skeleton_skull", "Wither Skeleton Skull");
        mappings.put("item.minecraft.zombie_head", "Zombie Head");
        mappings.put("item.minecraft.creeper_head", "Creeper Head");
        mappings.put("item.minecraft.dragon_head", "Dragon Head");
        mappings.put("entity.minecraft.zombie_pigman", "Zombie Pigman");
        mappings.put("death.fell.accident.water", "%1$s fell out of the water");
        mappings.put("death.attack.netherBed.message", "%1$s was killed by %2$s");
        mappings.put("death.attack.netherBed.link", "Intentional Game Design");
        mappings.put("advancements.husbandry.break_diamond_hoe.title", "Serious Dedication");
        mappings.put("advancements.husbandry.break_diamond_hoe.description", "Completely use up a diamond hoe, and then reevaluate your life choices");
        mappings.put("biome.minecraft.nether", "Nether");
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
