package me.PauMAVA.TTR.match;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class AutoStarter {

    private TTRCore plugin;
    private int playersToStart;
    private boolean enabled;
    private List<Player> queue = new ArrayList<>();
    private int taskID;

    public AutoStarter(TTRCore plugin, FileConfiguration configuration) {
        this.plugin = plugin;
        this.enabled = false;
        this.playersToStart = 999; // Esto lo puse en 999 porque la verdad no conozco la forma de hacerlo xd
    }

    public void addPlayerToQueue(Player player) {
        if (!queue.contains(player)) {
            queue.add(player);
            checkStart();
        }
    }

    public void removePlayerFromQueue(Player player) {
        queue.remove(player);
    }

    private void checkStart() {
        // Método silenciado intencionalmente para control manual
        if (queue.size() >= playersToStart) {
        }
    }

    public void cancel() {
        if (plugin.isCounting()) {
            Bukkit.getScheduler().cancelTask(taskID);
            plugin.setCounting(false);
        }
    }
}