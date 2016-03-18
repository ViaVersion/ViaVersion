package us.myles.ViaVersion.protocols.protocol1_9to1_8;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.MovementTracker;

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
