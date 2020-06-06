package us.myles.ViaVersion.api.protocol;

/**
 * Interface to be implemented by server outgoing packet type enums,
 * representing PLAY state packets, ordered by their packet id.
 */
public interface ClientboundPacketType {

    /**
     * @return name of the packet, to be consistent over multiple versions
     */
    String name();

    /**
     * @return ordinal, being the packet id for the implemented protocol
     */
    int ordinal();
}
