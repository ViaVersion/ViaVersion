package us.myles.ViaVersion.protocols.protocol18w43bto1_13_2;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.BlockStorage;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.EntityTracker;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.TabCompleteTracker;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

// Development of 1.14 support!
public class Protocol18w43bTo1_13_2 extends Protocol {

    @Override
    protected void registerPackets() {
        // new packet 0x4E AND 0x57
        registerOutgoing(State.PLAY, 0x4E, 0X4F);
        registerOutgoing(State.PLAY, 0x4F, 0X50);
        registerOutgoing(State.PLAY, 0x50, 0X51);
        registerOutgoing(State.PLAY, 0x51, 0X52);
        registerOutgoing(State.PLAY, 0x52, 0X53);
        registerOutgoing(State.PLAY, 0x53, 0X54);
        registerOutgoing(State.PLAY, 0x54, 0X55);
        registerOutgoing(State.PLAY, 0x55, 0X56);
    }

    @Override
    public void init(UserConnection userConnection) {
        userConnection.put(new EntityTracker(userConnection));
        userConnection.put(new TabCompleteTracker(userConnection));
        if (!userConnection.has(ClientWorld.class))
            userConnection.put(new ClientWorld(userConnection));
        userConnection.put(new BlockStorage(userConnection));
    }
}
