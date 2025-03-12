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
package com.viaversion.viaversion.update;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.util.GsonUtil;
import com.viaversion.viaversion.util.Pair;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class UpdateUtil {

    private static final String PREFIX = "§a§l[ViaVersion] §a";
    private static final String URL = "https://update.viaversion.com/";
    private static final String PLUGIN = "ViaVersion/";

    public static void sendUpdateMessage(final UserConnection connection) {
        Via.getPlatform().runAsync(() -> {
            final Pair<Level, String> message = getUpdateMessage(false);
            if (message != null) {
                Via.getPlatform().runSync(() -> Via.getPlatform().sendMessage(connection, PREFIX + message.value()));
            }
        });
    }

    public static void sendUpdateMessage() {
        Via.getPlatform().runAsync(() -> {
            final Pair<Level, String> message = getUpdateMessage(true);
            if (message != null) {
                Via.getPlatform().runSync(() -> Via.getPlatform().getLogger().log(message.key(), message.value()));
            }
        });
    }

    private static @Nullable Pair<Level, String> getUpdateMessage(boolean console) {
        if (Via.getPlatform().getPluginVersion().equals("${version}")) {
            return new Pair<>(Level.WARNING, "You are using a debug/custom version, consider updating.");
        }

        String newestString;
        try {
            newestString = getNewestVersion();
        } catch (IOException | JsonParseException ignored) {
            return console ? new Pair<>(Level.WARNING, "Could not check for updates, check your connection.") : null;
        }

        Version current;
        try {
            current = new Version(Via.getPlatform().getPluginVersion());
        } catch (IllegalArgumentException e) {
            return new Pair<>(Level.INFO, "You are using a custom version, consider updating.");
        }

        Version newest = new Version(newestString);
        if (current.compareTo(newest) < 0) {
            return new Pair<>(Level.WARNING, "There is a newer plugin version available: " + newest + ", you're on: " + current);
        } else if (console && current.compareTo(newest) != 0) {
            String tag = current.getTag().toLowerCase(Locale.ROOT);
            if (tag.endsWith("dev") || tag.endsWith("snapshot")) {
                return new Pair<>(Level.INFO, "You are running a development version of the plugin, please report any bugs to GitHub.");
            } else {
                return new Pair<>(Level.WARNING, "You are running a newer version of the plugin than is released!");
            }
        }
        return null;
    }

    private static String getNewestVersion() throws IOException {
        URL url = new URL(URL + PLUGIN);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setUseCaches(false);
        connection.addRequestProperty("User-Agent", "ViaVersion " + Via.getPlatform().getPluginVersion() + " " + Via.getPlatform().getPlatformName());
        connection.addRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);

        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String input;
            while ((input = reader.readLine()) != null) {
                builder.append(input);
            }
        }

        JsonObject statistics = GsonUtil.getGson().fromJson(builder.toString(), JsonObject.class);
        return statistics.get("name").getAsString();
    }
}
