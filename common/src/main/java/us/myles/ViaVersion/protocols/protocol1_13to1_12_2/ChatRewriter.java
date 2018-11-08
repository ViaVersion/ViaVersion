package us.myles.ViaVersion.protocols.protocol1_13to1_12_2;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.MappingData;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatRewriter {
    // Based on https://github.com/SpigotMC/BungeeCord/blob/master/chat/src/main/java/net/md_5/bungee/api/chat/TextComponent.java

    private static final Pattern url = Pattern.compile("^(?:(https?)://)?([-\\w_\\.]{2,}\\.[a-z]{2,4})(/\\S*)?$");

    public static BaseComponent[] fromLegacyText(String message, ChatColor defaultColor) {
        ArrayList<BaseComponent> components = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        TextComponent component = new TextComponent();
        Matcher matcher = url.matcher(message);

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
                switch (format) {
                    case BOLD:
                        component.setBold(true);
                        break;
                    case ITALIC:
                        component.setItalic(true);
                        break;
                    case UNDERLINE:
                        component.setUnderlined(true);
                        break;
                    case STRIKETHROUGH:
                        component.setStrikethrough(true);
                        break;
                    case MAGIC:
                        component.setObfuscated(true);
                        break;
                    case RESET:
                        format = defaultColor;
                    default:
                        component = new TextComponent();
                        component.setColor(format);
                        // ViaVersion start
                        component.setBold(false);
                        component.setItalic(false);
                        component.setUnderlined(false);
                        component.setStrikethrough(false);
                        component.setObfuscated(false);
                        // ViaVersion end
                        break;
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

        return components.toArray(new BaseComponent[0]);
    }

    public static String legacyTextToJson(String legacyText) {
        return ComponentSerializer.toString(fromLegacyText(legacyText, ChatColor.WHITE));
    }

    public static String jsonTextToLegacy(String value) {
        return TextComponent.toLegacyText(ComponentSerializer.parse(value));
    }

    public static String processTranslate(String value) {
        BaseComponent[] components = ComponentSerializer.parse(value);
        for (BaseComponent component : components) {
            processTranslate(component);
        }
        if (components.length == 1) {
            return ComponentSerializer.toString(components[0]);
        } else {
            return ComponentSerializer.toString(components);
        }
    }

    private static void processTranslate(BaseComponent component) {
        if (component instanceof TranslatableComponent) {
            String oldTranslate = ((TranslatableComponent) component).getTranslate();
            String newTranslate;
            newTranslate = MappingData.translateMapping.get(oldTranslate);
            if (newTranslate == null) MappingData.mojangTranslation.get(oldTranslate);
            if (newTranslate != null) {
                ((TranslatableComponent) component).setTranslate(newTranslate);
            }
            if (((TranslatableComponent) component).getWith() != null) {
                for (BaseComponent baseComponent : ((TranslatableComponent) component).getWith()) {
                    processTranslate(baseComponent);
                }
            }
        }
        if (component.getHoverEvent() != null) {
            for (BaseComponent baseComponent : component.getHoverEvent().getValue()) {
                processTranslate(baseComponent);
            }
        }
        if (component.getExtra() != null) {
            for (BaseComponent baseComponent : component.getExtra()) {
                processTranslate(baseComponent);
            }
        }
    }
}
