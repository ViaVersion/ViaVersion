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
import us.myles.ViaVersion.commands.ViaCommandHandler;
import us.myles.ViaVersion.handlers.ViaVersionInitializer;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.update.UpdateListener;
import us.myles.ViaVersion.update.UpdateUtil;
import us.myles.ViaVersion.util.Configuration;
import us.myles.ViaVersion.util.ListWrapper;
import us.myles.ViaVersion.util.ReflectionUtil;
import us.myles.ViaVersion.util.filter.FieldFilter;
import us.myles.ViaVersion.util.filter.MethodFilter;
import us.myles.ViaVersion.util.filter.ResultIterator;
import us.myles.ViaVersion.util.filter.Searcher;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

    @Override
    public void onLoad() {
        ViaVersion.setInstance(this);
        generateConfig();
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

        getLogger().info("ViaVersion " + getDescription().getVersion() + " is now loaded, injecting.");
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
            }
        });


        Bukkit.getPluginManager().registerEvents(new UpdateListener(this), this);

        getCommand("viaversion").setExecutor(commandHandler = new ViaCommandHandler());
        getCommand("viaversion").setTabCompleter(commandHandler);

        // Register Protocol Listeners
        ProtocolRegistry.registerListeners();
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

            Searcher searcher = new Searcher(serverClazz, server)
                    .search(new FieldFilter() {
                        @Override
                        public boolean filter(Field field, Object in) throws Exception {
                            return field.getType() != null && field.getType().getSimpleName().equals("ServerPing");
                        }
                    });

            if (searcher.checkNull()) {
                getLogger().warning("Unable to locate ServerPing D:");
                return;
            }

            searcher = searcher.nextSearcher(ReflectionUtil.nms("ServerPing"))
                    .search(new FieldFilter() {
                        @Override
                        public boolean filter(Field field, Object in) throws Exception {
                            return field.getType() != null && field.getType().getSimpleName().equalsIgnoreCase("ServerPingServerData");
                        }
                    });

            if (searcher.checkNull()){
                getLogger().warning("Unable to locate serverData, this version may be incompatible with ViaVersion!");
                return;
            }

            int protocolVersion;
            protocolVersion = (Integer) searcher.nextSearcher().search(new FieldFilter() {
                @Override
                public boolean filter(Field f, Object in) throws Exception {
                    return f.getType() != null && f.getType() == int.class;
                }
            }).finalResult();

            ProtocolRegistry.SERVER_PROTOCOL = (protocolVersion == -1 ? ProtocolRegistry.SERVER_PROTOCOL : protocolVersion);

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

            Searcher searcher = new Searcher(serverClazz, server);

            searcher.search(new MethodFilter() {
                @Override
                public boolean filter(Method m, Object in) {
                    return m.getReturnType() != null && m.getReturnType().getSimpleName().equals("ServerConnection") && m.getParameterTypes().length == 0;
                }
            });

            if (searcher.checkNull()) {
                getLogger().severe("We failed to find the ServerConnection? :( What server are you running?");
                return;
            }


            final Object connection = searcher.finalResult();
            searcher = searcher.nextSearcher();

            searcher.iterator(new ResultIterator<Field>() {
                @Override
                public void iterate(Field match) throws Exception{
                    final Object value = match.get(connection);

                    List wrapper = new ListWrapper((List) value) {
                        @Override
                        public synchronized void handleAdd(Object o) {
                            synchronized (this) {
                                inject((ChannelFuture) o);
                            }
                        }
                    };

                    injectedLists.add(new Pair<>(match, connection));
                    match.set(connection, wrapper);

                    synchronized (wrapper) {
                        for (ChannelFuture o : (List<ChannelFuture>) value) {
                            inject(o);
                        }
                    }
                }
            });

            searcher.search(new FieldFilter() {
                @Override
                public boolean filter(Field field, Object in) throws Exception {
                    return field.getType().getSimpleName().equals("List") && !((List) field.get(in)).isEmpty() && ((List) field.get(in)).get(0) instanceof ChannelFuture;
                }
            });

            System.setProperty("ViaVersion", getDescription().getVersion());
        } catch (Exception e) {
            getLogger().severe("Unable to inject handlers, are you on 1.8?");
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
        return getConfig().getBoolean("suppress-entityid-errors", false);
    }

    public double getHologramYOffset() {
        return getConfig().getDouble("hologram-y", -1D);
    }

    public boolean isBlockBreakPatch() {
        return getConfig().getBoolean("block-break-patch", true);
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
