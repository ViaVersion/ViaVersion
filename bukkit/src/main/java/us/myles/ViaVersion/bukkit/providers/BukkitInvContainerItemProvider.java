package us.myles.ViaVersion.bukkit.providers;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;
import us.myles.ViaVersion.bukkit.protocol1_12to1_11_1.BukkitInvContainerUpdateTask;
import us.myles.ViaVersion.bukkit.util.NMSUtil;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_12to1_11_1.providers.InvContainerItemProvider;
import us.myles.ViaVersion.protocols.protocol1_12to1_11_1.storage.InvItemStorage;
import us.myles.ViaVersion.util.ReflectionUtil;

public class BukkitInvContainerItemProvider extends InvContainerItemProvider {

    private static Map<UUID, BukkitInvContainerUpdateTask> updateTasks = new ConcurrentHashMap<>();
    private boolean supported;
    // packet class
    private Class<?> wclickPacketClass;
    private Object clickTypeEnum;
    // Use for nms
    private Method nmsItemMethod;
    private Method ephandle;
    private Field connection;
    private Method packetMethod;

    public BukkitInvContainerItemProvider() {
        this.supported = isSupported();
        setupReflection();
    }

    @Override
    public boolean registerInvClickPacket(short windowId, short slotId, short anumber, UserConnection uconnection) {
        if (!supported) {
            return false;
        }
        ProtocolInfo info = uconnection.get(ProtocolInfo.class);
        UUID uuid = info.getUuid();
        BukkitInvContainerUpdateTask utask = updateTasks.get(uuid);
        final boolean registered = utask != null;
        if (!registered) {
            utask = new BukkitInvContainerUpdateTask(this, uuid);
            updateTasks.put(uuid, utask);
        }
        // http://wiki.vg/index.php?title=Protocol&oldid=13223#Click_Window
        utask.addItem(windowId, slotId, anumber);
        if (!registered) {
            scheduleTask(utask);
        }
        return true;
    }

    public Object buildWindowClickPacket(Player p, InvItemStorage storage) {
        if (!supported) {
            return null;
        }
        InventoryView inv = p.getOpenInventory();
        short slotId = storage.getSlotId();
        if (slotId < 0) { // clicked out of inv slot
            return null;
        }
        if (slotId > inv.countSlots()) {
            return null; // wrong container open?
        }
        ItemStack itemstack = inv.getItem(slotId);
        if (itemstack == null) {
            return null;
        }
        Object cinstance = null;
        try {
            cinstance = wclickPacketClass.newInstance();
            Object nmsItem = nmsItemMethod.invoke(null, itemstack);
            ReflectionUtil.set(cinstance, "a", (int) storage.getWindowId());
            ReflectionUtil.set(cinstance, "slot", (int) slotId);
            ReflectionUtil.set(cinstance, "button", 0); // shift + left mouse click
            ReflectionUtil.set(cinstance, "d", storage.getActionNumber());
            ReflectionUtil.set(cinstance, "item", nmsItem);
            int protocolId = ProtocolRegistry.SERVER_PROTOCOL;
            if (protocolId == ProtocolVersion.v1_8.getId()) {
                ReflectionUtil.set(cinstance, "shift", 1);
            } else if (protocolId >= ProtocolVersion.v1_9.getId()) { // 1.9+
                ReflectionUtil.set(cinstance, "shift", clickTypeEnum);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cinstance;
    }

    public boolean sendPlayer(Player p, Object packet) {
        if (packet == null) {
            return false;
        }
        try {
            Object entityPlayer = ephandle.invoke(p);
            Object pconnection = connection.get(entityPlayer);
            // send
            packetMethod.invoke(pconnection, packet);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void onTaskExecuted(UUID uuid) {
        updateTasks.remove(uuid);
    }

    private void scheduleTask(BukkitInvContainerUpdateTask utask) {
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        Plugin instance = Bukkit.getServer().getPluginManager().getPlugin("ViaVersion");
        scheduler.runTaskLater(instance, utask, 2); // 2 ticks later (possible double click action).
    }

    private void setupReflection() {
        if (!supported) {
            return;
        }
        try {
            this.wclickPacketClass = NMSUtil.nms("PacketPlayInWindowClick");
            int protocolId = ProtocolRegistry.SERVER_PROTOCOL;
            if (protocolId >= ProtocolVersion.v1_9.getId()) {
                Class<?> eclassz = NMSUtil.nms("InventoryClickType");
                Object[] constants = eclassz.getEnumConstants();
                this.clickTypeEnum = constants[1]; // QUICK_MOVE
            }
            Class<?> citemStack = NMSUtil.obc("inventory.CraftItemStack");
            this.nmsItemMethod = citemStack.getDeclaredMethod("asNMSCopy", ItemStack.class);
        } catch (Exception e) {
            this.supported = false;
            throw new RuntimeException("Couldn't find required inventory classes", e);
        }
        try {
            this.ephandle = NMSUtil.obc("entity.CraftPlayer").getDeclaredMethod("getHandle");
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            this.supported = false;
            throw new RuntimeException("Couldn't find CraftPlayer", e);
        }
        try {
            this.connection = NMSUtil.nms("EntityPlayer").getDeclaredField("playerConnection");
        } catch (NoSuchFieldException | ClassNotFoundException e) {
            this.supported = false;
            throw new RuntimeException("Couldn't find Player Connection", e);
        }
        try {
            this.packetMethod = NMSUtil.nms("PlayerConnection").getDeclaredMethod("a", wclickPacketClass);
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            this.supported = false;
            throw new RuntimeException("Couldn't find CraftPlayer", e);
        }
    }

    private boolean isSupported() {
        int protocolId = ProtocolRegistry.SERVER_PROTOCOL;
        if (protocolId >= ProtocolVersion.v1_8.getId() && protocolId <= ProtocolVersion.v1_11_1.getId()) {
            return true; // 1.8-1.11.2
        }
        // this is not needed on 1.12+ servers
        return false;
    }
}