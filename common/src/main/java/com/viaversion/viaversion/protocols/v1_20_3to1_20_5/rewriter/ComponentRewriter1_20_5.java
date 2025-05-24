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
import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.FloatTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.GameProfile;
import com.viaversion.viaversion.api.minecraft.GlobalBlockPosition;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.minecraft.item.data.AdventureModePredicate;
import com.viaversion.viaversion.api.minecraft.item.data.ArmorTrim;
import com.viaversion.viaversion.api.minecraft.item.data.ArmorTrimMaterial;
import com.viaversion.viaversion.api.minecraft.item.data.ArmorTrimPattern;
import com.viaversion.viaversion.api.minecraft.item.data.AttributeModifiers1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.AttributeModifiers1_20_5.AttributeModifier;
import com.viaversion.viaversion.api.minecraft.item.data.AttributeModifiers1_20_5.ModifierData;
import com.viaversion.viaversion.api.minecraft.item.data.BannerPattern;
import com.viaversion.viaversion.api.minecraft.item.data.BannerPatternLayer;
import com.viaversion.viaversion.api.minecraft.item.data.Bee;
import com.viaversion.viaversion.api.minecraft.item.data.BlockPredicate;
import com.viaversion.viaversion.api.minecraft.item.data.BlockStateProperties;
import com.viaversion.viaversion.api.minecraft.item.data.DyedColor;
import com.viaversion.viaversion.api.minecraft.item.data.Enchantments;
import com.viaversion.viaversion.api.minecraft.item.data.FilterableComponent;
import com.viaversion.viaversion.api.minecraft.item.data.FilterableString;
import com.viaversion.viaversion.api.minecraft.item.data.FireworkExplosion;
import com.viaversion.viaversion.api.minecraft.item.data.Fireworks;
import com.viaversion.viaversion.api.minecraft.item.data.FoodProperties1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.Instrument1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.LodestoneTracker;
import com.viaversion.viaversion.api.minecraft.item.data.PotDecorations;
import com.viaversion.viaversion.api.minecraft.item.data.PotionContents;
import com.viaversion.viaversion.api.minecraft.item.data.PotionEffect;
import com.viaversion.viaversion.api.minecraft.item.data.PotionEffectData;
import com.viaversion.viaversion.api.minecraft.item.data.StatePropertyMatcher;
import com.viaversion.viaversion.api.minecraft.item.data.SuspiciousStewEffect;
import com.viaversion.viaversion.api.minecraft.item.data.ToolProperties;
import com.viaversion.viaversion.api.minecraft.item.data.ToolRule;
import com.viaversion.viaversion.api.minecraft.item.data.Unbreakable;
import com.viaversion.viaversion.api.minecraft.item.data.WritableBook;
import com.viaversion.viaversion.api.minecraft.item.data.WrittenBook;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.type.types.UnsignedByteType;
import com.viaversion.viaversion.api.type.types.item.StructuredDataType;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.Protocol1_20_3To1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.ArmorMaterials1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.Attributes1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.BannerPatterns1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.DyeColors;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.Enchantments1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.EquipmentSlots1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.Instruments1_20_3;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.PotionEffects1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.Potions1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage.ArmorTrimStorage;
import com.viaversion.viaversion.rewriter.text.JsonNBTComponentRewriter;
import com.viaversion.viaversion.util.Either;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.MathUtil;
import com.viaversion.viaversion.util.SerializerVersion;
import com.viaversion.viaversion.util.StringUtil;
import com.viaversion.viaversion.util.UUIDUtil;
import com.viaversion.viaversion.util.Unit;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ComponentRewriter1_20_5<C extends ClientboundPacketType> extends JsonNBTComponentRewriter<C> {

    protected final Map<StructuredDataKey<?>, ConverterPair<?>> converters = new Reference2ObjectOpenHashMap<>();
    protected final StructuredDataType structuredDataType;

    /**
     * @param protocol           protocol
     * @param structuredDataType unmapped structured data type
     */
    public ComponentRewriter1_20_5(final Protocol<C, ?, ?, ?> protocol, final StructuredDataType structuredDataType) {
        super(protocol, ReadType.NBT);
        this.structuredDataType = structuredDataType;

        register(StructuredDataKey.CUSTOM_DATA, this::customDataToTag, this::customDataFromTag);
        register(StructuredDataKey.MAX_STACK_SIZE, this::maxStackSizeToTag, this::maxStackSizeFromTag);
        register(StructuredDataKey.MAX_DAMAGE, this::maxDamageToTag, this::maxDamageFromTag);
        register(StructuredDataKey.DAMAGE, this::damageToTag, this::damageFromTag);
        register(StructuredDataKey.UNBREAKABLE1_20_5, this::unbreakableToTag, this::unbreakableFromTag);
        register(StructuredDataKey.CUSTOM_NAME, this::customNameToTag, this::customNameFromTag);
        register(StructuredDataKey.ITEM_NAME, this::itemNameToTag, this::itemNameFromTag);
        register(StructuredDataKey.LORE, this::loreToTag, this::loreFromTag);
        register(StructuredDataKey.RARITY, this::rarityToTag, this::rarityFromTag);
        register(StructuredDataKey.ENCHANTMENTS1_20_5, this::enchantmentsToTag, this::enchantmentsFromTag);
        register(StructuredDataKey.CAN_PLACE_ON1_20_5, this::canPlaceOnToTag, this::canPlaceOnFromTag);
        register(StructuredDataKey.CAN_BREAK1_20_5, this::canBreakToTag, this::canBreakFromTag);
        register(StructuredDataKey.ATTRIBUTE_MODIFIERS1_20_5, this::attributeModifiersToTag, this::attributeModifiersFromTag);
        register(StructuredDataKey.CUSTOM_MODEL_DATA1_20_5, this::customModelDataToTag, this::customModelDataFromTag);
        register(StructuredDataKey.HIDE_ADDITIONAL_TOOLTIP, this::hideAdditionalTooltipToTag, this::hideAdditionalTooltipFromTag);
        register(StructuredDataKey.HIDE_TOOLTIP, this::hideTooltipToTag, this::hideTooltipFromTag);
        register(StructuredDataKey.REPAIR_COST, this::repairCostToTag, this::repairCostFromTag);
        register(StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE, this::enchantmentGlintOverrideToTag, this::enchantmentGlintOverrideFromTag);
        registerEmpty(StructuredDataKey.CREATIVE_SLOT_LOCK);
        register(StructuredDataKey.INTANGIBLE_PROJECTILE, this::intangibleProjectileToTag, this::intangibleProjectileFromTag);
        register(StructuredDataKey.FOOD1_20_5, this::foodToTag, this::foodFromTag);
        register(StructuredDataKey.FIRE_RESISTANT, this::fireResistantToTag, this::fireResistantFromTag);
        register(StructuredDataKey.TOOL1_20_5, this::toolToTag, this::toolFromTag);
        register(StructuredDataKey.STORED_ENCHANTMENTS1_20_5, this::storedEnchantmentsToTag, this::storedEnchantmentsFromTag);
        register(StructuredDataKey.DYED_COLOR1_20_5, this::dyedColorToTag, this::dyedColorFromTag);
        register(StructuredDataKey.MAP_COLOR, this::mapColorToTag, this::mapColorFromTag);
        register(StructuredDataKey.MAP_ID, this::mapIdToTag, this::mapIdFromTag);
        register(StructuredDataKey.MAP_DECORATIONS, this::mapDecorationsToTag, this::mapDecorationsFromTag);
        registerEmpty(StructuredDataKey.MAP_POST_PROCESSING);
        register(StructuredDataKey.V1_20_5.chargedProjectiles, this::chargedProjectilesToTag, this::chargedProjectilesFromTag);
        register(StructuredDataKey.V1_20_5.bundleContents, this::bundleContentsToTag, this::bundleContentsFromTag);
        register(StructuredDataKey.POTION_CONTENTS1_20_5, this::potionContentsToTag, this::potionContentsFromTag);
        register(StructuredDataKey.SUSPICIOUS_STEW_EFFECTS, this::suspiciousStewEffectsToTag, this::suspiciousStewEffectsFromTag);
        register(StructuredDataKey.WRITABLE_BOOK_CONTENT, this::writableBookContentToTag, this::writableBookContentFromTag);
        register(StructuredDataKey.WRITTEN_BOOK_CONTENT, this::writtenBookContentToTag, this::writtenBookContentFromTag);
        register(StructuredDataKey.TRIM1_20_5, this::trimToTag, this::trimFromTag);
        register(StructuredDataKey.DEBUG_STICK_STATE, this::debugStickRateToTag, this::debugStickRateFromTag);
        register(StructuredDataKey.ENTITY_DATA, this::entityDataToTag, this::entityDataFromTag);
        register(StructuredDataKey.BUCKET_ENTITY_DATA, this::bucketEntityDataToTag, this::bucketEntityDataFromTag);
        register(StructuredDataKey.BLOCK_ENTITY_DATA, this::blockEntityDataToTag, this::blockEntityDataFromTag);
        register(StructuredDataKey.INSTRUMENT1_20_5, this::instrumentToTag, this::instrumentFromTag);
        register(StructuredDataKey.OMINOUS_BOTTLE_AMPLIFIER, this::ominousBottleAmplifierToTag, this::ominousBottleAmplifierFromTag);
        register(StructuredDataKey.RECIPES, this::recipesToTag, this::recipesFromTag);
        register(StructuredDataKey.LODESTONE_TRACKER, this::lodestoneTrackerToTag, this::lodestoneTrackerFromTag);
        register(StructuredDataKey.FIREWORK_EXPLOSION, this::fireworkExplosionToTag, this::fireworkExplosionFromTag);
        register(StructuredDataKey.FIREWORKS, this::fireworksToTag, this::fireworksFromTag);
        register(StructuredDataKey.PROFILE, this::profileToTag, this::profileFromTag);
        register(StructuredDataKey.NOTE_BLOCK_SOUND, this::noteBlockSoundToTag, this::noteBlockSoundFromTag);
        register(StructuredDataKey.BANNER_PATTERNS, this::bannerPatternsToTag, this::bannerPatternsFromTag);
        register(StructuredDataKey.BASE_COLOR, this::baseColorToTag, this::baseColorFromTag);
        register(StructuredDataKey.POT_DECORATIONS, this::potDecorationsToTag, this::potDecorationsFromTag);
        register(StructuredDataKey.V1_20_5.container, this::containerToTag, this::containerFromTag);
        register(StructuredDataKey.BLOCK_STATE, this::blockStateToTag, this::blockStateFromTag);
        register(StructuredDataKey.BEES, this::beesToTag, this::beesFromTag);
        register(StructuredDataKey.LOCK, this::lockToTag, this::lockFromTag);
        register(StructuredDataKey.CONTAINER_LOOT, this::containerLootToTag, this::containerLootFromTag);
    }

    @Override
    protected void handleHoverEvent(final UserConnection connection, final CompoundTag hoverEventTag) {
        super.handleHoverEvent(connection, hoverEventTag);

        final StringTag actionTag = hoverEventTag.getStringTag("action");
        if (actionTag == null) {
            return;
        }
        if (actionTag.getValue().equals("show_entity")) {
            final CompoundTag contentsTag = hoverEventTag.getCompoundTag("contents");
            if (contentsTag == null) {
                return;
            }

            if (this.protocol.getMappingData().getEntityMappings().mappedId(contentsTag.getString("type")) == -1) {
                contentsTag.put("type", new StringTag("pig"));
            }
        }
    }

    @Override
    protected void handleShowItem(final UserConnection connection, final CompoundTag itemTag, final @Nullable CompoundTag componentsTag) {
        super.handleShowItem(connection, itemTag, componentsTag);

        final StringTag idTag = itemTag.getStringTag("id");
        if (idTag == null) {
            return;
        }

        int itemId = Protocol1_20_3To1_20_5.MAPPINGS.getFullItemMappings().id(idTag.getValue());
        if (itemId == -1) {
            // Default to stone (anything that is not air)
            itemId = 1;
        }

        final StringTag tag = (StringTag) itemTag.remove("tag");
        final CompoundTag tagTag;
        try {
            tagTag = tag != null ? (CompoundTag) inputSerializerVersion().toTag(tag.getValue()) : null;
        } catch (final Exception e) {
            if (!Via.getConfig().isSuppressTextComponentConversionWarnings()) {
                protocol.getLogger().log(Level.WARNING, "Error reading NBT in show_item: " + StringUtil.forLogging(itemTag), e);
            }
            return;
        }

        final Item dataItem = new DataItem();
        dataItem.setIdentifier(itemId);
        if (tagTag != null) { // We don't need to remap data if there is none
            dataItem.setTag(tagTag);
        }

        final Item structuredItem = protocol.getItemRewriter().handleItemToClient(connection, dataItem);
        if (structuredItem.amount() < 1) {
            // Cannot be empty
            structuredItem.setAmount(1);
        }

        if (structuredItem.identifier() != 0) {
            final String identifier = mappedIdentifier(structuredItem.identifier());
            if (identifier != null) {
                itemTag.putString("id", identifier);
            }
        } else {
            // Cannot be air
            itemTag.putString("id", "minecraft:stone");
        }

        final Map<StructuredDataKey<?>, StructuredData<?>> data = structuredItem.dataContainer().data();
        if (!data.isEmpty()) {
            final CompoundTag components;
            try {
                components = toTag(connection, data);
            } catch (final Exception e) {
                if (!Via.getConfig().isSuppressTextComponentConversionWarnings()) {
                    protocol.getLogger().log(Level.WARNING, "Error writing components in show_item!", e);
                }
                return;
            }
            itemTag.put("components", components);
        }
    }

    public CompoundTag toTag(final UserConnection connection, final Map<StructuredDataKey<?>, StructuredData<?>> data) {
        final CompoundTag tag = new CompoundTag();
        for (final Map.Entry<StructuredDataKey<?>, StructuredData<?>> entry : data.entrySet()) {
            final StructuredDataKey<?> key = entry.getKey();
            final String identifier = key.identifier();

            //noinspection rawtypes
            final ConverterPair converter = converters.get(key);
            if (converter == null) { // Should NOT happen
                Via.getPlatform().getLogger().severe("No converter found for data component: " + identifier);
                continue;
            }

            final StructuredData<?> value = entry.getValue();
            if (value.isEmpty()) {
                tag.put("!" + identifier, new CompoundTag());
                continue;
            }

            //noinspection unchecked
            final Tag valueTag = converter.dataConverter.convert(connection, value.value());
            if (valueTag == null) {
                continue;
            }

            tag.put(identifier, valueTag);
        }
        return tag;
    }

    public List<StructuredData<?>> toData(final UserConnection connection, final CompoundTag tag) {
        final List<StructuredData<?>> list = new ArrayList<>();
        if (tag != null) {
            for (final Map.Entry<String, Tag> entry : tag.entrySet()) {
                final StructuredData<?> data = readFromTag(connection, entry.getKey(), entry.getValue());
                list.add(data);
            }
        }
        return list;
    }

    private StructuredData<?> readFromTag(final UserConnection connection, String identifier, final Tag tag) {
        final boolean removed = identifier.startsWith("!");
        if (removed) {
            identifier = identifier.substring(1);
        }
        final int id = protocol.getMappingData().getDataComponentSerializerMappings().mappedId(identifier);
        Preconditions.checkArgument(id != -1, "Unknown data component: %s", identifier);
        final StructuredDataKey<?> key = structuredDataType.key(id);
        if (removed) {
            return StructuredData.empty(key, id);
        } else {
            return readFromTag(connection, key, id, tag);
        }
    }

    protected <T> StructuredData<T> readFromTag(final UserConnection connection, final StructuredDataKey<T> key, final int id, final Tag tag) {
        final TagConverter<T> converter = tagConverter(key);
        Preconditions.checkNotNull(converter, "No converter found for: %s", key);
        return StructuredData.of(key, converter.convert(connection, tag), id);
    }

    private String mappedIdentifier(final int id) {
        return Protocol1_20_3To1_20_5.MAPPINGS.getFullItemMappings().mappedIdentifier(id);
    }

    private int mappedId(final String identifier) {
        return Protocol1_20_3To1_20_5.MAPPINGS.getFullItemMappings().mappedId(identifier);
    }

    // ---------------------------------------------------------------------------------------

    protected CompoundTag customDataToTag(final CompoundTag value) {
        return value;
    }

    protected CompoundTag customDataFromTag(final Tag value) {
        return (CompoundTag) value;
    }

    protected IntTag maxStackSizeToTag(final Integer value) {
        return intRangeToTag(value, 1, 99);
    }

    protected Integer maxStackSizeFromTag(final Tag value) {
        return checkIntRange(1, 99, asInt(value));
    }

    protected IntTag maxDamageToTag(final Integer value) {
        return positiveIntToTag(value);
    }

    protected Integer maxDamageFromTag(final Tag value) {
        return checkPositiveInt(asInt(value));
    }

    protected IntTag damageToTag(final Integer value) {
        return nonNegativeIntToTag(value);
    }

    protected Integer damageFromTag(final Tag value) {
        return checkNonNegativeInt(asInt(value));
    }

    protected CompoundTag unbreakableToTag(final Unbreakable value) {
        final CompoundTag tag = new CompoundTag();
        if (!value.showInTooltip()) {
            tag.putBoolean("show_in_tooltip", false);
        }
        return tag;
    }

    protected Unbreakable unbreakableFromTag(final Tag tag) {
        if (tag instanceof CompoundTag compoundTag) {
            return new Unbreakable(compoundTag.getBoolean("show_in_tooltip", true));
        } else {
            return null;
        }
    }

    protected StringTag customNameToTag(final Tag value) {
        return componentToTag(value);
    }

    protected Tag customNameFromTag(final Tag value) {
        return componentFromTag(value);
    }

    protected StringTag itemNameToTag(final Tag value) {
        return componentToTag(value);
    }

    protected Tag itemNameFromTag(final Tag value) {
        return componentFromTag(value);
    }

    protected ListTag<StringTag> loreToTag(final Tag[] value) {
        return componentsToTag(value, 256);
    }

    protected Tag[] loreFromTag(final Tag value) {
        return componentsFromTag(value, 256);
    }

    protected StringTag rarityToTag(final Integer value) {
        return enumEntryToTag(value, "common", "uncommon", "rare", "epic");
    }

    protected Integer rarityFromTag(final Tag value) {
        if (value instanceof StringTag stringTag) {
            return enumEntryFromTag(stringTag, "common", "uncommon", "rare", "epic");
        } else {
            return null;
        }
    }

    protected CompoundTag enchantmentsToTag(final Enchantments value) {
        final CompoundTag tag = new CompoundTag();

        final CompoundTag levels = new CompoundTag();
        for (final Int2IntMap.Entry entry : value.enchantments().int2IntEntrySet()) {
            final int level = checkIntRange(0, 255, entry.getIntValue());
            levels.putInt(Enchantments1_20_5.idToKey(entry.getIntKey()), level);
        }
        tag.put("levels", levels);
        if (!value.showInTooltip()) {
            tag.putBoolean("show_in_tooltip", false);
        }
        return tag;
    }

    protected Enchantments enchantmentsFromTag(final Tag tag) {
        final CompoundTag compoundTag = (CompoundTag) tag;

        final Int2IntMap enchantments = new Int2IntOpenHashMap();
        final CompoundTag levels = compoundTag.getCompoundTag("levels");
        if (levels == null) {
            return null;
        }
        for (final Map.Entry<String, Tag> level : levels) {
            final int id = Enchantments1_20_5.keyToId(level.getKey());
            if (id != -1) {
                enchantments.put(id, checkIntRange(0, 255, asInt(level.getValue())));
            }
        }
        return new Enchantments(enchantments, compoundTag.getBoolean("show_in_tooltip", true));
    }

    protected CompoundTag canPlaceOnToTag(final AdventureModePredicate value) {
        return blockPredicateToTag(value);
    }

    protected CompoundTag canBreakToTag(final AdventureModePredicate value) {
        return blockPredicateToTag(value);
    }

    protected CompoundTag blockPredicateToTag(final AdventureModePredicate value) {
        final CompoundTag tag = new CompoundTag();
        final ListTag<CompoundTag> predicates = new ListTag<>(CompoundTag.class);
        for (final BlockPredicate predicate : value.predicates()) {
            final CompoundTag predicateTag = new CompoundTag();
            if (predicate.holderSet() != null) {
                holderSetToTag(predicateTag, "blocks", predicate.holderSet());
            }
            if (predicate.propertyMatchers() != null) {
                predicateTag.put("state", createState(predicate));
            }
            if (predicate.tag() != null) {
                predicateTag.put("nbt", predicate.tag());
            }

            predicates.add(predicateTag);
        }
        tag.put("predicates", predicates);
        if (!value.showInTooltip()) {
            tag.putBoolean("show_in_tooltip", false);
        }
        return tag;
    }

    protected AdventureModePredicate canPlaceOnFromTag(final Tag tag) {
        return blockPredicateFromTag(tag);
    }

    protected AdventureModePredicate canBreakFromTag(final Tag tag) {
        return blockPredicateFromTag(tag);
    }

    protected AdventureModePredicate blockPredicateFromTag(final Tag tag) {
        final CompoundTag value = (CompoundTag) tag;
        final boolean showInTooltip = value.getBoolean("show_in_tooltip", true);
        final ListTag<CompoundTag> predicates = value.getListTag("predicates", CompoundTag.class);
        final List<BlockPredicate> list = new ArrayList<>();
        if (predicates != null) {
            for (final CompoundTag predicateTag : predicates) {
                final HolderSet holderSet = holderSetFromTag(predicateTag, "blocks");
                final StatePropertyMatcher[] state = fromState(predicateTag.getCompoundTag("state"));

                list.add(new BlockPredicate(holderSet, state, predicateTag.getCompoundTag("nbt")));
            }
        }

        return new AdventureModePredicate(list.toArray(BlockPredicate[]::new), showInTooltip);
    }

    /*
    Not own conversion methods, just to avoid high nesting
     */

    protected CompoundTag createState(final BlockPredicate predicate) {
        final CompoundTag state = new CompoundTag();

        for (final StatePropertyMatcher matcher : predicate.propertyMatchers()) {
            final Either<String, StatePropertyMatcher.RangedMatcher> match = matcher.matcher();
            if (match.isLeft()) {
                state.putString(matcher.name(), match.left());
            } else {
                final StatePropertyMatcher.RangedMatcher range = match.right();
                final CompoundTag rangeTag = new CompoundTag();
                if (range.minValue() != null) {
                    rangeTag.putString("min", range.minValue());
                }
                if (range.maxValue() != null) {
                    rangeTag.putString("max", range.maxValue());
                }
                state.put(matcher.name(), rangeTag);
            }
        }
        return state;
    }

    protected StatePropertyMatcher @Nullable [] fromState(final CompoundTag value) {
        if (value == null) {
            return null;
        }

        final List<StatePropertyMatcher> list = new ArrayList<>();
        for (final Map.Entry<String, Tag> entry : value.entrySet()) {
            final String name = entry.getKey();
            final Tag tag = entry.getValue();
            if (tag instanceof StringTag stringTag) {
                list.add(new StatePropertyMatcher(name, Either.left(stringTag.getValue())));
            } else if (tag instanceof CompoundTag compoundTag) {
                final String min = compoundTag.getString("min");
                final String max = compoundTag.getString("max");
                list.add(new StatePropertyMatcher(name, Either.right(new StatePropertyMatcher.RangedMatcher(min, max))));
            }
        }
        return list.toArray(StatePropertyMatcher[]::new);
    }

    protected CompoundTag attributeModifiersToTag(final AttributeModifiers1_20_5 value) {
        final CompoundTag tag = new CompoundTag();
        final ListTag<CompoundTag> modifiers = new ListTag<>(CompoundTag.class);
        for (final AttributeModifier modifier : value.modifiers()) {
            final CompoundTag modifierTag = new CompoundTag();
            final String type = Attributes1_20_5.idToKey(modifier.attribute());
            if (type == null) {
                throw new IllegalArgumentException("Unknown attribute type: " + modifier.attribute());
            }

            modifierTag.putString("type", type);
            modifierDataToTag(modifierTag, modifier.modifier());
            if (modifier.slotType() != 0) {
                final String slotType = EquipmentSlots1_20_5.idToKey(modifier.slotType());
                Preconditions.checkNotNull(slotType, "Unknown slot type %s", modifier.slotType());
                modifierTag.putString("slot", slotType);
            }

            modifiers.add(modifierTag);
        }

        tag.put("modifiers", modifiers);
        if (!value.showInTooltip()) {
            tag.putBoolean("show_in_tooltip", false);
        }
        return tag;
    }

    protected AttributeModifiers1_20_5 attributeModifiersFromTag(final Tag tag) {
        final CompoundTag value = (CompoundTag) tag;

        final boolean showInTooltip = value.getBoolean("show_in_tooltip", true);
        final ListTag<CompoundTag> modifiers = value.getListTag("modifiers", CompoundTag.class);

        final List<AttributeModifier> list = new ArrayList<>();
        if (modifiers != null) {
            for (final CompoundTag modifierTag : modifiers) {
                final int type = Attributes1_20_5.keyToId(modifierTag.getString("type"));
                final ModifierData modifier = modifierDataFromTag(modifierTag);
                final int slotType = EquipmentSlots1_20_5.keyToId(modifierTag.getString("slot", "any"));
                list.add(new AttributeModifier(type, modifier, slotType));
            }
        }
        return new AttributeModifiers1_20_5(list.toArray(AttributeModifier[]::new), showInTooltip);
    }

    protected IntTag customModelDataToTag(final Integer value) {
        return new IntTag(value);
    }

    protected Integer customModelDataFromTag(final Tag value) {
        return asInt(value);
    }

    protected CompoundTag hideAdditionalTooltipToTag(final Unit value) {
        return unitToTag();
    }

    protected Unit hideAdditionalTooltipFromTag(final Tag value) {
        return Unit.INSTANCE;
    }

    protected CompoundTag hideTooltipToTag(final Unit value) {
        return unitToTag();
    }

    protected Unit hideTooltipFromTag(final Tag value) {
        return Unit.INSTANCE;
    }

    protected IntTag repairCostToTag(final Integer value) {
        return intRangeToTag(value, 0, Integer.MAX_VALUE);
    }

    protected Integer repairCostFromTag(final Tag value) {
        return checkIntRange(0, Integer.MAX_VALUE, asInt(value));
    }

    protected ByteTag enchantmentGlintOverrideToTag(final Boolean value) {
        return new ByteTag(value);
    }

    protected Boolean enchantmentGlintOverrideFromTag(final Tag value) {
        return asBoolean(value);
    }

    protected CompoundTag intangibleProjectileToTag(final Tag value) {
        return unitToTag();
    }

    protected Tag intangibleProjectileFromTag(final Tag value) {
        return value;
    }

    protected CompoundTag foodToTag(final FoodProperties1_20_5 value) {
        final CompoundTag tag = new CompoundTag();
        tag.put("nutrition", nonNegativeIntToTag(value.nutrition()));
        tag.putFloat("saturation", value.saturationModifier());
        if (value.canAlwaysEat()) {
            tag.putBoolean("can_always_eat", true);
        }
        if (value.eatSeconds() != 1.6F) {
            tag.put("eat_seconds", positiveFloatToTag(value.eatSeconds()));
        }
        if (value.possibleEffects().length > 0) {
            final ListTag<CompoundTag> effects = new ListTag<>(CompoundTag.class);
            for (final FoodProperties1_20_5.FoodEffect foodEffect : value.possibleEffects()) {
                final CompoundTag effectTag = new CompoundTag();
                final CompoundTag potionEffectTag = new CompoundTag();
                potionEffectToTag(potionEffectTag, foodEffect.effect());
                effectTag.put("effect", potionEffectTag);
                if (foodEffect.probability() != 1.0F) {
                    effectTag.putFloat("probability", foodEffect.probability());
                }
            }
            tag.put("effects", effects);
        }
        return tag;
    }

    protected FoodProperties1_20_5 foodFromTag(final Tag tag) {
        final CompoundTag value = (CompoundTag) tag;

        final int nutrition = checkNonNegativeInt(value.getInt("nutrition"));
        final float saturation = value.getFloat("saturation");
        final boolean canAlwaysEat = value.getBoolean("can_always_eat", false);
        final float eatSeconds = checkPositiveFloat(value.getFloat("eat_seconds", 1.6F));
        final ListTag<CompoundTag> effects = value.getListTag("effects", CompoundTag.class);
        final List<FoodProperties1_20_5.FoodEffect> list = new ArrayList<>();
        if (effects != null) {
            for (final CompoundTag effectTag : effects) {
                final PotionEffect effect = potionEffectFromTag(effectTag.getCompoundTag("effect"));
                final float probability = effectTag.getFloat("probability", 1.0F);
                list.add(new FoodProperties1_20_5.FoodEffect(effect, probability));
            }
        }
        return new FoodProperties1_20_5(nutrition, saturation, canAlwaysEat, eatSeconds, null, list.toArray(FoodProperties1_20_5.FoodEffect[]::new));
    }

    protected CompoundTag fireResistantToTag(final Unit value) {
        return unitToTag();
    }

    protected Unit fireResistantFromTag(final Tag value) {
        return Unit.INSTANCE;
    }

    protected CompoundTag toolToTag(final ToolProperties value) {
        final CompoundTag tag = new CompoundTag();

        final ListTag<CompoundTag> rules = new ListTag<>(CompoundTag.class);
        for (final ToolRule rule : value.rules()) {
            final CompoundTag ruleTag = new CompoundTag();
            holderSetToTag(ruleTag, "blocks", rule.blocks());
            if (rule.speed() != null) {
                ruleTag.putFloat("speed", rule.speed());
            }
            if (rule.correctForDrops() != null) {
                ruleTag.putBoolean("correct_for_drops", rule.correctForDrops());
            }
            rules.add(ruleTag);
        }
        tag.put("rules", rules);
        if (value.defaultMiningSpeed() != 1.0F) {
            tag.putFloat("default_mining_speed", value.defaultMiningSpeed());
        }
        if (value.damagePerBlock() != 1) {
            tag.put("damage_per_block", nonNegativeIntToTag(value.damagePerBlock()));
        }
        return tag;
    }

    protected ToolProperties toolFromTag(final Tag tag) {
        final CompoundTag value = (CompoundTag) tag;

        final ListTag<CompoundTag> rules = value.getListTag("rules", CompoundTag.class);
        final List<ToolRule> list = new ArrayList<>();
        if (rules != null) {
            for (final CompoundTag ruleTag : rules) {
                final HolderSet blocks = holderSetFromTag(ruleTag, "blocks");
                final Float speed = ruleTag.getFloat("speed", 0F);
                final Boolean correctForDrops = ruleTag.getBoolean("correct_for_drops", false);
                list.add(new ToolRule(blocks, speed, correctForDrops));
            }
        }
        final float defaultMiningSpeed = value.getFloat("default_mining_speed", 1.0F);
        final int damagePerBlock = checkNonNegativeInt(value.getInt("damage_per_block", 1));

        return new ToolProperties(list.toArray(ToolRule[]::new), defaultMiningSpeed, damagePerBlock);
    }

    protected CompoundTag storedEnchantmentsToTag(final Enchantments value) {
        return enchantmentsToTag(value);
    }

    protected Enchantments storedEnchantmentsFromTag(final Tag tag) {
        return enchantmentsFromTag(tag);
    }

    protected CompoundTag dyedColorToTag(final DyedColor value) {
        final CompoundTag tag = new CompoundTag();
        tag.putInt("rgb", value.rgb());
        if (!value.showInTooltip()) {
            tag.putBoolean("show_in_tooltip", false);
        }
        return tag;
    }

    protected DyedColor dyedColorFromTag(final Tag tag) {
        final CompoundTag value = (CompoundTag) tag;

        final int rgb = value.getInt("rgb");
        final boolean showInTooltip = value.getBoolean("show_in_tooltip", true);
        return new DyedColor(rgb, showInTooltip);
    }

    protected IntTag mapColorToTag(final Integer value) {
        return new IntTag(value);
    }

    protected Integer mapColorFromTag(final Tag tag) {
        return asInt(tag);
    }

    protected IntTag mapIdToTag(final Integer value) {
        return new IntTag(value);
    }

    protected Integer mapIdFromTag(final Tag tag) {
        return asInt(tag);
    }

    protected CompoundTag mapDecorationsToTag(final CompoundTag value) {
        return value; // String<->id conversion is already done by the item rewriter
    }

    protected CompoundTag mapDecorationsFromTag(final Tag tag) {
        return (CompoundTag) tag;
    }

    protected ListTag<CompoundTag> chargedProjectilesToTag(final UserConnection connection, final Item[] value) {
        return itemArrayToTag(connection, value);
    }

    protected Item[] chargedProjectilesFromTag(final UserConnection connection, final Tag tag) {
        final ListTag<CompoundTag> value = (ListTag<CompoundTag>) tag;
        return itemArrayFromTag(connection, value);
    }

    protected ListTag<CompoundTag> bundleContentsToTag(final UserConnection connection, final Item[] value) {
        return itemArrayToTag(connection, value);
    }

    protected Item[] bundleContentsFromTag(final UserConnection connection, final Tag tag) {
        final ListTag<CompoundTag> value = (ListTag<CompoundTag>) tag;
        return itemArrayFromTag(connection, value);
    }

    protected CompoundTag potionContentsToTag(final PotionContents value) {
        final CompoundTag tag = new CompoundTag();
        if (value.potion() != null) {
            final String potion = Potions1_20_5.idToKey(value.potion());
            if (potion != null) {
                tag.putString("potion", potion);
            }
        }
        if (value.customColor() != null) {
            tag.putInt("custom_color", value.customColor());
        }
        final ListTag<CompoundTag> customEffects = new ListTag<>(CompoundTag.class);
        for (final PotionEffect effect : value.customEffects()) {
            final CompoundTag effectTag = new CompoundTag();
            potionEffectToTag(effectTag, effect);
            customEffects.add(effectTag);
        }
        tag.put("custom_effects", customEffects);
        if (value.customName() != null) {
            tag.putString("custom_name", value.customName());
        }
        tag.put("custom_effects", customEffects);
        return tag;
    }

    protected PotionContents potionContentsFromTag(final Tag tag) {
        final CompoundTag value = (CompoundTag) tag;

        final int potion = Potions1_20_5.keyToId(value.getString("potion", ""));
        final IntTag customColor = value.getIntTag("custom_color");
        final ListTag<CompoundTag> customEffects = value.getListTag("custom_effects", CompoundTag.class);
        final List<PotionEffect> list = new ArrayList<>();
        if (customEffects != null) {
            for (final CompoundTag effectTag : customEffects) {
                list.add(potionEffectFromTag(effectTag));
            }
        }
        return new PotionContents(potion != -1 ? potion : null, customColor != null ? customColor.asInt() : null, list.toArray(PotionEffect[]::new));
    }

    protected ListTag<CompoundTag> suspiciousStewEffectsToTag(final SuspiciousStewEffect[] value) {
        final ListTag<CompoundTag> tag = new ListTag<>(CompoundTag.class);
        for (final SuspiciousStewEffect effect : value) {
            final CompoundTag effectTag = new CompoundTag();
            final String id = PotionEffects1_20_5.idToKey(effect.mobEffect());
            if (id != null) {
                effectTag.putString("id", id);
            }
            if (effect.duration() != 160) {
                effectTag.putInt("duration", effect.duration());
            }
            tag.add(effectTag);
        }
        return tag;
    }

    protected SuspiciousStewEffect[] suspiciousStewEffectsFromTag(final Tag tag) {
        final ListTag<CompoundTag> value = (ListTag<CompoundTag>) tag;

        final List<SuspiciousStewEffect> list = new ArrayList<>();
        for (final CompoundTag effectTag : value) {
            final int id = PotionEffects1_20_5.keyToId(effectTag.getString("id", ""));
            final int duration = effectTag.getInt("duration", 160);
            list.add(new SuspiciousStewEffect(id, duration));
        }

        return list.toArray(SuspiciousStewEffect[]::new);
    }

    protected CompoundTag writableBookContentToTag(final WritableBook value) {
        final CompoundTag tag = new CompoundTag();
        if (value == null) {
            return tag;
        }

        if (value.pages().length > 100) {
            throw new IllegalArgumentException("Too many pages: " + value.pages().length);
        }

        final ListTag<CompoundTag> pagesTag = new ListTag<>(CompoundTag.class);
        for (final FilterableString page : value.pages()) {
            final CompoundTag pageTag = new CompoundTag();
            filterableStringToTag(pageTag, page, 1024);
            pagesTag.add(pageTag);
        }
        tag.put("pages", pagesTag);
        return tag;
    }

    protected WritableBook writableBookContentFromTag(final Tag tag) {
        final CompoundTag value = (CompoundTag) tag;

        final ListTag<CompoundTag> pagesTag = value.getListTag("pages", CompoundTag.class);
        if (pagesTag == null) {
            return null;
        }

        final FilterableString[] pages = new FilterableString[pagesTag.size()];
        for (int i = 0; i < pagesTag.size(); i++) {
            pages[i] = filterableStringFromTag(pagesTag.get(i));
        }
        return new WritableBook(pages);
    }

    protected CompoundTag writtenBookContentToTag(final WrittenBook value) {
        final CompoundTag tag = new CompoundTag();

        final CompoundTag title = new CompoundTag();
        filterableStringToTag(title, value.title(), 32);
        tag.put("title", title);
        tag.putString("author", value.author());
        if (value.generation() != 0) {
            tag.put("generation", intRangeToTag(value.generation(), 0, 3));
        }

        final ListTag<CompoundTag> pagesTag = new ListTag<>(CompoundTag.class);
        for (final FilterableComponent page : value.pages()) {
            final CompoundTag pageTag = new CompoundTag();
            filterableComponentToTag(pageTag, page);
            pagesTag.add(pageTag);
        }

        if (!pagesTag.isEmpty()) {
            tag.put("pages", pagesTag);
        }

        if (value.resolved()) {
            tag.putBoolean("resolved", true);
        }
        return tag;
    }

    protected WrittenBook writtenBookContentFromTag(final Tag tag) {
        final CompoundTag value = (CompoundTag) tag;

        final FilterableString title = filterableStringFromTag(value.getCompoundTag("title"));
        final String author = value.getString("author");
        final int generation = checkIntRange(0, 3, value.getInt("generation", 0));

        final ListTag<CompoundTag> pagesTag = value.getListTag("pages", CompoundTag.class);
        final List<FilterableComponent> pages = new ArrayList<>();
        if (pagesTag != null) {
            for (final CompoundTag pageTag : pagesTag) {
                pages.add(filterableComponentFromTag(pageTag));
            }
        }

        final boolean resolved = value.getBoolean("resolved", false);
        return new WrittenBook(title, author, generation, pages.toArray(FilterableComponent[]::new), resolved);
    }

    protected CompoundTag trimToTag(final UserConnection connection, final ArmorTrim value) {
        final CompoundTag tag = new CompoundTag();
        final Holder<ArmorTrimMaterial> material = value.material();
        final ArmorTrimStorage trimStorage = connection.get(ArmorTrimStorage.class);
        if (material.hasId()) {
            final String trimMaterial = trimStorage.trimMaterials().idToKey(material.id());
            tag.putString("material", trimMaterial);
        } else {
            final ArmorTrimMaterial armorTrimMaterial = material.value();
            final CompoundTag materialTag = new CompoundTag();
            final String ingredient = Protocol1_20_3To1_20_5.MAPPINGS.getFullItemMappings().identifier(armorTrimMaterial.itemId());
            if (ingredient == null) {
                throw new IllegalArgumentException("Unknown item: " + armorTrimMaterial.itemId());
            }

            final CompoundTag overrideArmorMaterialsTag = new CompoundTag();
            for (final Map.Entry<String, String> entry : armorTrimMaterial.overrideArmorMaterials().entrySet()) {
                final String materialKey = ArmorMaterials1_20_5.idToKey(Integer.parseInt(entry.getKey()));
                if (materialKey != null) {
                    overrideArmorMaterialsTag.putString(materialKey, entry.getValue());
                }
            }

            materialTag.putString("asset_name", armorTrimMaterial.assetName());
            materialTag.putString("ingredient", ingredient);
            materialTag.putFloat("item_model_index", armorTrimMaterial.itemModelIndex());
            materialTag.put("override_armor_materials", overrideArmorMaterialsTag);
            materialTag.put("description", armorTrimMaterial.description());
            tag.put("material", materialTag);
        }

        final Holder<ArmorTrimPattern> pattern = value.pattern();
        if (pattern.hasId()) {
            tag.putString("pattern", trimStorage.trimPatterns().idToKey(pattern.id()));
        } else {
            final ArmorTrimPattern armorTrimPattern = pattern.value();
            final CompoundTag patternTag = new CompoundTag();
            final String templateItem = Protocol1_20_3To1_20_5.MAPPINGS.getFullItemMappings().identifier(armorTrimPattern.itemId());
            if (templateItem == null) {
                throw new IllegalArgumentException("Unknown item: " + armorTrimPattern.itemId());
            }

            patternTag.put("asset_id", identifierToTag(armorTrimPattern.assetName()));
            patternTag.putString("template_item", templateItem);
            patternTag.put("description", armorTrimPattern.description());
            if (armorTrimPattern.decal()) {
                patternTag.putBoolean("decal", true);
            }
            tag.put("pattern", patternTag);
        }

        if (!value.showInTooltip()) {
            tag.putBoolean("show_in_tooltip", false);
        }
        return tag;
    }

    protected ArmorTrim trimFromTag(final UserConnection connection, final Tag tag) {
        final CompoundTag value = (CompoundTag) tag;

        final Tag materialTag = value.get("material");
        Holder<ArmorTrimMaterial> material;
        final ArmorTrimStorage trimStorage = connection.get(ArmorTrimStorage.class);
        if (materialTag instanceof StringTag stringTag) {
            material = Holder.of(trimStorage.trimMaterials().keyToId(stringTag.getValue()));
        } else {
            final CompoundTag materialValue = (CompoundTag) materialTag;
            final String assetName = identifierFromTag(materialValue.getStringTag("asset_name"));
            final int ingredient = Protocol1_20_3To1_20_5.MAPPINGS.getFullItemMappings().mappedId(materialValue.getString("ingredient"));
            final float itemModelIndex = materialValue.getFloat("item_model_index");
            final Map<String, String> overrideArmorMaterials = new HashMap<>();
            final CompoundTag overrideArmorMaterialsTag = materialValue.getCompoundTag("override_armor_materials");
            if (overrideArmorMaterialsTag != null) {
                for (final Map.Entry<String, Tag> entry : overrideArmorMaterialsTag.entrySet()) {
                    overrideArmorMaterials.put(entry.getKey(), ((StringTag) entry.getValue()).getValue());
                }
            }
            final Tag description = materialValue.get("description");
            material = Holder.of(new ArmorTrimMaterial(assetName, ingredient, itemModelIndex, overrideArmorMaterials, description));
        }

        final Tag patternTag = value.get("pattern");
        Holder<ArmorTrimPattern> pattern;
        if (patternTag instanceof StringTag stringTag) {
            pattern = Holder.of(trimStorage.trimPatterns().keyToId(stringTag.getValue()));
        } else {
            final CompoundTag patternValue = (CompoundTag) patternTag;
            final String assetName = identifierFromTag(patternValue.getStringTag("asset_id"));
            final int templateItem = Protocol1_20_3To1_20_5.MAPPINGS.getFullItemMappings().mappedId(patternValue.getString("template_item"));
            final Tag description = patternValue.get("description");
            final boolean decal = patternValue.getBoolean("decal", false);
            pattern = Holder.of(new ArmorTrimPattern(assetName, templateItem, description, decal));
        }

        final boolean showInTooltip = value.getBoolean("show_in_tooltip", true);
        return new ArmorTrim(material, pattern, showInTooltip);
    }

    protected CompoundTag debugStickRateToTag(final CompoundTag value) {
        return value;
    }

    protected CompoundTag debugStickRateFromTag(final Tag value) {
        return (CompoundTag) value;
    }

    protected CompoundTag entityDataToTag(final CompoundTag value) {
        return nbtWithIdToTag(value);
    }

    protected CompoundTag entityDataFromTag(final Tag value) {
        return (CompoundTag) value;
    }

    protected CompoundTag bucketEntityDataToTag(final CompoundTag value) {
        return nbtToTag(value);
    }

    protected CompoundTag bucketEntityDataFromTag(final Tag value) {
        return (CompoundTag) value;
    }

    protected CompoundTag blockEntityDataToTag(final CompoundTag value) {
        return nbtWithIdToTag(value);
    }

    protected CompoundTag blockEntityDataFromTag(final Tag value) {
        return (CompoundTag) value;
    }

    protected Tag instrumentToTag(final Holder<Instrument1_20_5> value) {
        if (value.hasId()) {
            return new StringTag(Instruments1_20_3.idToKey(value.id()));
        }

        final Instrument1_20_5 instrument = value.value();
        final CompoundTag tag = new CompoundTag();
        final Holder<SoundEvent> sound = instrument.soundEvent();
        if (sound.hasId()) {
            tag.putString("sound_event", Protocol1_20_3To1_20_5.MAPPINGS.soundName(sound.id()));
        } else {
            final SoundEvent soundEvent = sound.value();
            final CompoundTag soundEventTag = new CompoundTag();
            soundEventTag.put("sound_id", identifierToTag(soundEvent.identifier()));
            if (soundEvent.fixedRange() != null) {
                soundEventTag.putFloat("range", soundEvent.fixedRange());
            }
            tag.put("sound_event", soundEventTag);
        }

        tag.put("use_duration", positiveIntToTag(instrument.useDuration()));
        tag.put("range", positiveFloatToTag(instrument.range()));
        return tag;
    }

    protected Holder<Instrument1_20_5> instrumentFromTag(final Tag tag) {
        if (tag instanceof StringTag stringTag) {
            return Holder.of(Instruments1_20_3.keyToId(stringTag.getValue()));
        }

        final CompoundTag value = (CompoundTag) tag;
        Holder<SoundEvent> soundEvent = null;
        final Tag soundEventTag = value.get("sound_event");
        if (soundEventTag instanceof StringTag stringTag) {
            soundEvent = Holder.of(Protocol1_20_3To1_20_5.MAPPINGS.soundId(stringTag.getValue()));
        } else if (soundEventTag instanceof CompoundTag compoundTag) {
            final String soundId = identifierFromTag(compoundTag.getStringTag("sound_id"));
            final Float range = compoundTag.getFloat("range", 0.0F);
            soundEvent = Holder.of(new SoundEvent(soundId, range));
        }

        final int useDuration = checkPositiveInt(value.getInt("use_duration"));
        final float range = checkPositiveFloat(value.getFloat("range"));

        return Holder.of(new Instrument1_20_5(soundEvent, useDuration, range));
    }

    protected IntTag ominousBottleAmplifierToTag(final Integer value) {
        return intRangeToTag(value, 0, 4);
    }

    protected Integer ominousBottleAmplifierFromTag(final Tag value) {
        return checkIntRange(0, 4, asInt(value));
    }

    protected Tag recipesToTag(final Tag value) {
        return value; // Item rewriter takes care of validation
    }

    protected Tag recipesFromTag(final Tag value) {
        return value; // Item rewriter takes care of validation
    }

    protected CompoundTag lodestoneTrackerToTag(final LodestoneTracker value) {
        final CompoundTag tag = new CompoundTag();
        if (value.position() != null) {
            globalPosToTag(tag, value.position());
        }
        if (!value.tracked()) {
            tag.putBoolean("tracked", false);
        }
        return tag;
    }

    protected LodestoneTracker lodestoneTrackerFromTag(final Tag tag) {
        final CompoundTag value = (CompoundTag) tag;
        final GlobalBlockPosition position = globalPosFromTag(value);
        final boolean tracked = value.getBoolean("tracked", true);
        return new LodestoneTracker(position, tracked);
    }

    protected CompoundTag fireworkExplosionToTag(final FireworkExplosion value) {
        final CompoundTag tag = new CompoundTag();
        tag.put("shape", enumEntryToTag(value.shape(), "small_ball", "large_ball", "star", "creeper", "burst"));
        if (value.colors().length > 0) {
            tag.put("colors", new IntArrayTag(value.colors()));
        }
        if (value.fadeColors().length > 0) {
            tag.put("fade_colors", new IntArrayTag(value.fadeColors()));
        }
        if (value.hasTrail()) {
            tag.putBoolean("trail", true);
        }
        if (value.hasTwinkle()) {
            tag.putBoolean("twinkle", true);
        }
        return tag;
    }

    protected FireworkExplosion fireworkExplosionFromTag(final Tag tag) {
        final CompoundTag value = (CompoundTag) tag;

        final int shape = enumEntryFromTag(value.getStringTag("shape"), "small_ball", "large_ball", "star", "creeper", "burst");
        final IntArrayTag colors = value.getIntArrayTag("colors");
        final IntArrayTag fadeColors = value.getIntArrayTag("fade_colors");
        final boolean trail = value.getBoolean("trail", false);
        final boolean twinkle = value.getBoolean("twinkle", false);

        return new FireworkExplosion(shape, colors == null ? new int[0] : colors.getValue(), fadeColors == null ? new int[0] : fadeColors.getValue(), trail, twinkle);
    }

    protected CompoundTag fireworksToTag(final Fireworks value) {
        final CompoundTag tag = new CompoundTag();
        if (value.flightDuration() != 0) {
            tag.put("flight_duration", unsignedByteToTag((byte) value.flightDuration()));
        }
        final ListTag<CompoundTag> explosions = new ListTag<>(CompoundTag.class);
        if (value.explosions().length > 256) {
            throw new IllegalArgumentException("Too many explosions: " + value.explosions().length);
        }
        for (final FireworkExplosion explosion : value.explosions()) {
            explosions.add(fireworkExplosionToTag(explosion));
        }
        tag.put("explosions", explosions);
        return tag;
    }

    protected Fireworks fireworksFromTag(final Tag value) {
        final CompoundTag tag = (CompoundTag) value;
        final short flightDuration = tag.getShort("flight_duration");
        final ListTag<CompoundTag> explosions = tag.getListTag("explosions", CompoundTag.class);
        final FireworkExplosion[] list = new FireworkExplosion[explosions != null ? explosions.size() : 0];
        for (int i = 0; i < list.length; i++) {
            list[i] = fireworkExplosionFromTag(explosions.get(i));
        }
        return new Fireworks(flightDuration, list);
    }

    protected CompoundTag profileToTag(final GameProfile value) {
        final CompoundTag tag = new CompoundTag();
        if (value.name() != null) {
            tag.putString("name", value.name());
        }
        if (value.id() != null) {
            tag.put("id", new IntArrayTag(UUIDUtil.toIntArray(value.id())));
        }
        if (value.properties().length > 0) {
            propertiesToTag(tag, value.properties());
        }
        return tag;
    }

    protected GameProfile profileFromTag(final Tag tag) {
        final CompoundTag value = (CompoundTag) tag;

        final String name = value.getString("name");
        final IntArrayTag idTag = value.getIntArrayTag("id");
        final UUID id = idTag == null ? null : UUIDUtil.fromIntArray(idTag.getValue());
        final GameProfile.Property[] properties = propertiesFromTag(value);
        return new GameProfile(name, id, properties);
    }

    protected StringTag noteBlockSoundToTag(final Key value) {
        return new StringTag(value.original());
    }

    protected Key noteBlockSoundFromTag(final Tag value) {
        return Key.of(((StringTag) value).getValue());
    }

    protected ListTag<CompoundTag> bannerPatternsToTag(final BannerPatternLayer[] value) {
        final ListTag<CompoundTag> tag = new ListTag<>(CompoundTag.class);
        for (final BannerPatternLayer layer : value) {
            final CompoundTag layerTag = new CompoundTag();
            bannerPatternToTag(layerTag, layer.pattern());
            layerTag.put("color", dyeColorToTag(layer.dyeColor()));
            tag.add(layerTag);
        }
        return tag;
    }

    protected BannerPatternLayer[] bannerPatternsFromTag(final Tag tag) {
        final ListTag<CompoundTag> value = (ListTag<CompoundTag>) tag;
        final BannerPatternLayer[] layers = new BannerPatternLayer[value.size()];
        for (int i = 0; i < value.size(); i++) {
            final CompoundTag layerTag = value.get(i);
            final Holder<BannerPattern> pattern = bannerPatternFromTag(layerTag.get("pattern"));
            final int color = dyeColorFromTag(layerTag.get("color"));
            layers[i] = new BannerPatternLayer(pattern, color);
        }
        return layers;
    }

    protected StringTag baseColorToTag(final Integer value) {
        return dyeColorToTag(value);
    }

    protected Integer baseColorFromTag(final Tag tag) {
        return dyeColorFromTag(tag);
    }

    protected ListTag<StringTag> potDecorationsToTag(final PotDecorations value) {
        final ListTag<StringTag> tag = new ListTag<>(StringTag.class);
        for (final int decoration : value.itemIds()) {
            final String identifier = mappedIdentifier(decoration);
            if (identifier == null) {
                throw new IllegalArgumentException("Unknown item: " + decoration);
            }
            tag.add(new StringTag(identifier));
        }
        return tag;
    }

    protected PotDecorations potDecorationsFromTag(final Tag tag) {
        final ListTag<StringTag> value = (ListTag<StringTag>) tag;
        final int[] itemIds = new int[value.size()];
        for (int i = 0; i < value.size(); i++) {
            final String identifier = value.get(i).getValue();
            final int id = mappedId(identifier);
            if (id == -1) {
                throw new IllegalArgumentException("Unknown item: " + identifier);
            }
            itemIds[i] = id;
        }
        return new PotDecorations(itemIds);
    }

    protected ListTag<CompoundTag> containerToTag(final UserConnection connection, final Item[] value) {
        final ListTag<CompoundTag> tag = new ListTag<>(CompoundTag.class);
        for (int i = 0; i < value.length; i++) {
            final Item item = value[i];
            if (item.isEmpty()) {
                continue;
            }

            final CompoundTag slotTag = new CompoundTag();
            final CompoundTag itemTag = new CompoundTag();
            itemToTag(connection, itemTag, item);
            slotTag.putInt("slot", i);
            slotTag.put("item", itemTag);
            tag.add(slotTag);
        }
        return tag;
    }

    protected Item[] containerFromTag(final UserConnection connection, final Tag tag) {
        final ListTag<CompoundTag> value = (ListTag<CompoundTag>) tag;
        int highestSlot = 0;

        for (int i = 0, size = Math.min(value.size(), 256); i < size; i++) {
            final CompoundTag itemTag = value.get(i);
            final Item item = itemFromTag(connection, itemTag);
            if (item.isEmpty()) {
                continue;
            }

            final int slot = itemTag.getInt("slot");
            highestSlot = MathUtil.clamp(slot, highestSlot, 255);
        }

        final Item[] filteredItems = new Item[highestSlot + 1];
        for (final CompoundTag itemTag : value) {
            final Item item = itemFromTag(connection, itemTag.getCompoundTag("item"));
            if (item.isEmpty()) {
                continue;
            }

            final int slot = itemTag.getInt("slot");
            if (slot >= 0 && slot < filteredItems.length) {
                filteredItems[slot] = item;
            }
        }
        return filteredItems;
    }

    protected CompoundTag blockStateToTag(final BlockStateProperties value) {
        final CompoundTag tag = new CompoundTag();
        for (final Map.Entry<String, String> entry : value.properties().entrySet()) {
            tag.putString(entry.getKey(), entry.getValue());
        }
        return tag;
    }

    protected BlockStateProperties blockStateFromTag(final Tag tag) {
        final CompoundTag value = (CompoundTag) tag;
        final Map<String, String> properties = new HashMap<>();
        for (final Map.Entry<String, Tag> entry : value.entrySet()) {
            properties.put(entry.getKey(), ((StringTag) entry.getValue()).getValue());
        }
        return new BlockStateProperties(properties);
    }

    protected ListTag<CompoundTag> beesToTag(final Bee[] value) {
        final ListTag<CompoundTag> tag = new ListTag<>(CompoundTag.class);
        for (final Bee bee : value) {
            final CompoundTag beeTag = new CompoundTag();
            if (!bee.entityData().isEmpty()) {
                beeTag.put("entity_data", nbtToTag(bee.entityData()));
            }
            beeTag.putInt("ticks_in_hive", bee.ticksInHive());
            beeTag.putInt("min_ticks_in_hive", bee.minTicksInHive());
        }
        return tag;
    }

    protected Bee[] beesFromTag(final Tag tag) {
        final ListTag<CompoundTag> value = (ListTag<CompoundTag>) tag;
        final Bee[] bees = new Bee[value.size()];
        for (int i = 0; i < value.size(); i++) {
            final CompoundTag beeTag = value.get(i);
            final CompoundTag entityData = beeTag.getCompoundTag("entity_data");
            final int ticksInHive = beeTag.getInt("ticks_in_hive");
            final int minTicksInHive = beeTag.getInt("min_ticks_in_hive");
            bees[i] = new Bee(entityData != null ? entityData : new CompoundTag(), ticksInHive, minTicksInHive);
        }
        return bees;
    }

    protected StringTag lockToTag(final Tag value) {
        return (StringTag) value;
    }

    protected Tag lockFromTag(final Tag value) {
        return value;
    }

    protected CompoundTag containerLootToTag(final CompoundTag value) {
        return value; // Handled by the item rewriter
    }

    protected CompoundTag containerLootFromTag(final Tag value) {
        return (CompoundTag) value;
    }

    // ---------------------------------------------------------------------------------------

    protected void modifierDataToTag(final CompoundTag tag, final ModifierData data) {
        tag.put("uuid", new IntArrayTag(UUIDUtil.toIntArray(data.uuid())));
        tag.putString("name", data.name());
        tag.putDouble("amount", data.amount());
        tag.putString("operation", BlockItemPacketRewriter1_20_5.ATTRIBUTE_OPERATIONS[data.operation()]);
    }

    protected ModifierData modifierDataFromTag(final CompoundTag tag) {
        final UUID uuid = UUIDUtil.fromIntArray(tag.getIntArrayTag("uuid").getValue());
        final String name = tag.getString("name");
        final double amount = tag.getDouble("amount");

        final String operationName = tag.getString("operation");
        int operation;
        for (operation = 0; operation < BlockItemPacketRewriter1_20_5.ATTRIBUTE_OPERATIONS.length; operation++) {
            if (BlockItemPacketRewriter1_20_5.ATTRIBUTE_OPERATIONS[operation].equals(operationName)) {
                break;
            }
        }
        return new ModifierData(uuid, name, amount, operation);
    }

    protected void potionEffectToTag(final CompoundTag tag, final PotionEffect effect) {
        final String id = PotionEffects1_20_5.idToKey(effect.effect());
        if (id == null) {
            throw new IllegalArgumentException("Unknown potion effect: " + effect.effect());
        }
        tag.putString("id", id);
        potionEffectDataToTag(tag, effect.effectData());
    }

    protected PotionEffect potionEffectFromTag(final CompoundTag tag) {
        final int id = PotionEffects1_20_5.keyToId(tag.getString("id"));
        if (id == -1) {
            return null;
        }
        final PotionEffectData data = potionEffectDataFromTag(tag);
        return new PotionEffect(id, data);
    }

    protected void potionEffectDataToTag(final CompoundTag tag, final PotionEffectData data) {
        if (data.amplifier() != 0) {
            tag.putInt("amplifier", data.amplifier());
        }
        if (data.duration() != 0) {
            tag.putInt("duration", data.duration());
        }
        if (data.ambient()) {
            tag.putBoolean("ambient", true);
        }
        if (!data.showParticles()) {
            tag.putBoolean("show_particles", false);
        }
        tag.putBoolean("show_icon", data.showIcon());
        if (data.hiddenEffect() != null) {
            final CompoundTag hiddenEffect = new CompoundTag();
            potionEffectDataToTag(hiddenEffect, data.hiddenEffect());
            tag.put("hidden_effect", hiddenEffect);
        }
    }

    protected PotionEffectData potionEffectDataFromTag(final CompoundTag tag) {
        final int amplifier = tag.getInt("amplifier", 0);
        final int duration = tag.getInt("duration", 0);
        final boolean ambient = tag.getBoolean("ambient", false);
        final boolean showParticles = tag.getBoolean("show_particles", true);
        final boolean showIcon = tag.getBoolean("show_icon", true);
        final CompoundTag hiddenEffectTag = tag.getCompoundTag("hidden_effect");
        final PotionEffectData hiddenEffect = hiddenEffectTag != null ? potionEffectDataFromTag(hiddenEffectTag) : null;

        return new PotionEffectData(amplifier, duration, ambient, showParticles, showIcon, hiddenEffect);
    }

    protected void holderSetToTag(final CompoundTag tag, final String name, final HolderSet set) {
        if (set.hasTagKey()) {
            tag.putString(name, set.tagKey());
        } else {
            final ListTag<StringTag> identifiers = new ListTag<>(StringTag.class);
            for (final int id : set.ids()) {
                // Can use old block list because new ids are only at the end :tm:
                final String identifier = Protocol1_20_3To1_20_5.MAPPINGS.blockName(id);
                if (identifier == null) {
                    continue;
                }

                identifiers.add(new StringTag(identifier));
            }
            tag.put(name, identifiers);
        }
    }

    protected HolderSet holderSetFromTag(final CompoundTag tag, final String name) {
        final Tag value = tag.get(name);
        if (value instanceof StringTag stringTag) {
            return HolderSet.of(stringTag.getValue());
        } else if (value instanceof ListTag<?> listTag) {
            final ListTag<StringTag> identifiers = (ListTag<StringTag>) listTag;
            final int[] ids = new int[identifiers.size()];
            for (int i = 0; i < identifiers.size(); i++) {
                final String identifier = identifiers.get(i).getValue();
                final int id = Protocol1_20_3To1_20_5.MAPPINGS.blockId(identifier);
                if (id != -1) {
                    ids[i] = id;
                }
            }
            return HolderSet.of(ids);
        } else {
            return null;
        }
    }

    protected ListTag<CompoundTag> itemArrayToTag(final UserConnection connection, final Item[] value) {
        final ListTag<CompoundTag> tag = new ListTag<>(CompoundTag.class);
        for (final Item item : value) {
            final CompoundTag itemTag = new CompoundTag();
            itemToTag(connection, itemTag, item);
            tag.add(itemTag);
        }
        return tag;
    }

    protected Item[] itemArrayFromTag(final UserConnection connection, final ListTag<CompoundTag> tag) {
        final Item[] items = new Item[tag.size()];
        for (int i = 0; i < tag.size(); i++) {
            items[i] = itemFromTag(connection, tag.get(i));
        }
        return items;
    }

    protected void itemToTag(final UserConnection connection, final CompoundTag tag, final Item item) {
        final String identifier = mappedIdentifier(item.identifier());
        if (identifier == null) {
            throw new IllegalArgumentException("Unknown item: " + item.identifier());
        }
        tag.putString("id", identifier);
        try {
            tag.put("count", positiveIntToTag(item.amount()));
        } catch (IllegalArgumentException ignored) { // Fallback value
            tag.putInt("count", 1);
        }
        final Map<StructuredDataKey<?>, StructuredData<?>> components = item.dataContainer().data();
        final CompoundTag componentsTag = toTag(connection, components);
        if (!componentsTag.isEmpty()) {
            tag.put("components", componentsTag);
        }
    }

    protected Item itemFromTag(final UserConnection connection, final CompoundTag tag) {
        final int id = mappedId(tag.getString("id", ""));
        final int amount = checkPositiveInt(tag.getInt("count", 1));
        final List<StructuredData<?>> components = toData(connection, tag.getCompoundTag("components"));

        return new StructuredItem(id, amount, new StructuredDataContainer(components.toArray(StructuredData[]::new)));
    }

    protected void filterableStringToTag(final CompoundTag tag, final FilterableString string, final int max) {
        tag.put("raw", stringToTag(string.raw(), 0, max));
        if (string.filtered() != null) {
            tag.put("filtered", stringToTag(string.filtered(), 0, max));
        }
    }

    protected FilterableString filterableStringFromTag(final CompoundTag tag) {
        if (tag == null) {
            return null;
        }
        final String raw = checkStringRange(0, 1024, tag.getString("raw"));
        final StringTag filteredTag = tag.getStringTag("filtered");
        if (filteredTag == null) {
            return new FilterableString(raw, null);
        } else {
            return new FilterableString(raw, checkStringRange(0, 1024, filteredTag.getValue()));
        }
    }

    protected void filterableComponentToTag(final CompoundTag tag, final FilterableComponent component) {
        tag.put("raw", componentToTag(component.raw()));
        if (component.filtered() != null) {
            tag.put("filtered", componentToTag(component.filtered()));
        }
    }

    protected FilterableComponent filterableComponentFromTag(final CompoundTag tag) {
        if (tag == null) {
            return null;
        }
        final Tag raw = componentFromTag(tag.get("raw"));
        final Tag filtered = componentFromTag(tag.get("filtered"));
        return new FilterableComponent(raw, filtered);
    }

    protected void globalPosToTag(final CompoundTag tag, final GlobalBlockPosition position) {
        final CompoundTag posTag = new CompoundTag();
        posTag.putString("dimension", position.dimension());
        posTag.put("pos", new IntArrayTag(new int[]{position.x(), position.y(), position.z()}));
        tag.put("target", posTag);
    }

    protected GlobalBlockPosition globalPosFromTag(final CompoundTag tag) {
        final CompoundTag posTag = tag.getCompoundTag("target");
        if (posTag == null) {
            return null;
        }
        final String dimension = posTag.getString("dimension");
        final int[] pos = posTag.getIntArrayTag("pos").getValue();
        return new GlobalBlockPosition(dimension, pos[0], pos[1], pos[2]);
    }

    protected void propertiesToTag(final CompoundTag tag, final GameProfile.Property[] properties) {
        final ListTag<CompoundTag> propertiesTag = new ListTag<>(CompoundTag.class);
        for (final GameProfile.Property property : properties) {
            final CompoundTag propertyTag = new CompoundTag();
            propertyTag.putString("name", property.name());
            propertyTag.putString("value", property.value());
            if (property.signature() != null) {
                propertyTag.putString("signature", property.signature());
            }
            propertiesTag.add(propertyTag);
        }
        tag.put("properties", propertiesTag);
    }

    protected GameProfile.Property[] propertiesFromTag(final CompoundTag tag) {
        final ListTag<CompoundTag> propertiesTag = tag.getListTag("properties", CompoundTag.class);
        if (propertiesTag == null) {
            return new GameProfile.Property[0];
        }

        final GameProfile.Property[] properties = new GameProfile.Property[propertiesTag.size()];
        for (int i = 0; i < propertiesTag.size(); i++) {
            final CompoundTag propertyTag = propertiesTag.get(i);
            final String name = propertyTag.getString("name");
            final String value = propertyTag.getString("value");
            final String signature = propertyTag.getString("signature", null);
            properties[i] = new GameProfile.Property(name, value, signature);
        }
        return properties;
    }

    protected void bannerPatternToTag(final CompoundTag tag, final Holder<BannerPattern> pattern) {
        if (pattern.hasId()) {
            tag.putString("pattern", BannerPatterns1_20_5.idToKey(pattern.id()));
            return;
        }

        final BannerPattern bannerPattern = pattern.value();
        final CompoundTag patternTag = new CompoundTag();
        patternTag.put("asset_id", identifierToTag(bannerPattern.assetId()));
        patternTag.putString("translation_key", bannerPattern.translationKey());
        tag.put("pattern", patternTag);
    }

    protected Holder<BannerPattern> bannerPatternFromTag(final Tag tag) {
        if (tag instanceof StringTag stringTag) {
            return Holder.of(BannerPatterns1_20_5.keyToId(stringTag.getValue()));
        } else if (tag instanceof CompoundTag compoundTag) {
            final String assetId = identifierFromTag(compoundTag.getStringTag("asset_id"));
            final String translationKey = compoundTag.getString("translation_key");
            return Holder.of(new BannerPattern(assetId, translationKey));
        } else {
            return null;
        }
    }

    // ---------------------------------------------------------------------------------------

    protected Integer asInt(final Tag tag) {
        return ((NumberTag) tag).asInt();
    }

    protected Boolean asBoolean(final Tag tag) {
        return ((ByteTag) tag).asByte() != 0;
    }

    protected Short asUnsignedByte(final Tag tag) {
        return ((ByteTag) tag).asShort();
    }

    protected IntTag positiveIntToTag(final Integer value) {
        return intRangeToTag(value, 1, Integer.MAX_VALUE);
    }

    protected Integer checkPositiveInt(final Integer value) {
        return checkIntRange(1, Integer.MAX_VALUE, value);
    }

    protected IntTag nonNegativeIntToTag(final Integer value) {
        return intRangeToTag(value, 0, Integer.MAX_VALUE);
    }

    protected Integer checkNonNegativeInt(final Integer value) {
        return checkIntRange(0, Integer.MAX_VALUE, value);
    }

    protected IntTag intRangeToTag(final Integer value, final int min, final int max) {
        return new IntTag(checkIntRange(min, max, value));
    }

    protected FloatTag positiveFloatToTag(final Float value) {
        return floatRangeToTag(value, 0, Float.MAX_VALUE);
    }

    protected FloatTag floatRangeToTag(final Float value, final float min, final float max) {
        return new FloatTag(checkFloatRange(min, max, value));
    }

    protected Float checkPositiveFloat(final Float value) {
        return checkFloatRange(0, Float.MAX_VALUE, value);
    }

    protected StringTag stringToTag(final String value, final int min, final int max) {
        return new StringTag(checkStringRange(min, max, value));
    }

    protected ByteTag unsignedByteToTag(final byte value) {
        if (value > UnsignedByteType.MAX_VALUE) {
            throw new IllegalArgumentException("Value out of range: " + value);
        }
        return new ByteTag(value);
    }

    protected StringTag componentToTag(final Tag value) {
        return componentToTag(value, Integer.MAX_VALUE);
    }

    protected Tag componentFromTag(final Tag value) {
        return componentFromTag(value, Integer.MAX_VALUE);
    }

    protected StringTag componentToTag(final Tag value, final int max) {
        final String json = outputSerializerVersion().toString(outputSerializerVersion().toComponent(value));
        return new StringTag(checkStringRange(0, max, json));
    }

    protected Tag componentFromTag(final Tag value, final int max) {
        if (value instanceof StringTag stringTag) {
            final String input = checkStringRange(0, max, stringTag.getValue());
            return inputSerializerVersion().toTag(inputSerializerVersion().toComponent(input));
        } else {
            return null;
        }
    }

    protected ListTag<StringTag> componentsToTag(final Tag[] value, final int maxLength) {
        checkIntRange(0, maxLength, value.length);
        final ListTag<StringTag> listTag = new ListTag<>(StringTag.class);
        for (final Tag tag : value) {
            final String json = outputSerializerVersion().toString(outputSerializerVersion().toComponent(tag));
            listTag.add(new StringTag(json));
        }
        return listTag;
    }

    protected Tag[] componentsFromTag(final Tag value, final int maxLength) {
        final ListTag<StringTag> listTag = (ListTag<StringTag>) value;
        checkIntRange(0, maxLength, listTag.size());

        final Tag[] components = new Tag[listTag.size()];
        for (int i = 0; i < listTag.size(); i++) {
            components[i] = inputSerializerVersion().toTag(inputSerializerVersion().toComponent(listTag.get(i).getValue()));
        }
        return components;
    }

    protected StringTag enumEntryToTag(Integer value, final String... values) {
        Preconditions.checkArgument(value >= 0 && value < values.length, "Enum value out of range: " + value);
        return new StringTag(values[value]);
    }

    protected Integer enumEntryFromTag(final StringTag value, final String... values) {
        final String string = value.getValue();
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(string)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Unknown enum value: " + string);
    }

    protected CompoundTag unitToTag() {
        return new CompoundTag();
    }

    protected CompoundTag nbtToTag(final CompoundTag tag) {
        return tag;
    }

    protected CompoundTag nbtWithIdToTag(final CompoundTag tag) {
        if (tag.getStringTag("id") == null) {
            throw new IllegalArgumentException("Missing id tag in nbt: " + tag);
        }
        return tag;
    }

    protected StringTag identifierToTag(final String value) {
        if (!Key.isValid(value)) {
            throw new IllegalArgumentException("Invalid identifier: " + value);
        }
        return new StringTag(value);
    }

    protected String identifierFromTag(final StringTag tag) {
        final String value = tag.getValue();
        if (!Key.isValid(value)) {
            throw new IllegalArgumentException("Invalid identifier: " + value);
        }
        return value;
    }

    protected StringTag dyeColorToTag(final Integer value) {
        return new StringTag(DyeColors.idToKey(value));
    }

    protected Integer dyeColorFromTag(final Tag tag) {
        return DyeColors.keyToId(((StringTag) tag).getValue());
    }

    // ---------------------------------------------------------------------------------------

    protected int checkIntRange(final int min, final int max, final int value) {
        Preconditions.checkArgument(value >= min && value <= max, "Value out of range: " + value);
        return value;
    }

    protected float checkFloatRange(final float min, final float max, final float value) {
        Preconditions.checkArgument(value >= min && value <= max, "Value out of range: " + value);
        return value;
    }

    protected String checkStringRange(final int min, final int max, final String value) {
        final int length = value.length();
        Preconditions.checkArgument(length >= min && length <= max, "Value out of range: " + value);
        return value;
    }

    // ---------------------------------------------------------------------------------------

    protected <T> void registerEmpty(final StructuredDataKey<T> key) {
        converters.put(key, new ConverterPair<>(null, null));
    }

    protected <T> void register(final StructuredDataKey<T> key, final SimpleDataConverter<T> dataConverter, final SimpleTagConverter<T> tagConverter) {
        final DataConverter<T> converter = ($, value) -> dataConverter.convert(value);
        converters.put(key, new ConverterPair<>(converter, (connection, tag) -> tagConverter.convert(tag)));
    }

    protected <T> void register(final StructuredDataKey<T> key, final DataConverter<T> dataConverter, final TagConverter<T> tagConverter) {
        converters.put(key, new ConverterPair<>(dataConverter, tagConverter));
    }

    protected @Nullable <T> DataConverter<T> dataConverter(final StructuredDataKey<T> key) {
        //noinspection unchecked
        final ConverterPair<T> converters = (ConverterPair<T>) this.converters.get(key);
        return converters != null ? converters.dataConverter : null;
    }

    protected @Nullable <T> TagConverter<T> tagConverter(final StructuredDataKey<T> key) {
        //noinspection unchecked
        final ConverterPair<T> converters = (ConverterPair<T>) this.converters.get(key);
        return converters != null ? converters.tagConverter : null;
    }

    @Override
    protected @Nullable SerializerVersion inputSerializerVersion() {
        return SerializerVersion.V1_20_3;
    }

    @Override
    protected @Nullable SerializerVersion outputSerializerVersion() {
        return SerializerVersion.V1_20_5;
    }

    @FunctionalInterface
    protected interface SimpleDataConverter<T> {

        Tag convert(T value);
    }

    @FunctionalInterface
    protected interface DataConverter<T> {

        Tag convert(UserConnection connection, T value);
    }

    @FunctionalInterface
    protected interface SimpleTagConverter<T> {

        T convert(final Tag tag);
    }

    @FunctionalInterface
    protected interface TagConverter<T> {

        T convert(UserConnection connection, Tag tag);
    }

    protected record ConverterPair<T>(DataConverter<T> dataConverter, TagConverter<T> tagConverter) {
    }
}
