package me.PauMAVA.TTR.ui;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.teams.TTRTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TTRCustomTab extends BukkitRunnable {

    private final TTRCore plugin;

    public TTRCustomTab(TTRCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.enabled()) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            updateTabList(player);
        }
    }

    private void updateTabList(Player player) {
        String header = "\n" +
                ChatColor.DARK_GRAY + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬" + "\n" +
                ChatColor.GOLD + "" + ChatColor.BOLD + " THE TOWERS " + ChatColor.YELLOW + "REMASTERED" + "\n" +
                "\n" +
                ChatColor.GRAY + "Estado: " + getMatchStatus() + "\n" +
                ChatColor.DARK_GRAY + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬" + "\n";
        String footer = "\n" +
                ChatColor.DARK_GRAY + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬" + "\n" +
                ChatColor.AQUA + "Jugadores: " + ChatColor.WHITE + Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers() +
                ChatColor.GRAY + "  |  " +
                ChatColor.GREEN + "Ping: " + player.getPing() + "ms" + "\n" +
                "\n" +
                ChatColor.YELLOW + "DESTINY OWNERS HOST" + "\n";

        player.setPlayerListHeaderFooter(header, footer);
        updatePlayerName(player);
    }

    private void updatePlayerName(Player player) {
        TTRTeam team = plugin.getTeamHandler().getPlayerTeam(player);
        String formattedName;

        if (team != null) {
            ChatColor color = team.getColor();
            formattedName = ChatColor.DARK_GRAY + "[" + color + team.getIdentifier().charAt(0) + ChatColor.DARK_GRAY + "] "
                    + color + player.getName();
        } else {
            if (player.isOp()) {
                formattedName = ChatColor.RED + "ADMIN " + ChatColor.WHITE + player.getName();
            } else {
                formattedName = ChatColor.GRAY + player.getName();
            }
        }
        player.setPlayerListName(formattedName);
    }

    private String getMatchStatus() {
        if (plugin.getCurrentMatch() == null) return ChatColor.RED + "Desactivado"; // Protección extra

        if (plugin.getCurrentMatch().getStatus() == MatchStatus.INGAME) {
            String time = plugin.getCurrentMatch().getFormattedTime();
            return ChatColor.GREEN + "En Curso " + ChatColor.GRAY + "(" + ChatColor.WHITE + time + ChatColor.GRAY + ")";
        } else if (plugin.getCurrentMatch().getStatus() == MatchStatus.LOBBY) {
            return ChatColor.YELLOW + "Esperando Jugadores...";
        } else {
            return ChatColor.RED + "Finalizado";
        }
    }
}