package us.myles.ViaVersion.protocols.protocol1_14to1_13_2;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class InventoryNameRewriter {
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
        if (component == null) return;
        if (component instanceof TranslatableComponent) {
            String oldTranslate = ((TranslatableComponent) component).getTranslate();

            // Mojang decided to remove .name from inventory titles
            if (oldTranslate.startsWith("block.") && oldTranslate.endsWith(".name")) {
                ((TranslatableComponent) component).setTranslate(oldTranslate.substring(0, oldTranslate.length() - 5));
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
