package us.myles.ViaVersion.api.protocol;

import com.google.common.base.Preconditions;

public class VersionRange {
    private final String baseVersion;
    private final int rangeFrom;
    private final int rangeTo;

    /**
     * Creates a new version range. Giving "1.7", 0, and 5 for example would represent the range from 1.7-1.7.5.
     *
     * @param baseVersion base version
     * @param rangeFrom   minor version the range begins at, must be greater than or equal to 0
     * @param rangeTo     minor version the range ends at, must be greater than 0 and {@link #rangeFrom}
     */
    public VersionRange(String baseVersion, int rangeFrom, int rangeTo) {
        Preconditions.checkNotNull(baseVersion);
        Preconditions.checkArgument(rangeFrom >= 0);
        Preconditions.checkArgument(rangeTo > rangeFrom && rangeTo > 0);
        this.baseVersion = baseVersion;
        this.rangeFrom = rangeFrom;
        this.rangeTo = rangeTo;
    }

    public String getBaseVersion() {
        return baseVersion;
    }

    public int getRangeFrom() {
        return rangeFrom;
    }

    public int getRangeTo() {
        return rangeTo;
    }
}
