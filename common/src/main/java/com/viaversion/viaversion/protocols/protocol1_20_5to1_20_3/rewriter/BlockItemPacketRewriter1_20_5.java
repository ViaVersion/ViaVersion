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
package com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.rewriter;

import com.github.steveice10.opennbt.stringified.SNBT;
import com.github.steveice10.opennbt.tag.builtin.ByteTag;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntArrayTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.NumberTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.ParticleMappings;
import com.viaversion.viaversion.api.minecraft.GameProfile;
import com.viaversion.viaversion.api.minecraft.GlobalPosition;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.Particle;
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
import com.viaversion.viaversion.api.minecraft.item.data.AttributeModifier;
import com.viaversion.viaversion.api.minecraft.item.data.AttributeModifiers;
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
import com.viaversion.viaversion.api.minecraft.item.data.ModifierData;
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
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.api.type.types.version.Types1_20_3;
import com.viaversion.viaversion.api.type.types.version.Types1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPacket1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.rewriter.RecipeRewriter1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.Protocol1_20_5To1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data.Attributes1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data.BannerPatterns1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data.DyeColors;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data.Enchantments1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data.EquipmentSlots1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data.Instruments1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data.MapDecorations1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data.PotionEffects1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data.Potions1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data.TrimMaterials1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data.TrimPatterns1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.packet.ServerboundPacket1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.Either;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.SerializerVersion;
import com.viaversion.viaversion.util.UUIDUtil;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.viaversion.viaversion.util.MathUtil.clamp;

// 1.20.3 nbt -> 1.20.5 data component conversion
public final class BlockItemPacketRewriter1_20_5 extends ItemRewriter<ClientboundPacket1_20_3, ServerboundPacket1_20_5, Protocol1_20_5To1_20_3> {

    public static final String[] MOB_TAGS = {"NoAI", "Silent", "NoGravity", "Glowing", "Invulnerable", "Health", "Age", "Variant", "HuntingCooldown", "BucketVariantTag"};
    public static final String[] ATTRIBUTE_OPERATIONS = {"add_value", "add_multiplied_base", "add_multiplied_total"};
    private static final StructuredDataConverter DATA_CONVERTER = new StructuredDataConverter(false);
    private static final GameProfile.Property[] EMPTY_PROPERTIES = new GameProfile.Property[0];
    private static final StatePropertyMatcher[] EMPTY_PROPERTY_MATCHERS = new StatePropertyMatcher[0];

    public BlockItemPacketRewriter1_20_5(final Protocol1_20_5To1_20_3 protocol) {
        super(protocol, Type.ITEM1_20_2, Type.ITEM1_20_2_ARRAY, Types1_20_5.ITEM, Types1_20_5.ITEM_ARRAY);
    }

    @Override
    public void registerPackets() {
        final BlockRewriter<ClientboundPacket1_20_3> blockRewriter = BlockRewriter.for1_20_2(protocol);
        blockRewriter.registerBlockAction(ClientboundPackets1_20_3.BLOCK_ACTION);
        blockRewriter.registerBlockChange(ClientboundPackets1_20_3.BLOCK_CHANGE);
        blockRewriter.registerVarLongMultiBlockChange1_20(ClientboundPackets1_20_3.MULTI_BLOCK_CHANGE);
        blockRewriter.registerEffect(ClientboundPackets1_20_3.EFFECT, 1010, 2001);
        blockRewriter.registerChunkData1_19(ClientboundPackets1_20_3.CHUNK_DATA, ChunkType1_20_2::new, (user, blockEntity) -> updateBlockEntityTag(user, null, blockEntity.tag()));
        protocol.registerClientbound(ClientboundPackets1_20_3.BLOCK_ENTITY_DATA, wrapper -> {
            wrapper.passthrough(Type.POSITION1_14); // Position
            wrapper.passthrough(Type.VAR_INT); // Block entity type

            CompoundTag tag = wrapper.read(Type.COMPOUND_TAG);
            if (tag != null) {
                updateBlockEntityTag(wrapper.user(), null, tag);
            } else {
                // No longer nullable
                tag = new CompoundTag();
            }
            wrapper.write(Type.COMPOUND_TAG, tag);
        });

        registerSetCooldown(ClientboundPackets1_20_3.COOLDOWN);
        registerWindowItems1_17_1(ClientboundPackets1_20_3.WINDOW_ITEMS);
        registerSetSlot1_17_1(ClientboundPackets1_20_3.SET_SLOT);
        registerEntityEquipmentArray(ClientboundPackets1_20_3.ENTITY_EQUIPMENT);
        registerClickWindow1_17_1(ServerboundPackets1_20_5.CLICK_WINDOW);
        registerWindowPropertyEnchantmentHandler(ClientboundPackets1_20_3.WINDOW_PROPERTY);
        registerCreativeInvAction(ServerboundPackets1_20_5.CREATIVE_INVENTORY_ACTION);
        protocol.registerServerbound(ServerboundPackets1_20_5.CLICK_WINDOW_BUTTON, wrapper -> {
            final byte containerId = wrapper.read(Type.VAR_INT).byteValue();
            final byte buttonId = wrapper.read(Type.VAR_INT).byteValue();
            wrapper.write(Type.BYTE, containerId);
            wrapper.write(Type.BYTE, buttonId);
        });

        protocol.registerClientbound(ClientboundPackets1_20_3.ADVANCEMENTS, wrapper -> {
            wrapper.passthrough(Type.BOOLEAN); // Reset/clear
            int size = wrapper.passthrough(Type.VAR_INT); // Mapping size
            for (int i = 0; i < size; i++) {
                wrapper.passthrough(Type.STRING); // Identifier
                wrapper.passthrough(Type.OPTIONAL_STRING); // Parent

                // Display data
                if (wrapper.passthrough(Type.BOOLEAN)) {
                    wrapper.passthrough(Type.TAG); // Title
                    wrapper.passthrough(Type.TAG); // Description

                    Item item = handleNonNullItemToClient(wrapper.user(), wrapper.read(itemType()));
                    wrapper.write(mappedItemType(), item);

                    wrapper.passthrough(Type.VAR_INT); // Frame type
                    int flags = wrapper.passthrough(Type.INT); // Flags
                    if ((flags & 1) != 0) {
                        wrapper.passthrough(Type.STRING); // Background texture
                    }
                    wrapper.passthrough(Type.FLOAT); // X
                    wrapper.passthrough(Type.FLOAT); // Y
                }

                int requirements = wrapper.passthrough(Type.VAR_INT);
                for (int array = 0; array < requirements; array++) {
                    wrapper.passthrough(Type.STRING_ARRAY);
                }

                wrapper.passthrough(Type.BOOLEAN); // Send telemetry
            }
        });

        protocol.registerClientbound(ClientboundPackets1_20_3.SPAWN_PARTICLE, wrapper -> {
            final int particleId = wrapper.read(Type.VAR_INT);

            wrapper.passthrough(Type.BOOLEAN); // Long Distance
            wrapper.passthrough(Type.DOUBLE); // X
            wrapper.passthrough(Type.DOUBLE); // Y
            wrapper.passthrough(Type.DOUBLE); // Z
            wrapper.passthrough(Type.FLOAT); // Offset X
            wrapper.passthrough(Type.FLOAT); // Offset Y
            wrapper.passthrough(Type.FLOAT); // Offset Z
            final float data = wrapper.passthrough(Type.FLOAT);
            wrapper.passthrough(Type.INT); // Particle Count

            // Read data and add it to Particle
            final ParticleMappings mappings = protocol.getMappingData().getParticleMappings();
            final int mappedId = mappings.getNewId(particleId);
            final Particle particle = new Particle(mappedId);
            if (mappedId == mappings.mappedId("entity_effect")) {
                particle.add(Type.INT, data != 0 ? ThreadLocalRandom.current().nextInt() : 0); // rgb
            } else if (particleId == mappings.id("dust_color_transition")) {
                for (int i = 0; i < 7; i++) {
                    particle.add(Type.FLOAT, wrapper.read(Type.FLOAT));
                }
                // fromColor, scale, toColor -> fromColor, toColor, scale
                particle.add(Type.FLOAT, particle.<Float>removeArgument(3).getValue());
            } else if (mappings.isBlockParticle(particleId)) {
                final int blockStateId = wrapper.read(Type.VAR_INT);
                particle.add(Type.VAR_INT, protocol.getMappingData().getNewBlockStateId(blockStateId));
            } else if (mappings.isItemParticle(particleId)) {
                final Item item = handleNonNullItemToClient(wrapper.user(), wrapper.read(Type.ITEM1_20_2));
                particle.add(Types1_20_5.ITEM, item);
            } else if (particleId == mappings.id("dust")) {
                // R, g, b, scale
                for (int i = 0; i < 4; i++) {
                    particle.add(Type.FLOAT, wrapper.read(Type.FLOAT));
                }
            } else if (particleId == mappings.id("vibration")) {
                final int sourceTypeId = wrapper.read(Type.VAR_INT);
                particle.add(Type.VAR_INT, sourceTypeId);
                if (sourceTypeId == 0) { // Block
                    particle.add(Type.POSITION1_14, wrapper.read(Type.POSITION1_14)); // Target block pos
                } else if (sourceTypeId == 1) { // Entity
                    particle.add(Type.VAR_INT, wrapper.read(Type.VAR_INT)); // Target entity
                    particle.add(Type.FLOAT, wrapper.read(Type.FLOAT)); // Y offset
                } else {
                    Via.getPlatform().getLogger().warning("Unknown vibration path position source type: " + sourceTypeId);
                }
                particle.add(Type.VAR_INT, wrapper.read(Type.VAR_INT)); // Arrival in ticks
            } else if (particleId == mappings.id("sculk_charge")) {
                particle.add(Type.FLOAT, wrapper.read(Type.FLOAT)); // Roll
            } else if (particleId == mappings.id("shriek")) {
                particle.add(Type.VAR_INT, wrapper.read(Type.VAR_INT)); // Delay
            }

            wrapper.write(Types1_20_5.PARTICLE, particle);
        });

        protocol.registerClientbound(ClientboundPackets1_20_3.EXPLOSION, wrapper -> {
            wrapper.passthrough(Type.DOUBLE); // X
            wrapper.passthrough(Type.DOUBLE); // Y
            wrapper.passthrough(Type.DOUBLE); // Z
            wrapper.passthrough(Type.FLOAT); // Power
            final int blocks = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < blocks; i++) {
                wrapper.passthrough(Type.BYTE); // Relative X
                wrapper.passthrough(Type.BYTE); // Relative Y
                wrapper.passthrough(Type.BYTE); // Relative Z
            }
            wrapper.passthrough(Type.FLOAT); // Knockback X
            wrapper.passthrough(Type.FLOAT); // Knockback Y
            wrapper.passthrough(Type.FLOAT); // Knockback Z
            wrapper.passthrough(Type.VAR_INT); // Block interaction type

            protocol.getEntityRewriter().rewriteParticle(wrapper, Types1_20_3.PARTICLE, Types1_20_5.PARTICLE); // Small explosion particle
            protocol.getEntityRewriter().rewriteParticle(wrapper, Types1_20_3.PARTICLE, Types1_20_5.PARTICLE); // Large explosion particle

            wrapper.write(Type.VAR_INT, 0); // "Empty" registry id to instead use the resource location that follows after
        });

        protocol.registerClientbound(ClientboundPackets1_20_3.TRADE_LIST, wrapper -> {
            wrapper.passthrough(Type.VAR_INT); // Container id
            final int size = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < size; i++) {
                final Item input = handleItemToClient(wrapper.user(), wrapper.read(Type.ITEM1_20_2));
                wrapper.write(Types1_20_5.ITEM_COST, input);

                final Item output = handleNonNullItemToClient(wrapper.user(), wrapper.read(Type.ITEM1_20_2));
                wrapper.write(Types1_20_5.ITEM, output);

                final Item secondInput = handleItemToClient(wrapper.user(), wrapper.read(Type.ITEM1_20_2));
                wrapper.write(Types1_20_5.OPTIONAL_ITEM_COST, secondInput);

                wrapper.passthrough(Type.BOOLEAN); // Out of stock
                wrapper.passthrough(Type.INT); // Number of trade uses
                wrapper.passthrough(Type.INT); // Maximum number of trade uses
                wrapper.passthrough(Type.INT); // XP
                wrapper.passthrough(Type.INT); // Special price
                wrapper.passthrough(Type.FLOAT); // Price multiplier
                wrapper.passthrough(Type.INT); // Demand
            }
        });

        final RecipeRewriter1_20_3<ClientboundPacket1_20_3> recipeRewriter = new RecipeRewriter1_20_3<ClientboundPacket1_20_3>(protocol) {
            @Override
            protected Item rewrite(final UserConnection connection, @Nullable Item item) {
                item = super.rewrite(connection, item);
                if (item == null || item.isEmpty()) {
                    // Does not allow empty items
                    return new StructuredItem(1, 1);
                }
                return item;
            }
        };
        protocol.registerClientbound(ClientboundPackets1_20_3.DECLARE_RECIPES, wrapper -> {
            final int size = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < size; i++) {
                // Change order and write the type as an int
                final String type = wrapper.read(Type.STRING);
                wrapper.passthrough(Type.STRING); // Recipe Identifier

                wrapper.write(Type.VAR_INT, protocol.getMappingData().getRecipeSerializerMappings().mappedId(type));
                recipeRewriter.handleRecipeType(wrapper, Key.stripMinecraftNamespace(type));
            }
        });
    }

    public Item handleNonNullItemToClient(final UserConnection connection, @Nullable Item item) {
        item = handleItemToClient(connection, item);
        // Items are no longer nullable in a few places
        if (item == null || item.isEmpty()) {
            return new StructuredItem(1, 1);
        }
        return item;
    }

    @Override
    public @Nullable Item handleItemToClient(final UserConnection connection, @Nullable final Item item) {
        if (item == null) return null;

        // Add the original as custom data, to be re-used for creative clients as well
        final CompoundTag tag = item.tag();
        if (tag != null) {
            tag.putBoolean(nbtTagName(), true);
        }

        final Item structuredItem = toStructuredItem(connection, item);
        return super.handleItemToClient(connection, structuredItem);
    }

    @Override
    public @Nullable Item handleItemToServer(UserConnection connection, @Nullable final Item item) {
        if (item == null) return null;

        super.handleItemToServer(connection, item);
        return toOldItem(item, DATA_CONVERTER);
    }

    public Item toOldItem(final Item item, final StructuredDataConverter dataConverter) {
        // Start out with custom data and add the rest on top, or short-curcuit with the original item
        final StructuredDataContainer data = item.structuredData();
        data.setIdLookup(protocol, true);

        final StructuredData<CompoundTag> customData = data.getNonEmpty(StructuredDataKey.CUSTOM_DATA);
        final CompoundTag tag = customData != null ? customData.value() : new CompoundTag();
        final DataItem dataItem = new DataItem(item.identifier(), (byte) item.amount(), (short) 0, tag);
        if (customData != null && tag.remove(nbtTagName()) != null) {
            return dataItem;
        }

        for (final StructuredData<?> structuredData : data.data().values()) {
            dataConverter.writeToTag(structuredData, tag);
        }

        return dataItem;
    }

    public Item toStructuredItem(final UserConnection connection, final Item old) {
        final CompoundTag tag = old.tag();
        final StructuredItem item = new StructuredItem(old.identifier(), (byte) old.amount(), new StructuredDataContainer());
        final StructuredDataContainer data = item.structuredData();
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
        if (damage != null && damage.asInt() != 0) {
            data.set(StructuredDataKey.DAMAGE, damage.asInt());
        }

        final NumberTag repairCost = tag.getNumberTag("RepairCost");
        if (repairCost != null && repairCost.asInt() != 0) {
            data.set(StructuredDataKey.REPAIR_COST, repairCost.asInt());
        }

        final NumberTag customModelData = tag.getNumberTag("CustomModelData");
        if (customModelData != null) {
            data.set(StructuredDataKey.CUSTOM_MODEL_DATA, customModelData.asInt());
        }

        final CompoundTag blockState = tag.getCompoundTag("BlockStateTag");
        if (blockState != null) {
            updateBlockState(data, blockState);
        }

        final CompoundTag entityTag = tag.getCompoundTag("EntityTag");
        if (entityTag != null) {
            data.set(StructuredDataKey.ENTITY_DATA, entityTag.copy());
        }

        final CompoundTag blockEntityTag = tag.getCompoundTag("BlockEntityTag");
        if (blockEntityTag != null) {
            final CompoundTag clonedTag = blockEntityTag.copy();
            updateBlockEntityTag(connection, data, clonedTag);
            item.structuredData().set(StructuredDataKey.BLOCK_ENTITY_DATA, clonedTag);
        }

        final CompoundTag debugProperty = tag.getCompoundTag("DebugProperty");
        if (debugProperty != null) {
            data.set(StructuredDataKey.DEBUG_STICK_STATE, debugProperty.copy());
        }

        final NumberTag unbreakable = tag.getNumberTag("Unbreakable");
        if (unbreakable != null && unbreakable.asBoolean()) {
            data.set(StructuredDataKey.UNBREAKABLE, new Unbreakable((hideFlagsValue & StructuredDataConverter.HIDE_UNBREAKABLE) == 0));
        }

        final CompoundTag trimTag = tag.getCompoundTag("Trim");
        if (trimTag != null) {
            updateArmorTrim(data, trimTag, (hideFlagsValue & StructuredDataConverter.HIDE_ARMOR_TRIM) == 0);
        }

        final CompoundTag explosionTag = tag.getCompoundTag("Explosion");
        if (explosionTag != null) {
            data.set(StructuredDataKey.FIREWORK_EXPLOSION, readExplosion(explosionTag));
        }

        final ListTag<StringTag> recipesTag = tag.getListTag("Recipes", StringTag.class);
        if (recipesTag != null) {
            data.set(StructuredDataKey.RECIPES, recipesTag);
        }

        final CompoundTag lodestonePosTag = tag.getCompoundTag("LodestonePos");
        final String lodestoneDimension = tag.getString("LodestoneDimension");
        if (lodestonePosTag != null && lodestoneDimension != null) {
            updateLodestoneTracker(tag, lodestonePosTag, lodestoneDimension, data);
        }

        final ListTag<CompoundTag> effectsTag = tag.getListTag("effects", CompoundTag.class);
        if (effectsTag != null) {
            updateEffects(effectsTag, data);
        }

        final String instrument = tag.getString("instrument");
        if (instrument != null) {
            final int id = Instruments1_20_3.keyToId(instrument);
            if (id != -1) {
                data.set(StructuredDataKey.INSTRUMENT, Holder.of(id));
            }
        }

        final ListTag<CompoundTag> attributeModifiersTag = tag.getListTag("AttributeModifiers", CompoundTag.class);
        final boolean showAttributes = (hideFlagsValue & StructuredDataConverter.HIDE_ATTRIBUTES) == 0;
        if (attributeModifiersTag != null) {
            updateAttributes(data, attributeModifiersTag, showAttributes);
        } else if (!showAttributes) {
            data.set(StructuredDataKey.ATTRIBUTE_MODIFIERS, new AttributeModifiers(new AttributeModifier[0], false));
        }

        final CompoundTag fireworksTag = tag.getCompoundTag("Fireworks");
        if (fireworksTag != null) {
            final ListTag<CompoundTag> explosionsTag = fireworksTag.getListTag("Explosions", CompoundTag.class);
            if (explosionsTag != null) {
                updateFireworks(data, fireworksTag, explosionsTag);
            }
        }

        if (old.identifier() == 1085) {
            updateWritableBookPages(data, tag);
        } else if (old.identifier() == 1086) {
            updateWrittenBookPages(connection, data, tag);
        }

        updatePotionTags(data, tag);

        updateMobTags(data, tag);

        updateItemList(connection, data, tag, "ChargedProjectiles", StructuredDataKey.CHARGED_PROJECTILES, false);
        if (old.identifier() == 927) {
            updateItemList(connection, data, tag, "Items", StructuredDataKey.BUNDLE_CONTENTS, false);
        }

        updateEnchantments(data, tag, "Enchantments", StructuredDataKey.ENCHANTMENTS, (hideFlagsValue & StructuredDataConverter.HIDE_ENCHANTMENTS) == 0);
        updateEnchantments(data, tag, "StoredEnchantments", StructuredDataKey.STORED_ENCHANTMENTS, (hideFlagsValue & StructuredDataConverter.HIDE_ADDITIONAL) == 0);

        final NumberTag mapId = tag.getNumberTag("map");
        if (mapId != null) {
            data.set(StructuredDataKey.MAP_ID, mapId.asInt());
        }

        final ListTag<CompoundTag> decorationsTag = tag.getListTag("Decorations", CompoundTag.class);
        if (decorationsTag != null) {
            updateMapDecorations(data, decorationsTag);
        }

        updateProfile(data, tag.get("SkullOwner"));

        final CompoundTag customCreativeLock = tag.getCompoundTag("CustomCreativeLock");
        if (customCreativeLock != null) {
            data.set(StructuredDataKey.CREATIVE_SLOT_LOCK);
        }

        final ListTag<StringTag> canPlaceOnTag = tag.getListTag("CanPlaceOn", StringTag.class);
        if (canPlaceOnTag != null) {
            data.set(StructuredDataKey.CAN_PLACE_ON, updateBlockPredicates(canPlaceOnTag, (hideFlagsValue & StructuredDataConverter.HIDE_CAN_PLACE_ON) == 0));
        }

        final ListTag<StringTag> canDestroyTag = tag.getListTag("CanDestroy", StringTag.class);
        if (canDestroyTag != null) {
            data.set(StructuredDataKey.CAN_BREAK, updateBlockPredicates(canDestroyTag, (hideFlagsValue & StructuredDataConverter.HIDE_CAN_DESTROY) == 0));
        }

        final IntTag mapScaleDirectionTag = tag.getIntTag("map_scale_direction");
        if (mapScaleDirectionTag != null) {
            data.set(StructuredDataKey.MAP_POST_PROCESSING, 1); // Scale
        } else {
            final NumberTag mapToLockTag = tag.getNumberTag("map_to_lock");
            if (mapToLockTag != null) {
                data.set(StructuredDataKey.MAP_POST_PROCESSING, 0); // Lock
            }
        }

        final CompoundTag backupTag = StructuredDataConverter.removeBackupTag(tag);
        if (backupTag != null) {
            // Restore original data components
            restoreFromBackupTag(backupTag, data);
        }

        data.set(StructuredDataKey.CUSTOM_DATA, tag);
        return item;
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

        data.set(StructuredDataKey.INSTRUMENT, Holder.of(new Instrument(soundEvent, useDuration, range)));
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
        data.set(StructuredDataKey.FOOD, new FoodProperties(nutrition, saturation, canAlwaysEat, eatSeconds, possibleEffects.toArray(new FoodEffect[0])));
    }

    private void restoreToolFromBackup(final CompoundTag tool, final StructuredDataContainer data) {
        final ListTag<CompoundTag> rulesTag = tool.getListTag("rules", CompoundTag.class);
        if (rulesTag == null) {
            return;
        }

        final List<ToolRule> rules = new ArrayList<>();
        for (final CompoundTag tag : rulesTag) {
            HolderSet blocks = null;
            if (tag.get("blocks") instanceof StringTag) {
                blocks = HolderSet.of(tag.getString("blocks"));
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
        data.set(StructuredDataKey.TOOL, new ToolProperties(
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

    private AdventureModePredicate updateBlockPredicates(final ListTag<StringTag> tag, final boolean showInTooltip) {
        final BlockPredicate[] predicates = tag.stream()
            .map(StringTag::getValue)
            .map(this::deserializeBlockPredicate)
            .filter(Objects::nonNull)
            .toArray(BlockPredicate[]::new);
        return new AdventureModePredicate(predicates, showInTooltip);
    }

    private @Nullable BlockPredicate deserializeBlockPredicate(final String rawPredicate) {
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
            final int id = Protocol1_20_5To1_20_3.MAPPINGS.blockId(identifier);
            if (id == -1) {
                return null;
            }

            holders = HolderSet.of(new int[]{id});
        } else {
            holders = HolderSet.of(identifier.substring(1));
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
                tag = SNBT.deserializeCompoundTag(rawPredicate.substring(tagStartIndex, tagEndIndex + 1));
            } catch (final Exception e) {
                if (Via.getManager().isDebug()) {
                    Via.getPlatform().getLogger().log(Level.SEVERE, "Failed to parse block predicate tag: " + rawPredicate.substring(tagStartIndex, tagEndIndex + 1), e);
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
        data.set(StructuredDataKey.ATTRIBUTE_MODIFIERS, new AttributeModifiers(modifiers.toArray(new AttributeModifier[0]), showInTooltip));
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
            data.set(StructuredDataKey.POTION_CONTENTS, new PotionContents(
                potionId,
                customPotionColorTag != null ? customPotionColorTag.asInt() : null,
                potionEffects != null ? potionEffects : new PotionEffect[0]
            ));
        }
    }

    private void updateArmorTrim(final StructuredDataContainer data, final CompoundTag trimTag, final boolean showInTooltip) {
        final Tag materialTag = trimTag.get("material");
        final Holder<ArmorTrimMaterial> materialHolder;
        if (materialTag instanceof StringTag) {
            // Would technically have to be stored and retrieved from registry data, but that'd mean a lot of work
            final int id = TrimMaterials1_20_3.keyToId(((StringTag) materialTag).getValue());
            if (id == -1) {
                return;
            }

            materialHolder = Holder.of(id);
        } else if (materialTag instanceof CompoundTag) {
            final CompoundTag materialCompoundTag = (CompoundTag) materialTag;
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
            final CompoundTag overrideArmorMaterialsTag = materialCompoundTag.get("override_armor_materials");
            final Tag descriptionTag = materialCompoundTag.get("description");

            final Int2ObjectMap<String> overrideArmorMaterials = new Int2ObjectOpenHashMap<>();
            if (overrideArmorMaterialsTag != null) {
                for (final Map.Entry<String, Tag> entry : overrideArmorMaterialsTag.entrySet()) {
                    if (!(entry.getValue() instanceof StringTag)) {
                        continue;
                    }
                    try {
                        final int id = Integer.parseInt(entry.getKey());
                        overrideArmorMaterials.put(id, ((StringTag) entry.getValue()).getValue());
                    } catch (NumberFormatException ignored) {
                    }
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
        if (patternTag instanceof StringTag) {
            // Would technically have to be stored and retrieved from registry data, but that'd mean a lot of work
            final int id = TrimPatterns1_20_3.keyToId(((StringTag) patternTag).getValue());
            if (id == -1) {
                return;
            }
            patternHolder = Holder.of(id);
        } else if (patternTag instanceof CompoundTag) {
            final CompoundTag patternCompoundTag = (CompoundTag) patternTag;
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

        data.set(StructuredDataKey.TRIM, new ArmorTrim(materialHolder, patternHolder, showInTooltip));
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
            if (value instanceof StringTag) {
                properties.put(entry.getKey(), ((StringTag) value).getValue());
            } else if (value instanceof IntTag) {
                properties.put(entry.getKey(), Integer.toString(((NumberTag) value).asInt()));
            }
        }
        data.set(StructuredDataKey.BLOCK_STATE, new BlockStateProperties(properties));
    }

    private void updateFireworks(final StructuredDataContainer data, final CompoundTag fireworksTag, final ListTag<CompoundTag> explosionsTag) {
        final int flightDuration = fireworksTag.getInt("Flight");
        final Fireworks fireworks = new Fireworks(
            flightDuration,
            explosionsTag.stream().limit(256).map(this::readExplosion).toArray(FireworkExplosion[]::new)
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

    private void updateLodestoneTracker(final CompoundTag tag, final CompoundTag lodestonePosTag, final String lodestoneDimensionTag, final StructuredDataContainer data) {
        final boolean tracked = tag.getBoolean("LodestoneTracked");
        final int x = lodestonePosTag.getInt("X");
        final int y = lodestonePosTag.getInt("Y");
        final int z = lodestonePosTag.getInt("Z");
        final GlobalPosition position = new GlobalPosition(lodestoneDimensionTag, x, y, z);
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
        data.set(StructuredDataKey.WRITABLE_BOOK_CONTENT, pages.toArray(new FilterableString[0]));
    }

    private void updateWrittenBookPages(final UserConnection connection, final StructuredDataContainer data, final CompoundTag tag) {
        final ListTag<StringTag> pagesTag = tag.getListTag("pages", StringTag.class);
        final CompoundTag filteredPagesTag = tag.getCompoundTag("filtered_pages");
        if (pagesTag == null) {
            return;
        }

        final List<FilterableComponent> pages = new ArrayList<>();
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

        final String title = tag.getString("title", "");
        final String filteredTitle = tag.getString("filtered_title"); // Nullable
        final String author = tag.getString("author", "");
        final int generation = tag.getInt("generation");
        final boolean resolved = tag.getBoolean("resolved");
        final WrittenBook writtenBook = new WrittenBook(
            new FilterableString(limit(title, 32), limit(filteredTitle, 32)),
            author,
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
                                final String key, final StructuredDataKey<Item[]> dataKey, final boolean allowEmpty) {
        final ListTag<CompoundTag> itemsTag = tag.getListTag(key, CompoundTag.class);
        if (itemsTag != null) {
            final Item[] items = itemsTag.stream()
                .limit(256)
                .map(item -> itemFromTag(connection, item))
                .filter(Objects::nonNull)
                .filter(item -> allowEmpty || !item.isEmpty())
                .toArray(Item[]::new);
            data.set(dataKey, items);
        }
    }

    private @Nullable Item itemFromTag(final UserConnection connection, final CompoundTag item) {
        final String id = item.getString("id");
        if (id == null) {
            return null;
        }

        final int itemId = StructuredDataConverter.removeItemBackupTag(item, unmappedItemId(id));
        if (itemId == -1) {
            return null;
        }

        final byte count = item.getByte("Count", (byte) 1);
        final CompoundTag tag = item.getCompoundTag("tag");
        return handleItemToClient(connection, new DataItem(itemId, count, (short) 0, tag));
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
                id = "minecraft:sweeping_edge";
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
        if (skullOwnerTag instanceof StringTag) {
            final String name = ((StringTag) skullOwnerTag).getValue();
            if (isValidName(name)) {
                data.set(StructuredDataKey.PROFILE, new GameProfile(name, null, EMPTY_PROPERTIES));
            }
        } else if (skullOwnerTag instanceof CompoundTag) {
            final CompoundTag skullOwner = (CompoundTag) skullOwnerTag;
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

            data.set(StructuredDataKey.PROFILE, new GameProfile(name, uuid, properties.toArray(EMPTY_PROPERTIES)));
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

        data.set(StructuredDataKey.BEES, bees);
    }

    private void updateProperties(final CompoundTag propertiesTag, final List<GameProfile.Property> properties) {
        for (final Map.Entry<String, Tag> entry : propertiesTag.entrySet()) {
            if (!(entry.getValue() instanceof ListTag)) {
                continue;
            }

            for (final Tag propertyTag : (ListTag<?>) entry.getValue()) {
                if (!(propertyTag instanceof CompoundTag)) {
                    continue;
                }

                final CompoundTag compoundTag = (CompoundTag) propertyTag;
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
            data.set(StructuredDataKey.DYED_COLOR, new DyedColor(colorTag.asInt(), (hideFlags & StructuredDataConverter.HIDE_DYE_COLOR) == 0));
        }
    }

    private void updateBlockEntityTag(final UserConnection connection, @Nullable final StructuredDataContainer data, final CompoundTag tag) {
        if (tag == null) {
            return;
        }

        if (data != null) {
            final StringTag lockTag = tag.getStringTag("Lock");
            if (lockTag != null) {
                data.set(StructuredDataKey.LOCK, lockTag);
            }

            final ListTag<CompoundTag> beesTag = tag.getListTag("Bees", CompoundTag.class);
            if (beesTag != null) {
                updateBees(data, beesTag);
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
            }

            final StringTag noteBlockSoundTag = tag.getStringTag("note_block_sound");
            if (noteBlockSoundTag != null) {
                data.set(StructuredDataKey.NOTE_BLOCK_SOUND, noteBlockSoundTag.getValue());
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
            if (baseColorTag instanceof NumberTag) {
                data.set(StructuredDataKey.BASE_COLOR, ((NumberTag) baseColorTag).asInt());
            }

            updateItemList(connection, data, tag, "Items", StructuredDataKey.CONTAINER, true);
        }

        final Tag skullOwnerTag = tag.remove("SkullOwner");
        if (skullOwnerTag instanceof StringTag) {
            final CompoundTag profileTag = new CompoundTag();
            profileTag.putString("name", ((StringTag) skullOwnerTag).getValue());
            tag.put("profile", profileTag);
        } else if (skullOwnerTag instanceof CompoundTag) {
            updateSkullOwnerTag(tag, (CompoundTag) skullOwnerTag);
        }

        final ListTag<CompoundTag> patternsTag = tag.getListTag("Patterns", CompoundTag.class);
        if (patternsTag != null) {
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
                patternTag.putString("color", DyeColors.colorById(color));

                final int id = BannerPatterns1_20_5.keyToId(fullPatternIdentifier);
                return new BannerPatternLayer(Holder.of(id), color);
            }).filter(Objects::nonNull).toArray(BannerPatternLayer[]::new);
            tag.remove("Patterns");
            tag.put("patterns", patternsTag);

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

        final Tag propertiesTag = skullOwnerTag.remove("Properties");
        if (!(propertiesTag instanceof CompoundTag)) {
            return;
        }

        final ListTag<CompoundTag> propertiesListTag = new ListTag<>(CompoundTag.class);
        for (final Map.Entry<String, Tag> entry : ((CompoundTag) propertiesTag).entrySet()) {
            if (!(entry.getValue() instanceof ListTag<?>)) {
                continue;
            }

            final ListTag<?> entryValue = (ListTag<?>) entry.getValue();
            for (final Tag propertyTag : entryValue) {
                if (!(propertyTag instanceof CompoundTag)) {
                    continue;
                }

                final CompoundTag updatedPropertyTag = new CompoundTag();
                final CompoundTag propertyCompoundTag = (CompoundTag) propertyTag;
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