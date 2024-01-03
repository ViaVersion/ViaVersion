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
import com.viaversion.viaversion.api.configuration.ViaVersionConfig;

public class DontBugMeSubCmd extends ViaSubCommand {

    @Override
    public String name() {
        return "dontbugme";
    }

    @Override
    public String description() {
        return "Toggle checking for updates.";
    }

    @Override
    public boolean execute(ViaCommandSender sender, String[] args) {
        final ViaVersionConfig config = Via.getConfig();
        boolean newValue = !config.isCheckForUpdates();

        config.setCheckForUpdates(newValue);
        config.save();
        sendMessage(sender, "&6We will %snotify you about updates.", (newValue ? "&a" : "&cnot "));

        return true;
    }
}
