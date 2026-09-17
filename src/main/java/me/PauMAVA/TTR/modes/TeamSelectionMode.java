package me.PauMAVA.TTR.modes;

import me.PauMAVA.TTR.util.TextUtil;
import org.bukkit.ChatColor;

public enum TeamSelectionMode {
    STANDARD(ChatColor.YELLOW + TextUtil.toTiny("Estándar (Libre/Auto)"), "Selección clásica de equipo por menú o asignación automática."),
    LEADER_VOTING(ChatColor.AQUA + TextUtil.toTiny("Votación de Líder"), "Votación interna por rondas hasta coronar a los 2 capitanes."),
    AUCTION_DRAFT(ChatColor.GOLD + TextUtil.toTiny("Subasta / Puja"), "Los capitanes pujan con créditos por los jugadores disponibles.");

    private final String displayName;
    private final String description;

    TeamSelectionMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public TeamSelectionMode next() {
        TeamSelectionMode[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }
}
