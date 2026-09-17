package me.PauMAVA.TTR.ui;

import me.PauMAVA.TTR.util.TextUtil;

public class DestinyTheme {

    // Destiny: Blanco (#FFFFFF), Towers: Rojo (#FF2E2E / #E63946)
    public static final String BRAND_STATIC = TextUtil.color("&#FFFFFF§lDESTINY &#FF2E2E§lTOWERS");
    public static final String BRAND_TINY_STATIC = TextUtil.color("&#FFFFFF§lᴅᴇsᴛɪɴʏ &#FF2E2E§lᴛᴏᴡᴇʀs");

    // Rol [Destiny] en blanco y grises (#FFFFFF, #CCCCCC, #888888)
    public static final String DESTINY_ROLE_BADGE = TextUtil.color("&#888888[&#FFFFFF" + TextUtil.toTiny("Destiny") + "&#888888]");

    // Animación fluida con brillo móvil para Scoreboard y Tablist (longitud fija estricta)
    public static final String[] SHINE_FRAMES = {
        TextUtil.color("&#888888◆ &#FFFFFF§lᴅᴇsᴛɪɴʏ &#FF2E2E§lᴛᴏᴡᴇʀs &#888888◆"),
        TextUtil.color("&#CCCCCC◆ &#FFFFFF§lᴅ&#D4D4D4§lsᴛɪɴʏ &#FF2E2E§lᴛᴏᴡᴇʀs &#888888◆"),
        TextUtil.color("&#888888◆ &#D4D4D4§lᴅ&#FFFFFF§lᴇ&#D4D4D4§lsᴛɪɴʏ &#FF2E2E§lᴛᴏᴡᴇʀs &#888888◆"),
        TextUtil.color("&#888888◆ &#D4D4D4§lᴅᴇ&#FFFFFF§ls&#D4D4D4§lᴛɪɴʏ &#FF2E2E§lᴛᴏᴡᴇʀs &#888888◆"),
        TextUtil.color("&#888888◆ &#D4D4D4§lᴅᴇs&#FFFFFF§lᴛ&#D4D4D4§lɪɴʏ &#FF2E2E§lᴛᴏᴡᴇʀs &#888888◆"),
        TextUtil.color("&#888888◆ &#D4D4D4§lᴅᴇsᴛ&#FFFFFF§lɪ&#D4D4D4§lɴʏ &#FF2E2E§lᴛᴏᴡᴇʀs &#888888◆"),
        TextUtil.color("&#888888◆ &#D4D4D4§lᴅᴇsᴛɪ&#FFFFFF§lɴ&#D4D4D4§lʏ &#FF2E2E§lᴛᴏᴡᴇʀs &#888888◆"),
        TextUtil.color("&#888888◆ &#D4D4D4§lᴅᴇsᴛɪɴ&#FFFFFF§lʏ &#FF2E2E§lᴛᴏᴡᴇʀs &#888888◆"),
        TextUtil.color("&#888888◆ &#FFFFFF§lᴅᴇsᴛɪɴʏ &#FF7575§lᴛ&#FF2E2E§lᴏᴡᴇʀs &#888888◆"),
        TextUtil.color("&#888888◆ &#FFFFFF§lᴅᴇsᴛɪɴʏ &#FF2E2E§lᴛ&#FF7575§lᴏ&#FF2E2E§lᴡᴇʀs &#888888◆"),
        TextUtil.color("&#888888◆ &#FFFFFF§lᴅᴇsᴛɪɴʏ &#FF2E2E§lᴛᴏ&#FF7575§lᴡ&#FF2E2E§lᴇʀs &#888888◆"),
        TextUtil.color("&#888888◆ &#FFFFFF§lᴅᴇsᴛɪɴʏ &#FF2E2E§lᴛᴏᴡ&#FF7575§lᴇ&#FF2E2E§lʀs &#888888◆"),
        TextUtil.color("&#888888◆ &#FFFFFF§lᴅᴇsᴛɪɴʏ &#FF2E2E§lᴛᴏᴡᴇ&#FF7575§lʀ&#FF2E2E§ls &#888888◆"),
        TextUtil.color("&#888888◆ &#FFFFFF§lᴅᴇsᴛɪɴʏ &#FF2E2E§lᴛᴏᴡᴇʀ&#FF7575§ls &#888888◆"),
        TextUtil.color("&#FF2E2E◆ &#FFFFFF§lᴅᴇsᴛɪɴʏ &#FF2E2E§lᴛᴏᴡᴇʀs &#FF2E2E◆"),
        TextUtil.color("&#FFFFFF◆ &#FFFFFF§lᴅᴇsᴛɪɴʏ &#FF2E2E§lᴛᴏᴡᴇʀs &#FFFFFF◆")
    };

    // Animación fija de DestinyOwners estrictamente en paleta de blancos y grises (#FFFFFF, #CCCCCC, #888888, #666666)
    // Longitud fija de 13 caracteres (sin símbolos cambiantes) para evitar parpadeo o cambios de tamaño en Scoreboard y Tab
    public static final String[] FOOTER_FRAMES = {
        TextUtil.color("&#666666§lᴅᴇsᴛɪɴʏᴏᴡɴᴇʀs"),
        TextUtil.color("&#FFFFFF§lᴅ&#888888§lᴇsᴛɪɴʏᴏᴡɴᴇʀs"),
        TextUtil.color("&#CCCCCC§lᴅ&#FFFFFF§lᴇ&#888888§lsᴛɪɴʏᴏᴡɴᴇʀs"),
        TextUtil.color("&#AAAAAA§lᴅ&#CCCCCC§lᴇ&#FFFFFF§ls&#888888§lᴛɪɴʏᴏᴡɴᴇʀs"),
        TextUtil.color("&#888888§lᴅ&#AAAAAA§lᴇ&#CCCCCC§ls&#FFFFFF§lᴛ&#888888§lɪɴʏᴏᴡɴᴇʀs"),
        TextUtil.color("&#888888§lᴅᴇ&#AAAAAA§ls&#CCCCCC§lᴛ&#FFFFFF§lɪ&#888888§lɴʏᴏᴡɴᴇʀs"),
        TextUtil.color("&#888888§lᴅᴇs&#AAAAAA§lᴛ&#CCCCCC§lɪ&#FFFFFF§lɴ&#888888§lʏᴏᴡɴᴇʀs"),
        TextUtil.color("&#888888§lᴅᴇsᴛ&#AAAAAA§lɪ&#CCCCCC§lɴ&#FFFFFF§lʏ&#888888§lᴏᴡɴᴇʀs"),
        TextUtil.color("&#888888§lᴅᴇsᴛɪ&#AAAAAA§lɴ&#CCCCCC§lʏ&#FFFFFF§lᴏ&#888888§lᴡɴᴇʀs"),
        TextUtil.color("&#888888§lᴅᴇsᴛɪɴ&#AAAAAA§lʏ&#CCCCCC§lᴏ&#FFFFFF§lᴡ&#888888§lɴᴇʀs"),
        TextUtil.color("&#888888§lᴅᴇsᴛɪɴʏ&#AAAAAA§lᴏ&#CCCCCC§lᴡ&#FFFFFF§lɴ&#888888§lᴇʀs"),
        TextUtil.color("&#888888§lᴅᴇsᴛɪɴʏᴏ&#AAAAAA§lᴡ&#CCCCCC§lɴ&#FFFFFF§lᴇ&#888888§lʀs"),
        TextUtil.color("&#888888§lᴅᴇsᴛɪɴʏᴏᴡ&#AAAAAA§lɴ&#CCCCCC§lᴇ&#FFFFFF§lʀ&#888888§ls"),
        TextUtil.color("&#888888§lᴅᴇsᴛɪɴʏᴏᴡɴ&#AAAAAA§lᴇ&#CCCCCC§lʀ&#FFFFFF§ls"),
        TextUtil.color("&#CCCCCC§lᴅᴇsᴛɪɴʏᴏᴡɴᴇʀs"),
        TextUtil.color("&#FFFFFF§lᴅᴇsᴛɪɴʏᴏᴡɴᴇʀs"),
        TextUtil.color("&#E0E0E0§lᴅᴇsᴛɪɴʏᴏᴡɴᴇʀs"),
        TextUtil.color("&#AAAAAA§lᴅᴇsᴛɪɴʏᴏᴡɴᴇʀs"),
        TextUtil.color("&#888888§lᴅᴇsᴛɪɴʏᴏᴡɴᴇʀs")
    };
}
