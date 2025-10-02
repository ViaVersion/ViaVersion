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

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.util.GsonUtil;
import com.viaversion.viaversion.util.HttpClientUtil;
import com.viaversion.viaversion.util.Pair;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class UpdateUtil {

    private static final String PREFIX = "§a§l[ViaVersion] §a";
    private static final String URL = "https://update.viaversion.com/";
    private static final String PLUGIN = "ViaVersion/";

    public static void sendUpdateMessage(final UserConnection connection) {
        getUpdateMessage(false).thenAcceptAsync(message -> {
            if (message != null) {
                Via.getPlatform().sendMessage(connection, PREFIX + message.value());
            }
        }, Via.getPlatform());
    }

    public static void sendUpdateMessage() {
        getUpdateMessage(true).thenAcceptAsync(message -> {
            if (message != null) {
                Via.getPlatform().getLogger().log(message.key(), message.value());
            }
        }, Via.getPlatform());
    }

    private static CompletableFuture<@Nullable Pair<Level, String>> getUpdateMessage(boolean console) {
        if (Via.getPlatform().getPluginVersion().equals("${version}")) {
            return CompletableFuture.completedFuture(
                new Pair<>(Level.WARNING, "You are using a debug/custom version, consider updating.")
            );
        }

        return getNewestVersion().exceptionally(ex -> null)
            .thenApply(newestString -> {
                if (newestString == null) {
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
            });
    }

    private static CompletableFuture<String> getNewestVersion() {
        final HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(URL + PLUGIN))
            .header("User-Agent", "ViaVersion " + Via.getPlatform().getPluginVersion() + " " + Via.getPlatform().getPlatformName())
            .header("Accept", "application/json")
            .GET()
            .build();

        return HttpClientUtil.get(Via.getPlatform())
            .sendAsync(request, HttpClientUtil.jsonResponse(GsonUtil.getGson()))
            .thenApplyAsync(response -> response.body().getAsJsonObject().get("name").getAsString());
    }
}
