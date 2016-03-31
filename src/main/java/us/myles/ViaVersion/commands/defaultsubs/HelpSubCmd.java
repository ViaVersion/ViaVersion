package us.myles.ViaVersion.commands.defaultsubs;

import org.bukkit.command.CommandSender;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.command.ViaSubCommand;
import us.myles.ViaVersion.commands.ViaCommandHandler;

public class HelpSubCmd extends ViaSubCommand {
    @Override
    public String name() {
        return "help";
    }

    @Override
    public String description() {
        return "You are looking at it right now!";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        ViaVersionPlugin plugin = (ViaVersionPlugin) ViaVersion.getInstance();

        ((ViaCommandHandler) plugin.getCommandHandler()).showHelp(sender);
        return true;
    }
}
