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

public class TTRScoreboard {

    private int taskID;

    public void startScoreboardTask() {
        this.taskID = new BukkitRunnable() {
            @Override
            public void run() {
                refreshScoreboard();
            }
        }.runTaskTimer(TTRCore.getInstance(), 0L, 20L).getTaskId();
    }

    public void refreshScoreboard() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateScoreboard(player);
        }
    }

    public void stopScoreboardTask() {
        Bukkit.getScheduler().cancelTask(this.taskID);
    }

    public void removeScoreboard() {
        stopScoreboardTask();
    }

    private void updateScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        Scoreboard board = manager.getMainScoreboard();

        if (player.getScoreboard() != board) {
            player.setScoreboard(board);
        }

        Objective objective = board.getObjective("ttr_board");
        if (objective == null) {
            objective = board.registerNewObjective("ttr_board", Criteria.DUMMY, ChatColor.GOLD + "" + ChatColor.BOLD + " THE TOWERS ");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }


        TTRTeam redTeam = TTRCore.getInstance().getTeamHandler().getTeam("Red");
        TTRTeam blueTeam = TTRCore.getInstance().getTeamHandler().getTeam("Blue");
        int kills = TTRCore.getInstance().getCurrentMatch().getKills(player);
        String date = new SimpleDateFormat("dd/MM/yy").format(new Date());

        setScore(objective, ChatColor.GRAY + date, 12);
        setScore(objective, "§1", 11);

        // --- LÓGICA DE PUNTOS DINÁMICA ---
        int maxPoints;
        // Si estamos jugando, leemos el valor de la partida actual (que cambia con /ttrconfig)
        if (TTRCore.getInstance().getCurrentMatch().getStatus() == MatchStatus.INGAME) {
            maxPoints = TTRCore.getInstance().getCurrentMatch().getMaxPointsToWin();
        } else {
            // Si estamos en lobby, leemos de la config guardada
            maxPoints = TTRCore.getInstance().getConfigManager().getMaxPoints();
        }
        // -----------------------------------------------

        int redScore = (redTeam != null) ? redTeam.getPoints() : 0;
        int blueScore = (blueTeam != null) ? blueTeam.getPoints() : 0;

        setScore(objective, ChatColor.RED + "Rojo: " + ChatColor.WHITE + redScore + "/" + maxPoints, 10);
        setScore(objective, "§2", 9);
        setScore(objective, ChatColor.BLUE + "Azul: " + ChatColor.WHITE + blueScore + "/" + maxPoints, 8);
        setScore(objective, "§3", 7);
        setScore(objective, ChatColor.GREEN + "Kills: " + ChatColor.AQUA + kills, 6);
        setScore(objective, "§4", 5);

        // Mostrar tiempo solo si está jugando
        if (TTRCore.getInstance().getCurrentMatch().getStatus() == MatchStatus.INGAME) {
            String time = TTRCore.getInstance().getCurrentMatch().getFormattedTime();
            setScore(objective, ChatColor.YELLOW + "Tiempo: " + ChatColor.WHITE + time, 4);
        } else {
            setScore(objective, ChatColor.YELLOW + "play.server.com", 4);
        }
    }

    private void setScore(Objective objective, String text, int score) {
        Score s = objective.getScore(text);
        if (s.getScore() != score) {
            for (String entry : objective.getScoreboard().getEntries()) {
                if (objective.getScore(entry).getScore() == score) {
                    if (!entry.equals(text)) {
                        objective.getScoreboard().resetScores(entry);
                    }
                }
            }
            s.setScore(score);
        }
    }
}