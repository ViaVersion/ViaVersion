package us.myles.ViaVersion.protocols.protocol1_13to1_12_2;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.TabCompleteTracker;

public class TabCompleteThread implements Runnable {
    @Override
    public void run() {
        for (UserConnection info : Via.getManager().getConnections()) {
            if (info.getProtocolInfo() == null) continue;
            if (info.getProtocolInfo().getPipeline().contains(Protocol1_13To1_12_2.class)) {
                if (info.getChannel().isOpen()) {
                    info.get(TabCompleteTracker.class).sendPacketToServer();
                }
            }
        }
    }
}
