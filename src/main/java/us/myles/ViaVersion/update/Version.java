package us.myles.ViaVersion.update;

public class Version implements Comparable<Version>
{
    private int[] parts;

    public Version(String value)
    {
        if(value == null)
            throw new IllegalArgumentException("Version can not be null");

        if(value.matches("^[0-9]+(\\.[0-9]+)*$") == false)
            throw new IllegalArgumentException("Invalid version format");

        String[] split = value.split("\\.");
        parts = new int[split.length];

        for (int i = 0; i < split.length; i += 1)
            parts[i] = Integer.parseInt(split[i]);
    }

    @Override
    public String toString()
    {
        String[] split = new String[parts.length];

        for (int i = 0; i < parts.length; i += 1)
            split[i] = String.valueOf(parts[i]);

        return String.join(".", split);
    }

    public static int compare(Version verA, Version verB)
    {
        if (verA == verB) return 0;
        if (verA == null) return -1;
        if (verB == null) return 1;

        int max = Math.max(verA.parts.length, verB.parts.length);

        for (int i = 0; i < max; i += 1)
        {
            int partA = i < verA.parts.length ? verA.parts[i] : 0;
            int partB = i < verB.parts.length ? verB.parts[i] : 0;
            if (partA < partB) return -1;
            if (partA > partB) return 1;
        }

        return 0;
    }

    @Override
    public int compareTo(Version that)
    {
        return compare(this, that);
    }

    public static boolean equals(Version verA, Version verB)
    {
        if (verA == verB) return true;
        if (verA == null) return false;
        if (verB == null) return false;
        return compare(verA, verB) == 0;
    }

    @Override
    public boolean equals(Object that)
    {
        return that instanceof Version && equals(this, (Version)that);
    }
}