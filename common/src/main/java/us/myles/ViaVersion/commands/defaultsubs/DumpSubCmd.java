package us.myles.ViaVersion.commands.defaultsubs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import us.myles.ViaVersion.api.command.ViaCommandSender;
import us.myles.ViaVersion.api.command.ViaSubCommand;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.dump.DumpTemplate;
import us.myles.ViaVersion.dump.PluginInfo;
import us.myles.ViaVersion.dump.VersionInfo;

import java.io.InputStreamReader;
import java.io.InvalidObjectException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class DumpSubCmd extends ViaSubCommand {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

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
                Bukkit.getServer().getVersion(),
                Bukkit.getServer().getBukkitVersion(),
                System.getProperty("java.version"),
                System.getProperty("os.name"),
                ProtocolRegistry.SERVER_PROTOCOL,
                ProtocolRegistry.getSupportedVersions());

        List<PluginInfo> plugins = new ArrayList<>();
        for (Plugin p : Bukkit.getPluginManager().getPlugins())
            plugins.add(new PluginInfo(p.isEnabled(), p.getDescription().getName(), p.getDescription().getVersion(), p.getDescription().getMain(), p.getDescription().getAuthors()));

        Map<String, Object> configuration = ((ViaVersionPlugin) ViaVersion.getInstance()).getConfig().getValues(false);

        final DumpTemplate template = new DumpTemplate(version, configuration, plugins);

        Bukkit.getScheduler().runTaskAsynchronously((ViaVersionPlugin) ViaVersion.getInstance(), new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection con = (HttpURLConnection) new URL("http://hastebin.com/documents").openConnection();

                    con.setRequestProperty("Content-Type", "text/plain");
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);

                    OutputStream out = con.getOutputStream();
                    out.write(gson.toJson(template).getBytes(Charset.forName("UTF-8")));
                    out.close();

                    JsonObject output = gson.fromJson(new InputStreamReader(con.getInputStream()), JsonObject.class);
                    con.getInputStream().close();

                    if (!output.has("key"))
                        throw new InvalidObjectException("Key is not given in Hastebin output");

                    sender.sendMessage(ChatColor.GREEN + "We've made a dump with useful information, report your issue and provide this url: " + getUrl(output.get("key").getAsString()));
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "Failed to dump, please check the console for more information");
                    ((ViaVersionPlugin) ViaVersion.getInstance()).getLogger().log(Level.WARNING, "Could not paste ViaVersion dump to Hastebin", e);
                }
            }
        });

        return true;
    }

    private String getUrl(String id) {
        return String.format("http://hastebin.com/%s.json", id);
    }
}
