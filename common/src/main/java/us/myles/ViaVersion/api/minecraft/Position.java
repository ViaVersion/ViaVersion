package us.myles.ViaVersion.api.minecraft;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class Position {
    private int x;
    private short y;
    private int z;

    public Position(Position toCopy) {
        this(toCopy.getX(), toCopy.getY(), toCopy.getZ());
    }

    public Position getRelative(BlockFace face) {
        return new Position(x + face.getModX(), (short) (y + face.getModY()), z + face.getModZ());
    }

    public Position shift(BlockFace face) {
        this.x += face.getModX();
        this.y += face.getModY();
        this.z += face.getModZ();
        return this;
    }
}
