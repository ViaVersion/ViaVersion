package us.myles.ViaVersion.bungee.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class BungeeCommand extends Command implements TabExecutor {
    private final BungeeCommandHandler handler;

    public BungeeCommand(BungeeCommandHandler handler) {
        super("viaversion", "", "viaver", "vvbungee"); // The CommandHandler will handle the permission
        this.handler = handler;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        handler.onCommand(new BungeeCommandSender(commandSender), strings);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] strings) {
        return handler.onTabComplete(new BungeeCommandSender(commandSender), strings);
    }
}
