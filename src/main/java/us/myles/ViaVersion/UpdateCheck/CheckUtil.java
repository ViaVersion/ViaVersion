package UpdateCheck;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import us.myles.ViaVersion.ViaVersionPlugin;
import net.md_5.bungee.api.ChatColor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.bukkit.entity.Player;

public class CheckUtil {

    private static String URL = "https://api.spiget.org/v1/resources/";

    public static boolean getResource(double currentversion) {
        String url = URL + 19254;
        String content = "";
        double version = 0;
        try {
            HttpURLConnection con = createConnection(url);

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String input;
            while ((input = br.readLine()) != null) {
                content = content + input;
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObject statistics = new JsonObject();
        JsonParser parser = new JsonParser();
        String json = content;
        statistics = (JsonObject)parser.parse(json);

        try
        {
            version = statistics.get("version").getAsDouble();
        }
        catch (Exception e) {}

        if (version > currentversion) {
            return true;
        } else {
            return false;
        }
    }


    public static void update(Player p) {
    	double thisversion = ViaVersionPlugin.version;
    	
    	boolean update = getResource(thisversion);
    	if (update) {
    		p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "[ViaVersion]" + ChatColor.RED + "" + ChatColor.BOLD + "A new update is avaliable");
    	}
    }

    private static HttpURLConnection createConnection(String s) throws Exception {
        URL url = new URL(s);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setUseCaches(true);
        connection.addRequestProperty("User-Agent", "Mozilla/4.76");
        connection.setDoOutput(true);
        return connection;
    }

}
