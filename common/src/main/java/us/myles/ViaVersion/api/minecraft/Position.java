package us.myles.ViaVersion.api.minecraft;

public class Position {
    private final int x;
    private final short y;
    private final int z;

    public Position(int x, short y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Position(Position toCopy) {
        this(toCopy.getX(), toCopy.getY(), toCopy.getZ());
    }

    public Position getRelative(BlockFace face) {
        return new Position(x + face.getModX(), (short) (y + face.getModY()), z + face.getModZ());
    }

    public int getX() {
        return x;
    }

    public short getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        if (x != position.x) return false;
        if (y != position.y) return false;
        return z == position.z;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + (int) y;
        result = 31 * result + z;
        return result;
    }

    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
