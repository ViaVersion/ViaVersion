package us.myles.ViaVersion.commands.defaultsubs;

import com.google.common.io.CharStreams;
import com.google.gson.JsonObject;
import us.myles.ViaVersion.ViaManager;
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
import java.nio.charset.StandardCharsets;
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
    public boolean execute(ViaCommandSender sender, String[] args) {
        VersionInfo version = new VersionInfo(
                System.getProperty("java.version"),
                System.getProperty("os.name"),
                ProtocolRegistry.SERVER_PROTOCOL,
                ProtocolRegistry.getSupportedVersions(),
                Via.getPlatform().getPlatformName(),
                Via.getPlatform().getPlatformVersion(),
                Via.getPlatform().getPluginVersion(),
                "$IMPL_VERSION",
                Via.getManager().getSubPlatforms()
        );

        Map<String, Object> configuration = Via.getPlatform().getConfigurationProvider().getValues();

        DumpTemplate template = new DumpTemplate(version, configuration, Via.getPlatform().getDump(), Via.getManager().getInjector().getDump());

        Via.getPlatform().runAsync(new Runnable() {
            @Override
            public void run() {

                HttpURLConnection con = null;
                try {
                    con = (HttpURLConnection) new URL("https://dump.viaversion.com/documents").openConnection();
                } catch (IOException e) {
                    sender.sendMessage("§4Failed to dump, please check the console for more information");
                    Via.getPlatform().getLogger().log(Level.WARNING, "Could not paste ViaVersion dump to ViaVersion Dump", e);
                    return;
                }
                try {
                    con.setRequestProperty("Content-Type", "text/plain");
                    con.addRequestProperty("User-Agent", "ViaVersion/" + version.getPluginVersion());
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);

                    OutputStream out = con.getOutputStream();
                    out.write(GsonUtil.getGsonBuilder().setPrettyPrinting().create().toJson(template).getBytes(StandardCharsets.UTF_8));
                    out.close();

                    if (con.getResponseCode() == 429) {
                        sender.sendMessage("§4You can only paste ones every minute to protect our systems.");
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
}
