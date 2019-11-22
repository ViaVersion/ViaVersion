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
    private int posX;
    private short posY;
    private int posZ;

    @Deprecated
    public Position(Long x, Long y, Long z) {
        this.posX = x.intValue();
        this.posY = y.shortValue();
        this.posZ = z.intValue();
    }

    public Position(Position toCopy) {
        this(toCopy.getPosX(), toCopy.getPosY(), toCopy.getPosZ());
    }

    @Deprecated
    public void setX(Long x) {
        this.posX = x.intValue();
    }

    @Deprecated
    public void setY(Long y) {
        this.posY = y.shortValue();
    }

    @Deprecated
    public void setZ(Long z) {
        this.posZ = z.intValue();
    }

    @Deprecated
    public Long getX() {
        return (long) this.posX;
    }

    @Deprecated
    public Long getY() {
        return (long) this.posY;
    }

    @Deprecated
    public Long getZ() {
        return (long) this.posZ;
    }

    public Position getRelative(BlockFace face) {
        return new Position(posX + face.getModX(), (short) (posY + face.getModY()), posZ + face.getModZ());
    }

    public Position shift(BlockFace face) {
        this.posX += face.getModX();
        this.posY += face.getModY();
        this.posZ += face.getModZ();
        return this;
    }
}
