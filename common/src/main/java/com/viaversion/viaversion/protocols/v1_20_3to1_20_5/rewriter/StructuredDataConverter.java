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
package com.viaversion.viaversion.protocols.v1_20_3to1_20_5.rewriter;

import com.google.common.base.Preconditions;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.FloatTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.GameProfile;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.AdventureModePredicate;
import com.viaversion.viaversion.api.minecraft.item.data.ArmorTrimMaterial;
import com.viaversion.viaversion.api.minecraft.item.data.ArmorTrimPattern;
import com.viaversion.viaversion.api.minecraft.item.data.AttributeModifiers1_20_5.AttributeModifier;
import com.viaversion.viaversion.api.minecraft.item.data.BannerPattern;
import com.viaversion.viaversion.api.minecraft.item.data.BannerPatternLayer;
import com.viaversion.viaversion.api.minecraft.item.data.Bee;
import com.viaversion.viaversion.api.minecraft.item.data.BlockPredicate;
import com.viaversion.viaversion.api.minecraft.item.data.Enchantments;
import com.viaversion.viaversion.api.minecraft.item.data.FilterableComponent;
import com.viaversion.viaversion.api.minecraft.item.data.FilterableString;
import com.viaversion.viaversion.api.minecraft.item.data.FireworkExplosion;
import com.viaversion.viaversion.api.minecraft.item.data.FoodProperties1_20_5.FoodEffect;
import com.viaversion.viaversion.api.minecraft.item.data.Instrument1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.PotionEffect;
import com.viaversion.viaversion.api.minecraft.item.data.PotionEffectData;
import com.viaversion.viaversion.api.minecraft.item.data.StatePropertyMatcher;
import com.viaversion.viaversion.api.minecraft.item.data.SuspiciousStewEffect;
import com.viaversion.viaversion.api.minecraft.item.data.ToolRule;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.Protocol1_20_3To1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.Attributes1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.BannerPatterns1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.Enchantments1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.EquipmentSlots1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.Instruments1_20_3;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.MapDecorations1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.PotionEffects1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.Potions1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage.ArmorTrimStorage;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage.BannerPatternStorage;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.UUIDUtil;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class StructuredDataConverter {

    static final int HIDE_ENCHANTMENTS = 1;
    static final int HIDE_ATTRIBUTES = 1 << 1;
    static final int HIDE_UNBREAKABLE = 1 << 2;
    static final int HIDE_CAN_DESTROY = 1 << 3;
    static final int HIDE_CAN_PLACE_ON = 1 << 4;
    static final int HIDE_ADDITIONAL = 1 << 5;
    static final int HIDE_DYE_COLOR = 1 << 6;
    static final int HIDE_ARMOR_TRIM = 1 << 7;

    // Store invalid/inconvertible data in the backup tag and later
    // restore it when converting back to the original version
    private static final String BACKUP_TAG_KEY = "VV|DataComponents";

    // Store item ids separately to avoid copying whole data tree
    // just for the item id
    private static final String ITEM_BACKUP_TAG_KEY = "VV|Id";

    private final Map<StructuredDataKey<?>, DataConverter<?>> rewriters = new Reference2ObjectOpenHashMap<>();
    private final boolean backupInconvertibleData;

    public StructuredDataConverter(final boolean backupInconvertibleData) {
        this.backupInconvertibleData = backupInconvertibleData;
        register(StructuredDataKey.CUSTOM_DATA, (data, tag) -> {
            // Handled manually
        });
        register(StructuredDataKey.DAMAGE, (data, tag) -> tag.putInt("Damage", data));
        register(StructuredDataKey.UNBREAKABLE1_20_5, (data, tag) -> {
            tag.putBoolean("Unbreakable", true);
            if (!data.showInTooltip()) {
                putHideFlag(tag, HIDE_UNBREAKABLE);
            }
        });
        register(StructuredDataKey.CUSTOM_NAME, (data, tag) -> getDisplayTag(tag).putString("Name", ComponentUtil.tagToJsonString(data)));
        register(StructuredDataKey.ITEM_NAME, (data, tag) -> {
            final CompoundTag displayTag = getDisplayTag(tag);
            if (!displayTag.contains("Name")) {
                CompoundTag name = new CompoundTag();
                name.putBoolean("italic", false);
                name.putString("text", "");
                name.put("extra", new ListTag<>(Collections.singletonList(data)));
                displayTag.putString("Name", ComponentUtil.tagToJsonString(name));
            }
        });
        register(StructuredDataKey.LORE, (data, tag) -> {
            final ListTag<StringTag> lore = new ListTag<>(StringTag.class);
            for (final Tag loreEntry : data) {
                lore.add(new StringTag(ComponentUtil.tagToJsonString(loreEntry)));
            }
            getDisplayTag(tag).put("Lore", lore);
        });
        register(StructuredDataKey.ENCHANTMENTS1_20_5, (data, tag) -> convertEnchantments(data, tag, false));
        register(StructuredDataKey.STORED_ENCHANTMENTS1_20_5, (data, tag) -> convertEnchantments(data, tag, true));
        register(StructuredDataKey.ATTRIBUTE_MODIFIERS1_20_5, (data, tag) -> {
            final ListTag<CompoundTag> modifiers = new ListTag<>(CompoundTag.class);
            for (int i = 0; i < data.modifiers().length; i++) {
                final AttributeModifier modifier = data.modifiers()[i];
                final String identifier = Attributes1_20_5.idToKey(modifier.attribute());
                if (identifier == null) {
                    continue;
                }

                final CompoundTag modifierTag = new CompoundTag();
                modifierTag.put("UUID", new IntArrayTag(UUIDUtil.toIntArray(modifier.modifier().uuid())));
                modifierTag.putString("AttributeName", identifier.equals("generic.jump_strength") ? "horse.jump_strength" : identifier);
                modifierTag.putString("Name", modifier.modifier().name());
                modifierTag.putDouble("Amount", modifier.modifier().amount());
                if (modifier.slotType() != 0) {
                    modifierTag.putString("Slot", EquipmentSlots1_20_5.idToKey(modifier.slotType()));
                }
                modifierTag.putInt("Operation", modifier.modifier().operation());
                modifiers.add(modifierTag);
            }
            tag.put("AttributeModifiers", modifiers);

            if (!data.showInTooltip()) {
                putHideFlag(tag, HIDE_ATTRIBUTES);
            }
        });
        register(StructuredDataKey.CUSTOM_MODEL_DATA1_20_5, (data, tag) -> tag.putInt("CustomModelData", data));
        register(StructuredDataKey.HIDE_ADDITIONAL_TOOLTIP, (data, tag) -> putHideFlag(tag, 0x20));
        register(StructuredDataKey.REPAIR_COST, (data, tag) -> tag.putInt("RepairCost", data));
        register(StructuredDataKey.DYED_COLOR1_20_5, (data, tag) -> {
            getDisplayTag(tag).putInt("color", data.rgb());
            if (!data.showInTooltip()) {
                putHideFlag(tag, HIDE_DYE_COLOR);
            }
        });
        register(StructuredDataKey.MAP_COLOR, (data, tag) -> getDisplayTag(tag).putInt("MapColor", data));
        register(StructuredDataKey.MAP_ID, (data, tag) -> tag.putInt("map", data));
        register(StructuredDataKey.MAP_DECORATIONS, (data, tag) -> {
            final ListTag<CompoundTag> decorations = new ListTag<>(CompoundTag.class);
            for (final Map.Entry<String, Tag> entry : data.entrySet()) {
                final CompoundTag decorationTag = (CompoundTag) entry.getValue();
                final int id = MapDecorations1_20_5.keyToId(decorationTag.getString("type"));
                if (id == -1) {
                    continue;
                }

                final CompoundTag convertedDecoration = new CompoundTag();
                convertedDecoration.putString("id", entry.getKey());
                convertedDecoration.putInt("type", id); // Write the id even if it is a new 1.20.5 one
                convertedDecoration.putDouble("x", decorationTag.getDouble("x"));
                convertedDecoration.putDouble("z", decorationTag.getDouble("z"));
                convertedDecoration.putFloat("rot", decorationTag.getFloat("rotation"));
                decorations.add(convertedDecoration);
            }
            tag.put("Decorations", decorations);
        });
        register(StructuredDataKey.WRITABLE_BOOK_CONTENT, (data, tag) -> {
            final ListTag<StringTag> pages = new ListTag<>(StringTag.class);
            final CompoundTag filteredPages = new CompoundTag();
            for (int i = 0; i < data.pages().length; i++) {
                final FilterableString page = data.pages()[i];
                pages.add(new StringTag(page.raw()));
                if (page.filtered() != null) {
                    filteredPages.putString(Integer.toString(i), page.filtered());
                }
            }
            tag.put("pages", pages);
            tag.put("filtered_pages", filteredPages);
        });
        register(StructuredDataKey.WRITTEN_BOOK_CONTENT, (data, tag) -> {
            final ListTag<StringTag> pages = new ListTag<>(StringTag.class);
            final CompoundTag filteredPages = new CompoundTag();
            for (int i = 0; i < data.pages().length; i++) {
                final FilterableComponent page = data.pages()[i];
                pages.add(new StringTag(ComponentUtil.tagToJsonString(page.raw())));
                if (page.filtered() != null) {
                    filteredPages.putString(Integer.toString(i), ComponentUtil.tagToJsonString(page.filtered()));
                }
            }
            tag.put("pages", pages);
            tag.put("filtered_pages", filteredPages);

            tag.putString("author", data.author());
            tag.putInt("generation", data.generation());
            tag.putBoolean("resolved", data.resolved());
            tag.putString("title", data.title().raw());
            if (data.title().filtered() != null) {
                tag.putString("filtered_title", data.title().filtered());
            }
        });
        register(StructuredDataKey.BASE_COLOR, (data, tag) -> getBlockEntityTag(tag).putInt("Base", data));
        register(StructuredDataKey.V1_20_5.chargedProjectiles, (connection, data, tag) -> {
            convertItemList(connection, data, tag, "ChargedProjectiles");
            if (data.length != 0) {
                tag.putBoolean("Charged", true);
            }
        });
        register(StructuredDataKey.V1_20_5.bundleContents, (connection, data, tag) -> convertItemList(connection, data, tag, "Items"));
        register(StructuredDataKey.LODESTONE_TRACKER, (data, tag) -> {
            tag.putBoolean("LodestoneTracked", data.tracked());
            if (data.position() != null) {
                final CompoundTag positionTag = new CompoundTag();
                positionTag.putInt("X", data.position().x());
                positionTag.putInt("Y", data.position().y());
                positionTag.putInt("Z", data.position().z());
                tag.put("LodestonePos", positionTag);
                tag.putString("LodestoneDimension", data.position().dimension());
            } else {
                tag.putString("LodestoneDimension", "viaversion:viaversion");
            }
        });
        register(StructuredDataKey.FIREWORKS, (data, tag) -> {
            final CompoundTag fireworksTag = new CompoundTag();
            fireworksTag.putByte("Flight", (byte) data.flightDuration());
            tag.put("Fireworks", fireworksTag);

            if (data.explosions().length > 0) {
                final ListTag<CompoundTag> explosionsTag = new ListTag<>(CompoundTag.class);
                for (final FireworkExplosion explosion : data.explosions()) {
                    explosionsTag.add(convertExplosion(explosion));
                }
                fireworksTag.put("Explosions", explosionsTag);
            }
        });
        register(StructuredDataKey.FIREWORK_EXPLOSION, (data, tag) -> tag.put("Explosion", convertExplosion(data)));
        register(StructuredDataKey.PROFILE1_20_5, (data, tag) -> {
            if (data.name() != null && data.id() == null && data.properties().length == 0) {
                tag.putString("SkullOwner", data.name());
                return;
            }

            final CompoundTag profileTag = new CompoundTag();
            tag.put("SkullOwner", profileTag);
            if (data.name() != null) {
                profileTag.putString("Name", data.name());
            }
            if (data.id() != null) {
                profileTag.put("Id", new IntArrayTag(UUIDUtil.toIntArray(data.id())));
            }

            final CompoundTag propertiesTag = new CompoundTag();
            for (final GameProfile.Property property : data.properties()) {
                final ListTag<CompoundTag> values = new ListTag<>(CompoundTag.class);
                final CompoundTag propertyTag = new CompoundTag();
                propertyTag.putString("Value", property.value());
                if (property.signature() != null) {
                    propertyTag.putString("Signature", property.signature());
                }
                values.add(propertyTag);
                propertiesTag.put(property.name(), values);
            }
            profileTag.put("Properties", propertiesTag);
        });
        register(StructuredDataKey.INSTRUMENT1_20_5, (data, tag) -> {
            // Can't do anything with direct values
            if (!data.hasId()) {
                if (backupInconvertibleData) {
                    final CompoundTag backupTag = new CompoundTag();
                    final Instrument1_20_5 instrument = data.value();
                    if (instrument.soundEvent().hasId()) {
                        backupTag.putInt("sound_event", instrument.soundEvent().id());
                    } else {
                        final CompoundTag soundEventTag = new CompoundTag();
                        final SoundEvent soundEvent = instrument.soundEvent().value();
                        soundEventTag.putString("identifier", soundEvent.identifier());
                        if (soundEvent.fixedRange() != null) {
                            soundEventTag.putFloat("fixed_range", soundEvent.fixedRange());
                        }
                        backupTag.put("sound_event", soundEventTag);
                    }
                    backupTag.putInt("use_duration", instrument.useDuration());
                    backupTag.putFloat("range", instrument.range());
                    getBackupTag(tag).put("instrument", backupTag);
                }
                return;
            }

            final String identifier = Instruments1_20_3.idToKey(data.id());
            if (identifier != null) {
                tag.putString("instrument", identifier);
            }
        });
        register(StructuredDataKey.BEES1_20_5, (data, tag) -> {
            final ListTag<CompoundTag> bees = new ListTag<>(CompoundTag.class);
            for (final Bee bee : data) {
                final CompoundTag beeTag = new CompoundTag();
                beeTag.put("EntityData", bee.entityData().tag());
                beeTag.putInt("TicksInHive", bee.ticksInHive());
                beeTag.putInt("MinOccupationTicks", bee.minTicksInHive());
                bees.add(beeTag);
            }
            getBlockEntityTag(tag, "beehive").put("Bees", bees);
        });
        register(StructuredDataKey.LOCK1_20_5, (data, tag) -> getBlockEntityTag(tag).put("Lock", data));
        register(StructuredDataKey.NOTE_BLOCK_SOUND, (data, tag) -> getBlockEntityTag(tag, "player_head").putString("note_block_sound", data.original()));
        register(StructuredDataKey.POT_DECORATIONS, (data, tag) -> {
            IntArrayTag originalSherds = null;

            final ListTag<StringTag> sherds = new ListTag<>(StringTag.class);
            for (final int id : data.itemIds()) {
                final String name = toMappedItemName(id);
                if (name.isEmpty()) {
                    // Backup whole data if one of the entries is inconvertible
                    // Since we don't want to break the order of the entries
                    if (backupInconvertibleData && originalSherds == null) {
                        originalSherds = new IntArrayTag(data.itemIds());
                    }
                    continue;
                }
                sherds.add(new StringTag(name));
            }
            if (originalSherds != null) {
                getBackupTag(tag).put("pot_decorations", originalSherds);
            }
            getBlockEntityTag(tag, "decorated_pot").put("sherds", sherds);
        });
        register(StructuredDataKey.DEBUG_STICK_STATE, (data, tag) -> tag.put("DebugProperty", data.tag()));
        register(StructuredDataKey.RECIPES, (data, tag) -> tag.put("Recipes", data));
        register(StructuredDataKey.ENTITY_DATA1_20_5, (data, tag) -> tag.put("EntityTag", data));
        register(StructuredDataKey.BUCKET_ENTITY_DATA, (data, tag) -> {
            for (final String mobTagName : BlockItemPacketRewriter1_20_5.MOB_TAGS) {
                if (data.contains(mobTagName)) {
                    tag.put(mobTagName, data.get(mobTagName));
                }
            }
        });
        register(StructuredDataKey.BLOCK_ENTITY_DATA1_20_5, (data, tag) -> {
            // Handling of previously block entity tags is done using the getBlockEntityTag method
            // Merge with already added tag if needed
            final CompoundTag blockEntityTag = tag.getCompoundTag("BlockEntityTag");
            if (blockEntityTag != null) {
                blockEntityTag.putAll(data);
            } else {
                tag.put("BlockEntityTag", data);
            }
        });
        register(StructuredDataKey.CONTAINER_LOOT, (data, tag) -> {
            final Tag lootTable = data.get("loot_table");
            if (lootTable != null) {
                getBlockEntityTag(tag).put("LootTable", lootTable);
            }
            final Tag lootTableSeed = data.get("loot_table_seed");
            if (lootTableSeed != null) {
                getBlockEntityTag(tag).put("LootTableSeed", lootTableSeed);
            }
        });
        register(StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE, (data, tag) -> {
            if (backupInconvertibleData) {
                getBackupTag(tag).putBoolean("enchantment_glint_override", data);
            }
            if (!data) {
                // There is no way to remove the glint without removing the enchantments
                // which would lead to broken data, so we just don't do anything
                return;
            }

            // If the glint is overridden, we just add an invalid enchantment to the existing list
            ListTag<CompoundTag> enchantmentsTag = tag.getListTag("Enchantments", CompoundTag.class);
            if (enchantmentsTag == null) {
                enchantmentsTag = new ListTag<>(CompoundTag.class);
                tag.put("Enchantments", enchantmentsTag);
            } else if (!enchantmentsTag.isEmpty()) {
                // If there already are enchantments, we don't need to add an invalid one
                return;
            }

            final CompoundTag invalidEnchantment = new CompoundTag();
            invalidEnchantment.putString("id", "");
            // Skipping the level tag, causing the enchantment to be invalid

            enchantmentsTag.add(invalidEnchantment);
        });
        register(StructuredDataKey.POTION_CONTENTS1_20_5, (data, tag) -> {
            if (data.potion() != null) {
                final String potion = Potions1_20_5.idToKey(data.potion()); // Include 1.20.5 names
                if (potion != null) {
                    tag.putString("Potion", potion);
                }
            }
            if (data.customColor() != null) {
                tag.putInt("CustomPotionColor", data.customColor());
            }

            final ListTag<CompoundTag> customPotionEffectsTag = new ListTag<>(CompoundTag.class);
            for (final PotionEffect effect : data.customEffects()) {
                final CompoundTag effectTag = new CompoundTag();
                final String id = PotionEffects1_20_5.idToKey(effect.effect());
                if (id != null) {
                    effectTag.putString("id", id); // Include 1.20.5 ids
                }

                final PotionEffectData details = effect.effectData();
                effectTag.putByte("amplifier", (byte) details.amplifier());
                effectTag.putInt("duration", details.duration());
                effectTag.putBoolean("ambient", details.ambient());
                effectTag.putBoolean("show_particles", details.showParticles());
                effectTag.putBoolean("show_icon", details.showIcon());

                customPotionEffectsTag.add(effectTag);
            }
            tag.put("custom_potion_effects", customPotionEffectsTag);
        });
        register(StructuredDataKey.SUSPICIOUS_STEW_EFFECTS, (data, tag) -> {
            final ListTag<CompoundTag> effectsTag = new ListTag<>(CompoundTag.class);
            for (final SuspiciousStewEffect effect : data) {
                final CompoundTag effectTag = new CompoundTag();
                final String id = PotionEffects1_20_5.idToKey(effect.mobEffect());
                if (id != null) {
                    effectTag.putString("id", id); // Include 1.20.5 ids
                }
                effectTag.putInt("duration", effect.duration());

                effectsTag.add(effectTag);
            }
            tag.put("effects", effectsTag);
        });
        register(StructuredDataKey.BANNER_PATTERNS, (connection, data, tag) -> {
            final BannerPatternStorage patternStorage = connection.get(BannerPatternStorage.class);
            if (backupInconvertibleData) {
                // Backup whole data if one of the entries is inconvertible
                // Since we don't want to break the order of the entries
                if (Arrays.stream(data).anyMatch(layer -> {
                    if (layer.pattern().isDirect()) {
                        return true;
                    }

                    final int id = layer.pattern().id();
                    final String identifier = patternStorage != null ? patternStorage.pattern(id) : BannerPatterns1_20_5.idToKey(id);
                    return identifier == null || identifier.equals("flow") || identifier.equals("guster");
                })) {
                    final ListTag<CompoundTag> originalPatterns = new ListTag<>(CompoundTag.class);
                    for (final BannerPatternLayer layer : data) {
                        final CompoundTag layerTag = new CompoundTag();
                        if (layer.pattern().isDirect()) {
                            final CompoundTag patternTag = new CompoundTag();
                            final BannerPattern pattern = layer.pattern().value();
                            patternTag.putString("asset_id", pattern.assetId());
                            patternTag.putString("translation_key", pattern.translationKey());
                            layerTag.put("pattern", patternTag);
                        } else {
                            layerTag.putInt("pattern", layer.pattern().id());
                        }

                        layerTag.putInt("dye_color", layer.dyeColor());
                        originalPatterns.add(layerTag);
                    }
                    getBackupTag(tag).put("banner_patterns", originalPatterns);
                }
            }

            final ListTag<CompoundTag> patternsTag = new ListTag<>(CompoundTag.class);
            for (final BannerPatternLayer layer : data) {
                if (layer.pattern().isDirect()) {
                    continue;
                }

                final int id = layer.pattern().id();
                final String key = patternStorage != null ? patternStorage.pattern(id) : BannerPatterns1_20_5.idToKey(id);
                if (key == null) {
                    continue;
                }

                final String compactKey = BannerPatterns1_20_5.fullIdToCompact(key);
                if (compactKey == null) {
                    continue;
                }

                final CompoundTag patternTag = new CompoundTag();
                patternTag.putString("Pattern", compactKey);
                patternTag.putInt("Color", layer.dyeColor());
                patternsTag.add(patternTag);
            }
            getBlockEntityTag(tag, "banner").put("Patterns", patternsTag);
        });
        register(StructuredDataKey.V1_20_5.container, (connection, data, tag) -> convertItemList(connection, data, getBlockEntityTag(tag), "Items"));
        register(StructuredDataKey.CAN_PLACE_ON1_20_5, (data, tag) -> convertBlockPredicates(tag, data, "CanPlaceOn", HIDE_CAN_PLACE_ON));
        register(StructuredDataKey.CAN_BREAK1_20_5, (data, tag) -> convertBlockPredicates(tag, data, "CanDestroy", HIDE_CAN_DESTROY));
        register(StructuredDataKey.TRIM1_20_5, (connection, data, tag) -> {
            final CompoundTag trimTag = new CompoundTag();
            final ArmorTrimStorage trimStorage = connection.get(ArmorTrimStorage.class);
            if (data.material().isDirect()) {
                final CompoundTag materialTag = new CompoundTag();
                final ArmorTrimMaterial material = data.material().value();
                materialTag.putString("asset_name", material.assetName());

                final String ingredientName = toMappedItemName(material.itemId());
                if (backupInconvertibleData && ingredientName.isEmpty()) {
                    getBackupTag(materialTag).putInt(ITEM_BACKUP_TAG_KEY, material.itemId());
                }
                materialTag.putString("ingredient", ingredientName);
                materialTag.put("item_model_index", new FloatTag(material.itemModelIndex()));

                final CompoundTag overrideArmorMaterials = new CompoundTag();
                if (!material.overrideArmorMaterials().isEmpty()) {
                    for (final Map.Entry<String, String> entry : material.overrideArmorMaterials().entrySet()) {
                        overrideArmorMaterials.put(entry.getKey(), new StringTag(entry.getValue()));
                    }
                    materialTag.put("override_armor_materials", overrideArmorMaterials);
                }
                materialTag.put("description", material.description());
                trimTag.put("material", materialTag);
            } else {
                final String oldKey = trimStorage.trimMaterials().idToKey(data.material().id());
                if (oldKey != null) {
                    trimTag.putString("material", oldKey);
                }
            }
            if (data.pattern().isDirect()) {
                final CompoundTag patternTag = new CompoundTag();
                final ArmorTrimPattern pattern = data.pattern().value();

                patternTag.putString("assetId", pattern.assetName());
                final String itemName = toMappedItemName(pattern.itemId());
                if (backupInconvertibleData && itemName.isEmpty()) {
                    getBackupTag(patternTag).putInt(ITEM_BACKUP_TAG_KEY, pattern.itemId());
                }
                patternTag.putString("templateItem", itemName);
                patternTag.put("description", pattern.description());
                patternTag.putBoolean("decal", pattern.decal());
                trimTag.put("pattern", patternTag);
            } else {
                final String oldKey = trimStorage.trimPatterns().idToKey(data.pattern().id());
                if (oldKey != null) {
                    trimTag.putString("pattern", oldKey);
                }
            }
            tag.put("Trim", trimTag);
            if (!data.showInTooltip()) {
                putHideFlag(tag, HIDE_ARMOR_TRIM);
            }
        });
        register(StructuredDataKey.BLOCK_STATE, ((data, tag) -> {
            final CompoundTag blockStateTag = new CompoundTag();
            tag.put("BlockStateTag", blockStateTag);
            for (final Map.Entry<String, String> entry : data.properties().entrySet()) {
                blockStateTag.putString(entry.getKey(), entry.getValue());
            }
        }));
        register(StructuredDataKey.HIDE_TOOLTIP, (data, tag) -> {
            // Hide everything we can hide
            putHideFlag(tag, 0xFF);
            if (backupInconvertibleData) {
                getBackupTag(tag).putBoolean("hide_tooltip", true);
            }
        });

        // New in 1.20.5
        register(StructuredDataKey.INTANGIBLE_PROJECTILE, (data, tag) -> {
            if (backupInconvertibleData) {
                getBackupTag(tag).put("intangible_projectile", data);
            }
        });
        register(StructuredDataKey.MAX_STACK_SIZE, (data, tag) -> {
            if (backupInconvertibleData) {
                getBackupTag(tag).putInt("max_stack_size", data);
            }
        });
        register(StructuredDataKey.MAX_DAMAGE, (data, tag) -> {
            if (backupInconvertibleData) {
                getBackupTag(tag).putInt("max_damage", data);
            }
        });
        register(StructuredDataKey.RARITY, (data, tag) -> {
            if (backupInconvertibleData) {
                getBackupTag(tag).putInt("rarity", data);
            }
        });
        register(StructuredDataKey.FOOD1_20_5, (data, tag) -> {
            if (backupInconvertibleData) {
                final CompoundTag backupTag = new CompoundTag();
                backupTag.putInt("nutrition", data.nutrition());
                backupTag.putFloat("saturation_modifier", data.saturationModifier());
                backupTag.putBoolean("can_always_eat", data.canAlwaysEat());
                backupTag.putFloat("eat_seconds", data.eatSeconds());

                final ListTag<CompoundTag> possibleEffectsTag = new ListTag<>(CompoundTag.class);
                for (final FoodEffect effect : data.possibleEffects()) {
                    final CompoundTag effectTag = new CompoundTag();

                    final PotionEffect potionEffect = effect.effect();
                    final CompoundTag potionEffectTag = new CompoundTag();
                    potionEffectTag.putInt("effect", potionEffect.effect());
                    potionEffectTag.put("effect_data", convertPotionEffectData(potionEffect.effectData()));

                    effectTag.putFloat("probability", effect.probability());
                    effectTag.put("effect", potionEffectTag);
                    possibleEffectsTag.add(effectTag);
                }
                backupTag.put("possible_effects", possibleEffectsTag);
                getBackupTag(tag).put("food", backupTag);
            }
        });
        register(StructuredDataKey.FIRE_RESISTANT, (data, tag) -> {
            if (backupInconvertibleData) {
                getBackupTag(tag).putBoolean("fire_resistant", true);
            }
        });
        register(StructuredDataKey.TOOL1_20_5, (data, tag) -> {
            if (backupInconvertibleData) {
                final CompoundTag backupTag = new CompoundTag();
                final ListTag<CompoundTag> rulesTag = new ListTag<>(CompoundTag.class);
                for (final ToolRule rule : data.rules()) {
                    final CompoundTag ruleTag = new CompoundTag();
                    final HolderSet set = rule.blocks();
                    if (set.hasTagKey()) {
                        ruleTag.putString("blocks", set.tagKey());
                    } else {
                        ruleTag.put("blocks", new IntArrayTag(set.ids()));
                    }
                    if (rule.speed() != null) {
                        ruleTag.putFloat("speed", rule.speed());
                    }
                    if (rule.correctForDrops() != null) {
                        ruleTag.putBoolean("correct_for_drops", rule.correctForDrops());
                    }
                    rulesTag.add(ruleTag);
                }
                backupTag.put("rules", rulesTag);
                backupTag.putFloat("default_mining_speed", data.defaultMiningSpeed());
                backupTag.putInt("damage_per_block", data.damagePerBlock());
                getBackupTag(tag).put("tool", backupTag);
            }
        });
        register(StructuredDataKey.OMINOUS_BOTTLE_AMPLIFIER, (data, tag) -> {
            if (backupInconvertibleData) {
                getBackupTag(tag).putInt("ominous_bottle_amplifier", data);
            }
        });
    }

    private int unmappedItemId(final int id) {
        return Protocol1_20_3To1_20_5.MAPPINGS.getOldItemId(id);
    }

    private String toMappedItemName(final int id) {
        final int mappedId = unmappedItemId(id);
        return mappedId != -1 ? Protocol1_20_3To1_20_5.MAPPINGS.getFullItemMappings().identifier(mappedId) : "";
    }

    private static CompoundTag getBlockEntityTag(final CompoundTag tag) {
        return getOrCreate(tag, "BlockEntityTag");
    }

    private CompoundTag getBlockEntityTag(final CompoundTag tag, final String blockEntity) {
        final CompoundTag blockEntityTag = getOrCreate(tag, "BlockEntityTag");
        if (!blockEntityTag.contains("id")) {
            // Add in the assumed (and required) block entity id
            blockEntityTag.putString("id", blockEntity);
        }
        return blockEntityTag;
    }

    private static CompoundTag getDisplayTag(final CompoundTag tag) {
        return getOrCreate(tag, "display");
    }

    private static CompoundTag getBackupTag(final CompoundTag tag) {
        return getOrCreate(tag, BACKUP_TAG_KEY);
    }

    // If multiple item components which previously were stored in BlockEntityTag are present, we need to merge them
    private static CompoundTag getOrCreate(final CompoundTag tag, final String key) {
        CompoundTag subTag = tag.getCompoundTag(key);
        if (subTag == null) {
            subTag = new CompoundTag();
            tag.put(key, subTag);
        }
        return subTag;
    }

    public static @Nullable CompoundTag removeBackupTag(final CompoundTag tag) {
        final CompoundTag backupTag = tag.getCompoundTag(BACKUP_TAG_KEY);
        if (backupTag != null) {
            tag.remove(BACKUP_TAG_KEY);
        }
        return backupTag;
    }

    // Check if item name could be mapped, if not, locate original/backup item id and use it
    static int removeItemBackupTag(final CompoundTag tag, final int unmappedId) {
        final IntTag itemBackupTag = tag.getIntTag(ITEM_BACKUP_TAG_KEY);
        if (itemBackupTag != null) {
            tag.remove(ITEM_BACKUP_TAG_KEY);
            return itemBackupTag.asInt();
        }
        return unmappedId;
    }

    private void convertBlockPredicates(final CompoundTag tag, final AdventureModePredicate data, final String key, final int hideFlag) {
        final ListTag<StringTag> predicatedListTag = new ListTag<>(StringTag.class);
        for (final BlockPredicate predicate : data.predicates()) {
            final HolderSet holders = predicate.holderSet();
            if (holders == null) {
                // Can't do (nicely)
                if (backupInconvertibleData) {
                    // TODO Backup
                }
                continue;
            }
            if (holders.hasTagKey()) {
                final String tagKey = "#" + holders.tagKey();
                predicatedListTag.add(serializeBlockPredicate(predicate, tagKey));
            } else {
                for (final int id : holders.ids()) {
                    final String name = Protocol1_20_3To1_20_5.MAPPINGS.blockName(id);
                    if (name == null) {
                        if (backupInconvertibleData) {
                            // TODO Backup
                        }
                        continue;
                    }
                    predicatedListTag.add(serializeBlockPredicate(predicate, name));
                }
            }
        }

        tag.put(key, predicatedListTag);
        if (!data.showInTooltip()) {
            putHideFlag(tag, hideFlag);
        }
    }

    private StringTag serializeBlockPredicate(final BlockPredicate predicate, final String identifier) {
        final StringBuilder builder = new StringBuilder(identifier);
        if (predicate.propertyMatchers() != null) {
            for (final StatePropertyMatcher matcher : predicate.propertyMatchers()) {
                // Ranges were introduced in 1.20.5, so only handle the simple case
                if (matcher.matcher().isLeft()) {
                    builder.append(matcher.name()).append('=');
                    builder.append(matcher.matcher().left());
                }
            }
        }
        if (predicate.tag() != null) {
            builder.append(predicate.tag());
        }
        return new StringTag(builder.toString());
    }

    private CompoundTag convertExplosion(final FireworkExplosion explosion) {
        final CompoundTag explosionTag = new CompoundTag();
        explosionTag.putInt("Type", explosion.shape());
        explosionTag.put("Colors", new IntArrayTag(explosion.colors().clone()));
        explosionTag.put("FadeColors", new IntArrayTag(explosion.fadeColors().clone()));
        explosionTag.putBoolean("Trail", explosion.hasTrail());
        explosionTag.putBoolean("Flicker", explosion.hasTwinkle());
        return explosionTag;
    }

    private CompoundTag convertPotionEffectData(final PotionEffectData data) {
        final CompoundTag effectDataTag = new CompoundTag();
        effectDataTag.putInt("amplifier", data.amplifier());
        effectDataTag.putInt("duration", data.duration());
        effectDataTag.putBoolean("ambient", data.ambient());
        effectDataTag.putBoolean("show_particles", data.showParticles());
        effectDataTag.putBoolean("show_icon", data.showIcon());
        if (data.hiddenEffect() != null) {
            effectDataTag.put("hidden_effect", convertPotionEffectData(data.hiddenEffect()));
        }
        return effectDataTag;
    }

    private void convertItemList(final UserConnection connection, final Item[] items, final CompoundTag tag, final String key) {
        final ListTag<CompoundTag> itemsTag = new ListTag<>(CompoundTag.class);
        for (int i = 0; i < items.length; i++) {
            final Item item = items[i];
            final CompoundTag savedItem = itemToTag(connection, item);
            // 1.20.4 clients need the Slot to display the item correctly
            if (backupInconvertibleData) {
                savedItem.putByte("Slot", (byte) i);
            }
            itemsTag.add(savedItem);
        }
        tag.put(key, itemsTag);
    }

    private CompoundTag itemToTag(final UserConnection connection, final Item item) {
        final CompoundTag savedItem = new CompoundTag();
        if (item != null) {
            final String name = toMappedItemName(item.identifier());
            savedItem.putString("id", name);
            if (backupInconvertibleData && name.isEmpty()) {
                savedItem.putInt(ITEM_BACKUP_TAG_KEY, item.identifier());
            }
            savedItem.putByte("Count", (byte) item.amount());

            final CompoundTag itemTag = new CompoundTag();
            for (final StructuredData<?> data : item.dataContainer().data().values()) {
                writeToTag(connection, data, itemTag);
            }
            savedItem.put("tag", itemTag);
        } else {
            savedItem.putString("id", "air");
        }
        return savedItem;
    }

    private void convertEnchantments(final Enchantments data, final CompoundTag tag, final boolean storedEnchantments) {
        final ListTag<CompoundTag> enchantments = new ListTag<>(CompoundTag.class);
        for (final Int2IntMap.Entry entry : data.enchantments().int2IntEntrySet()) {
            final int enchantmentId = entry.getIntKey();
            String identifier = Enchantments1_20_5.idToKey(enchantmentId);
            if (identifier == null) {
                continue;
            }
            if (identifier.equals("density") || identifier.equals("breach") || identifier.equals("wind_burst")) {
                // New ones, backed up by VB
                continue;
            }

            if (identifier.equals("sweeping_edge")) {
                identifier = "sweeping";
            }

            final CompoundTag enchantment = new CompoundTag();
            enchantment.putString("id", identifier);
            enchantment.putShort("lvl", (short) entry.getIntValue());
            enchantments.add(enchantment);
        }
        tag.put(storedEnchantments ? "StoredEnchantments" : "Enchantments", enchantments);

        if (!data.showInTooltip()) {
            putHideFlag(tag, storedEnchantments ? HIDE_ADDITIONAL : HIDE_ENCHANTMENTS);
        }
    }

    private void putHideFlag(final CompoundTag tag, final int value) {
        tag.putInt("HideFlags", tag.getInt("HideFlags") | value);
    }

    public <T> void writeToTag(final UserConnection connection, final StructuredData<T> data, final CompoundTag tag) {
        if (data.isEmpty()) {
            return;
        }

        //noinspection unchecked
        final DataConverter<T> converter = (DataConverter<T>) rewriters.get(data.key());
        Preconditions.checkNotNull(converter, "No converter for %s found", data.key());
        converter.convert(connection, data.value(), tag);
    }

    private <T> void register(final StructuredDataKey<T> key, final DataConverter<T> converter) {
        rewriters.put(key, converter);
    }

    private <T> void register(final StructuredDataKey<T> key, final SimpleDataConverter<T> converter) {
        final DataConverter<T> c = (connection, data, tag) -> converter.convert(data, tag);
        rewriters.put(key, c);
    }

    public boolean backupInconvertibleData() {
        return backupInconvertibleData;
    }

    @FunctionalInterface
    interface SimpleDataConverter<T> {

        void convert(T data, CompoundTag tag);
    }

    @FunctionalInterface
    interface DataConverter<T> {

        void convert(UserConnection connection, T data, CompoundTag tag);
    }
}
