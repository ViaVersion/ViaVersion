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
import com.viaversion.viaversion.api.minecraft.GameProfile;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.*;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntMap;
import com.viaversion.viaversion.libs.fastutil.objects.Reference2ObjectOpenHashMap;
import com.viaversion.viaversion.util.Pair;
import net.lenni0451.mcstructs.converter.impl.v1_21_5.NbtConverter_v1_21_5;
import net.lenni0451.mcstructs.core.Identifier;
import net.lenni0451.mcstructs.itemcomponents.ItemComponent;
import net.lenni0451.mcstructs.itemcomponents.ItemComponentMap;
import net.lenni0451.mcstructs.itemcomponents.ItemComponentRegistry;
import net.lenni0451.mcstructs.itemcomponents.impl.v1_20_5.Types_v1_20_5;
import net.lenni0451.mcstructs.itemcomponents.impl.v1_21_2.Types_v1_21_2;
import net.lenni0451.mcstructs.itemcomponents.impl.v1_21_4.Types_v1_21_4;
import net.lenni0451.mcstructs.itemcomponents.impl.v1_21_5.Types_v1_21_5;
import net.lenni0451.mcstructs.nbt.tags.CompoundTag;
import net.lenni0451.mcstructs.text.TextComponent;
import net.lenni0451.mcstructs.text.serializer.TextComponentCodec;

import java.util.*;
import java.util.function.Function;

public class ItemDataComponentConverter {

    private static final Function<StructuredData<?>, Pair<ItemComponent<?>, Object>> NOT_IMPLEMENTED = structuredData -> null;

    private static Function<StructuredData<?>, Pair<ItemComponent<?>, Object>> passthroughValueFunction(final ItemComponent<?> itemComponent) {
        return structuredData -> new Pair<>(itemComponent, structuredData.value());
    }

    private static Function<StructuredData<?>, Pair<ItemComponent<?>, Object>> noValueFunction(final ItemComponent<?> itemComponent) {
        return structuredData -> new Pair<>(itemComponent, null);
    }

    private static Function<StructuredData<?>, Pair<ItemComponent<?>, Object>> convertNbtFunction(final ItemComponent<?> itemComponent) {
        return structuredData -> new Pair<>(itemComponent, NbtConverter.viaToMcStructs((Tag) structuredData.value()));
    }

    private static Function<StructuredData<?>, Pair<ItemComponent<?>, Object>> convertTextComponentFunction(final ItemComponent<?> itemComponent) {
        return structuredData -> new Pair<>(itemComponent, convertTextComponent((Tag) structuredData.value()));
    }

    private static Function<StructuredData<?>, Pair<ItemComponent<?>, Object>> convertTextComponentArrayFunction(final ItemComponent<?> itemComponent) {
        return structuredData -> {
            final Tag[] tags = (Tag[]) structuredData.value();
            final List<TextComponent> textComponents = new ArrayList<>(tags.length);
            for (Tag tag : tags) {
                textComponents.add(convertTextComponent(tag));
            }
            return new Pair<>(itemComponent, textComponents);
        };
    }

    private static Function<StructuredData<?>, Pair<ItemComponent<?>, Object>> stringToIdentifierFunction(final ItemComponent<?> itemComponent) {
        return structuredData -> new Pair<>(itemComponent, Identifier.of((String) structuredData.value()));
    }

    private static <T extends Enum<?>> Function<StructuredData<?>, Pair<ItemComponent<?>, Object>> intToEnumFunction(final ItemComponent<T> itemComponent, final Class<T> enumClass) {
        final Enum<?>[] enumConstants = enumClass.getEnumConstants();
        return structuredData -> new Pair<>(itemComponent, enumConstants[(Integer) structuredData.value()]);
    }

    private static Function<StructuredData<?>, Pair<ItemComponent<?>, Object>> passthroughNbtCodec(final ItemComponent<?> itemComponent) {
        return structuredData -> new Pair<>(itemComponent, itemComponent.getCodec().deserialize(NbtConverter_v1_21_5.INSTANCE, NbtConverter.viaToMcStructs((Tag) structuredData.value())).getOrThrow());
    }

    private static Types_v1_20_5.FireworkExplosions convertFireworkExplosion(final FireworkExplosion viaFireworkExplosion) {
        final Types_v1_20_5.FireworkExplosions.ExplosionShape explosionShape = Types_v1_20_5.FireworkExplosions.ExplosionShape.values()[viaFireworkExplosion.shape()];
        return new Types_v1_20_5.FireworkExplosions(explosionShape, intArrayToIntList(viaFireworkExplosion.colors()), intArrayToIntList(viaFireworkExplosion.fadeColors()), viaFireworkExplosion.hasTrail(), viaFireworkExplosion.hasTwinkle());
    }

    private static TextComponent convertTextComponent(final Tag tag) {
        if (tag != null) {
            return TextComponentCodec.V1_21_5.deserialize(NbtConverter.viaToMcStructs(tag));
        } else {
            return null;
        }
    }

    private static List<Integer> intArrayToIntList(final int[] array) {
        final List<Integer> list = new ArrayList<>(array.length);
        for (int i : array) {
            list.add(i);
        }
        return list;
    }

    private final RegistryAccess registryAccess;
    private final Map<StructuredDataKey<?>, Function<StructuredData<?>, Pair<ItemComponent<?>, Object>>> conversionFunctions = new Reference2ObjectOpenHashMap<>();

    public ItemDataComponentConverter(final RegistryAccess registryAccess) {
        this.registryAccess = registryAccess;
        this.conversionFunctions.put(StructuredDataKey.CUSTOM_DATA, convertNbtFunction(ItemComponentRegistry.V1_21_5.CUSTOM_DATA));
        this.conversionFunctions.put(StructuredDataKey.MAX_STACK_SIZE, passthroughValueFunction(ItemComponentRegistry.V1_21_5.MAX_STACK_SIZE));
        this.conversionFunctions.put(StructuredDataKey.MAX_DAMAGE, passthroughValueFunction(ItemComponentRegistry.V1_21_5.MAX_DAMAGE));
        this.conversionFunctions.put(StructuredDataKey.DAMAGE, passthroughValueFunction(ItemComponentRegistry.V1_21_5.DAMAGE));
        this.conversionFunctions.put(StructuredDataKey.UNBREAKABLE1_21_5, noValueFunction(ItemComponentRegistry.V1_21_5.UNBREAKABLE));
        this.conversionFunctions.put(StructuredDataKey.CUSTOM_NAME, convertTextComponentFunction(ItemComponentRegistry.V1_21_5.CUSTOM_NAME));
        this.conversionFunctions.put(StructuredDataKey.ITEM_NAME, convertTextComponentFunction(ItemComponentRegistry.V1_21_5.ITEM_NAME));
        this.conversionFunctions.put(StructuredDataKey.ITEM_MODEL, stringToIdentifierFunction(ItemComponentRegistry.V1_21_5.ITEM_MODEL));
        this.conversionFunctions.put(StructuredDataKey.LORE, convertTextComponentArrayFunction(ItemComponentRegistry.V1_21_5.LORE));
        this.conversionFunctions.put(StructuredDataKey.RARITY, intToEnumFunction(ItemComponentRegistry.V1_21_5.RARITY, Types_v1_20_5.Rarity.class));
        this.conversionFunctions.put(StructuredDataKey.ENCHANTMENTS1_21_5, this.convertEnchantmentsFunction(ItemComponentRegistry.V1_21_5.ENCHANTMENTS));
        this.conversionFunctions.put(StructuredDataKey.CAN_PLACE_ON1_21_5, NOT_IMPLEMENTED);
        this.conversionFunctions.put(StructuredDataKey.CAN_BREAK1_21_5, NOT_IMPLEMENTED);
        this.conversionFunctions.put(StructuredDataKey.ATTRIBUTE_MODIFIERS1_21_5, NOT_IMPLEMENTED);
        this.conversionFunctions.put(StructuredDataKey.CUSTOM_MODEL_DATA1_21_4, structuredData -> {
            final CustomModelData1_21_4 viaCustomModelData = (CustomModelData1_21_4) structuredData.value();
            final List<Float> floats = new ArrayList<>(viaCustomModelData.floats().length);
            for (float f : viaCustomModelData.floats()) {
                floats.add(f);
            }
            final List<Boolean> booleans = new ArrayList<>(viaCustomModelData.booleans().length);
            for (boolean b : viaCustomModelData.booleans()) {
                booleans.add(b);
            }
            return new Pair<>(ItemComponentRegistry.V1_21_5.CUSTOM_MODEL_DATA, new Types_v1_21_4.CustomModelData(floats, booleans, Arrays.asList(viaCustomModelData.strings()), intArrayToIntList(viaCustomModelData.colors())));
        });
        this.conversionFunctions.put(StructuredDataKey.TOOLTIP_DISPLAY, NOT_IMPLEMENTED);
        this.conversionFunctions.put(StructuredDataKey.REPAIR_COST, passthroughValueFunction(ItemComponentRegistry.V1_21_5.REPAIR_COST));
        this.conversionFunctions.put(StructuredDataKey.CREATIVE_SLOT_LOCK, noValueFunction(ItemComponentRegistry.V1_21_5.CREATIVE_SLOT_LOCK));
        this.conversionFunctions.put(StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE, passthroughValueFunction(ItemComponentRegistry.V1_21_5.ENCHANTMENT_GLINT_OVERRIDE));
        this.conversionFunctions.put(StructuredDataKey.INTANGIBLE_PROJECTILE, noValueFunction(ItemComponentRegistry.V1_21_5.INTANGIBLE_PROJECTILE));
        this.conversionFunctions.put(StructuredDataKey.FOOD1_21_2, structuredData -> {
            final FoodProperties1_21_2 viaFoodProperties = (FoodProperties1_21_2) structuredData.value();
            return new Pair<>(ItemComponentRegistry.V1_21_5.FOOD, new Types_v1_21_2.Food(viaFoodProperties.nutrition(), viaFoodProperties.saturationModifier(), viaFoodProperties.canAlwaysEat()));
        });
        this.conversionFunctions.put(StructuredDataKey.CONSUMABLE1_21_2, NOT_IMPLEMENTED);
        this.conversionFunctions.put(StructuredDataKey.USE_REMAINDER1_21_5, this.convertItemFunction(ItemComponentRegistry.V1_21_5.USE_REMAINDER));
        this.conversionFunctions.put(StructuredDataKey.USE_COOLDOWN, structuredData -> {
            final UseCooldown viaUseCooldown = (UseCooldown) structuredData.value();
            return new Pair<>(ItemComponentRegistry.V1_21_5.USE_COOLDOWN, new Types_v1_21_2.UseCooldown(viaUseCooldown.seconds(), Identifier.of(viaUseCooldown.cooldownGroup())));
        });
        this.conversionFunctions.put(StructuredDataKey.DAMAGE_RESISTANT, structuredData -> {
            final DamageResistant viaDamageResistant = (DamageResistant) structuredData.value();
            return new Pair<>(ItemComponentRegistry.V1_21_5.DAMAGE_RESISTANT, new Types_v1_21_2.DamageResistant(Identifier.of(viaDamageResistant.typesTagKey())));
        });
        this.conversionFunctions.put(StructuredDataKey.TOOL1_21_5, NOT_IMPLEMENTED);
        this.conversionFunctions.put(StructuredDataKey.WEAPON, structuredData -> {
            final Weapon viaWeapon = (Weapon) structuredData.value();
            return new Pair<>(ItemComponentRegistry.V1_21_5.WEAPON, new Types_v1_21_5.Weapon(viaWeapon.itemDamagePerAttack(), viaWeapon.disableBlockingForSeconds()));
        });
        this.conversionFunctions.put(StructuredDataKey.ENCHANTABLE, structuredData -> {
            return new Pair<>(ItemComponentRegistry.V1_21_5.ENCHANTABLE, new Types_v1_21_2.Enchantable((int) structuredData.value()));
        });
        this.conversionFunctions.put(StructuredDataKey.EQUIPPABLE1_21_5, NOT_IMPLEMENTED);
        this.conversionFunctions.put(StructuredDataKey.REPAIRABLE, NOT_IMPLEMENTED);
        this.conversionFunctions.put(StructuredDataKey.GLIDER, noValueFunction(ItemComponentRegistry.V1_21_5.GLIDER));
        this.conversionFunctions.put(StructuredDataKey.TOOLTIP_STYLE, stringToIdentifierFunction(ItemComponentRegistry.V1_21_5.TOOLTIP_STYLE));
        this.conversionFunctions.put(StructuredDataKey.DEATH_PROTECTION, NOT_IMPLEMENTED);
        this.conversionFunctions.put(StructuredDataKey.BLOCKS_ATTACKS, NOT_IMPLEMENTED);
        this.conversionFunctions.put(StructuredDataKey.STORED_ENCHANTMENTS1_21_5, this.convertEnchantmentsFunction(ItemComponentRegistry.V1_21_5.STORED_ENCHANTMENTS));
        this.conversionFunctions.put(StructuredDataKey.DYED_COLOR1_21_5, structuredData -> {
            return new Pair<>(ItemComponentRegistry.V1_21_5.DYED_COLOR, ((DyedColor) structuredData.value()).rgb());
        });
        this.conversionFunctions.put(StructuredDataKey.MAP_COLOR, passthroughValueFunction(ItemComponentRegistry.V1_21_5.MAP_COLOR));
        this.conversionFunctions.put(StructuredDataKey.MAP_ID, passthroughValueFunction(ItemComponentRegistry.V1_21_5.MAP_ID));
        this.conversionFunctions.put(StructuredDataKey.MAP_DECORATIONS, passthroughNbtCodec(ItemComponentRegistry.V1_21_5.MAP_DECORATIONS));
        this.conversionFunctions.put(StructuredDataKey.MAP_POST_PROCESSING, intToEnumFunction(ItemComponentRegistry.V1_21_5.MAP_POST_PROCESSING, Types_v1_20_5.MapPostProcessing.class));
        this.conversionFunctions.put(StructuredDataKey.CHARGED_PROJECTILES1_21_5, convertItemArrayFunction(ItemComponentRegistry.V1_21_5.CHARGED_PROJECTILES));
        this.conversionFunctions.put(StructuredDataKey.BUNDLE_CONTENTS1_21_5, convertItemArrayFunction(ItemComponentRegistry.V1_21_5.BUNDLE_CONTENTS));
        this.conversionFunctions.put(StructuredDataKey.POTION_CONTENTS1_21_2, NOT_IMPLEMENTED);
        this.conversionFunctions.put(StructuredDataKey.POTION_DURATION_SCALE, passthroughValueFunction(ItemComponentRegistry.V1_21_5.POTION_DURATION_SCALE));
        this.conversionFunctions.put(StructuredDataKey.SUSPICIOUS_STEW_EFFECTS, NOT_IMPLEMENTED);
        this.conversionFunctions.put(StructuredDataKey.WRITABLE_BOOK_CONTENT, structuredData -> {
            final FilterableString[] viaPages = (FilterableString[]) structuredData.value();
            final List<Types_v1_20_5.RawFilteredPair<String>> pages = new ArrayList<>(viaPages.length);
            for (FilterableString viaPage : viaPages) {
                pages.add(new Types_v1_20_5.RawFilteredPair<>(viaPage.raw(), viaPage.filtered()));
            }
            return new Pair<>(ItemComponentRegistry.V1_21_5.WRITABLE_BOOK_CONTENT, new Types_v1_20_5.WritableBook(pages));
        });
        this.conversionFunctions.put(StructuredDataKey.WRITTEN_BOOK_CONTENT, structuredData -> {
            final WrittenBook viaWrittenBook = (WrittenBook) structuredData.value();
            final Types_v1_20_5.RawFilteredPair<String> title = new Types_v1_20_5.RawFilteredPair<>(viaWrittenBook.title().raw(), viaWrittenBook.title().filtered());
            final List<Types_v1_20_5.RawFilteredPair<TextComponent>> pages = new ArrayList<>(viaWrittenBook.pages().length);
            for (FilterableComponent viaPage : viaWrittenBook.pages()) {
                pages.add(new Types_v1_20_5.RawFilteredPair<>(convertTextComponent(viaPage.raw()), convertTextComponent(viaPage.filtered())));
            }
            return new Pair<>(ItemComponentRegistry.V1_21_5.WRITTEN_BOOK_CONTENT, new Types_v1_20_5.WrittenBook(title, viaWrittenBook.author(), viaWrittenBook.generation(), pages, viaWrittenBook.resolved()));
        });
        this.conversionFunctions.put(StructuredDataKey.TRIM1_21_5, NOT_IMPLEMENTED);
        this.conversionFunctions.put(StructuredDataKey.DEBUG_STICK_STATE, passthroughNbtCodec(ItemComponentRegistry.V1_21_5.DEBUG_STICK_STATE));
        this.conversionFunctions.put(StructuredDataKey.ENTITY_DATA, convertNbtFunction(ItemComponentRegistry.V1_21_5.ENTITY_DATA));
        this.conversionFunctions.put(StructuredDataKey.BUCKET_ENTITY_DATA, convertNbtFunction(ItemComponentRegistry.V1_21_5.BUCKET_ENTITY_DATA));
        this.conversionFunctions.put(StructuredDataKey.BLOCK_ENTITY_DATA, convertNbtFunction(ItemComponentRegistry.V1_21_5.BLOCK_ENTITY_DATA));
        this.conversionFunctions.put(StructuredDataKey.INSTRUMENT1_21_5, NOT_IMPLEMENTED);
        this.conversionFunctions.put(StructuredDataKey.PROVIDES_TRIM_MATERIAL, NOT_IMPLEMENTED);
        this.conversionFunctions.put(StructuredDataKey.OMINOUS_BOTTLE_AMPLIFIER, passthroughValueFunction(ItemComponentRegistry.V1_21_5.OMINOUS_BOTTLE_AMPLIFIER));
        this.conversionFunctions.put(StructuredDataKey.JUKEBOX_PLAYABLE1_21_5, NOT_IMPLEMENTED);
        this.conversionFunctions.put(StructuredDataKey.PROVIDES_BANNER_PATTERNS, stringToIdentifierFunction(ItemComponentRegistry.V1_21_5.PROVIDES_BANNER_PATTERNS));
        this.conversionFunctions.put(StructuredDataKey.RECIPES, passthroughNbtCodec(ItemComponentRegistry.V1_21_5.RECIPES));
        this.conversionFunctions.put(StructuredDataKey.LODESTONE_TRACKER, structuredData -> {
            final LodestoneTracker viaLodestoneTracker = (LodestoneTracker) structuredData.value();
            final Types_v1_20_5.LodestoneTracker.GlobalPos targetGlobalPos;
            if (viaLodestoneTracker.position() != null) {
                final Types_v1_20_5.BlockPos targetPos = new Types_v1_20_5.BlockPos(viaLodestoneTracker.position().x(), viaLodestoneTracker.position().y(), viaLodestoneTracker.position().z());
                targetGlobalPos = new Types_v1_20_5.LodestoneTracker.GlobalPos(Identifier.of(viaLodestoneTracker.position().dimension()), targetPos);
            } else {
                targetGlobalPos = null;
            }
            return new Pair<>(ItemComponentRegistry.V1_21_5.LODESTONE_TRACKER, new Types_v1_20_5.LodestoneTracker(targetGlobalPos, viaLodestoneTracker.tracked()));
        });
        this.conversionFunctions.put(StructuredDataKey.FIREWORK_EXPLOSION, structuredData -> {
            return new Pair<>(ItemComponentRegistry.V1_21_5.FIREWORK_EXPLOSION, convertFireworkExplosion((FireworkExplosion) structuredData.value()));
        });
        this.conversionFunctions.put(StructuredDataKey.FIREWORKS, structuredData -> {
            final Fireworks viaFireworks = (Fireworks) structuredData.value();
            final List<Types_v1_20_5.FireworkExplosions> explosions = new ArrayList<>(viaFireworks.explosions().length);
            for (FireworkExplosion viaFireworkExplosion : viaFireworks.explosions()) {
                explosions.add(convertFireworkExplosion(viaFireworkExplosion));
            }
            return new Pair<>(ItemComponentRegistry.V1_21_5.FIREWORKS, new Types_v1_20_5.Fireworks(viaFireworks.flightDuration(), explosions));
        });
        this.conversionFunctions.put(StructuredDataKey.PROFILE, structuredData -> {
            final GameProfile viaProfile = (GameProfile) structuredData.value();
            final Map<String, List<Types_v1_20_5.GameProfile.Property>> properties = new HashMap<>();
            for (GameProfile.Property viaProperty : viaProfile.properties()) {
                final List<Types_v1_20_5.GameProfile.Property> propertyList = properties.computeIfAbsent(viaProperty.name(), k -> new ArrayList<>());
                propertyList.add(new Types_v1_20_5.GameProfile.Property(viaProperty.name(), viaProperty.value(), viaProperty.signature()));
            }
            return new Pair<>(ItemComponentRegistry.V1_21_5.PROFILE, new Types_v1_20_5.GameProfile(viaProfile.name(), viaProfile.id(), properties));
        });
        this.conversionFunctions.put(StructuredDataKey.NOTE_BLOCK_SOUND, stringToIdentifierFunction(ItemComponentRegistry.V1_21_5.NOTE_BLOCK_SOUND));
        this.conversionFunctions.put(StructuredDataKey.BANNER_PATTERNS, NOT_IMPLEMENTED);
        this.conversionFunctions.put(StructuredDataKey.BASE_COLOR, intToEnumFunction(ItemComponentRegistry.V1_21_5.BASE_COLOR, Types_v1_20_5.DyeColor.class));
        this.conversionFunctions.put(StructuredDataKey.POT_DECORATIONS, NOT_IMPLEMENTED);
        this.conversionFunctions.put(StructuredDataKey.CONTAINER1_21_5, structuredData -> {
            final Item[] items = (Item[]) structuredData.value();
            final List<Types_v1_20_5.ContainerSlot> slots = new ArrayList<>();
            for (int i = 0; i < items.length; i++) {
                final Item item = items[i];
                if (!item.isEmpty()) {
                    slots.add(new Types_v1_20_5.ContainerSlot(i, this.convertItemStack(item)));
                }
            }
            return new Pair<>(ItemComponentRegistry.V1_21_5.CONTAINER, slots);
        });
        this.conversionFunctions.put(StructuredDataKey.BLOCK_STATE, structuredData -> {
            return new Pair<>(ItemComponentRegistry.V1_21_5.BLOCK_STATE, ((BlockStateProperties) structuredData.value()).properties());
        });
        this.conversionFunctions.put(StructuredDataKey.BEES, structuredData -> {
            final Bee[] viaBees = (Bee[]) structuredData.value();
            final List<Types_v1_20_5.BeeData> beeData = new ArrayList<>();
            for (Bee viaBee : viaBees) {
                beeData.add(new Types_v1_20_5.BeeData((CompoundTag) NbtConverter.viaToMcStructs(viaBee.entityData()), viaBee.ticksInHive(), viaBee.minTicksInHive()));
            }
            return new Pair<>(ItemComponentRegistry.V1_21_5.BEES, beeData);
        });
        this.conversionFunctions.put(StructuredDataKey.LOCK, passthroughNbtCodec(ItemComponentRegistry.V1_21_5.LOCK));
        this.conversionFunctions.put(StructuredDataKey.CONTAINER_LOOT, passthroughNbtCodec(ItemComponentRegistry.V1_21_5.CONTAINER_LOOT));
        this.conversionFunctions.put(StructuredDataKey.BREAK_SOUND, NOT_IMPLEMENTED);
        this.conversionFunctions.put(StructuredDataKey.VILLAGER_VARIANT, intToEnumFunction(ItemComponentRegistry.V1_21_5.VILLAGER_VARIANT, Types_v1_21_5.VillagerVariant.class));
        this.conversionFunctions.put(StructuredDataKey.WOLF_VARIANT, NOT_IMPLEMENTED);
        this.conversionFunctions.put(StructuredDataKey.WOLF_SOUND_VARIANT, NOT_IMPLEMENTED);
        this.conversionFunctions.put(StructuredDataKey.WOLF_COLLAR, intToEnumFunction(ItemComponentRegistry.V1_21_5.WOLF_COLLAR, Types_v1_20_5.DyeColor.class));
        this.conversionFunctions.put(StructuredDataKey.FOX_VARIANT, intToEnumFunction(ItemComponentRegistry.V1_21_5.FOX_VARIANT, Types_v1_21_5.FoxVariant.class));
        this.conversionFunctions.put(StructuredDataKey.SALMON_SIZE, intToEnumFunction(ItemComponentRegistry.V1_21_5.SALMON_SIZE, Types_v1_21_5.SalmonSize.class));
        this.conversionFunctions.put(StructuredDataKey.PARROT_VARIANT, intToEnumFunction(ItemComponentRegistry.V1_21_5.PARROT_VARIANT, Types_v1_21_5.ParrotVariant.class));
        this.conversionFunctions.put(StructuredDataKey.TROPICAL_FISH_PATTERN, intToEnumFunction(ItemComponentRegistry.V1_21_5.TROPICAL_FISH_PATTERN, Types_v1_21_5.TropicalFishPattern.class));
        this.conversionFunctions.put(StructuredDataKey.TROPICAL_FISH_BASE_COLOR, intToEnumFunction(ItemComponentRegistry.V1_21_5.TROPICAL_FISH_BASE_COLOR, Types_v1_20_5.DyeColor.class));
        this.conversionFunctions.put(StructuredDataKey.TROPICAL_FISH_PATTERN_COLOR, intToEnumFunction(ItemComponentRegistry.V1_21_5.TROPICAL_FISH_PATTERN_COLOR, Types_v1_20_5.DyeColor.class));
        this.conversionFunctions.put(StructuredDataKey.MOOSHROOM_VARIANT, intToEnumFunction(ItemComponentRegistry.V1_21_5.MOOSHROOM_VARIANT, Types_v1_21_5.MooshroomVariant.class));
        this.conversionFunctions.put(StructuredDataKey.RABBIT_VARIANT, intToEnumFunction(ItemComponentRegistry.V1_21_5.RABBIT_VARIANT, Types_v1_21_5.RabbitVariant.class));
        this.conversionFunctions.put(StructuredDataKey.PIG_VARIANT, NOT_IMPLEMENTED);
        this.conversionFunctions.put(StructuredDataKey.COW_VARIANT, NOT_IMPLEMENTED);
        this.conversionFunctions.put(StructuredDataKey.CHICKEN_VARIANT, NOT_IMPLEMENTED);
        this.conversionFunctions.put(StructuredDataKey.FROG_VARIANT, NOT_IMPLEMENTED);
        this.conversionFunctions.put(StructuredDataKey.HORSE_VARIANT, intToEnumFunction(ItemComponentRegistry.V1_21_5.HORSE_VARIANT, Types_v1_21_5.HorseVariant.class));
        this.conversionFunctions.put(StructuredDataKey.PAINTING_VARIANT, NOT_IMPLEMENTED);
        this.conversionFunctions.put(StructuredDataKey.LLAMA_VARIANT, intToEnumFunction(ItemComponentRegistry.V1_21_5.LLAMA_VARIANT, Types_v1_21_5.LlamaVariant.class));
        this.conversionFunctions.put(StructuredDataKey.AXOLOTL_VARIANT, intToEnumFunction(ItemComponentRegistry.V1_21_5.AXOLOTL_VARIANT, Types_v1_21_5.AxolotlVariant.class));
        this.conversionFunctions.put(StructuredDataKey.CAT_VARIANT, NOT_IMPLEMENTED);
        this.conversionFunctions.put(StructuredDataKey.CAT_COLLAR, intToEnumFunction(ItemComponentRegistry.V1_21_5.CAT_COLLAR, Types_v1_20_5.DyeColor.class));
        this.conversionFunctions.put(StructuredDataKey.SHEEP_COLOR, intToEnumFunction(ItemComponentRegistry.V1_21_5.SHEEP_COLOR, Types_v1_20_5.DyeColor.class));
        this.conversionFunctions.put(StructuredDataKey.SHULKER_COLOR, intToEnumFunction(ItemComponentRegistry.V1_21_5.SHULKER_COLOR, Types_v1_20_5.DyeColor.class));
    }

    public Pair<ItemComponent<?>, Object> viaToMcStructs(final StructuredData<?> structuredData) {
        final Function<StructuredData<?>, Pair<ItemComponent<?>, Object>> conversionFunction = this.conversionFunctions.get(structuredData.key());
        if (conversionFunction == null) {
            throw new UnsupportedOperationException("Unsupported structured data key: " + structuredData.key());
        }
        return conversionFunction.apply(structuredData);
    }

    private Function<StructuredData<?>, Pair<ItemComponent<?>, Object>> convertItemFunction(final ItemComponent<?> itemComponent) {
        return structuredData -> new Pair<>(itemComponent, this.convertItemStack((Item) structuredData.value()));
    }

    private Function<StructuredData<?>, Pair<ItemComponent<?>, Object>> convertItemArrayFunction(final ItemComponent<?> itemComponent) {
        return structuredData -> {
            final Item[] items = (Item[]) structuredData.value();
            final List<Types_v1_20_5.ItemStack> itemStacks = new ArrayList<>(items.length);
            for (Item item : items) {
                itemStacks.add(this.convertItemStack(item));
            }
            return new Pair<>(itemComponent, itemStacks);
        };
    }

    private Function<StructuredData<?>, Pair<ItemComponent<?>, Object>> convertEnchantmentsFunction(final ItemComponent<?> itemComponent) {
        return structuredData -> {
            final Enchantments viaEnchantments = (Enchantments) structuredData.value();
            final Map<Identifier, Integer> enchantments = new HashMap<>();
            for (Int2IntMap.Entry entry : viaEnchantments.enchantments().int2IntEntrySet()) {
                enchantments.put(this.registryAccess.getEnchantment(entry.getIntKey()), entry.getIntValue());
            }
            return new Pair<>(itemComponent, enchantments);
        };
    }

    private Types_v1_20_5.ItemStack convertItemStack(final Item item) {
        final Types_v1_20_5.ItemStack itemStack = new Types_v1_20_5.ItemStack(this.registryAccess.getItem(item.identifier()), item.amount(), new ItemComponentMap(ItemComponentRegistry.V1_21_5));
        final ItemComponentMap itemComponentMap = itemStack.getComponents();

        for (StructuredData<?> structuredData : item.dataContainer().data().values()) {
            if (structuredData.isPresent()) {
                final Pair<ItemComponent<?>, Object> itemComponent = viaToMcStructs(structuredData);
                if (itemComponent != null) {
                    itemComponentMap.set(itemComponent.key(), cast(itemComponent.value()));
                }
            } else {
                itemComponentMap.markForRemoval(ItemComponentRegistry.V1_21_5.getComponentList().getById(structuredData.id()).get());
            }
        }

        return itemStack;
    }

    private static <T> T cast(final Object o) {
        return (T) o;
    }

}
