package me.PauMAVA.TTR.teams;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TTRTeam {

    private String identifier;
    private int points;
    private Set<String> players = new HashSet<>();

    public TTRTeam(String identifier) {
        this.identifier = identifier;
        this.points = 0;
    }

    public String getIdentifier() {
        return identifier;
    }

    public int getPoints() {
        return points;
    }

    public void addPoints(int amount) {
        this.points += amount;
    }

    public void addPlayer(Player p) {
        players.add(p.getName());
    }

    public void removePlayer(Player p) {
        players.remove(p.getName());
    }

    public boolean hasPlayer(Player p) {
        return players.contains(p.getName());
    }

    // Devuelve los NOMBRES (String) - Para guardar en config o comparar
    public Set<String> getPlayers() {
        return players;
    }

    // --- NUEVO MÉTODO: Devuelve JUGADORES (Player)
    public List<Player> getOnlinePlayers() {
        List<Player> online = new ArrayList<>();
        for (String name : players) {
            Player p = Bukkit.getPlayer(name);
            if (p != null) {
                online.add(p);
            }
        }
        return online;
    }
}