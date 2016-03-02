package us.myles.ViaVersion.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import us.myles.ViaVersion.api.ViaVersion;

/**
 * Created by fillefilip8 on 2016-03-02.
 */
public class MinecartListener implements Listener {
    @EventHandler
    public void onMinecartEnter(VehicleEnterEvent e){
        if(e.getEntered() instanceof Player) {
            Player player = (Player) e.getEntered();
            if(ViaVersion.getInstance().isPorted(player)){
                e.setCancelled(true);
                e.getEntered().sendMessage(ChatColor.RED + "Minecarts are not supported. Due to changes on minecarts in 1.9");
            }
        }

    }
}
