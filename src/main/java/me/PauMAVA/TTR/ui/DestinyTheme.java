package me.PauMAVA.TTR.ui;

import me.PauMAVA.TTR.util.TextUtil;

public class DestinyTheme {

    // Destiny: Blanco (#FFFFFF), Towers: Rojo (#FF2E2E / #E63946)
    public static final String BRAND_STATIC = TextUtil.color("&#FFFFFF§lDESTINY &#FF2E2E§lTOWERS");
    public static final String BRAND_TINY_STATIC = TextUtil.color("&#FFFFFF§lᴅᴇsᴛɪɴʏ &#FF2E2E§lᴛᴏᴡᴇʀs");

    // Rol [Destiny] en blanco y grises (#FFFFFF, #CCCCCC, #888888)
    public static final String DESTINY_ROLE_BADGE = TextUtil.color("&#888888[&#FFFFFF" + TextUtil.toTiny("Destiny") + "&#888888]");

    // Animación fluida con brillo móvil para Scoreboard y Tablist
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
        TextUtil.color("&#888888◆ &#FFFFFF§lᴅᴇsᴛɪɴʏ &#FF2E2E§lᴛᴏᴡᴇʀ&#FF7575§ls &#FF2E2E◆"),
        TextUtil.color("&#FF2E2E◆ &#FFFFFF§l◆ &#FFFFFF§lᴅᴇsᴛɪɴʏ &#FF2E2E§lᴛᴏᴡᴇʀs &#FFFFFF§l◆ &#FF2E2E◆"),
        TextUtil.color("&#FFFFFF◆ &#FFFFFF§lᴅᴇsᴛɪɴʏ &#FF2E2E§lᴛᴏᴡᴇʀs &#FFFFFF◆")
    };

    // Animación de DestinyOwners estrictamente en paleta de blancos y grises (#FFFFFF, #CCCCCC, #888888, etc.)
    public static final String[] FOOTER_FRAMES = {
        TextUtil.color("&#666666● &#888888§lᴅᴇsᴛɪɴʏᴏᴡɴᴇʀs &#666666●"),
        TextUtil.color("&#888888● &#AAAAAA§lᴅ&#888888§lᴇsᴛɪɴʏᴏᴡɴᴇʀs &#888888●"),
        TextUtil.color("&#AAAAAA● &#CCCCCC§lᴅᴇ&#AAAAAA§lsᴛɪɴʏᴏᴡɴᴇʀs &#AAAAAA●"),
        TextUtil.color("&#CCCCCC● &#E0E0E0§lᴅᴇs&#CCCCCC§lᴛɪɴʏᴏᴡɴᴇʀs &#CCCCCC●"),
        TextUtil.color("&#EAEAEA● &#FFFFFF§lᴅᴇsᴛ&#E0E0E0§lɪɴʏᴏᴡɴᴇʀs &#EAEAEA●"),
        TextUtil.color("&#FFFFFF● &#E0E0E0§lᴅᴇsᴛɪ&#FFFFFF§lɴ&#E0E0E0§lʏᴏᴡɴᴇʀs &#FFFFFF●"),
        TextUtil.color("&#FFFFFF● &#CCCCCC§lᴅᴇsᴛɪɴ&#FFFFFF§lʏ&#CCCCCC§lᴏᴡɴᴇʀs &#FFFFFF●"),
        TextUtil.color("&#EAEAEA● &#AAAAAA§lᴅᴇsᴛɪɴʏ&#FFFFFF§lᴏ&#AAAAAA§lᴡɴᴇʀs &#EAEAEA●"),
        TextUtil.color("&#CCCCCC● &#888888§lᴅᴇsᴛɪɴʏᴏ&#FFFFFF§lᴡ&#888888§lɴᴇʀs &#CCCCCC●"),
        TextUtil.color("&#AAAAAA● &#888888§lᴅᴇsᴛɪɴʏᴏᴡ&#FFFFFF§lɴ&#888888§lᴇʀs &#AAAAAA●"),
        TextUtil.color("&#888888● &#888888§lᴅᴇsᴛɪɴʏᴏᴡɴ&#FFFFFF§lᴇ&#888888§lʀs &#888888●"),
        TextUtil.color("&#666666● &#888888§lᴅᴇsᴛɪɴʏᴏᴡɴᴇ&#FFFFFF§lʀ&#888888§ls &#666666●"),
        TextUtil.color("&#888888● &#FFFFFF§l◆ &#E0E0E0§lᴅᴇsᴛɪɴʏᴏᴡɴᴇʀs &#FFFFFF§l◆ &#888888●"),
        TextUtil.color("&#FFFFFF● &#FFFFFF§l★ ᴅᴇsᴛɪɴʏᴏᴡɴᴇʀs ★ &#FFFFFF●")
    };
}
