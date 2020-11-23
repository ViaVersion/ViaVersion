package us.myles.ViaVersion.sponge.commands;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Identifiable;
import us.myles.ViaVersion.api.command.ViaCommandSender;

import java.util.UUID;

public class SpongeCommandSender implements ViaCommandSender {
    private final CommandSource source;

    public SpongeCommandSender(CommandSource source) {
        this.source = source;
    }

    @Override
    public boolean hasPermission(String permission) {
        return source.hasPermission(permission);
    }

    @Override
    public void sendMessage(String msg) {
        String serialized = LegacyComponentSerializer.legacySection().serialize(LegacyComponentSerializer.legacySection().deserialize(msg));
        source.sendMessage(TextSerializers.JSON.deserialize(serialized)); // Hacky way to fix links //TODO ??
    }

    @Override
    public UUID getUUID() {
        if (source instanceof Identifiable) {
            return ((Identifiable) source).getUniqueId();
        } else {
            return UUID.fromString(getName());
        }

    }

    @Override
    public String getName() {
        return source.getName();
    }
}
