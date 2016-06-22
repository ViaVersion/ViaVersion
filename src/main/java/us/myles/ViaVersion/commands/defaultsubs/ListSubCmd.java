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
        Map<ProtocolVersion, Set<String>> playerVersions = new TreeMap<>(new Comparator<ProtocolVersion>() {
            @Override
            public int compare(ProtocolVersion o1, ProtocolVersion o2) {
                return ProtocolVersion.getIndex(o2) - ProtocolVersion.getIndex(o1);
            }
        });

        for (Player p : Bukkit.getOnlinePlayers()) {
            int playerVersion = ViaVersion.getInstance().getPlayerVersion(p);
            ProtocolVersion key = ProtocolVersion.getProtocol(playerVersion);
            if (!playerVersions.containsKey(key))
                playerVersions.put(key, new HashSet<String>());
            playerVersions.get(key).add(p.getName());
        }

        for (Map.Entry<ProtocolVersion, Set<String>> entry : playerVersions.entrySet())
            sendMessage(sender, "&8[&6%s&8]: &b%s", entry.getKey().getName(), entry.getValue());

        playerVersions.clear();
        return true;
    }
}
