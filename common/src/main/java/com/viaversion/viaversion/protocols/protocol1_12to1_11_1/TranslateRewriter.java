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
package com.viaversion.viaversion.protocols.protocol1_12to1_11_1;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.protocols.protocol1_12to1_11_1.data.AchievementTranslationMapping;
import com.viaversion.viaversion.rewriter.ComponentRewriter;

public class TranslateRewriter {

    private final static ComponentRewriter achievementTextRewriter = new ComponentRewriter() {
        @Override
        protected void handleTranslate(JsonObject object, String translate) {
            String text = AchievementTranslationMapping.get(translate);
            if (text != null) {
                object.addProperty("translate", text);
            }
        }

        @Override
        protected void handleHoverEvent(JsonObject hoverEvent) {
            String action = hoverEvent.getAsJsonPrimitive("action").getAsString();
            if (!action.equals("show_achievement")) {
                super.handleHoverEvent(hoverEvent);
                return;
            }

            String textValue;
            JsonElement value = hoverEvent.get("value");
            if (value.isJsonObject()) {
                textValue = value.getAsJsonObject().get("text").getAsString();
            } else {
                textValue = value.getAsJsonPrimitive().getAsString();
            }

            if (AchievementTranslationMapping.get(textValue) == null) {
                JsonObject invalidText = new JsonObject();
                invalidText.addProperty("text", "Invalid statistic/achievement!");
                invalidText.addProperty("color", "red");
                hoverEvent.addProperty("action", "show_text");
                hoverEvent.add("value", invalidText);
                super.handleHoverEvent(hoverEvent);
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
                    namePart.addProperty("color", AchievementTranslationMapping.isSpecial(textValue) ? "dark_purple" : "green");
                    typePart.addProperty("translate", "stats.tooltip.type.achievement");
                    JsonObject description = new JsonObject();
                    typePart.addProperty("italic", true);
                    description.addProperty("translate", value + ".desc");
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
                Via.getPlatform().getLogger().warning("Error rewriting show_achievement: " + hoverEvent);
                e.printStackTrace();
                JsonObject invalidText = new JsonObject();
                invalidText.addProperty("text", "Invalid statistic/achievement!");
                invalidText.addProperty("color", "red");
                hoverEvent.addProperty("action", "show_text");
                hoverEvent.add("value", invalidText);
            }
            super.handleHoverEvent(hoverEvent);
        }
    };

    public static void toClient(JsonElement element, UserConnection user) {
        if (element instanceof JsonObject) {
            JsonObject obj = (JsonObject) element;
            JsonElement translate = obj.get("translate");
            if (translate != null) {
                if (translate.getAsString().startsWith("chat.type.achievement")) {
                    achievementTextRewriter.processText(obj);
                }
            }
        }
    }

}
