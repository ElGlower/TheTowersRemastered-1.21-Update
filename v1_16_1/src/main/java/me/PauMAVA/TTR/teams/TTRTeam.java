package me.PauMAVA.TTR.teams;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TTRTeam {

    private final String identifier;
    private final ChatColor color;
    private final List<UUID> players;
    private int points;

    public TTRTeam(String identifier, ChatColor color) {
        this.identifier = identifier;
        this.color = color;
        this.players = new ArrayList<>();
        this.points = 0;
    }

    // ↓↓↓ ESTOS SON LOS MÉTODOS QUE TE FALTAN ↓↓↓
    public String getIdentifier() {
        return identifier;
    }

    public ChatColor getColor() {
        return color;
    }
    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    public List<UUID> getPlayers() {
        return players;
    }

    public void addPlayer(Player player) {
        if (!players.contains(player.getUniqueId())) {
            players.add(player.getUniqueId());
        }
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
    }

    public int getPoints() { return points; }

    public void addPoint() { this.points++; }

    public void addPoints(int amount) {
        this.points += amount;
    }

    public void setPoints(int points) { this.points = points; }
}