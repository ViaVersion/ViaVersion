package us.myles.ViaVersion.protocols.protocol1_9to1_8.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.CraftingInventory;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.ArmorType;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9TO1_8;

import java.util.UUID;

@RequiredArgsConstructor
public class ArmorListener implements Listener {

    private static final UUID ARMOR_ATTRIBUTE = UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150");
    private final ViaVersionPlugin plugin;

    public static void sendArmorUpdate(Player player) {
        // Ensure that the player is on our pipe
        UserConnection userConnection = ((ViaVersionPlugin) ViaVersion.getInstance()).getConnection(player);
        if (userConnection == null) return;
        if (!userConnection.get(ProtocolInfo.class).getPipeline().contains(Protocol1_9TO1_8.class)) return;

        int armor = ArmorType.calculateArmorPoints(player.getInventory().getArmorContents());

        PacketWrapper wrapper = new PacketWrapper(0x4B, null, userConnection);
        try {
            wrapper.write(Type.VAR_INT, player.getEntityId()); // Player ID
            wrapper.write(Type.INT, 1); // only 1 property
            wrapper.write(Type.STRING, "generic.armor");
            wrapper.write(Type.DOUBLE, 0D); //default 0 armor
            wrapper.write(Type.VAR_INT, 1); // 1 modifier
            wrapper.write(Type.UUID, ARMOR_ATTRIBUTE); // armor modifier uuid
            wrapper.write(Type.DOUBLE, (double) armor); // the modifier value
            wrapper.write(Type.BYTE, (byte) 0);// the modifier operation, 0 is add number

            wrapper.send(Protocol1_9TO1_8.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        HumanEntity human = e.getWhoClicked();
        if (human instanceof Player && e.getInventory() instanceof CraftingInventory) {
            final Player player = (Player) human;
            if (ViaVersion.getInstance().isPorted(player)) {
                if (e.getCurrentItem() != null) {
                    if (ArmorType.isArmor(e.getCurrentItem().getType())) {
                        sendDelayedArmorUpdate(player);
                        return;
                    }
                }
                if (e.getRawSlot() >= 5 && e.getRawSlot() <= 8) {
                    sendDelayedArmorUpdate(player);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getItem() != null) {
            if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                final Player player = e.getPlayer();
                // Due to odd bugs it's 3 ticks later
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        if (ViaVersion.getInstance().isPorted(player)) {
                            sendArmorUpdate(player);
                        }
                    }
                }, 3L);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        sendDelayedArmorUpdate(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRespawn(PlayerRespawnEvent e) {
        if (ViaVersion.getInstance().isPorted(e.getPlayer()))
            sendDelayedArmorUpdate(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        if (ViaVersion.getInstance().isPorted(e.getPlayer()))
            sendArmorUpdate(e.getPlayer());
    }

    public void sendDelayedArmorUpdate(final Player player) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (ViaVersion.getInstance().isPorted(player)) {
                    sendArmorUpdate(player);
                }
            }
        });
    }
}