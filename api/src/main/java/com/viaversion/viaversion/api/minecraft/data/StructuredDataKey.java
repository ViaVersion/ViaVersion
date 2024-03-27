/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.api.minecraft.GameProfile;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.AdventureModePredicate;
import com.viaversion.viaversion.api.minecraft.item.data.ArmorTrim;
import com.viaversion.viaversion.api.minecraft.item.data.AttributeModifiers;
import com.viaversion.viaversion.api.minecraft.item.data.BannerPatternLayer;
import com.viaversion.viaversion.api.minecraft.item.data.Bee;
import com.viaversion.viaversion.api.minecraft.item.data.BlockStateProperties;
import com.viaversion.viaversion.api.minecraft.item.data.DyedColor;
import com.viaversion.viaversion.api.minecraft.item.data.Enchantments;
import com.viaversion.viaversion.api.minecraft.item.data.FilterableString;
import com.viaversion.viaversion.api.minecraft.item.data.FireworkExplosion;
import com.viaversion.viaversion.api.minecraft.item.data.Fireworks;
import com.viaversion.viaversion.api.minecraft.item.data.FoodProperties;
import com.viaversion.viaversion.api.minecraft.item.data.Instrument;
import com.viaversion.viaversion.api.minecraft.item.data.LodestoneTracker;
import com.viaversion.viaversion.api.minecraft.item.data.PotDecorations;
import com.viaversion.viaversion.api.minecraft.item.data.PotionContents;
import com.viaversion.viaversion.api.minecraft.item.data.SuspiciousStewEffect;
import com.viaversion.viaversion.api.minecraft.item.data.ToolProperties;
import com.viaversion.viaversion.api.minecraft.item.data.Unbreakable;
import com.viaversion.viaversion.api.minecraft.item.data.WrittenBook;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_20_5;
import com.viaversion.viaversion.util.Unit;

public final class StructuredDataKey<T> {

    public static final StructuredDataKey<CompoundTag> CUSTOM_DATA = new StructuredDataKey<>("custom_data", Type.COMPOUND_TAG);
    public static final StructuredDataKey<Integer> MAX_STACK_SIZE = new StructuredDataKey<>("max_stack_size", Type.VAR_INT);
    public static final StructuredDataKey<Integer> MAX_DAMAGE = new StructuredDataKey<>("max_damage", Type.VAR_INT);
    public static final StructuredDataKey<Integer> DAMAGE = new StructuredDataKey<>("damage", Type.VAR_INT);
    public static final StructuredDataKey<Unbreakable> UNBREAKABLE = new StructuredDataKey<>("unbreakable", Unbreakable.TYPE);
    public static final StructuredDataKey<Tag> CUSTOM_NAME = new StructuredDataKey<>("custom_name", Type.TAG);
    public static final StructuredDataKey<Tag> ITEM_NAME = new StructuredDataKey<>("item_name", Type.TAG);
    public static final StructuredDataKey<Tag[]> LORE = new StructuredDataKey<>("lore", Type.TAG_ARRAY);
    public static final StructuredDataKey<Integer> RARITY = new StructuredDataKey<>("rarity", Type.VAR_INT);
    public static final StructuredDataKey<Enchantments> ENCHANTMENTS = new StructuredDataKey<>("enchantments", Enchantments.TYPE);
    public static final StructuredDataKey<AdventureModePredicate> CAN_PLACE_ON = new StructuredDataKey<>("can_place_on", AdventureModePredicate.TYPE);
    public static final StructuredDataKey<AdventureModePredicate> CAN_BREAK = new StructuredDataKey<>("can_break", AdventureModePredicate.TYPE);
    public static final StructuredDataKey<AttributeModifiers> ATTRIBUTE_MODIFIERS = new StructuredDataKey<>("attribute_modifiers", AttributeModifiers.TYPE);
    public static final StructuredDataKey<Integer> CUSTOM_MODEL_DATA = new StructuredDataKey<>("custom_model_data", Type.VAR_INT);
    public static final StructuredDataKey<Unit> HIDE_ADDITIONAL_TOOLTIP = new StructuredDataKey<>("hide_additional_tooltip", Type.EMPTY);
    public static final StructuredDataKey<Unit> HIDE_TOOLTIP = new StructuredDataKey<>("hide_tooltip", Type.EMPTY);
    public static final StructuredDataKey<Integer> REPAIR_COST = new StructuredDataKey<>("repair_cost", Type.VAR_INT);
    public static final StructuredDataKey<Unit> CREATIVE_SLOT_LOCK = new StructuredDataKey<>("creative_slot_lock", Type.EMPTY);
    public static final StructuredDataKey<Boolean> ENCHANTMENT_GLINT_OVERRIDE = new StructuredDataKey<>("enchantment_glint_override", Type.BOOLEAN);
    public static final StructuredDataKey<Tag> INTANGIBLE_PROJECTILE = new StructuredDataKey<>("intangible_projectile", Type.TAG); // Doesn't actually hold data
    public static final StructuredDataKey<FoodProperties> FOOD = new StructuredDataKey<>("food", FoodProperties.TYPE);
    public static final StructuredDataKey<Unit> FIRE_RESISTANT = new StructuredDataKey<>("fire_resistant", Type.EMPTY);
    public static final StructuredDataKey<ToolProperties> TOOL = new StructuredDataKey<>("tool", ToolProperties.TYPE);
    public static final StructuredDataKey<Enchantments> STORED_ENCHANTMENTS = new StructuredDataKey<>("stored_enchantments", Enchantments.TYPE);
    public static final StructuredDataKey<DyedColor> DYED_COLOR = new StructuredDataKey<>("dyed_color", DyedColor.TYPE);
    public static final StructuredDataKey<Integer> MAP_COLOR = new StructuredDataKey<>("map_color", Type.INT);
    public static final StructuredDataKey<Integer> MAP_ID = new StructuredDataKey<>("map_id", Type.VAR_INT);
    public static final StructuredDataKey<CompoundTag> MAP_DECORATIONS = new StructuredDataKey<>("map_decorations", Type.COMPOUND_TAG);
    public static final StructuredDataKey<Integer> MAP_POST_PROCESSING = new StructuredDataKey<>("map_post_processing", Type.VAR_INT);
    public static final StructuredDataKey<Item[]> CHARGED_PROJECTILES = new StructuredDataKey<>("charged_projectiles", Types1_20_5.ITEM_ARRAY);
    public static final StructuredDataKey<Item[]> BUNDLE_CONTENTS = new StructuredDataKey<>("bundle_contents", Types1_20_5.ITEM_ARRAY);
    public static final StructuredDataKey<PotionContents> POTION_CONTENTS = new StructuredDataKey<>("potion_contents", PotionContents.TYPE);
    public static final StructuredDataKey<SuspiciousStewEffect[]> SUSPICIOUS_STEW_EFFECTS = new StructuredDataKey<>("suspicious_stew_effects", SuspiciousStewEffect.ARRAY_TYPE);
    public static final StructuredDataKey<FilterableString[]> WRITABLE_BOOK_CONTENT = new StructuredDataKey<>("writable_book_content", FilterableString.ARRAY_TYPE);
    public static final StructuredDataKey<WrittenBook> WRITTEN_BOOK_CONTENT = new StructuredDataKey<>("written_book_content", WrittenBook.TYPE);
    public static final StructuredDataKey<ArmorTrim> TRIM = new StructuredDataKey<>("trim", ArmorTrim.TYPE);
    public static final StructuredDataKey<CompoundTag> DEBUG_STICK_STATE = new StructuredDataKey<>("debug_stick_state", Type.COMPOUND_TAG);
    public static final StructuredDataKey<CompoundTag> ENTITY_DATA = new StructuredDataKey<>("entity_data", Type.COMPOUND_TAG);
    public static final StructuredDataKey<CompoundTag> BUCKET_ENTITY_DATA = new StructuredDataKey<>("bucket_entity_data", Type.COMPOUND_TAG);
    public static final StructuredDataKey<CompoundTag> BLOCK_ENTITY_DATA = new StructuredDataKey<>("block_entity_data", Type.COMPOUND_TAG);
    public static final StructuredDataKey<Holder<Instrument>> INSTRUMENT = new StructuredDataKey<>("instrument", Instrument.TYPE);
    public static final StructuredDataKey<Integer> OMINOUS_BOTTLE_AMPLIFIER = new StructuredDataKey<>("ominous_bottle_amplifier", Type.VAR_INT);
    public static final StructuredDataKey<Tag> RECIPES = new StructuredDataKey<>("recipes", Type.TAG);
    public static final StructuredDataKey<LodestoneTracker> LODESTONE_TRACKER = new StructuredDataKey<>("lodestone_tracker", LodestoneTracker.TYPE);
    public static final StructuredDataKey<FireworkExplosion> FIREWORK_EXPLOSION = new StructuredDataKey<>("firework_explosion", FireworkExplosion.TYPE);
    public static final StructuredDataKey<Fireworks> FIREWORKS = new StructuredDataKey<>("fireworks", Fireworks.TYPE);
    public static final StructuredDataKey<GameProfile> PROFILE = new StructuredDataKey<>("profile", Type.GAME_PROFILE);
    public static final StructuredDataKey<String> NOTE_BLOCK_SOUND = new StructuredDataKey<>("note_block_sound", Type.STRING);
    public static final StructuredDataKey<BannerPatternLayer[]> BANNER_PATTERNS = new StructuredDataKey<>("banner_patterns", BannerPatternLayer.ARRAY_TYPE);
    public static final StructuredDataKey<Integer> BASE_COLOR = new StructuredDataKey<>("base_color", Type.VAR_INT);
    public static final StructuredDataKey<PotDecorations> POT_DECORATIONS = new StructuredDataKey<>("pot_decorations", PotDecorations.TYPE);
    public static final StructuredDataKey<Item[]> CONTAINER = new StructuredDataKey<>("container", Types1_20_5.ITEM_ARRAY);
    public static final StructuredDataKey<BlockStateProperties> BLOCK_STATE = new StructuredDataKey<>("block_state", BlockStateProperties.TYPE);
    public static final StructuredDataKey<Bee[]> BEES = new StructuredDataKey<>("bees", Bee.ARRAY_TYPE);
    public static final StructuredDataKey<Tag> LOCK = new StructuredDataKey<>("lock", Type.TAG);
    public static final StructuredDataKey<CompoundTag> CONTAINER_LOOT = new StructuredDataKey<>("container_loot", Type.COMPOUND_TAG);

    private final String identifier;
    private final Type<T> type;

    public StructuredDataKey(final String identifier, final Type<T> type) {
        this.identifier = identifier;
        this.type = type;
    }

    public Type<T> type() {
        return type;
    }

    public String identifier() {
        return identifier;
    }

    @Override
    public String toString() {
        return "StructuredDataKey{" +
            "identifier='" + identifier + '\'' +
            ", type=" + type +
            '}';
    }
}
