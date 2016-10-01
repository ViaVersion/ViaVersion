package us.myles.ViaVersion.bungee.commands.subs;

import us.myles.ViaVersion.api.command.ViaCommandSender;
import us.myles.ViaVersion.api.command.ViaSubCommand;
import us.myles.ViaVersion.bungee.service.ProtocolDetectorService;

// TODO better name
public class SearchServersSubCmd extends ViaSubCommand {
    @Override
    public String name() {
        return "searchservers";
    }

    @Override
    public String description() {
        return "Force ViaVersion to search for servers to update the version list (Also happens every minute)";
    }

    @Override
    public boolean execute(ViaCommandSender sender, String[] args) {
        ProtocolDetectorService.getInstance().run();
        sendMessage(sender, "&6Started searching for subservers");
        return true;
    }
}
