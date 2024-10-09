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
import com.viaversion.viaversion.api.minecraft.item.data.FoodEffect;
import com.viaversion.viaversion.api.minecraft.item.data.FoodProperties;
import com.viaversion.viaversion.api.minecraft.item.data.Instrument;
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
import com.viaversion.viaversion.rewriter.ComponentRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.Either;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.SerializerVersion;
import com.viaversion.viaversion.util.UUIDUtil;
import com.viaversion.viaversion.util.Unit;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ComponentRewriter1_20_5<C extends ClientboundPacketType> extends ComponentRewriter<C> {

    private final Map<StructuredDataKey<?>, ConverterPair<?>> converters = new Reference2ObjectOpenHashMap<>();
    private final StructuredDataType structuredDataType;

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
        register(StructuredDataKey.UNBREAKABLE, this::unbreakableToTag, this::unbreakableFromTag);
        register(StructuredDataKey.CUSTOM_NAME, this::customNameToTag, this::customNameFromTag);
        register(StructuredDataKey.ITEM_NAME, this::itemNameToTag, this::itemNameFromTag);
        register(StructuredDataKey.LORE, this::loreToTag, this::loreFromTag);
        register(StructuredDataKey.RARITY, this::rarityToTag, this::rarityFromTag);
        register(StructuredDataKey.ENCHANTMENTS, this::enchantmentsToTag, this::enchantmentsFromTag);
        register(StructuredDataKey.CAN_PLACE_ON, this::canPlaceOnToTag, this::canPlaceOnFromTag);
        register(StructuredDataKey.CAN_BREAK, this::canBreakToTag, this::canBreakFromTag);
        register(StructuredDataKey.ATTRIBUTE_MODIFIERS1_20_5, this::attributeModifiersToTag, this::attributeModifiersFromTag);
        register(StructuredDataKey.CUSTOM_MODEL_DATA, this::customModelDataToTag, this::customModelDataFromTag);
        register(StructuredDataKey.HIDE_ADDITIONAL_TOOLTIP, this::hideAdditionalTooltipToTag, this::hideAdditionalTooltipFromTag);
        register(StructuredDataKey.HIDE_TOOLTIP, this::hideTooltipToTag, this::hideTooltipFromTag);
        register(StructuredDataKey.REPAIR_COST, this::repairCostToTag, this::repairCostFromTag);
        register(StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE, this::enchantmentGlintOverrideToTag, this::enchantmentGlintOverrideFromTag);
        registerEmpty(StructuredDataKey.CREATIVE_SLOT_LOCK);
        register(StructuredDataKey.INTANGIBLE_PROJECTILE, this::intangibleProjectileToTag, this::intangibleProjectileFromTag);
        register(StructuredDataKey.FOOD1_20_5, this::foodToTag, this::foodFromTag);
        register(StructuredDataKey.FIRE_RESISTANT, this::fireResistantToTag, this::fireResistantFromTag);
        register(StructuredDataKey.TOOL, this::toolToTag, this::toolFromTag);
        register(StructuredDataKey.STORED_ENCHANTMENTS, this::storedEnchantmentsToTag, this::storedEnchantmentsFromTag);
        register(StructuredDataKey.DYED_COLOR, this::dyedColorToTag, this::dyedColorFromTag);
        register(StructuredDataKey.MAP_COLOR, this::mapColorToTag, this::mapColorFromTag);
        register(StructuredDataKey.MAP_ID, this::mapIdToTag, this::mapIdFromTag);
        register(StructuredDataKey.MAP_DECORATIONS, this::mapDecorationsToTag, this::mapDecorationsFromTag);
        registerEmpty(StructuredDataKey.MAP_POST_PROCESSING);
        register(StructuredDataKey.CHARGED_PROJECTILES1_20_5, this::chargedProjectilesToTag, this::chargedProjectilesFromTag);
        register(StructuredDataKey.BUNDLE_CONTENTS1_20_5, this::bundleContentsToTag, this::bundleContentsFromTag);
        register(StructuredDataKey.POTION_CONTENTS, this::potionContentsToTag, this::potionContentsFromTag);
        register(StructuredDataKey.SUSPICIOUS_STEW_EFFECTS, this::suspiciousStewEffectsToTag, this::suspiciousStewEffectsFromTag);
        register(StructuredDataKey.WRITABLE_BOOK_CONTENT, this::writableBookContentToTag, this::writableBookContentFromTag);
        register(StructuredDataKey.WRITTEN_BOOK_CONTENT, this::writtenBookContentToTag, this::writtenBookContentFromTag);
        register(StructuredDataKey.TRIM, this::trimToTag);
        register(StructuredDataKey.DEBUG_STICK_STATE, this::debugStickRateToTag);
        register(StructuredDataKey.ENTITY_DATA, this::entityDataToTag);
        register(StructuredDataKey.BUCKET_ENTITY_DATA, this::bucketEntityDataToTag);
        register(StructuredDataKey.BLOCK_ENTITY_DATA, this::blockEntityDataToTag);
        register(StructuredDataKey.INSTRUMENT, this::instrumentToTag);
        register(StructuredDataKey.OMINOUS_BOTTLE_AMPLIFIER, this::ominousBottleAmplifierToTag);
        register(StructuredDataKey.RECIPES, this::recipesToTag);
        register(StructuredDataKey.LODESTONE_TRACKER, this::lodestoneTrackerToTag);
        register(StructuredDataKey.FIREWORK_EXPLOSION, this::fireworkExplosionToTag);
        register(StructuredDataKey.FIREWORKS, this::fireworksToTag);
        register(StructuredDataKey.PROFILE, this::profileToTag);
        register(StructuredDataKey.NOTE_BLOCK_SOUND, this::noteBlockSoundToTag);
        register(StructuredDataKey.BANNER_PATTERNS, this::bannerPatternsToTag);
        register(StructuredDataKey.BASE_COLOR, this::baseColorToTag);
        register(StructuredDataKey.POT_DECORATIONS, this::potDecorationsToTag);
        register(StructuredDataKey.CONTAINER1_20_5, this::containerToTag);
        register(StructuredDataKey.BLOCK_STATE, this::blockStateToTag);
        register(StructuredDataKey.BEES, this::beesToTag);
        register(StructuredDataKey.LOCK, this::lockToTag);
        register(StructuredDataKey.CONTAINER_LOOT, this::containerLootToTag);
    }

    @Override
    protected void handleHoverEvent(final UserConnection connection, final CompoundTag hoverEventTag) {
        super.handleHoverEvent(connection, hoverEventTag);

        final StringTag actionTag = hoverEventTag.getStringTag("action");
        if (actionTag == null) {
            return;
        }

        if (actionTag.getValue().equals("show_item")) {
            final Tag valueTag = hoverEventTag.remove("value");
            if (valueTag != null) { // Convert legacy hover event to new format for rewriting (Doesn't handle all cases, but good enough)
                final CompoundTag tag = ComponentUtil.deserializeShowItem(valueTag, SerializerVersion.V1_20_3);
                final CompoundTag contentsTag = new CompoundTag();
                contentsTag.put("id", tag.getStringTag("id"));
                contentsTag.put("count", new IntTag(tag.getByte("Count")));
                if (tag.get("tag") instanceof CompoundTag) {
                    contentsTag.put("tag", new StringTag(SerializerVersion.V1_20_3.toSNBT(tag.getCompoundTag("tag"))));
                }
                hoverEventTag.put("contents", contentsTag);
            }

            final CompoundTag contentsTag = hoverEventTag.getCompoundTag("contents");
            if (contentsTag == null) {
                return;
            }

            final StringTag idTag = contentsTag.getStringTag("id");
            if (idTag == null) {
                return;
            }

            int itemId = Protocol1_20_3To1_20_5.MAPPINGS.getFullItemMappings().id(idTag.getValue());
            if (itemId == -1) {
                // Default to stone (anything that is not air)
                itemId = 1;
            }

            final StringTag tag = (StringTag) contentsTag.remove("tag");
            final CompoundTag tagTag;
            try {
                tagTag = tag != null ? (CompoundTag) SerializerVersion.V1_20_3.toTag(tag.getValue()) : null;
            } catch (final Exception e) {
                if (!Via.getConfig().isSuppressConversionWarnings()) {
                    protocol.getLogger().log(Level.WARNING, "Error reading NBT in show_item: " + contentsTag, e);
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
                    contentsTag.putString("id", identifier);
                }
            } else {
                // Cannot be air
                contentsTag.putString("id", "minecraft:stone");
            }

            final Map<StructuredDataKey<?>, StructuredData<?>> data = structuredItem.dataContainer().data();
            if (!data.isEmpty()) {
                final CompoundTag components;
                try {
                    components = toTag(connection, data, false);
                } catch (final Exception e) {
                    if (!Via.getConfig().isSuppressConversionWarnings()) {
                        protocol.getLogger().log(Level.WARNING, "Error writing components in show_item!", e);
                    }
                    return;
                }
                contentsTag.put("components", components);
            }
        } else if (actionTag.getValue().equals("show_entity")) {
            final Tag valueTag = hoverEventTag.remove("value");
            if (valueTag != null) { // Convert legacy hover event to new format for rewriting (Doesn't handle all cases, but good enough)
                final CompoundTag tag = ComponentUtil.deserializeShowItem(valueTag, SerializerVersion.V1_20_3);
                final CompoundTag contentsTag = new CompoundTag();
                contentsTag.put("type", tag.getStringTag("type"));
                contentsTag.put("id", tag.getStringTag("id"));
                contentsTag.put("name", SerializerVersion.V1_20_3.toTag(SerializerVersion.V1_20_3.toComponent(tag.getString("name"))));
                hoverEventTag.put("contents", contentsTag);
            }

            final CompoundTag contentsTag = hoverEventTag.getCompoundTag("contents");
            if (contentsTag == null) {
                return;
            }

            if (this.protocol.getMappingData().getEntityMappings().mappedId(contentsTag.getString("type")) == -1) {
                contentsTag.put("type", new StringTag("pig"));
            }
        }
    }

    public CompoundTag toTag(final UserConnection connection, final Map<StructuredDataKey<?>, StructuredData<?>> data, final boolean empty) {
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
                if (empty) {
                    // Theoretically not needed here, but we'll keep it for consistency
                    tag.put("!" + identifier, new CompoundTag());
                    continue;
                }
                throw new IllegalArgumentException("Empty structured data: " + identifier);
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

    public List<StructuredData<?>> toData(final CompoundTag tag) {
        final List<StructuredData<?>> list = new ArrayList<>();
        if (tag != null) {
            for (final Map.Entry<String, Tag> entry : tag.entrySet()) {
                final StructuredData<?> data = readFromTag(entry.getKey(), entry.getValue());
                list.add(data);
            }
        }
        return list;
    }

    public StructuredData<?> readFromTag(final String identifier, final Tag tag) {
        final int id = protocol.getMappingData().getDataComponentSerializerMappings().mappedId(identifier);
        Preconditions.checkArgument(id != -1, "Unknown data component: %s", identifier);
        final StructuredDataKey<?> key = structuredDataType.key(id);
        return readFromTag(key, id, tag);
    }

    private <T> StructuredData<T> readFromTag(final StructuredDataKey<T> key, final int id, final Tag tag) {
        final TagConverter<T> converter = tagConverter(key);
        Preconditions.checkNotNull(converter, "No converter found for: %s", key);
        return StructuredData.of(key, converter.convert(tag), id);
    }

    private String mappedIdentifier(final int id) {
        return Protocol1_20_3To1_20_5.MAPPINGS.getFullItemMappings().mappedIdentifier(id);
    }

    private int mappedId(final String identifier) {
        return Protocol1_20_3To1_20_5.MAPPINGS.getFullItemMappings().mappedId(identifier);
    }

    // ---------------------------------------------------------------------------------------
    // Conversion methods, can be overridden in future protocols to handle new changes

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
                final List<StatePropertyMatcher> state = fromState(predicateTag.getCompoundTag("state"));

                list.add(new BlockPredicate(holderSet, state.toArray(StatePropertyMatcher[]::new), predicateTag.getCompoundTag("nbt")));
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

    protected List<StatePropertyMatcher> fromState(final CompoundTag value) {
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
        return list;
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

    protected CompoundTag foodToTag(final FoodProperties value) {
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
            for (final FoodEffect foodEffect : value.possibleEffects()) {
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

    protected FoodProperties foodFromTag(final Tag tag) {
        final CompoundTag value = (CompoundTag) tag;

        final int nutrition = checkNonNegativeInt(value.getInt("nutrition"));
        final float saturation = value.getFloat("saturation");
        final boolean canAlwaysEat = value.getBoolean("can_always_eat", false);
        final float eatSeconds = checkPositiveFloat(value.getFloat("eat_seconds", 1.6F));
        final ListTag<CompoundTag> effects = value.getListTag("effects", CompoundTag.class);
        final List<FoodEffect> list = new ArrayList<>();
        if (effects != null) {
            for (final CompoundTag effectTag : effects) {
                final PotionEffect effect = potionEffectFromTag(effectTag.getCompoundTag("effect"));
                final float probability = effectTag.getFloat("probability", 1.0F);
                list.add(new FoodEffect(effect, probability));
            }
        }
        return new FoodProperties(nutrition, saturation, canAlwaysEat, eatSeconds, null, list.toArray(FoodEffect[]::new));
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

    protected Item[] chargedProjectilesFromTag(final Tag tag) {
        final ListTag<CompoundTag> value = (ListTag<CompoundTag>) tag;
        return itemArrayFromTag(value);
    }

    protected ListTag<CompoundTag> bundleContentsToTag(final UserConnection connection, final Item[] value) {
        return itemArrayToTag(connection, value);
    }

    protected Item[] bundleContentsFromTag(final Tag tag) {
        final ListTag<CompoundTag> value = (ListTag<CompoundTag>) tag;
        return itemArrayFromTag(value);
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
        for (final PotionEffect effect : value.customEffects()) {
            potionEffectToTag(tag, effect);
        }
        return tag;
    }

    protected PotionContents potionContentsFromTag(final Tag tag) {
        final CompoundTag value = (CompoundTag) tag;

        final int potion = Potions1_20_5.keyToId(value.getString("potion", ""));
        final Integer customColor = value.getInt("custom_color");
        final List<PotionEffect> effects = new ArrayList<>();
        for (final Map.Entry<String, Tag> entry : value.entrySet()) {
            final PotionEffect effect = potionEffectFromTag((CompoundTag) entry.getValue());
            effects.add(effect);
        }
        return new PotionContents(potion, customColor, effects.toArray(PotionEffect[]::new));
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

    protected CompoundTag writableBookContentToTag(final FilterableString[] value) {
        final CompoundTag tag = new CompoundTag();
        if (value == null) {
            return tag;
        }

        if (value.length > 100) {
            throw new IllegalArgumentException("Too many pages: " + value.length);
        }

        final ListTag<CompoundTag> pagesTag = new ListTag<>(CompoundTag.class);
        for (final FilterableString page : value) {
            final CompoundTag pageTag = new CompoundTag();
            filterableStringToTag(pageTag, page, 1024);
            pagesTag.add(pageTag);
        }
        tag.put("pages", pagesTag);
        return tag;
    }

    protected FilterableString[] writableBookContentFromTag(final Tag tag) {
        final CompoundTag value = (CompoundTag) tag;

        final ListTag<CompoundTag> pagesTag = value.getListTag("pages", CompoundTag.class);
        if (pagesTag == null) {
            return null;
        }

        final FilterableString[] pages = new FilterableString[pagesTag.size()];
        for (int i = 0; i < pagesTag.size(); i++) {
            pages[i] = filterableStringFromTag(pagesTag.get(i));
        }
        return pages;
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
            for (final Int2ObjectMap.Entry<String> entry : armorTrimMaterial.overrideArmorMaterials().int2ObjectEntrySet()) {
                final String materialKey = ArmorMaterials1_20_5.idToKey(entry.getIntKey());
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

    protected CompoundTag debugStickRateToTag(final CompoundTag value) {
        return value;
    }

    protected CompoundTag entityDataToTag(final CompoundTag value) {
        return nbtWithIdToTag(value);
    }

    protected CompoundTag bucketEntityDataToTag(final CompoundTag value) {
        return nbtToTag(value);
    }

    protected CompoundTag blockEntityDataToTag(final CompoundTag value) {
        return nbtWithIdToTag(value);
    }

    protected Tag instrumentToTag(final Holder<Instrument> value) {
        if (value.hasId()) {
            return new StringTag(Instruments1_20_3.idToKey(value.id()));
        }

        final Instrument instrument = value.value();
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
        }

        tag.put("use_duration", positiveIntToTag(instrument.useDuration()));
        tag.put("range", positiveFloatToTag(instrument.range()));
        return tag;
    }

    protected IntTag ominousBottleAmplifierToTag(final Integer value) {
        return intRangeToTag(value, 0, 4);
    }

    protected Tag recipesToTag(final Tag value) {
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

    protected StringTag noteBlockSoundToTag(final String value) {
        return identifierToTag(value);
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

    protected StringTag baseColorToTag(final Integer value) {
        return dyeColorToTag(value);
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

    protected ListTag<CompoundTag> containerToTag(final UserConnection connection, final Item[] value) {
        final ListTag<CompoundTag> tag = new ListTag<>(CompoundTag.class);
        final ListTag<CompoundTag> items = itemArrayToTag(connection, value);
        for (int i = 0; i < items.size(); i++) {
            final CompoundTag itemTag = new CompoundTag();
            itemTag.putInt("slot", i);
            itemTag.put("item", items.get(i));
            tag.add(itemTag);
        }
        return tag;
    }

    protected CompoundTag blockStateToTag(final BlockStateProperties value) {
        final CompoundTag tag = new CompoundTag();
        for (final Map.Entry<String, String> entry : value.properties().entrySet()) {
            tag.putString(entry.getKey(), entry.getValue());
        }
        return tag;
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

    protected StringTag lockToTag(final Tag value) {
        return (StringTag) value;
    }

    protected CompoundTag containerLootToTag(final CompoundTag value) {
        return value; // Handled by the item rewriter
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

    protected Item[] itemArrayFromTag(final ListTag<CompoundTag> tag) {
        final Item[] items = new Item[tag.size()];
        for (int i = 0; i < tag.size(); i++) {
            items[i] = itemFromTag(tag.get(i));
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
        tag.put("components", toTag(connection, components, true));
    }

    protected Item itemFromTag(final CompoundTag tag) {
        final int id = mappedId(tag.getString("id", ""));
        final int amount = checkPositiveInt(tag.getInt("count", 1));
        final List<StructuredData<?>> components = toData(tag.getCompoundTag("components"));

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

    // ---------------------------------------------------------------------------------------

    protected Integer asInt(final Tag tag) {
        return ((NumberTag) tag).asInt();
    }

    protected Boolean asBoolean(final Tag tag) {
        return ((ByteTag) tag).asByte() != 0;
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
        final String json = serializerVersion().toString(serializerVersion().toComponent(value));
        return new StringTag(checkStringRange(0, max, json));
    }

    protected Tag componentFromTag(final Tag value, final int max) {
        if (value instanceof StringTag stringTag) {
            return serializerVersion().toTag(checkStringRange(0, max, stringTag.getValue()));
        } else {
            return null;
        }
    }

    protected ListTag<StringTag> componentsToTag(final Tag[] value, final int maxLength) {
        checkIntRange(0, maxLength, value.length);
        final ListTag<StringTag> listTag = new ListTag<>(StringTag.class);
        for (final Tag tag : value) {
            final String json = serializerVersion().toString(serializerVersion().toComponent(tag));
            listTag.add(new StringTag(json));
        }
        return listTag;
    }

    protected Tag[] componentsFromTag(final Tag value, final int maxLength) { // TODO verify
        final ListTag<StringTag> listTag = (ListTag<StringTag>) value;
        checkIntRange(0, maxLength, listTag.size());

        final Tag[] components = new Tag[listTag.size()];
        for (int i = 0; i < listTag.size(); i++) {
            components[i] = serializerVersion().toTag(listTag.get(i).getValue());
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

    protected StringTag dyeColorToTag(final Integer value) {
        return new StringTag(DyeColors.colorById(value));
    }

    // ---------------------------------------------------------------------------------------

    private int checkIntRange(final int min, final int max, final int value) {
        Preconditions.checkArgument(value >= min && value <= max, "Value out of range: " + value);
        return value;
    }

    private float checkFloatRange(final float min, final float max, final float value) {
        Preconditions.checkArgument(value >= min && value <= max, "Value out of range: " + value);
        return value;
    }

    private String checkStringRange(final int min, final int max, final String value) {
        final int length = value.length();
        Preconditions.checkArgument(length >= min && length <= max, "Value out of range: " + value);
        return value;
    }

    // ---------------------------------------------------------------------------------------

    protected <T> void registerEmpty(final StructuredDataKey<T> key) {
        converters.put(key, new ConverterPair<>(null, null));
    }

    protected <T> void register(final StructuredDataKey<T> key, final DataConverter<T> dataConverter) { // TODO Remove this method
        converters.put(key, new ConverterPair<>(dataConverter, null));
    }

    protected <T> void register(final StructuredDataKey<T> key, final SimpleDataConverter<T> dataConverter) {
        final DataConverter<T> converter = ($, value) -> dataConverter.convert(value);
        converters.put(key, new ConverterPair<>(converter, null));
    }

    protected <T> void register(final StructuredDataKey<T> key, final SimpleDataConverter<T> dataConverter, final TagConverter<T> tagConverter) {
        final DataConverter<T> converter = ($, value) -> dataConverter.convert(value);
        converters.put(key, new ConverterPair<>(converter, tagConverter));
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

    public SerializerVersion serializerVersion() {
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
    protected interface TagConverter<T> {

        T convert(final Tag tag);
    }

    private record ConverterPair<T>(DataConverter<T> dataConverter, TagConverter<T> tagConverter) {
    }
}
