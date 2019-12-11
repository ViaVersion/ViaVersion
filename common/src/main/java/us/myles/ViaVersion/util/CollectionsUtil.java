package us.myles.ViaVersion.util;

import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.experimental.UtilityClass;

import java.util.Set;

@UtilityClass
public class CollectionsUtil {
    public static boolean containsInt(Set<Integer> integerSet, int i) {
        if (integerSet instanceof IntSet) {
            return ((IntSet) integerSet).contains(i);
        } else {
            return integerSet.contains(i);
        }
    }
}
