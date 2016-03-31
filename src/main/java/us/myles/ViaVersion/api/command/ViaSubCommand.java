package us.myles.ViaVersion.api.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.commands.ViaCommandHandler;

public abstract class ViaSubCommand {
    public abstract String name();

    public abstract String description();

    public String usage(){
        return name();
    }

    public String permission(){
        return "viaversion.admin";
    }

    public abstract boolean execute(CommandSender sender, String[] args);

    public String color(String s){
        return ViaCommandHandler.color(s);
    }
}
