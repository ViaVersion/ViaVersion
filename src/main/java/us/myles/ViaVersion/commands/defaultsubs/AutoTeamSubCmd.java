package us.myles.ViaVersion.commands.defaultsubs;

import org.bukkit.command.CommandSender;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.command.ViaSubCommand;

public class AutoTeamSubCmd extends ViaSubCommand {
    @Override
    public String name() {
        return "autoteam";
    }

    @Override
    public String description() {
        return "Toggle automatically teaming to prevent colliding.";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        ViaVersionPlugin plugin = (ViaVersionPlugin) ViaVersion.getInstance();

        boolean newValue = !ViaVersion.getConfig().isAutoTeam();
        plugin.getConfig().set("auto-team", newValue);
        plugin.saveConfig();
        sendMessage(sender, "&6We will %s", (newValue ? "&aautomatically team players" : "&cno longer auto team players"));
        sendMessage(sender, "&6All players will need to re-login for the change to take place.");

        return true;
    }
}
