package us.myles.ViaVersion.bungee.commands;

import us.myles.ViaVersion.bungee.commands.subs.ProbeSubCmd;
import us.myles.ViaVersion.commands.ViaCommandHandler;

public class BungeeCommandHandler extends ViaCommandHandler {
    public BungeeCommandHandler() {
        try {
            registerSubCommand(new ProbeSubCmd());
        } catch (Exception e) {
            System.out.println("Failed to register Bungee subcommands");
            e.printStackTrace();
        }
    }
}
