/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.LongArrayTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.EitherHolder;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk1_21_5;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.Heightmap;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.data.predicate.DataComponentMatchers;
import com.viaversion.viaversion.api.minecraft.data.predicate.DataComponentPredicate;
import com.viaversion.viaversion.api.minecraft.item.HashedItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.minecraft.item.data.AdventureModePredicate;
import com.viaversion.viaversion.api.minecraft.item.data.ArmorTrim;
import com.viaversion.viaversion.api.minecraft.item.data.ArmorTrimPattern;
import com.viaversion.viaversion.api.minecraft.item.data.AttributeModifiers1_21;
import com.viaversion.viaversion.api.minecraft.item.data.BlockPredicate;
import com.viaversion.viaversion.api.minecraft.item.data.BlocksAttacks;
import com.viaversion.viaversion.api.minecraft.item.data.DyedColor;
import com.viaversion.viaversion.api.minecraft.item.data.Enchantments;
import com.viaversion.viaversion.api.minecraft.item.data.JukeboxPlayable;
import com.viaversion.viaversion.api.minecraft.item.data.TooltipDisplay;
import com.viaversion.viaversion.api.minecraft.item.data.TropicalFishPattern;
import com.viaversion.viaversion.api.minecraft.item.data.Unbreakable;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.rewriter.ComponentRewriter;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkBiomesType1_19_4;
import com.viaversion.viaversion.api.type.types.chunk.ChunkBiomesType1_21_5;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_21_5;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocol.packet.PacketWrapperImpl;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.Protocol1_21_4To1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ServerboundPacket1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ServerboundPackets1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.storage.ItemHashStorage1_21_5;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPacket1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.RecipeDisplayRewriter;
import com.viaversion.viaversion.rewriter.StructuredItemRewriter;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.Limit;
import com.viaversion.viaversion.util.Unit;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.viaversion.viaversion.util.MathUtil.ceilLog2;

public final class BlockItemPacketRewriter1_21_5 extends StructuredItemRewriter<ClientboundPacket1_21_2, ServerboundPacket1_21_5, Protocol1_21_4To1_21_5> {

    public static final List<StructuredDataKey<?>> HIDE_ADDITIONAL_KEYS = List.of(
        StructuredDataKey.BANNER_PATTERNS, StructuredDataKey.BEES1_20_5, StructuredDataKey.BLOCK_ENTITY_DATA1_20_5,
        StructuredDataKey.BLOCK_STATE, StructuredDataKey.V1_21_5.bundleContents, StructuredDataKey.V1_21_5.chargedProjectiles, StructuredDataKey.V1_21_5.container,
        StructuredDataKey.CONTAINER_LOOT, StructuredDataKey.FIREWORK_EXPLOSION, StructuredDataKey.FIREWORKS, StructuredDataKey.INSTRUMENT1_21_5, StructuredDataKey.MAP_ID,
        StructuredDataKey.PAINTING_VARIANT, StructuredDataKey.POT_DECORATIONS, StructuredDataKey.POTION_CONTENTS1_21_2, StructuredDataKey.TROPICAL_FISH_PATTERN,
        StructuredDataKey.WRITTEN_BOOK_CONTENT
    );
    public static final List<StructuredDataKey<?>> NEW_DATA_TO_REMOVE = List.of(
        StructuredDataKey.TOOLTIP_DISPLAY, StructuredDataKey.POTION_DURATION_SCALE, StructuredDataKey.WEAPON,
        StructuredDataKey.VILLAGER_VARIANT, StructuredDataKey.WOLF_VARIANT, StructuredDataKey.WOLF_COLLAR,
        StructuredDataKey.FOX_VARIANT, StructuredDataKey.SALMON_SIZE, StructuredDataKey.PARROT_VARIANT,
        StructuredDataKey.TROPICAL_FISH_PATTERN, StructuredDataKey.TROPICAL_FISH_BASE_COLOR, StructuredDataKey.TROPICAL_FISH_PATTERN_COLOR,
        StructuredDataKey.MOOSHROOM_VARIANT, StructuredDataKey.RABBIT_VARIANT, StructuredDataKey.COW_VARIANT,
        StructuredDataKey.PIG_VARIANT, StructuredDataKey.CHICKEN_VARIANT, StructuredDataKey.FROG_VARIANT,
        StructuredDataKey.HORSE_VARIANT, StructuredDataKey.PAINTING_VARIANT, StructuredDataKey.LLAMA_VARIANT,
        StructuredDataKey.AXOLOTL_VARIANT, StructuredDataKey.CAT_VARIANT, StructuredDataKey.CAT_COLLAR,
        StructuredDataKey.SHEEP_COLOR, StructuredDataKey.SHULKER_COLOR, StructuredDataKey.BLOCKS_ATTACKS,
        StructuredDataKey.PROVIDES_TRIM_MATERIAL, StructuredDataKey.BREAK_SOUND, StructuredDataKey.WOLF_SOUND_VARIANT,
        StructuredDataKey.PROVIDES_BANNER_PATTERNS
    );
    private static final DataComponentMatchers EMPTY_DATA_MATCHERS = new DataComponentMatchers(new StructuredData[0], new DataComponentPredicate[0]);
    private static final Heightmap[] EMPTY_HEIGHTMAPS = new Heightmap[0];

    public BlockItemPacketRewriter1_21_5(final Protocol1_21_4To1_21_5 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        final BlockRewriter<ClientboundPacket1_21_2> blockRewriter = new BlockPacketRewriter1_21_5(protocol);
        blockRewriter.registerBlockEvent(ClientboundPackets1_21_2.BLOCK_EVENT);
        blockRewriter.registerBlockUpdate(ClientboundPackets1_21_2.BLOCK_UPDATE);
        blockRewriter.registerSectionBlocksUpdate1_20(ClientboundPackets1_21_2.SECTION_BLOCKS_UPDATE);
        blockRewriter.registerLevelEvent1_21(ClientboundPackets1_21_2.LEVEL_EVENT, 2001);
        blockRewriter.registerBlockEntityData(ClientboundPackets1_21_2.BLOCK_ENTITY_DATA);

        protocol.registerClientbound(ClientboundPackets1_21_2.LEVEL_CHUNK_WITH_LIGHT, wrapper -> {
            final EntityTracker tracker = protocol.getEntityRewriter().tracker(wrapper.user());
            final Mappings blockStateMappings = protocol.getMappingData().getBlockStateMappings();
            final Type<Chunk> chunkType = new ChunkType1_20_2(tracker.currentWorldSectionHeight(), ceilLog2(blockStateMappings.size()), ceilLog2(tracker.biomesSent()));
            final Chunk chunk = wrapper.read(chunkType);
            blockRewriter.handleChunk(chunk);

            final Type<Chunk> newChunkType = new ChunkType1_21_5(tracker.currentWorldSectionHeight(), ceilLog2(blockStateMappings.mappedSize()), ceilLog2(tracker.biomesSent()));
            final List<Heightmap> heightmaps = new ArrayList<>();
            for (final Map.Entry<String, Tag> entry : chunk.getHeightMap().entrySet()) {
                final int type = heightmapType(entry.getKey());
                if (type == -1) {
                    protocol.getLogger().warning("Unknown heightmap type: " + entry.getKey());
                    continue;
                }

                if (entry.getValue() instanceof LongArrayTag longArrayTag) {
                    heightmaps.add(new Heightmap(type, longArrayTag.getValue()));
                }
            }

            final Chunk mappedChunk = new Chunk1_21_5(chunk.getX(), chunk.getZ(), chunk.getSections(), heightmaps.toArray(EMPTY_HEIGHTMAPS), chunk.blockEntities());
            blockRewriter.handleBlockEntities(chunk, wrapper.user());
            wrapper.write(newChunkType, mappedChunk);
        });

        protocol.registerClientbound(ClientboundPackets1_21_2.CHUNKS_BIOMES, wrapper -> {
            final EntityTracker tracker = protocol.getEntityRewriter().tracker(wrapper.user());
            final int globalPaletteBiomeBits = ceilLog2(tracker.biomesSent());
            final Type<DataPalette[]> biomesType = new ChunkBiomesType1_19_4(tracker.currentWorldSectionHeight(), globalPaletteBiomeBits);
            final Type<DataPalette[]> newBiomesType = new ChunkBiomesType1_21_5(tracker.currentWorldSectionHeight(), globalPaletteBiomeBits);

            final int size = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < size; i++) {
                wrapper.passthrough(Types.CHUNK_POSITION);
                wrapper.passthroughAndMap(biomesType, newBiomesType);
            }
        });

        registerSetCursorItem(ClientboundPackets1_21_2.SET_CURSOR_ITEM);
        registerSetPlayerInventory(ClientboundPackets1_21_2.SET_PLAYER_INVENTORY);
        registerCooldown1_21_2(ClientboundPackets1_21_2.COOLDOWN);
        registerSetContent1_21_2(ClientboundPackets1_21_2.CONTAINER_SET_CONTENT);
        registerSetSlot1_21_2(ClientboundPackets1_21_2.CONTAINER_SET_SLOT);
        registerSetEquipment(ClientboundPackets1_21_2.SET_EQUIPMENT);
        registerMerchantOffers1_20_5(ClientboundPackets1_21_2.MERCHANT_OFFERS);

        protocol.registerServerbound(ServerboundPackets1_21_5.SET_CREATIVE_MODE_SLOT, wrapper -> {
            if (!protocol.getEntityRewriter().tracker(wrapper.user()).canInstaBuild()) {
                wrapper.cancel();
                return;
            }

            wrapper.passthrough(Types.SHORT); // Slot

            final Item item = handleItemToServer(wrapper.user(), wrapper.read(VersionedTypes.V1_21_5.lengthPrefixedItem()));
            wrapper.write(itemType(), item);
        });

        protocol.registerServerbound(ServerboundPackets1_21_5.CONTAINER_CLICK, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Container id
            wrapper.passthrough(Types.VAR_INT); // State id
            wrapper.passthrough(Types.SHORT); // Slot
            wrapper.passthrough(Types.BYTE); // Button
            wrapper.passthrough(Types.VAR_INT); // Mode

            // Try our best to get the actual item out of it - will be wrong for some data component types that don't have their conversion implemented
            final int affectedItems = Limit.max(wrapper.passthrough(Types.VAR_INT), 128);
            for (int i = 0; i < affectedItems; i++) {
                wrapper.passthrough(Types.SHORT); // Slot
                final HashedItem item = wrapper.read(Types.HASHED_ITEM);
                wrapper.write(VersionedTypes.V1_21_5.item, handleItemToServer(wrapper.user(), this.convertHashedItemToStructuredItem(wrapper.user(), item)));
            }

            final HashedItem carriedItem = wrapper.read(Types.HASHED_ITEM);
            wrapper.write(VersionedTypes.V1_21_5.item, handleItemToServer(wrapper.user(), this.convertHashedItemToStructuredItem(wrapper.user(), carriedItem)));
        });

        protocol.registerClientbound(ClientboundPackets1_21_2.UPDATE_ADVANCEMENTS, wrapper -> {
            wrapper.passthrough(Types.BOOLEAN); // Reset/clear
            int size = wrapper.passthrough(Types.VAR_INT); // Mapping size
            for (int i = 0; i < size; i++) {
                wrapper.passthrough(Types.STRING); // Identifier
                wrapper.passthrough(Types.OPTIONAL_STRING); // Parent

                // Display data
                if (wrapper.passthrough(Types.BOOLEAN)) {
                    final Tag title = wrapper.passthrough(Types.TAG);
                    final Tag description = wrapper.passthrough(Types.TAG);
                    final ComponentRewriter componentRewriter = protocol.getComponentRewriter();
                    if (componentRewriter != null) {
                        componentRewriter.processTag(wrapper.user(), title);
                        componentRewriter.processTag(wrapper.user(), description);
                    }

                    passthroughClientboundItem(wrapper); // Icon
                    wrapper.passthrough(Types.VAR_INT); // Frame type
                    int flags = wrapper.passthrough(Types.INT); // Flags
                    if ((flags & 1) != 0) {
                        convertClientAsset(wrapper);
                    }
                    wrapper.passthrough(Types.FLOAT); // X
                    wrapper.passthrough(Types.FLOAT); // Y
                }

                int requirements = wrapper.passthrough(Types.VAR_INT);
                for (int array = 0; array < requirements; array++) {
                    wrapper.passthrough(Types.STRING_ARRAY);
                }

                wrapper.passthrough(Types.BOOLEAN); // Send telemetry
            }

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

    private void convertClientAsset(final PacketWrapper wrapper) {
        final String background = wrapper.read(Types.STRING);
        final String namespace = Key.namespace(background);
        final String path = Key.stripNamespace(background);
        if (path.startsWith("textures/") && path.endsWith(".png")) {
            final String stripped = path.substring("textures/".length(), path.length() - ".png".length());
            wrapper.write(Types.STRING, namespace + ":" + stripped);
        } else {
            wrapper.write(Types.STRING, namespace + ":" + path);
        }
    }

    private int heightmapType(final String id) {
        return switch (id) {
            case "WORLD_SURFACE_WG" -> 0;
            case "WORLD_SURFACE" -> 1;
            case "OCEAN_FLOOR_WG" -> 2;
            case "OCEAN_FLOOR" -> 3;
            case "MOTION_BLOCKING" -> 4;
            case "MOTION_BLOCKING_NO_LEAVES" -> 5;
            default -> -1;
        };
    }

    @Override
    public Item handleItemToClient(final UserConnection connection, final Item item) {
        // Don't call super, not actually using ItemHasherBase
        if (item.isEmpty()) {
            return item;
        }

        final MappingData mappingData = protocol.getMappingData();
        if (mappingData != null && mappingData.getItemMappings() != null) {
            item.setIdentifier(mappingData.getNewItemId(item.identifier()));
        }

        final StructuredDataContainer dataContainer = item.dataContainer();
        updateItemDataComponentTypeIds(dataContainer, true);
        handleRewritablesToClient(connection, dataContainer, null);

        updateItemData(item);

        handleItemDataComponentsToClient(connection, item, dataContainer);

        // Add data components to fix issues in older protocols
        appendItemDataFixComponents(connection, item);

        // Store the data components if necessary for the server, the client only sends data hashes now
        final ItemHashStorage1_21_5 itemHasher = itemHasher(connection);
        if (itemHasher.isProcessingClientboundInventoryPacket()) {
            for (final StructuredData<?> data : dataContainer.data().values()) {
                itemHasher.trackStructuredData(data);
            }
        }

        return item;
    }

    @Override
    protected void handleItemDataComponentsToServer(final UserConnection connection, final Item item, final StructuredDataContainer container) {
        downgradeItemData(item);
        super.handleItemDataComponentsToServer(connection, item, container);
    }

    public static void updateItemData(final Item item) {
        final StructuredDataContainer dataContainer = item.dataContainer();
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

        if ((hideTooltip || !hiddenComponents.isEmpty()) && !dataContainer.has(StructuredDataKey.TOOLTIP_DISPLAY)) {
            dataContainer.set(StructuredDataKey.TOOLTIP_DISPLAY, new TooltipDisplay(hideTooltip, hiddenComponents));
        }

        updateBucketVariant(dataContainer);
        dataContainer.replace(StructuredDataKey.UNBREAKABLE1_20_5, StructuredDataKey.UNBREAKABLE1_21_5, unbreakable -> Unit.INSTANCE);
        dataContainer.replace(StructuredDataKey.CAN_PLACE_ON1_20_5, StructuredDataKey.V1_21_5.canPlaceOn, BlockItemPacketRewriter1_21_5::updateAdventureModePredicate);
        dataContainer.replace(StructuredDataKey.CAN_BREAK1_20_5, StructuredDataKey.V1_21_5.canBreak, BlockItemPacketRewriter1_21_5::updateAdventureModePredicate);
        dataContainer.replaceKey(StructuredDataKey.JUKEBOX_PLAYABLE1_21, StructuredDataKey.JUKEBOX_PLAYABLE1_21_5);
        dataContainer.replaceKey(StructuredDataKey.DYED_COLOR1_20_5, StructuredDataKey.DYED_COLOR1_21_5);
        dataContainer.replaceKey(StructuredDataKey.ATTRIBUTE_MODIFIERS1_21, StructuredDataKey.ATTRIBUTE_MODIFIERS1_21_5);
        dataContainer.replaceKey(StructuredDataKey.TRIM1_21_4, StructuredDataKey.TRIM1_21_5);
        dataContainer.replaceKey(StructuredDataKey.ENCHANTMENTS1_20_5, StructuredDataKey.ENCHANTMENTS1_21_5);
        dataContainer.replaceKey(StructuredDataKey.STORED_ENCHANTMENTS1_20_5, StructuredDataKey.STORED_ENCHANTMENTS1_21_5);
        dataContainer.remove(StructuredDataKey.HIDE_TOOLTIP);
        dataContainer.remove(StructuredDataKey.HIDE_ADDITIONAL_TOOLTIP);
    }

    private static AdventureModePredicate updateAdventureModePredicate(final AdventureModePredicate predicate) {
        final BlockPredicate[] blockPredicates = new BlockPredicate[predicate.predicates().length];
        for (int i = 0; i < predicate.predicates().length; i++) {
            final BlockPredicate blockPredicate = predicate.predicates()[i];
            blockPredicates[i] = new BlockPredicate(blockPredicate.holderSet(), blockPredicate.propertyMatchers(), blockPredicate.tag(), EMPTY_DATA_MATCHERS);
        }
        return new AdventureModePredicate(blockPredicates);
    }

    private static void updateBucketVariant(final StructuredDataContainer dataContainer) {
        final CompoundTag bucketEntityData = dataContainer.get(StructuredDataKey.BUCKET_ENTITY_DATA);
        if (bucketEntityData == null) {
            return;
        }

        final IntTag bucketVariantTag = bucketEntityData.removeUnchecked("BucketVariantTag");
        if (bucketVariantTag == null) {
            return;
        }

        // Unpack into new item components
        final int packedVariant = bucketVariantTag.asInt();
        dataContainer.set(StructuredDataKey.TROPICAL_FISH_BASE_COLOR, packedVariant >> 16 & 0xFF);
        dataContainer.set(StructuredDataKey.TROPICAL_FISH_PATTERN_COLOR, packedVariant >> 24 & 0xFF);
        dataContainer.set(StructuredDataKey.TROPICAL_FISH_PATTERN, new TropicalFishPattern(packedVariant & 65535));
    }

    public static void downgradeItemData(final Item item) {
        final StructuredDataContainer dataContainer = item.dataContainer();
        dataContainer.replaceKey(StructuredDataKey.TOOL1_21_5, StructuredDataKey.TOOL1_20_5);
        dataContainer.replaceKey(StructuredDataKey.EQUIPPABLE1_21_5, StructuredDataKey.EQUIPPABLE1_21_2);
        dataContainer.replace(StructuredDataKey.INSTRUMENT1_21_5, StructuredDataKey.INSTRUMENT1_21_2, instrument -> instrument.hasHolder() ? instrument.holder() : null);

        final TooltipDisplay tooltipDisplay = dataContainer.get(StructuredDataKey.TOOLTIP_DISPLAY);
        if (tooltipDisplay != null) {
            if (tooltipDisplay.hideTooltip()) {
                dataContainer.set(StructuredDataKey.HIDE_TOOLTIP);
            }

            final FullMappings mappings = Protocol1_21_4To1_21_5.MAPPINGS.getDataComponentSerializerMappings();
            if (tooltipDisplay.hiddenComponents().containsAll(HIDE_ADDITIONAL_KEYS.stream().map(key -> mappings.id(key.identifier())).toList())) {
                dataContainer.set(StructuredDataKey.HIDE_ADDITIONAL_TOOLTIP);
            }
        }

        downgradeBucketVariant(dataContainer);
        dataContainer.replace(StructuredDataKey.UNBREAKABLE1_21_5, StructuredDataKey.UNBREAKABLE1_20_5, unbreakable -> new Unbreakable(shouldShowToServer(tooltipDisplay, StructuredDataKey.UNBREAKABLE1_20_5)));
        updateShowInTooltip(dataContainer, tooltipDisplay, StructuredDataKey.DYED_COLOR1_21_5, StructuredDataKey.DYED_COLOR1_20_5, dyedColor -> new DyedColor(dyedColor.rgb(), false));
        updateShowInTooltip(dataContainer, tooltipDisplay, StructuredDataKey.ATTRIBUTE_MODIFIERS1_21_5, StructuredDataKey.ATTRIBUTE_MODIFIERS1_21, attributeModifiers -> new AttributeModifiers1_21(attributeModifiers.modifiers(), false));
        updateShowInTooltip(dataContainer, tooltipDisplay, StructuredDataKey.TRIM1_21_5, StructuredDataKey.TRIM1_21_4, trim -> new ArmorTrim(trim.material(), trim.pattern(), false));
        updateShowInTooltip(dataContainer, tooltipDisplay, StructuredDataKey.ENCHANTMENTS1_21_5, StructuredDataKey.ENCHANTMENTS1_20_5, enchantments -> new Enchantments(enchantments.enchantments(), false));
        updateShowInTooltip(dataContainer, tooltipDisplay, StructuredDataKey.STORED_ENCHANTMENTS1_21_5, StructuredDataKey.STORED_ENCHANTMENTS1_20_5, enchantments -> new Enchantments(enchantments.enchantments(), false));
        updateShowInTooltip(dataContainer, tooltipDisplay, StructuredDataKey.V1_21_5.canPlaceOn, StructuredDataKey.CAN_PLACE_ON1_20_5, canPlaceOn -> new AdventureModePredicate(canPlaceOn.predicates(), false));
        updateShowInTooltip(dataContainer, tooltipDisplay, StructuredDataKey.V1_21_5.canBreak, StructuredDataKey.CAN_BREAK1_20_5, canBreak -> new AdventureModePredicate(canBreak.predicates(), false));
        updateShowInTooltip(dataContainer, tooltipDisplay, StructuredDataKey.JUKEBOX_PLAYABLE1_21_5, StructuredDataKey.JUKEBOX_PLAYABLE1_21, playable -> new JukeboxPlayable(playable.song(), false));

        dataContainer.remove(NEW_DATA_TO_REMOVE);
    }

    private static void downgradeBucketVariant(final StructuredDataContainer dataContainer) {
        final TropicalFishPattern tropicalFishPattern = dataContainer.get(StructuredDataKey.TROPICAL_FISH_PATTERN);
        if (tropicalFishPattern == null) {
            return;
        }

        final Integer base = dataContainer.get(StructuredDataKey.TROPICAL_FISH_BASE_COLOR);
        final Integer pattern = dataContainer.get(StructuredDataKey.TROPICAL_FISH_PATTERN_COLOR);

        // Pack into legacy tag again
        final int baseColor = base != null ? base : 16777215;
        final int patternColor = pattern != null ? pattern : 16777215;
        final int packedVariant = (patternColor & 0xFF) << 24 | (baseColor & 0xFF) << 16 | (tropicalFishPattern.packedId() & 0xFFFF);

        CompoundTag bucketEntityData = dataContainer.get(StructuredDataKey.BUCKET_ENTITY_DATA);
        if (bucketEntityData == null) {
            dataContainer.set(StructuredDataKey.BUCKET_ENTITY_DATA, bucketEntityData = new CompoundTag());
        }
        bucketEntityData.put("BucketVariantTag", new IntTag(packedVariant));
    }

    private StructuredItem convertHashedItemToStructuredItem(final UserConnection connection, final HashedItem hashedItem) {
        final StructuredItem item = new StructuredItem(hashedItem.identifier(), hashedItem.amount());
        final ItemHashStorage1_21_5 hasher = itemHasher(connection);
        final Map<StructuredDataKey<?>, StructuredData<?>> structuredDataMap = item.dataContainer().data();
        for (final Int2IntMap.Entry hashEntry : hashedItem.dataHashesById().int2IntEntrySet()) {
            final StructuredData<?> structuredData = hasher.dataFromHash(hashEntry.getIntKey(), hashEntry.getIntValue());
            if (structuredData != null) {
                structuredDataMap.put(structuredData.key(), structuredData);
            }
        }
        for (final int dataId : hashedItem.removedDataIds()) {
            final StructuredDataKey<?> structuredDataKey = VersionedTypes.V1_21_5.structuredData.key(dataId);
            structuredDataMap.put(structuredDataKey, StructuredData.empty(structuredDataKey, dataId));
        }
        return item;
    }

    private void appendItemDataFixComponents(final UserConnection connection, final Item item) {
        final ProtocolVersion serverVersion = connection.getProtocolInfo().serverProtocolVersion();
        if (serverVersion.olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            if (item.identifier() == 858 || item.identifier() == 863 || item.identifier() == 873 || item.identifier() == 868 || item.identifier() == 878) { // swords
                item.dataContainer().remove(StructuredDataKey.CONSUMABLE1_21_2);
                item.dataContainer().set(StructuredDataKey.BLOCKS_ATTACKS,
                    new BlocksAttacks(
                        0F,
                        0F,
                        new BlocksAttacks.DamageReduction[]{new BlocksAttacks.DamageReduction(90F, null, -0.5F, 0.5F)},
                        new BlocksAttacks.ItemDamageFunction(0F, 0F, 0F),
                        null,
                        null,
                        null
                    )
                );
            }
        }
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
