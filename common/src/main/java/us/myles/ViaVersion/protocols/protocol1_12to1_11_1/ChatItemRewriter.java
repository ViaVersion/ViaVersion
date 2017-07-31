package us.myles.ViaVersion.protocols.protocol1_12to1_11_1;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import us.myles.ViaVersion.api.data.UserConnection;

import java.util.regex.Pattern;

public class ChatItemRewriter {
    private static Pattern indexRemoval = Pattern.compile("\\d+:(?=([^\"\\\\]*(\\\\.|\"([^\"\\\\]*\\\\.)*[^\"\\\\]*\"))*[^\"]*$)");
    // Taken from https://stackoverflow.com/questions/6462578/alternative-to-regex-match-all-instances-not-inside-quotes

    public static void toClient(JsonElement element, UserConnection user) {
        if (element instanceof JsonObject) {
            JsonObject obj = (JsonObject) element;
            if (obj.has("hoverEvent")) {
                if (obj.get("hoverEvent") instanceof JsonObject) {
                    JsonObject hoverEvent = (JsonObject) obj.get("hoverEvent");
                    if (hoverEvent.has("action") && hoverEvent.has("value")) {
                        String type = hoverEvent.get("action").getAsString();
                        if (type.equals("show_item") || type.equals("show_entity")) {
                            String value = hoverEvent.get("value").getAsString();
                            value = indexRemoval.matcher(value).replaceAll("");
                            hoverEvent.addProperty("value", value);
                        }
                    }
                }
            } else {
                if (obj.has("extra")) {
                    toClient(obj.get("extra"), user);
                }
            }
        }
        if (element instanceof JsonArray) {
            JsonArray array = (JsonArray) element;
            for (JsonElement value : array) {
                toClient(value, user);
            }
        }
    }
}
