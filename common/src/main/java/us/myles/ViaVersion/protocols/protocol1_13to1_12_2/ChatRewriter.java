package us.myles.ViaVersion.protocols.protocol1_13to1_12_2;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.rewriters.ComponentRewriter;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.ComponentRewriter1_13;

public class ChatRewriter {
    private static final ComponentRewriter COMPONENT_REWRITER = new ComponentRewriter1_13();

    public static String legacyTextToJsonString(String message, boolean itemData) {
        Component component = Component.text(builder -> {
            if (itemData) {
                builder.decoration(TextDecoration.ITALIC, false);
            }
            builder.append(LegacyComponentSerializer.legacySection().deserialize(message));
        });
        return GsonComponentSerializer.gson().serialize(component);
    }

    public static String legacyTextToJsonString(String legacyText) {
        return legacyTextToJsonString(legacyText, false);
    }

    public static JsonElement legacyTextToJson(String legacyText) {
        return JsonParser.parseString(legacyTextToJsonString(legacyText, false));
    }

    public static String jsonToLegacyText(String value) {
        try {
            Component component = GsonComponentSerializer.gson().deserialize(value);
            return LegacyComponentSerializer.legacySection().serialize(component);
        } catch (Exception e) {
            Via.getPlatform().getLogger().warning("Error converting json text to legacy: " + value);
            e.printStackTrace();
            return "";
        }
    }

    public static void processTranslate(JsonElement value) {
        COMPONENT_REWRITER.processText(value);
    }
}
