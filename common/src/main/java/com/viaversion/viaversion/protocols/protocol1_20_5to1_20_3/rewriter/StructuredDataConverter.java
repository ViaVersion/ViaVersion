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

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.data.AttributeModifier;
import com.viaversion.viaversion.api.minecraft.item.data.Enchantments;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data.Attributes1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data.Enchantments1_20_3;
import com.viaversion.viaversion.util.ComponentUtil;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Map;

final class StructuredDataConverter {

    private static final Map<StructuredDataKey<?>, Rewriter<?>> REWRITERS = new Reference2ObjectOpenHashMap<>();

    static {
        register(StructuredDataKey.DAMAGE, (data, tag) -> tag.putInt("Damage", data));
        register(StructuredDataKey.UNBREAKABLE, (data, tag) -> {
            tag.putBoolean("Unbreakable", true);
            if (!data.showInTooltip()) {
                putHideFlag(tag, 0x04);
            }
        });
        register(StructuredDataKey.CUSTOM_NAME, (data, tag) -> tag.putString("CustomName", ComponentUtil.tagToJsonString(data)));
        register(StructuredDataKey.LORE, (data, tag) -> {
            final ListTag<StringTag> lore = new ListTag<>(StringTag.class);
            for (final Tag loreEntry : data) {
                lore.add(new StringTag(ComponentUtil.tagToJsonString(loreEntry)));
            }
            tag.put("Lore", lore);
        });
        register(StructuredDataKey.ENCHANTMENTS, StructuredDataConverter::convertEnchantments);
        register(StructuredDataKey.STORED_ENCHANTMENTS, StructuredDataConverter::convertEnchantments);
        //register(StructuredDataKey.CAN_PLACE_ON, (data, tag) -> ); // TODO
        //register(StructuredDataKey.CAN_BREAK, (data, tag) -> ); // TODO
        register(StructuredDataKey.ATTRIBUTE_MODIFIERS, (data, tag) -> {
            final ListTag<CompoundTag> modifiers = new ListTag<>(CompoundTag.class);
            for (final AttributeModifier modifier : data.modifiers()) {
                final String identifier = Attributes1_20_3.idToKey(modifier.attribute());
                if (identifier == null) {
                    continue;
                }

                final CompoundTag modifierTag = new CompoundTag();
                modifierTag.putString("AttributeName", identifier);
                modifierTag.putString("Name", modifier.modifier().name());
                modifierTag.putDouble("Amount", modifier.modifier().amount());
                modifierTag.putInt("Slot", modifier.slot());
                modifierTag.putInt("Operation", modifier.modifier().operation());
                modifiers.add(modifierTag);
            }
            tag.put("AttributeModifiers", modifiers);

            if (!data.showInTooltip()) {
                putHideFlag(tag, 0x02);
            }
        });
        register(StructuredDataKey.CUSTOM_MODEL_DATA, (data, tag) -> tag.putInt("CustomModelData", data));
        register(StructuredDataKey.HIDE_ADDITIONAL_TOOLTIP, (data, tag) -> putHideFlag(tag, 0x20));
        register(StructuredDataKey.REPAIR_COST, (data, tag) -> tag.putInt("RepairCost", data));

    }

    private static void convertEnchantments(final Enchantments data, final CompoundTag tag) {
        final ListTag<CompoundTag> enchantments = new ListTag<>(CompoundTag.class);
        for (final Int2IntMap.Entry entry : data.enchantments().int2IntEntrySet()) {
            final String identifier = Enchantments1_20_3.idToKey(entry.getIntKey());
            if (identifier == null) {
                continue;
            }

            final CompoundTag enchantment = new CompoundTag();
            enchantment.putString("id", identifier);
            enchantment.putShort("lvl", (short) entry.getIntKey());
            enchantments.add(enchantment);
        }
        tag.put("Enchantments", enchantments);

        if (!data.showInTooltip()) {
            putHideFlag(tag, 0x01);
        }
    }

    private static void putHideFlag(final CompoundTag tag, final int value) {
        tag.putInt("HideFlags", tag.getInt("HideFlags") | value);
    }

    public static <T> void rewrite(final StructuredData<T> data, final CompoundTag tag) {
        if (data.isEmpty()) {
            return;
        }

        //noinspection unchecked
        final Rewriter<T> rewriter = (Rewriter<T>) REWRITERS.get(data.key());
        if (rewriter != null) {
            rewriter.rewrite(data.value(), tag);
        }
    }

    private static <T> void register(final StructuredDataKey<T> key, final Rewriter<T> rewriter) {
        REWRITERS.put(key, rewriter);
    }

    @FunctionalInterface
    interface Rewriter<T> {

        void rewrite(T data, CompoundTag tag);
    }
}
