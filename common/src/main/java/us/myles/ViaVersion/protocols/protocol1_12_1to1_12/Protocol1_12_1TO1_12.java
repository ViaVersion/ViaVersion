package us.myles.ViaVersion.protocols.protocol1_12_1to1_12;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
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

        // TODO Where did the Prepare Crafting Grid packet go to?
        registerIncoming(State.PLAY, 0x01, -1);

        registerIncoming(State.PLAY, 0x02, 0x01);
        registerIncoming(State.PLAY, 0x03, 0x02);
        registerIncoming(State.PLAY, 0x04, 0x03);
        registerIncoming(State.PLAY, 0x05, 0x04);
        registerIncoming(State.PLAY, 0x06, 0x05);
        registerIncoming(State.PLAY, 0x07, 0x06);
        registerIncoming(State.PLAY, 0x08, 0x07);
        registerIncoming(State.PLAY, 0x09, 0x08);
        registerIncoming(State.PLAY, 0x0a, 0x09);
        registerIncoming(State.PLAY, 0x0b, 0x0a);
        registerIncoming(State.PLAY, 0x0c, 0x0b);
        registerIncoming(State.PLAY, 0x0d, 0x0c);
        registerIncoming(State.PLAY, 0x0e, 0x0d);
        registerIncoming(State.PLAY, 0x0f, 0x0e);
        registerIncoming(State.PLAY, 0x10, 0x0f);
        registerIncoming(State.PLAY, 0x11, 0x10);
        registerIncoming(State.PLAY, 0x12, 0x11);

        // TODO hello new packet
        registerIncoming(State.PLAY, -1, 0x12, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.BYTE); // 0 - Unknown
                map(Type.VAR_INT); // 1 - Unknown
                map(Type.BOOLEAN); // 2 - Unknown
            }
        });


    }

    @Override
    public void init(UserConnection userConnection) {

    }
}
