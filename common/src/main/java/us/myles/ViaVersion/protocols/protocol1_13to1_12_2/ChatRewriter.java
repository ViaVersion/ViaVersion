package us.myles.ViaVersion.protocols.protocol1_13to1_12_2;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ShortTag;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.minecraft.nbt.BinaryTagIO;
import us.myles.ViaVersion.api.rewriters.ComponentRewriter;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.MappingData;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.packets.InventoryPackets;
import us.myles.ViaVersion.util.GsonUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatRewriter {
    private static final Pattern URL = Pattern.compile("^(?:(https?)://)?([-\\w_.]{2,}\\.[a-z]{2,4})(/\\S*)?$");
    private static final BaseComponent[] EMPTY_COMPONENTS = new BaseComponent[0];
    private static final ComponentRewriter COMPONENT_REWRITER = new ComponentRewriter() {
        @Override
        protected void handleHoverEvent(JsonObject hoverEvent) {
            super.handleHoverEvent(hoverEvent);
            String action = hoverEvent.getAsJsonPrimitive("action").getAsString();
            if (!action.equals("show_item")) return;

            JsonElement value = hoverEvent.get("value");
            if (value == null) return;

            String text = findItemNBT(value);
            if (text == null) return;
            try {
                CompoundTag tag = BinaryTagIO.readString(text);
                CompoundTag itemTag = tag.get("tag");
                ShortTag damageTag = tag.get("Damage");

                // Call item converter
                short damage = damageTag != null ? damageTag.getValue() : 0;
                Item item = new Item();
                item.setData(damage);
                item.setTag(itemTag);
                InventoryPackets.toClient(item);

                // Serialize again
                if (damage != item.getData()) {
                    tag.put(new ShortTag("Damage", item.getData()));
                }
                if (itemTag != null) {
                    tag.put(itemTag);
                }

                JsonArray array = new JsonArray();
                JsonObject object = new JsonObject();
                array.add(object);
                String serializedNBT = BinaryTagIO.writeString(tag);
                object.addProperty("text", serializedNBT);
                hoverEvent.add("value", array);
            } catch (IOException e) {
                Via.getPlatform().getLogger().warning("Invalid NBT in show_item:");
                e.printStackTrace();
            }
        }

        private String findItemNBT(JsonElement element) {
            if (element.isJsonArray()) {
                for (JsonElement jsonElement : element.getAsJsonArray()) {
                    String value = findItemNBT(jsonElement);
                    if (value != null) {
                        return value;
                    }
                }
            } else if (element.isJsonObject()) {
                JsonPrimitive text = element.getAsJsonObject().getAsJsonPrimitive("text");
                if (text != null) {
                    return text.getAsString();
                }
            } else if (element.isJsonPrimitive()) {
                return element.getAsJsonPrimitive().getAsString();
            }
            return null;
        }

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

        final String serializedComponents = ComponentSerializer.toString(components.toArray(EMPTY_COMPONENTS));
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
