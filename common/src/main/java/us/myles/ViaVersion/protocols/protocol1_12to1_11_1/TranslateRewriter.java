package us.myles.ViaVersion.protocols.protocol1_12to1_11_1;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import us.myles.ViaVersion.api.data.UserConnection;

public class TranslateRewriter {
    public static boolean toClient(JsonElement element, UserConnection user) {
        if (element instanceof JsonObject) {
            JsonObject obj = (JsonObject) element;
            if (obj.has("translate")) {
                if (obj.get("translate").getAsString().equals("chat.type.achievement")) {
                    return false;
                }
            }
        }
        return true;
    }
}
