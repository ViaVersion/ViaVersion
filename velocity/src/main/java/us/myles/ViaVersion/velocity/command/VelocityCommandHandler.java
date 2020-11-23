package us.myles.ViaVersion.velocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import us.myles.ViaVersion.commands.ViaCommandHandler;
import us.myles.ViaVersion.velocity.command.subs.ProbeSubCmd;

import java.util.List;

public class VelocityCommandHandler extends ViaCommandHandler implements SimpleCommand {
    public VelocityCommandHandler() {
        try {
            registerSubCommand(new ProbeSubCmd());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(Invocation invocation) {
        onCommand(new VelocityCommandSender(invocation.source()), invocation.arguments());
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return onTabComplete(new VelocityCommandSender(invocation.source()), invocation.arguments());
    }
}
