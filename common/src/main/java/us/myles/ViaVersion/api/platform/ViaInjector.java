package us.myles.ViaVersion.api.platform;

public interface ViaInjector {
    /**
     * Inject into the current Platform
     *
     * @throws Exception If there is an error with injecting
     */
    void inject() throws Exception;

    /**
     * Uninject into the current Platform
     *
     * @throws Exception If there is an error with uninjecting
     */
    void uninject() throws Exception;

    /**
     * Get the server protocol version
     *
     * @return The server protocol integer
     * @throws Exception If there is an error with getting this info, eg. not binded.
     */
    int getServerProtocolVersion() throws Exception;

    /**
     * Get the name of the encoder for then netty pipeline for this platform.
     *
     * @return The name
     */
    String getEncoderName();
}
