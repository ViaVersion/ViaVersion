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
package com.viaversion.viaversion.api.minecraft.data;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.minecraft.GameProfile;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.AdventureModePredicate;
import com.viaversion.viaversion.api.minecraft.item.data.ArmorTrim;
import com.viaversion.viaversion.api.minecraft.item.data.AttributeModifiers1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.AttributeModifiers1_21;
import com.viaversion.viaversion.api.minecraft.item.data.BannerPatternLayer;
import com.viaversion.viaversion.api.minecraft.item.data.Bee;
import com.viaversion.viaversion.api.minecraft.item.data.BlockStateProperties;
import com.viaversion.viaversion.api.minecraft.item.data.Consumable1_21_2;
import com.viaversion.viaversion.api.minecraft.item.data.CustomModelData1_21_4;
import com.viaversion.viaversion.api.minecraft.item.data.DamageResistant;
import com.viaversion.viaversion.api.minecraft.item.data.DeathProtection;
import com.viaversion.viaversion.api.minecraft.item.data.DyedColor;
import com.viaversion.viaversion.api.minecraft.item.data.Enchantments;
import com.viaversion.viaversion.api.minecraft.item.data.Equippable;
import com.viaversion.viaversion.api.minecraft.item.data.FilterableString;
import com.viaversion.viaversion.api.minecraft.item.data.FireworkExplosion;
import com.viaversion.viaversion.api.minecraft.item.data.Fireworks;
import com.viaversion.viaversion.api.minecraft.item.data.FoodProperties1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.FoodProperties1_21_2;
import com.viaversion.viaversion.api.minecraft.item.data.Instrument1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.Instrument1_21_2;
import com.viaversion.viaversion.api.minecraft.item.data.JukeboxPlayable;
import com.viaversion.viaversion.api.minecraft.item.data.LodestoneTracker;
import com.viaversion.viaversion.api.minecraft.item.data.PotDecorations;
import com.viaversion.viaversion.api.minecraft.item.data.PotionContents;
import com.viaversion.viaversion.api.minecraft.item.data.SuspiciousStewEffect;
import com.viaversion.viaversion.api.minecraft.item.data.ToolProperties;
import com.viaversion.viaversion.api.minecraft.item.data.Unbreakable;
import com.viaversion.viaversion.api.minecraft.item.data.UseCooldown;
import com.viaversion.viaversion.api.minecraft.item.data.WrittenBook;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.api.type.types.version.Types1_20_5;
import com.viaversion.viaversion.api.type.types.version.Types1_21;
import com.viaversion.viaversion.api.type.types.version.Types1_21_2;
import com.viaversion.viaversion.api.type.types.version.Types1_21_4;
import com.viaversion.viaversion.util.Unit;

public record StructuredDataKey<T>(String identifier, Type<T> type) {

    public static final StructuredDataKey<CompoundTag> CUSTOM_DATA = new StructuredDataKey<>("custom_data", Types.COMPOUND_TAG);
    public static final StructuredDataKey<Integer> MAX_STACK_SIZE = new StructuredDataKey<>("max_stack_size", Types.VAR_INT);
    public static final StructuredDataKey<Integer> MAX_DAMAGE = new StructuredDataKey<>("max_damage", Types.VAR_INT);
    public static final StructuredDataKey<Integer> DAMAGE = new StructuredDataKey<>("damage", Types.VAR_INT);
    public static final StructuredDataKey<Unbreakable> UNBREAKABLE = new StructuredDataKey<>("unbreakable", Unbreakable.TYPE);
    public static final StructuredDataKey<Tag> CUSTOM_NAME = new StructuredDataKey<>("custom_name", Types.TAG);
    public static final StructuredDataKey<Tag> ITEM_NAME = new StructuredDataKey<>("item_name", Types.TAG);
    public static final StructuredDataKey<String> ITEM_MODEL = new StructuredDataKey<>("item_model", Types.STRING);
    public static final StructuredDataKey<Tag[]> LORE = new StructuredDataKey<>("lore", new ArrayType<>(Types.TAG, 256));
    public static final StructuredDataKey<Integer> RARITY = new StructuredDataKey<>("rarity", Types.VAR_INT);
    public static final StructuredDataKey<Enchantments> ENCHANTMENTS = new StructuredDataKey<>("enchantments", Enchantments.TYPE);
    public static final StructuredDataKey<AdventureModePredicate> CAN_PLACE_ON = new StructuredDataKey<>("can_place_on", AdventureModePredicate.TYPE);
    public static final StructuredDataKey<AdventureModePredicate> CAN_BREAK = new StructuredDataKey<>("can_break", AdventureModePredicate.TYPE);
    public static final StructuredDataKey<AttributeModifiers1_20_5> ATTRIBUTE_MODIFIERS1_20_5 = new StructuredDataKey<>("attribute_modifiers", AttributeModifiers1_20_5.TYPE);
    public static final StructuredDataKey<AttributeModifiers1_21> ATTRIBUTE_MODIFIERS1_21 = new StructuredDataKey<>("attribute_modifiers", AttributeModifiers1_21.TYPE);
    public static final StructuredDataKey<Integer> CUSTOM_MODEL_DATA1_20_5 = new StructuredDataKey<>("custom_model_data", Types.VAR_INT);
    public static final StructuredDataKey<CustomModelData1_21_4> CUSTOM_MODEL_DATA1_21_4 = new StructuredDataKey<>("custom_model_data", CustomModelData1_21_4.TYPE);
    public static final StructuredDataKey<Unit> HIDE_ADDITIONAL_TOOLTIP = new StructuredDataKey<>("hide_additional_tooltip", Types.EMPTY);
    public static final StructuredDataKey<Unit> HIDE_TOOLTIP = new StructuredDataKey<>("hide_tooltip", Types.EMPTY);
    public static final StructuredDataKey<Integer> REPAIR_COST = new StructuredDataKey<>("repair_cost", Types.VAR_INT);
    public static final StructuredDataKey<Unit> CREATIVE_SLOT_LOCK = new StructuredDataKey<>("creative_slot_lock", Types.EMPTY);
    public static final StructuredDataKey<Boolean> ENCHANTMENT_GLINT_OVERRIDE = new StructuredDataKey<>("enchantment_glint_override", Types.BOOLEAN);
    public static final StructuredDataKey<Tag> INTANGIBLE_PROJECTILE = new StructuredDataKey<>("intangible_projectile", Types.TAG); // Doesn't actually hold data
    public static final StructuredDataKey<FoodProperties1_20_5> FOOD1_20_5 = new StructuredDataKey<>("food", FoodProperties1_20_5.TYPE1_20_5);
    public static final StructuredDataKey<FoodProperties1_20_5> FOOD1_21 = new StructuredDataKey<>("food", FoodProperties1_20_5.TYPE1_21);
    public static final StructuredDataKey<FoodProperties1_21_2> FOOD1_21_2 = new StructuredDataKey<>("food", FoodProperties1_21_2.TYPE);
    public static final StructuredDataKey<Consumable1_21_2> CONSUMABLE1_21_2 = new StructuredDataKey<>("consumable", Consumable1_21_2.TYPE);
    public static final StructuredDataKey<Item> USE_REMAINDER1_21_2 = new StructuredDataKey<>("use_remainder", Types1_21_2.ITEM);
    public static final StructuredDataKey<Item> USE_REMAINDER1_21_4 = new StructuredDataKey<>("use_remainder", Types1_21_4.ITEM);
    public static final StructuredDataKey<UseCooldown> USE_COOLDOWN = new StructuredDataKey<>("use_cooldown", UseCooldown.TYPE);
    public static final StructuredDataKey<Unit> FIRE_RESISTANT = new StructuredDataKey<>("fire_resistant", Types.EMPTY);
    public static final StructuredDataKey<DamageResistant> DAMAGE_RESISTANT = new StructuredDataKey<>("damage_resistant", DamageResistant.TYPE);
    public static final StructuredDataKey<ToolProperties> TOOL = new StructuredDataKey<>("tool", ToolProperties.TYPE);
    public static final StructuredDataKey<Integer> ENCHANTABLE = new StructuredDataKey<>("enchantable", Types.VAR_INT);
    public static final StructuredDataKey<Equippable> EQUIPPABLE = new StructuredDataKey<>("equippable", Equippable.TYPE);
    public static final StructuredDataKey<HolderSet> REPAIRABLE = new StructuredDataKey<>("repairable", Types.HOLDER_SET);
    public static final StructuredDataKey<Unit> GLIDER = new StructuredDataKey<>("glider", Types.EMPTY);
    public static final StructuredDataKey<String> TOOLTIP_STYLE = new StructuredDataKey<>("tooltip_style", Types.STRING);
    public static final StructuredDataKey<DeathProtection> DEATH_PROTECTION = new StructuredDataKey<>("death_protection", DeathProtection.TYPE);
    public static final StructuredDataKey<Enchantments> STORED_ENCHANTMENTS = new StructuredDataKey<>("stored_enchantments", Enchantments.TYPE);
    public static final StructuredDataKey<DyedColor> DYED_COLOR = new StructuredDataKey<>("dyed_color", DyedColor.TYPE);
    public static final StructuredDataKey<Integer> MAP_COLOR = new StructuredDataKey<>("map_color", Types.INT);
    public static final StructuredDataKey<Integer> MAP_ID = new StructuredDataKey<>("map_id", Types.VAR_INT);
    public static final StructuredDataKey<CompoundTag> MAP_DECORATIONS = new StructuredDataKey<>("map_decorations", Types.COMPOUND_TAG);
    public static final StructuredDataKey<Integer> MAP_POST_PROCESSING = new StructuredDataKey<>("map_post_processing", Types.VAR_INT);
    public static final StructuredDataKey<Item[]> CHARGED_PROJECTILES1_20_5 = new StructuredDataKey<>("charged_projectiles", Types1_20_5.ITEM_ARRAY);
    public static final StructuredDataKey<Item[]> CHARGED_PROJECTILES1_21 = new StructuredDataKey<>("charged_projectiles", Types1_21.ITEM_ARRAY);
    public static final StructuredDataKey<Item[]> CHARGED_PROJECTILES1_21_2 = new StructuredDataKey<>("charged_projectiles", Types1_21_2.ITEM_ARRAY);
    public static final StructuredDataKey<Item[]> CHARGED_PROJECTILES1_21_4 = new StructuredDataKey<>("charged_projectiles", Types1_21_4.ITEM_ARRAY);
    public static final StructuredDataKey<Item[]> BUNDLE_CONTENTS1_20_5 = new StructuredDataKey<>("bundle_contents", Types1_20_5.ITEM_ARRAY);
    public static final StructuredDataKey<Item[]> BUNDLE_CONTENTS1_21 = new StructuredDataKey<>("bundle_contents", Types1_21.ITEM_ARRAY);
    public static final StructuredDataKey<Item[]> BUNDLE_CONTENTS1_21_2 = new StructuredDataKey<>("bundle_contents", Types1_21_2.ITEM_ARRAY);
    public static final StructuredDataKey<Item[]> BUNDLE_CONTENTS1_21_4 = new StructuredDataKey<>("bundle_contents", Types1_21_4.ITEM_ARRAY);
    public static final StructuredDataKey<PotionContents> POTION_CONTENTS1_20_5 = new StructuredDataKey<>("potion_contents", PotionContents.TYPE1_20_5);
    public static final StructuredDataKey<PotionContents> POTION_CONTENTS1_21_2 = new StructuredDataKey<>("potion_contents", PotionContents.TYPE1_21_2);
    public static final StructuredDataKey<SuspiciousStewEffect[]> SUSPICIOUS_STEW_EFFECTS = new StructuredDataKey<>("suspicious_stew_effects", SuspiciousStewEffect.ARRAY_TYPE);
    public static final StructuredDataKey<FilterableString[]> WRITABLE_BOOK_CONTENT = new StructuredDataKey<>("writable_book_content", FilterableString.ARRAY_TYPE);
    public static final StructuredDataKey<WrittenBook> WRITTEN_BOOK_CONTENT = new StructuredDataKey<>("written_book_content", WrittenBook.TYPE);
    public static final StructuredDataKey<ArmorTrim> TRIM1_20_5 = new StructuredDataKey<>("trim", ArmorTrim.TYPE1_20_5);
    public static final StructuredDataKey<ArmorTrim> TRIM1_21_2 = new StructuredDataKey<>("trim", ArmorTrim.TYPE1_21_2);
    public static final StructuredDataKey<ArmorTrim> TRIM1_21_4 = new StructuredDataKey<>("trim", ArmorTrim.TYPE1_21_4);
    public static final StructuredDataKey<CompoundTag> DEBUG_STICK_STATE = new StructuredDataKey<>("debug_stick_state", Types.COMPOUND_TAG);
    public static final StructuredDataKey<CompoundTag> ENTITY_DATA = new StructuredDataKey<>("entity_data", Types.COMPOUND_TAG);
    public static final StructuredDataKey<CompoundTag> BUCKET_ENTITY_DATA = new StructuredDataKey<>("bucket_entity_data", Types.COMPOUND_TAG);
    public static final StructuredDataKey<CompoundTag> BLOCK_ENTITY_DATA = new StructuredDataKey<>("block_entity_data", Types.COMPOUND_TAG);
    public static final StructuredDataKey<Holder<Instrument1_20_5>> INSTRUMENT1_20_5 = new StructuredDataKey<>("instrument", Instrument1_20_5.TYPE);
    public static final StructuredDataKey<Holder<Instrument1_21_2>> INSTRUMENT1_21_2 = new StructuredDataKey<>("instrument", Instrument1_21_2.TYPE);
    public static final StructuredDataKey<Integer> OMINOUS_BOTTLE_AMPLIFIER = new StructuredDataKey<>("ominous_bottle_amplifier", Types.VAR_INT);
    public static final StructuredDataKey<JukeboxPlayable> JUKEBOX_PLAYABLE = new StructuredDataKey<>("jukebox_playable", JukeboxPlayable.TYPE);
    public static final StructuredDataKey<Tag> RECIPES = new StructuredDataKey<>("recipes", Types.TAG);
    public static final StructuredDataKey<LodestoneTracker> LODESTONE_TRACKER = new StructuredDataKey<>("lodestone_tracker", LodestoneTracker.TYPE);
    public static final StructuredDataKey<FireworkExplosion> FIREWORK_EXPLOSION = new StructuredDataKey<>("firework_explosion", FireworkExplosion.TYPE);
    public static final StructuredDataKey<Fireworks> FIREWORKS = new StructuredDataKey<>("fireworks", Fireworks.TYPE);
    public static final StructuredDataKey<GameProfile> PROFILE = new StructuredDataKey<>("profile", Types.GAME_PROFILE);
    public static final StructuredDataKey<String> NOTE_BLOCK_SOUND = new StructuredDataKey<>("note_block_sound", Types.STRING);
    public static final StructuredDataKey<BannerPatternLayer[]> BANNER_PATTERNS = new StructuredDataKey<>("banner_patterns", BannerPatternLayer.ARRAY_TYPE);
    public static final StructuredDataKey<Integer> BASE_COLOR = new StructuredDataKey<>("base_color", Types.VAR_INT);
    public static final StructuredDataKey<PotDecorations> POT_DECORATIONS = new StructuredDataKey<>("pot_decorations", PotDecorations.TYPE);
    public static final StructuredDataKey<Item[]> CONTAINER1_20_5 = new StructuredDataKey<>("container", Types1_20_5.ITEM_ARRAY);
    public static final StructuredDataKey<Item[]> CONTAINER1_21 = new StructuredDataKey<>("container", Types1_21.ITEM_ARRAY);
    public static final StructuredDataKey<Item[]> CONTAINER1_21_2 = new StructuredDataKey<>("container", new ArrayType<>(Types1_21_2.ITEM, 256));
    public static final StructuredDataKey<Item[]> CONTAINER1_21_4 = new StructuredDataKey<>("container", Types1_21_4.ITEM_ARRAY);
    public static final StructuredDataKey<BlockStateProperties> BLOCK_STATE = new StructuredDataKey<>("block_state", BlockStateProperties.TYPE);
    public static final StructuredDataKey<Bee[]> BEES = new StructuredDataKey<>("bees", Bee.ARRAY_TYPE);
    public static final StructuredDataKey<Tag> LOCK = new StructuredDataKey<>("lock", Types.TAG);
    public static final StructuredDataKey<CompoundTag> CONTAINER_LOOT = new StructuredDataKey<>("container_loot", Types.COMPOUND_TAG);

    @Override
    public String toString() {
        return "StructuredDataKey{" +
            "identifier='" + identifier + '\'' +
            ", type=" + type +
            '}';
    }
}
