package us.myles.ViaVersion.update;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version implements Comparable<Version> {
    private static Pattern semVer = Pattern.compile("(?<a>0|[1-9]\\d*)\\.(?<b>0|[1-9]\\d*)(?:\\.(?<c>0|[1-9]\\d*))?(?:-(?<tag>[A-z0-9.-]*))?");
    private int[] parts = new int[3];
    private String tag;

    public Version(String value) {
        if (value == null)
            throw new IllegalArgumentException("Version can not be null");

        Matcher matcher = semVer.matcher(value);
        if (!matcher.matches())
            throw new IllegalArgumentException("Invalid version format");
        parts[0] = Integer.parseInt(matcher.group("a"));
        parts[1] = Integer.parseInt(matcher.group("b"));
        parts[2] = matcher.group("c") == null ? 0 : Integer.parseInt(matcher.group("c"));

        tag = matcher.group("tag") == null ? "" : matcher.group("tag");
    }

    public static int compare(Version verA, Version verB) {
        if (verA == verB) return 0;
        if (verA == null) return -1;
        if (verB == null) return 1;

        int max = Math.max(verA.parts.length, verB.parts.length);

        for (int i = 0; i < max; i += 1) {
            int partA = i < verA.parts.length ? verA.parts[i] : 0;
            int partB = i < verB.parts.length ? verB.parts[i] : 0;
            if (partA < partB) return -1;
            if (partA > partB) return 1;
        }

        // Simple tag check
        if (verA.tag.length() == 0 && verB.tag.length() > 0)
            return 1;
        if (verA.tag.length() > 0 && verB.tag.length() == 0)
            return -1;

        return 0;
    }

    public static boolean equals(Version verA, Version verB) {
        return verA == verB || verA != null && verB != null && compare(verA, verB) == 0;
    }

    @Override
    public String toString() {
        String[] split = new String[parts.length];

        for (int i = 0; i < parts.length; i += 1)
            split[i] = String.valueOf(parts[i]);

        return StringUtils.join(split, ".") + (tag.length() != 0 ? "-" + tag : "");
    }

    @Override
    public int compareTo(Version that) {
        return compare(this, that);
    }

    @Override
    public boolean equals(Object that) {
        return that instanceof Version && equals(this, (Version) that);
    }

    public String getTag() {
        return tag;
    }
}