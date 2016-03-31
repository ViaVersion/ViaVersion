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
        return null;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        ViaVersionPlugin plugin = (ViaVersionPlugin) ViaVersion.getInstance();

        boolean newValue = !plugin.isCheckForUpdates();
        plugin.getConfig().set("checkforupdates", newValue);
        plugin.saveConfig();
        sender.sendMessage(color("&6We will " + (newValue ? "&anotify you about updates." : "&cnot tell you about updates.")));

        return true;
    }
}
