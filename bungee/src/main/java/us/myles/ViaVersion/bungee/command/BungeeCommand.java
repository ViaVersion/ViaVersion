package us.myles.ViaVersion.bungee.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class BungeeCommand extends Command {
    private final BungeeCommandHandler handler;

    public BungeeCommand(BungeeCommandHandler handler) {
        super("viaversion"); // TODO PERMS HERE
        this.handler = handler;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        handler.onCommand(new BungeeCommandSender(commandSender), strings);
    }

}
