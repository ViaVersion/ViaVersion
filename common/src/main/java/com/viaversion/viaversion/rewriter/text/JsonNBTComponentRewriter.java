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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.SerializerVersion;
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
            }
        });
    }

    public void registerPlayerChat(final C packetType, final Type<?> chatType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.UUID); // Sender
            wrapper.passthrough(Types.VAR_INT); // Index
            wrapper.passthrough(Types.OPTIONAL_SIGNATURE_BYTES); // Signature
            wrapper.passthrough(Types.STRING); // Plain content
            wrapper.passthrough(Types.LONG); // Timestamp
            wrapper.passthrough(Types.LONG); // Salt

            final int lastSeen = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < lastSeen; i++) {
                final int index = wrapper.passthrough(Types.VAR_INT);
                if (index == 0) {
                    wrapper.passthrough(Types.SIGNATURE_BYTES);
                }
            }

            processTag(wrapper.user(), wrapper.passthrough(Types.TRUSTED_OPTIONAL_TAG)); // Unsigned content

            final int filterMaskType = wrapper.passthrough(Types.VAR_INT);
            if (filterMaskType == 2) { // Partially filtered
                wrapper.passthrough(Types.LONG_ARRAY_PRIMITIVE); // Mask
            }

            wrapper.passthrough(chatType); // Chat Type
            processTag(wrapper.user(), wrapper.passthrough(Types.TRUSTED_TAG)); // Name
            processTag(wrapper.user(), wrapper.passthrough(Types.TRUSTED_OPTIONAL_TAG)); // Target Name
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
            if (contentsTag != null) {
                final CompoundTag componentsTag = contentsTag.getCompoundTag("components");
                handleShowItem(connection, contentsTag, componentsTag);
            }
        }
    }

    @Override
    protected void handleNestedComponent(final UserConnection connection, @Nullable final Tag tag) {
        if (!(tag instanceof StringTag stringTag)) {
            return;
        }

        // Stored as a json string within a text component...
        final var input = inputSerializerVersion();
        final var output = outputSerializerVersion();

        final Tag asTag = input.toTag(input.toComponent(stringTag.getValue()));
        processTag(connection, asTag);

        stringTag.setValue(output.toString(output.toComponent(asTag)));
    }

    @Override
    protected void handleWrittenBookContents(final UserConnection connection, final CompoundTag tag) {
        if (inputSerializerVersion() != null) {
            super.handleWrittenBookContents(connection, tag);
        }
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

    protected void convertLegacyEntityContents(final CompoundTag hoverEvent) {
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

    protected void convertLegacyItemContents(final CompoundTag hoverEvent) {
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
