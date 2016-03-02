package us.myles.ViaVersion.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import us.myles.ViaVersion.api.ViaVersion;

/**
 * Created by fillefilip8 on 2016-03-02.
 */
public class VehicleListener implements Listener {
    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent e){
        if(e.getEntered() instanceof Player) {
            Player player = (Player) e.getEntered();
            if(ViaVersion.getInstance().isPorted(player)){

                if(e.getVehicle() instanceof Minecart){
                    e.getEntered().sendMessage(ChatColor.RED + "Minecarts are not supported. Due to changes on minecarts in 1.9");
                    e.setCancelled(true);
                }else if(e.getVehicle() instanceof Boat){
                    e.getEntered().sendMessage(ChatColor.RED + "Boats are not supported. Due to changes on boats in 1.9");
                    e.setCancelled(true);
                }


            }
        }

    }

}
