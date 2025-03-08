/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.viaversion.viaversion.api.command;

import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface ViaVersionCommand {
    /**
     * Register your own subcommand inside ViaVersion
     *
     * @param command Your own SubCommand instance to handle it.
     * @throws IllegalArgumentException throws an exception when the subcommand already exists or if it's not valid, example: space
     */
    void registerSubCommand(ViaSubCommand command);

    /**
     * Removes a subcommand by name, can be used to unload default subcommands which are not supported
     * on the platform.
     * @param name Subcommand name
     */
    void removeSubCommand(String name);

    /**
     * Check if a subcommand is registered.
     *
     * @param name Subcommand name
     * @return true if it exists
     */
    boolean hasSubCommand(String name);

    /**
     * Get subcommand instance by name
     *
     * @param name subcommand name
     * @return ViaSubCommand instance
     */
    @Nullable ViaSubCommand getSubCommand(String name);

    /**
     * Executed when the Command sender executes the commands
     *
     * @param sender Sender object
     * @param args   arguments provided
     * @return was successful
     */
    boolean onCommand(ViaCommandSender sender, String[] args);

    /**
     * Executed when the Command sender tab-completes
     *
     * @param sender Sender object
     * @param args   arguments provided
     * @return was successful
     */
    List<String> onTabComplete(ViaCommandSender sender, String[] args);

    void showHelp(ViaCommandSender sender);
}
