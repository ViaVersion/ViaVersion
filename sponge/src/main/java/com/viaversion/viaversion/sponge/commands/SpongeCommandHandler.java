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
package com.viaversion.viaversion.sponge.commands;

import com.viaversion.viaversion.commands.ViaCommandHandler;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;

public class SpongeCommandHandler extends ViaCommandHandler implements CommandCallable {

    @Override
    public CommandResult process(CommandSource source, String arguments) throws CommandException {
        String[] args = arguments.length() > 0 ? arguments.split(" ") : new String[0];
        onCommand(new SpongeCommandSender(source), args);
        return CommandResult.success();
    }

    public List<String> getSuggestions(CommandSource commandSource, String s, @Nullable Location<World> location) throws CommandException {
        return getSuggestions(commandSource, s);
    }

    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        String[] args = arguments.split(" ", -1); // ViaCommandHandler handles empty String in array. -1: do not discard empty strings
        return onTabComplete(new SpongeCommandSender(source), args);
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return source.hasPermission("viaversion.admin");
    }

    @Override
    public Optional<Text> getShortDescription(CommandSource source) {
        return Optional.of(Text.of("Shows ViaVersion Version and more."));
    }

    @Override
    public Optional<Text> getHelp(CommandSource source) {
        return Optional.empty();
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Text.of("Usage /viaversion");
    }
}
