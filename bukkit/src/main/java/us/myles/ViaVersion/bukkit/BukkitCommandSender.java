package us.myles.ViaVersion.bukkit;

import lombok.AllArgsConstructor;
import org.bukkit.command.CommandSender;
import us.myles.ViaVersion.api.command.ViaCommandSender;

@AllArgsConstructor
public class BukkitCommandSender implements ViaCommandSender {
    private CommandSender sender;

    @Override
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public void sendMessage(String msg) {
        sender.sendMessage(msg);
    }
}
