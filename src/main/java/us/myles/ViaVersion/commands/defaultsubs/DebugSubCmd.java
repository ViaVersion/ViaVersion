package us.myles.ViaVersion.commands.defaultsubs;

import org.bukkit.command.CommandSender;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.command.ViaSubCommand;

public class DebugSubCmd extends ViaSubCommand {
    @Override
    public String name() {
        return "debug";
    }

    @Override
    public String description() {
        return "Toggle debug mode";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        ViaVersionPlugin plugin = (ViaVersionPlugin) ViaVersion.getInstance();

        plugin.setDebug(!plugin.isDebug());
        sendMessage(sender, "&6Debug mode is now %s", (plugin.isDebug() ? "&aenabled" : "&cdisabled"));
        return true;
    }
}
