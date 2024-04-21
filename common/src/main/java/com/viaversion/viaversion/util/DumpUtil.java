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
package com.viaversion.viaversion.util;

import com.google.common.io.CharStreams;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.ViaPlatform;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.dump.DumpTemplate;
import com.viaversion.viaversion.dump.VersionInfo;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InvalidObjectException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class DumpUtil {

    /**
     * Creates a platform dump and posts it to ViaVersion's dump server asynchronously.
     * May complete exceptionally with {@link DumpException}.
     *
     * @param playerToSample uuid of the player to include the pipeline of
     * @return completable future that completes with the url of the dump
     */
    public static CompletableFuture<String> postDump(@Nullable final UUID playerToSample) {
        final ProtocolVersion protocolVersion = Via.getAPI().getServerVersion().lowestSupportedProtocolVersion();
        final ViaPlatform<?> platform = Via.getPlatform();
        final VersionInfo version = new VersionInfo(
                System.getProperty("java.version"),
                System.getProperty("os.name"),
                protocolVersion.getVersionType(),
                protocolVersion.getVersion(),
                protocolVersion.getName(),
                Via.getManager().getProtocolManager().getSupportedVersions().stream().map(ProtocolVersion::toString).collect(Collectors.toCollection(LinkedHashSet::new)),
                platform.getPlatformName(),
                platform.getPlatformVersion(),
                platform.getPluginVersion(),
                com.viaversion.viaversion.util.VersionInfo.getImplementationVersion(),
                Via.getManager().getSubPlatforms()
        );
        final Map<String, Object> configuration = ((Config) Via.getConfig()).getValues();
        final DumpTemplate template = new DumpTemplate(version, configuration, platform.getDump(), Via.getManager().getInjector().getDump(), getPlayerSample(playerToSample));
        final CompletableFuture<String> result = new CompletableFuture<>();
        platform.runAsync(() -> {
            final HttpURLConnection con;
            try {
                con = (HttpURLConnection) new URL("https://dump.viaversion.com/documents").openConnection();
            } catch (final IOException e) {
                platform.getLogger().log(Level.SEVERE, "Error when opening connection to ViaVersion dump service", e);
                result.completeExceptionally(new DumpException(DumpErrorType.CONNECTION, e));
                return;
            }

            try {
                con.setRequestProperty("Content-Type", "application/json");
                con.addRequestProperty("User-Agent", "ViaVersion-" + platform.getPlatformName() + "/" + version.getPluginVersion());
                con.setRequestMethod("POST");
                con.setDoOutput(true);

                try (final OutputStream out = con.getOutputStream()) {
                    out.write(new GsonBuilder().setPrettyPrinting().create().toJson(template).getBytes(StandardCharsets.UTF_8));
                }

                if (con.getResponseCode() == 429) {
                    result.completeExceptionally(new DumpException(DumpErrorType.RATE_LIMITED));
                    return;
                }

                final String rawOutput;
                try (final InputStream inputStream = con.getInputStream()) {
                    rawOutput = CharStreams.toString(new InputStreamReader(inputStream));
                }

                final JsonObject output = GsonUtil.getGson().fromJson(rawOutput, JsonObject.class);
                if (!output.has("key")) {
                    throw new InvalidObjectException("Key is not given in Hastebin output");
                }

                result.complete(urlForId(output.get("key").getAsString()));
            } catch (final Exception e) {
                platform.getLogger().log(Level.SEVERE, "Error when posting ViaVersion dump", e);
                result.completeExceptionally(new DumpException(DumpErrorType.POST, e));
                printFailureInfo(con);
            }
        });
        return result;
    }

    private static void printFailureInfo(final HttpURLConnection connection) {
        try {
            if (connection.getResponseCode() < 200 || connection.getResponseCode() > 400) {
                try (final InputStream errorStream = connection.getErrorStream()) {
                    final String rawOutput = CharStreams.toString(new InputStreamReader(errorStream));
                    Via.getPlatform().getLogger().log(Level.SEVERE, "Page returned: " + rawOutput);
                }
            }
        } catch (final IOException e) {
            Via.getPlatform().getLogger().log(Level.SEVERE, "Failed to capture further info", e);
        }
    }

    public static String urlForId(final String id) {
        return String.format("https://dump.viaversion.com/%s", id);
    }

    private static JsonObject getPlayerSample(@Nullable final UUID uuid) {
        final JsonObject playerSample = new JsonObject();
        // Player versions
        final JsonObject versions = new JsonObject();
        playerSample.add("versions", versions);
        final Map<ProtocolVersion, Integer> playerVersions = new TreeMap<>(ProtocolVersion::compareTo);
        for (final UserConnection connection : Via.getManager().getConnectionManager().getConnections()) {
            final ProtocolVersion protocolVersion = connection.getProtocolInfo().protocolVersion();
            playerVersions.compute(protocolVersion, (v, num) -> num != null ? num + 1 : 1);
        }
        for (final Map.Entry<ProtocolVersion, Integer> entry : playerVersions.entrySet()) {
            versions.addProperty(entry.getKey().getName(), entry.getValue());
        }

        final Set<List<String>> pipelines = new HashSet<>();
        if (uuid != null) {
            // Pipeline of sender
            final UserConnection senderConnection = Via.getAPI().getConnection(uuid);
            if (senderConnection != null && senderConnection.getChannel() != null) {
                pipelines.add(senderConnection.getChannel().pipeline().names());
            }
        }

        // Other pipelines if different ones are found (3 max)
        for (final UserConnection connection : Via.getManager().getConnectionManager().getConnections()) {
            if (connection.getChannel() == null) {
                continue;
            }

            final List<String> names = connection.getChannel().pipeline().names();
            if (pipelines.add(names) && pipelines.size() == 3) {
                break;
            }
        }

        int i = 0;
        for (final List<String> pipeline : pipelines) {
            final JsonArray senderPipeline = new JsonArray(pipeline.size());
            for (final String name : pipeline) {
                senderPipeline.add(name);
            }

            playerSample.add("pipeline-" + i++, senderPipeline);
        }

        return playerSample;
    }

    public static final class DumpException extends RuntimeException {
        private final DumpErrorType errorType;

        private DumpException(final DumpErrorType errorType, final Throwable cause) {
            super(errorType.message(), cause);
            this.errorType = errorType;
        }

        private DumpException(final DumpErrorType errorType) {
            super(errorType.message());
            this.errorType = errorType;
        }

        public DumpErrorType errorType() {
            return errorType;
        }
    }

    public enum DumpErrorType {

        CONNECTION("Failed to dump, please check the console for more information"),
        RATE_LIMITED("Please wait before creating another dump"),
        POST("Failed to dump, please check the console for more information");

        private final String message;

        DumpErrorType(final String message) {
            this.message = message;
        }

        public String message() {
            return message;
        }
    }
}
