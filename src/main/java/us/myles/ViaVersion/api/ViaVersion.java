package us.myles.ViaVersion.api;

public class ViaVersion {

    private static ViaVersionAPI INSTANCE;

    public static void setInstance(ViaVersionAPI api) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Instance already set.");
        }
        INSTANCE = api;
    }

    public static ViaVersionAPI getInstance() {
        return INSTANCE;
    }
}
