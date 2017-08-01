package us.myles.ViaVersion.protocols.protocol1_12_1to1_12;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.packets.State;

public class Protocol1_12_1TO1_12 extends Protocol {
    @Override
    protected void registerPackets() {
        registerOutgoing(State.PLAY, -1, 0x2B); // TODO new packet?
        registerOutgoing(State.PLAY, 0x2b, 0x2c);
        registerOutgoing(State.PLAY, 0x2c, 0x2d);
        registerOutgoing(State.PLAY, 0x2d, 0x2e);
        registerOutgoing(State.PLAY, 0x2e, 0x2f);
        registerOutgoing(State.PLAY, 0x2f, 0x30);
        registerOutgoing(State.PLAY, 0x30, 0x31);
        registerOutgoing(State.PLAY, 0x31, 0x32);
        registerOutgoing(State.PLAY, 0x32, 0x33);
        registerOutgoing(State.PLAY, 0x33, 0x34);
        registerOutgoing(State.PLAY, 0x34, 0x35);
        registerOutgoing(State.PLAY, 0x35, 0x36);
        registerOutgoing(State.PLAY, 0x36, 0x37);
        registerOutgoing(State.PLAY, 0x37, 0x38);
        registerOutgoing(State.PLAY, 0x38, 0x39);
        registerOutgoing(State.PLAY, 0x39, 0x3a);
        registerOutgoing(State.PLAY, 0x3a, 0x3b);
        registerOutgoing(State.PLAY, 0x3b, 0x3c);
        registerOutgoing(State.PLAY, 0x3c, 0x3d);
        registerOutgoing(State.PLAY, 0x3d, 0x3e);
        registerOutgoing(State.PLAY, 0x3e, 0x3f);
        registerOutgoing(State.PLAY, 0x3f, 0x40);
        registerOutgoing(State.PLAY, 0x40, 0x41);
        registerOutgoing(State.PLAY, 0x41, 0x42);
        registerOutgoing(State.PLAY, 0x42, 0x43);
        registerOutgoing(State.PLAY, 0x43, 0x44);
        registerOutgoing(State.PLAY, 0x44, 0x45);
        registerOutgoing(State.PLAY, 0x45, 0x46);
        registerOutgoing(State.PLAY, 0x46, 0x47);
        registerOutgoing(State.PLAY, 0x47, 0x48);
        registerOutgoing(State.PLAY, 0x48, 0x49);
        registerOutgoing(State.PLAY, 0x49, 0x4a);
        registerOutgoing(State.PLAY, 0x4a, 0x4b);
        registerOutgoing(State.PLAY, 0x4b, 0x4c);
        registerOutgoing(State.PLAY, 0x4c, 0x4d);
        registerOutgoing(State.PLAY, 0x4d, 0x4e);
        registerOutgoing(State.PLAY, 0x4e, 0x4f);


    }

    @Override
    public void init(UserConnection userConnection) {

    }
}
