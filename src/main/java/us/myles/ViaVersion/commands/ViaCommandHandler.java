package us.myles.ViaVersion.commands;

import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.command.ViaSubCommand;
import us.myles.ViaVersion.commands.defaultsubs.*;

import java.util.*;

public class ViaCommandHandler implements us.myles.ViaVersion.api.command.ViaVersionCommand, CommandExecutor {
    private Map<String, ViaSubCommand> commandMap;

    public ViaCommandHandler() {
        commandMap = new HashMap<>();
        try {
            registerDefaults();
        } catch (Exception e) {
            //ignore never throws exception because it doesn't exists
        }
    }

    @Override
    public void registerSubCommand(@NonNull ViaSubCommand command) throws Exception {
        if (hasSubCommand(command.name()))
            throw new Exception("ViaSubCommand " + command.name() + " does already exists!"); //Maybe another exception later.
        commandMap.put(command.name().toLowerCase(), command);
    }

    @Override
    public boolean hasSubCommand(String name) {
        return commandMap.containsKey(name.toLowerCase());
    }

    @Override
    public ViaSubCommand getSubCommand(String name) {
        return commandMap.get(name.toLowerCase());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return false;
        }

        if (!hasSubCommand(args[0])){
            sender.sendMessage(color("&cThis command is not found"));
            showHelp(sender);
            return false;
        }

        ViaSubCommand handler = getSubCommand(args[0]);
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        boolean result = handler.execute(sender, subArgs);
        if (!result)
            sender.sendMessage("Usage: /viaversion " + handler.usage());
        return result;
    }

    public void showHelp(CommandSender sender) {
        Set<ViaSubCommand> allowed = calculateAllowedCommands(sender);
        if (allowed.size() == 0){
            sender.sendMessage("&cYou are not allowed to use this command!");
            return;
        }
        sender.sendMessage(color("&aViaVersion &c" + ViaVersion.getInstance().getVersion()));
        sender.sendMessage(color("&6Commands:"));
        for (ViaSubCommand cmd : allowed)
            sender.sendMessage(color(String.format("&2/viaversion %s &7- &6%s", cmd.usage(), cmd.description())));
        allowed.clear();
    }

    private Set<ViaSubCommand> calculateAllowedCommands(CommandSender sender) {
        Set<ViaSubCommand> cmds = new HashSet<>();
        for (ViaSubCommand sub : commandMap.values())
            if (sub.permission() == null || sender.hasPermission(sub.permission()))
                cmds.add(sub);
        return cmds;
    }


    public static String color(String string) {
        try {
            string = ChatColor.translateAlternateColorCodes('&', string); //Dont replace all & with $ like we did before.
        } catch (Exception ignored) {
        }
        return string;
    }

    private void registerDefaults() throws Exception {
        registerSubCommand(new ListSubCmd());
        registerSubCommand(new DebugSubCmd());
        registerSubCommand(new DisplayLeaksSubCmd());
        registerSubCommand(new DontBugMeSubCmd());
        registerSubCommand(new AutoTeamSubCmd());
    }
}
