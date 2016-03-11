package us.myles.ViaVersion2.api.protocol1_9to1_8;

import us.myles.ViaVersion2.api.data.UserConnection;
import us.myles.ViaVersion2.api.protocol.Protocol;
import us.myles.ViaVersion2.api.protocol1_9to1_8.packets.SpawnPackets;
import us.myles.ViaVersion2.api.protocol1_9to1_8.storage.EntityTracker;

public class Protocol1_9TO1_8 extends Protocol {
    @Override
    public void registerPackets() {
        // Example PLAY_SPAWN_OBJECT(State.PLAY, Direction.OUTGOING, 0x0E, 0x00),
        SpawnPackets.register(this);
    }

    @Override
    public void init(UserConnection userConnection) {
        // Entity tracker
        userConnection.add(new EntityTracker());
    }
}
