package us.myles.ViaVersion.protocols.protocol1_13to1_12_2;

import com.google.gson.JsonElement;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.rewriters.ComponentRewriter;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.ComponentRewriter1_13;
import us.myles.ViaVersion.util.GsonUtil;

public class ChatRewriter {
    private static final ComponentRewriter COMPONENT_REWRITER = new ComponentRewriter1_13();

    // Based on https://github.com/SpigotMC/BungeeCord/blob/master/chat/src/main/java/net/md_5/bungee/api/chat/TextComponent.java
    public static String fromLegacyTextAsString(String message, ChatColor defaultColor, boolean itemData) {
        TextComponent headComponent = new TextComponent();
        TextComponent component = new TextComponent();
        StringBuilder builder = new StringBuilder();
        if (itemData) {
            // Workaround for all italic lore
            headComponent.setItalic(false);
            //TODO set first child to italics if it doesn't have a color
        }

        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            if (c == ChatColor.COLOR_CHAR) {
                if (++i >= message.length()) {
                    break;
                }
                c = message.charAt(i);
                if (c >= 'A' && c <= 'Z') {
                    c += 32;
                }
                ChatColor format = ChatColor.getByChar(c);
                if (format == null) {
                    continue;
                }
                if (builder.length() > 0) {
                    TextComponent old = component;
                    component = new TextComponent(old);
                    old.setText(builder.toString());
                    builder = new StringBuilder();
                    headComponent.addExtra(old);
                }
                if (ChatColor.BOLD.equals(format)) {
                    component.setBold(true);
                } else if (ChatColor.ITALIC.equals(format)) {
                    component.setItalic(true);
                } else if (ChatColor.UNDERLINE.equals(format)) {
                    component.setUnderlined(true);
                } else if (ChatColor.STRIKETHROUGH.equals(format)) {
                    component.setStrikethrough(true);
                } else if (ChatColor.MAGIC.equals(format)) {
                    component.setObfuscated(true);
                } else if (ChatColor.RESET.equals(format)) {
                    format = defaultColor;

                    component = new TextComponent();
                    component.setColor(format);
                } else {
                    component = new TextComponent();
                    component.setColor(format);
                }
                continue;
            }
            builder.append(c);
        }

        component.setText(builder.toString());
        headComponent.addExtra(component);

        return ComponentSerializer.toString(headComponent);
    }

    public static JsonElement fromLegacyText(String message, ChatColor defaultColor) {
        return GsonUtil.getJsonParser().parse(fromLegacyTextAsString(message, defaultColor, false));
    }

    public static JsonElement legacyTextToJson(String legacyText) {
        return fromLegacyText(legacyText, ChatColor.WHITE);
    }

    public static String legacyTextToJsonString(String legacyText) {
        return fromLegacyTextAsString(legacyText, ChatColor.WHITE, false);
    }

    public static String jsonTextToLegacy(String value) {
        try {
            return TextComponent.toLegacyText(ComponentSerializer.parse(value));
        } catch (Exception e) {
            Via.getPlatform().getLogger().warning("Error converting json text to legacy: " + value);
            return "";
        }
    }

    public static void processTranslate(JsonElement value) {
        COMPONENT_REWRITER.processText(value);
    }
}
