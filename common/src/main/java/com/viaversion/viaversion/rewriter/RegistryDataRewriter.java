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
package com.viaversion.viaversion.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.data.entity.DimensionData;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.data.entity.DimensionDataImpl;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.TagUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class RegistryDataRewriter {
    private final Map<String, Consumer<CompoundTag>> enchantmentEffectRewriters = new Object2ObjectArrayMap<>();
    private final Map<String, List<RegistryEntry>> toAdd = new Object2ObjectArrayMap<>();
    private final Protocol<?, ?, ?, ?> protocol;

    public RegistryDataRewriter(Protocol<?, ?, ?, ?> protocol) {
        this.protocol = protocol;
    }

    public void handle(final PacketWrapper wrapper) {
        final String registryKey = wrapper.passthrough(Types.STRING);
        RegistryEntry[] entries = wrapper.read(Types.REGISTRY_ENTRY_ARRAY);
        entries = handle(wrapper.user(), Key.stripMinecraftNamespace(registryKey), entries);
        wrapper.write(Types.REGISTRY_ENTRY_ARRAY, entries);
    }

    public RegistryEntry[] handle(final UserConnection connection, String key, RegistryEntry[] entries) {
        key = Key.stripMinecraftNamespace(key);
        if (key.equals("enchantment")) {
            updateEnchantments(entries);
        } else if (key.equals("trim_material")) {
            updateTrimMaterials(entries);
        }

        final List<RegistryEntry> toAdd = this.toAdd.get(key);
        if (toAdd != null) {
            final Set<String> existingKeys = new HashSet<>();

            final RegistryEntry[] updatedEntries = new RegistryEntry[entries.length + toAdd.size()];
            int index = 0;
            for (final RegistryEntry entry : entries) {
                updatedEntries[index++] = entry;
                existingKeys.add(entry.key());
            }
            for (final RegistryEntry entry : toAdd) {
                if (existingKeys.contains(entry.key())) {
                    continue;
                }

                updatedEntries[index++] = entry.copy();
            }

            if (index < updatedEntries.length) {
                entries = Arrays.copyOf(updatedEntries, index);
            } else {
                entries = updatedEntries;
            }
        }

        trackDimensionAndBiomes(connection, key, entries);
        return entries;
    }

    public void addEntries(final String registryKey, final RegistryEntry... entries) {
        toAdd.computeIfAbsent(Key.stripMinecraftNamespace(registryKey), $ -> new ArrayList<>()).addAll(List.of(entries));
    }

    public void addEnchantmentEffectRewriter(final String key, final Consumer<CompoundTag> rewriter) {
        enchantmentEffectRewriters.put(Key.stripMinecraftNamespace(key), rewriter);
    }

    public void trackDimensionAndBiomes(final UserConnection connection, final String registryKey, final RegistryEntry[] entries) {
        if (registryKey.equals("worldgen/biome")) {
            protocol.getEntityRewriter().tracker(connection).setBiomesSent(entries.length);
        } else if (registryKey.equals("dimension_type")) {
            final Map<String, DimensionData> dimensionDataMap = new HashMap<>(entries.length);
            for (int i = 0; i < entries.length; i++) {
                final RegistryEntry entry = entries[i];
                final String key = Key.stripMinecraftNamespace(entry.key());
                final DimensionData dimensionData = entry.tag() != null
                    ? new DimensionDataImpl(i, (CompoundTag) entry.tag())
                    : DimensionDataImpl.withDefaultsFor(key, i);
                dimensionDataMap.put(key, dimensionData);
            }
            protocol.getEntityRewriter().tracker(connection).setDimensions(dimensionDataMap);
        }
    }

    public void updateEnchantments(final RegistryEntry[] entries) {
        for (final RegistryEntry entry : entries) {
            if (entry.tag() == null) {
                continue;
            }

            final CompoundTag tag = (CompoundTag) entry.tag();
            if (protocol.getMappingData().getFullItemMappings() != null) {
                updateItemList(tag.getListTag("supported_items", StringTag.class));
                updateItemList(tag.getListTag("primary_items", StringTag.class));
            }

            final CompoundTag effects = tag.getCompoundTag("effects");
            if (effects == null) {
                continue;
            }

            // Go through all effects, almost all of them may contain an "effect" element
            for (final Map.Entry<String, Tag> effectEntry : effects.entrySet()) {
                if (effectEntry.getValue() instanceof final CompoundTag compoundTag) {
                    updateNestedEffect(compoundTag);
                } else if (effectEntry.getValue() instanceof final ListTag<?> listTag && listTag.getElementType() == CompoundTag.class) {
                    for (final Tag effectTag : listTag) {
                        updateNestedEffect((CompoundTag) effectTag);
                    }
                }
            }

            updateAttributesFields(effects);
        }
    }

    public void updateTrimMaterials(final RegistryEntry[] entries) {
        if (protocol.getMappingData().getFullItemMappings() == null) {
            return;
        }

        for (final RegistryEntry entry : entries) {
            if (entry.tag() == null) {
                continue;
            }

            final StringTag ingredientTag = ((CompoundTag) entry.tag()).getStringTag("ingredient");
            if (ingredientTag == null) {
                return;
            }

            updateItem(ingredientTag);
        }
    }

    private void updateNestedEffect(final CompoundTag effectsTag) {
        final CompoundTag effect = effectsTag.getCompoundTag("effect");
        if (effect == null) {
            return;
        }

        runEffectRewriters(effect);

        final ListTag<CompoundTag> innerEffects = effect.getListTag("effects", CompoundTag.class);
        if (innerEffects == null) {
            return;
        }

        for (final CompoundTag innerEffect : innerEffects) {
            runEffectRewriters(innerEffect);
        }
    }

    private void updateAttributesFields(final CompoundTag effects) {
        if (!hasAttributeMappings()) {
            return;
        }

        final ListTag<CompoundTag> attributesList = TagUtil.getNamespacedCompoundTagList(effects, "attributes");
        if (attributesList == null) {
            return;
        }

        for (final CompoundTag attributeData : attributesList) {
            updateAttributeField(attributeData);
        }
    }

    private void runEffectRewriters(final CompoundTag effectTag) {
        String effect = effectTag.getString("type");
        if (effect == null) {
            return;
        }

        effect = Key.stripMinecraftNamespace(effect);
        updateAttributeField(effectTag);

        final Consumer<CompoundTag> rewriter = enchantmentEffectRewriters.get(effect);
        if (rewriter != null) {
            rewriter.accept(effectTag);
        }
    }

    private void updateAttributeField(final CompoundTag attributeData) {
        final StringTag attributeTag = attributeData.getStringTag("attribute");
        if (attributeTag == null) {
            return;
        }

        final FullMappings mappings = protocol.getMappingData().getAttributeMappings();
        final String attribute = Key.stripMinecraftNamespace(attributeTag.getValue());
        String mappedAttribute = mappings.mappedIdentifier(attribute);
        if (mappedAttribute == null) {
            mappedAttribute = mappings.mappedIdentifier(0); // Dummy
        }
        attributeTag.setValue(mappedAttribute);
    }

    private boolean hasAttributeMappings() {
        return protocol.getMappingData() != null && protocol.getMappingData().getAttributeMappings() != null;
    }

    private void updateItemList(final ListTag<StringTag> listTag) {
        if (listTag == null) {
            return;
        }
        for (final StringTag tag : listTag) {
            updateItem(tag);
        }
    }

    private void updateItem(final StringTag tag) {
        final String mapped = protocol.getMappingData().getFullItemMappings().mappedIdentifier(tag.getValue());
        if (mapped != null) {
            tag.setValue(mapped);
        }
    }
}
