package us.myles.ViaVersion.commands.defaultsubs;

import org.bukkit.command.CommandSender;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.command.ViaSubCommand;

public class ReloadSubCmd extends ViaSubCommand {
    @Override
    public String name() {
        return "reload";
    }

    @Override
    public String description() {
        return "Reload the config from the disk";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        ViaVersionPlugin plugin = (ViaVersionPlugin) ViaVersion.getInstance();

        plugin.generateConfig();
        sendMessage(sender, "&6Configuration successfully reloaded! Some features may need a restart.");
        return true;
    }
}
