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
import com.viaversion.viaversion.protocols.v26_2to26_3.Protocol26_2To26_3;
import com.viaversion.viaversion.rewriter.RegistryDataRewriter;
import com.viaversion.viaversion.util.Key;

public final class RegistryDataRewriter26_3 extends RegistryDataRewriter {

    public RegistryDataRewriter26_3(final Protocol26_2To26_3 protocol) {
        super(protocol);
    }

    @Override
    public void updateEnchantmentTerm(final CompoundTag term) {
        final StringTag condition = term.removeUnchecked("condition");
        if (condition != null) {
            term.put("type", condition);
        }

        if (Key.equals(condition.getValue(), "damage_source_properties")) {
            final CompoundTag predicate = term.getCompoundTag("predicate");
            if (predicate != null) {
                final ListTag<CompoundTag> tags = predicate.getListTag("tags", CompoundTag.class);
                if (tags != null) {
                    updateTagKey(tags);
                }
            }
        }

        super.updateEnchantmentTerm(term);
    }

    @Override
    protected boolean updateBlockState(final Tag blockStateTag) {
        if (blockStateTag instanceof CompoundTag compoundTag) { // can only be a compound tag pre-26.3
            final Tag id = compoundTag.remove("Name");
            compoundTag.put("id", id);

            final Tag properties = compoundTag.remove("Properties");
            if (properties != null) {
                compoundTag.put("properties", properties);
            }
        }
        return super.updateBlockState(blockStateTag);
    }

    private void updateTagKey(final ListTag<CompoundTag> tags) {
        for (final CompoundTag tag : tags) {
            final StringTag tagKey = tag.getStringTag("id");
            tagKey.setValue("#" + tagKey.getValue());
        }
    }
}
