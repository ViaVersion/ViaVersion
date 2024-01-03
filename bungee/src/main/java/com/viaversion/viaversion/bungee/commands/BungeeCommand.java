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
package com.viaversion.viaversion.bungee.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class BungeeCommand extends Command implements TabExecutor {
    private final BungeeCommandHandler handler;

    public BungeeCommand(BungeeCommandHandler handler) {
        super("viaversion", "viaversion.command", "viaver", "vvbungee"); // The permission is also referenced here to filter root suggestions (/via<tab>)
        this.handler = handler;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        handler.onCommand(new BungeeCommandSender(commandSender), strings);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] strings) {
        return handler.onTabComplete(new BungeeCommandSender(commandSender), strings);
    }
}
