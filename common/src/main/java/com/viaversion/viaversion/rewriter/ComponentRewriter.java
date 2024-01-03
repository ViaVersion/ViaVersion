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

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Handles json chat components, containing methods to override certain parts of the handling.
 * Also contains methods to register a few of the packets using components.
 */
public class ComponentRewriter<C extends ClientboundPacketType> {
    protected final Protocol<C, ?, ?, ?> protocol;
    protected final ReadType type;

    @Deprecated/*(forRemoval = true)*/
    public ComponentRewriter(final Protocol<C, ?, ?, ?> protocol) {
        this(protocol, ReadType.JSON);
    }

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

    public void registerBossBar(final C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UUID);
                map(Type.VAR_INT);
                handler(wrapper -> {
                    final int action = wrapper.get(Type.VAR_INT, 0);
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
    public void registerCombatEvent(final C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            if (wrapper.passthrough(Type.VAR_INT) == 2) {
                wrapper.passthrough(Type.VAR_INT);
                wrapper.passthrough(Type.INT);
                processText(wrapper.passthrough(Type.COMPONENT));
            }
        });
    }

    /**
     * Handles sub 1.17 title components.
     */
    public void registerTitle(final C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            final int action = wrapper.passthrough(Type.VAR_INT);
            if (action >= 0 && action <= 2) {
                processText(wrapper.passthrough(Type.COMPONENT));
            }
        });
    }

    public void registerPing() {
        // Always json
        protocol.registerClientbound(State.LOGIN, ClientboundLoginPackets.LOGIN_DISCONNECT, wrapper -> processText(wrapper.passthrough(Type.COMPONENT)));
    }

    public void registerLegacyOpenWindow(final C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UNSIGNED_BYTE); // Id
                map(Type.STRING); // Window Type
                handler(wrapper -> processText(wrapper.passthrough(Type.COMPONENT)));
            }
        });
    }

    public void registerOpenWindow(final C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Id
                map(Type.VAR_INT); // Window Type
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

    public void registerCombatKill(final C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT);
                map(Type.INT);
                handler(wrapper -> processText(wrapper.passthrough(Type.COMPONENT)));
            }
        });
    }

    public void registerCombatKill1_20(final C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Duration
                handler(wrapper -> passthroughAndProcess(wrapper));
            }
        });
    }

    public void passthroughAndProcess(final PacketWrapper wrapper) throws Exception {
        switch (type) {
            case JSON:
                processText(wrapper.passthrough(Type.COMPONENT));
                break;
            case NBT:
                processTag(wrapper.passthrough(Type.TAG));
                break;
        }
    }

    public JsonElement processText(final String value) {
        try {
            final JsonElement root = JsonParser.parseString(value);
            processText(root);
            return root;
        } catch (final JsonSyntaxException e) {
            if (Via.getManager().isDebug()) {
                Via.getPlatform().getLogger().severe("Error when trying to parse json: " + value);
                throw e;
            }
            // Yay to malformed json being accepted
            return new JsonPrimitive(value);
        }
    }

    public void processText(final JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return;
        }

        if (element.isJsonArray()) {
            processJsonArray(element.getAsJsonArray());
        } else if (element.isJsonObject()) {
            processJsonObject(element.getAsJsonObject());
        }
    }

    protected void processJsonArray(final JsonArray array) {
        for (final JsonElement jsonElement : array) {
            processText(jsonElement);
        }
    }

    protected void processJsonObject(final JsonObject object) {
        final JsonElement translate = object.get("translate");
        if (translate != null && translate.isJsonPrimitive()) {
            handleTranslate(object, translate.getAsString());

            final JsonElement with = object.get("with");
            if (with != null && with.isJsonArray()) {
                processJsonArray(with.getAsJsonArray());
            }
        }

        final JsonElement extra = object.get("extra");
        if (extra != null && extra.isJsonArray()) {
            processJsonArray(extra.getAsJsonArray());
        }

        final JsonElement hoverEvent = object.get("hoverEvent");
        if (hoverEvent != null && hoverEvent.isJsonObject()) {
            handleHoverEvent(hoverEvent.getAsJsonObject());
        }
    }

    protected void handleTranslate(final JsonObject object, final String translate) {
        // To override if needed
    }

    protected void handleHoverEvent(final JsonObject hoverEvent) {
        // To override if needed (don't forget to call super)
        final JsonPrimitive actionElement = hoverEvent.getAsJsonPrimitive("action");
        if (!actionElement.isString()) {
            return;
        }

        final String action = actionElement.getAsString();
        if (action.equals("show_text")) {
            final JsonElement value = hoverEvent.get("value");
            processText(value != null ? value : hoverEvent.get("contents"));
        } else if (action.equals("show_entity")) {
            final JsonElement contents = hoverEvent.get("contents");
            if (contents != null && contents.isJsonObject()) {
                processText(contents.getAsJsonObject().get("name"));
            }
        }
    }

    // -----------------------------------------------------------------------
    // Tag methods

    public void processTag(@Nullable final Tag tag) {
        if (tag == null) {
            return;
        }

        if (tag instanceof ListTag) {
            processListTag((ListTag) tag);
        } else if (tag instanceof CompoundTag) {
            processCompoundTag((CompoundTag) tag);
        }
    }

    private void processListTag(final ListTag tag) {
        for (final Tag entry : tag) {
            processTag(entry);
        }
    }

    protected void processCompoundTag(final CompoundTag tag) {
        final Tag translate = tag.get("translate");
        if (translate instanceof StringTag) {
            handleTranslate(tag, ((StringTag) translate));

            final Tag with = tag.get("with");
            if (with instanceof ListTag) {
                processListTag((ListTag) with);
            }
        }

        final Tag extra = tag.get("extra");
        if (extra instanceof ListTag) {
            processListTag((ListTag) extra);
        }

        final Tag hoverEvent = tag.get("hoverEvent");
        if (hoverEvent instanceof CompoundTag) {
            handleHoverEvent((CompoundTag) hoverEvent);
        }
    }

    protected void handleTranslate(final CompoundTag parentTag, final StringTag translateTag) {
        // To override if needed
    }

    protected void handleHoverEvent(final CompoundTag hoverEventTag) {
        // To override if needed (don't forget to call super)
        final Tag actionTag = hoverEventTag.get("action");
        if (!(actionTag instanceof StringTag)) {
            return;
        }

        final String action = ((StringTag) actionTag).getValue();
        if (action.equals("show_text")) {
            final Tag value = hoverEventTag.get("value");
            processTag(value != null ? value : hoverEventTag.get("contents"));
        } else if (action.equals("show_entity")) {
            final Tag contents = hoverEventTag.get("contents");
            if (contents instanceof CompoundTag) {
                processTag(((CompoundTag) contents).get("name"));
            }
        }
    }

    public enum ReadType {

        JSON,
        NBT
    }
}
