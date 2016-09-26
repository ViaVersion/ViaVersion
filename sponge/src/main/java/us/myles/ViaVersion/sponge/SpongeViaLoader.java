package us.myles.ViaVersion.sponge;

import lombok.AllArgsConstructor;
import org.spongepowered.api.Sponge;
import us.myles.ViaVersion.SpongePlugin;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.platform.ViaPlatformLoader;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.BulkChunkTranslatorProvider;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.MovementTransmitterProvider;
import us.myles.ViaVersion.sponge.listeners.ClientLeaveListener;
import us.myles.ViaVersion.sponge.listeners.UpdateListener;
import us.myles.ViaVersion.sponge.providers.SpongeViaBulkChunkTranslator;
import us.myles.ViaVersion.sponge.providers.SpongeViaMovementTransmitter;

@AllArgsConstructor
public class SpongeViaLoader implements ViaPlatformLoader {
    private SpongePlugin plugin;

    @Override
    public void load() {
        // Update Listener
        Sponge.getEventManager().registerListeners(plugin, new UpdateListener());
//
        /* Base Protocol */
        Sponge.getEventManager().registerListeners(plugin, new ClientLeaveListener());
//        /* 1.9 client to 1.8 server */
//
//        new ArmorListener(plugin).register();
//        new CommandBlockListener(plugin).register();
//        new DeathListener(plugin).register();
//        new BlockListener(plugin).register();
//
//        if (Bukkit.getVersion().toLowerCase().contains("paper") || Bukkit.getVersion().toLowerCase().contains("taco")) {
//            plugin.getLogger().info("Enabling PaperSpigot/TacoSpigot patch: Fixes block placement.");
//            new PaperPatch(plugin).register();
//        }
//        if (plugin.getConf().isItemCache()) {
//            new HandItemCache().runTaskTimerAsynchronously(plugin, 2L, 2L); // Updates player's items :)
//            HandItemCache.CACHE = true;
//        }
//
//        /* Providers */
        Via.getManager().getProviders().use(BulkChunkTranslatorProvider.class, new SpongeViaBulkChunkTranslator());
        Via.getManager().getProviders().use(MovementTransmitterProvider.class, new SpongeViaMovementTransmitter());
//        Via.getManager().getProviders().use(HandItemProvider.class, new HandItemProvider() {
//            @Override
//            public Item getHandItem(final UserConnection info) {
//                if (HandItemCache.CACHE) {
//                    return HandItemCache.getHandItem(info.get(ProtocolInfo.class).getUuid());
//                } else {
//                    try {
//                        return Bukkit.getScheduler().callSyncMethod(Bukkit.getPluginManager().getPlugin("ViaVersion"), new Callable<Item>() {
//                            @Override
//                            public Item call() throws Exception {
//                                UUID playerUUID = info.get(ProtocolInfo.class).getUuid();
//                                if (Bukkit.getPlayer(playerUUID) != null) {
//                                    return HandItemCache.convert(Bukkit.getPlayer(playerUUID).getItemInHand());
//                                }
//                                return null;
//                            }
//                        }).get(10, TimeUnit.SECONDS);
//                    } catch (Exception e) {
//                        System.out.println("Error fetching hand item: " + e.getClass().getName());
//                        if (Via.getManager().isDebug())
//                            e.printStackTrace();
//                        return null;
//                    }
//                }
//            }
//        });
    }
}
