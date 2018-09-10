package us.myles.ViaVersion.bukkit.platform;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.platform.ViaPlatformLoader;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;
import us.myles.ViaVersion.bukkit.listeners.UpdateListener;
import us.myles.ViaVersion.bukkit.listeners.protocol1_9to1_8.*;
import us.myles.ViaVersion.bukkit.providers.BukkitInventoryQuickMoveProvider;
import us.myles.ViaVersion.bukkit.providers.BukkitViaBulkChunkTranslator;
import us.myles.ViaVersion.bukkit.providers.BukkitViaMovementTransmitter;
import us.myles.ViaVersion.bukkit.classgenerator.ClassGenerator;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_12to1_11_1.providers.InventoryQuickMoveProvider;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.BulkChunkTranslatorProvider;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.HandItemProvider;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.MovementTransmitterProvider;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class BukkitViaLoader implements ViaPlatformLoader {
    private ViaVersionPlugin plugin;

    private Set<Listener> listeners = new HashSet<>();
    private Set<BukkitTask> tasks = new HashSet<>();

    public BukkitViaLoader(ViaVersionPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(storeListener(listener), plugin);
    }

    public <T extends Listener> T storeListener(T listener) {
        listeners.add(listener);
        return listener;
    }

    @Override
    public void load() {
        // Update Listener
        registerListener(new UpdateListener());

        /* Base Protocol */
        final ViaVersionPlugin plugin = (ViaVersionPlugin) Bukkit.getPluginManager().getPlugin("ViaVersion");

        // Add ProtocolSupport ConnectListener if necessary.
        ClassGenerator.registerPSConnectListener(plugin);

        registerListener(new Listener() {
            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent e) {
                Via.getManager().removePortedClient(e.getPlayer().getUniqueId());
            }
        });

        /* 1.9 client to 1.8 server */

        storeListener(new ArmorListener(plugin)).register();
        storeListener(new DeathListener(plugin)).register();
        storeListener(new BlockListener(plugin)).register();

        if ((Bukkit.getVersion().toLowerCase().contains("paper")
                || Bukkit.getVersion().toLowerCase().contains("taco")
                || Bukkit.getVersion().toLowerCase().contains("torch"))
				&& ProtocolRegistry.SERVER_PROTOCOL < ProtocolVersion.v1_12.getId()) {
            plugin.getLogger().info("Enabling Paper/TacoSpigot/Torch patch: Fixes block placement.");
            storeListener(new PaperPatch(plugin)).register();
        }
        if (plugin.getConf().isItemCache()) {
            tasks.add(new HandItemCache().runTaskTimerAsynchronously(plugin, 2L, 2L)); // Updates player's items :)
            HandItemCache.CACHE = true;
        }

        /* Providers */
        Via.getManager().getProviders().use(BulkChunkTranslatorProvider.class, new BukkitViaBulkChunkTranslator());
        Via.getManager().getProviders().use(MovementTransmitterProvider.class, new BukkitViaMovementTransmitter());
        if (plugin.getConf().is1_12QuickMoveActionFix()) {
            Via.getManager().getProviders().use(InventoryQuickMoveProvider.class, new BukkitInventoryQuickMoveProvider());
        }
        Via.getManager().getProviders().use(HandItemProvider.class, new HandItemProvider() {
            @Override
            public Item getHandItem(final UserConnection info) {
                if (HandItemCache.CACHE) {
                    return HandItemCache.getHandItem(info.get(ProtocolInfo.class).getUuid());
                } else {
                    try {
                        return Bukkit.getScheduler().callSyncMethod(Bukkit.getPluginManager().getPlugin("ViaVersion"), new Callable<Item>() {
                            @Override
                            public Item call() throws Exception {
                                UUID playerUUID = info.get(ProtocolInfo.class).getUuid();
                                if (Bukkit.getPlayer(playerUUID) != null) {
                                    return HandItemCache.convert(Bukkit.getPlayer(playerUUID).getItemInHand());
                                }
                                return null;
                            }
                        }).get(10, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        Via.getPlatform().getLogger().severe("Error fetching hand item: " + e.getClass().getName());
                        if (Via.getManager().isDebug())
                            e.printStackTrace();
                        return null;
                    }
                }
            }
        });

    }

    @Override
    public void unload() {
        for (Listener listener : listeners) {
            HandlerList.unregisterAll(listener);
        }
        listeners.clear();
        for (BukkitTask task : tasks) {
            task.cancel();
        }
        tasks.clear();
    }
}
