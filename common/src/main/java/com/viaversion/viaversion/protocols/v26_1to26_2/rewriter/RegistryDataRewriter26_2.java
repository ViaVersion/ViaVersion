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
package com.viaversion.viaversion.protocols.v26_1to26_2.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.protocols.v26_1to26_2.Protocol26_1To26_2;
import com.viaversion.viaversion.rewriter.RegistryDataRewriter;
import com.viaversion.viaversion.util.Key;

public final class RegistryDataRewriter26_2 extends RegistryDataRewriter {

    public RegistryDataRewriter26_2(final Protocol26_1To26_2 protocol) {
        super(protocol);
    }

    @Override
    public void updateEnchantmentTerm(final CompoundTag term) {
        super.updateEnchantmentTerm(term);

        final String condition = term.getString("condition");
        if (Key.equals(condition, "entity_properties")) {
            final CompoundTag predicate = term.getCompoundTag("predicate");
            if (predicate != null) {
                updateEntityPredicate(predicate);
            }
        } else if (Key.equals(condition, "damage_source_properties")) {
            final CompoundTag predicate = term.getCompoundTag("predicate");
            if (predicate != null) {
                updateNestedEntityPredicate(predicate, "source_entity");
                updateNestedEntityPredicate(predicate, "direct_entity");
            }
        }
    }

    // Entity predicates are now a map of sub predicate type -> data, with the previously inlined fields keeping their names
    private void updateEntityPredicate(final CompoundTag predicate) {
        final CompoundTag typeSpecific = predicate.removeUnchecked("type_specific");
        if (typeSpecific != null) {
            updateTypeSpecificTerm(predicate, typeSpecific);
        }

        final Tag typeTag = predicate.remove("type");
        if (typeTag != null) {
            predicate.put("entity_type", typeTag);
        }

        updateNestedEntityPredicate(predicate, "vehicle");
        updateNestedEntityPredicate(predicate, "passenger");
        updateNestedEntityPredicate(predicate, "targeted_entity");
    }

    private void updateNestedEntityPredicate(final CompoundTag predicate, final String key) {
        final CompoundTag nestedPredicate = predicate.getCompoundTag(key);
        if (nestedPredicate != null) {
            updateEntityPredicate(nestedPredicate);
        }
    }

    private void updateTypeSpecificTerm(final CompoundTag predicate, final CompoundTag typeSpecific) {
        final StringTag type = typeSpecific.removeUnchecked("type");
        if (type == null) {
            return;
        }

        final String strippedType = Key.stripMinecraftNamespace(type.getValue());
        switch (strippedType) {
            case "player", "lightning", "fishing_hook", "raider", "sheep" -> predicate.put("type_specific/" + strippedType, typeSpecific);
            case "slime" -> predicate.put("type_specific/cube_mob", typeSpecific);
        }
    }
}
