package us.myles.ViaVersion.api.rewriters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
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
public class ComponentRewriter {

    public JsonElement processText(String value) {
        JsonElement root = GsonUtil.getJsonParser().parse(value);
        processText(root);
        return root;
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
}
