package us.myles.ViaVersion.transformers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.inventory.ItemStack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import us.myles.ViaVersion.CancelException;
import us.myles.ViaVersion.ConnectionInfo;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.handlers.ViaInboundHandler;
import us.myles.ViaVersion.packets.PacketType;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.util.PacketUtil;
import us.myles.ViaVersion.util.ReflectionUtil;

public class IncomingTransformer {
	
	private final ViaInboundHandler handler;
    private final Channel channel;
    private final ConnectionInfo info;

    public IncomingTransformer(ViaInboundHandler viaInboundHandler, Channel channel, ConnectionInfo info) {
    	this.handler = viaInboundHandler;
        this.channel = channel;
        this.info = info;
    }

    public void transform(int packetID, ByteBuf input, ByteBuf output) throws CancelException {
        PacketType packet = PacketType.getIncomingPacket(info.getState(), packetID);
        if (packet == null) {
            System.out.println("incoming packet not found " + packetID + " state: " + info.getState());
            throw new RuntimeException("Incoming Packet not found? " + packetID + " State: " + info.getState() + " Version: " + info.getProtocol());
        }
        int original = packetID;

        if (packet.getPacketID() != -1) {
            packetID = packet.getPacketID();
        }
//        if (packet != PacketType.PLAY_PLAYER_POSITION_LOOK_REQUEST && packet != PacketType.PLAY_KEEP_ALIVE_REQUEST && packet != PacketType.PLAY_PLAYER_POSITION_REQUEST && packet != PacketType.PLAY_PLAYER_LOOK_REQUEST) {
//            System.out.println("Packet Type: " + packet + " New ID: " + packetID + " Original: " + original);
//        }
        if (packet == PacketType.PLAY_TP_CONFIRM || packet == PacketType.PLAY_VEHICLE_MOVE_REQUEST) { //TODO handle client-sided horse riding
            throw new CancelException();
        }
        PacketUtil.writeVarInt(packetID, output);
        if (packet == PacketType.HANDSHAKE) {
            int protVer = PacketUtil.readVarInt(input);
            info.setProtocol(protVer);
            PacketUtil.writeVarInt(protVer <= 102 ? protVer : 47, output); // pretend to be older

            if (protVer <= 102) {
                // not 1.9, remove pipes
                this.handler.remove();
            }
            String serverAddress = PacketUtil.readString(input);
            PacketUtil.writeString(serverAddress, output);

            int serverPort = input.readUnsignedShort();
            output.writeShort(serverPort);

            int nextState = PacketUtil.readVarInt(input);
            PacketUtil.writeVarInt(nextState, output);

            if (nextState == 1) {
                info.setState(State.STATUS);
            }
            if (nextState == 2) {
                info.setState(State.LOGIN);
            }
            return;
        }
        if (packet == PacketType.PLAY_UPDATE_SIGN_REQUEST) {
            Long location = input.readLong();
            output.writeLong(location);
            for (int i = 0; i < 4; i++) {
                String line = PacketUtil.readString(input);
                line = "{\"text\":\"" + line + "\"}";
                PacketUtil.writeString(line, output);
            }
            return;
        }
        if (packet == PacketType.PLAY_TAB_COMPLETE_REQUEST) {
            String text = PacketUtil.readString(input);
            PacketUtil.writeString(text, output);
            input.readBoolean(); // assume command
            output.writeBytes(input);
            return;
        }
        if (packet == PacketType.PLAY_PLAYER_DIGGING) {
            byte status = input.readByte();
            if (status == 6) { // item swap
                throw new CancelException();
            }
            output.writeByte(status);
            // read position
            Long pos = input.readLong();
            output.writeLong(pos);
            short face = input.readUnsignedByte();
            output.writeByte(face);
            return;
        }
        if (packet == PacketType.PLAY_CLICK_WINDOW) {
            // if placed in new slot, reject :)
            int windowID = input.readUnsignedByte();
            short slot = input.readShort();

            byte button = input.readByte();
            short action = input.readShort();
            byte mode = input.readByte();
            if (slot == 45 && windowID == 0) {
                try {
                    Class<?> setSlot = ReflectionUtil.nms("PacketPlayOutSetSlot");
                    Object setSlotPacket = setSlot.getConstructors()[1].newInstance(windowID, slot, null);
                    channel.writeAndFlush(setSlotPacket); // slot is empty
                    slot = -999; // we're evil, they'll throw item on the ground
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }

            }
            output.writeByte(windowID);
            output.writeShort(slot);
            output.writeByte(button);
            output.writeShort(action);
            output.writeByte(mode);
            output.writeBytes(input);
            return;
        }
        if (packet == PacketType.PLAY_CLIENT_SETTINGS) {
            String locale = PacketUtil.readString(input);
            PacketUtil.writeString(locale, output);

            byte view = input.readByte();
            output.writeByte(view);

            int chatMode = PacketUtil.readVarInt(input);
            output.writeByte(chatMode);

            boolean chatColours = input.readBoolean();
            output.writeBoolean(chatColours);

            short skinParts = input.readUnsignedByte();
            output.writeByte(skinParts);

            PacketUtil.readVarInt(input);
            return;
        }
        if (packet == PacketType.PLAY_ANIMATION_REQUEST) {
            PacketUtil.readVarInt(input);
            return;
        }
        if (packet == PacketType.PLAY_USE_ENTITY) {
            int target = PacketUtil.readVarInt(input);
            PacketUtil.writeVarInt(target, output);

            int type = PacketUtil.readVarInt(input);
            PacketUtil.writeVarInt(type, output);
            if (type == 2) {
                float targetX = input.readFloat();
                output.writeFloat(targetX);
                float targetY = input.readFloat();
                output.writeFloat(targetY);
                float targetZ = input.readFloat();
                output.writeFloat(targetZ);
            }
            if (type == 0 || type == 2) {
                PacketUtil.readVarInt(input);
            }
            return;
        }
        if (packet == PacketType.PLAY_PLAYER_BLOCK_PLACEMENT) {
            Long position = input.readLong();
            output.writeLong(position);
            int face = PacketUtil.readVarInt(input);
            output.writeByte(face);
            int hand = PacketUtil.readVarInt(input);

            ItemStack inHand = ViaVersionPlugin.getHandItem(info);
            Object item = null;
            try {
                Method m = ReflectionUtil.obc("inventory.CraftItemStack").getDeclaredMethod("asNMSCopy", ItemStack.class);
                item = m.invoke(null, inHand);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            PacketUtil.writeItem(item, output);

            short curX = input.readUnsignedByte();
            output.writeByte(curX);
            short curY = input.readUnsignedByte();
            output.writeByte(curY);
            short curZ = input.readUnsignedByte();
            output.writeByte(curZ);
            return;
        }
        if (packet == PacketType.PLAY_USE_ITEM) {
            output.clear();
            PacketUtil.writeVarInt(PacketType.PLAY_PLAYER_BLOCK_PLACEMENT.getPacketID(), output);
            // Simulate using item :)
            output.writeLong(-1L);
            output.writeByte(-1);
            // write item in hand
            output.writeShort(-1);

            output.writeByte(-1);
            output.writeByte(-1);
            output.writeByte(-1);
            return;
        }
        output.writeBytes(input);
    }
}
