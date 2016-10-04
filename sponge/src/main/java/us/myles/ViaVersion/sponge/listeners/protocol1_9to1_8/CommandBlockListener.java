package us.myles.ViaVersion.sponge.listeners.protocol1_9to1_8;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import org.spacehq.opennbt.tag.builtin.ByteTag;
import org.spacehq.opennbt.tag.builtin.CompoundTag;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.CommandBlock;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import us.myles.ViaVersion.SpongePlugin;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9TO1_8;
import us.myles.ViaVersion.sponge.listeners.ViaSpongeListener;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.util.Optional;

// TODO Change to bytebuf to not use reflection bullsh*t
public class CommandBlockListener extends ViaSpongeListener {
    public CommandBlockListener(SpongePlugin plugin) {
        super(plugin, Protocol1_9TO1_8.class);
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join e) {
        sendOp(e.getTargetEntity());
    }

    @Listener
    public void onRespawn(RespawnPlayerEvent e) {
        if (!isOnPipe(e.getTargetEntity().getUniqueId())) return;

        Sponge.getScheduler().createTaskBuilder().delayTicks(1).execute(new Runnable() {
            @Override
            public void run() {
                sendOp(e.getTargetEntity());
            }
        }).submit(getPlugin());
    }

    @Listener
    public void onInteract(InteractBlockEvent e, @Root Player player) {
        Optional<Location<World>> location = e.getTargetBlock().getLocation();
        if (!location.isPresent()) return;
        Optional<TileEntity> optTile = location.get().getTileEntity();
        if (!optTile.isPresent()) return;
        TileEntity block = optTile.get();
        if (block instanceof CommandBlock) {
            CommandBlock cmd = (CommandBlock) block;
            try {
                sendCommandBlockPacket(cmd, player);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

//  TODO Change world
//    @EventHandler(priority = EventPriority.MONITOR)
//    public void onWorldChange(PlayerChangedWorldEvent e) {
//        sendOp(e.getPlayer());
//    }

//    @EventHandler(ignoreCancelled = true)
//    public void onInteract(PlayerInteractEvent e) {
//        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && isOnPipe(e.getPlayer()) && e.getPlayer().isOp()) {
//            try {
//                sendCommandBlockPacket(e.getClickedBlock(), e.getPlayer());
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//    }

    private void sendOp(Player p) {
        // TODO Is there an isOp check?
        if (p.hasPermission("viaversion.commandblocks") && isOnPipe(p.getUniqueId())) {
            try {
                PacketWrapper wrapper = new PacketWrapper(0x1B, null, getUserConnection(p.getUniqueId())); // Entity status

                wrapper.write(Type.INT, getEntityId(p)); // Entity ID
                wrapper.write(Type.BYTE, (byte) 26); //Hardcoded op permission level

                wrapper.send(Protocol1_9TO1_8.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendCommandBlockPacket(CommandBlock b, Player player) throws Exception {
        Method m = b.getClass().getDeclaredMethod("func_145844_m");
        m.setAccessible(true);

        Object updatePacket = m.invoke(b);

        PacketWrapper wrapper = generatePacket(updatePacket, getUserConnection(player.getUniqueId()));
        wrapper.send(Protocol1_9TO1_8.class);
    }

    //
    private PacketWrapper generatePacket(Object updatePacket, UserConnection usr) throws Exception {
        PacketWrapper wrapper = new PacketWrapper(0x09, null, usr); // Update block entity

        long[] pos = getPosition(ReflectionUtil.get(updatePacket, "field_179824_a", Class.forName("net.minecraft.util.BlockPos")));

        wrapper.write(Type.POSITION, new Position(pos[0], pos[1], pos[2])); //Block position
        wrapper.write(Type.BYTE, (byte) 2); // Action id always 2

        CompoundTag nbt = getNBT(ReflectionUtil.get(updatePacket, "field_148860_e", Class.forName("net.minecraft.nbt.NBTTagCompound")));
        if (nbt == null) {
            wrapper.write(Type.BYTE, (byte) 0); //If nbt is null. Use 0 as nbt
            return wrapper;
        }
        nbt.put(new ByteTag("powered", (byte) 0));
        nbt.put(new ByteTag("auto", (byte) 0));
        nbt.put(new ByteTag("conditionMet", (byte) 0));

        wrapper.write(Type.NBT, nbt); // NBT TAG
        return wrapper;
    }

    private long[] getPosition(Object obj) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        return new long[]{
                (long) ReflectionUtil.getSuper(obj, "field_177962_a", int.class), //X
                (long) ReflectionUtil.getSuper(obj, "field_177960_b", int.class), //Y
                (long) ReflectionUtil.getSuper(obj, "field_177961_c", int.class)  //Z
        };
    }

    private CompoundTag getNBT(Object obj) throws Exception {
        ByteBuf buf = Unpooled.buffer();
        Method m = Class.forName("net.minecraft.nbt.CompressedStreamTools").getMethod("func_74800_a", Class.forName("net.minecraft.nbt.NBTTagCompound"), DataOutput.class);
        m.invoke(null, obj, new DataOutputStream(new ByteBufOutputStream(buf)));
        try {
            return Type.NBT.read(buf);
        } finally {
            buf.release();
        }
    }
}
