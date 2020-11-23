package us.myles.ViaVersion.update;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.Nullable;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.util.GsonUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.UUID;

public class UpdateUtil {

    public static final String PREFIX = "§a§l[ViaVersion] §a";
    private static final String URL = "https://api.spiget.org/v2/resources/";
    private static final int PLUGIN = 19254;
    private static final String LATEST_VERSION = "/versions/latest";

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
        if (Via.getPlatform().getPluginVersion().equals("${version}")) {
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
            URL url = new URL(URL + PLUGIN + LATEST_VERSION + "?" + System.currentTimeMillis());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(true);
            connection.addRequestProperty("User-Agent", "ViaVersion " + Via.getPlatform().getPluginVersion() + " " + Via.getPlatform().getPlatformName());
            connection.setDoOutput(true);
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String input;
            StringBuilder builder = new StringBuilder();
            while ((input = br.readLine()) != null) {
                builder.append(input);
            }
            br.close();
            JsonObject statistics;
            try {
                statistics = GsonUtil.getGson().fromJson(builder.toString(), JsonObject.class);
            } catch (JsonParseException e) {
                e.printStackTrace();
                return null;
            }
            return statistics.get("name").getAsString();
        } catch (MalformedURLException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }
}
