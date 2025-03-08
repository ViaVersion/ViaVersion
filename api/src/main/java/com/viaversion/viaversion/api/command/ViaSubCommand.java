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

import com.viaversion.viaversion.util.ChatColorUtil;
import java.util.Collections;
import java.util.List;

public interface ViaSubCommand {

    /**
     * Subcommand name
     *
     * @return The commands name
     */
    String name();

    /**
     * subcommand description, this'll show in /viaversion list
     *
     * @return The commands description
     */
    String description();

    /**
     * Usage example:
     * "playerversion [name]"
     *
     * @return The commands usage
     */
    default String usage() {
        return name();
    }

    /**
     * Permission, null for everyone
     *
     * @return The permission required to use the commands
     */
    default String permission() {
        return "viaversion.admin." + name();
    }

    /**
     * Gets triggered on execution
     *
     * @param sender Command sender
     * @param args   Arguments
     * @return commands executed successfully if false, show usage
     */
    boolean execute(ViaCommandSender sender, String[] args);

    /**
     * Yay, possibility to implement tab-completion
     *
     * @param sender Command sender
     * @param args   args
     * @return tab complete possibilities
     */
    default List<String> onTabComplete(ViaCommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    /**
     * Replaces color codes in a string.
     *
     * @param s string to replace
     * @return output String
     */
    static String color(String s) {
        return ChatColorUtil.translateAlternateColorCodes(s);
    }

    /**
     * Send a color coded string with replacements to a user.
     *
     * @param sender  target to send the message to
     * @param message message
     * @param args    objects to replace
     */
    default void sendMessage(ViaCommandSender sender, String message, Object... args) {
        sender.sendMessage(color(args == null ? message : String.format(message, args)));
    }
}
