package me.PauMAVA.TTR.match;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.util.TextUtil;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AutoStarter {

    private final TTRCore plugin;
    private int taskID = -1;
    private int countdown = 10;

    public AutoStarter(TTRCore plugin, FileConfiguration configuration) {
        this.plugin = plugin;
    }

    public void addPlayerToQueue(Player player) {
        checkStart();
    }

    public void removePlayerFromQueue(Player player) {
        checkStart();
    }

    public void checkStart() {
        if (plugin.getCurrentMatch() == null || plugin.getCurrentMatch().getStatus() != MatchStatus.LOBBY) {
            return;
        }

        if (!plugin.getConfigManager().isAutoStartEnabled()) {
            return;
        }

        int required = plugin.getConfigManager().getAutoStartPlayers();
        int currentOnline = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!TTRCore.isAdmin(p)) currentOnline++;
        }

        if (currentOnline >= required) {
            if (!plugin.isCounting()) {
                startCountdown();
            }
        } else {
            if (plugin.isCounting()) {
                cancel();
                Bukkit.broadcastMessage(TTRPrefix.TTR_GAME + ChatColor.YELLOW + 
                        TextUtil.toTiny("Conteo cancelado. Se necesitan al menos ") + 
                        ChatColor.GOLD + required + ChatColor.YELLOW + TextUtil.toTiny(" jugadores."));
            }
        }
    }

    private void startCountdown() {
        plugin.setCounting(true);
        this.countdown = plugin.getConfigManager().getAutoStartCountdown();

        this.taskID = new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.isCounting() || plugin.getCurrentMatch() == null || plugin.getCurrentMatch().getStatus() != MatchStatus.LOBBY) {
                    cancel();
                    return;
                }

                int required = plugin.getConfigManager().getAutoStartPlayers();
                int activeCount = 0;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!TTRCore.isAdmin(p)) activeCount++;
                }
                if (activeCount < required) {
                    cancel();
                    plugin.setCounting(false);
                    Bukkit.broadcastMessage(TTRPrefix.TTR_GAME + ChatColor.RED + 
                            TextUtil.toTiny("¡Conteo cancelado por falta de jugadores!"));
                    return;
                }

                if (countdown == 10 || (countdown <= 5 && countdown > 0)) {
                    String title = ChatColor.GOLD + TextUtil.toTiny("Iniciando en");
                    String sub = ChatColor.YELLOW + String.valueOf(countdown) + TextUtil.toTiny("s");
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle(title, sub, 5, 20, 5);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, (countdown <= 3) ? 2.0f : 1.0f);
                    }
                }

                if (countdown <= 0) {
                    plugin.setCounting(false);
                    cancel();
                    int prep = plugin.getConfig().getInt("match.prestart_countdown", 15);
                    plugin.getCurrentMatch().startPreparationPhase(prep);
                    return;
                }

                countdown--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }

    public int getCountdown() {
        return countdown;
    }

    public void checkStartConditions() {
        checkStart();
    }

    public void cancelCountdown() {
        cancel();
    }

    public void startMatchCountdown(int seconds) {
        cancel();
        plugin.setCounting(true);
        this.countdown = seconds;

        this.taskID = new BukkitRunnable() {
            @Override
            public void run() {
                if (countdown <= 0) {
                    plugin.setCounting(false);
                    cancel();
                    int prep = plugin.getConfig().getInt("match.prestart_countdown", 15);
                    plugin.getCurrentMatch().startPreparationPhase(prep);
                    return;
                }

                if (countdown == 10 || (countdown <= 5 && countdown > 0)) {
                    String title = TextUtil.color("&#FFFFFF" + TextUtil.toTiny("Iniciando en"));
                    String sub = TextUtil.color("&#FF2E2E§l" + countdown + "s");
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle(title, sub, 0, 25, 5);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, (countdown <= 3) ? 2.0f : 1.0f);
                    }
                }

                countdown--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }

    public void cancel() {
        if (taskID != -1) {
            Bukkit.getScheduler().cancelTask(taskID);
            taskID = -1;
        }
        plugin.setCounting(false);
    }
}
