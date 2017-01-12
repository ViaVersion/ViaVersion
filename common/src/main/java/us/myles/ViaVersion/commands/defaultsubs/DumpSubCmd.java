package us.myles.ViaVersion.commands.defaultsubs;

import com.google.common.io.CharStreams;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.ChatColor;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.command.ViaCommandSender;
import us.myles.ViaVersion.api.command.ViaSubCommand;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.dump.DumpTemplate;
import us.myles.ViaVersion.dump.VersionInfo;
import us.myles.ViaVersion.util.GsonUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InvalidObjectException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
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
    public boolean execute(final ViaCommandSender sender, String[] args) {
        VersionInfo version = new VersionInfo(
                System.getProperty("java.version"),
                System.getProperty("os.name"),
                ProtocolRegistry.SERVER_PROTOCOL,
                ProtocolRegistry.getSupportedVersions(),
                Via.getPlatform().getPlatformName(),
                Via.getPlatform().getPlatformVersion(),
                Via.getPlatform().getPluginVersion()
        );

        Map<String, Object> configuration = Via.getPlatform().getConfigurationProvider().getValues();

        final DumpTemplate template = new DumpTemplate(version, configuration, Via.getPlatform().getDump());

        Via.getPlatform().runAsync(new Runnable() {
            @Override
            public void run() {

                HttpURLConnection con;
                try {
                    con = (HttpURLConnection) new URL("https://api.github.com/gists").openConnection();
                } catch (IOException e) {
                    sender.sendMessage(ChatColor.RED + "Failed to dump, please check the console for more information");
                    Via.getPlatform().getLogger().log(Level.WARNING, "Could not paste ViaVersion dump to Gist", e);
                    return;
                }
                try {
                    con.setRequestProperty("Content-Type", "application/json");
                    con.addRequestProperty("User-Agent", "ViaVersion");
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);

                    OutputStream out = con.getOutputStream();
                    String contents = GsonUtil.getGsonBuilder().setPrettyPrinting().create().toJson(template);
                    // Create payload
                    JsonObject payload = new JsonObject();
                    payload.addProperty("description", "ViaVersion Dump");
                    payload.addProperty("public", "false");
                    // Create file contents
                    JsonObject file = new JsonObject();
                    file.addProperty("content", contents);
                    // Create file list
                    JsonObject files = new JsonObject();
                    files.add("dump.json", file);
                    payload.add("files", files);
                    // Write to stream
                    out.write(GsonUtil.getGson().toJson(payload).getBytes(Charset.forName("UTF-8")));
                    out.close();

                    String rawOutput = CharStreams.toString(new InputStreamReader(con.getInputStream()));
                    con.getInputStream().close();
                    JsonObject output = GsonUtil.getGson().fromJson(rawOutput, JsonObject.class);


                    if (!output.has("html_url"))
                        throw new InvalidObjectException("URL is not given in Gist output");

                    sender.sendMessage(ChatColor.GREEN + "We've made a dump with useful information, report your issue and provide this url: " + output.get("html_url").getAsString());
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "Failed to dump, please check the console for more information");
                    try {
                        if (con.getResponseCode() == 403) {
                            // Ensure 403 is due to rate limit being hit
                            if("0".equals(con.getHeaderField("X-RateLimit-Remaining"))) {
                                Via.getPlatform().getLogger().log(Level.WARNING, "You may only create 60 dumps per hour, please try again later.");
                                return;
                            }
                        }
                        Via.getPlatform().getLogger().log(Level.WARNING, "Could not paste ViaVersion dump to Gist", e);

                    } catch (IOException e1) {
                        Via.getPlatform().getLogger().log(Level.WARNING, "Failed to capture further info", e1);
                    }
                }
            }
        });

        return true;
    }
}
