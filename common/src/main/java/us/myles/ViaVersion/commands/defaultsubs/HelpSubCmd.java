package us.myles.ViaVersion.commands.defaultsubs;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.command.ViaCommandSender;
import us.myles.ViaVersion.api.command.ViaSubCommand;

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
    public boolean execute(ViaCommandSender sender, String[] args) {
        Via.getManager().getCommandHandler().showHelp(sender);
        return true;
    }
}
