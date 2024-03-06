/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2023 ViaVersion and contributors
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

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntArrayTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.NumberTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.api.data.ParticleMappings;
import com.viaversion.viaversion.api.minecraft.GameProfile;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.minecraft.item.data.BlockStateProperties;
import com.viaversion.viaversion.api.minecraft.item.data.Enchantments;
import com.viaversion.viaversion.api.minecraft.item.data.MapDecoration;
import com.viaversion.viaversion.api.minecraft.item.data.MapDecorations;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.api.type.types.version.Types1_20_3;
import com.viaversion.viaversion.api.type.types.version.Types1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPacket1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.rewriter.RecipeRewriter1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.Protocol1_20_5To1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data.EnchantmentMappings;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.packet.ServerboundPacket1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.UUIDUtil;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BlockItemPacketRewriter1_20_5 extends ItemRewriter<ClientboundPacket1_20_3, ServerboundPacket1_20_5, Protocol1_20_5To1_20_3> {

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
        blockRewriter.registerChunkData1_19(ClientboundPackets1_20_3.CHUNK_DATA, ChunkType1_20_2::new);
        protocol.registerClientbound(ClientboundPackets1_20_3.BLOCK_ENTITY_DATA, wrapper -> {
            wrapper.passthrough(Type.POSITION1_14); // Position
            wrapper.passthrough(Type.VAR_INT); // Block entity type

            // No longer nullable
            final CompoundTag tag = wrapper.read(Type.COMPOUND_TAG);
            wrapper.write(Type.COMPOUND_TAG, tag != null ? tag : new CompoundTag());
        });

        registerSetCooldown(ClientboundPackets1_20_3.COOLDOWN);
        registerWindowItems1_17_1(ClientboundPackets1_20_3.WINDOW_ITEMS);
        registerSetSlot1_17_1(ClientboundPackets1_20_3.SET_SLOT);
        registerAdvancements1_20_3(ClientboundPackets1_20_3.ADVANCEMENTS);
        registerEntityEquipmentArray(ClientboundPackets1_20_3.ENTITY_EQUIPMENT);
        registerClickWindow1_17_1(ServerboundPackets1_20_5.CLICK_WINDOW);
        registerCreativeInvAction(ServerboundPackets1_20_5.CREATIVE_INVENTORY_ACTION);
        registerWindowPropertyEnchantmentHandler(ClientboundPackets1_20_3.WINDOW_PROPERTY);

        protocol.registerClientbound(ClientboundPackets1_20_3.SPAWN_PARTICLE, wrapper -> {
            final int particleId = wrapper.read(Type.VAR_INT);

            wrapper.passthrough(Type.BOOLEAN); // Long Distance
            wrapper.passthrough(Type.DOUBLE); // X
            wrapper.passthrough(Type.DOUBLE); // Y
            wrapper.passthrough(Type.DOUBLE); // Z
            wrapper.passthrough(Type.FLOAT); // Offset X
            wrapper.passthrough(Type.FLOAT); // Offset Y
            wrapper.passthrough(Type.FLOAT); // Offset Z
            wrapper.passthrough(Type.FLOAT); // Particle Data
            wrapper.passthrough(Type.INT); // Particle Count

            // Read data and add it to Particle
            final ParticleMappings mappings = protocol.getMappingData().getParticleMappings();
            final Particle particle = new Particle(mappings.getNewId(particleId));
            if (mappings.isBlockParticle(particleId)) {
                final int blockStateId = wrapper.read(Type.VAR_INT);
                particle.add(Type.VAR_INT, protocol.getMappingData().getNewBlockStateId(blockStateId));
            } else if (mappings.isItemParticle(particleId)) {
                final Item item = handleItemToClient(wrapper.read(Type.ITEM1_20_2));
                particle.add(Types1_20_5.ITEM, item);
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
                final Item input = handleItemToClient(wrapper.read(Type.ITEM1_20_2));
                final Item output = handleItemToClient(wrapper.read(Type.ITEM1_20_2));
                final Item secondItem = handleItemToClient(wrapper.read(Type.ITEM1_20_2));
                wrapper.write(Types1_20_5.ITEM, input);
                wrapper.write(Types1_20_5.ITEM, output);
                wrapper.write(Types1_20_5.ITEM, secondItem);

                wrapper.passthrough(Type.BOOLEAN); // Trade disabled
                wrapper.passthrough(Type.INT); // Number of tools uses
                wrapper.passthrough(Type.INT); // Maximum number of trade uses
                wrapper.passthrough(Type.INT); // XP
                wrapper.passthrough(Type.INT); // Special price
                wrapper.passthrough(Type.FLOAT); // Price multiplier
                wrapper.passthrough(Type.INT); // Demand

                wrapper.write(Type.BOOLEAN, false); // Ignore tags
            }
        });

        final RecipeRewriter1_20_3<ClientboundPacket1_20_3> recipeRewriter = new RecipeRewriter1_20_3<>(protocol);
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

    @Override
    public @Nullable Item handleItemToClient(@Nullable final Item item) {
        if (item == null) return null;

        super.handleItemToClient(item);
        return toStructuredItem(item);
    }

    @Override
    public @Nullable Item handleItemToServer(@Nullable final Item item) {
        if (item == null) return null;

        super.handleItemToServer(item);
        return toOldItem(item);
    }

    public Item toOldItem(final Item item) {
        // Start out with custom data and add the rest on top
        final StructuredDataContainer data = item.structuredData();
        data.setIdLookup(protocol, true);

        final StructuredData<CompoundTag> customData = data.getNonEmpty(StructuredDataKey.CUSTOM_DATA);
        final CompoundTag tag = customData != null ? customData.value() : new CompoundTag();

        // TODO

        return new DataItem(item.identifier(), (byte) item.amount(), (short) 0, tag);
    }

    public Item toStructuredItem(final Item old) {
        final CompoundTag tag = old.tag();
        final StructuredItem item = new StructuredItem(old.identifier(), (byte) old.amount(), new StructuredDataContainer());
        final StructuredDataContainer data = item.structuredData();
        data.setIdLookup(protocol, true);
        if (tag == null) {
            return item;
        }

        // Rewrite nbt to new data structures
        updateDisplay(data, tag);

        final NumberTag damage = tag.getNumberTag("Damage");
        if (damage != null && damage.asInt() != 0) {
            tag.remove("Damage");
            data.add(StructuredDataKey.DAMAGE, damage.asInt());
        }

        final NumberTag repairCost = tag.getNumberTag("RepairCost");
        if (repairCost != null && repairCost.asInt() != 0) {
            tag.remove("RepairCost");
            data.add(StructuredDataKey.REPAIR_COST, repairCost.asInt());
        }

        final NumberTag customModelData = tag.getNumberTag("CustomModelData");
        if (customModelData != null) {
            tag.remove("CustomModelData");
            data.add(StructuredDataKey.CUSTOM_MODEL_DATA, customModelData.asInt());
        }

        final CompoundTag blockState = tag.getCompoundTag("BlockStateTag");
        if (blockState != null) {
            tag.remove("BlockStateTag");
            final Map<String, String> properties = new HashMap<>();
            for (final Map.Entry<String, Tag> entry : blockState.entrySet()) {
                if (entry.getValue() instanceof StringTag) {
                    properties.put(entry.getKey(), ((StringTag) entry.getValue()).getValue());
                }
            }
            data.add(StructuredDataKey.BLOCK_STATE, new BlockStateProperties(properties));
        }

        final CompoundTag entityTag = tag.getCompoundTag("EntityTag");
        if (entityTag != null) {
            tag.remove("EntityTag");
            data.add(StructuredDataKey.ENTITY_DATA, entityTag);
        }

        final CompoundTag blockEntityTag = tag.getCompoundTag("BlockEntityTag");
        if (blockEntityTag != null) {
            // TODO lots of stuff
            // item.structuredData().add(protocol, "block_entity_data", Type.COMPOUND_TAG, blockEntityTag);
        }

        final NumberTag hideFlags = tag.getNumberTag("HideFlags");
        final int hideFlagsValue = hideFlags != null ? hideFlags.asInt() : 0;
        tag.remove("HideFlags");

        final NumberTag unbreakable = tag.getNumberTag("Unbreakable");
        if (unbreakable != null && unbreakable.asBoolean()) {
            tag.remove("Unbreakable");
            if ((hideFlagsValue & 0x04) != 0) {
                data.add(StructuredDataKey.UNBREAKABLE, true); // TODO Value is hide, should have a wrapper
            } else {
                data.addEmpty(StructuredDataKey.UNBREAKABLE);
            }
        }

        updateEnchantments(data, tag, "Enchantments", StructuredDataKey.ENCHANTMENTS, (hideFlagsValue & 0x01) == 0);
        updateEnchantments(data, tag, "StoredEnchantments", StructuredDataKey.STORED_ENCHANTMENTS, (hideFlagsValue & 0x20) == 0);

        final NumberTag map = tag.getNumberTag("map");
        if (map != null) {
            tag.remove("map");
            data.add(StructuredDataKey.MAP_ID, map.asInt());
        }

        updateMapDecorations(data, tag);

        // MAP_POST_PROCESSING is only used internally

        updateProfile(data, tag);

        // TODO
        //  StructuredDataKey.CUSTOM_NAME
        //  StructuredDataKey.LORE
        //  StructuredDataKey.CAN_PLACE_ON
        //  StructuredDataKey.CAN_BREAK
        //  StructuredDataKey.ATTRIBUTE_MODIFIERS
        //  StructuredDataKey.HIDE_ADDITIONAL_TOOLTIP
        //  StructuredDataKey.REPAIR_COST
        //  StructuredDataKey.CREATIVE_SLOT_LOCK
        //  StructuredDataKey.INTANGIBLE_PROJECTILE
        //  StructuredDataKey.DYED_COLOR
        //  StructuredDataKey.CHARGED_PROJECTILES
        //  StructuredDataKey.BUNDLE_CONTENTS
        //  StructuredDataKey.POTION_CONTENTS
        //  StructuredDataKey.SUSPICIOUS_STEW_EFFECTS
        //  StructuredDataKey.WRITABLE_BOOK_CONTENT
        //  StructuredDataKey.WRITTEN_BOOK_CONTENT
        //  StructuredDataKey.TRIM
        //  StructuredDataKey.DEBUG_STICK_STATE
        //  StructuredDataKey.BUCKET_ENTITY_DATA
        //  StructuredDataKey.BLOCK_ENTITY_DATA
        //  StructuredDataKey.INSTRUMENT
        //  StructuredDataKey.RECIPES
        //  StructuredDataKey.LODESTONE_TARGET
        //  StructuredDataKey.FIREWORK_EXPLOSION
        //  StructuredDataKey.FIREWORKS
        //  StructuredDataKey.NOTE_BLOCK_SOUND
        //  StructuredDataKey.BANNER_PATTERNS
        //  StructuredDataKey.BASE_COLOR
        //  StructuredDataKey.POT_DECORATIONS
        //  StructuredDataKey.CONTAINER
        //  StructuredDataKey.BEES
        //  StructuredDataKey.LOCK
        //  StructuredDataKey.CONTAINER_LOOT

        // Add the rest as custom data
        data.add(StructuredDataKey.CUSTOM_DATA, tag);
        return item;
    }

    private void updateEnchantments(final StructuredDataContainer data, final CompoundTag tag, final String key,
                                    final StructuredDataKey<Enchantments> newKey, final boolean show) {
        final ListTag enchantmentsTag = tag.getListTag(key);
        if (enchantmentsTag == null) {
            return;
        }

        tag.remove(key);

        final Enchantments enchantments = new Enchantments(new Int2IntOpenHashMap(), show);
        for (final Tag enchantment : enchantmentsTag) {
            if (!(enchantment instanceof CompoundTag)) {
                continue;
            }

            final CompoundTag compound = (CompoundTag) enchantment;
            final StringTag id = compound.getStringTag("id");
            final NumberTag lvl = compound.getNumberTag("lvl");
            if (id == null || lvl == null) {
                continue;
            }

            final int intId = EnchantmentMappings.id(id.getValue());
            if (intId == -1) {
                continue;
            }

            enchantments.enchantments().put(intId, lvl.asInt());
        }

        data.add(newKey, enchantments);

        // Add glint if none of the enchantments were valid
        if (enchantments.size() == 0 && !enchantmentsTag.isEmpty()) {
            data.add(StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
    }

    private void updateProfile(final StructuredDataContainer data, final CompoundTag tag) {
        final Tag skullOwnerTag = tag.get("SkullOwner");
        String name;
        UUID uuid = null;
        GameProfile.Property[] properties = {};
        if (skullOwnerTag instanceof StringTag) {
            name = skullOwnerTag.asRawString();
        } else if (skullOwnerTag instanceof CompoundTag) {
            CompoundTag skullOwner = (CompoundTag) skullOwnerTag;
            final StringTag nameTag = skullOwner.getStringTag("Name");
            name = nameTag != null ? nameTag.asRawString() : "";
            IntArrayTag idTag = skullOwner.getIntArrayTag("Id");
            if (idTag != null) {
                uuid = UUIDUtil.fromIntArray(idTag.getValue());
            }
            final CompoundTag propertiesTag = skullOwner.getCompoundTag("Properties");
            if (propertiesTag != null) {
                // It is very likely that there is only going to be one property attached ("textures")
                ArrayList<GameProfile.Property> propertyList = new ArrayList<>(1);
                for (final Map.Entry<String, Tag> mapEntry : propertiesTag.entrySet()) {
                    final String key = mapEntry.getKey();
                    final Tag valueTag = mapEntry.getValue();
                    if (valueTag instanceof ListTag) {
                        for (final Tag propertyTag : (ListTag) valueTag) {
                            if (propertyTag instanceof CompoundTag) {
                                final StringTag tagValue = ((CompoundTag) propertyTag).getStringTag("Value");
                                String value = tagValue != null ? tagValue.asRawString() : "";
                                final StringTag signatureValue = ((CompoundTag) propertyTag).getStringTag("Signature");
                                String signature = signatureValue != null ? signatureValue.asRawString() : null;
                                propertyList.add(new GameProfile.Property(key, value, signature));
                            }

                        }
                    }
                }
                properties = propertyList.toArray(new GameProfile.Property[]{});
            }
        } else {
            return;
        }
        data.add(StructuredDataKey.PROFILE, new GameProfile(name, uuid, properties));
        tag.remove("SkullOwner");
    }

    // TODO Where should data like this be stored
    private static final String[] MAP_DECORATIONS = {"frame", "red_marker", "blue_marker", "target_x", "target_point", "player_off_map", "player_off_limits", "mansion", "monument", "banner_white", "banner_orange", "banner_magenta", "banner_light_blue", "banner_yellow", "banner_lime", "banner_pink", "banner_gray", "banner_light_gray", "banner_cyan", "banner_purple", "banner_blue", "banner_brown", "banner_green", "banner_red", "banner_black", "red_x", "village_desert", "village_plains", "village_savanna", "village_snowy", "village_taiga", "jungle_temple", "swamp_hut"};

    private void updateMapDecorations(final StructuredDataContainer data, final CompoundTag tag) {
        final ListTag decorationsTag = tag.getListTag("Decorations");
        if (decorationsTag == null) return;
        tag.remove("Decorations");
        MapDecorations decorations = new MapDecorations(new Object2ObjectOpenHashMap<>());
        for (final Tag decorationTag : decorationsTag) {
            if (!(decorationTag instanceof CompoundTag)) {
                continue;
            }
            final CompoundTag decoration = (CompoundTag) decorationTag;
            final StringTag idTag = decoration.getStringTag("id");
            String id = idTag != null ? idTag.asRawString() : "";
            final NumberTag typeTag = decoration.getNumberTag("type");
            final int type_index = (typeTag != null ? typeTag.asInt() : 0) - 1;
            String stringType = "player";
            if (type_index < MAP_DECORATIONS.length && type_index >= 0) {
                stringType = MAP_DECORATIONS[type_index];
            }
            final NumberTag xTag = decoration.getNumberTag("x");
            final NumberTag zTag = decoration.getNumberTag("z");
            final NumberTag rotTag = decoration.getNumberTag("rot");
            double x = xTag != null ? xTag.asDouble() : 0.0;
            double z = zTag != null ? zTag.asDouble() : 0.0;
            double rot = rotTag != null ? rotTag.asDouble() : 0.0;
            decorations.decorations().put(id, new MapDecoration(stringType, x, z, (float) rot));
        }
        data.add(StructuredDataKey.MAP_DECORATIONS, decorations);
    }

    private void updateDisplay(final StructuredDataContainer data, final CompoundTag tag) {
        final CompoundTag displayTag = tag.getCompoundTag("display");
        if (displayTag == null) {
            return;
        }
        final NumberTag mapColorTag = displayTag.getNumberTag("MapColor");
        if (mapColorTag != null) {
            data.add(StructuredDataKey.MAP_COLOR, mapColorTag.asInt());
        }
        // TODO other display values

        tag.remove("display");
    }
}