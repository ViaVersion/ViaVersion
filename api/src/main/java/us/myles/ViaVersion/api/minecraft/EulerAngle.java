package us.myles.ViaVersion.api.minecraft;

public class EulerAngle {
    private final float x;
    private final float y;
    private final float z;

    public EulerAngle(final float x, final float y, final float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }
}
