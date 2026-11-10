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
package com.viaversion.viaversion.protocols.v1_20_2to1_20_3.storage;

import com.viaversion.nbt.tag.Tag;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ScoreboardStorage {

    private static final int SIDEBAR_SLOT = 1;
    private static final int FIRST_TEAM_SIDEBAR_SLOT = 3;
    private static final int LAST_TEAM_SIDEBAR_SLOT = 18;
    private final Map<String, Objective> objectives = new HashMap<>();
    private final Int2ObjectMap<String> displaySlots = new Int2ObjectOpenHashMap<>();

    public void putObjective(final String name, final Tag displayName, final int renderType, final boolean numberFormatHidden) {
        objectives.put(name, new Objective(displayName, renderType, numberFormatHidden));
    }

    public void removeObjective(final String name) {
        objectives.remove(name);
        displaySlots.values().removeIf(name::equals);
    }

    public @Nullable Objective objective(final String name) {
        return objectives.get(name);
    }

    public void setNumberFormatHidden(final String name, final boolean numberFormatHidden) {
        final Objective objective = objectives.get(name);
        if (objective != null) {
            objectives.put(name, new Objective(objective.displayName(), objective.renderType(), numberFormatHidden));
        }
    }

    public @Nullable String setDisplaySlot(final int slot, final String objectiveName) {
        if (objectiveName.isEmpty()) {
            return displaySlots.remove(slot);
        }
        return displaySlots.put(slot, objectiveName);
    }

    public boolean isSidebar(final String objectiveName) {
        for (final Int2ObjectMap.Entry<String> entry : displaySlots.int2ObjectEntrySet()) {
            if (isSidebarSlot(entry.getIntKey()) && objectiveName.equals(entry.getValue())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSidebarSlot(final int slot) {
        return slot == SIDEBAR_SLOT || slot >= FIRST_TEAM_SIDEBAR_SLOT && slot <= LAST_TEAM_SIDEBAR_SLOT;
    }

    public record Objective(Tag displayName, int renderType, boolean numberFormatHidden) {
    }
}
