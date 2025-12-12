package me.PauMAVA.TTR.ui;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.teams.TTRTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ScoreboardHandler {

    private final TTRCore plugin;
    private int taskID;

    public ScoreboardHandler(TTRCore plugin) {
        this.plugin = plugin;
    }

    public void startScoreboardTask() {
        this.taskID = new BukkitRunnable() {
            @Override
            public void run() {
                updateAll();
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }

    public void stopScoreboardTask() {
        try {
            Bukkit.getScheduler().cancelTask(this.taskID);
        } catch (Exception ignored) {}
    }

    public void updateAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            updateScoreboard(p);
        }
    }


    public void refreshScoreboard() {
        updateAll();
    }

    private void updateScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;
        Scoreboard board = manager.getNewScoreboard();

        // Título
        Objective obj = board.registerNewObjective("TTR", "dummy", ChatColor.YELLOW + "" + ChatColor.BOLD + "THE TOWERS");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        int line = 15;

        // Fecha y Separador
        obj.getScore(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯").setScore(line--);

        // Estado / Tiempo
        if (plugin.getCurrentMatch() != null && plugin.getCurrentMatch().getStatus() == MatchStatus.INGAME) {
            String time = plugin.getCurrentMatch().getFormattedTime();
            obj.getScore(ChatColor.WHITE + "Tiempo: " + ChatColor.GREEN + time).setScore(line--);
        } else {
            String state = getMatchState(plugin.getCurrentMatch() != null ? plugin.getCurrentMatch().getStatus() : MatchStatus.STOPPED);
            obj.getScore(ChatColor.WHITE + "Estado: " + state).setScore(line--);
        }

        obj.getScore("§1").setScore(line--); // Espacio vacío

        // Puntos de Equipos
        if (plugin.getTeamHandler() != null) {
            for (TTRTeam team : plugin.getTeamHandler().getTeams()) {
                // Diseño: █ Red: 5/10
                String symbol = team.getColor() + "█ " + ChatColor.WHITE;
                String name = capitalize(team.getIdentifier());
                int points = team.getPoints();
                int max = (plugin.getCurrentMatch() != null) ? plugin.getCurrentMatch().getMaxPointsToWin() : 10;

                String entry = symbol + name + ": " + ChatColor.YELLOW + points + ChatColor.GRAY + "/" + max;
                obj.getScore(entry).setScore(line--);
            }
        }

        obj.getScore("§2").setScore(line--); // Espacio vacío

        // Datos Jugador
        TTRTeam playerTeam = plugin.getTeamHandler().getPlayerTeam(player);
        String teamName = (playerTeam != null) ? playerTeam.getColor() + capitalize(playerTeam.getIdentifier()) : ChatColor.GRAY + "Espectador";

        obj.getScore(ChatColor.WHITE + "Equipo: " + teamName).setScore(line--);

        // Kills
        if (plugin.getCurrentMatch() != null && plugin.getCurrentMatch().getStatus() == MatchStatus.INGAME) {
            int kills = plugin.getCurrentMatch().getKills(player);
            obj.getScore(ChatColor.WHITE + "Kills: " + ChatColor.GREEN + kills).setScore(line--);
        }

        // Footer
        obj.getScore(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯ ").setScore(line--);
        obj.getScore(ChatColor.GOLD + "SINDICATO").setScore(line--);

        player.setScoreboard(board);
    }

    private String getMatchState(MatchStatus status) {
        switch (status) {
            case LOBBY: return ChatColor.YELLOW + "Esperando...";
            case STARTING: return ChatColor.GOLD + "Iniciando";
            case INGAME: return ChatColor.GREEN + "En Curso";
            case ENDED: return ChatColor.RED + "Terminado";
            default: return ChatColor.RED + "Offline";
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}