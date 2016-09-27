package us.myles.ViaVersion.sponge.commands;

import lombok.AllArgsConstructor;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.serializer.TextSerializers;
import us.myles.ViaVersion.api.command.ViaCommandSender;

import java.util.UUID;

@AllArgsConstructor
public class SpongeCommandSender implements ViaCommandSender {
    private CommandSource source;

    @Override
    public boolean hasPermission(String permission) {
        return source.hasPermission(permission);
    }

    @Override
    public void sendMessage(String msg) {
        source.sendMessage(TextSerializers.LEGACY_FORMATTING_CODE.deserialize(msg));
    }

    @Override
    public UUID getUUID() {
        if (source instanceof Player) {
            return ((Player) source).getUniqueId();
        } else {
            return UUID.fromString(getName());
        }
    }

    @Override
    public String getName() {
        return source.getName();
    }
}
