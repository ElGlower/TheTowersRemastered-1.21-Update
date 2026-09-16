package me.PauMAVA.TTR.teams;

import me.PauMAVA.TTR.TTRCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TTRTeamHandler {

    private final List<TTRTeam> teams = new ArrayList<>();
    private final Map<String, TTRTeam> teamMap = new HashMap<>();

    public void setUpDefaultTeams() {
        createTeam("Red", ChatColor.RED);
        createTeam("Blue", ChatColor.BLUE);

        Bukkit.getScheduler().runTaskLater(TTRCore.getInstance(), this::loadSpawnsFromConfig, 1L);
    }

    public void loadSpawnsFromConfig() {
        for (TTRTeam team : teams) {
            Location loc = TTRCore.getInstance().getConfigManager().getTeamSpawn(team.getIdentifier());

            if (loc != null) {
                team.setSpawnPoint(loc);
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[TTR] Spawn cargado correctamente para: " + team.getIdentifier());
            } else {
                Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[TTR] ADVERTENCIA: No se encontró spawn guardado para el equipo " + team.getIdentifier());
            }
        }
    }

    public void createTeam(String identifier, ChatColor color) {
        TTRTeam newTeam = new TTRTeam(identifier, color);
        teams.add(newTeam);
        teamMap.put(identifier, newTeam);
    }

    public TTRTeam getTeam(String identifier) {
        for (String key : teamMap.keySet()) {
            if (key.equalsIgnoreCase(identifier)) return teamMap.get(key);
        }
        return null;
    }

    public TTRTeam getPlayerTeam(Player player) {
        for (TTRTeam team : teams) {
            if (team.getPlayers().contains(player.getUniqueId())) return team;
        }
        return null;
    }

    public void addPlayerToTeam(Player player, String teamIdentifier) {
        removePlayer(player);
        TTRTeam team = getTeam(teamIdentifier);

        if (team != null) {
            team.addPlayer(player);
            player.sendMessage(ChatColor.GRAY + "Te has unido al equipo " + team.getColor() + team.getIdentifier());

            if (team.getSpawnPoint() != null) {
                player.teleport(team.getSpawnPoint());
            }
        }
    }

    public void removePlayer(Player player) {
        TTRTeam current = getPlayerTeam(player);
        if (current != null) current.removePlayer(player);
    }

    public List<TTRTeam> getTeams() { return teams; }

    public void restartTeams() {
        for (TTRTeam team : teams) {
            team.setPoints(0);
            team.setArmorProtectionLevel(0);
            team.setTeamSpeed(false);
            team.setTeamHaste(false);
        }
    }

    public void clearTeams() {
        for (TTRTeam team : teams) {
            team.getPlayers().clear();
        }
    }
}