/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.type.types.EnumType;
import com.viaversion.viaversion.api.type.types.EnumType.Fallback;
import com.viaversion.viaversion.api.type.types.FakeEnumType;
import com.viaversion.viaversion.api.type.types.RegistryValueType;
import java.util.List;

import static com.viaversion.viaversion.api.type.types.FakeEnumType.Entry.of;

/**
 * Enum types that aren't associated with any specific item data in this package.
 */
public final class EnumTypes {

    public static final EnumType DYE_COLOR = new EnumType("white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black");
    public static final EnumType RARITY = new EnumType("common", "uncommon", "rare", "epic");
    public static final EnumType FOX_VARIANT = new EnumType("red", "snow");
    public static final EnumType SALMON_VARIANT = new EnumType(Fallback.CLAMP, "small", "medium", "large");
    public static final EnumType PARROT_VARIANT = new EnumType(Fallback.CLAMP, "red_blue", "blue", "green", "yellow_blue", "gray");
    public static final EnumType MUSHROOM_COW_VARIANT = new EnumType(Fallback.CLAMP, "red", "brown");
    public static final EnumType HORSE_VARIANT = new EnumType(Fallback.WRAP, "white", "creamy", "chestnut", "brown", "black", "gray", "dark_brown");
    public static final EnumType LLAMA_VARIANT = new EnumType(Fallback.CLAMP, "creamy", "white", "brown", "gray");
    public static final EnumType AXOLOTL_VARIANT = new EnumType("lucy", "wild", "gold", "cyan", "blue");
    public static final EnumType EQUIPMENT_SLOT = new EnumType("mainhand", "feet", "legs", "chest", "head", "offhand", "body", "saddle");
    // Enums with non-ordinal ids
    public static final FakeEnumType RABBIT_VARIANT = new FakeEnumType(List.of("brown", "white", "black", "white_splotched", "gold", "salt"), of(99, "evil"));
    // Pretty much enums, but with a resource location
    public static final RegistryValueType VILLAGER_TYPE = new RegistryValueType("desert", "jungle", "plains", "savanna", "snow", "swamp", "taiga");
    public static final RegistryValueType POTION = new RegistryValueType("water", "mundane", "thick", "awkward", "night_vision", "long_night_vision", "invisibility", "long_invisibility", "leaping", "long_leaping", "strong_leaping", "fire_resistance", "long_fire_resistance", "swiftness", "long_swiftness", "strong_swiftness", "slowness", "long_slowness", "strong_slowness", "turtle_master", "long_turtle_master", "strong_turtle_master", "water_breathing", "long_water_breathing", "healing", "strong_healing", "harming", "strong_harming", "poison", "long_poison", "strong_poison", "regeneration", "long_regeneration", "strong_regeneration", "strength", "long_strength", "strong_strength", "weakness", "long_weakness", "luck", "slow_falling", "long_slow_falling", "wind_charged", "weaving", "oozing", "infested");
    public static final RegistryValueType MOB_EFFECT = new RegistryValueType("speed", "slowness", "haste", "mining_fatigue", "strength", "instant_health", "instant_damage", "jump_boost", "nausea", "regeneration", "resistance", "fire_resistance", "water_breathing", "invisibility", "blindness", "night_vision", "hunger", "weakness", "poison", "wither", "health_boost", "absorption", "saturation", "glowing", "levitation", "luck", "unluck", "slow_falling", "conduit_power", "dolphins_grace", "bad_omen", "hero_of_the_village", "darkness", "trial_omen", "raid_omen", "wind_charged", "weaving", "oozing", "infested");
}
