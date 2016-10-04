package us.myles.ViaVersion.protocols.protocol1_9to1_8;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.MovementTransmitterProvider;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.MovementTracker;

public class ViaIdleThread implements Runnable {
    @Override
    public void run() {
        for (UserConnection info : Via.getManager().getPortedPlayers().values()) {
            if (info.has(ProtocolInfo.class) && info.get(ProtocolInfo.class).getPipeline().contains(Protocol1_9TO1_8.class)) {
                long nextIdleUpdate = info.get(MovementTracker.class).getNextIdlePacket();
                if (nextIdleUpdate <= System.currentTimeMillis()) {
                    if (info.getChannel().isOpen()) {
                        Via.getManager().getProviders().get(MovementTransmitterProvider.class).sendPlayer(info);
                    }
                }
            }
        }
    }
}
