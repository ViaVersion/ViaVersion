/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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
package com.viaversion.viaversion.commands.defaultsubs;

import com.google.common.io.CharStreams;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.command.ViaCommandSender;
import com.viaversion.viaversion.api.command.ViaSubCommand;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.dump.DumpTemplate;
import com.viaversion.viaversion.dump.VersionInfo;
import com.viaversion.viaversion.util.GsonUtil;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InvalidObjectException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;

public class DumpSubCmd extends ViaSubCommand {

    @Override
    public String name() {
        return "dump";
    }

    @Override
    public String description() {
        return "Dump information about your server, this is helpful if you report bugs.";
    }

    @Override
    public boolean execute(ViaCommandSender sender, String[] args) {
        VersionInfo version = new VersionInfo(
                System.getProperty("java.version"),
                System.getProperty("os.name"),
                Via.getAPI().getServerVersion().lowestSupportedVersion(),
                Via.getManager().getProtocolManager().getSupportedVersions(),
                Via.getPlatform().getPlatformName(),
                Via.getPlatform().getPlatformVersion(),
                Via.getPlatform().getPluginVersion(),
                "$IMPL_VERSION",
                Via.getManager().getSubPlatforms()
        );

        Map<String, Object> configuration = Via.getPlatform().getConfigurationProvider().getValues();
        DumpTemplate template = new DumpTemplate(version, configuration, Via.getPlatform().getDump(), Via.getManager().getInjector().getDump(), getPlayerSample(sender.getUUID()));

        Via.getPlatform().runAsync(new Runnable() {
            @Override
            public void run() {

                HttpURLConnection con;
                try {
                    con = (HttpURLConnection) new URL("https://dump.viaversion.com/documents").openConnection();
                } catch (IOException e) {
                    sender.sendMessage("§4Failed to dump, please check the console for more information");
                    Via.getPlatform().getLogger().log(Level.WARNING, "Could not paste ViaVersion dump to ViaVersion Dump", e);
                    return;
                }
                try {
                    con.setRequestProperty("Content-Type", "application/json");
                    con.addRequestProperty("User-Agent", "ViaVersion/" + version.getPluginVersion());
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);

                    OutputStream out = con.getOutputStream();
                    out.write(new GsonBuilder().setPrettyPrinting().create().toJson(template).getBytes(StandardCharsets.UTF_8));
                    out.close();

                    if (con.getResponseCode() == 429) {
                        sender.sendMessage("§4You can only paste once every minute to protect our systems.");
                        return;
                    }

                    String rawOutput = CharStreams.toString(new InputStreamReader(con.getInputStream()));
                    con.getInputStream().close();

                    JsonObject output = GsonUtil.getGson().fromJson(rawOutput, JsonObject.class);

                    if (!output.has("key"))
                        throw new InvalidObjectException("Key is not given in Hastebin output");

                    sender.sendMessage("§2We've made a dump with useful information, report your issue and provide this url: " + getUrl(output.get("key").getAsString()));
                } catch (Exception e) {
                    sender.sendMessage("§4Failed to dump, please check the console for more information");
                    Via.getPlatform().getLogger().log(Level.WARNING, "Could not paste ViaVersion dump to Hastebin", e);
                    try {
                        if (con.getResponseCode() < 200 || con.getResponseCode() > 400) {
                            String rawOutput = CharStreams.toString(new InputStreamReader(con.getErrorStream()));
                            con.getErrorStream().close();
                            Via.getPlatform().getLogger().log(Level.WARNING, "Page returned: " + rawOutput);
                        }
                    } catch (IOException e1) {
                        Via.getPlatform().getLogger().log(Level.WARNING, "Failed to capture further info", e1);
                    }
                }
            }
        });

        return true;
    }

    private String getUrl(String id) {
        return String.format("https://dump.viaversion.com/%s", id);
    }

    private JsonObject getPlayerSample(UUID senderUuid) {
        JsonObject playerSample = new JsonObject();
        // Player versions
        JsonObject versions = new JsonObject();
        playerSample.add("versions", versions);
        Map<ProtocolVersion, Integer> playerVersions = new TreeMap<>((o1, o2) -> ProtocolVersion.getIndex(o2) - ProtocolVersion.getIndex(o1));
        for (UserConnection connection : Via.getManager().getConnectionManager().getConnections()) {
            ProtocolVersion protocolVersion = ProtocolVersion.getProtocol(connection.getProtocolInfo().getProtocolVersion());
            playerVersions.compute(protocolVersion, (v, num) -> num != null ? num + 1 : 1);
        }
        for (Map.Entry<ProtocolVersion, Integer> entry : playerVersions.entrySet()) {
            versions.addProperty(entry.getKey().getName(), entry.getValue());
        }

        // Pipeline of sender
        Set<List<String>> pipelines = new HashSet<>();
        UserConnection senderConnection = Via.getAPI().getConnection(senderUuid);
        if (senderConnection != null && senderConnection.getChannel() != null) {
            pipelines.add(senderConnection.getChannel().pipeline().names());
        }

        // Other pipelines if different ones are found (3 max)
        for (UserConnection connection : Via.getManager().getConnectionManager().getConnections()) {
            if (connection.getChannel() == null) {
                continue;
            }

            List<String> names = connection.getChannel().pipeline().names();
            if (pipelines.add(names) && pipelines.size() == 3) {
                break;
            }
        }

        int i = 0;
        for (List<String> pipeline : pipelines) {
            JsonArray senderPipeline = new JsonArray(pipeline.size());
            for (String name : pipeline) {
                senderPipeline.add(name);
            }

            playerSample.add("pipeline-" + i++, senderPipeline);
        }

        return playerSample;
    }
}
