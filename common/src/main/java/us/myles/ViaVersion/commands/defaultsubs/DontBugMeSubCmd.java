package us.myles.ViaVersion.commands.defaultsubs;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.command.ViaCommandSender;
import us.myles.ViaVersion.api.command.ViaSubCommand;
import us.myles.ViaVersion.api.configuration.ConfigurationProvider;

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
    public boolean execute(ViaCommandSender sender, String[] args) {
        ConfigurationProvider provider = Via.getPlatform().getConfigurationProvider();
        boolean newValue = !Via.getConfig().isCheckForUpdates();

        Via.getConfig().setCheckForUpdates(newValue);
        provider.saveConfig();
        sendMessage(sender, "&6We will %snotify you about updates.", (newValue ? "&a" : "&cnot "));

        return true;
    }
}
