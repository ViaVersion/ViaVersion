/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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

public class DebugSubCmd extends ViaSubCommand {
    @Override
    public String name() {
        return "debug";
    }

    @Override
    public String description() {
        return "Toggle debug mode";
    }

    @Override
    public boolean execute(ViaCommandSender sender, String[] args) {
        Via.getManager().setDebug(!Via.getManager().isDebug());
        sendMessage(sender, "&6Debug mode is now %s", (Via.getManager().isDebug() ? "&aenabled" : "&cdisabled"));
        return true;
    }
}
