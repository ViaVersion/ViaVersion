package us.myles.ViaVersion.api.protocol;

/**
 * Interface representing PLAY state packets, ordered by their packet id.
 *
 * @see ClientboundPacketType
 * @see ServerboundPacketType
 */
public interface PacketType {

    /**
     * @return name of the packet, to be consistent over multiple versions
     */
    String name();

    /**
     * @return ordinal, being the packet id for the implemented protocol
     */
    int ordinal();
}
