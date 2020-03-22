package us.myles.ViaVersion.velocity.listeners;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import net.kyori.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import us.myles.ViaVersion.api.Via;

public class GeneralListener {
    @Subscribe
    public void onLogin(LoginEvent e){
        if(!Via.getManager().isMappingsLoaded()){
            e.setResult(ResultedEvent.ComponentResult.denied(GsonComponentSerializer.INSTANCE.deserialize(
                    ComponentSerializer.toString(TextComponent.fromLegacyText("§cViaVersion has not yet been fully activated"))
            )));
        }
    }
}
