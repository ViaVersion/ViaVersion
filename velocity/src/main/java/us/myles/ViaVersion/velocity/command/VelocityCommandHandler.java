package us.myles.ViaVersion.velocity.command;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import us.myles.ViaVersion.commands.ViaCommandHandler;
import us.myles.ViaVersion.velocity.command.subs.ProbeSubCmd;

import java.util.List;

public class VelocityCommandHandler extends ViaCommandHandler implements Command {
    public VelocityCommandHandler() {
        try {
            registerSubCommand(new ProbeSubCmd());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(@NonNull CommandSource source, String[] args) {
        onCommand(new VelocityCommandSender(source), args);
    }

    @Override
    public List<String> suggest(@NonNull CommandSource source, String[] currentArgs) {
        return onTabComplete(new VelocityCommandSender(source), currentArgs);
    }
}
