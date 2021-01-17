package us.myles.ViaVersion.commands.defaultsubs;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.command.ViaCommandSender;
import us.myles.ViaVersion.api.command.ViaSubCommand;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
    public boolean execute(ViaCommandSender sender, String[] args) {
        Map<ProtocolVersion, Set<String>> playerVersions = new TreeMap<>((o1, o2) -> ProtocolVersion.getIndex(o2) - ProtocolVersion.getIndex(o1));

        for (ViaCommandSender p : Via.getPlatform().getOnlinePlayers()) {
            int playerVersion = Via.getAPI().getPlayerVersion(p.getUUID());
            ProtocolVersion key = ProtocolVersion.getProtocol(playerVersion);
            playerVersions.computeIfAbsent(key, s -> new HashSet<>()).add(p.getName());
        }

        for (Map.Entry<ProtocolVersion, Set<String>> entry : playerVersions.entrySet()) {
            sendMessage(sender, "&8[&6%s&8] (&7%d&8): &b%s", entry.getKey().getName(), entry.getValue().size(), entry.getValue());
        }

        playerVersions.clear();
        return true;
    }
}
