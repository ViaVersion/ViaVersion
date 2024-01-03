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

import com.viaversion.viaversion.api.command.ViaCommandSender;
import com.viaversion.viaversion.api.command.ViaSubCommand;
import io.netty.util.ResourceLeakDetector;

public class DisplayLeaksSubCmd extends ViaSubCommand {
    @Override
    public String name() {
        return "displayleaks";
    }

    @Override
    public String description() {
        return "Try to hunt memory leaks!";
    }

    @Override
    public boolean execute(ViaCommandSender sender, String[] args) {
        if (ResourceLeakDetector.getLevel() != ResourceLeakDetector.Level.PARANOID)
            ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
        else
            ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);

        sendMessage(sender, "&6Leak detector is now %s", (ResourceLeakDetector.getLevel() == ResourceLeakDetector.Level.PARANOID ? "&aenabled" : "&cdisabled"));
        return true;
    }
}
