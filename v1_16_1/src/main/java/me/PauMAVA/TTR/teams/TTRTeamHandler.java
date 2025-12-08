package me.PauMAVA.TTR.teams;

import org.bukkit.ChatColor;
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
    }

    public void createTeam(String identifier, ChatColor color) {
        TTRTeam newTeam = new TTRTeam(identifier, color);
        teams.add(newTeam);
        teamMap.put(identifier, newTeam);
    }

    public TTRTeam getTeam(String identifier) {
        return teamMap.get(identifier);
    }

    public TTRTeam getPlayerTeam(Player player) {
        for (TTRTeam team : teams) {
            if (team.getPlayers().contains(player.getUniqueId())) return team;
        }
        return null;
    }

    // Método corregido para ForceJoinCommand
    public void addPlayer(String teamIdentifier, Player player) {
        addPlayerToTeam(player, teamIdentifier);
    }

    public void addPlayerToTeam(Player player, String teamIdentifier) {
        removePlayer(player);
        TTRTeam team = getTeam(teamIdentifier);
        if (team != null) team.addPlayer(player);
    }

    public void removePlayer(Player player) {
        TTRTeam current = getPlayerTeam(player);
        if (current != null) current.removePlayer(player);
    }

    public List<TTRTeam> getTeams() { return teams; }
}