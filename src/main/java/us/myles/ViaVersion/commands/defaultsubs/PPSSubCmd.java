package us.myles.ViaVersion.commands.defaultsubs;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.command.ViaSubCommand;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;

import java.util.*;

public class PPSSubCmd extends ViaSubCommand {
    @Override
    public String name() {
        return "pps";
    }

    @Override
    public String description() {
        return "Shows the packets per second of online players";
    }

    @Override
    public String usage() {
        return "pps";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Map<Integer, Set<String>> playerVersions = new HashMap<>();
        int totalPackets = 0;
        int clients = 0;
        long max = 0;

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!ViaVersion.getInstance().isPorted(p))
                continue;
            int playerVersion = ViaVersion.getInstance().getPlayerVersion(p);
            if (!playerVersions.containsKey(playerVersion))
                playerVersions.put(playerVersion, new HashSet<String>());
            UserConnection uc = ((ViaVersionPlugin) ViaVersion.getInstance()).getConnection(p);
            if (uc.getPacketsPerSecond() > -1) {
                playerVersions.get(playerVersion).add(p.getName() + " (" + uc.getPacketsPerSecond() + " PPS)");
                totalPackets += uc.getPacketsPerSecond();
                if (uc.getPacketsPerSecond() > max) {
                    max = uc.getPacketsPerSecond();
                }
                clients++;
            }
        }
        Map<Integer, Set<String>> sorted = new TreeMap<>(playerVersions);
        sendMessage(sender, "&4Live Packets Per Second");
        if (clients > 1) {
            sendMessage(sender, "&cAverage: &f" + (totalPackets / clients));
            sendMessage(sender, "&cHighest: &f" + max);
        }
        for (Map.Entry<Integer, Set<String>> entry : sorted.entrySet())
            sendMessage(sender, "&8[&6%s&8]: &b%s", ProtocolVersion.getProtocol(entry.getKey()).getName(), entry.getValue());
        sorted.clear();
        return true;
    }
}
