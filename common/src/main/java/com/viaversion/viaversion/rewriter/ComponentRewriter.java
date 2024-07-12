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
package com.viaversion.viaversion.rewriter;

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
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.SerializerVersion;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Handles json and tag components, containing methods to override certain parts of the handling.
 * Also contains methods to register a few of the packets using components.
 */
public class ComponentRewriter<C extends ClientboundPacketType> implements com.viaversion.viaversion.api.rewriter.ComponentRewriter {
    protected final Protocol<C, ?, ?, ?> protocol;
    protected final ReadType type;

    public ComponentRewriter(final Protocol<C, ?, ?, ?> protocol, final ReadType type) {
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
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.UUID);
                map(Types.VAR_INT);
                handler(wrapper -> {
                    final int action = wrapper.get(Types.VAR_INT, 0);
                    if (action == 0 || action == 3) {
                        passthroughAndProcess(wrapper);
                    }
                });
            }
        });
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

    public void registerPing() {
        // Always json
        protocol.registerClientbound(State.LOGIN, ClientboundLoginPackets.LOGIN_DISCONNECT, wrapper -> processText(wrapper.user(), wrapper.passthrough(Types.COMPONENT)));
    }

    public void registerLegacyOpenWindow(final C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.UNSIGNED_BYTE); // Id
                map(Types.STRING); // Window Type
                handler(wrapper -> processText(wrapper.user(), wrapper.passthrough(Types.COMPONENT)));
            }
        });
    }

    public void registerOpenScreen(final C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // Id
                map(Types.VAR_INT); // Window Type
                handler(wrapper -> passthroughAndProcess(wrapper));
            }
        });
    }

    public void registerTabList(final C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            passthroughAndProcess(wrapper);
            passthroughAndProcess(wrapper);
        });
    }

    public void registerPlayerCombatKill(final C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // Player ID
                map(Types.INT); // Killer ID
                handler(wrapper -> processText(wrapper.user(), wrapper.passthrough(Types.COMPONENT)));
            }
        });
    }

    public void registerPlayerCombatKill1_20(final C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // Player ID
                handler(wrapper -> passthroughAndProcess(wrapper));
            }
        });
    }

    public void passthroughAndProcess(final PacketWrapper wrapper) {
        switch (type) {
            case JSON:
                processText(wrapper.user(), wrapper.passthrough(Types.COMPONENT));
                break;
            case NBT:
                processTag(wrapper.user(), wrapper.passthrough(Types.TAG));
                break;
        }
    }

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

        final JsonElement hoverEvent = object.get("hoverEvent");
        if (hoverEvent != null && hoverEvent.isJsonObject()) {
            handleHoverEvent(connection, hoverEvent.getAsJsonObject());
        }
    }

    protected void handleTranslate(final JsonObject object, final String translate) {
        // To override if needed
    }

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

        final CompoundTag hoverEvent = tag.getCompoundTag("hoverEvent");
        if (hoverEvent != null) {
            handleHoverEvent(connection, hoverEvent);
        }
    }

    protected void handleTranslate(final UserConnection connection, final CompoundTag parentTag, final StringTag translateTag) {
        // To override if needed
    }

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
            final CompoundTag contents = hoverEventTag.getCompoundTag("contents");
            if (contents != null) {
                processTag(connection, contents.get("name"));
            }
        } else if (action.equals("show_item")) {
            convertLegacyContents(hoverEventTag);

            final CompoundTag contentsTag = hoverEventTag.getCompoundTag("contents");
            if (contentsTag == null) {
                return;
            }

            final CompoundTag componentsTag = contentsTag.getCompoundTag("components");
            if (componentsTag != null) {
                handleShowItem(connection, componentsTag);
            }
        }
    }

    protected void handleShowItem(final UserConnection connection, final CompoundTag componentsTag) {
        // To override if needed
    }

    protected SerializerVersion inputSerializerVersion() {
        return null;
    }

    protected SerializerVersion outputSerializerVersion() {
        return null;
    }

    private void convertLegacyContents(final CompoundTag hoverEvent) {
        if (inputSerializerVersion() == null || outputSerializerVersion() == null) {
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

    public enum ReadType {

        JSON,
        NBT
    }
}
