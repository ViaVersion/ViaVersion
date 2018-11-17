package us.myles.ViaVersion.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import lombok.AllArgsConstructor;
import net.kyori.text.serializer.ComponentSerializers;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import us.myles.ViaVersion.api.command.ViaCommandSender;

import java.util.UUID;

@AllArgsConstructor
public class VelocityCommandSender implements ViaCommandSender {
    private CommandSource source;

    @Override
    public boolean hasPermission(String permission) {
        return source.hasPermission(permission);
    }

    @Override
    public void sendMessage(String msg) {
        source.sendMessage(
                ComponentSerializers.JSON.deserialize(
                        ComponentSerializer.toString(TextComponent.fromLegacyText(msg)) // Fixes links
                )
        );
    }

    @Override
    public UUID getUUID() {
        if (source instanceof Player) {
            return ((Player) source).getUniqueId();
        }
        return UUID.fromString(getName());
    }

    @Override
    public String getName() {
        if (source instanceof Player) {
            return ((Player) source).getUsername();
        }
        return "?"; // :(
    }
}
