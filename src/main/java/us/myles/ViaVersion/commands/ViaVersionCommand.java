package us.myles.ViaVersion.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.api.ViaVersion;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fillefilip8 on 2016-03-03.
 */
public class ViaVersionCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("viaversion.admin")) {
            if (args.length == 0) {
                sender.sendMessage(color("&aViaVersion &c" + ViaVersion.getInstance().getVersion()));
                sender.sendMessage(color("&6Commands:"));
                sender.sendMessage(color("&2/viaversion list &7- &6Shows lists of all 1.9 clients and 1.8 clients."));
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("list")) {
                    List<String> portedPlayers = new ArrayList<String>();
                    List<String> normalPlayers = new ArrayList<String>();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (ViaVersion.getInstance().isPorted(p)) {
                            portedPlayers.add(p.getName());
                        } else {
                            normalPlayers.add(p.getName());
                        }
                    }

                    sender.sendMessage(color("&8[&61.9&8]: &b" + portedPlayers.toString()));
                    sender.sendMessage(color("&8[&61.8&8]: &b" + normalPlayers.toString()));
                }
            }

        }
        return false;
    }
    public String color(String string){
        return string.replace("&", "§");
    }
}
