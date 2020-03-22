package us.myles.ViaVersion.sponge.listeners;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.serializer.TextSerializers;
import us.myles.ViaVersion.api.Via;

public class GeneralListener {

    @Listener
    public void onLogin(ClientConnectionEvent.Login login){
        if(!Via.getManager().isMappingsLoaded()){
            login.setCancelled(true);
            login.setMessage(TextSerializers.JSON.deserialize(
                    ComponentSerializer.toString(
                            TextComponent.fromLegacyText("§cViaVersion has not yet been fully activated")
                    )
            ));
        }
    }

}
