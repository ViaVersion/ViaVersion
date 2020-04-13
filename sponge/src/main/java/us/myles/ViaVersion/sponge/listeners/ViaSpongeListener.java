package us.myles.ViaVersion.sponge.listeners;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import us.myles.ViaVersion.SpongePlugin;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaListener;
import us.myles.ViaVersion.api.protocol.Protocol;

import java.lang.reflect.Field;

public class ViaSpongeListener extends ViaListener {
    private static Field entityIdField;

    private final SpongePlugin plugin;

    public ViaSpongeListener(SpongePlugin plugin, Class<? extends Protocol> requiredPipeline) {
        super(requiredPipeline);
        this.plugin = plugin;
    }

    @Override
    public void register() {
        if (isRegistered()) return;

        Sponge.getEventManager().registerListeners(plugin, this);
        setRegistered(true);
    }

    // Hey sponge, please create a getEntityId method :'(
    protected int getEntityId(Player p) {
        try {
            if (entityIdField == null) {
                entityIdField = p.getClass().getSuperclass().getSuperclass().getSuperclass().getDeclaredField("field_145783_c");
                entityIdField.setAccessible(true);
            }

            return entityIdField.getInt(p);
        } catch (Exception e) {
            Via.getPlatform().getLogger().severe("Could not get the entity id, please report this on our Github");
            e.printStackTrace();
        }

        Via.getPlatform().getLogger().severe("Could not get the entity id, please report this on our Github");
        return -1;
    }

    public SpongePlugin getPlugin() {
        return plugin;
    }
}
