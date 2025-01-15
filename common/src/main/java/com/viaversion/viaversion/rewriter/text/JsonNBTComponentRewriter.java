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
package com.viaversion.viaversion.rewriter.text;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.SerializerVersion;
import com.viaversion.viaversion.util.TagUtil;
import java.util.BitSet;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Rewrites both json and nbt serialized components up to 1.21.5.
 *
 * @param <C> clientbound packet type
 */
public class JsonNBTComponentRewriter<C extends ClientboundPacketType> extends ComponentRewriterBase<C> {

    public JsonNBTComponentRewriter(final Protocol<C, ?, ?, ?> protocol, final ReadType type) {
        super(protocol, type);
    }

    /**
     * Handles sub 1.17 combat event components.
     */
    public void registerPlayerCombat(final C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            if (wrapper.passthrough(Types.VAR_INT) == 2) {
                wrapper.passthrough(Types.VAR_INT); // Player ID
                wrapper.passthrough(Types.INT); // Killer ID
                processText(wrapper.user(), wrapper.passthrough(Types.COMPONENT));
            }
        });
    }

    /**
     * Handles sub 1.17 title components.
     */
    public void registerTitle(final C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            final int action = wrapper.passthrough(Types.VAR_INT);
            if (action >= 0 && action <= 2) {
                processText(wrapper.user(), wrapper.passthrough(Types.COMPONENT));
            }
        });
    }

    public void registerLegacyOpenWindow(final C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.UNSIGNED_BYTE); // Id
            wrapper.passthrough(Types.STRING); // Window Type
            processText(wrapper.user(), wrapper.passthrough(Types.COMPONENT));
        });
    }

    public void registerPlayerCombatKill(final C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Player ID
            wrapper.passthrough(Types.INT); // Killer ID
            processText(wrapper.user(), wrapper.passthrough(Types.COMPONENT));
        });
    }

    public void registerPlayerInfoUpdate1_20_3(final C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            final BitSet actions = wrapper.passthrough(Types.PROFILE_ACTIONS_ENUM1_19_3);
            if (!actions.get(5)) { // Update display name
                return;
            }

            final int entries = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < entries; i++) {
                wrapper.passthrough(Types.UUID);
                if (actions.get(0)) {
                    wrapper.passthrough(Types.STRING); // Player Name

                    final int properties = wrapper.passthrough(Types.VAR_INT);
                    for (int j = 0; j < properties; j++) {
                        wrapper.passthrough(Types.STRING); // Name
                        wrapper.passthrough(Types.STRING); // Value
                        wrapper.passthrough(Types.OPTIONAL_STRING); // Signature
                    }
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
                processTag(wrapper.user(), wrapper.passthrough(Types.OPTIONAL_TAG));
            }
        });
    }

    @Override
    protected void handleHoverEvent(final UserConnection connection, final JsonObject hoverEvent) {
        // To override if needed (don't forget to call super)
        final JsonPrimitive actionElement = hoverEvent.getAsJsonPrimitive("action");
        if (!actionElement.isString()) {
            return;
        }

        final String action = actionElement.getAsString();
        if (action.equals("show_text")) {
            final JsonElement value = hoverEvent.get("value");
            processText(connection, value != null ? value : hoverEvent.get("contents"));
        } else if (action.equals("show_entity")) {
            final JsonElement contents = hoverEvent.get("contents");
            if (contents != null && contents.isJsonObject()) {
                processText(connection, contents.getAsJsonObject().get("name"));
            }
        }
    }

    @Override
    protected void handleHoverEvent(final UserConnection connection, final CompoundTag hoverEventTag) {
        // To override if needed (don't forget to call super)
        final StringTag actionTag = hoverEventTag.getStringTag("action");
        if (actionTag == null) {
            return;
        }

        final String action = actionTag.getValue();
        if (action.equals("show_text")) {
            final Tag value = hoverEventTag.get("value");
            processTag(connection, value != null ? value : hoverEventTag.get("contents"));
        } else if (action.equals("show_entity")) {
            convertLegacyEntityContents(hoverEventTag);

            final CompoundTag contents = hoverEventTag.getCompoundTag("contents");
            if (contents != null) {
                processTag(connection, contents.get("name"));

                final StringTag typeTag = contents.getStringTag("type");
                if (typeTag != null && protocol.getEntityRewriter() != null) {
                    typeTag.setValue(protocol.getEntityRewriter().mappedEntityIdentifier(typeTag.getValue()));
                }
            }
        } else if (action.equals("show_item")) {
            convertLegacyItemContents(hoverEventTag);

            final CompoundTag contentsTag = hoverEventTag.getCompoundTag("contents");
            if (contentsTag == null) {
                return;
            }

            final CompoundTag componentsTag = contentsTag.getCompoundTag("components");
            handleShowItem(connection, contentsTag, componentsTag);
            if (componentsTag != null) {
                final CompoundTag useRemainder = TagUtil.getNamespacedCompoundTag(componentsTag, "use_remainder");
                if (useRemainder != null) {
                    handleShowItem(connection, useRemainder);
                }
                handleContainerContents(connection, componentsTag);
                if (inputSerializerVersion() != null) {
                    handleWrittenBookContents(connection, componentsTag);
                }

                handleItemArrayContents(connection, componentsTag, "bundle_contents");
                handleItemArrayContents(connection, componentsTag, "charged_projectiles");
            }
        }
    }

    @Override
    protected void handleNestedComponent(final UserConnection connection, final CompoundTag parent, final String key) {
        final StringTag tag = parent.getStringTag(key);
        if (tag == null) {
            return;
        }

        // Stored as a json string within a text component...
        final var input = inputSerializerVersion();
        final var output = outputSerializerVersion();

        final Tag asTag = input.toTag(input.toComponent(tag.getValue()));
        processTag(connection, asTag);

        tag.setValue(output.toString(output.toComponent(asTag)));
    }

    @Override
    protected String hoverEventKey() {
        return "hoverEvent";
    }

    protected @Nullable SerializerVersion inputSerializerVersion() {
        return null;
    }

    protected @Nullable SerializerVersion outputSerializerVersion() {
        return inputSerializerVersion(); // Only matters if the nbt serializer changed
    }

    private void convertLegacyEntityContents(final CompoundTag hoverEvent) {
        if (inputSerializerVersion() == null) {
            return;
        }

        final Tag valueTag = hoverEvent.remove("value");
        if (valueTag != null) {
            final CompoundTag tag = ComponentUtil.deserializeShowItem(valueTag, inputSerializerVersion());
            final CompoundTag contentsTag = new CompoundTag();
            contentsTag.put("type", tag.getStringTag("type"));
            contentsTag.put("id", tag.getStringTag("id"));
            contentsTag.put("name", outputSerializerVersion().toTag(outputSerializerVersion().toComponent(tag.getString("name"))));
            hoverEvent.put("contents", contentsTag);
        }
    }

    private void convertLegacyItemContents(final CompoundTag hoverEvent) {
        if (inputSerializerVersion() == null) {
            return;
        }

        final Tag valueTag = hoverEvent.remove("value");
        if (valueTag != null) {
            final CompoundTag tag = ComponentUtil.deserializeShowItem(valueTag, inputSerializerVersion());
            final CompoundTag contentsTag = new CompoundTag();
            contentsTag.put("id", tag.getStringTag("id"));
            contentsTag.put("count", tag.getIntTag("count"));
            if (tag.get("tag") instanceof CompoundTag) {
                contentsTag.putString("tag", outputSerializerVersion().toSNBT(tag.getCompoundTag("tag")));
            }
            hoverEvent.put("contents", contentsTag);
        }
    }
}
