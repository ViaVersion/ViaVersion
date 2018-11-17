package us.myles.ViaVersion;

import com.google.gson.JsonObject;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaAPI;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.command.ViaCommandSender;
import us.myles.ViaVersion.api.configuration.ConfigurationProvider;
import us.myles.ViaVersion.api.platform.TaskId;
import us.myles.ViaVersion.api.platform.ViaPlatform;
import us.myles.ViaVersion.bukkit.classgenerator.ClassGenerator;
import us.myles.ViaVersion.bukkit.commands.BukkitCommandHandler;
import us.myles.ViaVersion.bukkit.commands.BukkitCommandSender;
import us.myles.ViaVersion.bukkit.platform.*;
import us.myles.ViaVersion.bukkit.util.NMSUtil;
import us.myles.ViaVersion.dump.PluginInfo;
import us.myles.ViaVersion.util.GsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ViaVersionPlugin extends JavaPlugin implements ViaPlatform {

    private BukkitCommandHandler commandHandler;
    private boolean compatSpigotBuild = false;
    private boolean spigot = true;
    private boolean lateBind = false;
    private boolean protocolSupport = false;
    @Getter
    private BukkitViaConfig conf;
    @Getter
    private ViaAPI<Player> api = new BukkitViaAPI(this);
    private List<Runnable> queuedTasks = new ArrayList<>();
    private List<Runnable> asyncQueuedTasks = new ArrayList<>();

    public ViaVersionPlugin() {
        // Command handler
        commandHandler = new BukkitCommandHandler();
        // Init platform
        Via.init(ViaManager.builder()
                .platform(this)
                .commandHandler(commandHandler)
                .injector(new BukkitViaInjector())
                .loader(new BukkitViaLoader(this))
                .build());
        // Config magic
        conf = new BukkitViaConfig();
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
            compatSpigotBuild = NMSUtil.nms("PacketEncoder").getDeclaredField("version") != null;
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

        getCommand("viaversion").setExecutor(commandHandler);
        getCommand("viaversion").setTabCompleter(commandHandler);

        // Warn them if they have anti-xray on and they aren't using spigot
        if (conf.isAntiXRay() && !spigot) {
            getLogger().info("You have anti-xray on in your config, since you're not using spigot it won't fix xray!");
        }

        // Run queued tasks
        for (Runnable r : queuedTasks) {
            Bukkit.getScheduler().runTask(this, r);
        }
        queuedTasks.clear();

        // Run async queued tasks
        for (Runnable r : asyncQueuedTasks) {
            Bukkit.getScheduler().runTaskAsynchronously(this, r);
        }
        asyncQueuedTasks.clear();
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

    public boolean isProtocolSupport() {
        return protocolSupport;
    }

    @Override
    public String getPlatformName() {
        return Bukkit.getServer().getName();
    }

    @Override
    public String getPlatformVersion() {
        return Bukkit.getServer().getVersion();
    }

    @Override
    public String getPluginVersion() {
        return getDescription().getVersion();
    }

    @Override
    public TaskId runAsync(Runnable runnable) {
        if (isPluginEnabled()) {
            return new BukkitTaskId(getServer().getScheduler().runTaskAsynchronously(this, runnable).getTaskId());
        } else {
            asyncQueuedTasks.add(runnable);
            return new BukkitTaskId(null);
        }
    }

    @Override
    public TaskId runSync(Runnable runnable) {
        if (isPluginEnabled()) {
            return new BukkitTaskId(getServer().getScheduler().runTask(this, runnable).getTaskId());
        } else {
            queuedTasks.add(runnable);
            return new BukkitTaskId(null);
        }
    }

    @Override
    public TaskId runSync(Runnable runnable, Long ticks) {
        return new BukkitTaskId(getServer().getScheduler().runTaskLater(this, runnable, ticks).getTaskId());
    }

    @Override
    public TaskId runRepeatingSync(Runnable runnable, Long ticks) {
        return new BukkitTaskId(getServer().getScheduler().runTaskTimer(this, runnable, 0, ticks).getTaskId());
    }

    @Override
    public void cancelTask(TaskId taskId) {
        if (taskId == null) return;
        if (taskId.getObject() == null) return;
        if (taskId instanceof BukkitTaskId) {
            getServer().getScheduler().cancelTask((Integer) taskId.getObject());
        }
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
    public ConfigurationProvider getConfigurationProvider() {
        return conf;
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

    @Override
    public JsonObject getDump() {
        JsonObject platformSpecific = new JsonObject();

        List<PluginInfo> plugins = new ArrayList<>();
        for (Plugin p : Bukkit.getPluginManager().getPlugins())
            plugins.add(new PluginInfo(p.isEnabled(), p.getDescription().getName(), p.getDescription().getVersion(), p.getDescription().getMain(), p.getDescription().getAuthors()));

        platformSpecific.add("plugins", GsonUtil.getGson().toJsonTree(plugins));
        // TODO more? ProtocolLib things etc?

        return platformSpecific;
    }

    @Override
    public boolean isOldClientsAllowed() {
        return !protocolSupport; // Use protocolsupport for older clients
    }
}
