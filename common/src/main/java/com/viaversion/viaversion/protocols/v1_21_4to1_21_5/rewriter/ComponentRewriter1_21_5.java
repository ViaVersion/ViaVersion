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
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.Protocol1_21_4To1_21_5;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPacket1_21_2;
import com.viaversion.viaversion.rewriter.ComponentRewriter;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.SerializerVersion;
import com.viaversion.viaversion.util.TagUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public final class ComponentRewriter1_21_5 extends ComponentRewriter<ClientboundPacket1_21_2> {

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
            hoverEventTag.put("text", contents);
            return;
        }

        // Move to text field
        final Tag contents = hoverEventTag.remove("contents");
        processTag(connection, contents);
        hoverEventTag.put("text", contents);
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

            if (componentsTag == null) {
                return;
            }

            hoverEventTag.put("components", componentsTag);

            final CompoundTag useRemainder = TagUtil.getNamespacedCompoundTag(componentsTag, "use_remainder");
            if (useRemainder != null) {
                handleShowItem(connection, useRemainder);
            }
            handleContainerContents(connection, componentsTag);
            handleItemArrayContents(connection, componentsTag, "bundle_contents");
            handleItemArrayContents(connection, componentsTag, "charged_projectiles");
            handleWrittenBookContents(connection, componentsTag);

            updateUglyJson(componentsTag, connection);
        } else if (contentsTag instanceof final StringTag inlinedContents) {
            hoverEventTag.put("id", inlinedContents);
        }
    }

    private void updateUglyJson(final CompoundTag componentsTag, final UserConnection connection) {
        updateUglyJson(componentsTag, "item_name", connection);
        updateUglyJson(componentsTag, "custom_name", connection);

        final String loreKey = componentsTag.contains("lore") ? "lore" : "minecraft:lore";
        final ListTag<StringTag> lore = componentsTag.getListTag(loreKey, StringTag.class);
        if (lore == null) {
            return;
        }

        final ListTag<CompoundTag> updatedLore = new ListTag<>(CompoundTag.class);
        componentsTag.put(loreKey, updatedLore);
        for (final StringTag line : lore) {
            // Convert and make sure they're all of the same type
            final Tag output = uglyJsonToTag(connection, line.getValue());
            final CompoundTag wrappedComponent = new CompoundTag();
            wrappedComponent.putString("text", "");
            wrappedComponent.put("extra", new ListTag<>(List.of(output)));
            updatedLore.add(wrappedComponent);
        }
    }

    private void updateUglyJson(final CompoundTag componentsTag, final String key, final UserConnection connection) {
        String actualKey = Key.namespaced(key);
        String json = componentsTag.getString(actualKey);
        if (json == null) {
            actualKey = Key.stripMinecraftNamespace(key);
            json = componentsTag.getString(actualKey);

            if (json == null) {
                return;
            }
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

    private Tag uglyJsonToTag(final UserConnection connection, final String value) {
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
            if (compoundTag.remove("raw") instanceof final StringTag raw) {
                compoundTag.put("raw", uglyJsonToTag(connection, raw.getValue()));
            }
            if (compoundTag.remove("filtered") instanceof final StringTag raw) {
                compoundTag.put("filtered", uglyJsonToTag(connection, raw.getValue()));
            }
        }
    }
}
