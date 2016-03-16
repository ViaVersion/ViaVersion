package us.myles.ViaVersion2.api.protocol1_9to1_8;

import us.myles.ViaVersion2.api.PacketWrapper;
import us.myles.ViaVersion2.api.protocol1_9to1_8.storage.MovementTracker;
import us.myles.ViaVersion2.api.remapper.PacketHandler;
import us.myles.ViaVersion2.api.remapper.PacketRemapper;

public class PlayerMovementMapper extends PacketRemapper {
    @Override
    public void registerMap() {
        handler(new PacketHandler() {
            @Override
            public void handle(PacketWrapper wrapper) throws Exception {
                wrapper.user().get(MovementTracker.class).incrementIdlePacket();
            }
        });
    }
}
