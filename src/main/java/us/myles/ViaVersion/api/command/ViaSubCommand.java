package us.myles.ViaVersion.api.command;

import org.bukkit.command.CommandSender;
import us.myles.ViaVersion.commands.ViaCommandHandler;

import java.util.Collections;
import java.util.List;

public abstract class ViaSubCommand {
    /**
     * Subcommand name
     *
     * @return your input
     */
    public abstract String name();

    /**
     * subcommand description, this'll show in /viaversion list
     *
     * @return your input
     */
    public abstract String description();

    /**
     * Usage example:
     * "playerversion [name]"
     *
     * @return your input
     */
    public String usage() {
        return name();
    }

    /**
     * Permission, null for everyone
     *
     * @return
     */
    public String permission() {
        return "viaversion.admin";
    }

    /**
     * Gets triggered on execution
     *
     * @param sender Command sender
     * @param args   Arguments
     * @return command executed succesfully if false, show usage
     */
    public abstract boolean execute(CommandSender sender, String[] args);

    /**
     * Yay, possibility to implement tab-completion
     *
     * @param sender Command sender
     * @param args   args
     * @return tab complete possibilities
     */
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    public String color(String s) {
        return ViaCommandHandler.color(s);
    }

    /**
     * Send message formatted / colored
     *
     * @param sender  command sender
     * @param message string message
     * @param args    optional objects
     */
    public void sendMessage(CommandSender sender, String message, Object... args) {
        ViaCommandHandler.sendMessage(sender, message, args);
    }
}
