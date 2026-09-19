package me.PauMAVA.TTR.util;

import org.bukkit.ChatColor;

public class TTRPrefix {
    public static final String TTR_PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "" + ChatColor.BOLD + TextUtil.toTiny("DT") + ChatColor.DARK_GRAY + "] " + ChatColor.RESET;
    public static final String TTR_ADMIN = TTR_PREFIX + ChatColor.WHITE + "" + ChatColor.BOLD + TextUtil.toTiny("Destiny") + ChatColor.DARK_GRAY + " » " + ChatColor.GRAY;
    public static final String TTR_GAME = TTR_PREFIX + ChatColor.AQUA + TextUtil.toTiny("Juego") + ChatColor.DARK_GRAY + " » " + ChatColor.GRAY;
    public static final String TTR_SUCCESS = TTR_PREFIX + ChatColor.GREEN + TextUtil.toTiny("Éxito") + ChatColor.DARK_GRAY + " » " + ChatColor.GREEN;
    public static final String TTR_ERROR = TTR_PREFIX + ChatColor.RED + TextUtil.toTiny("Error") + ChatColor.DARK_GRAY + " » " + ChatColor.RED;

    public static final String DT_PREFIX = TTR_PREFIX;
    public static final String DT_ADMIN = TTR_ADMIN;
    public static final String DT_GAME = TTR_GAME;
    public static final String DT_SUCCESS = TTR_SUCCESS;
    public static final String DT_ERROR = TTR_ERROR;
}