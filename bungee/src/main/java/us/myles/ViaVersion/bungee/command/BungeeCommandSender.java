package us.myles.ViaVersion.bungee.command;

import lombok.AllArgsConstructor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import us.myles.ViaVersion.api.command.ViaCommandSender;

import java.util.UUID;

@AllArgsConstructor
public class BungeeCommandSender implements ViaCommandSender {
    private CommandSender sender;

    @Override
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public void sendMessage(String msg) {
        sender.sendMessage(new TextComponent(msg));
    }

    @Override
    public UUID getUUID() {
        if (sender instanceof ProxiedPlayer) {
            return ((ProxiedPlayer) sender).getUniqueId();
        } else {
            return UUID.fromString(getName());
        }
    }

    @Override
    public String getName() {
        return sender.getName();
    }
}
