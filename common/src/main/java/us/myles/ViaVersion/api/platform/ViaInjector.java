package us.myles.ViaVersion.api.platform;

public interface ViaInjector {
    public void inject() throws Exception;

    public void uninject() throws Exception;

    public int getServerProtocolVersion() throws Exception;
}
