package us.myles.ViaVersion.commands.defaultsubs;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.command.ViaCommandSender;
import us.myles.ViaVersion.api.command.ViaSubCommand;
import us.myles.ViaVersion.api.configuration.ConfigurationProvider;

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
    public boolean execute(ViaCommandSender sender, String[] args) {
        ConfigurationProvider provider = Via.getPlatform().getConfigurationProvider();
        boolean newValue = !Via.getConfig().isAutoTeam();

        provider.set("auto-team", newValue);
        provider.saveConfig();
        sendMessage(sender, "&6We will %s", (newValue ? "&aautomatically team players" : "&cno longer auto team players"));
        sendMessage(sender, "&6All players will need to re-login for the change to take place.");

        return true;
    }
}
