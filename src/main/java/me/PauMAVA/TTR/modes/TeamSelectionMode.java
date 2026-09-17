package me.PauMAVA.TTR.modes;

import me.PauMAVA.TTR.util.TextUtil;
import org.bukkit.ChatColor;

public enum TeamSelectionMode {
    STANDARD(ChatColor.YELLOW + TextUtil.toTiny("Estándar (Libre/Auto)"), "Selección clásica de equipo por menú o asignación automática."),
    AUCTION_DRAFT(ChatColor.GOLD + TextUtil.toTiny("Subasta / Puja"), "Votación de líderes y posterior puja con créditos por miembros.");

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
