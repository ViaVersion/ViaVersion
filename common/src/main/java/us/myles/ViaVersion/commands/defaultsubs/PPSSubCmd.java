package us.myles.ViaVersion.commands.defaultsubs;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.command.ViaCommandSender;
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
    public boolean execute(ViaCommandSender sender, String[] args) {
        Map<Integer, Set<String>> playerVersions = new HashMap<>();
        int totalPackets = 0;
        int clients = 0;
        long max = 0;

        for (ViaCommandSender p : Via.getPlatform().getOnlinePlayers()) {
            int playerVersion = Via.getAPI().getPlayerVersion(p.getUUID());
            if (!playerVersions.containsKey(playerVersion))
                playerVersions.put(playerVersion, new HashSet<String>());
            UserConnection uc = Via.getManager().getConnection(p.getUUID());
            if (uc != null && uc.getPacketsPerSecond() > -1) {
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
        if (clients == 0) {
            sendMessage(sender, "&cNo clients to display.");
        }
        for (Map.Entry<Integer, Set<String>> entry : sorted.entrySet())
            sendMessage(sender, "&8[&6%s&8]: &b%s", ProtocolVersion.getProtocol(entry.getKey()).getName(), entry.getValue());
        sorted.clear();
        return true;
    }
}
