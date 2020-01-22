package us.myles.ViaVersion.sponge.platform;

import org.spongepowered.api.Sponge;
import us.myles.ViaVersion.SpongePlugin;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.platform.TaskId;
import us.myles.ViaVersion.api.platform.ViaPlatformLoader;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;
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

import java.util.HashSet;
import java.util.Set;

public class SpongeViaLoader implements ViaPlatformLoader {

    private final SpongePlugin plugin;

    private final Set<Object> listeners = new HashSet<>();
    private final Set<TaskId> tasks = new HashSet<>();

    public SpongeViaLoader(SpongePlugin plugin) {
        this.plugin = plugin;
    }

    private void registerListener(Object listener) {
        Sponge.getEventManager().registerListeners(plugin, storeListener(listener));
    }

    private <T> T storeListener(T listener) {
        listeners.add(listener);
        return listener;
    }

    @Override
    public void load() {
        // Update Listener
        registerListener(new UpdateListener());
        /* Base Protocol */
        registerListener(new ClientLeaveListener());

        /* 1.9 client to 1.8 server */
        if (ProtocolRegistry.SERVER_PROTOCOL < ProtocolVersion.v1_9.getId()) {
            try {
                Class.forName("org.spongepowered.api.event.entity.DisplaceEntityEvent");
                storeListener(new Sponge4ArmorListener()).register();
            } catch (ClassNotFoundException e) {
                storeListener(new Sponge5ArmorListener(plugin)).register();
            }
            storeListener(new DeathListener(plugin)).register();
            storeListener(new BlockListener(plugin)).register();

            if (plugin.getConf().isItemCache()) {
                tasks.add(Via.getPlatform().runRepeatingSync(new HandItemCache(), 2L)); // Updates players items :)
                HandItemCache.CACHE = true;
            }
        }

        /* Providers */
        if (ProtocolRegistry.SERVER_PROTOCOL < ProtocolVersion.v1_9.getId()) {
            Via.getManager().getProviders().use(BulkChunkTranslatorProvider.class, new SpongeViaBulkChunkTranslator());
            Via.getManager().getProviders().use(MovementTransmitterProvider.class, new SpongeViaMovementTransmitter());

            Via.getManager().getProviders().use(HandItemProvider.class, new HandItemProvider() {
                @Override
                public Item getHandItem(final UserConnection info) {
                    if (HandItemCache.CACHE) {
                        return HandItemCache.getHandItem(info.get(ProtocolInfo.class).getUuid());
                    } else {
                        return super.getHandItem(info);
                    }
                }
            });
        }
    }

    public void unload() {
        listeners.forEach(Sponge.getEventManager()::unregisterListeners);
        listeners.clear();
        tasks.forEach(Via.getPlatform()::cancelTask);
        tasks.clear();
    }
}