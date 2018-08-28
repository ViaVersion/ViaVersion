package us.myles.ViaVersion.bungee.providers;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9TO1_8;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.MovementTransmitterProvider;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.MovementTracker;

public class BungeeMovementTransmitter extends MovementTransmitterProvider {
    @Override
    public Object getFlyingPacket() {
        return null;
    }

    @Override
    public Object getGroundPacket() {
        return null;
    }

    public void sendPlayer(UserConnection userConnection) {
        if (userConnection.get(ProtocolInfo.class).getState() == State.PLAY) {
            PacketWrapper wrapper = new PacketWrapper(0x03, null, userConnection);
            wrapper.write(Type.BOOLEAN, userConnection.get(MovementTracker.class).isGround());
            try {
                wrapper.sendToServer(Protocol1_9TO1_8.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // PlayerPackets will increment idle
        }
    }
}
