package us.myles.ViaVersion.commands.defaultsubs;

import org.bukkit.command.CommandSender;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.command.ViaSubCommand;

public class DontBugMeSubCmd extends ViaSubCommand {
    @Override
    public String name() {
        return "dontbugme";
    }

    @Override
    public String description() {
        return "Toggle checking for updates";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        ViaVersionPlugin plugin = (ViaVersionPlugin) ViaVersion.getInstance();

        boolean newValue = !ViaVersion.getConfig().isCheckForUpdates();
        plugin.getConfig().set("checkforupdates", newValue);
        plugin.saveConfig();
        sendMessage(sender, "&6We will %snotify you about updates.", (newValue ? "&a" : "&cnot "));

        return true;
    }
}
