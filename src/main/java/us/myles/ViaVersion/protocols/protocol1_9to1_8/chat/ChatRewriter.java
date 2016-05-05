package us.myles.ViaVersion.protocols.protocol1_9to1_8.chat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.EntityTracker;

public class ChatRewriter {
    public static void toClient(JsonObject obj, UserConnection user) {
        //Check gamemode change
        if (obj.get("translate") != null && obj.get("translate").getAsString().equals("gameMode.changed")) {
            String gameMode = user.get(EntityTracker.class).getGameMode().getText();

            JsonObject gameModeObject = new JsonObject();
            gameModeObject.addProperty("text", gameMode);
            gameModeObject.addProperty("color", "gray");
            gameModeObject.addProperty("italic", true);

            JsonArray array = new JsonArray();
            array.add(gameModeObject);

            obj.add("with", array);
        }
    }
}
