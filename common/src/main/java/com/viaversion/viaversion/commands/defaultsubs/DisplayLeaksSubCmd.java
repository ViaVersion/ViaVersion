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
package com.viaversion.viaversion.commands.defaultsubs;

import com.viaversion.viaversion.api.command.ViaCommandSender;
import com.viaversion.viaversion.api.command.ViaSubCommand;
import io.netty.util.ResourceLeakDetector;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DisplayLeaksSubCmd implements ViaSubCommand {
    @Override
    public String name() {
        return "displayleaks";
    }

    @Override
    public String description() {
        return "Try to hunt memory leaks!";
    }

    @Override
    public String usage() {
        return "displayleaks <level>";
    }

    @Override
    public boolean execute(ViaCommandSender sender, String[] args) {
        if (args.length == 1) {
            try {
                ResourceLeakDetector.Level level = ResourceLeakDetector.Level.valueOf(args[0]);
                ResourceLeakDetector.setLevel(level);
                sendMessage(sender, "&6Set leak detector level to &2" + level);
            } catch (IllegalArgumentException e) {
                sendMessage(sender, "&cInvalid level (" + Arrays.toString(ResourceLeakDetector.Level.values()) + ")");
            }
        } else {
            sendMessage(sender, "&6Current leak detection level is &2" + ResourceLeakDetector.getLevel());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(ViaCommandSender sender, String[] args) {
        if (args.length == 1) {
            return Arrays.stream(ResourceLeakDetector.Level.values())
                .map(Enum::name)
                .filter(it -> it.startsWith(args[0]))
                .collect(Collectors.toList());
        }
        return ViaSubCommand.super.onTabComplete(sender, args);
    }
}
