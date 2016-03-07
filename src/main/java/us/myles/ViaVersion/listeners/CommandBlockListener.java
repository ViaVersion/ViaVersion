package us.myles.ViaVersion.listeners;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.RequiredArgsConstructor;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.packets.PacketType;
import us.myles.ViaVersion.util.PacketUtil;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@RequiredArgsConstructor
public class CommandBlockListener implements Listener {

    private final ViaVersionPlugin plugin;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onJoin(final PlayerJoinEvent e) {
        if (e.getPlayer().isOp() && plugin.isPorted(e.getPlayer())) {
            ByteBuf buf = Unpooled.buffer();
            PacketUtil.writeVarInt(PacketType.PLAY_ENTITY_STATUS.getNewPacketID(), buf);
            buf.writeInt(e.getPlayer().getEntityId());
            buf.writeByte(26);
            plugin.sendRawPacket(e.getPlayer(), buf);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && plugin.isPorted(e.getPlayer()) && e.getPlayer().isOp()) {
            try {
                sendCommandBlockPacket(e.getClickedBlock(), e.getPlayer());
            } catch (NoSuchFieldException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void sendCommandBlockPacket(Block b, Player player) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException {
        if (!(b.getState() instanceof CommandBlock))
            return;
        CommandBlock cmd = (CommandBlock) b.getState();

        Object tileEntityCommand = ReflectionUtil.get(cmd, "commandBlock", ReflectionUtil.nms("TileEntityCommand"));
        Object updatePacket = ReflectionUtil.invoke(tileEntityCommand, "getUpdatePacket");
        Object nmsPlayer = ReflectionUtil.invoke(player, "getHandle");
        Object playerConnection = ReflectionUtil.get(nmsPlayer, "playerConnection", ReflectionUtil.nms("PlayerConnection"));
        Method sendPacket = playerConnection.getClass().getMethod("sendPacket", ReflectionUtil.nms("Packet"));
        sendPacket.invoke(playerConnection, updatePacket); //Let the transformer do the work
    }
}
