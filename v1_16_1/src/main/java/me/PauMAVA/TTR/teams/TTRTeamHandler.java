package me.PauMAVA.TTR.teams;

import me.PauMAVA.TTR.TTRCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

public class TTRTeamHandler {

    private List<TTRTeam> teams = new ArrayList<>();
    private Scoreboard sb;

    public void setUpDefaultTeams() {
        // USAR MAIN SCOREBOARD
        this.sb = Bukkit.getScoreboardManager().getMainScoreboard();

        for (String teamName : TTRCore.getInstance().getConfigManager().getTeamNames()) {
            this.teams.add(new TTRTeam(teamName));

            Team bukkitTeam = sb.getTeam(teamName);
            if (bukkitTeam == null) bukkitTeam = sb.registerNewTeam(teamName);

            ChatColor color = TTRCore.getInstance().getConfigManager().getTeamColor(teamName);
            bukkitTeam.setColor(color);
            bukkitTeam.setPrefix(color + "[" + teamName + "] ");

            // --- DESACTIVAR FUEGO AMIGO ---
            bukkitTeam.setAllowFriendlyFire(false);
            bukkitTeam.setCanSeeFriendlyInvisibles(true);
            // ------------------------------
        }
    }

    public boolean addPlayerToTeam(Player player, String teamIdentifier) {
        TTRTeam team = getTeam(teamIdentifier);
        if (team == null) return false;

        TTRTeam oldTeam = getPlayerTeam(player);
        if (oldTeam != null) removePlayer(oldTeam.getIdentifier(), player);

        team.addPlayer(player);

        // Meter al jugador en el equipo de Bukkit (para que el server sepa que son aliados)
        Team bukkitTeam = sb.getTeam(team.getIdentifier());
        if (bukkitTeam != null) bukkitTeam.addEntry(player.getName());

        return true;
    }

    public void removePlayer(String teamIdentifier, Player player) {
        TTRTeam team = getTeam(teamIdentifier);
        if (team != null) {
            team.removePlayer(player);
            Team bukkitTeam = sb.getTeam(teamIdentifier);
            if (bukkitTeam != null) bukkitTeam.removeEntry(player.getName());
        }
    }

    public TTRTeam getPlayerTeam(Player player) {
        for (TTRTeam team : this.teams) {
            if (team.getPlayers().contains(player.getName())) return team;
        }
        return null;
    }

    public TTRTeam getTeam(String teamIdentifier) {
        for (TTRTeam team : this.teams) {
            if (ChatColor.stripColor(teamIdentifier).equalsIgnoreCase(team.getIdentifier())) return team;
        }
        return null;
    }

    public void addPlayer(String teamIdentifier, Player player) {
        addPlayerToTeam(player, teamIdentifier);
    }
}