package us.myles.ViaVersion.bukkit.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import us.myles.ViaVersion.api.Via;

public class GeneralListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogin(AsyncPlayerPreLoginEvent e){
        if(!Via.getManager().isMappingsLoaded()){
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§cViaVersion has not yet been fully activated");
        }
    }
}
