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
package com.viaversion.viaversion.rewriter.item;

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.minecraft.GameProfile;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.AttributeModifiers1_21;
import com.viaversion.viaversion.api.minecraft.item.data.Bee;
import com.viaversion.viaversion.api.minecraft.item.data.Enchantments;
import com.viaversion.viaversion.api.minecraft.item.data.FilterableComponent;
import com.viaversion.viaversion.api.minecraft.item.data.FilterableString;
import com.viaversion.viaversion.api.minecraft.item.data.FireworkExplosion;
import com.viaversion.viaversion.util.SerializerVersion;
import com.viaversion.viaversion.util.Unit;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.lenni0451.mcstructs.converter.impl.v1_21_5.NbtConverter_v1_21_5;
import net.lenni0451.mcstructs.core.Identifier;
import net.lenni0451.mcstructs.itemcomponents.ItemComponent;
import net.lenni0451.mcstructs.itemcomponents.ItemComponentMap;
import net.lenni0451.mcstructs.itemcomponents.ItemComponentRegistry;
import net.lenni0451.mcstructs.itemcomponents.impl.Registries;
import net.lenni0451.mcstructs.itemcomponents.impl.v1_20_5.Types_v1_20_5;
import net.lenni0451.mcstructs.itemcomponents.impl.v1_21.Types_v1_21;
import net.lenni0451.mcstructs.itemcomponents.impl.v1_21_2.Types_v1_21_2;
import net.lenni0451.mcstructs.itemcomponents.impl.v1_21_4.Types_v1_21_4;
import net.lenni0451.mcstructs.itemcomponents.impl.v1_21_5.Types_v1_21_5;
import net.lenni0451.mcstructs.itemcomponents.registry.Registry;
import net.lenni0451.mcstructs.itemcomponents.registry.RegistryEntry;
import net.lenni0451.mcstructs.itemcomponents.registry.RegistryTag;
import net.lenni0451.mcstructs.text.TextComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ItemDataComponentConverter {

    private final Map<StructuredDataKey<?>, Converter<?, ?>> converters = new Reference2ObjectOpenHashMap<>();
    private final SerializerVersion serializerVersion;
    private final SerializerVersion mappedSerializerVersion;
    private final RegistryAccess registryAccess;

    public ItemDataComponentConverter(final SerializerVersion unmappedSerializerVersion, final SerializerVersion mappedSerializerVersion, final RegistryAccess registryAccess) {
        this.serializerVersion = unmappedSerializerVersion;
        this.mappedSerializerVersion = mappedSerializerVersion;
        this.registryAccess = registryAccess;
        this.direct(StructuredDataKey.CUSTOM_DATA, ItemComponentRegistry.V1_21_5.CUSTOM_DATA);
        this.direct(StructuredDataKey.MAX_STACK_SIZE, ItemComponentRegistry.V1_21_5.MAX_STACK_SIZE);
        this.direct(StructuredDataKey.MAX_DAMAGE, ItemComponentRegistry.V1_21_5.MAX_DAMAGE);
        this.direct(StructuredDataKey.DAMAGE, ItemComponentRegistry.V1_21_5.DAMAGE);
        this.unit(StructuredDataKey.UNBREAKABLE1_21_5, ItemComponentRegistry.V1_21_5.UNBREAKABLE);
        this.textComponent(StructuredDataKey.CUSTOM_NAME, ItemComponentRegistry.V1_21_5.CUSTOM_NAME);
        this.textComponent(StructuredDataKey.ITEM_NAME, ItemComponentRegistry.V1_21_5.ITEM_NAME);
        this.identifier(StructuredDataKey.ITEM_MODEL, ItemComponentRegistry.V1_21_5.ITEM_MODEL);
        this.register(StructuredDataKey.LORE, stringArrayToTextComponentArray(ItemComponentRegistry.V1_21_5.LORE));
        this.intToEnum(StructuredDataKey.RARITY, ItemComponentRegistry.V1_21_5.RARITY, Types_v1_20_5.Rarity.class);
        this.register(StructuredDataKey.ENCHANTMENTS1_21_5, this.convertEnchantmentsFunction(ItemComponentRegistry.V1_21_5.ENCHANTMENTS));
        this.notImplemented(StructuredDataKey.CAN_PLACE_ON1_21_5);
        this.notImplemented(StructuredDataKey.CAN_BREAK1_21_5);
        this.register(StructuredDataKey.ATTRIBUTE_MODIFIERS1_21_5, (attributes, mapped) -> {
            final List<Types_v1_21.AttributeModifier> result = new ArrayList<>(attributes.modifiers().length);
            for (final AttributeModifiers1_21.AttributeModifier modifier : attributes.modifiers()) {
                final AttributeModifiers1_21.ModifierData modifierData = modifier.modifier();
                result.add(new Types_v1_21.AttributeModifier(
                    registryAccess.attributeModifier(modifier.attribute(), mapped),
                    new Types_v1_21.AttributeModifier.EntityAttribute(Identifier.of(modifierData.id()), modifierData.amount(), Types_v1_21.AttributeModifier.EntityAttribute.Operation.values()[modifierData.operation()]),
                    Types_v1_20_5.AttributeModifier.Slot.values()[modifier.slotType()]
                ));
            }
            return new Result<>(ItemComponentRegistry.V1_21_5.ATTRIBUTE_MODIFIERS, result);
        });
        this.register(StructuredDataKey.CUSTOM_MODEL_DATA1_21_4, customModelData -> {
            final List<Float> floats = new ArrayList<>(customModelData.floats().length);
            for (final float f : customModelData.floats()) {
                floats.add(f);
            }
            final List<Boolean> booleans = new ArrayList<>(customModelData.booleans().length);
            for (final boolean b : customModelData.booleans()) {
                booleans.add(b);
            }
            return new Result<>(ItemComponentRegistry.V1_21_5.CUSTOM_MODEL_DATA, new Types_v1_21_4.CustomModelData(
                floats,
                booleans,
                Arrays.asList(customModelData.strings()),
                intArrayToIntList(customModelData.colors())
            ));
        });
        this.register(StructuredDataKey.TOOLTIP_DISPLAY, (tooltipDisplay, mapped) -> {
            final List<ItemComponent<?>> result = new ArrayList<>(tooltipDisplay.hiddenComponents().size());
            for (final int hiddenComponent : tooltipDisplay.hiddenComponents()) {
                result.add(dataComponentType(hiddenComponent, mapped));
            }
            return new Result<>(ItemComponentRegistry.V1_21_5.TOOLTIP_DISPLAY, new Types_v1_21_5.TooltipDisplay(tooltipDisplay.hideTooltip(), result));
        });
        this.direct(StructuredDataKey.REPAIR_COST, ItemComponentRegistry.V1_21_5.REPAIR_COST);
        this.unit(StructuredDataKey.CREATIVE_SLOT_LOCK, ItemComponentRegistry.V1_21_5.CREATIVE_SLOT_LOCK);
        this.direct(StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE, ItemComponentRegistry.V1_21_5.ENCHANTMENT_GLINT_OVERRIDE);
        this.register(StructuredDataKey.INTANGIBLE_PROJECTILE, value -> new Result<>(ItemComponentRegistry.V1_21_5.INTANGIBLE_PROJECTILE, null));
        this.register(StructuredDataKey.FOOD1_21_2, foodProperties -> {
            return new Result<>(ItemComponentRegistry.V1_21_5.FOOD, new Types_v1_21_2.Food(foodProperties.nutrition(), foodProperties.saturationModifier(), foodProperties.canAlwaysEat()));
        });
        this.notImplemented(StructuredDataKey.CONSUMABLE1_21_2);
        this.item(StructuredDataKey.USE_REMAINDER1_21_5, ItemComponentRegistry.V1_21_5.USE_REMAINDER);
        this.register(StructuredDataKey.USE_COOLDOWN, useCooldown -> {
            return new Result<>(ItemComponentRegistry.V1_21_5.USE_COOLDOWN, new Types_v1_21_2.UseCooldown(useCooldown.seconds(), Identifier.of(useCooldown.cooldownGroup())));
        });
        this.register(StructuredDataKey.DAMAGE_RESISTANT, damageResistant -> {
            return new Result<>(ItemComponentRegistry.V1_21_5.DAMAGE_RESISTANT, new Types_v1_21_2.DamageResistant(new RegistryTag(registries().damageType, Identifier.of(damageResistant.typesTagKey()))));
        });
        this.notImplemented(StructuredDataKey.TOOL1_21_5);
        this.register(StructuredDataKey.WEAPON, weapon -> {
            return new Result<>(ItemComponentRegistry.V1_21_5.WEAPON, new Types_v1_21_5.Weapon(weapon.itemDamagePerAttack(), weapon.disableBlockingForSeconds()));
        });
        this.register(StructuredDataKey.ENCHANTABLE, structuredData -> {
            return new Result<>(ItemComponentRegistry.V1_21_5.ENCHANTABLE, new Types_v1_21_2.Enchantable(structuredData));
        });
        this.notImplemented(StructuredDataKey.EQUIPPABLE1_21_5);
        this.notImplemented(StructuredDataKey.REPAIRABLE);
        this.unit(StructuredDataKey.GLIDER, ItemComponentRegistry.V1_21_5.GLIDER);
        this.identifier(StructuredDataKey.TOOLTIP_STYLE, ItemComponentRegistry.V1_21_5.TOOLTIP_STYLE);
        this.notImplemented(StructuredDataKey.DEATH_PROTECTION);
        this.notImplemented(StructuredDataKey.BLOCKS_ATTACKS);
        this.register(StructuredDataKey.STORED_ENCHANTMENTS1_21_5, this.convertEnchantmentsFunction(ItemComponentRegistry.V1_21_5.STORED_ENCHANTMENTS));
        this.register(StructuredDataKey.DYED_COLOR1_21_5, dyedColor -> {
            return new Result<>(ItemComponentRegistry.V1_21_5.DYED_COLOR, dyedColor.rgb());
        });
        this.direct(StructuredDataKey.MAP_COLOR, ItemComponentRegistry.V1_21_5.MAP_COLOR);
        this.direct(StructuredDataKey.MAP_ID, ItemComponentRegistry.V1_21_5.MAP_ID);
        this.register(StructuredDataKey.MAP_DECORATIONS, passthroughNbtCodec(ItemComponentRegistry.V1_21_5.MAP_DECORATIONS));
        this.intToEnum(StructuredDataKey.MAP_POST_PROCESSING, ItemComponentRegistry.V1_21_5.MAP_POST_PROCESSING, Types_v1_20_5.MapPostProcessing.class);
        this.register(StructuredDataKey.CHARGED_PROJECTILES1_21_5, convertItemArrayFunction(ItemComponentRegistry.V1_21_5.CHARGED_PROJECTILES));
        this.register(StructuredDataKey.BUNDLE_CONTENTS1_21_5, convertItemArrayFunction(ItemComponentRegistry.V1_21_5.BUNDLE_CONTENTS));
        this.notImplemented(StructuredDataKey.POTION_CONTENTS1_21_2);
        this.direct(StructuredDataKey.POTION_DURATION_SCALE, ItemComponentRegistry.V1_21_5.POTION_DURATION_SCALE);
        this.notImplemented(StructuredDataKey.SUSPICIOUS_STEW_EFFECTS);
        this.register(StructuredDataKey.WRITABLE_BOOK_CONTENT, pages -> {
            final List<Types_v1_20_5.RawFilteredPair<String>> resultPages = new ArrayList<>(pages.length);
            for (final FilterableString page : pages) {
                resultPages.add(new Types_v1_20_5.RawFilteredPair<>(page.raw(), page.filtered()));
            }
            return new Result<>(ItemComponentRegistry.V1_21_5.WRITABLE_BOOK_CONTENT, new Types_v1_20_5.WritableBook(resultPages));
        });
        this.register(StructuredDataKey.WRITTEN_BOOK_CONTENT, (writtenBook, mapped) -> {
            final Types_v1_20_5.RawFilteredPair<String> resultTitle = new Types_v1_20_5.RawFilteredPair<>(writtenBook.title().raw(), writtenBook.title().filtered());
            final List<Types_v1_20_5.RawFilteredPair<TextComponent>> resultPages = new ArrayList<>(writtenBook.pages().length);
            for (final FilterableComponent page : writtenBook.pages()) {
                resultPages.add(new Types_v1_20_5.RawFilteredPair<>(convertTextComponent(page.raw(), mapped), convertTextComponent(page.filtered(), mapped)));
            }
            return new Result<>(ItemComponentRegistry.V1_21_5.WRITTEN_BOOK_CONTENT, new Types_v1_20_5.WrittenBook(resultTitle, writtenBook.author(), writtenBook.generation(), resultPages, writtenBook.resolved()));
        });
        this.notImplemented(StructuredDataKey.TRIM1_21_5);
        this.register(StructuredDataKey.DEBUG_STICK_STATE, passthroughNbtCodec(ItemComponentRegistry.V1_21_5.DEBUG_STICK_STATE));
        this.direct(StructuredDataKey.ENTITY_DATA, ItemComponentRegistry.V1_21_5.ENTITY_DATA);
        this.direct(StructuredDataKey.BUCKET_ENTITY_DATA, ItemComponentRegistry.V1_21_5.BUCKET_ENTITY_DATA);
        this.direct(StructuredDataKey.BLOCK_ENTITY_DATA, ItemComponentRegistry.V1_21_5.BLOCK_ENTITY_DATA);
        this.notImplemented(StructuredDataKey.INSTRUMENT1_21_5);
        this.notImplemented(StructuredDataKey.PROVIDES_TRIM_MATERIAL);
        this.direct(StructuredDataKey.OMINOUS_BOTTLE_AMPLIFIER, ItemComponentRegistry.V1_21_5.OMINOUS_BOTTLE_AMPLIFIER);
        this.notImplemented(StructuredDataKey.JUKEBOX_PLAYABLE1_21_5);
        this.registryTag(StructuredDataKey.PROVIDES_BANNER_PATTERNS, ItemComponentRegistry.V1_21_5.PROVIDES_BANNER_PATTERNS, registries().bannerPattern);
        this.register(StructuredDataKey.RECIPES, passthroughNbtCodec(ItemComponentRegistry.V1_21_5.RECIPES));
        this.register(StructuredDataKey.LODESTONE_TRACKER, lodestoneTracker -> {
            final Types_v1_20_5.LodestoneTracker.GlobalPos targetGlobalPos;
            if (lodestoneTracker.position() != null) {
                final Types_v1_20_5.BlockPos targetPos = new Types_v1_20_5.BlockPos(lodestoneTracker.position().x(), lodestoneTracker.position().y(), lodestoneTracker.position().z());
                targetGlobalPos = new Types_v1_20_5.LodestoneTracker.GlobalPos(new RegistryEntry(registries().dimension, Identifier.of(lodestoneTracker.position().dimension())), targetPos);
            } else {
                targetGlobalPos = null;
            }
            return new Result<>(ItemComponentRegistry.V1_21_5.LODESTONE_TRACKER, new Types_v1_20_5.LodestoneTracker(targetGlobalPos, lodestoneTracker.tracked()));
        });
        this.register(StructuredDataKey.FIREWORK_EXPLOSION, fireworkExplosion -> {
            return new Result<>(ItemComponentRegistry.V1_21_5.FIREWORK_EXPLOSION, convertFireworkExplosion(fireworkExplosion));
        });
        this.register(StructuredDataKey.FIREWORKS, fireworks -> {
            final List<Types_v1_20_5.FireworkExplosions> resultExplosions = new ArrayList<>(fireworks.explosions().length);
            for (final FireworkExplosion explosion : fireworks.explosions()) {
                resultExplosions.add(convertFireworkExplosion(explosion));
            }
            return new Result<>(ItemComponentRegistry.V1_21_5.FIREWORKS, new Types_v1_20_5.Fireworks(fireworks.flightDuration(), resultExplosions));
        });
        this.register(StructuredDataKey.PROFILE, profile -> {
            final Map<String, List<Types_v1_20_5.GameProfile.Property>> resultProperties = new HashMap<>();
            for (final GameProfile.Property property : profile.properties()) {
                final List<Types_v1_20_5.GameProfile.Property> propertyList = resultProperties.computeIfAbsent(property.name(), k -> new ArrayList<>());
                propertyList.add(new Types_v1_20_5.GameProfile.Property(property.name(), property.value(), property.signature()));
            }
            return new Result<>(ItemComponentRegistry.V1_21_5.PROFILE, new Types_v1_20_5.GameProfile(profile.name(), profile.id(), resultProperties));
        });
        this.identifier(StructuredDataKey.NOTE_BLOCK_SOUND, ItemComponentRegistry.V1_21_5.NOTE_BLOCK_SOUND);
        this.notImplemented(StructuredDataKey.BANNER_PATTERNS);
        this.intToEnum(StructuredDataKey.BASE_COLOR, ItemComponentRegistry.V1_21_5.BASE_COLOR, Types_v1_20_5.DyeColor.class);
        this.notImplemented(StructuredDataKey.POT_DECORATIONS);
        this.register(StructuredDataKey.CONTAINER1_21_5, (items, mapped) -> {
            final List<Types_v1_20_5.ContainerSlot> resultSlots = new ArrayList<>(items.length);
            for (int i = 0; i < items.length; i++) {
                final Item item = items[i];
                if (!item.isEmpty()) {
                    resultSlots.add(new Types_v1_20_5.ContainerSlot(i, this.convertItemStack(item, mapped)));
                }
            }
            return new Result<>(ItemComponentRegistry.V1_21_5.CONTAINER, resultSlots);
        });
        this.register(StructuredDataKey.BLOCK_STATE, blockState -> {
            return new Result<>(ItemComponentRegistry.V1_21_5.BLOCK_STATE, blockState.properties());
        });
        this.register(StructuredDataKey.BEES, bees -> {
            final List<Types_v1_20_5.BeeData> resultBeeData = new ArrayList<>(bees.length);
            for (final Bee bee : bees) {
                resultBeeData.add(new Types_v1_20_5.BeeData(bee.entityData(), bee.ticksInHive(), bee.minTicksInHive()));
            }
            return new Result<>(ItemComponentRegistry.V1_21_5.BEES, resultBeeData);
        });
        this.register(StructuredDataKey.LOCK, passthroughNbtCodec(ItemComponentRegistry.V1_21_5.LOCK));
        this.register(StructuredDataKey.CONTAINER_LOOT, passthroughNbtCodec(ItemComponentRegistry.V1_21_5.CONTAINER_LOOT));
        this.notImplemented(StructuredDataKey.BREAK_SOUND);
        this.intToEnum(StructuredDataKey.VILLAGER_VARIANT, ItemComponentRegistry.V1_21_5.VILLAGER_VARIANT, Types_v1_21_5.VillagerVariant.class);
        this.notImplemented(StructuredDataKey.WOLF_VARIANT);
        this.notImplemented(StructuredDataKey.WOLF_SOUND_VARIANT);
        this.intToEnum(StructuredDataKey.WOLF_COLLAR, ItemComponentRegistry.V1_21_5.WOLF_COLLAR, Types_v1_20_5.DyeColor.class);
        this.intToEnum(StructuredDataKey.FOX_VARIANT, ItemComponentRegistry.V1_21_5.FOX_VARIANT, Types_v1_21_5.FoxVariant.class);
        this.intToEnum(StructuredDataKey.SALMON_SIZE, ItemComponentRegistry.V1_21_5.SALMON_SIZE, Types_v1_21_5.SalmonSize.class);
        this.intToEnum(StructuredDataKey.PARROT_VARIANT, ItemComponentRegistry.V1_21_5.PARROT_VARIANT, Types_v1_21_5.ParrotVariant.class);
        this.intToEnum(StructuredDataKey.TROPICAL_FISH_PATTERN, ItemComponentRegistry.V1_21_5.TROPICAL_FISH_PATTERN, Types_v1_21_5.TropicalFishPattern.class);
        this.intToEnum(StructuredDataKey.TROPICAL_FISH_BASE_COLOR, ItemComponentRegistry.V1_21_5.TROPICAL_FISH_BASE_COLOR, Types_v1_20_5.DyeColor.class);
        this.intToEnum(StructuredDataKey.TROPICAL_FISH_PATTERN_COLOR, ItemComponentRegistry.V1_21_5.TROPICAL_FISH_PATTERN_COLOR, Types_v1_20_5.DyeColor.class);
        this.intToEnum(StructuredDataKey.MOOSHROOM_VARIANT, ItemComponentRegistry.V1_21_5.MOOSHROOM_VARIANT, Types_v1_21_5.MooshroomVariant.class);
        this.intToEnum(StructuredDataKey.RABBIT_VARIANT, ItemComponentRegistry.V1_21_5.RABBIT_VARIANT, Types_v1_21_5.RabbitVariant.class);
        this.notImplemented(StructuredDataKey.PIG_VARIANT);
        this.notImplemented(StructuredDataKey.COW_VARIANT);
        this.notImplemented(StructuredDataKey.CHICKEN_VARIANT);
        this.notImplemented(StructuredDataKey.FROG_VARIANT);
        this.intToEnum(StructuredDataKey.HORSE_VARIANT, ItemComponentRegistry.V1_21_5.HORSE_VARIANT, Types_v1_21_5.HorseVariant.class);
        this.notImplemented(StructuredDataKey.PAINTING_VARIANT);
        this.intToEnum(StructuredDataKey.LLAMA_VARIANT, ItemComponentRegistry.V1_21_5.LLAMA_VARIANT, Types_v1_21_5.LlamaVariant.class);
        this.intToEnum(StructuredDataKey.AXOLOTL_VARIANT, ItemComponentRegistry.V1_21_5.AXOLOTL_VARIANT, Types_v1_21_5.AxolotlVariant.class);
        this.notImplemented(StructuredDataKey.CAT_VARIANT);
        this.intToEnum(StructuredDataKey.CAT_COLLAR, ItemComponentRegistry.V1_21_5.CAT_COLLAR, Types_v1_20_5.DyeColor.class);
        this.intToEnum(StructuredDataKey.SHEEP_COLOR, ItemComponentRegistry.V1_21_5.SHEEP_COLOR, Types_v1_20_5.DyeColor.class);
        this.intToEnum(StructuredDataKey.SHULKER_COLOR, ItemComponentRegistry.V1_21_5.SHULKER_COLOR, Types_v1_20_5.DyeColor.class);
    }

    public <I> Result<?> viaToMcStructs(final StructuredData<I> structuredData, final boolean mapped) {
        final Converter<?, ?> conversionFunction = this.converters.get(structuredData.key());
        if (conversionFunction == null) {
            throw new IllegalArgumentException("Unknown structured data key: " + structuredData.key());
        }
        //noinspection unchecked
        return ((Converter<I, ?>) conversionFunction).convert(structuredData.value(), mapped);
    }

    private <I, O> void register(final StructuredDataKey<I> key, final SimpleConverter<I, O> converter) {
        this.converters.put(key, converter);
    }

    private <I, O> void register(final StructuredDataKey<I> key, final Converter<I, O> converter) {
        this.converters.put(key, converter);
    }

    private <I> void direct(final StructuredDataKey<I> key, final ItemComponent<I> result) {
        this.register(key, value -> new Result<>(result, value));
    }

    private <I> void notImplemented(final StructuredDataKey<I> key) {
        this.register(key, value -> null);
    }

    private void unit(final StructuredDataKey<Unit> key, final ItemComponent<?> result) {
        this.register(key, value -> new Result<>(result, null));
    }

    private <O extends Enum<O>> void intToEnum(final StructuredDataKey<Integer> key, final ItemComponent<O> result, final Class<O> enumClass) {
        this.register(key, ordinal -> {
            final O[] enumConstants = enumClass.getEnumConstants();
            return new Result<>(result, enumConstants[ordinal]);
        });
    }

    private void textComponent(final StructuredDataKey<Tag> key, final ItemComponent<TextComponent> result) {
        this.register(key, (tag, mapped) -> new Result<>(result, convertTextComponent(tag, mapped)));
    }

    private void item(final StructuredDataKey<Item> key, final ItemComponent<Types_v1_20_5.ItemStack> result) {
        this.register(key, (item, mapped) -> new Result<>(result, this.convertItemStack(item, mapped)));
    }

    private void identifier(final StructuredDataKey<String> key, final ItemComponent<Identifier> result) {
        this.register(key, value -> new Result<>(result, Identifier.of(value)));
    }

    private void registryTag(final StructuredDataKey<String> key, final ItemComponent<RegistryTag> result, final Registry registry) {
        this.register(key, value -> new Result<>(result, new RegistryTag(registry, Identifier.of(value))));
    }

    private Converter<Tag[], List<TextComponent>> stringArrayToTextComponentArray(final ItemComponent<List<TextComponent>> itemComponent) {
        return (tags, mapped) -> {
            final List<TextComponent> textComponents = new ArrayList<>(tags.length);
            for (final Tag tag : tags) {
                textComponents.add(convertTextComponent(tag, mapped));
            }
            return new Result<>(itemComponent, textComponents);
        };
    }

    private static <I extends Tag, O> SimpleConverter<I, O> passthroughNbtCodec(final ItemComponent<O> itemComponent) {
        return tag -> new Result<>(itemComponent, itemComponent.getCodec().deserialize(NbtConverter_v1_21_5.INSTANCE, tag).getOrThrow());
    }

    private static Types_v1_20_5.FireworkExplosions convertFireworkExplosion(final FireworkExplosion explosion) {
        final Types_v1_20_5.FireworkExplosions.ExplosionShape explosionShape = Types_v1_20_5.FireworkExplosions.ExplosionShape.values()[explosion.shape()];
        return new Types_v1_20_5.FireworkExplosions(explosionShape, intArrayToIntList(explosion.colors()), intArrayToIntList(explosion.fadeColors()), explosion.hasTrail(), explosion.hasTwinkle());
    }

    private TextComponent convertTextComponent(final Tag tag, final boolean mapped) {
        if (tag != null) {
            return mapped ? this.mappedSerializerVersion.toComponent(tag) : this.serializerVersion.toComponent(tag);
        }
        return null;
    }

    private static List<Integer> intArrayToIntList(final int[] array) {
        final List<Integer> list = new ArrayList<>(array.length);
        for (final int i : array) {
            list.add(i);
        }
        return list;
    }

    private Converter<Item[], List<Types_v1_20_5.ItemStack>> convertItemArrayFunction(final ItemComponent<List<Types_v1_20_5.ItemStack>> itemComponent) {
        return (items, mapped) -> {
            final List<Types_v1_20_5.ItemStack> itemStacks = new ArrayList<>(items.length);
            for (Item item : items) {
                itemStacks.add(this.convertItemStack(item, mapped));
            }
            return new Result<>(itemComponent, itemStacks);
        };
    }

    private SimpleConverter<Enchantments, Map<RegistryEntry, Integer>> convertEnchantmentsFunction(final ItemComponent<Map<RegistryEntry, Integer>> itemComponent) {
        return enchantments -> {
            final Map<RegistryEntry, Integer> enchantmentMap = new HashMap<>();
            for (final Int2IntMap.Entry entry : enchantments.enchantments().int2IntEntrySet()) {
                enchantmentMap.put(this.registryAccess.enchantment(entry.getIntKey()), entry.getIntValue());
            }
            return new Result<>(itemComponent, enchantmentMap);
        };
    }

    private Types_v1_20_5.ItemStack convertItemStack(final Item item, final boolean mapped) {
        final Types_v1_20_5.ItemStack itemStack = new Types_v1_20_5.ItemStack(this.registryAccess.item(item.identifier(), mapped), item.amount(), new ItemComponentMap(ItemComponentRegistry.V1_21_5));
        final ItemComponentMap itemComponentMap = itemStack.getComponents();

        for (final StructuredData<?> structuredData : item.dataContainer().data().values()) {
            if (structuredData.isPresent()) {
                final Result<?> itemComponent = viaToMcStructs(structuredData, mapped);
                if (itemComponent != null) {
                    setGeneric(itemComponentMap, itemComponent);
                }
            } else {
                itemComponentMap.markForRemoval(ItemComponentRegistry.V1_21_5.getComponentList().getById(structuredData.id()).get());
            }
        }

        return itemStack;
    }

    private <O> void setGeneric(final ItemComponentMap map, final Result<O> result) {
        map.set(result.type(), result.value());
    }

    private Registries registries() {
        return ItemComponentRegistry.V1_21_5.getRegistries();
    }

    private ItemComponent<?> dataComponentType(final int id, final boolean mapped) {
        final String identifier = this.registryAccess.dataComponentType(id, mapped);
        return mapped ? this.mappedSerializerVersion.getItemComponent(identifier) : this.serializerVersion.getItemComponent(identifier);
    }

    /**
     * Converts one of our own data types to MCStructs data types.
     *
     * @param <I> input ViaVersion data type
     * @param <O> output MCStructs data type
     */
    @FunctionalInterface
    public interface Converter<I, O> {

        Result<O> convert(I value, boolean mapped);
    }

    /**
     * Converts one of our own data types to MCStructs data types, assumed to not use the {@code mapped} value.
     *
     * @param <I> input ViaVersion data type
     * @param <O> output MCStructs data type
     */
    @FunctionalInterface
    public interface SimpleConverter<I, O> extends Converter<I, O> {

        Result<O> convert(I value);

        @Override
        default Result<O> convert(final I value, final boolean mapped) {
            return this.convert(value);
        }
    }

    /**
     * Result of a conversion operation.
     *
     * @param type MCStructs data type
     * @param value resulting value
     * @param <O> output type
     */
    public record Result<O>(ItemComponent<O> type, @Nullable O value) {
    }

    public interface RegistryAccess {

        RegistryEntry item(int id, boolean mapped);

        RegistryEntry enchantment(int id);

        RegistryEntry attributeModifier(int id, boolean mapped);

        String dataComponentType(int id, boolean mapped);

        static RegistryAccess of(final List<String> enchantments, final Registries registries, final MappingData mappingData) {
            return new RegistryAccessImpl(enchantments, registries, mappingData);
        }
    }
}
