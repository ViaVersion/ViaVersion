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
package com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.minecraft.EitherHolder;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.AdventureModePredicate;
import com.viaversion.viaversion.api.minecraft.item.data.ArmorTrim;
import com.viaversion.viaversion.api.minecraft.item.data.ArmorTrimPattern;
import com.viaversion.viaversion.api.minecraft.item.data.AttributeModifiers1_21;
import com.viaversion.viaversion.api.minecraft.item.data.DyedColor;
import com.viaversion.viaversion.api.minecraft.item.data.Enchantments;
import com.viaversion.viaversion.api.minecraft.item.data.JukeboxPlayable;
import com.viaversion.viaversion.api.minecraft.item.data.TooltipDisplay;
import com.viaversion.viaversion.api.minecraft.item.data.Unbreakable;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.api.type.types.version.Types1_21_4;
import com.viaversion.viaversion.api.type.types.version.Types1_21_5;
import com.viaversion.viaversion.protocol.packet.PacketWrapperImpl;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.Protocol1_21_4To1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ServerboundPacket1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ServerboundPackets1_21_5;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPacket1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.RecipeDisplayRewriter;
import com.viaversion.viaversion.rewriter.StructuredItemRewriter;
import com.viaversion.viaversion.util.Unit;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.List;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BlockItemPacketRewriter1_21_5 extends StructuredItemRewriter<ClientboundPacket1_21_2, ServerboundPacket1_21_5, Protocol1_21_4To1_21_5> {

    public static final List<StructuredDataKey<?>> HIDE_ADDITIONAL_KEYS = List.of(StructuredDataKey.BANNER_PATTERNS, StructuredDataKey.BEES, StructuredDataKey.BLOCK_ENTITY_DATA,
        StructuredDataKey.BLOCK_STATE, StructuredDataKey.BUNDLE_CONTENTS1_21_5, StructuredDataKey.CHARGED_PROJECTILES1_21_5, StructuredDataKey.CONTAINER1_21_5,
        StructuredDataKey.CONTAINER_LOOT, StructuredDataKey.FIREWORK_EXPLOSION, StructuredDataKey.FIREWORKS, StructuredDataKey.INSTRUMENT1_21_5, StructuredDataKey.MAP_ID,
        StructuredDataKey.PAINTING_VARIANT, StructuredDataKey.POT_DECORATIONS, StructuredDataKey.POTION_CONTENTS1_21_2, StructuredDataKey.TROPICAL_FISH_PATTERN,
        StructuredDataKey.WRITTEN_BOOK_CONTENT
    );

    public BlockItemPacketRewriter1_21_5(final Protocol1_21_4To1_21_5 protocol) {
        super(protocol,
            Types1_21_4.ITEM, Types1_21_4.ITEM_ARRAY, Types1_21_5.ITEM, Types1_21_5.ITEM_ARRAY,
            Types1_21_4.ITEM_COST, Types1_21_4.OPTIONAL_ITEM_COST, Types1_21_5.ITEM_COST, Types1_21_5.OPTIONAL_ITEM_COST
        );
    }

    @Override
    public void registerPackets() {
        final BlockRewriter<ClientboundPacket1_21_2> blockRewriter = BlockRewriter.for1_20_2(protocol);
        blockRewriter.registerBlockEvent(ClientboundPackets1_21_2.BLOCK_EVENT);
        blockRewriter.registerBlockUpdate(ClientboundPackets1_21_2.BLOCK_UPDATE);
        blockRewriter.registerSectionBlocksUpdate1_20(ClientboundPackets1_21_2.SECTION_BLOCKS_UPDATE);
        blockRewriter.registerLevelEvent1_21(ClientboundPackets1_21_2.LEVEL_EVENT, 2001);
        blockRewriter.registerLevelChunk1_19(ClientboundPackets1_21_2.LEVEL_CHUNK_WITH_LIGHT, ChunkType1_20_2::new);
        blockRewriter.registerBlockEntityData(ClientboundPackets1_21_2.BLOCK_ENTITY_DATA);

        protocol.registerClientbound(ClientboundPackets1_21_2.SET_CURSOR_ITEM, this::passthroughClientboundItem);
        registerSetPlayerInventory(ClientboundPackets1_21_2.SET_PLAYER_INVENTORY);
        registerCooldown1_21_2(ClientboundPackets1_21_2.COOLDOWN);
        registerSetContent1_21_2(ClientboundPackets1_21_2.CONTAINER_SET_CONTENT);
        registerSetSlot1_21_2(ClientboundPackets1_21_2.CONTAINER_SET_SLOT);
        registerSetEquipment(ClientboundPackets1_21_2.SET_EQUIPMENT);
        registerMerchantOffers1_20_5(ClientboundPackets1_21_2.MERCHANT_OFFERS);
        registerContainerClick1_21_2(ServerboundPackets1_21_5.CONTAINER_CLICK);
        registerSetCreativeModeSlot(ServerboundPackets1_21_5.SET_CREATIVE_MODE_SLOT);

        registerAdvancements1_20_3(ClientboundPackets1_21_2.UPDATE_ADVANCEMENTS);
        protocol.appendClientbound(ClientboundPackets1_21_2.UPDATE_ADVANCEMENTS, wrapper -> {
            wrapper.passthrough(Types.STRING_ARRAY); // Removed
            final int progressSize = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < progressSize; i++) {
                wrapper.passthrough(Types.STRING); // Key

                final int criterionSize = wrapper.passthrough(Types.VAR_INT);
                for (int j = 0; j < criterionSize; j++) {
                    wrapper.passthrough(Types.STRING); // Key
                    wrapper.passthrough(Types.OPTIONAL_LONG); // Obtained instant
                }
            }

            // Show advancements
            wrapper.write(Types.BOOLEAN, true);
        });

        final RecipeDisplayRewriter<ClientboundPacket1_21_2> recipeRewriter = new RecipeDisplayRewriter<>(protocol) {
            @Override
            protected void handleSmithingTrimSlotDisplay(final PacketWrapper wrapper) {
                handleSlotDisplay(wrapper); // Base
                handleSlotDisplay(wrapper); // Material

                // Read away the pattern
                ((PacketWrapperImpl) wrapper).setAllActionsRead(true);
                handleSlotDisplay(wrapper);
                ((PacketWrapperImpl) wrapper).setAllActionsRead(false);

                // Pattern - can't really be inferred from data
                wrapper.write(ArmorTrimPattern.TYPE1_21_5, Holder.of(0));
            }
        };
        recipeRewriter.registerUpdateRecipes(ClientboundPackets1_21_2.UPDATE_RECIPES);
        recipeRewriter.registerRecipeBookAdd(ClientboundPackets1_21_2.RECIPE_BOOK_ADD);
        recipeRewriter.registerPlaceGhostRecipe(ClientboundPackets1_21_2.PLACE_GHOST_RECIPE);
    }

    @Override
    public Item handleItemToClient(final UserConnection connection, final Item item) {
        super.handleItemToClient(connection, item);
        updateItemData(item);
        return item;
    }

    @Override
    public Item handleItemToServer(final UserConnection connection, final Item item) {
        super.handleItemToServer(connection, item);
        downgradeItemData(item);
        return item;
    }

    public static void updateItemData(final Item item) {
        final StructuredDataContainer dataContainer = item.dataContainer();
        dataContainer.replaceKey(StructuredDataKey.CHARGED_PROJECTILES1_21_4, StructuredDataKey.CHARGED_PROJECTILES1_21_5);
        dataContainer.replaceKey(StructuredDataKey.BUNDLE_CONTENTS1_21_4, StructuredDataKey.BUNDLE_CONTENTS1_21_5);
        dataContainer.replaceKey(StructuredDataKey.CONTAINER1_21_4, StructuredDataKey.CONTAINER1_21_5);
        dataContainer.replaceKey(StructuredDataKey.USE_REMAINDER1_21_4, StructuredDataKey.USE_REMAINDER1_21_5);

        dataContainer.replaceKey(StructuredDataKey.TOOL1_20_5, StructuredDataKey.TOOL1_21_5);
        dataContainer.replaceKey(StructuredDataKey.EQUIPPABLE1_21_2, StructuredDataKey.EQUIPPABLE1_21_5);
        dataContainer.replace(StructuredDataKey.INSTRUMENT1_21_2, StructuredDataKey.INSTRUMENT1_21_5, EitherHolder::of);

        // Collect hidden tooltips
        final IntSortedSet hiddenComponents = new IntLinkedOpenHashSet(4);
        final boolean hideTooltip = dataContainer.hasValue(StructuredDataKey.HIDE_TOOLTIP);
        if (dataContainer.hasValue(StructuredDataKey.HIDE_ADDITIONAL_TOOLTIP)) {
            final FullMappings mappings = Protocol1_21_4To1_21_5.MAPPINGS.getDataComponentSerializerMappings();
            for (final StructuredDataKey<?> key : HIDE_ADDITIONAL_KEYS) {
                hiddenComponents.add(mappings.mappedId(key.identifier()));
            }
        }

        final StructuredData<Unbreakable> unbreakableData = dataContainer.getNonEmptyData(StructuredDataKey.UNBREAKABLE1_20_5);
        if (unbreakableData != null && !unbreakableData.value().showInTooltip()) {
            hiddenComponents.add(unbreakableData.id());
        }

        final StructuredData<AdventureModePredicate> canPlaceOnData = dataContainer.getNonEmptyData(StructuredDataKey.CAN_PLACE_ON1_20_5);
        if (canPlaceOnData != null && !canPlaceOnData.value().showInTooltip()) {
            hiddenComponents.add(canPlaceOnData.id());
        }

        final StructuredData<AdventureModePredicate> canBreak = dataContainer.getNonEmptyData(StructuredDataKey.CAN_BREAK1_20_5);
        if (canBreak != null && !canBreak.value().showInTooltip()) {
            hiddenComponents.add(canBreak.id());
        }

        final StructuredData<DyedColor> dyedColorData = dataContainer.getNonEmptyData(StructuredDataKey.DYED_COLOR1_20_5);
        if (dyedColorData != null && !dyedColorData.value().showInTooltip()) {
            hiddenComponents.add(dyedColorData.id());
        }

        final StructuredData<AttributeModifiers1_21> attributeModifiersData = dataContainer.getNonEmptyData(StructuredDataKey.ATTRIBUTE_MODIFIERS1_21);
        if (attributeModifiersData != null && !attributeModifiersData.value().showInTooltip()) {
            hiddenComponents.add(attributeModifiersData.id());
        }

        final StructuredData<ArmorTrim> armorTrimData = dataContainer.getNonEmptyData(StructuredDataKey.TRIM1_21_4);
        if (armorTrimData != null && !armorTrimData.value().showInTooltip()) {
            hiddenComponents.add(armorTrimData.id());
        }

        final StructuredData<Enchantments> enchantmentsData = dataContainer.getNonEmptyData(StructuredDataKey.ENCHANTMENTS1_20_5);
        if (enchantmentsData != null && !enchantmentsData.value().showInTooltip()) {
            hiddenComponents.add(enchantmentsData.id());
        }

        final StructuredData<Enchantments> storedEnchantmentsData = dataContainer.getNonEmptyData(StructuredDataKey.STORED_ENCHANTMENTS1_20_5);
        if (storedEnchantmentsData != null && !storedEnchantmentsData.value().showInTooltip()) {
            hiddenComponents.add(storedEnchantmentsData.id());
        }

        final StructuredData<JukeboxPlayable> jukeboxPlayable = dataContainer.getNonEmptyData(StructuredDataKey.JUKEBOX_PLAYABLE1_21);
        if (jukeboxPlayable != null && !jukeboxPlayable.value().showInTooltip()) {
            hiddenComponents.add(jukeboxPlayable.id());
        }

        if (hideTooltip || !hiddenComponents.isEmpty()) {
            dataContainer.set(StructuredDataKey.TOOLTIP_DISPLAY, new TooltipDisplay(hideTooltip, hiddenComponents));
        }

        dataContainer.replace(StructuredDataKey.UNBREAKABLE1_20_5, StructuredDataKey.UNBREAKABLE1_21_5, unbreakable -> Unit.INSTANCE);
        dataContainer.replaceKey(StructuredDataKey.CAN_PLACE_ON1_20_5, StructuredDataKey.CAN_PLACE_ON1_21_5);
        dataContainer.replaceKey(StructuredDataKey.CAN_BREAK1_20_5, StructuredDataKey.CAN_BREAK1_21_5);
        dataContainer.replaceKey(StructuredDataKey.JUKEBOX_PLAYABLE1_21, StructuredDataKey.JUKEBOX_PLAYABLE1_21_5);
        dataContainer.replaceKey(StructuredDataKey.DYED_COLOR1_20_5, StructuredDataKey.DYED_COLOR1_21_5);
        dataContainer.replaceKey(StructuredDataKey.ATTRIBUTE_MODIFIERS1_21, StructuredDataKey.ATTRIBUTE_MODIFIERS1_21_5);
        dataContainer.replaceKey(StructuredDataKey.TRIM1_21_4, StructuredDataKey.TRIM1_21_5);
        dataContainer.replaceKey(StructuredDataKey.ENCHANTMENTS1_20_5, StructuredDataKey.ENCHANTMENTS1_21_5);
        dataContainer.replaceKey(StructuredDataKey.STORED_ENCHANTMENTS1_20_5, StructuredDataKey.STORED_ENCHANTMENTS1_21_5);
        dataContainer.remove(StructuredDataKey.HIDE_TOOLTIP);
        dataContainer.remove(StructuredDataKey.HIDE_ADDITIONAL_TOOLTIP);
    }

    public static void downgradeItemData(final Item item) {
        final StructuredDataContainer dataContainer = item.dataContainer();
        dataContainer.replaceKey(StructuredDataKey.CHARGED_PROJECTILES1_21_5, StructuredDataKey.CHARGED_PROJECTILES1_21_4);
        dataContainer.replaceKey(StructuredDataKey.BUNDLE_CONTENTS1_21_5, StructuredDataKey.BUNDLE_CONTENTS1_21_4);
        dataContainer.replaceKey(StructuredDataKey.CONTAINER1_21_5, StructuredDataKey.CONTAINER1_21_4);
        dataContainer.replaceKey(StructuredDataKey.USE_REMAINDER1_21_5, StructuredDataKey.USE_REMAINDER1_21_4);

        dataContainer.replaceKey(StructuredDataKey.TOOL1_21_5, StructuredDataKey.TOOL1_20_5);
        dataContainer.replaceKey(StructuredDataKey.EQUIPPABLE1_21_5, StructuredDataKey.EQUIPPABLE1_21_2);
        dataContainer.replace(StructuredDataKey.INSTRUMENT1_21_5, StructuredDataKey.INSTRUMENT1_21_2, instrument -> instrument.hasHolder() ? instrument.holder() : null);

        final TooltipDisplay tooltipDisplay = dataContainer.get(StructuredDataKey.TOOLTIP_DISPLAY);
        if (tooltipDisplay != null && tooltipDisplay.hideTooltip()) {
            dataContainer.set(StructuredDataKey.HIDE_TOOLTIP);
        }

        dataContainer.replace(StructuredDataKey.UNBREAKABLE1_21_5, StructuredDataKey.UNBREAKABLE1_20_5, unbreakable -> new Unbreakable(shouldShowToServer(tooltipDisplay, StructuredDataKey.UNBREAKABLE1_20_5)));
        updateShowInTooltip(dataContainer, tooltipDisplay, StructuredDataKey.DYED_COLOR1_21_5, StructuredDataKey.DYED_COLOR1_20_5, dyedColor -> new DyedColor(dyedColor.rgb(), false));
        updateShowInTooltip(dataContainer, tooltipDisplay, StructuredDataKey.ATTRIBUTE_MODIFIERS1_21_5, StructuredDataKey.ATTRIBUTE_MODIFIERS1_21, attributeModifiers -> new AttributeModifiers1_21(attributeModifiers.modifiers(), false));
        updateShowInTooltip(dataContainer, tooltipDisplay, StructuredDataKey.TRIM1_21_5, StructuredDataKey.TRIM1_21_4, trim -> new ArmorTrim(trim.material(), trim.pattern(), false));
        updateShowInTooltip(dataContainer, tooltipDisplay, StructuredDataKey.ENCHANTMENTS1_21_5, StructuredDataKey.ENCHANTMENTS1_20_5, enchantments -> new Enchantments(enchantments.enchantments(), false));
        updateShowInTooltip(dataContainer, tooltipDisplay, StructuredDataKey.STORED_ENCHANTMENTS1_21_5, StructuredDataKey.STORED_ENCHANTMENTS1_20_5, enchantments -> new Enchantments(enchantments.enchantments(), false));
        updateShowInTooltip(dataContainer, tooltipDisplay, StructuredDataKey.CAN_PLACE_ON1_21_5, StructuredDataKey.CAN_PLACE_ON1_20_5, canPlaceOn -> new AdventureModePredicate(canPlaceOn.predicates(), false));
        updateShowInTooltip(dataContainer, tooltipDisplay, StructuredDataKey.CAN_BREAK1_21_5, StructuredDataKey.CAN_BREAK1_20_5, canBreak -> new AdventureModePredicate(canBreak.predicates(), false));
        updateShowInTooltip(dataContainer, tooltipDisplay, StructuredDataKey.JUKEBOX_PLAYABLE1_21_5, StructuredDataKey.JUKEBOX_PLAYABLE1_21, playable -> new JukeboxPlayable(playable.song(), false));

        dataContainer.remove(StructuredDataKey.TOOLTIP_DISPLAY);
        dataContainer.remove(StructuredDataKey.POTION_DURATION_SCALE);
        dataContainer.remove(StructuredDataKey.WEAPON);
        dataContainer.remove(StructuredDataKey.VILLAGER_VARIANT);
        dataContainer.remove(StructuredDataKey.WOLF_VARIANT);
        dataContainer.remove(StructuredDataKey.WOLF_COLLAR);
        dataContainer.remove(StructuredDataKey.FOX_VARIANT);
        dataContainer.remove(StructuredDataKey.SALMON_SIZE);
        dataContainer.remove(StructuredDataKey.PARROT_VARIANT);
        dataContainer.remove(StructuredDataKey.TROPICAL_FISH_PATTERN);
        dataContainer.remove(StructuredDataKey.TROPICAL_FISH_BASE_COLOR);
        dataContainer.remove(StructuredDataKey.TROPICAL_FISH_PATTERN_COLOR);
        dataContainer.remove(StructuredDataKey.MOOSHROOM_VARIANT);
        dataContainer.remove(StructuredDataKey.RABBIT_VARIANT);
        dataContainer.remove(StructuredDataKey.PIG_VARIANT);
        dataContainer.remove(StructuredDataKey.FROG_VARIANT);
        dataContainer.remove(StructuredDataKey.HORSE_VARIANT);
        dataContainer.remove(StructuredDataKey.PAINTING_VARIANT);
        dataContainer.remove(StructuredDataKey.LLAMA_VARIANT);
        dataContainer.remove(StructuredDataKey.AXOLOTL_VARIANT);
        dataContainer.remove(StructuredDataKey.CAT_VARIANT);
        dataContainer.remove(StructuredDataKey.CAT_COLLAR);
        dataContainer.remove(StructuredDataKey.SHEEP_COLOR);
        dataContainer.remove(StructuredDataKey.SHULKER_COLOR);
        dataContainer.remove(StructuredDataKey.BLOCKS_ATTACKS);
        dataContainer.remove(StructuredDataKey.PROVIDES_TRIM_MATERIAL);
        dataContainer.remove(StructuredDataKey.BREAK_SOUND);
    }

    private static <T> void updateShowInTooltip(final StructuredDataContainer container, @Nullable final TooltipDisplay display, final StructuredDataKey<T> key, final StructuredDataKey<T> mappedKey, final Function<T, T> function) {
        if (shouldShowToServer(display, key)) {
            container.replaceKey(key, mappedKey);
        } else {
            container.replace(key, mappedKey, function);
        }
    }

    private static boolean shouldShowToServer(@Nullable final TooltipDisplay display, final StructuredDataKey<?> key) {
        if (display == null) {
            return true;
        }

        final int unmappedId = Protocol1_21_4To1_21_5.MAPPINGS.getDataComponentSerializerMappings().id(key.identifier());
        return !display.hiddenComponents().contains(unmappedId);
    }
}
