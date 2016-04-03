package us.myles.ViaVersion.commands.defaultsubs;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.command.ViaSubCommand;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;

import java.util.*;

public class ListSubCmd extends ViaSubCommand {
    @Override
    public String name() {
        return "list";
    }

    @Override
    public String description() {
        return "Shows lists of the versions from logged in players";
    }

    @Override
    public String usage() {
        return "list";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Map<Integer, Set<String>> playerVersions = new HashMap<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            int playerVersion = ViaVersion.getInstance().getPlayerVersion(p);
            if (!playerVersions.containsKey(playerVersion))
                playerVersions.put(playerVersion, new HashSet<String>());
            playerVersions.get(playerVersion).add(p.getName());
        }
        Map<Integer, Set<String>> sorted = new TreeMap<>(playerVersions);

        for (Map.Entry<Integer, Set<String>> entry : sorted.entrySet())
            sendMessage(sender, "&8[&6%s&8]: &b%s", ProtocolVersion.getProtocol(entry.getKey()).getName(), entry.getValue());

        sorted.clear();
        return true;
    }
}
