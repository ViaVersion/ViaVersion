package us.myles.ViaVersion.commands.defaultsubs;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.command.ViaCommandSender;
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
    public boolean execute(ViaCommandSender sender, String[] args) {
        Via.getPlatform().getConfigurationProvider().reloadConfig();
        sendMessage(sender, "&6Configuration successfully reloaded! Some features may need a restart.");
        return true;
    }
}
