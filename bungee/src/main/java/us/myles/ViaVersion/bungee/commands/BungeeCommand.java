package us.myles.ViaVersion.bungee.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class BungeeCommand extends Command {
    private final BungeeCommandHandler handler;

    public BungeeCommand(BungeeCommandHandler handler) {
        super("viaversion", "", "viaver"); // The CommandHandler will handle the permission
        this.handler = handler;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        handler.onCommand(new BungeeCommandSender(commandSender), strings);
    }

}
