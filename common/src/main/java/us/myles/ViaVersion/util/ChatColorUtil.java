package us.myles.ViaVersion.util;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.util.regex.Pattern;

// Based on https://github.com/SpigotMC/BungeeCord/blob/master/chat/src/main/java/net/md_5/bungee/api/ChatColor.java
public class ChatColorUtil {

    public static final String ALL_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";
    public static final char COLOR_CHAR = '§';
    public static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + COLOR_CHAR + "[0-9A-FK-ORX]");
    private static final Int2IntMap COLOR_ORDINALS = new Int2IntOpenHashMap();
    private static int ordinalCounter;

    static {
        addColorOrindal('0', '9');
        addColorOrindal('a', 'f');
        addColorOrindal('k', 'o');
        addColorOrindal('r');
    }

    public static boolean isColorCode(char c) {
        return COLOR_ORDINALS.containsKey(c);
    }

    public static int getColorOrdinal(char c) {
        return COLOR_ORDINALS.getOrDefault(c, -1);
    }

    public static String translateAlternateColorCodes(String s) {
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] == '&' && ALL_CODES.indexOf(chars[i + 1]) > -1) {
                chars[i] = COLOR_CHAR;
                chars[i + 1] = Character.toLowerCase(chars[i + 1]);
            }
        }
        return new String(chars);
    }

    public static String stripColor(final String input) {
        return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }

    private static void addColorOrindal(int from, int to) {
        for (int c = from; c <= to; c++) {
            addColorOrindal(c);
        }
    }

    private static void addColorOrindal(int colorChar) {
        COLOR_ORDINALS.put(colorChar, ordinalCounter++);
    }
}
