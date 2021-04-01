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
package us.myles.ViaVersion.update;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.Nullable;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.util.GsonUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.UUID;

public class UpdateUtil {

    public static final String PREFIX = ChatColor.GREEN + "" + ChatColor.BOLD + "[ViaVersion] " + ChatColor.GREEN;
    private static final String URL = "https://api.github.com/repos/ViaVersion/ViaVersion/releases/latest";

    public static void sendUpdateMessage(final UUID uuid) {
        Via.getPlatform().runAsync(() -> {
            final String message = getUpdateMessage(false);
            if (message != null) {
                Via.getPlatform().runSync(() -> Via.getPlatform().sendMessage(uuid, PREFIX + message));
            }
        });
    }

    public static void sendUpdateMessage() {
        Via.getPlatform().runAsync(() -> {
            final String message = getUpdateMessage(true);
            if (message != null) {
                Via.getPlatform().runSync(() -> Via.getPlatform().getLogger().warning(message));
            }
        });
    }

    @Nullable
    private static String getUpdateMessage(boolean console) {
        if (Via.getPlatform().getPluginVersion().equals("${project.version}")) {
            return "You are using a debug/custom version, consider updating.";
        }
        String newestString = getNewestVersion();
        if (newestString == null) {
            if (console) {
                return "Could not check for updates, check your connection.";
            } else {
                return null;
            }
        }
        Version current;
        try {
            current = new Version(Via.getPlatform().getPluginVersion());
        } catch (IllegalArgumentException e) {
            return "You are using a custom version, consider updating.";
        }
        Version newest = new Version(newestString);
        if (current.compareTo(newest) < 0)
            return "There is a newer version available: " + newest.toString() + ", you're on: " + current.toString();
        else if (console && current.compareTo(newest) != 0) {
            if (current.getTag().toLowerCase(Locale.ROOT).startsWith("dev") || current.getTag().toLowerCase(Locale.ROOT).startsWith("snapshot")) {
                return "You are running a development version, please report any bugs to GitHub.";
            } else {
                return "You are running a newer version than is released!";
            }
        }
        return null;
    }

    @Nullable
    private static String getNewestVersion() {
        try {
            URL url = new URL(URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(true);
            connection.addRequestProperty("User-Agent", "ViaVersion " + Via.getPlatform().getPluginVersion() + " " + Via.getPlatform().getPlatformName());
            connection.setDoOutput(true);
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String input = br.readLine();
            br.close();
            JsonObject statistics;
            try {
                statistics = GsonUtil.getGson().fromJson(input, JsonObject.class);
            } catch (JsonParseException e) {
                e.printStackTrace();
                return null;
            }
            return statistics.get("tag_name").getAsString();
        } catch (IOException e) {
            return null;
        }
    }
}
