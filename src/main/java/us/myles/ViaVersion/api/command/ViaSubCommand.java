package us.myles.ViaVersion.api.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.commands.ViaCommandHandler;

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
    public String usage(){
        return name();
    }

    /**
     * Permission, null for everyone
     * @return
     */
    public String permission(){
        return "viaversion.admin";
    }

    public abstract boolean execute(CommandSender sender, String[] args);

    public String color(String s){
        return ViaCommandHandler.color(s);
    }
}
