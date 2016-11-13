package us.myles.ViaVersion.commands.defaultsubs;

import com.google.gson.JsonObject;
import net.md_5.bungee.api.ChatColor;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.command.ViaCommandSender;
import us.myles.ViaVersion.api.command.ViaSubCommand;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.dump.DumpTemplate;
import us.myles.ViaVersion.dump.VersionInfo;
import us.myles.ViaVersion.util.GsonUtil;

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
                try {
                    HttpURLConnection con = (HttpURLConnection) new URL("http://hastebin.com/documents").openConnection();

                    con.setRequestProperty("Content-Type", "text/plain");
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);

                    OutputStream out = con.getOutputStream();
                    out.write(GsonUtil.getGsonBuilder().setPrettyPrinting().create().toJson(template).getBytes(Charset.forName("UTF-8")));
                    out.close();

                    JsonObject output = GsonUtil.getGson().fromJson(new InputStreamReader(con.getInputStream()), JsonObject.class);
                    con.getInputStream().close();

                    if (!output.has("key"))
                        throw new InvalidObjectException("Key is not given in Hastebin output");

                    sender.sendMessage(ChatColor.GREEN + "We've made a dump with useful information, report your issue and provide this url: " + getUrl(output.get("key").getAsString()));
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "Failed to dump, please check the console for more information");
                    Via.getPlatform().getLogger().log(Level.WARNING, "Could not paste ViaVersion dump to Hastebin", e);
                }
            }
        });

        return true;
    }

    private String getUrl(String id) {
        return String.format("http://hastebin.com/%s.json", id);
    }
}
