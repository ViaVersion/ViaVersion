package us.myles.ViaVersion.protocols.base;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.type.Type;

import java.util.UUID;

public class BaseProtocol1_16 extends BaseProtocol1_7 {

    @Override
    protected UUID passthroughLoginUUID(final PacketWrapper wrapper) throws Exception {
        return wrapper.passthrough(Type.UUID_INT_ARRAY);
    }
}
