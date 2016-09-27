package us.myles.ViaVersion.bukkit.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import us.myles.ViaVersion.commands.ViaCommandHandler;

import java.util.List;

public class BukkitCommandHandler extends ViaCommandHandler implements CommandExecutor, TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return onCommand(new BukkitCommandSender(sender), args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return onTabComplete(new BukkitCommandSender(sender), args);
    }
}
