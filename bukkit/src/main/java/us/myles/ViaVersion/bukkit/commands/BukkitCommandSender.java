package us.myles.ViaVersion.bukkit.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.api.command.ViaCommandSender;

import java.util.UUID;

public class BukkitCommandSender implements ViaCommandSender {
    private final CommandSender sender;

    public BukkitCommandSender(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public void sendMessage(String msg) {
        sender.sendMessage(msg);
    }

    @Override
    public UUID getUUID() {
        if (sender instanceof Player) {
            return ((Player) sender).getUniqueId();
        } else {
            return UUID.fromString(getName());
        }
    }

    @Override
    public String getName() {
        return sender.getName();
    }
}
