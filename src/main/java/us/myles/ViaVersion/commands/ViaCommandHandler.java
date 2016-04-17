package us.myles.ViaVersion.commands;

import lombok.NonNull;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.command.ViaSubCommand;
import us.myles.ViaVersion.api.command.ViaVersionCommand;
import us.myles.ViaVersion.commands.defaultsubs.*;

import java.util.*;

public class ViaCommandHandler implements ViaVersionCommand, CommandExecutor, TabCompleter {
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
        Validate.isTrue(command.name().matches("^[a-z0-9_-]{3,15}$"), command.name() + " is not a valid subcommand name");
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

        if (!hasSubCommand(args[0])) {
            sender.sendMessage(color("&cThis command is not found"));
            showHelp(sender);
            return false;
        }
        ViaSubCommand handler = getSubCommand(args[0]);

        if (!hasPermission(sender, handler.permission())) {
            sender.sendMessage(color("&cYou are not allowed to use this command!"));
            return false;
        }

        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        boolean result = handler.execute(sender, subArgs);
        if (!result)
            sender.sendMessage("Usage: /viaversion " + handler.usage());
        return result;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String arg, String[] args) {
        Set<ViaSubCommand> allowed = calculateAllowedCommands(sender);
        List<String> output = new ArrayList<>();

        //SubCommands tabcomplete
        if (args.length == 1) {
            if (!args[0].equals("")) {
                for (ViaSubCommand sub : allowed)
                    if (sub.name().toLowerCase().startsWith(args[0].toLowerCase()))
                        output.add(sub.name());
            } else {
                for (ViaSubCommand sub : allowed)
                    output.add(sub.name());
            }
        }
        //Let the SubCommand handle it
        else if (args.length >= 2) {
            if (getSubCommand(args[0]) != null) {
                ViaSubCommand sub = getSubCommand(args[0]);
                if (!allowed.contains(sub))
                    return output;

                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

                List<String> tab = sub.onTabComplete(sender, subArgs);
                Collections.sort(tab);
                return tab;
            }
        }
        return output;
    }

    public void showHelp(CommandSender sender) {
        Set<ViaSubCommand> allowed = calculateAllowedCommands(sender);
        if (allowed.size() == 0) {
            sender.sendMessage(color("&cYou are not allowed to use this command!"));
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
            if (hasPermission(sender, sub.permission()))
                cmds.add(sub);
        return cmds;
    }

    private boolean hasPermission(CommandSender sender, String permission) {
        return permission == null || sender.hasPermission(permission);
    }

    private void registerDefaults() throws Exception {
        registerSubCommand(new ListSubCmd());
        registerSubCommand(new PPSSubCmd());
        registerSubCommand(new DebugSubCmd());
        registerSubCommand(new DisplayLeaksSubCmd());
        registerSubCommand(new DontBugMeSubCmd());
        registerSubCommand(new AutoTeamSubCmd());
        registerSubCommand(new HelpSubCmd());
        registerSubCommand(new ReloadSubCmd());
    }

    public static String color(String string) {
        try {
            string = ChatColor.translateAlternateColorCodes('&', string); //Dont replace all & with $ like we did before.
        } catch (Exception ignored) {
        }
        return string;
    }

    public static void sendMessage(@NonNull CommandSender sender, String message, Object... args) {
        sender.sendMessage(color(args == null ? message : String.format(message, args)));
    }
}
