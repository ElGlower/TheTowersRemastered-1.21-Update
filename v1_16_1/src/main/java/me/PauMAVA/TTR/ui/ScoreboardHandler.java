package me.PauMAVA.TTR.ui;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.teams.TTRTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

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
                for (Player p : Bukkit.getOnlinePlayers()) {
                    updateScoreboard(p);
                }
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

        Objective obj = board.registerNewObjective("TTR", "dummy", ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "THE TOWERS");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        obj.getScore("§7----------------").setScore(10);

        String estado = "Esperando...";
        if (plugin.getCurrentMatch() != null) {
            if (plugin.getCurrentMatch().getStatus() == MatchStatus.INGAME) estado = "En Juego";
            else if (plugin.getCurrentMatch().getStatus() == MatchStatus.ENDED) estado = "Terminado";
        }
        obj.getScore("§fEstado: §a" + estado).setScore(9);
        obj.getScore("§f ").setScore(8);

        if (plugin.getTeamHandler() != null) {
            for (TTRTeam team : plugin.getTeamHandler().getTeams()) {
                String entry = team.getColor() + team.getIdentifier() + ": §f" + team.getPoints();
                obj.getScore(entry).setScore(team.getIdentifier().equals("Red") ? 7 : 6);
            }
        }

        obj.getScore("§f  ").setScore(5);

        if (plugin.getCurrentMatch() != null && plugin.getCurrentMatch().getStatus() == MatchStatus.INGAME) {
            obj.getScore("§fTiempo: §e" + plugin.getCurrentMatch().getFormattedTime()).setScore(4);
        } else {
            obj.getScore("§fTiempo: §e--:--").setScore(4);
        }

        obj.getScore("§7---------------- ").setScore(1);
        obj.getScore("§eSINDICATO").setScore(0);

        player.setScoreboard(board);
    }
}