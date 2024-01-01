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
package com.viaversion.viaversion.protocols.protocol1_9to1_8.chat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.storage.EntityTracker1_9;

public class ChatRewriter {
    /**
     * Rewrite chat being sent to the client so that gamemode issues don't occur.
     *
     * @param obj  The json object being sent by the server
     * @param user The player involved. (Required for Gamemode info)
     */
    public static void toClient(JsonObject obj, UserConnection user) {
        //Check gamemode change
        if (obj.get("translate") != null && obj.get("translate").getAsString().equals("gameMode.changed")) {
            EntityTracker1_9 tracker = user.getEntityTracker(Protocol1_9To1_8.class);
            String gameMode = tracker.getGameMode().getText();

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
