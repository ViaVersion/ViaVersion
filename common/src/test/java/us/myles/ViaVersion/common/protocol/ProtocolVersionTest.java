package us.myles.ViaVersion.common.protocol;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;

public class ProtocolVersionTest {

    @Test
    void testVersionWildcard() {
        Assertions.assertEquals(ProtocolVersion.v1_8, ProtocolVersion.getClosest("1.8.3"));
        Assertions.assertEquals(ProtocolVersion.v1_8, ProtocolVersion.getClosest("1.8"));
        Assertions.assertEquals(ProtocolVersion.v1_8, ProtocolVersion.getClosest("1.8.x"));
    }

    @Test
    void testVersionRange() {
        Assertions.assertEquals(ProtocolVersion.v1_7_1, ProtocolVersion.getClosest("1.7"));
        Assertions.assertEquals(ProtocolVersion.v1_7_1, ProtocolVersion.getClosest("1.7.0"));
        Assertions.assertEquals(ProtocolVersion.v1_7_1, ProtocolVersion.getClosest("1.7.1"));
        Assertions.assertEquals(ProtocolVersion.v1_7_1, ProtocolVersion.getClosest("1.7.5"));
    }

    @Test
    void testGet() {
        Assertions.assertEquals(ProtocolVersion.v1_16_3, ProtocolVersion.getProtocol(753));
    }
}
