package us.myles.ViaVersion;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.ViaVersionAPI;
import us.myles.ViaVersion.api.ViaVersionConfig;
import us.myles.ViaVersion.api.boss.BossBar;
import us.myles.ViaVersion.api.boss.BossColor;
import us.myles.ViaVersion.api.boss.BossStyle;
import us.myles.ViaVersion.api.command.ViaVersionCommand;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.boss.ViaBossBar;
import us.myles.ViaVersion.classgenerator.ClassGenerator;
import us.myles.ViaVersion.commands.ViaCommandHandler;
import us.myles.ViaVersion.handlers.ViaVersionInitializer;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.update.UpdateListener;
import us.myles.ViaVersion.update.UpdateUtil;
import us.myles.ViaVersion.util.Configuration;
import us.myles.ViaVersion.util.ListWrapper;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ViaVersionPlugin extends JavaPlugin implements ViaVersionAPI, ViaVersionConfig {

    private final Map<UUID, UserConnection> portedPlayers = new ConcurrentHashMap<>();
    private List<ChannelFuture> injectedFutures = new ArrayList<>();
    private List<Pair<Field, Object>> injectedLists = new ArrayList<>();
    private ViaCommandHandler commandHandler;
    private boolean debug = false;
    private boolean compatSpigotBuild = false;
    private boolean spigot = true;

    @Override
    public void onLoad() {
        ViaVersion.setInstance(this);
        // Config magic
        generateConfig();
        // Handle reloads
        if (System.getProperty("ViaVersion") != null) {
            if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
                getLogger().severe("ViaVersion is already loaded, we're going to kick all the players... because otherwise we'll crash because of ProtocolLib.");
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.kickPlayer("Server reload, please rejoin!");
                }

            } else {
                getLogger().severe("ViaVersion is already loaded, this should work fine... Otherwise reboot the server!!!");

            }
        }
        // Spigot detector
        try {
           Class.forName("org.spigotmc.SpigotConfig");
        } catch (ClassNotFoundException e) {
            spigot = false;
        }
        // Check if it's a spigot build with a protocol mod
        try {
            compatSpigotBuild = ReflectionUtil.nms("PacketEncoder").getDeclaredField("version") != null;
        } catch (Exception e){
            compatSpigotBuild = false;
        }
        // Generate classes needed (only works if it's compat)
        ClassGenerator.generate();

        getLogger().info("ViaVersion " + getDescription().getVersion() + (compatSpigotBuild ? "compat" : "") + " is now loaded, injecting.");
        injectPacketHandler();
    }

    @Override
    public void onEnable() {
        if (isCheckForUpdates())
            UpdateUtil.sendUpdateMessage(this);
        // Gather version :)
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                gatherProtocolVersion();
                // Check if there are any pipes to this version
                if (ProtocolRegistry.SERVER_PROTOCOL != -1) {
                    getLogger().info("ViaVersion detected protocol version: " + ProtocolRegistry.SERVER_PROTOCOL);
                    if (!ProtocolRegistry.isWorkingPipe()) {
                        getLogger().warning("ViaVersion will not function on the current protocol.");
                    }
                }
                ProtocolRegistry.refreshVersions();
            }
        });


        Bukkit.getPluginManager().registerEvents(new UpdateListener(this), this);

        getCommand("viaversion").setExecutor(commandHandler = new ViaCommandHandler());
        getCommand("viaversion").setTabCompleter(commandHandler);

        // Register Protocol Listeners
        ProtocolRegistry.registerListeners();

        // Warn them if they have anti-xray on and they aren't using spigot
        if(isAntiXRay() && !spigot){
            getLogger().info("You have anti-xray on in your config, since you're not using spigot it won't fix xray!");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("ViaVersion is disabling, if this is a reload it may not work.");
        uninject();
    }

    public void gatherProtocolVersion() {
        try {
            Class<?> serverClazz = ReflectionUtil.nms("MinecraftServer");
            Object server = ReflectionUtil.invokeStatic(serverClazz, "getServer");
            Class<?> pingClazz = ReflectionUtil.nms("ServerPing");
            Object ping = null;
            // Search for ping method
            for (Field f : serverClazz.getDeclaredFields()) {
                if (f.getType() != null) {
                    if (f.getType().getSimpleName().equals("ServerPing")) {
                        f.setAccessible(true);
                        ping = f.get(server);
                    }
                }
            }
            if (ping != null) {
                Object serverData = null;
                for (Field f : pingClazz.getDeclaredFields()) {
                    if (f.getType() != null) {
                        if (f.getType().getSimpleName().endsWith("ServerData")) {
                            f.setAccessible(true);
                            serverData = f.get(ping);
                        }
                    }
                }
                if (serverData != null) {
                    int protocolVersion = -1;
                    for (Field f : serverData.getClass().getDeclaredFields()) {
                        if (f.getType() != null) {
                            if (f.getType() == int.class) {
                                f.setAccessible(true);
                                protocolVersion = (int) f.get(serverData);
                            }
                        }
                    }
                    if (protocolVersion != -1) {
                        ProtocolRegistry.SERVER_PROTOCOL = protocolVersion;
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // We couldn't work it out... We'll just use ping and hope for the best...
        }
    }

    public void generateConfig() {
        File file = new File(getDataFolder(), "config.yml");
        if (file.exists()) {
            // Update config options
            Configuration oldConfig = new Configuration(file);
            oldConfig.reload(false); // Load current options from config
            file.delete(); // Delete old config
            saveDefaultConfig(); // Generate new config
            Configuration newConfig = new Configuration(file);
            newConfig.reload(true); // Load default options
            for (String key : oldConfig.getKeys(false)) {
                // Set option in new config if exists
                if (newConfig.contains(key)) {
                    newConfig.set(key, oldConfig.get(key));
                }
            }
            newConfig.save();
        } else {
            saveDefaultConfig();
        }
    }

    public void injectPacketHandler() {
        try {
            Class<?> serverClazz = ReflectionUtil.nms("MinecraftServer");
            Object server = ReflectionUtil.invokeStatic(serverClazz, "getServer");
            Object connection = null;
            for (Method m : serverClazz.getDeclaredMethods()) {
                if (m.getReturnType() != null) {
                    if (m.getReturnType().getSimpleName().equals("ServerConnection")) {
                        if (m.getParameterTypes().length == 0) {
                            connection = m.invoke(server);
                        }
                    }
                }
            }
            if (connection == null) {
                getLogger().warning("We failed to find the ServerConnection? :( What server are you running?");
                return;
            }
            for (Field field : connection.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                final Object value = field.get(connection);
                if (value instanceof List) {
                    // Inject the list
                    List wrapper = new ListWrapper((List) value) {
                        @Override
                        public synchronized void handleAdd(Object o) {
                            synchronized (this) {
                                if (o instanceof ChannelFuture) {
                                    inject((ChannelFuture) o);
                                }
                            }
                        }
                    };
                    injectedLists.add(new Pair<>(field, connection));
                    field.set(connection, wrapper);
                    // Iterate through current list
                    synchronized (wrapper) {
                        for (Object o : (List) value) {
                            if (o instanceof ChannelFuture) {
                                inject((ChannelFuture) o);
                            } else {
                                break; // not the right list.
                            }
                        }
                    }
                }
            }
            System.setProperty("ViaVersion", getDescription().getVersion());
        } catch (Exception e) {
            getLogger().severe("Unable to inject handlers, are you on 1.8? ");
            e.printStackTrace();
        }
    }

    private void inject(ChannelFuture future) {
        try {
            ChannelHandler bootstrapAcceptor = future.channel().pipeline().first();
            try {
                ChannelInitializer<SocketChannel> oldInit = ReflectionUtil.get(bootstrapAcceptor, "childHandler", ChannelInitializer.class);
                ChannelInitializer newInit = new ViaVersionInitializer(oldInit);

                ReflectionUtil.set(bootstrapAcceptor, "childHandler", newInit);
                injectedFutures.add(future);
            } catch (NoSuchFieldException e) {
                // field not found
                throw new Exception("Unable to find childHandler, blame " + bootstrapAcceptor.getClass().getName());
            }
        } catch (Exception e) {
            getLogger().severe("Have you got late-bind enabled with something else? (ProtocolLib?)");
            e.printStackTrace();
        }
    }

    private void uninject() {
        // TODO: Uninject from players currently online to prevent protocol lib issues.
        for (ChannelFuture future : injectedFutures) {
            ChannelHandler bootstrapAcceptor = future.channel().pipeline().first();
            try {
                ChannelInitializer<SocketChannel> oldInit = ReflectionUtil.get(bootstrapAcceptor, "childHandler", ChannelInitializer.class);
                if (oldInit instanceof ViaVersionInitializer) {
                    ReflectionUtil.set(bootstrapAcceptor, "childHandler", ((ViaVersionInitializer) oldInit).getOriginal());
                }
            } catch (Exception e) {
                System.out.println("Failed to remove injection... reload won't work with connections sorry");
            }
        }
        injectedFutures.clear();

        for (Pair<Field, Object> pair : injectedLists) {
            try {
                Object o = pair.getKey().get(pair.getValue());
                if (o instanceof ListWrapper) {
                    pair.getKey().set(pair.getValue(), ((ListWrapper) o).getOriginalList());
                }
            } catch (IllegalAccessException e) {
                System.out.println("Failed to remove injection... reload might not work with connections sorry");
            }
        }

        injectedLists.clear();
    }

    @Override
    public boolean isPorted(Player player) {
        return isPorted(player.getUniqueId());
    }

    @Override
    public int getPlayerVersion(@NonNull Player player) {
        if (!isPorted(player))
            return ProtocolRegistry.SERVER_PROTOCOL;
        return portedPlayers.get(player.getUniqueId()).get(ProtocolInfo.class).getProtocolVersion();
    }

    @Override
    public int getPlayerVersion(@NonNull UUID uuid) {
        if (!isPorted(uuid))
            return ProtocolRegistry.SERVER_PROTOCOL;
        return portedPlayers.get(uuid).get(ProtocolInfo.class).getProtocolVersion();
    }

    @Override
    public boolean isPorted(UUID playerUUID) {
        return portedPlayers.containsKey(playerUUID);
    }

    @Override
    public String getVersion() {
        return getDescription().getVersion();
    }

    public UserConnection getConnection(UUID playerUUID) {
        return portedPlayers.get(playerUUID);
    }

    public UserConnection getConnection(Player player) {
        return portedPlayers.get(player.getUniqueId());
    }

    public void sendRawPacket(Player player, ByteBuf packet) throws IllegalArgumentException {
        sendRawPacket(player.getUniqueId(), packet);
    }

    @Override
    public void sendRawPacket(UUID uuid, ByteBuf packet) throws IllegalArgumentException {
        if (!isPorted(uuid)) throw new IllegalArgumentException("This player is not controlled by ViaVersion!");
        UserConnection ci = portedPlayers.get(uuid);
        ci.sendRawPacket(packet);
    }

    @Override
    public BossBar createBossBar(String title, BossColor color, BossStyle style) {
        return new ViaBossBar(title, 1F, color, style);
    }

    @Override
    public BossBar createBossBar(String title, float health, BossColor color, BossStyle style) {
        return new ViaBossBar(title, health, color, style);
    }

    @Override
    public boolean isDebug() {
        return this.debug;
    }

    public void setDebug(boolean value) {
        this.debug = value;
    }

    @Override
    public ViaVersionCommand getCommandHandler() {
        return commandHandler;
    }

    @Override
    public boolean isCompatSpigotBuild() {
        return compatSpigotBuild;
    }

    @Override
    public SortedSet<Integer> getSupportedVersions() {
        return ProtocolRegistry.getSupportedVersions();
    }

    @Override
    public boolean isSpigot() {
        return this.spigot;
    }

    public boolean isCheckForUpdates() {
        return getConfig().getBoolean("checkforupdates", true);
    }

    public boolean isPreventCollision() {
        return getConfig().getBoolean("prevent-collision", true);
    }

    public boolean isNewEffectIndicator() {
        return getConfig().getBoolean("use-new-effect-indicator", true);
    }

    @Override
    public boolean isShowNewDeathMessages() {
        return getConfig().getBoolean("use-new-deathmessages", false);
    }

    public boolean isSuppressMetadataErrors() {
        return getConfig().getBoolean("suppress-metadata-errors", false);
    }

    public boolean isShieldBlocking() {
        return getConfig().getBoolean("shield-blocking", true);
    }

    public boolean isHologramPatch() {
        return getConfig().getBoolean("hologram-patch", false);
    }

    public boolean isBossbarPatch() {
        return getConfig().getBoolean("bossbar-patch", true);
    }

    public boolean isBossbarAntiflicker() {
        return getConfig().getBoolean("bossbar-anti-flicker", false);
    }

    public boolean isUnknownEntitiesSuppressed() {
        return false;
    }

    public double getHologramYOffset() {
        return getConfig().getDouble("hologram-y", -1D);
    }

    public boolean isBlockBreakPatch() {
        return false;
    }

    @Override
    public int getMaxPPS() {
        return getConfig().getInt("max-pps", 140);
    }

    @Override
    public String getMaxPPSKickMessage() {
        return getConfig().getString("max-pps-kick-msg", "Sending packets too fast? lag?");
    }

    @Override
    public int getTrackingPeriod() {
        return getConfig().getInt("tracking-period", 6);
    }

    @Override
    public int getWarningPPS() {
        return getConfig().getInt("tracking-warning-pps", 120);
    }

    @Override
    public int getMaxWarnings() {
        return getConfig().getInt("tracking-max-warnings", 3);
    }

    @Override
    public String getMaxWarningsKickMessage() {
        return getConfig().getString("tracking-max-kick-msg", "You are sending too many packets, :(");
    }

    @Override
    public boolean isAntiXRay() {
        return getConfig().getBoolean("anti-xray-patch", true);
    }

    @Override
    public boolean isSendSupportedVersions() {
        return getConfig().getBoolean("send-supported-versions", false);
    }

    public boolean isAutoTeam() {
        // Collision has to be enabled first
        return isPreventCollision() && getConfig().getBoolean("auto-team", true);
    }

    public void addPortedClient(UserConnection info) {
        portedPlayers.put(info.get(ProtocolInfo.class).getUuid(), info);
    }

    public void removePortedClient(UUID clientID) {
        portedPlayers.remove(clientID);
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

    public Map<UUID, UserConnection> getPortedPlayers() {
        return portedPlayers;
    }

    public boolean handlePPS(UserConnection info) {
        // Max PPS Checker
        if (getMaxPPS() > 0) {
            if (info.getPacketsPerSecond() >= getMaxPPS()) {
                info.disconnect(getMaxPPSKickMessage());
                return true; // don't send current packet
            }
        }

        // Tracking PPS Checker
        if (getMaxWarnings() > 0 && getTrackingPeriod() > 0) {
            if (info.getSecondsObserved() > getTrackingPeriod()) {
                // Reset
                info.setWarnings(0);
                info.setSecondsObserved(1);
            } else {
                info.setSecondsObserved(info.getSecondsObserved() + 1);
                if (info.getPacketsPerSecond() >= getWarningPPS()) {
                    info.setWarnings(info.getWarnings() + 1);
                }

                if (info.getWarnings() >= getMaxWarnings()) {
                    info.disconnect(getMaxWarningsKickMessage());
                    return true; // don't send current packet
                }
            }
        }
        return false;
    }
}
