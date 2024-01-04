/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
package com.viaversion.viaversion.commands.defaultsubs;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.command.ViaCommandSender;
import com.viaversion.viaversion.api.command.ViaSubCommand;
import com.viaversion.viaversion.api.debug.DebugHandler;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DebugSubCmd extends ViaSubCommand {
    @Override
    public String name() {
        return "debug";
    }

    @Override
    public String description() {
        return "Toggle various debug modes.";
    }

    @Override
    public boolean execute(ViaCommandSender sender, String[] args) {
        final DebugHandler debug = Via.getManager().debugHandler();
        if (args.length == 0) {
            Via.getManager().debugHandler().setEnabled(!Via.getManager().debugHandler().enabled());
            sendMessage(sender, "&6Debug mode is now %s", (Via.getManager().debugHandler().enabled() ? "&aenabled" : "&cdisabled"));
            return true;
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("clear")) {
                debug.clearPacketTypesToLog();
                sendMessage(sender, "&6Cleared packet types to log");
                return true;
            } else if (args[0].equalsIgnoreCase("logposttransform")) {
                debug.setLogPostPacketTransform(!debug.logPostPacketTransform());
                sendMessage(sender, "&6Post transform packet logging is now %s", (debug.logPostPacketTransform() ? "&aenabled" : "&cdisabled"));
                return true;
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
                debug.addPacketTypeNameToLog(args[1].toUpperCase(Locale.ROOT));
                sendMessage(sender, "&6Added packet type %s to debug logging", args[1]);
                return true;
            } else if (args[0].equalsIgnoreCase("remove")) {
                debug.removePacketTypeNameToLog(args[1].toUpperCase(Locale.ROOT));
                sendMessage(sender, "&6Removed packet type %s from debug logging", args[1]);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(final ViaCommandSender sender, final String[] args) {
        if (args.length == 1) {
            return Arrays.asList("clear", "logposttransform", "add", "remove");
        }
        return Collections.emptyList();
    }
}
