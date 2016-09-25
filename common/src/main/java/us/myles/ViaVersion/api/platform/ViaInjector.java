package us.myles.ViaVersion.api.platform;

public interface ViaInjector {
    public void inject();

    public void uninject();

    public int getServerProtocolVersion();
}
