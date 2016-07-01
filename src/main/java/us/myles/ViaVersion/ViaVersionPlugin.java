package us.myles.ViaVersion;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.ViaVersionAPI;
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
import us.myles.ViaVersion.util.ConcurrentList;
import us.myles.ViaVersion.util.ListWrapper;
import us.myles.ViaVersion.util.ProtocolSupportUtil;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ViaVersionPlugin extends JavaPlugin implements ViaVersionAPI {

    private final Map<UUID, UserConnection> portedPlayers = new ConcurrentHashMap<>();
    private List<ChannelFuture> injectedFutures = new ArrayList<>();
    private List<Pair<Field, Object>> injectedLists = new ArrayList<>();
    private ViaCommandHandler commandHandler;
    private boolean debug = false;
    private boolean compatSpigotBuild = false;
    private boolean spigot = true;
    private boolean lateBind = false;
    private boolean protocolSupport = false;
    @Getter
    private ViaConfig conf;

    public ViaVersionPlugin() {
        // Check if we're using protocol support too
        protocolSupport = Bukkit.getPluginManager().getPlugin("ProtocolSupport") != null;

        if (protocolSupport) {
            getLogger().info("Patching to prevent concurrency issues...");
            try {
                patchLists();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLoad() {
        // Config magic
        conf = new ViaConfig(this);
        ViaVersion.setInstance(this);

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
        } catch (Exception e) {
            compatSpigotBuild = false;
        }

        // Generate classes needed (only works if it's compat or ps)
        ClassGenerator.generate();
        lateBind = !isBinded();

        getLogger().info("ViaVersion " + getDescription().getVersion() + (compatSpigotBuild ? "compat" : "") + " is now loaded" + (lateBind ? ", waiting for boot. (late-bind)" : ", injecting!"));
        if (!lateBind)
            injectPacketHandler();
    }

    @Override
    public void onEnable() {
        if (lateBind)
            injectPacketHandler();
        if (conf.isCheckForUpdates())
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
        if (conf.isAntiXRay() && !spigot) {
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

    public Object getServerConnection() throws Exception {
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
        return connection;
    }

    public void injectPacketHandler() {
        try {
            Object connection = getServerConnection();
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


    public void patchLists() throws Exception {
        Object connection = getServerConnection();
        if (connection == null) {
            getLogger().warning("We failed to find the ServerConnection? :( What server are you running?");
            return;
        }
        for (Field field : connection.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            final Object value = field.get(connection);
            if (value instanceof List) {
                if (!(value instanceof ConcurrentList)) {
                    ConcurrentList list = new ConcurrentList();
                    list.addAll((List) value);
                    field.set(connection, list);
                }
            }
        }
    }


    public boolean isBinded() {
        try {
            Object connection = getServerConnection();
            if (connection == null) {
                return false;
            }
            for (Field field : connection.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                final Object value = field.get(connection);
                if (value instanceof List) {
                    // Inject the list
                    synchronized (value) {
                        for (Object o : (List) value) {
                            if (o instanceof ChannelFuture) {
                                return true;
                            } else {
                                break; // not the right list.
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return false;
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
                // let's find who to blame!
                ClassLoader cl = bootstrapAcceptor.getClass().getClassLoader();
                if (cl.getClass().getName().equals("org.bukkit.plugin.java.PluginClassLoader")) {
                    PluginDescriptionFile yaml = ReflectionUtil.get(cl, "description", PluginDescriptionFile.class);
                    throw new Exception("Unable to inject, due to " + bootstrapAcceptor.getClass().getName() + ", try without the plugin " + yaml.getName() + "?");
                } else {
                    throw new Exception("Unable to find childHandler, weird server version? " + bootstrapAcceptor.getClass().getName());
                }

            }
        } catch (Exception e) {
            getLogger().severe("Have you got late-bind enabled with something else?");
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
            return getExternalVersion(player);
        return portedPlayers.get(player.getUniqueId()).get(ProtocolInfo.class).getProtocolVersion();
    }

    @Override
    public int getPlayerVersion(@NonNull UUID uuid) {
        if (!isPorted(uuid))
            return getExternalVersion(Bukkit.getPlayer(uuid));
        return portedPlayers.get(uuid).get(ProtocolInfo.class).getProtocolVersion();
    }

    private int getExternalVersion(Player player) {
        if (!isProtocolSupport()) {
            return ProtocolRegistry.SERVER_PROTOCOL;
        } else {
            return ProtocolSupportUtil.getProtocolVersion(player);
        }
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
        SortedSet<Integer> outputSet = new TreeSet<>(ProtocolRegistry.getSupportedVersions());
        outputSet.removeAll(getConf().getBlockedProtocols());

        return outputSet;
    }

    @Override
    public boolean isSpigot() {
        return this.spigot;
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

    public boolean isProtocolSupport() {
        return protocolSupport;
    }

    public Map<UUID, UserConnection> getPortedPlayers() {
        return portedPlayers;
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
}
