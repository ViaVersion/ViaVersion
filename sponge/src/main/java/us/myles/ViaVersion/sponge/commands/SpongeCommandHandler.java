package us.myles.ViaVersion.sponge.commands;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import us.myles.ViaVersion.commands.ViaCommandHandler;

import java.util.List;
import java.util.Optional;

public class SpongeCommandHandler extends ViaCommandHandler implements CommandCallable {

    @Override
    public CommandResult process(CommandSource source, String arguments) throws CommandException {
        String[] args = arguments.length() > 0 ? arguments.split(" ") : new String[0];
        onCommand(new SpongeCommandSender(source), args);
        return CommandResult.success();
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        String[] args = arguments.length() > 0 ? arguments.split(" ") : new String[0];
        return onTabComplete(new SpongeCommandSender(source), args);
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return source.hasPermission("viaversion.admin");
    }

    @Override
    public Optional<? extends Text> getShortDescription(CommandSource source) {
        return Optional.of(Text.of("Shows ViaVersion Version and more."));
    }

    @Override
    public Optional<? extends Text> getHelp(CommandSource source) {
        return Optional.empty();
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Text.of("Usage /viaversion");
    }
}
