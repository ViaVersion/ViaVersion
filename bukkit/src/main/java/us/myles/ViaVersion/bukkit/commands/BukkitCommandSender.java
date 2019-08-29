package us.myles.ViaVersion.bukkit.commands;

import lombok.AllArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.api.command.ViaCommandSender;

import java.util.UUID;

@AllArgsConstructor
public class BukkitCommandSender implements ViaCommandSender {
    private final CommandSender sender;

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
