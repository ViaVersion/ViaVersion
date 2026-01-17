/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.commands;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.command.ViaCommandSender;
import com.viaversion.viaversion.api.command.ViaSubCommand;
import com.viaversion.viaversion.api.command.ViaVersionCommand;
import com.viaversion.viaversion.commands.defaultsubs.AutoTeamSubCmd;
import com.viaversion.viaversion.commands.defaultsubs.DebugSubCmd;
import com.viaversion.viaversion.commands.defaultsubs.DisplayLeaksSubCmd;
import com.viaversion.viaversion.commands.defaultsubs.DontBugMeSubCmd;
import com.viaversion.viaversion.commands.defaultsubs.DumpSubCmd;
import com.viaversion.viaversion.commands.defaultsubs.ListSubCmd;
import com.viaversion.viaversion.commands.defaultsubs.PPSSubCmd;
import com.viaversion.viaversion.commands.defaultsubs.PlayerSubCmd;
import com.viaversion.viaversion.commands.defaultsubs.ReloadSubCmd;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.viaversion.viaversion.api.command.ViaSubCommand.color;

public class ViaCommandHandler implements ViaVersionCommand {
    private final Map<String, ViaSubCommand> commandMap = new HashMap<>();

    @Deprecated
    public ViaCommandHandler() {
        this(true);
    }

    public ViaCommandHandler(final boolean checkForUpdates) {
        registerSubCommand(new ListSubCmd());
        registerSubCommand(new PPSSubCmd());
        registerSubCommand(new DebugSubCmd());
        registerSubCommand(new DumpSubCmd());
        registerSubCommand(new DisplayLeaksSubCmd());
        registerSubCommand(new AutoTeamSubCmd());
        registerSubCommand(new ReloadSubCmd());
        registerSubCommand(new PlayerSubCmd());
        if (checkForUpdates) {
            registerSubCommand(new DontBugMeSubCmd());
        }
    }

    @Override
    public void registerSubCommand(ViaSubCommand command) {
        Preconditions.checkArgument(command.name().matches("^[a-z0-9_-]{3,15}$"), command.name() + " is not a valid sub-command name.");
        Preconditions.checkArgument(!hasSubCommand(command.name()), "ViaSubCommand " + command.name() + " does already exists!");
        commandMap.put(command.name().toLowerCase(Locale.ROOT), command);
    }

    @Override
    public void removeSubCommand(final String name) {
        commandMap.remove(name.toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean hasSubCommand(String name) {
        return commandMap.containsKey(name.toLowerCase(Locale.ROOT));
    }

    @Override
    public ViaSubCommand getSubCommand(String name) {
        return commandMap.get(name.toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean onCommand(ViaCommandSender sender, String[] args) {
        boolean hasPermissions = sender.hasPermission("viaversion.admin");
        for (ViaSubCommand command : commandMap.values()) {
            if (sender.hasPermission(command.permission())) {
                hasPermissions = true;
                break;
            }
        }

        if (!hasPermissions) {
            sender.sendMessage(color("&cYou are not allowed to use this command!"));
            return false;
        }

        if (args.length == 0) {
            showHelp(sender);
            return false;
        }

        if (!hasSubCommand(args[0])) {
            sender.sendMessage(color("&cThis command does not exist."));
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
        if (!result) {
            sender.sendMessage("Usage: /viaversion " + handler.usage());
        }
        return result;
    }

    @Override
    public List<String> onTabComplete(ViaCommandSender sender, String[] args) {
        Set<ViaSubCommand> allowed = calculateAllowedCommands(sender);
        List<String> output = new ArrayList<>();

        //SubCommands tabcomplete
        if (args.length == 1) {
            if (!args[0].isEmpty()) {
                for (ViaSubCommand sub : allowed) {
                    if (sub.name().toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT))) {
                        output.add(sub.name());
                    }
                }
            } else {
                for (ViaSubCommand sub : allowed) {
                    output.add(sub.name());
                }
            }
        }
        //Let the SubCommand handle it
        else if (args.length >= 2) {
            if (getSubCommand(args[0]) != null) {
                ViaSubCommand sub = getSubCommand(args[0]);
                if (!allowed.contains(sub)) {
                    return output;
                }

                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

                List<String> tab = sub.onTabComplete(sender, subArgs);
                Collections.sort(tab);
                if (!tab.isEmpty()) {
                    final String currArg = subArgs[subArgs.length - 1];
                    for (String s : tab) {
                        if (s.toLowerCase(Locale.ROOT).startsWith(currArg.toLowerCase(Locale.ROOT))) {
                            output.add(s);
                        }
                    }
                }
                return output;
            }
        }
        return output;
    }

    /**
     * Shows the ViaVersion help to a sender
     *
     * @param sender The sender to send the help to
     */
    @Override
    public void showHelp(ViaCommandSender sender) {
        Set<ViaSubCommand> allowed = calculateAllowedCommands(sender);
        if (allowed.isEmpty()) {
            sender.sendMessage(color("&cYou are not allowed to use these commands!"));
            return;
        }
        sender.sendMessage(color("&aViaVersion &c" + Via.getPlatform().getPluginVersion()));
        sender.sendMessage(color("&6Commands:"));
        for (ViaSubCommand cmd : allowed) {
            sender.sendMessage(color(String.format("&2/viaversion %s &7- &6%s", cmd.usage(), cmd.description())));
        }
        allowed.clear();
    }

    private Set<ViaSubCommand> calculateAllowedCommands(ViaCommandSender sender) {
        Set<ViaSubCommand> cmds = new HashSet<>();
        for (ViaSubCommand sub : commandMap.values()) {
            if (hasPermission(sender, sub.permission())) {
                cmds.add(sub);
            }
        }
        return cmds;
    }

    private boolean hasPermission(ViaCommandSender sender, String permission) {
        return permission == null || sender.hasPermission("viaversion.admin") || sender.hasPermission(permission);
    }
}
