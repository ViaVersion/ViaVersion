package us.myles.ViaVersion.protocols.protocol1_13to1_12_2;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import us.myles.ViaVersion.api.rewriters.ComponentRewriter;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.MappingData;
import us.myles.ViaVersion.util.GsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatRewriter {
    private static final Pattern URL = Pattern.compile("^(?:(https?)://)?([-\\w_.]{2,}\\.[a-z]{2,4})(/\\S*)?$");
    private static final BaseComponent[] A = new BaseComponent[0];
    private static final ComponentRewriter COMPONENT_REWRITER = new ComponentRewriter() {
        @Override
        protected void handleTranslate(JsonObject object, String translate) {
            super.handleTranslate(object, translate);
            String newTranslate;
            newTranslate = MappingData.translateMapping.get(translate);
            if (newTranslate == null) {
                newTranslate = MappingData.mojangTranslation.get(translate);
            }
            if (newTranslate != null) {
                object.addProperty("translate", newTranslate);
            }
        }
    };

    // Based on https://github.com/SpigotMC/BungeeCord/blob/master/chat/src/main/java/net/md_5/bungee/api/chat/TextComponent.java
    public static JsonElement fromLegacyText(String message, ChatColor defaultColor) {
        List<BaseComponent> components = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        TextComponent component = new TextComponent();
        Matcher matcher = URL.matcher(message);

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
                    components.add(old);
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
                    // ViaVersion start
                    component.setBold(false);
                    component.setItalic(false);
                    component.setUnderlined(false);
                    component.setStrikethrough(false);
                    component.setObfuscated(false);
                    // ViaVersion end
                } else {
                    component = new TextComponent();
                    component.setColor(format);
                    // ViaVersion start
                    component.setBold(false);
                    component.setItalic(false);
                    component.setUnderlined(false);
                    component.setStrikethrough(false);
                    component.setObfuscated(false);
                    // ViaVersion end
                }
                continue;
            }
            int pos = message.indexOf(' ', i);
            if (pos == -1) {
                pos = message.length();
            }
            if (matcher.region(i, pos).find()) { //Web link handling

                if (builder.length() > 0) {
                    TextComponent old = component;
                    component = new TextComponent(old);
                    old.setText(builder.toString());
                    builder = new StringBuilder();
                    components.add(old);
                }

                TextComponent old = component;
                component = new TextComponent(old);
                String urlString = message.substring(i, pos);
                component.setText(urlString);
                component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                        urlString.startsWith("http") ? urlString : "http://" + urlString));
                components.add(component);
                i += pos - i - 1;
                component = old;
                continue;
            }
            builder.append(c);
        }

        component.setText(builder.toString());
        components.add(component);

        final String serializedComponents = ComponentSerializer.toString(components.toArray(A));
        return GsonUtil.getJsonParser().parse(serializedComponents);
    }

    public static JsonElement legacyTextToJson(String legacyText) {
        return fromLegacyText(legacyText, ChatColor.WHITE);
    }

    public static String jsonTextToLegacy(String value) {
        return TextComponent.toLegacyText(ComponentSerializer.parse(value));
    }

    public static void processTranslate(JsonElement value) {
        COMPONENT_REWRITER.processText(value);
    }
}
