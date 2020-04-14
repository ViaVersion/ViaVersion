package us.myles.ViaVersion.bukkit.listeners.protocol1_15to1_14_4;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.potion.PotionEffectType;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_14;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_14;
import us.myles.ViaVersion.bukkit.listeners.ViaBukkitListener;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.Protocol1_15To1_14_4;

import java.util.Arrays;

public class EntityToggleGlideListener extends ViaBukkitListener {

    public EntityToggleGlideListener(ViaVersionPlugin plugin) {
        super(plugin, Protocol1_15To1_14_4.class);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void entityToggleGlide(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (!isOnPipe(player)) return;

        // Cancelling can only be done by updating the player's metadata
        if (event.isGliding() && event.isCancelled()) {
            PacketWrapper packet = new PacketWrapper(0x44, null, getUserConnection(player));
            try {
                packet.write(Type.VAR_INT, player.getEntityId());

                byte bitmask = 0;
                // Collect other metadata for the mitmask
                if (player.getFireTicks() > 0) {
                    bitmask |= 0x01;
                }
                if (player.isSneaking()) {
                    bitmask |= 0x02;
                }
                // 0x04 is unused
                if (player.isSprinting()) {
                    bitmask |= 0x08;
                }
                if (player.isSwimming()) {
                    bitmask |= 0x10;
                }
                if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    bitmask |= 0x20;
                }
                if (player.isGlowing()) {
                    bitmask |= 0x40;
                }

                // leave 0x80 as 0 to stop gliding
                packet.write(Types1_14.METADATA_LIST, Arrays.asList(new Metadata(0, MetaType1_14.Byte, bitmask)));
                packet.send(Protocol1_15To1_14_4.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
