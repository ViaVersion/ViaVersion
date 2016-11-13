package us.myles.ViaVersion.bungee.commands;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.bungee.commands.subs.ProbeSubCmd;
import us.myles.ViaVersion.commands.ViaCommandHandler;

public class BungeeCommandHandler extends ViaCommandHandler {
    public BungeeCommandHandler() {
        try {
            registerSubCommand(new ProbeSubCmd());
        } catch (Exception e) {
            Via.getPlatform().getLogger().severe("Failed to register Bungee subcommands");
            e.printStackTrace();
        }
    }
}
