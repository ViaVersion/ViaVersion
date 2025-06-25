/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.util;

import net.lenni0451.mcstructs.text.Style;
import net.lenni0451.mcstructs.text.TextComponent;
import net.lenni0451.mcstructs.text.TextFormatting;

public class LegacyToComponent {

    /**
     * Converts a legacy formatted string to a Modern Component JSON.
     * @param text The legacy formatted string to convert
     * @return A TextComponent representing the converted message
     */
    public static TextComponent convertToComponent(String text) {
        StringBuilder currentText = new StringBuilder();
        Style currentStyle = newStyle();

        final TextComponent parent = TextComponent.empty();
        boolean coding = false;
        for (char f : text.toCharArray()) {
            if (f == TextFormatting.COLOR_CHAR) {
                coding = true;
                continue;
            } else if (coding) {
                appendSibling(parent, currentText, currentStyle);
                currentStyle = cloneStyle(currentStyle);

                TextFormatting formatting = TextFormatting.getByCode(f);
                if (formatting == TextFormatting.RESET) {
                    formatting = TextFormatting.WHITE;
                }

                if (formatting == TextFormatting.STRIKETHROUGH) {
                    currentStyle.setStrikethrough(true);
                } else if (formatting == TextFormatting.UNDERLINE) {
                    currentStyle.setUnderlined(true);
                } else if (formatting == TextFormatting.BOLD) {
                    currentStyle.setBold(true);
                } else if (formatting == TextFormatting.ITALIC) {
                    currentStyle.setItalic(true);
                } else if (formatting == TextFormatting.OBFUSCATED) {
                    currentStyle.setObfuscated(true);
                } else if (formatting != null) { // this is a color which means we should reset current style
                    currentStyle = newStyle();
                    currentStyle.setColor(formatting.getRgbValue());
                }

                coding = false;
                continue;
            }

            currentText.append(f);
        }

        appendSibling(parent, currentText, currentStyle);

        return parent;
    }

    /**
     * Creates a new Style object with default settings.
     * @return The new Style object
     */
    private static Style newStyle() {
        final Style style = new Style();
        style.setColor(TextFormatting.WHITE.getRgbValue());
        return style;
    }

    /**
     * Clones a Style object, creating a new instance with the same properties.
     * This is useful to avoid modifying the original style when applying new formatting.
     * @param style The Style object to clone
     * @return A new Style object with the same properties as the original
     */
    private static Style cloneStyle(Style style) {
        final Style newStyle = new Style();
        newStyle.setColor(style.getColor().getRgbValue());
        newStyle.setBold(style.isBold());
        newStyle.setItalic(style.isItalic());
        newStyle.setStrikethrough(style.isStrikethrough());
        newStyle.setUnderlined(style.isUnderlined());
        newStyle.setObfuscated(style.isObfuscated());
        return newStyle;
    }

    /**
     * Appends the current text as a sibling to the parent TextComponent.
     * @param parent The parent TextComponent to append to
     * @param currentText The current text being built
     * @param currentStyle The current style of the text
     */
    private static void appendSibling(TextComponent parent, StringBuilder currentText, Style currentStyle) {
        if (!currentText.isEmpty()) {
            TextComponent component = TextComponent.of(currentText.toString());
            component.setStyle(currentStyle);
            parent.append(component);

            currentText.setLength(0);
        }
    }

}
