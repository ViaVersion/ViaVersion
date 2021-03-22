package us.myles.ViaVersion.api.command;

import us.myles.ViaVersion.util.ChatColorUtil;

import java.util.Collections;
import java.util.List;

public abstract class ViaSubCommand {

    /**
     * Subcommand name
     *
     * @return The commands name
     */
    public abstract String name();

    /**
     * subcommand description, this'll show in /viaversion list
     *
     * @return The commands description
     */
    public abstract String description();

    /**
     * Usage example:
     * "playerversion [name]"
     *
     * @return The commands usage
     */
    public String usage() {
        return name();
    }

    /**
     * Permission, null for everyone
     *
     * @return The permission required to use the commands
     */
    public String permission() {
        return "viaversion.admin";
    }

    /**
     * Gets triggered on execution
     *
     * @param sender Command sender
     * @param args   Arguments
     * @return commands executed succesfully if false, show usage
     */
    public abstract boolean execute(ViaCommandSender sender, String[] args);

    /**
     * Yay, possibility to implement tab-completion
     *
     * @param sender Command sender
     * @param args   args
     * @return tab complete possibilities
     */
    public List<String> onTabComplete(ViaCommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    /**
     * Replaces color codes in a string.
     *
     * @param s string to replace
     * @return output String
     */
    public static String color(String s) {
        return ChatColorUtil.translateAlternateColorCodes(s);
    }

    /**
     * Send a color coded string with replacements to a user.
     *
     * @param sender  target to send the message to
     * @param message message
     * @param args    objects to replace
     */
    public static void sendMessage(ViaCommandSender sender, String message, Object... args) {
        sender.sendMessage(color(args == null ? message : String.format(message, args)));
    }
}
