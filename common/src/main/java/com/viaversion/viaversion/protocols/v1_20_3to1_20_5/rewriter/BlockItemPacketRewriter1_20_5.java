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

import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.ParticleMappings;
import com.viaversion.viaversion.api.minecraft.GameProfile;
import com.viaversion.viaversion.api.minecraft.GlobalBlockPosition;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
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
import com.viaversion.viaversion.api.minecraft.item.data.DebugStickState;
import com.viaversion.viaversion.api.minecraft.item.data.DyedColor;
import com.viaversion.viaversion.api.minecraft.item.data.Enchantments;
import com.viaversion.viaversion.api.minecraft.item.data.FilterableComponent;
import com.viaversion.viaversion.api.minecraft.item.data.FilterableString;
import com.viaversion.viaversion.api.minecraft.item.data.FireworkExplosion;
import com.viaversion.viaversion.api.minecraft.item.data.Fireworks;
import com.viaversion.viaversion.api.minecraft.item.data.FoodProperties1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.FoodProperties1_20_5.FoodEffect;
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
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.api.type.types.version.Types1_20_3;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundPacket1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.Protocol1_20_3To1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.Attributes1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.BannerPatterns1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.DyeColors;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.Enchantments1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.EquipmentSlots1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.Instruments1_20_3;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.MapDecorations1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.MaxStackSize1_20_3;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.PotionEffects1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.Potions1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPacket1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage.ArmorTrimStorage;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage.BannerPatternStorage;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage.TagKeys;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.Either;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.MathUtil;
import com.viaversion.viaversion.util.SerializerVersion;
import com.viaversion.viaversion.util.UUIDUtil;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.viaversion.viaversion.util.MathUtil.clamp;

public final class BlockItemPacketRewriter1_20_5 extends ItemRewriter<ClientboundPacket1_20_3, ServerboundPacket1_20_5, Protocol1_20_3To1_20_5> {

    public static final String[] MOB_TAGS = {"NoAI", "Silent", "NoGravity", "Glowing", "Invulnerable", "Health", "Age", "Variant", "HuntingCooldown", "BucketVariantTag"};
    public static final String[] ATTRIBUTE_OPERATIONS = {"add_value", "add_multiplied_base", "add_multiplied_total"};
    private static final StructuredDataConverter DATA_CONVERTER = new StructuredDataConverter(false);
    private static final GameProfile.Property[] EMPTY_PROPERTIES = new GameProfile.Property[0];
    private static final StatePropertyMatcher[] EMPTY_PROPERTY_MATCHERS = new StatePropertyMatcher[0];

    public BlockItemPacketRewriter1_20_5(final Protocol1_20_3To1_20_5 protocol) {
        super(protocol, Types.ITEM1_20_2, Types.ITEM1_20_2_ARRAY, VersionedTypes.V1_20_5.item, VersionedTypes.V1_20_5.itemArray);
    }

    @Override
    public void registerPackets() {
        final BlockRewriter<ClientboundPacket1_20_3> blockRewriter = BlockRewriter.for1_20_2(protocol);
        blockRewriter.registerBlockEvent(ClientboundPackets1_20_3.BLOCK_EVENT);
        blockRewriter.registerBlockUpdate(ClientboundPackets1_20_3.BLOCK_UPDATE);
        blockRewriter.registerSectionBlocksUpdate1_20(ClientboundPackets1_20_3.SECTION_BLOCKS_UPDATE);
        blockRewriter.registerLevelEvent(ClientboundPackets1_20_3.LEVEL_EVENT, 1010, 2001);
        protocol.registerClientbound(ClientboundPackets1_20_3.LEVEL_CHUNK_WITH_LIGHT, wrapper -> {
            final Chunk chunk = blockRewriter.handleChunk1_19(wrapper, ChunkType1_20_2::new);
            for (int i = 0; i < chunk.blockEntities().size(); i++) {
                final BlockEntity blockEntity = chunk.blockEntities().get(i);
                if (isUnknownBlockEntity(blockEntity.typeId())) {
                    // The client no longer ignores unknown block entities
                    chunk.blockEntities().remove(i--);
                    continue;
                }

                updateBlockEntityTag(wrapper.user(), null, blockEntity.tag());
            }
        });
        protocol.registerClientbound(ClientboundPackets1_20_3.BLOCK_ENTITY_DATA, wrapper -> {
            wrapper.passthrough(Types.BLOCK_POSITION1_14); // Position

            final int typeId = wrapper.passthrough(Types.VAR_INT);
            if (isUnknownBlockEntity(typeId)) {
                wrapper.cancel();
                return;
            }

            CompoundTag tag = wrapper.read(Types.COMPOUND_TAG);
            if (tag != null) {
                updateBlockEntityTag(wrapper.user(), null, tag);
            } else {
                // No longer nullable
                tag = new CompoundTag();
            }
            wrapper.write(Types.COMPOUND_TAG, tag);
        });

        registerCooldown(ClientboundPackets1_20_3.COOLDOWN);
        registerSetContent1_17_1(ClientboundPackets1_20_3.CONTAINER_SET_CONTENT);
        registerSetSlot1_17_1(ClientboundPackets1_20_3.CONTAINER_SET_SLOT);
        registerContainerClick1_17_1(ServerboundPackets1_20_5.CONTAINER_CLICK);
        registerContainerSetData(ClientboundPackets1_20_3.CONTAINER_SET_DATA);
        registerSetCreativeModeSlot(ServerboundPackets1_20_5.SET_CREATIVE_MODE_SLOT);

        protocol.registerServerbound(ServerboundPackets1_20_5.CONTAINER_BUTTON_CLICK, wrapper -> {
            final byte containerId = wrapper.read(Types.VAR_INT).byteValue();
            final byte buttonId = wrapper.read(Types.VAR_INT).byteValue();
            wrapper.write(Types.BYTE, containerId);
            wrapper.write(Types.BYTE, buttonId);
        });

        protocol.registerClientbound(ClientboundPackets1_20_3.UPDATE_ADVANCEMENTS, wrapper -> {
            wrapper.passthrough(Types.BOOLEAN); // Reset/clear
            int size = wrapper.passthrough(Types.VAR_INT); // Mapping size
            for (int i = 0; i < size; i++) {
                wrapper.passthrough(Types.STRING); // Identifier
                wrapper.passthrough(Types.OPTIONAL_STRING); // Parent

                // Display data
                if (wrapper.passthrough(Types.BOOLEAN)) {
                    wrapper.passthrough(Types.TAG); // Title
                    wrapper.passthrough(Types.TAG); // Description

                    Item item = handleNonEmptyItemToClient(wrapper.user(), wrapper.read(itemType()));
                    wrapper.write(mappedItemType(), item);

                    wrapper.passthrough(Types.VAR_INT); // Frame type
                    int flags = wrapper.passthrough(Types.INT); // Flags
                    if ((flags & 1) != 0) {
                        wrapper.passthrough(Types.STRING); // Background texture
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
        });

        protocol.registerClientbound(ClientboundPackets1_20_3.LEVEL_PARTICLES, wrapper -> {
            final int particleId = wrapper.read(Types.VAR_INT);

            wrapper.passthrough(Types.BOOLEAN); // Long Distance
            wrapper.passthrough(Types.DOUBLE); // X
            wrapper.passthrough(Types.DOUBLE); // Y
            wrapper.passthrough(Types.DOUBLE); // Z
            final float offX = wrapper.passthrough(Types.FLOAT);
            final float offY = wrapper.passthrough(Types.FLOAT);
            final float offZ = wrapper.passthrough(Types.FLOAT);
            final float data = wrapper.passthrough(Types.FLOAT);
            final int count = wrapper.passthrough(Types.INT);

            // Read data and add it to Particle
            final ParticleMappings mappings = protocol.getMappingData().getParticleMappings();
            final int mappedId = mappings.getNewId(particleId);
            final Particle particle = new Particle(mappedId);
            if (mappedId == mappings.mappedId("entity_effect")) {
                final int color;
                if (data == 0) {
                    // Black
                    color = 0;
                } else if (count != 0) {
                    // Randomized color
                    color = ThreadLocalRandom.current().nextInt();
                } else {
                    // From offset
                    final int red = Math.round(offX * 255);
                    final int green = Math.round(offY * 255);
                    final int blue = Math.round(offZ * 255);
                    color = (red << 16) | (green << 8) | blue;
                }
                particle.add(Types.INT, EntityPacketRewriter1_20_5.withAlpha(color));
            } else if (particleId == mappings.id("dust_color_transition")) {
                for (int i = 0; i < 7; i++) {
                    particle.add(Types.FLOAT, wrapper.read(Types.FLOAT));
                }
                // fromColor, scale, toColor -> fromColor, toColor, scale
                particle.add(Types.FLOAT, particle.<Float>removeArgument(3).getValue());
            } else if (mappings.isBlockParticle(particleId)) {
                final int blockStateId = wrapper.read(Types.VAR_INT);
                particle.add(Types.VAR_INT, protocol.getMappingData().getNewBlockStateId(blockStateId));
            } else if (mappings.isItemParticle(particleId)) {
                final Item item = handleNonEmptyItemToClient(wrapper.user(), wrapper.read(Types.ITEM1_20_2));
                particle.add(VersionedTypes.V1_20_5.item, item);
            } else if (particleId == mappings.id("dust")) {
                // R, g, b, scale
                for (int i = 0; i < 4; i++) {
                    particle.add(Types.FLOAT, wrapper.read(Types.FLOAT));
                }
            } else if (particleId == mappings.id("vibration")) {
                final int sourceTypeId = wrapper.read(Types.VAR_INT);
                particle.add(Types.VAR_INT, sourceTypeId);
                if (sourceTypeId == 0) { // Block
                    particle.add(Types.BLOCK_POSITION1_14, wrapper.read(Types.BLOCK_POSITION1_14)); // Target block pos
                } else if (sourceTypeId == 1) { // Entity
                    particle.add(Types.VAR_INT, wrapper.read(Types.VAR_INT)); // Target entity
                    particle.add(Types.FLOAT, wrapper.read(Types.FLOAT)); // Y offset
                } else {
                    protocol.getLogger().warning("Unknown vibration path position source type: " + sourceTypeId);
                }
                particle.add(Types.VAR_INT, wrapper.read(Types.VAR_INT)); // Arrival in ticks
            } else if (particleId == mappings.id("sculk_charge")) {
                particle.add(Types.FLOAT, wrapper.read(Types.FLOAT)); // Roll
            } else if (particleId == mappings.id("shriek")) {
                particle.add(Types.VAR_INT, wrapper.read(Types.VAR_INT)); // Delay
            }

            wrapper.write(VersionedTypes.V1_20_5.particle, particle);
        });

        protocol.registerClientbound(ClientboundPackets1_20_3.EXPLODE, wrapper -> {
            wrapper.passthrough(Types.DOUBLE); // X
            wrapper.passthrough(Types.DOUBLE); // Y
            wrapper.passthrough(Types.DOUBLE); // Z
            wrapper.passthrough(Types.FLOAT); // Power
            final int blocks = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < blocks; i++) {
                wrapper.passthrough(Types.BYTE); // Relative X
                wrapper.passthrough(Types.BYTE); // Relative Y
                wrapper.passthrough(Types.BYTE); // Relative Z
            }
            wrapper.passthrough(Types.FLOAT); // Knockback X
            wrapper.passthrough(Types.FLOAT); // Knockback Y
            wrapper.passthrough(Types.FLOAT); // Knockback Z
            wrapper.passthrough(Types.VAR_INT); // Block interaction type

            final Particle smallExplosionParticle = wrapper.passthroughAndMap(Types1_20_3.PARTICLE, VersionedTypes.V1_20_5.particle);
            final Particle largeExplosionParticle = wrapper.passthroughAndMap(Types1_20_3.PARTICLE, VersionedTypes.V1_20_5.particle);
            protocol.getParticleRewriter().rewriteParticle(wrapper.user(), smallExplosionParticle);
            protocol.getParticleRewriter().rewriteParticle(wrapper.user(), largeExplosionParticle);

            final String sound = wrapper.read(Types.STRING);
            final Float range = wrapper.read(Types.OPTIONAL_FLOAT);
            wrapper.write(Types.SOUND_EVENT, Holder.of(new SoundEvent(sound, range)));
        });

        protocol.registerClientbound(ClientboundPackets1_20_3.MERCHANT_OFFERS, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Container id
            final int size = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < size; i++) {
                Item input = handleNonEmptyItemToClient(wrapper.user(), wrapper.read(Types.ITEM1_20_2));
                wrapper.write(VersionedTypes.V1_20_5.itemCost, input);

                final Item output = handleNonEmptyItemToClient(wrapper.user(), wrapper.read(Types.ITEM1_20_2));
                wrapper.write(VersionedTypes.V1_20_5.item, output);

                Item secondInput = wrapper.read(Types.ITEM1_20_2);
                if (secondInput != null) {
                    secondInput = handleItemToClient(wrapper.user(), secondInput);
                    if (secondInput.isEmpty()) {
                        secondInput = null;
                    }
                }
                wrapper.write(VersionedTypes.V1_20_5.optionalItemCost, secondInput);

                wrapper.passthrough(Types.BOOLEAN); // Out of stock
                wrapper.passthrough(Types.INT); // Number of trade uses
                wrapper.passthrough(Types.INT); // Maximum number of trade uses
                wrapper.passthrough(Types.INT); // XP
                wrapper.passthrough(Types.INT); // Special price
                wrapper.passthrough(Types.FLOAT); // Price multiplier
                wrapper.passthrough(Types.INT); // Demand
            }
        });

        final RecipeRewriter1_20_5<ClientboundPacket1_20_3> recipeRewriter = new RecipeRewriter1_20_5<>(protocol);
        protocol.registerClientbound(ClientboundPackets1_20_3.UPDATE_RECIPES, wrapper -> {
            final int size = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < size; i++) {
                // Change order and write the type as an int
                final String type = wrapper.read(Types.STRING);
                wrapper.passthrough(Types.STRING); // Recipe Identifier

                wrapper.write(Types.VAR_INT, protocol.getMappingData().getRecipeSerializerMappings().mappedId(type));
                recipeRewriter.handleRecipeType(wrapper, type);
            }
        });
    }

    public Item handleNonEmptyItemToClient(final UserConnection connection, @Nullable Item item) {
        item = handleItemToClient(connection, item);
        // Items are no longer nullable in a few places
        if (item.isEmpty()) {
            return new StructuredItem(1, 1);
        }
        return item;
    }

    @Override
    public Item handleItemToClient(final UserConnection connection, @Nullable final Item item) {
        if (item == null) {
            // We no longer want null items, always unify them to empty
            return StructuredItem.empty();
        }

        final CompoundTag tag = item.tag();
        final Item structuredItem = toStructuredItem(connection, item);

        // Add the original as custom data, to be re-used for creative clients as well
        if (tag != null) {
            tag.putBoolean(nbtTagName(), true);
            structuredItem.dataContainer().set(StructuredDataKey.CUSTOM_DATA, tag);
        }

        // Add data components to fix issues in older protocols
        appendItemDataFixComponents(connection, structuredItem);

        if (Via.getConfig().handleInvalidItemCount()) {
            // Server can send amounts which are higher than vanilla's default, and 1.20.4 will still accept them,
            // let's use the new added data key to emulate this behavior
            if (structuredItem.amount() > MaxStackSize1_20_3.getMaxStackSize(structuredItem.identifier())) {
                structuredItem.dataContainer().set(StructuredDataKey.MAX_STACK_SIZE, structuredItem.amount());
            }
        }
        return super.handleItemToClient(connection, structuredItem);
    }

    @Override
    public @Nullable Item handleItemToServer(UserConnection connection, final Item item) {
        if (item.isEmpty()) {
            // Empty to null for the old protocols
            return null;
        }

        super.handleItemToServer(connection, item);
        return toOldItem(connection, item, DATA_CONVERTER);
    }

    public Item toOldItem(final UserConnection connection, final Item item, final StructuredDataConverter dataConverter) {
        // Start out with custom data and add the rest on top, or short-circuit with the original item
        final StructuredDataContainer data = item.dataContainer();
        data.setIdLookup(protocol, true);

        final StructuredData<CompoundTag> customData = data.getNonEmptyData(StructuredDataKey.CUSTOM_DATA);
        final CompoundTag tag = customData != null ? customData.value() : new CompoundTag();
        final DataItem dataItem = new DataItem(item.identifier(), (byte) item.amount(), tag);
        if (!dataConverter.backupInconvertibleData() && customData != null && tag.remove(nbtTagName()) != null) {
            // Skip for VB since it's used for incoming item data
            return dataItem;
        }

        for (final StructuredData<?> structuredData : data.data().values()) {
            dataConverter.writeToTag(connection, structuredData, tag);
        }

        if (tag.isEmpty()) {
            dataItem.setTag(null);
        }

        return dataItem;
    }

    public Item toStructuredItem(final UserConnection connection, final Item old) {
        final CompoundTag tag = old.tag();
        final StructuredItem item = new StructuredItem(old.identifier(), (byte) old.amount(), new StructuredDataContainer());
        final StructuredDataContainer data = item.dataContainer();
        data.setIdLookup(protocol, true);

        if (tag == null) {
            return item;
        }

        // Rewrite nbt to new data structures
        final int hideFlagsValue = tag.getInt("HideFlags");
        if ((hideFlagsValue & StructuredDataConverter.HIDE_ADDITIONAL) != 0) {
            data.set(StructuredDataKey.HIDE_ADDITIONAL_TOOLTIP);
        }

        updateDisplay(connection, data, tag.getCompoundTag("display"), hideFlagsValue);

        final NumberTag damage = tag.getNumberTag("Damage");
        if (damage != null && damage.asInt() > 0) {
            data.set(StructuredDataKey.DAMAGE, damage.asInt());
        }

        final NumberTag repairCost = tag.getNumberTag("RepairCost");
        if (repairCost != null && repairCost.asInt() > 0) {
            data.set(StructuredDataKey.REPAIR_COST, repairCost.asInt());
        }

        final NumberTag customModelData = tag.getNumberTag("CustomModelData");
        if (customModelData != null) {
            data.set(StructuredDataKey.CUSTOM_MODEL_DATA1_20_5, customModelData.asInt());
        }

        final CompoundTag blockState = tag.getCompoundTag("BlockStateTag");
        if (blockState != null) {
            updateBlockState(data, blockState);
        }

        CompoundTag entityTag = tag.getCompoundTag("EntityTag");
        if (entityTag != null) {
            entityTag = entityTag.copy();
            // Additional tooltips of items don't validate the entity id in 1.20.4, add the entity id for these back so
            // 1.20.5+ parses it correctly (as otherwise not used on the client).
            if (entityTag.contains("variant")) {
                entityTag.putString("id", "minecraft:painting");
            }

            if (entityTag.contains("id")) {
                data.set(StructuredDataKey.ENTITY_DATA1_20_5, entityTag);
            }
        }

        final CompoundTag blockEntityTag = tag.getCompoundTag("BlockEntityTag");
        if (blockEntityTag != null) {
            final CompoundTag clonedTag = blockEntityTag.copy();
            // Same as above
            updateBlockEntityTag(connection, data, clonedTag);
            CompoundTag spawnData = clonedTag.getCompoundTag("SpawnData");
            if (spawnData == null) {
                spawnData = clonedTag.getCompoundTag("spawn_data");
            }
            if (spawnData != null) {
                final CompoundTag entity = spawnData.getCompoundTag("entity");
                if (entity != null && entity.getString("id") != null) {
                    addBlockEntityId(clonedTag, clonedTag.contains("SpawnData") ? "mob_spawner" : "trial_spawner");
                }
            }

            // Not always needed, e.g. shields that had the base color in a block entity tag before
            if (clonedTag.contains("id")) {
                item.dataContainer().set(StructuredDataKey.BLOCK_ENTITY_DATA1_20_5, clonedTag);
            }
        }

        final CompoundTag debugProperty = tag.getCompoundTag("DebugProperty");
        if (debugProperty != null) {
            data.set(StructuredDataKey.DEBUG_STICK_STATE, new DebugStickState(debugProperty.copy()));
        }

        final NumberTag unbreakable = tag.getNumberTag("Unbreakable");
        if (unbreakable != null && unbreakable.asBoolean()) {
            data.set(StructuredDataKey.UNBREAKABLE1_20_5, new Unbreakable((hideFlagsValue & StructuredDataConverter.HIDE_UNBREAKABLE) == 0));
        }

        final CompoundTag trimTag = tag.getCompoundTag("Trim");
        if (trimTag != null) {
            updateArmorTrim(connection, data, trimTag, (hideFlagsValue & StructuredDataConverter.HIDE_ARMOR_TRIM) == 0);
        }

        final CompoundTag explosionTag = tag.getCompoundTag("Explosion");
        if (explosionTag != null) {
            data.set(StructuredDataKey.FIREWORK_EXPLOSION, readExplosion(explosionTag));
        }

        final ListTag<StringTag> recipesTag = tag.getListTag("Recipes", StringTag.class);
        if (recipesTag != null) {
            data.set(StructuredDataKey.RECIPES, recipesTag);
        }

        final NumberTag trackedTag = tag.getNumberTag("LodestoneTracked");
        if (trackedTag != null) {
            final CompoundTag lodestonePosTag = tag.getCompoundTag("LodestonePos");
            final String lodestoneDimension = tag.getString("LodestoneDimension");
            updateLodestoneTracker(trackedTag.asBoolean(), lodestonePosTag, lodestoneDimension, data);
        }

        final ListTag<CompoundTag> effectsTag = tag.getListTag("effects", CompoundTag.class);
        if (effectsTag != null) {
            updateEffects(effectsTag, data);
        }

        final String instrument = tag.getString("instrument");
        if (instrument != null) {
            final int id = Instruments1_20_3.keyToId(instrument);
            if (id != -1) {
                data.set(StructuredDataKey.INSTRUMENT1_20_5, Holder.of(id));
            }
        }

        final ListTag<CompoundTag> attributeModifiersTag = tag.getListTag("AttributeModifiers", CompoundTag.class);
        final boolean showAttributes = (hideFlagsValue & StructuredDataConverter.HIDE_ATTRIBUTES) == 0;
        if (attributeModifiersTag != null) {
            updateAttributes(data, attributeModifiersTag, showAttributes);
        } else if (!showAttributes) {
            data.set(StructuredDataKey.ATTRIBUTE_MODIFIERS1_20_5, new AttributeModifiers1_20_5(new AttributeModifier[0], false));
        }

        final CompoundTag fireworksTag = tag.getCompoundTag("Fireworks");
        if (fireworksTag != null) {
            final ListTag<CompoundTag> explosionsTag = fireworksTag.getListTag("Explosions", CompoundTag.class);
            updateFireworks(data, fireworksTag, explosionsTag);
        }

        if (old.identifier() == 1085) {
            updateWritableBookPages(data, tag);
        } else if (old.identifier() == 1086) {
            updateWrittenBookPages(connection, data, tag);
        }

        updatePotionTags(data, tag);

        updateMobTags(data, tag);

        updateItemList(connection, data, tag, "ChargedProjectiles", StructuredDataKey.V1_20_5.chargedProjectiles);
        if (old.identifier() == 927) {
            updateItemList(connection, data, tag, "Items", StructuredDataKey.V1_20_5.bundleContents);
        }

        updateEnchantments(data, tag, "Enchantments", StructuredDataKey.ENCHANTMENTS1_20_5, (hideFlagsValue & StructuredDataConverter.HIDE_ENCHANTMENTS) == 0);
        updateEnchantments(data, tag, "StoredEnchantments", StructuredDataKey.STORED_ENCHANTMENTS1_20_5, (hideFlagsValue & StructuredDataConverter.HIDE_ADDITIONAL) == 0);

        final NumberTag mapId = tag.getNumberTag("map");
        if (mapId != null) {
            data.set(StructuredDataKey.MAP_ID, mapId.asInt());
        }

        final ListTag<CompoundTag> decorationsTag = tag.getListTag("Decorations", CompoundTag.class);
        if (decorationsTag != null) {
            updateMapDecorations(data, decorationsTag);
        }

        updateProfile(data, tag.get("SkullOwner"));

        final ListTag<StringTag> canPlaceOnTag = tag.getListTag("CanPlaceOn", StringTag.class);
        if (canPlaceOnTag != null) {
            data.set(StructuredDataKey.CAN_PLACE_ON1_20_5, updateBlockPredicates(connection, canPlaceOnTag, (hideFlagsValue & StructuredDataConverter.HIDE_CAN_PLACE_ON) == 0));
        }

        final ListTag<StringTag> canDestroyTag = tag.getListTag("CanDestroy", StringTag.class);
        if (canDestroyTag != null) {
            data.set(StructuredDataKey.CAN_BREAK1_20_5, updateBlockPredicates(connection, canDestroyTag, (hideFlagsValue & StructuredDataConverter.HIDE_CAN_DESTROY) == 0));
        }

        // Only for VB, but kept here for simplicity; In VV we back up the original tag and later restore it, in VB
        // we use the converted data and manually restore what broke during the conversion
        final CompoundTag backupTag = StructuredDataConverter.removeBackupTag(tag);
        if (backupTag != null) {
            restoreFromBackupTag(backupTag, data);
        }
        return item;
    }

    private void appendItemDataFixComponents(final UserConnection connection, final Item item) {
        final ProtocolVersion serverVersion = connection.getProtocolInfo().serverProtocolVersion();
        if (serverVersion.olderThanOrEqualTo(ProtocolVersion.v1_17_1)) {
            if (item.identifier() == 1182) { // crossbow
                // Change crossbow damage to value used in 1.17.1 and lower
                item.dataContainer().set(StructuredDataKey.MAX_DAMAGE, 326);
            }
        }
    }

    private int unmappedItemId(final String name) {
        return protocol.getMappingData().getFullItemMappings().id(name);
    }

    private int toMappedItemId(final String name) {
        final int unmappedId = unmappedItemId(name);
        return unmappedId != -1 ? protocol.getMappingData().getNewItemId(unmappedId) : -1;
    }

    private void restoreFromBackupTag(final CompoundTag backupTag, final StructuredDataContainer data) {
        final CompoundTag instrument = backupTag.getCompoundTag("instrument");
        if (instrument != null) {
            restoreInstrumentFromBackup(instrument, data);
        }

        final IntArrayTag potDecorationsTag = backupTag.getIntArrayTag("pot_decorations");
        if (potDecorationsTag != null && potDecorationsTag.getValue().length == 4) {
            data.set(StructuredDataKey.POT_DECORATIONS, new PotDecorations(potDecorationsTag.getValue()));
        }

        final ByteTag enchantmentGlintOverride = backupTag.getByteTag("enchantment_glint_override");
        if (enchantmentGlintOverride != null) {
            data.set(StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE, enchantmentGlintOverride.asBoolean());
        }

        if (backupTag.contains("hide_tooltip")) {
            data.set(StructuredDataKey.HIDE_TOOLTIP);
        }

        final Tag intangibleProjectile = backupTag.get("intangible_projectile");
        if (intangibleProjectile != null) {
            data.set(StructuredDataKey.INTANGIBLE_PROJECTILE, intangibleProjectile);
        }

        final IntTag maxStackSize = backupTag.getIntTag("max_stack_size");
        if (maxStackSize != null) {
            data.set(StructuredDataKey.MAX_STACK_SIZE, clamp(maxStackSize.asInt(), 1, 99));
        }

        final IntTag maxDamage = backupTag.getIntTag("max_damage");
        if (maxDamage != null) {
            data.set(StructuredDataKey.MAX_DAMAGE, Math.max(maxDamage.asInt(), 1));
        }

        final IntTag rarity = backupTag.getIntTag("rarity");
        if (rarity != null) {
            data.set(StructuredDataKey.RARITY, rarity.asInt());
        }

        final CompoundTag food = backupTag.getCompoundTag("food");
        if (food != null) {
            restoreFoodFromBackup(food, data);
        }

        if (backupTag.contains("fire_resistant")) {
            data.set(StructuredDataKey.FIRE_RESISTANT);
        }

        final CompoundTag tool = backupTag.getCompoundTag("tool");
        if (tool != null) {
            restoreToolFromBackup(tool, data);
        }

        final IntTag ominousBottleAmplifier = backupTag.getIntTag("ominous_bottle_amplifier");
        if (ominousBottleAmplifier != null) {
            data.set(StructuredDataKey.OMINOUS_BOTTLE_AMPLIFIER, clamp(ominousBottleAmplifier.asInt(), 0, 4));
        }

        final ListTag<CompoundTag> bannerPatterns = backupTag.getListTag("banner_patterns", CompoundTag.class);
        if (bannerPatterns != null) {
            restoreBannerPatternsFromBackup(bannerPatterns, data);
        }
    }

    private void restoreInstrumentFromBackup(final CompoundTag instrument, final StructuredDataContainer data) {
        final int useDuration = instrument.getInt("use_duration");
        final float range = instrument.getFloat("range");

        final Holder<SoundEvent> soundEvent;
        final CompoundTag soundEventTag = instrument.getCompoundTag("sound_event");
        if (soundEventTag != null) {
            final StringTag identifier = soundEventTag.getStringTag("identifier");
            if (identifier == null) {
                return;
            }

            soundEvent = Holder.of(new SoundEvent(
                identifier.getValue(),
                soundEventTag.contains("fixed_range") ? soundEventTag.getFloat("fixed_range") : null
            ));
        } else {
            soundEvent = Holder.of(instrument.getInt("sound_event"));
        }

        data.set(StructuredDataKey.INSTRUMENT1_20_5, Holder.of(new Instrument1_20_5(soundEvent, useDuration, range)));
    }

    private void restoreFoodFromBackup(final CompoundTag food, final StructuredDataContainer data) {
        final int nutrition = food.getInt("nutrition");
        final float saturation = food.getFloat("saturation");
        final boolean canAlwaysEat = food.getBoolean("can_always_eat");
        final float eatSeconds = food.getFloat("eat_seconds");

        final ListTag<CompoundTag> possibleEffectsTag = food.getListTag("possible_effects", CompoundTag.class);
        if (possibleEffectsTag == null) {
            return;
        }

        final List<FoodEffect> possibleEffects = new ArrayList<>();
        for (final CompoundTag effect : possibleEffectsTag) {
            final CompoundTag potionEffectTag = effect.getCompoundTag("effect");
            if (potionEffectTag == null) {
                continue;
            }

            possibleEffects.add(new FoodEffect(
                new PotionEffect(
                    potionEffectTag.getInt("effect"),
                    readPotionEffectData(potionEffectTag)
                ),
                effect.getFloat("probability")
            ));
        }
        data.set(StructuredDataKey.FOOD1_20_5, new FoodProperties1_20_5(nutrition, saturation, canAlwaysEat, eatSeconds, null, possibleEffects.toArray(new FoodEffect[0])));
    }

    private void restoreToolFromBackup(final CompoundTag tool, final StructuredDataContainer data) {
        final ListTag<CompoundTag> rulesTag = tool.getListTag("rules", CompoundTag.class);
        if (rulesTag == null) {
            return;
        }

        final List<ToolRule> rules = new ArrayList<>();
        for (final CompoundTag tag : rulesTag) {
            HolderSet blocks = null;
            if (tag.get("blocks") instanceof StringTag blocksTag) {
                blocks = HolderSet.of(blocksTag.getValue());
            } else {
                final IntArrayTag blockIds = tag.getIntArrayTag("blocks");
                if (blockIds != null) {
                    blocks = HolderSet.of(blockIds.getValue());
                }
            }
            if (blocks == null) {
                continue;
            }

            rules.add(new ToolRule(
                blocks,
                tag.contains("speed") ? tag.getFloat("speed") : null,
                tag.contains("correct_for_drops") ? tag.getBoolean("correct_for_drops") : null
            ));
        }
        data.set(StructuredDataKey.TOOL1_20_5, new ToolProperties(
            rules.toArray(new ToolRule[0]),
            tool.getFloat("default_mining_speed"),
            tool.getInt("damage_per_block")
        ));
    }

    private void restoreBannerPatternsFromBackup(final ListTag<CompoundTag> bannerPatterns, final StructuredDataContainer data) {
        final List<BannerPatternLayer> patternLayer = new ArrayList<>();
        for (final CompoundTag tag : bannerPatterns) {
            final CompoundTag patternTag = tag.getCompoundTag("pattern");
            final Holder<BannerPattern> pattern;
            if (patternTag != null) {
                final String assetId = patternTag.getString("asset_id");
                final String translationKey = patternTag.getString("translation_key");
                pattern = Holder.of(new BannerPattern(assetId, translationKey));
            } else {
                pattern = Holder.of(tag.getInt("pattern"));
            }

            final int dyeColor = tag.getInt("dye_color");
            patternLayer.add(new BannerPatternLayer(pattern, dyeColor));
        }
        data.set(StructuredDataKey.BANNER_PATTERNS, patternLayer.toArray(new BannerPatternLayer[0]));
    }

    private AdventureModePredicate updateBlockPredicates(final UserConnection connection, final ListTag<StringTag> tag, final boolean showInTooltip) {
        final BlockPredicate[] predicates = tag.stream()
            .map(StringTag::getValue)
            .map(rawPredicate -> deserializeBlockPredicate(connection, rawPredicate))
            .filter(Objects::nonNull)
            .toArray(BlockPredicate[]::new);
        return new AdventureModePredicate(predicates, showInTooltip);
    }

    private @Nullable BlockPredicate deserializeBlockPredicate(final UserConnection connection, final String rawPredicate) {
        final int propertiesStartIndex = rawPredicate.indexOf('[');
        final int tagStartIndex = rawPredicate.indexOf('{');
        int idLength = rawPredicate.length();
        if (propertiesStartIndex != -1) {
            idLength = propertiesStartIndex;
        }
        if (tagStartIndex != -1) {
            idLength = Math.min(propertiesStartIndex, tagStartIndex);
        }

        final String identifier = rawPredicate.substring(0, idLength);
        final HolderSet holders;
        if (!identifier.startsWith("#")) {
            final int id = Protocol1_20_3To1_20_5.MAPPINGS.blockId(identifier);
            if (id == -1) {
                return null;
            }

            holders = HolderSet.of(new int[]{id});
        } else {
            final String tagKey = identifier.substring(1);
            if (!connection.get(TagKeys.class).isValidIdentifier(tagKey)) {
                return null;
            }

            holders = HolderSet.of(tagKey);
        }

        final int propertiesEndIndex = rawPredicate.indexOf(']');
        final List<StatePropertyMatcher> propertyMatchers = new ArrayList<>();
        if (propertiesStartIndex != -1 && propertiesEndIndex != -1) {
            for (final String property : rawPredicate.substring(propertiesStartIndex + 1, propertiesEndIndex).split(",")) {
                final int propertySplitIndex = property.indexOf('=');
                if (propertySplitIndex == -1) {
                    continue;
                }

                final String propertyId = property.substring(0, propertySplitIndex).trim();
                final String propertyValue = property.substring(propertySplitIndex + 1).trim();
                propertyMatchers.add(new StatePropertyMatcher(propertyId, Either.left(propertyValue)));
            }
        }

        final int tagEndIndex = rawPredicate.indexOf('}');
        CompoundTag tag = null;
        if (tagStartIndex != -1 && tagEndIndex != -1) {
            try {
                tag = (CompoundTag) SerializerVersion.V1_20_3.toTag(rawPredicate.substring(tagStartIndex, tagEndIndex + 1));
            } catch (final Exception e) {
                if (Via.getManager().isDebug()) {
                    Protocol1_20_3To1_20_5.LOGGER.log(Level.SEVERE, "Failed to parse block predicate tag: " + rawPredicate.substring(tagStartIndex, tagEndIndex + 1), e);
                }
            }
        }

        return new BlockPredicate(
            holders,
            propertyMatchers.isEmpty() ? null : propertyMatchers.toArray(EMPTY_PROPERTY_MATCHERS),
            tag
        );
    }

    private void updateAttributes(final StructuredDataContainer data, final ListTag<CompoundTag> attributeModifiersTag, final boolean showInTooltip) {
        final List<AttributeModifier> modifiers = new ArrayList<>();
        for (int i = 0; i < attributeModifiersTag.size(); i++) {
            final CompoundTag modifierTag = attributeModifiersTag.get(i);
            final String attributeName = modifierTag.getString("AttributeName");
            final String name = modifierTag.getString("Name");
            final NumberTag amountTag = modifierTag.getNumberTag("Amount");
            final IntArrayTag uuidTag = modifierTag.getIntArrayTag("UUID");
            final String slotType = modifierTag.getString("Slot", "any");
            if (name == null || attributeName == null || amountTag == null || uuidTag == null) {
                continue;
            }

            final int slotTypeId = EquipmentSlots1_20_5.keyToId(slotType);
            if (slotTypeId == -1) {
                continue;
            }

            final int operationId = modifierTag.getInt("Operation");
            if (operationId < 0 || operationId > 2) {
                continue;
            }

            final int attributeId = Attributes1_20_5.keyToId(attributeName);
            if (attributeId == -1) {
                continue;
            }

            modifiers.add(new AttributeModifier(
                attributeId,
                new ModifierData(
                    UUIDUtil.fromIntArray(uuidTag.getValue()),
                    name,
                    amountTag.asDouble(),
                    operationId
                ),
                slotTypeId
            ));
        }
        data.set(StructuredDataKey.ATTRIBUTE_MODIFIERS1_20_5, new AttributeModifiers1_20_5(modifiers.toArray(new AttributeModifier[0]), showInTooltip));
    }

    private PotionEffectData readPotionEffectData(final CompoundTag tag) {
        final byte amplifier = tag.getByte("amplifier");
        final int duration = tag.getInt("duration");
        final boolean ambient = tag.getBoolean("ambient");
        final boolean showParticles = tag.getBoolean("show_particles");
        final boolean showIcon = tag.getBoolean("show_icon");

        PotionEffectData hiddenEffect = null;
        final CompoundTag hiddenEffectTag = tag.getCompoundTag("hidden_effect");
        if (hiddenEffectTag != null) {
            hiddenEffect = readPotionEffectData(hiddenEffectTag);
        }
        return new PotionEffectData(amplifier, duration, ambient, showParticles, showIcon, hiddenEffect);
    }

    private void updatePotionTags(final StructuredDataContainer data, final CompoundTag tag) {
        final String potion = tag.getString("Potion");
        Integer potionId = null;
        if (potion != null) {
            final int id = Potions1_20_5.keyToId(potion);
            if (id != -1) {
                potionId = id;
            }
        }

        final NumberTag customPotionColorTag = tag.getNumberTag("CustomPotionColor");
        final ListTag<CompoundTag> customPotionEffectsTag = tag.getListTag("custom_potion_effects", CompoundTag.class);
        PotionEffect[] potionEffects = null;
        if (customPotionEffectsTag != null) {
            potionEffects = customPotionEffectsTag.stream().map(effectTag -> {
                final String identifier = effectTag.getString("id");
                if (identifier == null) {
                    return null;
                }

                final int id = PotionEffects1_20_5.keyToId(identifier);
                if (id == -1) {
                    return null;
                }
                return new PotionEffect(id, readPotionEffectData(effectTag));
            }).filter(Objects::nonNull).toArray(PotionEffect[]::new);
        }

        if (potionId != null || customPotionColorTag != null || potionEffects != null) {
            data.set(StructuredDataKey.POTION_CONTENTS1_20_5, new PotionContents(
                potionId,
                customPotionColorTag != null ? customPotionColorTag.asInt() : null,
                potionEffects != null ? potionEffects : new PotionEffect[0]
            ));
        }
    }

    private void updateArmorTrim(final UserConnection connection, final StructuredDataContainer data, final CompoundTag trimTag, final boolean showInTooltip) {
        final Tag materialTag = trimTag.get("material");
        final Holder<ArmorTrimMaterial> materialHolder;
        final ArmorTrimStorage trimStorage = connection.get(ArmorTrimStorage.class);
        if (materialTag instanceof StringTag materialStringTag) {
            final int id = trimStorage.trimMaterials().keyToId(materialStringTag.getValue());
            if (id == -1) {
                return;
            }

            materialHolder = Holder.of(id);
        } else if (materialTag instanceof CompoundTag materialCompoundTag) {
            final StringTag assetNameTag = materialCompoundTag.getStringTag("asset_name");
            final StringTag ingredientTag = materialCompoundTag.getStringTag("ingredient");
            if (assetNameTag == null || ingredientTag == null) {
                return;
            }

            final int ingredientId = StructuredDataConverter.removeItemBackupTag(materialCompoundTag, toMappedItemId(ingredientTag.getValue()));
            if (ingredientId == -1) {
                return;
            }

            final NumberTag itemModelIndexTag = materialCompoundTag.getNumberTag("item_model_index");
            final CompoundTag overrideArmorMaterialsTag = materialCompoundTag.getCompoundTag("override_armor_materials");
            final Tag descriptionTag = materialCompoundTag.get("description");

            final Map<String, String> overrideArmorMaterials = new Object2ObjectArrayMap<>();
            if (overrideArmorMaterialsTag != null) {
                for (final Map.Entry<String, Tag> entry : overrideArmorMaterialsTag.entrySet()) {
                    if (!(entry.getValue() instanceof StringTag valueTag)) {
                        continue;
                    }

                    overrideArmorMaterials.put(entry.getKey(), valueTag.getValue());
                }
            }

            materialHolder = Holder.of(new ArmorTrimMaterial(
                assetNameTag.getValue(),
                ingredientId,
                itemModelIndexTag != null ? itemModelIndexTag.asFloat() : 0,
                overrideArmorMaterials,
                descriptionTag
            ));
        } else return;

        final Tag patternTag = trimTag.get("pattern");
        final Holder<ArmorTrimPattern> patternHolder;
        if (patternTag instanceof StringTag patternStringTag) {
            final int id = trimStorage.trimPatterns().keyToId(patternStringTag.getValue());
            if (id == -1) {
                return;
            }

            patternHolder = Holder.of(id);
        } else if (patternTag instanceof CompoundTag patternCompoundTag) {
            final String assetId = patternCompoundTag.getString("assetId");
            final String templateItem = patternCompoundTag.getString("templateItem");
            if (assetId == null || templateItem == null) {
                return;
            }

            final int templateItemId = StructuredDataConverter.removeItemBackupTag(patternCompoundTag, toMappedItemId(templateItem));
            if (templateItemId == -1) {
                return;
            }

            final Tag descriptionTag = patternCompoundTag.get("description");
            final boolean decal = patternCompoundTag.getBoolean("decal");
            patternHolder = Holder.of(new ArmorTrimPattern(
                assetId,
                templateItemId,
                descriptionTag,
                decal
            ));
        } else return;

        data.set(StructuredDataKey.TRIM1_20_5, new ArmorTrim(materialHolder, patternHolder, showInTooltip));
    }

    private void updateMobTags(final StructuredDataContainer data, final CompoundTag tag) {
        final CompoundTag bucketEntityData = new CompoundTag();
        for (final String mobTagKey : MOB_TAGS) {
            final Tag mobTag = tag.get(mobTagKey);
            if (mobTag != null) {
                bucketEntityData.put(mobTagKey, mobTag);
            }
        }

        if (!bucketEntityData.isEmpty()) {
            data.set(StructuredDataKey.BUCKET_ENTITY_DATA, bucketEntityData);
        }
    }

    private void updateBlockState(final StructuredDataContainer data, final CompoundTag blockState) {
        final Map<String, String> properties = new HashMap<>();
        for (final Map.Entry<String, Tag> entry : blockState.entrySet()) {
            // Only String and IntTags are valid
            final Tag value = entry.getValue();
            if (value instanceof StringTag valueStringTag) {
                properties.put(entry.getKey(), valueStringTag.getValue());
            } else if (value instanceof IntTag valueIntTag) {
                properties.put(entry.getKey(), Integer.toString(valueIntTag.asInt()));
            }
        }
        data.set(StructuredDataKey.BLOCK_STATE, new BlockStateProperties(properties));
    }

    private void updateFireworks(final StructuredDataContainer data, final CompoundTag fireworksTag, final ListTag<CompoundTag> explosionsTag) {
        final int flightDuration = fireworksTag.getByte("Flight");
        final Fireworks fireworks = new Fireworks(
            flightDuration,
            explosionsTag != null ? explosionsTag.stream().limit(256).
                map(this::readExplosion).toArray(FireworkExplosion[]::new) : new FireworkExplosion[0]
        );
        data.set(StructuredDataKey.FIREWORKS, fireworks);
    }

    private void updateEffects(final ListTag<CompoundTag> effects, final StructuredDataContainer data) {
        final SuspiciousStewEffect[] suspiciousStewEffects = new SuspiciousStewEffect[effects.size()];
        for (int i = 0; i < effects.size(); i++) {
            final CompoundTag effect = effects.get(i);
            final String effectIdString = effect.getString("id", "luck");
            final int duration = effect.getInt("duration");
            final int effectId = PotionEffects1_20_5.keyToId(effectIdString);
            if (effectId != -1) {
                final SuspiciousStewEffect stewEffect = new SuspiciousStewEffect(
                    effectId,
                    duration
                );
                suspiciousStewEffects[i] = stewEffect;
            }
        }
        data.set(StructuredDataKey.SUSPICIOUS_STEW_EFFECTS, suspiciousStewEffects);
    }

    private void updateLodestoneTracker(final boolean tracked, final CompoundTag lodestonePosTag, final String lodestoneDimension, final StructuredDataContainer data) {
        GlobalBlockPosition position = null;
        if (lodestonePosTag != null && lodestoneDimension != null) {
            final int x = lodestonePosTag.getInt("X");
            final int y = lodestonePosTag.getInt("Y");
            final int z = lodestonePosTag.getInt("Z");
            position = new GlobalBlockPosition(lodestoneDimension, x, y, z);
        }
        data.set(StructuredDataKey.LODESTONE_TRACKER, new LodestoneTracker(position, tracked));
    }

    private FireworkExplosion readExplosion(final CompoundTag tag) {
        final int shape = tag.getInt("Type");
        final IntArrayTag colors = tag.getIntArrayTag("Colors");
        final IntArrayTag fadeColors = tag.getIntArrayTag("FadeColors");
        final boolean trail = tag.getBoolean("Trail");
        final boolean flicker = tag.getBoolean("Flicker");
        return new FireworkExplosion(
            shape,
            colors != null ? colors.getValue() : new int[0],
            fadeColors != null ? fadeColors.getValue() : new int[0],
            trail,
            flicker
        );
    }

    private void updateWritableBookPages(final StructuredDataContainer data, final CompoundTag tag) {
        final ListTag<StringTag> pagesTag = tag.getListTag("pages", StringTag.class);
        final CompoundTag filteredPagesTag = tag.getCompoundTag("filtered_pages");
        if (pagesTag == null) {
            return;
        }

        final List<FilterableString> pages = new ArrayList<>();
        for (int i = 0; i < pagesTag.size(); i++) {
            final StringTag page = pagesTag.get(i);
            String filtered = null;
            if (filteredPagesTag != null) {
                final StringTag filteredPage = filteredPagesTag.getStringTag(String.valueOf(i));
                if (filteredPage != null) {
                    filtered = limit(filteredPage.getValue(), 1024);
                }
            }
            pages.add(new FilterableString(limit(page.getValue(), 1024), filtered));

            if (pages.size() == 100) {
                // Network limit
                break;
            }
        }
        data.set(StructuredDataKey.WRITABLE_BOOK_CONTENT, new WritableBook(pages.toArray(new FilterableString[0])));
    }

    private void updateWrittenBookPages(final UserConnection connection, final StructuredDataContainer data, final CompoundTag tag) {
        final String title = tag.getString("title");
        final String author = tag.getString("author");
        final ListTag<StringTag> pagesTag = tag.getListTag("pages", StringTag.class);

        boolean valid = author != null && title != null && title.length() <= 32 && pagesTag != null;
        if (valid) {
            for (final StringTag page : pagesTag) {
                if (page.getValue().length() > Short.MAX_VALUE) {
                    valid = false;
                    break;
                }
            }
        }

        final List<FilterableComponent> pages = new ArrayList<>();
        if (valid) {
            final CompoundTag filteredPagesTag = tag.getCompoundTag("filtered_pages");

            for (int i = 0; i < pagesTag.size(); i++) {
                final StringTag page = pagesTag.get(i);
                Tag filtered = null;
                if (filteredPagesTag != null) {
                    final StringTag filteredPage = filteredPagesTag.getStringTag(String.valueOf(i));
                    if (filteredPage != null) {
                        try {
                            filtered = jsonToTag(connection, filteredPage);
                        } catch (final Exception e) {
                            // A 1.20.4 client would display the broken json raw, but a 1.20.5 client would die
                            continue;
                        }
                    }
                }

                final Tag parsedPage;
                try {
                    parsedPage = jsonToTag(connection, page);
                } catch (final Exception e) {
                    // Same as above
                    continue;
                }

                pages.add(new FilterableComponent(parsedPage, filtered));
            }
        } else {
            final CompoundTag invalidPage = new CompoundTag();
            invalidPage.putString("text", "* Invalid book tag *");
            invalidPage.putString("color", "#AA0000"); // dark red

            pages.add(new FilterableComponent(invalidPage, null));
        }

        final String filteredTitle = tag.getString("filtered_title"); // Nullable
        final int generation = tag.getInt("generation");
        final boolean resolved = tag.getBoolean("resolved");
        final WrittenBook writtenBook = new WrittenBook(
            new FilterableString(limit(title == null ? "" : title, 32), limit(filteredTitle, 32)),
            author == null ? "" : author,
            clamp(generation, 0, 3),
            pages.toArray(new FilterableComponent[0]),
            resolved
        );
        data.set(StructuredDataKey.WRITTEN_BOOK_CONTENT, writtenBook);
    }

    private Tag jsonToTag(final UserConnection connection, final StringTag stringTag) {
        // Use the same version for deserializing and serializing, as the only write changes are in hovers, which we handle ourselves
        final Tag tag = ComponentUtil.jsonStringToTag(stringTag.getValue(), SerializerVersion.V1_20_3, SerializerVersion.V1_20_3);
        protocol.getComponentRewriter().processTag(connection, tag);
        return tag;
    }

    private void updateItemList(final UserConnection connection, final StructuredDataContainer data, final CompoundTag tag,
                                final String key, final StructuredDataKey<Item[]> dataKey) {
        final ListTag<CompoundTag> itemsTag = tag.getListTag(key, CompoundTag.class);
        if (itemsTag != null) {
            final Item[] items = itemsTag.stream()
                .limit(256)
                .map(item -> itemFromTag(connection, item))
                .filter(item -> !item.isEmpty())
                .toArray(Item[]::new);
            data.set(dataKey, items);
        }
    }

    private Item itemFromTag(final UserConnection connection, final CompoundTag item) {
        final String id = item.getString("id");
        if (id == null) {
            return StructuredItem.empty();
        }

        final int itemId = StructuredDataConverter.removeItemBackupTag(item, unmappedItemId(id));
        if (itemId == -1) {
            return StructuredItem.empty();
        }

        final byte count = item.getByte("Count", (byte) 1);
        final CompoundTag tag = item.getCompoundTag("tag");
        return handleItemToClient(connection, new DataItem(itemId, count, tag));
    }

    private void updateEnchantments(final StructuredDataContainer data, final CompoundTag tag, final String key,
                                    final StructuredDataKey<Enchantments> newKey, final boolean show) {
        final ListTag<CompoundTag> enchantmentsTag = tag.getListTag(key, CompoundTag.class);
        if (enchantmentsTag == null) {
            return;
        }

        final Enchantments enchantments = new Enchantments(new Int2IntOpenHashMap(), show);
        for (final CompoundTag enchantment : enchantmentsTag) {
            String id = enchantment.getString("id");
            final NumberTag lvl = enchantment.getNumberTag("lvl");
            if (id == null || lvl == null) {
                continue;
            }

            if (Key.stripMinecraftNamespace(id).equals("sweeping")) {
                // Renamed
                id = Key.namespaced("sweeping_edge");
            }

            final int intId = Enchantments1_20_5.keyToId(id);
            if (intId == -1) {
                continue;
            }

            enchantments.enchantments().put(intId, clamp(lvl.asInt(), 0, 255));
        }

        data.set(newKey, enchantments);

        // Add glint if none of the enchantments were valid
        if (!enchantmentsTag.isEmpty() && enchantments.size() == 0) {
            data.set(StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
    }

    private void updateProfile(final StructuredDataContainer data, final Tag skullOwnerTag) {
        if (skullOwnerTag instanceof StringTag nameTag) {
            final String name = nameTag.getValue();
            if (isValidName(name)) {
                data.set(StructuredDataKey.PROFILE1_20_5, new GameProfile(name, null, EMPTY_PROPERTIES));
            }
        } else if (skullOwnerTag instanceof CompoundTag skullOwner) {
            String name = skullOwner.getString("Name", "");
            if (!isValidName(name)) {
                name = null;
            }

            final IntArrayTag idTag = skullOwner.getIntArrayTag("Id");
            UUID uuid = null;
            if (idTag != null) {
                uuid = UUIDUtil.fromIntArray(idTag.getValue());
            }

            final List<GameProfile.Property> properties = new ArrayList<>(1);
            final CompoundTag propertiesTag = skullOwner.getCompoundTag("Properties");
            if (propertiesTag != null) {
                updateProperties(propertiesTag, properties);
            }

            data.set(StructuredDataKey.PROFILE1_20_5, new GameProfile(name, uuid, properties.toArray(EMPTY_PROPERTIES)));
        }
    }

    private @Nullable String limit(@Nullable final String s, final int length) {
        if (s == null) {
            return null;
        }
        return s.length() > length ? s.substring(0, length) : s;
    }

    private void updateBees(final StructuredDataContainer data, final ListTag<CompoundTag> beesTag) {
        final Bee[] bees = beesTag.stream().map(bee -> {
            final CompoundTag entityData = bee.getCompoundTag("EntityData");
            if (entityData == null) {
                return null;
            }

            final int ticksInHive = bee.getInt("TicksInHive");
            final int minOccupationTicks = bee.getInt("MinOccupationTicks");

            return new Bee(entityData, ticksInHive, minOccupationTicks);
        }).filter(Objects::nonNull).toArray(Bee[]::new);

        data.set(StructuredDataKey.BEES1_20_5, bees);
    }

    private void updateProperties(final CompoundTag propertiesTag, final List<GameProfile.Property> properties) {
        for (final Map.Entry<String, Tag> entry : propertiesTag.entrySet()) {
            if (!(entry.getValue() instanceof ListTag<?> listTag)) {
                continue;
            }

            for (final Tag propertyTag : listTag) {
                if (!(propertyTag instanceof CompoundTag compoundTag)) {
                    continue;
                }

                final String value = compoundTag.getString("Value", "");
                final String signature = compoundTag.getString("Signature");
                properties.add(new GameProfile.Property(
                    limit(entry.getKey(), 64),
                    value,
                    limit(signature, 1024)
                ));

                if (properties.size() == 16) {
                    // Max 16 properties
                    return;
                }
            }
        }
    }

    private void updateMapDecorations(final StructuredDataContainer data, final ListTag<CompoundTag> decorationsTag) {
        final CompoundTag updatedDecorationsTag = new CompoundTag();
        for (final CompoundTag decorationTag : decorationsTag) {
            final String id = decorationTag.getString("id", "");
            final int type = decorationTag.getInt("type");
            final double x = decorationTag.getDouble("x");
            final double z = decorationTag.getDouble("z");
            final float rotation = decorationTag.getFloat("rot");

            final CompoundTag updatedDecorationTag = new CompoundTag();
            updatedDecorationTag.putString("type", MapDecorations1_20_5.idToKey(type));
            updatedDecorationTag.putDouble("x", x);
            updatedDecorationTag.putDouble("z", z);
            updatedDecorationTag.putFloat("rotation", rotation);
            updatedDecorationsTag.put(id, updatedDecorationTag);
        }

        data.set(StructuredDataKey.MAP_DECORATIONS, updatedDecorationsTag);
    }

    private void updateDisplay(final UserConnection connection, final StructuredDataContainer data, final CompoundTag displayTag, final int hideFlags) {
        if (displayTag == null) {
            return;
        }

        final NumberTag mapColorTag = displayTag.getNumberTag("MapColor");
        if (mapColorTag != null) {
            data.set(StructuredDataKey.MAP_COLOR, mapColorTag.asInt());
        }

        final StringTag nameTag = displayTag.getStringTag("Name");
        if (nameTag != null) {
            try {
                final Tag convertedName = jsonToTag(connection, nameTag);
                data.set(StructuredDataKey.CUSTOM_NAME, convertedName);
            } catch (final Exception ignored) {
                // No display name if it fails to parse
            }
        }

        final ListTag<StringTag> loreTag = displayTag.getListTag("Lore", StringTag.class);
        if (loreTag != null) {
            // Apply limit as per new network codec. Some servers send these lores to do trickery with shaders
            try {
                data.set(StructuredDataKey.LORE, loreTag.stream().limit(256).map(t -> jsonToTag(connection, t)).toArray(Tag[]::new));
            } catch (final Exception ignored) {
                // No lore if any one of them fail to parse
            }
        }

        final NumberTag colorTag = displayTag.getNumberTag("color");
        if (colorTag != null) {
            data.set(StructuredDataKey.DYED_COLOR1_20_5, new DyedColor(colorTag.asInt(), (hideFlags & StructuredDataConverter.HIDE_DYE_COLOR) == 0));
        }
    }

    private void addBlockEntityId(final CompoundTag tag, final String id) {
        if (!tag.contains("id")) {
            tag.putString("id", id);
        }
    }

    private boolean isUnknownBlockEntity(final int id) {
        return id < 0 || id > 42;
    }

    private void updateBlockEntityTag(final UserConnection connection, @Nullable final StructuredDataContainer data, final CompoundTag tag) {
        if (tag == null) {
            return;
        }

        if (data != null) {
            final StringTag lockTag = tag.getStringTag("Lock");
            if (lockTag != null) {
                data.set(StructuredDataKey.LOCK1_20_5, lockTag);
            }

            final ListTag<CompoundTag> beesTag = tag.getListTag("Bees", CompoundTag.class);
            if (beesTag != null) {
                updateBees(data, beesTag);
                addBlockEntityId(tag, "beehive");
            }

            final ListTag<StringTag> sherdsTag = tag.getListTag("sherds", StringTag.class);
            if (sherdsTag != null && sherdsTag.size() == 4) {
                final String backSherd = sherdsTag.get(0).getValue();
                final String leftSherd = sherdsTag.get(1).getValue();
                final String rightSherd = sherdsTag.get(2).getValue();
                final String frontSherd = sherdsTag.get(3).getValue();

                data.set(StructuredDataKey.POT_DECORATIONS, new PotDecorations(
                    toMappedItemId(backSherd),
                    toMappedItemId(leftSherd),
                    toMappedItemId(rightSherd),
                    toMappedItemId(frontSherd)
                ));
                addBlockEntityId(tag, "decorated_pot");
            }

            final StringTag noteBlockSoundTag = tag.getStringTag("note_block_sound");
            if (noteBlockSoundTag != null) {
                data.set(StructuredDataKey.NOTE_BLOCK_SOUND, Key.of(noteBlockSoundTag.getValue()));
                addBlockEntityId(tag, "player_head");
            }

            final StringTag lootTableTag = tag.getStringTag("LootTable");
            if (lootTableTag != null) {
                final long lootTableSeed = tag.getLong("LootTableSeed");

                final CompoundTag containerLoot = new CompoundTag();
                containerLoot.putString("loot_table", lootTableTag.getValue());
                containerLoot.putLong("loot_table_seed", lootTableSeed);
                data.set(StructuredDataKey.CONTAINER_LOOT, containerLoot);
            }

            final Tag baseColorTag = tag.remove("Base");
            if (baseColorTag instanceof NumberTag baseColorIntTag) {
                data.set(StructuredDataKey.BASE_COLOR, baseColorIntTag.asInt());
            }

            final ListTag<CompoundTag> itemsTag = tag.getListTag("Items", CompoundTag.class);
            if (itemsTag != null) {
                int highestSlot = 0;

                for (int i = 0, size = Math.min(itemsTag.size(), 256); i < size; i++) {
                    final CompoundTag itemTag = itemsTag.get(i);
                    final Item item = itemFromTag(connection, itemTag);
                    if (item.isEmpty()) {
                        continue;
                    }

                    final int slot = itemTag.getByte("Slot");
                    highestSlot = MathUtil.clamp(slot, highestSlot, 255);
                }

                final Item[] filteredItems = new Item[highestSlot + 1];
                Arrays.fill(filteredItems, StructuredItem.empty());
                for (final CompoundTag itemTag : itemsTag) {
                    final Item item = itemFromTag(connection, itemTag);
                    if (item.isEmpty()) {
                        continue;
                    }

                    final int slot = itemTag.getByte("Slot");
                    if (slot >= 0 && slot < filteredItems.length) {
                        filteredItems[slot] = item;
                    }
                }

                data.set(StructuredDataKey.V1_20_5.container, filteredItems);
                addBlockEntityId(tag, "shulker_box");
            }
        }

        final Tag skullOwnerTag = tag.remove("SkullOwner");
        if (skullOwnerTag instanceof StringTag nameTag) {
            final CompoundTag profileTag = new CompoundTag();
            profileTag.putString("name", nameTag.getValue());
            tag.put("profile", profileTag);
        } else if (skullOwnerTag instanceof CompoundTag skullOwnerCompoundTag) {
            updateSkullOwnerTag(tag, skullOwnerCompoundTag);
        }

        final ListTag<CompoundTag> patternsTag = tag.getListTag("Patterns", CompoundTag.class);
        if (patternsTag != null) {
            final BannerPatternStorage patternStorage = connection.get(BannerPatternStorage.class);
            final BannerPatternLayer[] layers = patternsTag.stream().map(patternTag -> {
                final String pattern = patternTag.getString("Pattern", "");
                final int color = patternTag.getInt("Color", -1);
                final String fullPatternIdentifier = BannerPatterns1_20_5.compactToFullId(pattern);
                if (fullPatternIdentifier == null || color == -1) {
                    return null;
                }

                patternTag.remove("Pattern");
                patternTag.remove("Color");
                patternTag.putString("pattern", fullPatternIdentifier);
                patternTag.putString("color", DyeColors.idToKey(color));

                final int id = patternStorage != null ? patternStorage.bannerPatterns().keyToId(fullPatternIdentifier) : BannerPatterns1_20_5.keyToId(fullPatternIdentifier);
                return id != -1 ? new BannerPatternLayer(Holder.of(id), color) : null;
            }).filter(Objects::nonNull).toArray(BannerPatternLayer[]::new);
            tag.remove("Patterns");
            tag.put("patterns", patternsTag);
            addBlockEntityId(tag, "banner");

            if (data != null) {
                data.set(StructuredDataKey.BANNER_PATTERNS, layers);
            }
        }

        // Remove air item from brushable blocks, decorated pots, jukeboxes, and lecterns
        removeEmptyItem(tag, "item");
        removeEmptyItem(tag, "RecordItem");
        removeEmptyItem(tag, "Book");
    }

    private void removeEmptyItem(final CompoundTag tag, final String key) {
        final CompoundTag itemTag = tag.getCompoundTag(key);
        if (itemTag != null) {
            final int id = itemTag.getInt("id");
            if (id == 0) {
                tag.remove(key);
            }
        }
    }

    private void updateSkullOwnerTag(final CompoundTag tag, final CompoundTag skullOwnerTag) {
        final CompoundTag profileTag = new CompoundTag();
        tag.put("profile", profileTag);

        final String name = skullOwnerTag.getString("Name");
        if (name != null && isValidName(name)) {
            profileTag.putString("name", name);
        }

        final IntArrayTag idTag = skullOwnerTag.getIntArrayTag("Id");
        if (idTag != null) {
            profileTag.put("id", idTag);
        }

        if (!(skullOwnerTag.remove("Properties") instanceof CompoundTag propertiesTag)) {
            return;
        }

        final ListTag<CompoundTag> propertiesListTag = new ListTag<>(CompoundTag.class);
        for (final Map.Entry<String, Tag> entry : propertiesTag.entrySet()) {
            if (!(entry.getValue() instanceof ListTag<?> entryValue)) {
                continue;
            }

            for (final Tag propertyTag : entryValue) {
                if (!(propertyTag instanceof CompoundTag propertyCompoundTag)) {
                    continue;
                }

                final CompoundTag updatedPropertyTag = new CompoundTag();
                final String value = propertyCompoundTag.getString("Value", "");
                final String signature = propertyCompoundTag.getString("Signature");
                updatedPropertyTag.putString("name", entry.getKey());
                updatedPropertyTag.putString("value", value);
                if (signature != null) {
                    updatedPropertyTag.putString("signature", signature);
                }
                propertiesListTag.add(updatedPropertyTag);
            }
        }

        profileTag.put("properties", propertiesListTag);
    }

    private boolean isValidName(final String name) {
        // Ignore invalid player profile names
        if (name.length() > 16) {
            return false;
        }

        for (int i = 0, len = name.length(); i < len; ++i) {
            final char c = name.charAt(i);
            if (c < '!' || c > '~') {
                return false;
            }
        }

        return true;
    }
}
