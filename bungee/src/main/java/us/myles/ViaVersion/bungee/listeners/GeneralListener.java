package us.myles.ViaVersion.bungee.listeners;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import us.myles.ViaVersion.api.Via;

public class GeneralListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(LoginEvent e){
        if(!Via.getManager().isMappingsLoaded()){
            e.setCancelled(true);
            e.setCancelReason(TextComponent.fromLegacyText("§cViaVersion has not yet been fully activated"));
        }
    }

}
