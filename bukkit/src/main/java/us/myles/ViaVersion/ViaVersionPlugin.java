package us.myles.ViaVersion;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaAPI;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.command.ViaCommandSender;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.platform.ViaPlatform;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.bukkit.BukkitCommandHandler;
import us.myles.ViaVersion.bukkit.BukkitCommandSender;
import us.myles.ViaVersion.bukkit.BukkitViaAPI;
import us.myles.ViaVersion.bukkit.BukkitViaInjector;
import us.myles.ViaVersion.classgenerator.ClassGenerator;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ViaVersionPlugin extends JavaPlugin implements ViaPlatform {

    private BukkitCommandHandler commandHandler;
    private boolean compatSpigotBuild = false;
    private boolean spigot = true;
    private boolean lateBind = false;
    private boolean protocolSupport = false;
    @Getter
    private ViaConfig conf;
    @Getter
    private ViaAPI<Player> api = new BukkitViaAPI(this);

    public ViaVersionPlugin() {
        // Config magic
        conf = new ViaConfig(this);
        // Init platform
        Via.init(this);
        // For compatibility
        ViaVersion.setInstance(this);

        // Check if we're using protocol support too
        protocolSupport = Bukkit.getPluginManager().getPlugin("ProtocolSupport") != null;

        if (protocolSupport) {
            getLogger().info("Hooking into ProtocolSupport, to prevent issues!");
            try {
                BukkitViaInjector.patchLists();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLoad() {
        // Spigot detector
        try {
            Class.forName("org.spigotmc.SpigotConfig");
        } catch (ClassNotFoundException e) {
            spigot = false;
        }

        // Check if it's a spigot build with a protocol mod
        try {
            compatSpigotBuild = ReflectionUtil.nms("PacketEncoder").getDeclaredField("version") != null;
        } catch (Exception e) {
            compatSpigotBuild = false;
        }

        // Generate classes needed (only works if it's compat or ps)
        ClassGenerator.generate();
        lateBind = !BukkitViaInjector.isBinded();

        getLogger().info("ViaVersion " + getDescription().getVersion() + (compatSpigotBuild ? "compat" : "") + " is now loaded" + (lateBind ? ", waiting for boot. (late-bind)" : ", injecting!"));
        if (!lateBind) {
            Via.getManager().init();
        }
    }

    @Override
    public void onEnable() {
        if (lateBind) {
            Via.getManager().init();
        }


        getCommand("viaversion").setExecutor(commandHandler = new BukkitCommandHandler());
        getCommand("viaversion").setTabCompleter(commandHandler);

        // Warn them if they have anti-xray on and they aren't using spigot
        if (conf.isAntiXRay() && !spigot) {
            getLogger().info("You have anti-xray on in your config, since you're not using spigot it won't fix xray!");
        }
    }

    @Override
    public void onDisable() {
        Via.getManager().destroy();
    }

    public boolean isCompatSpigotBuild() {
        return compatSpigotBuild;
    }


    public boolean isSpigot() {
        return this.spigot;
    }

    public void run(final Runnable runnable, boolean wait) {
        try {
            Future f = Bukkit.getScheduler().callSyncMethod(Bukkit.getPluginManager().getPlugin("ViaVersion"), new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    runnable.run();
                    return true;
                }
            });
            if (wait) {
                f.get(10, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            System.out.println("Failed to run task: " + e.getClass().getName());
            if (ViaVersion.getInstance().isDebug())
                e.printStackTrace();
        }
    }

    public boolean isProtocolSupport() {
        return protocolSupport;
    }

    public boolean handlePPS(UserConnection info) {
        // Max PPS Checker
        if (conf.getMaxPPS() > 0) {
            if (info.getPacketsPerSecond() >= conf.getMaxPPS()) {
                info.disconnect(conf.getMaxPPSKickMessage().replace("%pps", ((Long) info.getPacketsPerSecond()).intValue() + ""));
                return true; // don't send current packet
            }
        }

        // Tracking PPS Checker
        if (conf.getMaxWarnings() > 0 && conf.getTrackingPeriod() > 0) {
            if (info.getSecondsObserved() > conf.getTrackingPeriod()) {
                // Reset
                info.setWarnings(0);
                info.setSecondsObserved(1);
            } else {
                info.setSecondsObserved(info.getSecondsObserved() + 1);
                if (info.getPacketsPerSecond() >= conf.getWarningPPS()) {
                    info.setWarnings(info.getWarnings() + 1);
                }

                if (info.getWarnings() >= conf.getMaxWarnings()) {
                    info.disconnect(conf.getMaxWarningsKickMessage().replace("%pps", ((Long) info.getPacketsPerSecond()).intValue() + ""));
                    return true; // don't send current packet
                }
            }
        }
        return false;
    }

    @Override
    public String getPlatformName() {
        return "Bukkit";
    }

    @Override
    public String getPluginVersion() {
        return getDescription().getVersion();
    }

    @Override
    public void runAsync(Runnable runnable) {
        getServer().getScheduler().runTaskAsynchronously(this, runnable);
    }

    @Override
    public void runSync(Runnable runnable) {
        getServer().getScheduler().runTask(this, runnable);
    }

    @Override
    public ViaCommandSender[] getOnlinePlayers() {
        ViaCommandSender[] array = new ViaCommandSender[Bukkit.getOnlinePlayers().size()];
        int i = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            array[i++] = new BukkitCommandSender(player);
        }
        return array;
    }

    @Override
    public void sendMessage(UUID uuid, String message) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            player.sendMessage(message);
        }
    }

    @Override
    public boolean kickPlayer(UUID uuid, String message) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            player.kickPlayer(message);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isPluginEnabled() {
        return Bukkit.getPluginManager().getPlugin("ViaVersion").isEnabled();
    }

    @Override
    public void onReload() {
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            getLogger().severe("ViaVersion is already loaded, we're going to kick all the players... because otherwise we'll crash because of ProtocolLib.");
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.kickPlayer(ChatColor.translateAlternateColorCodes('&', getConf().getReloadDisconnectMsg()));
            }

        } else {
            getLogger().severe("ViaVersion is already loaded, this should work fine. If you get any console errors, try rebooting.");
        }
    }
}
