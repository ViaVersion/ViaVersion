package us.myles.ViaVersion.chunks;

import lombok.RequiredArgsConstructor;

import java.util.BitSet;

@RequiredArgsConstructor
public class MagicBitSet extends BitSet {
    private final int initLength;

    public int getTrueLength() {
        return length() == 0 ? initLength : length();
    }
}
