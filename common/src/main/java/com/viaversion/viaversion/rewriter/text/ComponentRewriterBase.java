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
package com.viaversion.viaversion.rewriter.text;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.data.ChatType;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.rewriter.ComponentRewriter;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.TagUtil;
import java.util.BitSet;
import java.util.Collection;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Handles json and tag components, containing methods to override certain parts of the handling.
 * Also contains methods to register a few of the packets using components.
 *
 * @see JsonNBTComponentRewriter
 * @see NBTComponentRewriter
 */
public abstract class ComponentRewriterBase<C extends ClientboundPacketType> implements ComponentRewriter {
    protected final Protocol<C, ?, ?, ?> protocol;
    protected final ReadType type;

    protected ComponentRewriterBase(final Protocol<C, ?, ?, ?> protocol, final ReadType type) {
        this.protocol = protocol;
        this.type = type;
    }

    /**
     * Processes components at the beginning of the packet.
     * Used for packets that have components as their very first value, so no special pre-reading is necessary.
     *
     * @param packetType clientbound packet type
     */
    public void registerComponentPacket(final C packetType) {
        protocol.registerClientbound(packetType, this::passthroughAndProcess);
    }

    public void registerBossEvent(final C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.UUID);
            final int action = wrapper.passthrough(Types.VAR_INT);
            if (action == 0 || action == 3) {
                passthroughAndProcess(wrapper);
            }
        });
    }

    public void registerPing() {
        // Always json
        protocol.registerClientbound(State.LOGIN, ClientboundLoginPackets.LOGIN_DISCONNECT, wrapper -> processText(wrapper.user(), wrapper.passthrough(Types.COMPONENT)));
    }

    public void registerOpenScreen1_14(final C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Id
            wrapper.passthrough(Types.VAR_INT); // Window Type
            passthroughAndProcess(wrapper);
        });
    }

    public void registerTabList(final C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            passthroughAndProcess(wrapper);
            passthroughAndProcess(wrapper);
        });
    }

    public void registerSetObjective(final C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.STRING); // Objective Name
            final byte action = wrapper.passthrough(Types.BYTE);
            if (action == 0 || action == 2) {
                passthroughAndProcess(wrapper); // Display Name
            }
        });
    }

    public void registerSetScore1_20_3(final C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.STRING); // Owner
            wrapper.passthrough(Types.STRING); // Objective name
            wrapper.passthrough(Types.VAR_INT); // Score
            passthroughAndProcessOptional(wrapper);
            if (wrapper.passthrough(Types.BOOLEAN)) {
                final int numberFormatType = wrapper.passthrough(Types.VAR_INT);
                if (numberFormatType == 1) { // styled
                    passthroughAndProcess(wrapper); // Only contains the style
                } else if (numberFormatType == 2) { // fixed
                    passthroughAndProcess(wrapper);
                }
            }
        });
    }

    public void registerSetPlayerTeam1_13(final C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.STRING); // Team Name
            final byte action = wrapper.passthrough(Types.BYTE); // Mode
            if (action == 0 || action == 2) {
                passthroughAndProcess(wrapper); // Display name
                wrapper.passthrough(Types.BYTE); // Flags
                wrapper.passthrough(Types.STRING); // Nametag visibility
                wrapper.passthrough(Types.STRING); // Collision rule
                wrapper.passthrough(Types.VAR_INT); // Color
                passthroughAndProcess(wrapper); // Prefix
                passthroughAndProcess(wrapper); // Suffix
            }
        });
    }

    public void registerSetPlayerTeam1_21_5(final C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.STRING); // Team Name
            final byte action = wrapper.passthrough(Types.BYTE); // Mode
            if (action == 0 || action == 2) {
                passthroughAndProcess(wrapper); // Display name
                wrapper.passthrough(Types.BYTE); // Flags
                wrapper.passthrough(Types.VAR_INT); // Nametag visibility
                wrapper.passthrough(Types.VAR_INT); // Collision rule
                wrapper.passthrough(Types.VAR_INT); // Color
                passthroughAndProcess(wrapper); // Prefix
                passthroughAndProcess(wrapper); // Suffix
            }
        });
    }

    public void registerPlayerInfoUpdate1_21_4(final C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            final BitSet actions = wrapper.passthrough(Types.PROFILE_ACTIONS_ENUM1_21_4);
            if (!actions.get(5)) { // Update display name
                return;
            }

            final int entries = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < entries; i++) {
                wrapper.passthrough(Types.UUID);
                if (actions.get(0)) {
                    wrapper.passthrough(Types.STRING); // Player Name
                    wrapper.passthrough(Types.PROFILE_PROPERTY_ARRAY);
                }
                if (actions.get(1) && wrapper.passthrough(Types.BOOLEAN)) {
                    wrapper.passthrough(Types.UUID); // Session UUID
                    wrapper.passthrough(Types.PROFILE_KEY);
                }
                if (actions.get(2)) {
                    wrapper.passthrough(Types.VAR_INT); // Gamemode
                }
                if (actions.get(3)) {
                    wrapper.passthrough(Types.BOOLEAN); // Listed
                }
                if (actions.get(4)) {
                    wrapper.passthrough(Types.VAR_INT); // Latency
                }

                processTag(wrapper.user(), wrapper.passthrough(Types.TRUSTED_OPTIONAL_TAG));

                if (actions.get(6)) {
                    wrapper.passthrough(Types.VAR_INT); // List order
                }
                if (actions.get(7)) {
                    wrapper.passthrough(Types.BOOLEAN); // Show hat
                }
            }
        });
    }

    public void registerPlayerCombatKill1_20(final C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Player ID
            passthroughAndProcess(wrapper);
        });
    }

    public void registerDisguisedChat(final C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            passthroughAndProcess(wrapper); // Message
            wrapper.passthrough(ChatType.TYPE); // Chat Type
            passthroughAndProcess(wrapper); // Name
            passthroughAndProcessOptional(wrapper); // Target Name
        });
    }

    public void passthroughAndProcess(final PacketWrapper wrapper) {
        switch (type) {
            case JSON -> processText(wrapper.user(), wrapper.passthrough(Types.COMPONENT));
            case NBT -> processTag(wrapper.user(), wrapper.passthrough(Types.TRUSTED_TAG));
        }
    }

    public void passthroughAndProcessOptional(final PacketWrapper wrapper) {
        switch (type) {
            case JSON -> processText(wrapper.user(), wrapper.passthrough(Types.OPTIONAL_COMPONENT));
            case NBT -> processTag(wrapper.user(), wrapper.passthrough(Types.TRUSTED_OPTIONAL_TAG));
        }
    }

    // -----------------------------------------------------------------------
    // Json methods

    @Override
    public JsonElement processText(final UserConnection connection, final String value) {
        try {
            final JsonElement root = JsonParser.parseString(value);
            processText(connection, root);
            return root;
        } catch (final JsonSyntaxException e) {
            if (Via.getManager().isDebug()) {
                protocol.getLogger().severe("Error when trying to parse json: " + value);
                throw e;
            }
            // Yay to malformed json being accepted
            return new JsonPrimitive(value);
        }
    }

    public void processText(final UserConnection connection, final JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return;
        }

        if (element.isJsonArray()) {
            processJsonArray(connection, element.getAsJsonArray());
        } else if (element.isJsonObject()) {
            processJsonObject(connection, element.getAsJsonObject());
        }
    }

    protected void processJsonArray(final UserConnection connection, final JsonArray array) {
        for (final JsonElement jsonElement : array) {
            processText(connection, jsonElement);
        }
    }

    protected void processJsonObject(final UserConnection connection, final JsonObject object) {
        final JsonElement translate = object.get("translate");
        if (translate != null && translate.isJsonPrimitive()) {
            handleTranslate(object, translate.getAsString());

            final JsonElement with = object.get("with");
            if (with != null && with.isJsonArray()) {
                processJsonArray(connection, with.getAsJsonArray());
            }
        }

        final JsonElement extra = object.get("extra");
        if (extra != null && extra.isJsonArray()) {
            processJsonArray(connection, extra.getAsJsonArray());
        }

        final JsonElement hoverEvent = object.get(hoverEventKey());
        if (hoverEvent != null && hoverEvent.isJsonObject()) {
            handleHoverEvent(connection, hoverEvent.getAsJsonObject());
        }
    }

    protected abstract void handleHoverEvent(final UserConnection connection, final JsonObject hoverEvent);

    protected void handleTranslate(final JsonObject object, final String translate) {
        // To override if needed
    }

    // -----------------------------------------------------------------------
    // Tag methods

    @Override
    public void processTag(final UserConnection connection, @Nullable final Tag tag) {
        if (tag == null) {
            return;
        }

        if (tag instanceof ListTag) {
            processListTag(connection, (ListTag<?>) tag);
        } else if (tag instanceof CompoundTag) {
            processCompoundTag(connection, (CompoundTag) tag);
        }
    }

    private void processListTag(final UserConnection connection, final ListTag<?> tag) {
        for (final Tag entry : tag) {
            processTag(connection, entry);
        }
    }

    protected void processCompoundTag(final UserConnection connection, final CompoundTag tag) {
        final StringTag translate = tag.getStringTag("translate");
        if (translate != null) {
            handleTranslate(connection, tag, translate);

            final ListTag<?> with = tag.getListTag("with");
            if (with != null) {
                processListTag(connection, with);
            }
        }

        final ListTag<?> extra = tag.getListTag("extra");
        if (extra != null) {
            processListTag(connection, extra);
        }

        final CompoundTag hoverEvent = tag.getCompoundTag(hoverEventKey());
        if (hoverEvent != null) {
            handleHoverEvent(connection, hoverEvent);
        }
    }

    protected void handleTranslate(final UserConnection connection, final CompoundTag parentTag, final StringTag translateTag) {
        // To override if needed
    }

    protected abstract void handleHoverEvent(final UserConnection connection, final CompoundTag hoverEventTag);

    @Override
    public final void handleShowItem(final UserConnection connection, final CompoundTag itemTag) {
        handleShowItem(connection, itemTag, itemTag.getCompoundTag("components"));
    }

    protected void handleShowItem(final UserConnection connection, final CompoundTag itemTag, @Nullable final CompoundTag componentsTag) {
        final StringTag idTag = itemTag.getStringTag("id");
        final String mappedId = protocol.getMappingData().getFullItemMappings().mappedIdentifier(idTag.getValue());
        if (mappedId != null) {
            idTag.setValue(mappedId);
        }

        if (componentsTag == null) {
            return;
        }

        handleNestedComponent(connection, TagUtil.getNamespacedTag(componentsTag, "item_name"));
        handleNestedComponent(connection, TagUtil.getNamespacedTag(componentsTag, "custom_name"));
        handleLore(connection, componentsTag);
        handleWrittenBookContents(connection, componentsTag);

        handleAttributeModifiers(componentsTag);
        handleContainerContents(connection, componentsTag);
        handleItemArrayContents(connection, componentsTag, "bundle_contents");
        handleItemArrayContents(connection, componentsTag, "charged_projectiles");
        final CompoundTag useRemainder = TagUtil.getNamespacedCompoundTag(componentsTag, "use_remainder");
        if (useRemainder != null) {
            handleShowItem(connection, useRemainder);
        }

        removeDataComponents(componentsTag, "lock", "debug_stick_state");
    }

    protected void handleAttributeModifiers(final CompoundTag tag) {
        if (protocol.getMappingData().getAttributeMappings() == null) {
            return;
        }

        final ListTag<CompoundTag> attributeModifiers = TagUtil.getNamespacedCompoundTagList(tag, "attribute_modifiers");
        if (attributeModifiers == null) {
            return;
        }

        attributeModifiers.getValue().removeIf(attributeTag -> {
            final StringTag typeTag = attributeTag.getStringTag("type");
            if (typeTag == null) {
                return false;
            }

            final String mappedId = protocol.getMappingData().getAttributeMappings().mappedIdentifier(typeTag.getValue());
            if (mappedId != null) {
                typeTag.setValue(mappedId);
                return false;
            }
            return true;
        });
    }

    protected void handleContainerContents(final UserConnection connection, final CompoundTag tag) {
        final ListTag<CompoundTag> container = TagUtil.getNamespacedCompoundTagList(tag, "container");
        if (container == null) {
            return;
        }

        for (final CompoundTag entryTag : container) {
            handleShowItem(connection, entryTag.getCompoundTag("item"));
        }
    }

    protected void handleLore(final UserConnection connection, final CompoundTag tag) {
        final ListTag<? extends Tag> loreTag = TagUtil.getNamespacedTagList(tag, "lore");
        if (loreTag == null) {
            return;
        }

        for (final Tag lore : loreTag) {
            handleNestedComponent(connection, lore);
        }
    }

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
            handleNestedComponent(connection, compoundTag.get("raw"));
            handleNestedComponent(connection, compoundTag.get("filtered"));
        }
    }

    protected void handleItemArrayContents(final UserConnection connection, final CompoundTag tag, final String key) {
        final ListTag<CompoundTag> container = TagUtil.getNamespacedCompoundTagList(tag, key);
        if (container == null) {
            return;
        }

        for (final CompoundTag itemTag : container) {
            handleShowItem(connection, itemTag);
        }
    }

    protected void removeDataComponents(final CompoundTag tag, final Collection<StructuredDataKey<?>> keys) {
        for (final StructuredDataKey<?> key : keys) {
            removeDataComponent(tag, key.identifier());
        }
    }

    protected void removeDataComponents(final CompoundTag tag, final StructuredDataKey<?>... keys) {
        for (final StructuredDataKey<?> key : keys) {
            removeDataComponent(tag, key.identifier());
        }
    }

    protected void removeDataComponents(final CompoundTag tag, final String... keys) {
        for (final String key : keys) {
            removeDataComponent(tag, key);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean removeDataComponent(final CompoundTag tag, final String key) {
        // Check overrides too... continue once one is found
        return TagUtil.removeNamespaced(tag, key)
            || tag.remove("!" + Key.namespaced(key)) != null
            || tag.remove("!" + Key.stripMinecraftNamespace(key)) != null;
    }

    /**
     * Rewrites a nested text component. Stored as a regular tag in 1.21.5+, but is nested via snbt in string components before that.
     *
     * @param connection user connection
     * @param tag        nested tag to handle
     */
    protected abstract void handleNestedComponent(UserConnection connection, @Nullable Tag tag);

    public enum ReadType {

        JSON,
        NBT
    }

    protected abstract String hoverEventKey();
}
