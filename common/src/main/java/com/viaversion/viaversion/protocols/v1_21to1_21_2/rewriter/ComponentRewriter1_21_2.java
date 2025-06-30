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
package com.viaversion.viaversion.protocols.v1_21to1_21_2.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPacket1_21;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.Protocol1_21To1_21_2;
import com.viaversion.viaversion.rewriter.text.JsonNBTComponentRewriter;
import com.viaversion.viaversion.util.SerializerVersion;
import com.viaversion.viaversion.util.TagUtil;
import java.util.Collections;
import java.util.Iterator;

public final class ComponentRewriter1_21_2 extends JsonNBTComponentRewriter<ClientboundPacket1_21> {

    public ComponentRewriter1_21_2(final Protocol1_21To1_21_2 protocol) {
        super(protocol, ReadType.NBT);
    }

    @Override
    protected void handleShowItem(final UserConnection connection, final CompoundTag itemTag, final CompoundTag componentsTag) {
        super.handleShowItem(connection, itemTag, componentsTag);
        if (componentsTag == null) {
            return;
        }

        convertAttributes(componentsTag, protocol.getMappingData().getAttributeMappings());

        final CompoundTag instrument = TagUtil.getNamespacedCompoundTag(componentsTag, "instrument");
        if (instrument != null) {
            instrument.putString("description", "");
        }

        final CompoundTag food = TagUtil.getNamespacedCompoundTag(componentsTag, "food");
        if (food != null) {
            final CompoundTag convertsTo = food.getCompoundTag("using_converts_to");
            if (convertsTo != null) {
                food.remove("using_converts_to");
                componentsTag.put("minecraft:use_remainder", convertsTo);
            }
            food.remove("eat_seconds");
            food.remove("effects");
        }


        final CompoundTag enchantments = TagUtil.getNamespacedCompoundTag(componentsTag, "enchantments");
        if (enchantments != null) {
            final CompoundTag levels = enchantments.getCompoundTag("levels");
            levels.entrySet().removeIf(entry -> ((NumberTag) entry.getValue()).asInt() == 0);
        }

        removeDataComponents(componentsTag, StructuredDataKey.FIRE_RESISTANT, StructuredDataKey.LOCK);

        final StringTag customName = TagUtil.getNamespacedStringTag(componentsTag, "custom_name");
        final StringTag itemName = TagUtil.getNamespacedStringTag(componentsTag, "item_name");
        if (customName != null || itemName == null) {
            return;
        }

        final int identifier = protocol.getMappingData().getFullItemMappings().mappedId(itemTag.getString("id"));
        if (identifier == 952 || identifier == 1147 || identifier == 1039 || identifier == 1203 || identifier == 1200 || identifier == 1204 || identifier == 1202) {
            final var input = inputSerializerVersion();
            final var output = outputSerializerVersion();

            final CompoundTag name = new CompoundTag();
            name.putBoolean("italic", false);
            name.putString("text", "");
            final Tag nameTag = input.toTag(input.toComponent(itemName.getValue()));
            name.put("extra", new ListTag<>(Collections.singletonList(nameTag)));

            componentsTag.put("minecraft:custom_name", new StringTag(output.toString(output.toComponent(name))));
        }
    }

    @Override
    protected void handleTranslate(final UserConnection connection, final CompoundTag parentTag, final StringTag translateTag) {
        switch (translateTag.getValue()) {
            case "commands.drop.no_loot_table" -> translateTag.setValue("Entity %s has no loot table");
            case "commands.advancement.advancementNotFound" -> translateTag.setValue("No advancement was found by the name '%1$s'");
            case "commands.function.success.single" -> translateTag.setValue("Test Executed %s command(s) from function '%s'");
            case "commands.function.success.single.result" -> translateTag.setValue("Function '%2$s' returned %1$s");
            case "commands.function.success.multiple" -> translateTag.setValue("Test Executed %s command(s) from %s functions");
            case "commands.function.success.multiple.result" -> translateTag.setValue("Executed %s functions");
            case "commands.fillbiome.success" -> translateTag.setValue("%s biome entry/entries set between %s, %s, %s and %s, %s, %s");
            case "commands.publish.success" -> translateTag.setValue("Multiplayer game is now hosted on port %s");
        }
    }

    public static void convertAttributes(final CompoundTag componentsTag, final FullMappings mappings) {
        final CompoundTag attributeModifiers = TagUtil.getNamespacedCompoundTag(componentsTag, "attribute_modifiers");
        if (attributeModifiers == null) {
            return;
        }

        final ListTag<CompoundTag> modifiers = attributeModifiers.getListTag("modifiers", CompoundTag.class);
        final Iterator<CompoundTag> iterator = modifiers.iterator();
        while (iterator.hasNext()) {
            final CompoundTag modifier = iterator.next();
            final StringTag attribute = modifier.getStringTag("type");
            final String mappedAttribute = mappings.mappedIdentifier(attribute.getValue());
            if (mappedAttribute != null) {
                attribute.setValue(mappedAttribute);
            } else {
                iterator.remove();
            }
        }
    }

    @Override
    protected SerializerVersion inputSerializerVersion() {
        return SerializerVersion.V1_20_5;
    }

    // Only cosmetic changes in the 1.21.2 serializer
}
