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
package com.viaversion.viaversion.protocols.v1_21to1_21_2.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.EnumTypes;
import com.viaversion.viaversion.api.minecraft.item.data.Equippable;
import com.viaversion.viaversion.util.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Converts data components declared as legacy nbt by servers older than the version that added them.
 * <p>
 * {@link com.viaversion.viaversion.protocols.v1_20_3to1_20_5.rewriter.BlockItemPacketRewriter1_20_5} converts
 * legacy nbt into the data components that have a legacy equivalent. Components added afterwards have none, so
 * servers predating them cannot express them at all. This reads them from a {@value #COMPONENTS_TAG} sub-tag of
 * the item's legacy nbt instead, using the same field names as the vanilla component definitions.
 * <p>
 * This is the inverse of {@link com.viaversion.viaversion.protocols.v1_20_3to1_20_5.rewriter.StructuredDataConverter},
 * and belongs in the protocol that first supports the components it converts.
 */
public final class LegacyComponentConverter {

    /**
     * Sub-tag of the item's legacy nbt holding data components, keyed by their vanilla identifier.
     */
    public static final String COMPONENTS_TAG = "VV|components";

    private static final Holder<SoundEvent> DEFAULT_EQUIP_SOUND = Holder.of(new SoundEvent("minecraft:item.armor.equip_generic", null));
    private static final Holder<SoundEvent> DEFAULT_SHEARING_SOUND = Holder.of(new SoundEvent("minecraft:item.shears.snip", null));

    private static final Map<String, BiConsumer<CompoundTag, StructuredDataContainer>> CONVERTERS = new HashMap<>();

    static {
        register("equippable", LegacyComponentConverter::convertEquippable);
    }

    private LegacyComponentConverter() {
    }

    /**
     * Adds data components declared in the item's legacy nbt. Unknown or malformed entries are ignored.
     *
     * @param item non-empty item, already converted to data components
     */
    public static void convertLegacyComponents(final Item item) {
        final StructuredDataContainer container = item.dataContainer();
        final CompoundTag customData = container.get(StructuredDataKey.CUSTOM_DATA);
        if (customData == null) {
            return;
        }

        final CompoundTag components = customData.getCompoundTag(COMPONENTS_TAG);
        if (components == null) {
            return;
        }

        // Note that the tag is deliberately left in custom_data: it holds the original legacy nbt, which is
        // handed back to the backend server as-is in serverbound item packets. Removing it here would make
        // the server see the item lose data as soon as the client moves it.
        for (final Map.Entry<String, Tag> entry : components.entrySet()) {
            if (!(entry.getValue() instanceof CompoundTag componentTag)) {
                continue;
            }

            final BiConsumer<CompoundTag, StructuredDataContainer> converter = CONVERTERS.get(Key.stripMinecraftNamespace(entry.getKey()));
            if (converter != null) {
                converter.accept(componentTag, container);
            }
        }
    }

    private static void convertEquippable(final CompoundTag tag, final StructuredDataContainer container) {
        final String slotName = tag.getString("slot");
        if (slotName == null) {
            return;
        }

        // idFromName falls back to the first entry for unknown names, so check the slot actually exists
        final int slot = EnumTypes.EQUIPMENT_SLOT.idFromName(slotName);
        if (!EnumTypes.EQUIPMENT_SLOT.nameFromId(slot).equals(slotName)) {
            return;
        }

        final String assetId = tag.getString("asset_id");
        if (assetId != null && !Key.isValid(assetId)) {
            // Would throw in the 1.21.5+ hash codec, which is far away from here
            return;
        }

        final String cameraOverlay = tag.getString("camera_overlay");
        if (cameraOverlay != null && !Key.isValid(cameraOverlay)) {
            return;
        }

        // allowed_entities is not supported; it would need entity registry resolution at this point
        container.set(StructuredDataKey.EQUIPPABLE1_21_2, new Equippable(
            slot,
            soundEvent(tag.getString("equip_sound"), DEFAULT_EQUIP_SOUND),
            assetId,
            cameraOverlay,
            null,
            getBoolean(tag, "dispensable", true),
            getBoolean(tag, "swappable", true),
            getBoolean(tag, "damage_on_hurt", true),
            getBoolean(tag, "equip_on_interact", false),
            getBoolean(tag, "can_be_sheared", false),
            soundEvent(tag.getString("shearing_sound"), DEFAULT_SHEARING_SOUND)
        ));
    }

    private static void register(final String identifier, final BiConsumer<CompoundTag, StructuredDataContainer> converter) {
        CONVERTERS.put(identifier, converter);
    }

    /**
     * Direct holders pass through the sound rewriting of later protocols untouched, unlike registry ids, which
     * would have to be in this protocol's mapped sound id space.
     */
    private static Holder<SoundEvent> soundEvent(final String identifier, final Holder<SoundEvent> defaultValue) {
        return identifier != null && Key.isValid(identifier) ? Holder.of(new SoundEvent(identifier, null)) : defaultValue;
    }

    private static boolean getBoolean(final CompoundTag tag, final String key, final boolean defaultValue) {
        final NumberTag value = tag.getNumberTag(key);
        return value != null ? value.asInt() != 0 : defaultValue;
    }
}
