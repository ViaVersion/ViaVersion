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
package com.viaversion.viaversion.protocols.v26_2to26_3.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.protocols.v26_2to26_3.Protocol26_2To26_3;
import com.viaversion.viaversion.rewriter.RegistryDataRewriter;
import com.viaversion.viaversion.util.Key;
import java.util.Map;

public final class RegistryDataRewriter26_3 extends RegistryDataRewriter {

    // Provider types lost their redundant suffixes
    private static final Map<String, String> PROVIDER_TYPES = Map.of(
        "simple_state_provider", "simple",
        "weighted_state_provider", "weighted",
        "noise_threshold_provider", "noise_threshold",
        "noise_provider", "noise",
        "dual_noise_provider", "dual_noise",
        "rotated_block_provider", "rotated",
        "randomized_int_state_provider", "randomized_int",
        "rule_based_state_provider", "rule_based"
    );

    public RegistryDataRewriter26_3(final Protocol26_2To26_3 protocol) {
        super(protocol);
        addEnchantmentEffectRewriter("replace_disk", RegistryDataRewriter26_3::updateBlockStateProvider);
        addEnchantmentEffectRewriter("replace_block", RegistryDataRewriter26_3::updateBlockStateProvider);
    }

    private static void updateBlockStateProvider(final CompoundTag effect) {
        final CompoundTag provider = effect.getCompoundTag("block_state");
        if (provider != null) {
            updateBlockStateProviderTree(provider);
        }
    }

    private static void updateBlockStateProviderTree(final Tag tag) {
        if (tag instanceof ListTag<?> list) {
            for (final Tag element : list) {
                updateBlockStateProviderTree(element);
            }
            return;
        }
        if (!(tag instanceof CompoundTag compound)) {
            return;
        }

        final String type = compound.getString("type");
        if (type != null) {
            final String mappedType = PROVIDER_TYPES.get(Key.stripMinecraftNamespace(type));
            if (mappedType != null) {
                compound.putString("type", Key.namespaced(mappedType));
            }
        }

        updateBlockState(compound);
        for (final Tag value : compound.values()) {
            updateBlockStateProviderTree(value);
        }
    }

    // Block states are serialized with lowercase field names now
    private static void updateBlockState(final CompoundTag compound) {
        final Tag name = compound.remove("Name");
        if (name != null) {
            compound.put("id", name);
        }

        final Tag properties = compound.remove("Properties");
        if (properties != null) {
            compound.put("properties", properties);
        }
    }

    @Override
    public void updateEnchantmentTerm(final CompoundTag term) {
        super.updateEnchantmentTerm(term);

        if (Key.equals(term.getString("condition"), "damage_source_properties")) {
            final CompoundTag predicate = term.getCompoundTag("predicate");
            if (predicate != null) {
                markTagReferences(predicate);
            }
        }

        // Loot conditions dispatch on the default key now
        final Tag condition = term.remove("condition");
        if (condition != null) {
            term.put("type", condition);
        }
    }

    // Tag predicates hold a holder set rather than a plain tag key, so bare ids are no longer accepted
    private static void markTagReferences(final CompoundTag predicate) {
        final ListTag<CompoundTag> tags = predicate.getListTag("tags", CompoundTag.class);
        if (tags == null) {
            return;
        }

        for (final CompoundTag tagEntry : tags) {
            final StringTag id = tagEntry.getStringTag("id");
            if (id != null && !id.getValue().startsWith("#")) {
                tagEntry.putString("id", "#" + id.getValue());
            }
        }
    }

    @Override
    public void updateTrimMaterials(final RegistryEntry[] entries) {
        super.updateTrimMaterials(entries);

        for (final RegistryEntry entry : entries) {
            if (entry.tag() == null) {
                continue;
            }

            // The asset group was replaced by a single palette id, dropping per-armor overrides
            final CompoundTag tag = (CompoundTag) entry.tag();
            final StringTag assetName = tag.removeUnchecked("asset_name");
            if (assetName != null) {
                tag.putString("palette_id", Key.namespaced("trim/" + assetName.getValue()));
            }
            tag.remove("override_armor_assets");
        }
    }
}
