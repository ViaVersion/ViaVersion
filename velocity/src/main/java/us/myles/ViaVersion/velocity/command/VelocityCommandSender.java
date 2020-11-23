package us.myles.ViaVersion.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import us.myles.ViaVersion.api.command.ViaCommandSender;

import java.util.UUID;

public class VelocityCommandSender implements ViaCommandSender {
    private final CommandSource source;

    public VelocityCommandSender(CommandSource source) {
        this.source = source;
    }

    @Override
    public boolean hasPermission(String permission) {
        return source.hasPermission(permission);
    }

    @Override
    public void sendMessage(String msg) {
        source.sendMessage(LegacyComponentSerializer.legacySection().deserialize(msg));
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
