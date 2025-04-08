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
package com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.Protocol1_21_4To1_21_5;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPacket1_21_2;
import com.viaversion.viaversion.rewriter.text.JsonNBTComponentRewriter;
import com.viaversion.viaversion.util.SerializerVersion;
import com.viaversion.viaversion.util.TagUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.viaversion.viaversion.util.TagUtil.getNamespacedCompoundTag;
import static com.viaversion.viaversion.util.TagUtil.removeNamespaced;

public final class ComponentRewriter1_21_5 extends JsonNBTComponentRewriter<ClientboundPacket1_21_2> {

    public ComponentRewriter1_21_5(final Protocol1_21_4To1_21_5 protocol) {
        super(protocol, ReadType.NBT);
    }

    @Override
    protected void processCompoundTag(final UserConnection connection, final CompoundTag tag) {
        super.processCompoundTag(connection, tag);
        if (tag.remove("hoverEvent") instanceof final CompoundTag hoverEvent) {
            tag.put("hover_event", hoverEvent);
        }

        if (tag.remove("clickEvent") instanceof final CompoundTag clickEvent) {
            try {
                updateClickEvent(clickEvent);
            } catch (final IllegalArgumentException | URISyntaxException ignored) {
                // Only readd if the format is correct
                return;
            }

            tag.put("click_event", clickEvent);
        }
    }

    @Override
    protected void handleShowItem(final UserConnection connection, final CompoundTag itemTag, final @Nullable CompoundTag componentsTag) {
        super.handleShowItem(connection, itemTag, componentsTag);
        if (componentsTag == null) {
            return;
        }

        // Some of the tooltip hiding handling
        final CompoundTag tooltipDisplay = new CompoundTag();
        final boolean hideTooltip = removeNamespaced(componentsTag, "hide_tooltip");
        final ListTag<StringTag> hiddenComponents = new ListTag<>(StringTag.class);
        if (removeNamespaced(componentsTag, "hide_additional_tooltip")) {
            for (final StructuredDataKey<?> key : BlockItemPacketRewriter1_21_5.HIDE_ADDITIONAL_KEYS) {
                hiddenComponents.add(new StringTag(key.identifier()));
            }
        }
        updateHiddenComponents(componentsTag, "unbreakable", hiddenComponents);
        updateHiddenComponents(componentsTag, "can_place_on", hiddenComponents);
        updateHiddenComponents(componentsTag, "can_break", hiddenComponents);
        updateHiddenComponents(componentsTag, "dyed_color", hiddenComponents);
        updateHiddenComponents(componentsTag, "attribute_modifiers", hiddenComponents);
        updateHiddenComponents(componentsTag, "trim", hiddenComponents);
        updateHiddenComponents(componentsTag, "enchantments", hiddenComponents);
        updateHiddenComponents(componentsTag, "stored_enchantments", hiddenComponents);
        updateHiddenComponents(componentsTag, "jukebox_playable", hiddenComponents);
        if (hideTooltip || !hiddenComponents.isEmpty()) {
            tooltipDisplay.putBoolean("hide_tooltip", hideTooltip);
            tooltipDisplay.put("hidden_components", hiddenComponents);
            componentsTag.put("tooltip_display", tooltipDisplay);
        }

        final CompoundTag attributeModifiers = getNamespacedCompoundTag(componentsTag, "attribute_modifiers");
        if (attributeModifiers != null) {
            removeDataComponents(componentsTag, "attribute_modifiers");
            componentsTag.put("attribute_modifiers", attributeModifiers.get("modifiers"));
        }

        final CompoundTag dyedColor = getNamespacedCompoundTag(componentsTag, "dyed_color");
        if (dyedColor != null) {
            removeDataComponents(componentsTag, "dyed_color");
            componentsTag.put("dyed_color", dyedColor.get("rgb"));
        }

        handleAdventureModePredicate(componentsTag, "can_break");
        handleAdventureModePredicate(componentsTag, "can_place_on");
        handleEnchantments(componentsTag, "enchantments");
        handleEnchantments(componentsTag, "stored_enchantments");

        // Usual item handling
        final CompoundTag useRemainder = TagUtil.getNamespacedCompoundTag(componentsTag, "use_remainder");
        if (useRemainder != null) {
            handleShowItem(connection, useRemainder);
        }
        handleContainerContents(connection, componentsTag);
        handleItemArrayContents(connection, componentsTag, "bundle_contents");
        handleItemArrayContents(connection, componentsTag, "charged_projectiles");
        handleWrittenBookContents(connection, componentsTag);

        // NO MORE SNBT IN TEXT COMPONENTS
        updateUglyJson(componentsTag, connection);

        removeDataComponents(componentsTag, StructuredDataKey.INSTRUMENT1_21_2, StructuredDataKey.JUKEBOX_PLAYABLE1_21);
    }

    private void updateHiddenComponents(final CompoundTag componentsTag, final String key, final ListTag<StringTag> hiddenComponents) {
        final CompoundTag component = getNamespacedCompoundTag(componentsTag, key);
        if (component == null) {
            return;
        }

        final boolean showInTooltip = component.getBoolean("show_in_tooltip", true);
        if (!showInTooltip) {
            hiddenComponents.add(new StringTag(key));
        }
        component.remove("show_in_tooltip");
    }

    private void handleAdventureModePredicate(final CompoundTag componentsTag, final String key) {
        final CompoundTag predicate = getNamespacedCompoundTag(componentsTag, key);
        if (predicate == null) {
            return;
        }

        final ListTag<CompoundTag> blockPredicates = predicate.getListTag("predicates", CompoundTag.class);
        removeDataComponents(componentsTag, key);
        componentsTag.put(key, blockPredicates);
    }

    private void handleEnchantments(final CompoundTag componentsTag, final String key) {
        final CompoundTag enchantments = getNamespacedCompoundTag(componentsTag, key);
        if (enchantments != null) {
            if (enchantments.remove("levels") instanceof final CompoundTag levels) {
                enchantments.putAll(levels);
            }
        }
    }

    @Override
    protected void handleHoverEvent(final UserConnection connection, final CompoundTag hoverEventTag) {
        final String action = hoverEventTag.getString("action");
        if (action == null) {
            return;
        }

        // Convert legacy values one last time and get rid of them forever
        switch (action) {
            case "show_text" -> updateShowTextHover(connection, hoverEventTag);
            case "show_entity" -> updateShowEntityHover(connection, hoverEventTag);
            case "show_item" -> updateShowItemHover(connection, hoverEventTag);
        }
    }

    private void updateClickEvent(final CompoundTag clickEventTag) throws URISyntaxException {
        final String action = clickEventTag.getString("action");
        if (action == null) {
            return;
        }

        if (action.equals("open_url")) {
            final StringTag url = clickEventTag.getStringTag("value");
            final URI uri = new URI(url.getValue());
            if (!"https".equalsIgnoreCase(uri.getScheme()) && !"http".equalsIgnoreCase(uri.getScheme())) {
                throw new IllegalArgumentException("Invalid URL");
            }

            clickEventTag.put("url", url);
        } else if (action.equals("change_page")) {
            final int page = Integer.parseInt(clickEventTag.getString("value"));
            if (page < 1) {
                throw new IllegalArgumentException("Invalid page number");
            }

            clickEventTag.putInt("page", page);
        } else if (action.equals("run_command") || action.equals("suggest_command")) {
            clickEventTag.put("command", clickEventTag.getStringTag("value"));
        }
    }

    private void updateShowTextHover(final UserConnection connection, final CompoundTag hoverEventTag) {
        if (hoverEventTag.remove("value") instanceof final StringTag value) {
            final Tag contents = uglyJsonToTag(connection, value.getValue());
            hoverEventTag.put("value", contents);
            return;
        }

        // Move to value field
        final Tag contents = hoverEventTag.remove("contents");
        processTag(connection, contents);
        hoverEventTag.put("value", contents);
    }

    private void updateShowItemHover(final UserConnection connection, final CompoundTag hoverEventTag) {
        convertLegacyItemContents(hoverEventTag);

        // Inline contents, rename fields
        final Tag contentsTag = hoverEventTag.remove("contents");
        if (contentsTag instanceof final CompoundTag compoundContents) {
            final Tag countTag = compoundContents.get("count");
            if (countTag != null) {
                hoverEventTag.put("count", countTag);
            }

            final Tag idTag = compoundContents.get("id");
            if (idTag != null) {
                hoverEventTag.put("id", idTag);
            }

            final CompoundTag componentsTag = compoundContents.getCompoundTag("components");
            handleShowItem(connection, compoundContents, componentsTag);

            if (componentsTag != null) {
                hoverEventTag.put("components", componentsTag);
            }
        } else if (contentsTag instanceof final StringTag inlinedContents) {
            hoverEventTag.put("id", inlinedContents);
        }
    }

    private void updateUglyJson(final CompoundTag componentsTag, final UserConnection connection) {
        updateUglyJson(componentsTag, "item_name", connection);
        updateUglyJson(componentsTag, "custom_name", connection);

        final String loreKey = TagUtil.getNamespacedTagKey(componentsTag, "lore");
        final ListTag<StringTag> lore = componentsTag.getListTag(loreKey, StringTag.class);
        if (lore != null) {
            componentsTag.put(loreKey, updateComponentList(connection, lore));
        }
    }

    public ListTag<CompoundTag> updateComponentList(final UserConnection connection, final ListTag<StringTag> messages) {
        final ListTag<CompoundTag> updatedMessages = new ListTag<>(CompoundTag.class);
        for (final StringTag message : messages) {
            // Convert and make sure they're all of the same type
            final Tag output = uglyJsonToTag(connection, message.getValue());
            final CompoundTag wrappedComponent = new CompoundTag();
            wrappedComponent.putString("text", "");
            wrappedComponent.put("extra", new ListTag<>(List.of(output)));
            updatedMessages.add(wrappedComponent);
        }
        return updatedMessages;
    }

    private void updateUglyJson(final CompoundTag componentsTag, final String key, final UserConnection connection) {
        final String actualKey = TagUtil.getNamespacedTagKey(componentsTag, key);
        final String json = componentsTag.getString(actualKey);
        if (json == null) {
            return;
        }

        componentsTag.put(actualKey, uglyJsonToTag(connection, json));
    }

    private void updateShowEntityHover(final UserConnection connection, final CompoundTag hoverEventTag) {
        convertLegacyEntityContents(hoverEventTag);

        if (!(hoverEventTag.remove("contents") instanceof final CompoundTag contents)) {
            return;
        }

        // Inline contents, rename fields
        final Tag nameTag = contents.get("name");
        if (nameTag != null) {
            processTag(connection, nameTag);
            hoverEventTag.put("name", nameTag);
        }

        final Tag uuidTag = contents.get("id");
        if (uuidTag != null) {
            hoverEventTag.put("uuid", uuidTag);
        }

        final StringTag typeTag = contents.getStringTag("type");
        if (typeTag != null) {
            hoverEventTag.put("id", typeTag);
        }
    }

    public Tag uglyJsonToTag(final UserConnection connection, final String value) {
        // Use the same version for deserializing and serializing, as we handle the remaining changes ourselves
        final Tag contents = SerializerVersion.V1_21_4.toTag(SerializerVersion.V1_21_4.toComponent(value));
        processTag(connection, contents);
        return contents;
    }

    @Override
    protected void handleWrittenBookContents(final UserConnection connection, final CompoundTag tag) {
        final CompoundTag book = TagUtil.getNamespacedCompoundTag(tag, "written_book_content");
        if (book == null) {
            return;
        }

        final ListTag<CompoundTag> pagesTag = book.getListTag("pages", CompoundTag.class);
        if (pagesTag == null) {
            return;
        }

        for (final CompoundTag compoundTag : pagesTag) {
            // To proper nbt
            final String raw = compoundTag.getString("raw");
            compoundTag.put("raw", uglyJsonToTag(connection, raw));

            final String filtered = compoundTag.getString("filtered");
            if (filtered != null) {
                compoundTag.put("filtered", uglyJsonToTag(connection, filtered));
            }
        }
    }
}
