package me.PauMAVA.TTR.util;

import org.bukkit.ChatColor;

public class TextUtil {

    private static final String NORMAL_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZáéíóúñÁÉÍÓÚÑ0123456789";
    private static final String TINY_CHARS   = "ᴀʙᴄᴅᴇғɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢᴀʙᴄᴅᴇғɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢᴀᴇɪᴏᴜɴᴀᴇɪᴏᴜɴ₀₁₂₃₄₅₆₇₈₉";

    /**
     * Convierte texto estándar a fuente estética Tiny / Small Caps,
     * preservando intactos los códigos de color de Minecraft (§a o &a).
     */
    public static String toTiny(String text) {
        if (text == null || text.isEmpty()) return text;
        StringBuilder sb = new StringBuilder();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);

            // Preservar códigos de color de Minecraft: §x o &x
            if ((c == '§' || c == '&') && i + 1 < length) {
                sb.append(c);
                sb.append(text.charAt(++i));
                continue;
            }

            int index = NORMAL_CHARS.indexOf(c);
            if (index != -1) {
                sb.append(TINY_CHARS.charAt(index));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String color(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String colorTiny(String text) {
        if (text == null) return "";
        return toTiny(color(text));
    }
}
