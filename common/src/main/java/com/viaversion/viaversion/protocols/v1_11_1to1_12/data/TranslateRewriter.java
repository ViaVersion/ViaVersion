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
package com.viaversion.viaversion.protocols.v1_11_1to1_12.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.packet.ClientboundPackets1_9_3;
import com.viaversion.viaversion.rewriter.text.JsonNBTComponentRewriter;
import com.viaversion.viaversion.util.SerializerVersion;
import com.viaversion.viaversion.util.StringUtil;
import java.util.logging.Level;

public class TranslateRewriter {

    private static final JsonNBTComponentRewriter<ClientboundPackets1_9_3> ACHIEVEMENT_TEXT_REWRITER = new JsonNBTComponentRewriter<>(null, JsonNBTComponentRewriter.ReadType.JSON) {
        @Override
        protected void handleTranslate(JsonObject object, String translate) {
            String text = AchievementTranslations1_12.get(translate);
            if (text != null) {
                object.addProperty("translate", text);
            }
        }

        @Override
        protected void handleHoverEvent(UserConnection connection, JsonObject hoverEvent) {
            String action = hoverEvent.getAsJsonPrimitive("action").getAsString();
            if (!action.equals("show_achievement")) {
                super.handleHoverEvent(connection, hoverEvent);
                return;
            }

            String textValue = SerializerVersion.V1_9.toComponent(hoverEvent.get("value")).asUnformattedString();

            if (AchievementTranslations1_12.get(textValue) == null) {
                JsonObject invalidText = new JsonObject();
                invalidText.addProperty("text", "Invalid statistic/achievement!");
                invalidText.addProperty("color", "red");
                hoverEvent.addProperty("action", "show_text");
                hoverEvent.add("value", invalidText);
                super.handleHoverEvent(connection, hoverEvent);
                return;
            }

            try {
                JsonObject newLine = new JsonObject();
                newLine.addProperty("text", "\n");
                JsonArray baseArray = new JsonArray();
                baseArray.add("");
                JsonObject namePart = new JsonObject();
                JsonObject typePart = new JsonObject();
                baseArray.add(namePart);
                baseArray.add(newLine);
                baseArray.add(typePart);
                if (textValue.startsWith("achievement")) {
                    namePart.addProperty("translate", textValue);
                    namePart.addProperty("color", AchievementTranslations1_12.isSpecial(textValue) ? "dark_purple" : "green");
                    typePart.addProperty("translate", "stats.tooltip.type.achievement");
                    JsonObject description = new JsonObject();
                    typePart.addProperty("italic", true);
                    description.addProperty("translate", textValue + ".desc");
                    baseArray.add(newLine);
                    baseArray.add(description);
                } else if (textValue.startsWith("stat")) {
                    namePart.addProperty("translate", textValue);
                    namePart.addProperty("color", "gray");
                    typePart.addProperty("translate", "stats.tooltip.type.statistic");
                    typePart.addProperty("italic", true);
                }
                hoverEvent.addProperty("action", "show_text");
                hoverEvent.add("value", baseArray);
            } catch (Exception e) {
                if (!Via.getConfig().isSuppressConversionWarnings()) {
                    protocol.getLogger().log(Level.WARNING, "Error rewriting show_achievement: " + StringUtil.forLogging(hoverEvent), e);
                }
                JsonObject invalidText = new JsonObject();
                invalidText.addProperty("text", "Invalid statistic/achievement!");
                invalidText.addProperty("color", "red");
                hoverEvent.addProperty("action", "show_text");
                hoverEvent.add("value", invalidText);
            }
            super.handleHoverEvent(connection, hoverEvent);
        }
    };

    public static void toClient(UserConnection connection, JsonElement element) {
        if (element instanceof JsonObject obj) {
            JsonElement translate = obj.get("translate");
            if (translate != null && translate.getAsString().startsWith("chat.type.achievement")) {
                ACHIEVEMENT_TEXT_REWRITER.processText(connection, obj);
            }
        }
    }
}
