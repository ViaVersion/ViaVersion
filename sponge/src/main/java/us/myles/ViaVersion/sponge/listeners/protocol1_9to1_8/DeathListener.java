package us.myles.ViaVersion.sponge.listeners.protocol1_9to1_8;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.world.World;
import us.myles.ViaVersion.SpongePlugin;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9TO1_8;
import us.myles.ViaVersion.sponge.listeners.ViaSpongeListener;

import java.util.Optional;

public class DeathListener extends ViaSpongeListener {
    public DeathListener(SpongePlugin plugin) {
        super(plugin, Protocol1_9TO1_8.class);
    }

    @Listener(order = Order.LAST)
    public void onDeath(DestructEntityEvent.Death e) {
        if (!(e.getTargetEntity() instanceof Player))
            return;

        Player p = (Player) e.getTargetEntity();
        if (isOnPipe(p.getUniqueId()) && Via.getConfig().isShowNewDeathMessages() && checkGamerule(p.getWorld())) {
            sendPacket(p, e.getMessage().toPlain());
        }
    }

    public boolean checkGamerule(World w) {
        Optional<String> gamerule = w.getGameRule("showDeathMessages");

        if (gamerule.isPresent()) {
            try {
                return Boolean.parseBoolean(gamerule.get());
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    private void sendPacket(final Player p, final String msg) {
        Via.getPlatform().runSync(new Runnable() {
            @Override
            public void run() {
                PacketWrapper wrapper = new PacketWrapper(0x2C, null, getUserConnection(p.getUniqueId()));
                try {
                    int entityId = getEntityId(p);
                    wrapper.write(Type.VAR_INT, 2); // Event - Entity dead
                    wrapper.write(Type.VAR_INT, entityId); // Player ID
                    wrapper.write(Type.INT, entityId); // Entity ID
                    Protocol1_9TO1_8.FIX_JSON.write(wrapper, msg); // Message

                    wrapper.send(Protocol1_9TO1_8.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
