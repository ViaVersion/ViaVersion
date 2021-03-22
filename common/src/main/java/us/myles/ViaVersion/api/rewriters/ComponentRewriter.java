/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
package us.myles.ViaVersion.api.rewriters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.protocol.ClientboundPacketType;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.util.GsonUtil;

// Packets using components:
// ping (status)
// disconnect (play and login)
// chat
// bossbar
// open window
// combat event
// title
// tablist
// teams
// scoreboard
// player info
// map data
// declare commands
// advancements
// update sign

/**
 * Handles json chat components, containing methods to override certain parts of the handling.
 * Also contains methods to register a few of the packets using components.
 */
public class ComponentRewriter {
    protected final Protocol protocol;

    public ComponentRewriter(Protocol protocol) {
        this.protocol = protocol;
    }

    /**
     * Use empty constructor if no packet registering is needed.
     */
    public ComponentRewriter() {
        this.protocol = null;
    }

    public void registerChatMessage(ClientboundPacketType packetType) {
        protocol.registerOutgoing(packetType, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> processText(wrapper.passthrough(Type.COMPONENT)));
            }
        });
    }

    public void registerBossBar(ClientboundPacketType packetType) {
        protocol.registerOutgoing(packetType, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.UUID);
                map(Type.VAR_INT);
                handler(wrapper -> {
                    int action = wrapper.get(Type.VAR_INT, 0);
                    if (action == 0 || action == 3) {
                        processText(wrapper.passthrough(Type.COMPONENT));
                    }
                });
            }
        });
    }

    public void registerCombatEvent(ClientboundPacketType packetType) {
        protocol.registerOutgoing(packetType, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    if (wrapper.passthrough(Type.VAR_INT) == 2) {
                        wrapper.passthrough(Type.VAR_INT);
                        wrapper.passthrough(Type.INT);
                        processText(wrapper.passthrough(Type.COMPONENT));
                    }
                });
            }
        });
    }

    public void registerTitle(ClientboundPacketType packetType) {
        protocol.registerOutgoing(packetType, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    int action = wrapper.passthrough(Type.VAR_INT);
                    if (action >= 0 && action <= 2) {
                        processText(wrapper.passthrough(Type.COMPONENT));
                    }
                });
            }
        });
    }

    public JsonElement processText(String value) {
        try {
            JsonElement root = GsonUtil.getJsonParser().parse(value);
            processText(root);
            return root;
        } catch (JsonSyntaxException e) {
            if (Via.getManager().isDebug()) {
                Via.getPlatform().getLogger().severe("Error when trying to parse json: " + value);
                throw e;
            }
            // Yay to malformed json being accepted
            return new JsonPrimitive(value);
        }
    }

    public void processText(JsonElement element) {
        if (element == null || element.isJsonNull()) return;
        if (element.isJsonArray()) {
            processAsArray(element);
            return;
        }
        if (element.isJsonPrimitive()) {
            handleText(element.getAsJsonPrimitive());
            return;
        }

        JsonObject object = element.getAsJsonObject();
        JsonPrimitive text = object.getAsJsonPrimitive("text");
        if (text != null) {
            handleText(text);
        }

        JsonElement translate = object.get("translate");
        if (translate != null) {
            handleTranslate(object, translate.getAsString());

            JsonElement with = object.get("with");
            if (with != null) {
                processAsArray(with);
            }
        }

        JsonElement extra = object.get("extra");
        if (extra != null) {
            processAsArray(extra);
        }

        JsonObject hoverEvent = object.getAsJsonObject("hoverEvent");
        if (hoverEvent != null) {
            handleHoverEvent(hoverEvent);
        }
    }

    protected void handleText(JsonPrimitive text) {
        // To override if needed
    }

    protected void handleTranslate(JsonObject object, String translate) {
        // To override if needed
    }

    // To override if needed (don't forget to call super if needed)
    protected void handleHoverEvent(JsonObject hoverEvent) {
        String action = hoverEvent.getAsJsonPrimitive("action").getAsString();
        if (action.equals("show_text")) {
            JsonElement value = hoverEvent.get("value");
            processText(value != null ? value : hoverEvent.get("contents"));
        } else if (action.equals("show_entity")) {
            JsonObject contents = hoverEvent.getAsJsonObject("contents");
            if (contents != null) {
                processText(contents.get("name"));
            }
        }
    }

    private void processAsArray(JsonElement element) {
        for (JsonElement jsonElement : element.getAsJsonArray()) {
            processText(jsonElement);
        }
    }

    public <T extends Protocol> T getProtocol() {
        return (T) protocol;
    }
}
