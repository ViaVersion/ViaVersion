package us.myles.ViaVersion.update;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import us.myles.ViaVersion.api.ViaVersion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class UpdateUtil {

    private final static String URL = "https://api.spiget.org/v1/resources/";
    private final static int PLUGIN = 19254;

    public final static String PREFIX = ChatColor.GREEN + "" + ChatColor.BOLD + "[ViaVersion] " + ChatColor.GREEN;

    public static void sendUpdateMessage(final UUID uuid, final Plugin plugin) {
        new BukkitRunnable() {

            @Override
            public void run() {
                final String message = getUpdateMessage(false);
                if (message != null) {
                    new BukkitRunnable() {

                        @Override
                        public void run() {
                            Player p = Bukkit.getPlayer(uuid);
                            if (p != null) {
                                p.sendMessage(PREFIX + message);
                            }
                        }
                    }.runTask(plugin);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public static void sendUpdateMessage(final Plugin plugin) {
        new BukkitRunnable() {

            @Override
            public void run() {
                final String message = getUpdateMessage(true);
                if (message != null) {
                    new BukkitRunnable() {

                        @Override
                        public void run() {
                            plugin.getLogger().warning(message);
                        }
                    }.runTask(plugin);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private static String getUpdateMessage(boolean console) {
        if (ViaVersion.getInstance().getVersion().equals("${project.version}")) {
            return "You are using a debug/custom version, consider updating.";
        }

        Version current = new Version(ViaVersion.getInstance().getVersion());
        Version newest = new Version(getNewestVersion());
        if (current.compareTo(newest) < 0)
            return "There is a newer version available: " + newest.toString();
        else if (console) {
            return "You are running a newer version than is released!";
        }
        return null;
    }

    private static String getNewestVersion() {
        String result = "";
        try {
            URL url = new URL(URL + PLUGIN + "?" + System.currentTimeMillis());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(true);
            connection.addRequestProperty("User-Agent", "Mozilla/4.76");
            connection.setDoOutput(true);
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String input;
            String content = "";
            while ((input = br.readLine()) != null) {
                content = content + input;
            }
            br.close();
            JsonParser parser = new JsonParser();
            JsonObject statistics = (JsonObject) parser.parse(content);
            result = statistics.get("version").getAsString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
