package us.myles.ViaVersion.api.command;

import us.myles.ViaVersion.commands.ViaCommandHandler;

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

    public String color(String s) {
        return ViaCommandHandler.color(s);
    }

    /**
     * Send message formatted / colored
     *
     * @param sender  commands sender
     * @param message string message
     * @param args    optional objects
     */
    public void sendMessage(ViaCommandSender sender, String message, Object... args) {
        ViaCommandHandler.sendMessage(sender, message, args);
    }
}
