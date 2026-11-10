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
package com.viaversion.viaversion.velocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.viaversion.viaversion.commands.ViaCommandHandler;
import com.viaversion.viaversion.velocity.command.subs.ProbeSubCmd;
import java.util.List;

public class VelocityCommandHandler extends ViaCommandHandler implements SimpleCommand {
    public VelocityCommandHandler() {
        super(true);
        registerSubCommand(new ProbeSubCmd());
    }

    @Override
    public void execute(Invocation invocation) {
        onCommand(new VelocityCommandSender(invocation.source()), invocation.arguments());
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return onTabComplete(new VelocityCommandSender(invocation.source()), invocation.arguments());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("viaversion.command"); // The permission is also referenced here to filter root suggestions (/via<tab>)
    }
}
