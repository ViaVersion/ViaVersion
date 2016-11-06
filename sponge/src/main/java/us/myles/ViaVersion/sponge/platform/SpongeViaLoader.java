package us.myles.ViaVersion.sponge.platform;

import lombok.AllArgsConstructor;
import org.spongepowered.api.Sponge;
import us.myles.ViaVersion.SpongePlugin;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.platform.ViaPlatformLoader;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.BulkChunkTranslatorProvider;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.HandItemProvider;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.MovementTransmitterProvider;
import us.myles.ViaVersion.sponge.listeners.ClientLeaveListener;
import us.myles.ViaVersion.sponge.listeners.UpdateListener;
import us.myles.ViaVersion.sponge.listeners.protocol1_9to1_8.BlockListener;
import us.myles.ViaVersion.sponge.listeners.protocol1_9to1_8.DeathListener;
import us.myles.ViaVersion.sponge.listeners.protocol1_9to1_8.HandItemCache;
import us.myles.ViaVersion.sponge.listeners.protocol1_9to1_8.sponge4.Sponge4ArmorListener;
import us.myles.ViaVersion.sponge.listeners.protocol1_9to1_8.sponge5.Sponge5ArmorListener;
import us.myles.ViaVersion.sponge.providers.SpongeViaBulkChunkTranslator;
import us.myles.ViaVersion.sponge.providers.SpongeViaMovementTransmitter;

@AllArgsConstructor
public class SpongeViaLoader implements ViaPlatformLoader {
    private SpongePlugin plugin;

    @Override
    public void load() {
        // Update Listener
        Sponge.getEventManager().registerListeners(plugin, new UpdateListener());
        /* Base Protocol */
        Sponge.getEventManager().registerListeners(plugin, new ClientLeaveListener());
        /* 1.9 client to 1.8 server */
        try {
            Class.forName("org.spongepowered.api.event.entity.DisplaceEntityEvent");
            new Sponge4ArmorListener().register();
        } catch (ClassNotFoundException e) {
            new Sponge5ArmorListener(plugin).register();
        }
        new DeathListener(plugin).register();
        new BlockListener(plugin).register();

        if (plugin.getConf().isItemCache()) {
            Via.getPlatform().runRepeatingSync(new HandItemCache(), 2L); // Updates player's items :)
            HandItemCache.CACHE = true;
        }

        /* Providers */
        Via.getManager().getProviders().use(BulkChunkTranslatorProvider.class, new SpongeViaBulkChunkTranslator());
        Via.getManager().getProviders().use(MovementTransmitterProvider.class, new SpongeViaMovementTransmitter());
        Via.getManager().getProviders().use(HandItemProvider.class, new HandItemProvider() {
            @Override
            public Item getHandItem(final UserConnection info) {
                if (HandItemCache.CACHE) {
                    return HandItemCache.getHandItem(info.get(ProtocolInfo.class).getUuid());
                } else {
                    return super.getHandItem(info); // TODO: On API Docs write about this
                }
            }
        });
    }
}
