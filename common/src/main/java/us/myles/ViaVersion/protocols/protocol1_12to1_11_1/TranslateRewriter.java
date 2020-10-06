package us.myles.ViaVersion.protocols.protocol1_12to1_11_1;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.rewriters.ComponentRewriter;
import us.myles.ViaVersion.protocols.protocol1_12to1_11_1.data.AchievementTranslationMapping;

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

            String value;
            if (!hoverEvent.get("value").isJsonPrimitive()) {
                value = hoverEvent.getAsJsonObject("value").get("text").getAsString();
            } else {
                value = hoverEvent.getAsJsonPrimitive("value").getAsString();
            }

            if (AchievementTranslationMapping.get(value) == null) {
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
                if (value.startsWith("achievement")) {
                    namePart.addProperty("translate", value);
                    namePart.addProperty("color", AchievementTranslationMapping.isSpecial(value) ? "dark_purple" : "green");
                    typePart.addProperty("translate", "stats.tooltip.type.achievement");
                    JsonObject description = new JsonObject();
                    typePart.addProperty("italic", true);
                    description.addProperty("translate", value + ".desc");
                    baseArray.add(newLine);
                    baseArray.add(description);
                } else if (value.startsWith("stat")) {
                    namePart.addProperty("translate", value);
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
