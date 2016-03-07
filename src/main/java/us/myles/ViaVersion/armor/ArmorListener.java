package us.myles.ViaVersion.armor;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.CraftingInventory;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.packets.PacketType;

import java.util.UUID;

import static us.myles.ViaVersion.util.PacketUtil.*;

@RequiredArgsConstructor
public class ArmorListener implements Listener {

    private static final UUID ARMOR_ATTRIBUTE = UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150");
    private final ViaVersionPlugin plugin;

    public static void sendArmorUpdate(Player player) {
        int armor = ArmorType.calculateArmorPoints(player.getInventory().getArmorContents());

        ByteBuf buf = Unpooled.buffer();
        writeVarInt(PacketType.PLAY_ENTITY_PROPERTIES.getNewPacketID(), buf);
        writeVarInt(player.getEntityId(), buf);
        buf.writeInt(1); // only 1 property
        writeString("generic.armor", buf);
        buf.writeDouble(0); //default 0 armor
        writeVarInt(1, buf); // 1 modifier
        writeUUID(ARMOR_ATTRIBUTE, buf); // armor modifier uuid
        buf.writeDouble((double) armor); // the modifier value
        buf.writeByte(0); // the modifier operation, 0 is add number

        ViaVersion.getInstance().sendRawPacket(player, buf);
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
                if (ArmorType.isArmor(e.getMaterial())) {
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
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        sendDelayedArmorUpdate(e.getPlayer());
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