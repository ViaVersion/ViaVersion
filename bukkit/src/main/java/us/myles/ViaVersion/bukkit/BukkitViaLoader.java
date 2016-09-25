package us.myles.ViaVersion.bukkit;

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.platform.ViaPlatformLoader;
import us.myles.ViaVersion.listeners.UpdateListener;
import us.myles.ViaVersion.listeners.protocol1_9to1_8.*;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.ViaIdleThread;

@AllArgsConstructor
public class BukkitViaLoader implements ViaPlatformLoader {
    private ViaVersionPlugin plugin;

    @Override
    public void load() {
        // Update Listener
        Bukkit.getPluginManager().registerEvents(new UpdateListener(), plugin);

        /* Base Protocol */
        final ViaVersionPlugin plugin = (ViaVersionPlugin) Bukkit.getPluginManager().getPlugin("ViaVersion");

        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent e) {
                Via.getManager().removePortedClient(e.getPlayer().getUniqueId());
            }
        }, plugin);

        /* 1.9 client to 1.8 server */
        new ArmorListener(plugin).register();
        new CommandBlockListener(plugin).register();
        new DeathListener(plugin).register();
        new BlockListener(plugin).register();

        if (Bukkit.getVersion().toLowerCase().contains("paper") || Bukkit.getVersion().toLowerCase().contains("taco")) {
            plugin.getLogger().info("Enabling PaperSpigot/TacoSpigot patch: Fixes block placement.");
            new PaperPatch(plugin).register();
        }
        if (plugin.getConf().isStimulatePlayerTick())
            new ViaIdleThread(Via.getManager().getPortedPlayers()).runTaskTimer(plugin, 1L, 1L); // Updates player's idle status
        if (plugin.getConf().isItemCache()) {
            new HandItemCache().runTaskTimerAsynchronously(plugin, 2L, 2L); // Updates player's items :)
            HandItemCache.CACHE = true;
        }
    }
}
