package me.PauMAVA.TTR.ui;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TTRCustomTab extends BukkitRunnable {

    private final TTRCore plugin;
    private int animStep = 0;

    public TTRCustomTab(TTRCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.enabled()) return;

        animStep++;
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateTabList(player);
        }
    }

    private void updateTabList(Player player) {
        String animatedTitle = DestinyTheme.SHINE_FRAMES[animStep % DestinyTheme.SHINE_FRAMES.length];
        String animatedFooter = DestinyTheme.FOOTER_FRAMES[(animStep / 2) % DestinyTheme.FOOTER_FRAMES.length];

        String header = "\n" +
                animatedTitle + "\n" +
                ChatColor.DARK_GRAY + "§m                             \n" +
                getMatchStatus() + "\n";

        int playingCount = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!TTRCore.isAdmin(p)) playingCount++;
        }
        int ping = player.getPing();

        String footer = "\n" +
                ChatColor.GRAY + TextUtil.toTiny("Jugadores: ") + ChatColor.AQUA + TextUtil.toTiny(String.valueOf(playingCount)) +
                ChatColor.DARK_GRAY + "  ▪  " + ChatColor.GRAY + TextUtil.toTiny("Ping: ") + getPingDisplay(ping) + "\n" +
                ChatColor.DARK_GRAY + "§m                             \n" +
                animatedFooter + "\n";

        player.setPlayerListHeaderFooter(header, footer);
        updatePlayerName(player);
    }

    private void updatePlayerName(Player player) {
        TTRTeam team = plugin.getTeamHandler().getPlayerTeam(player);
        String formattedName;

        String killsInfo = "";
        if (plugin.getCurrentMatch() != null && plugin.getCurrentMatch().getStatus() == MatchStatus.INGAME) {
            int kills = plugin.getCurrentMatch().getKills(player);
            killsInfo = ChatColor.DARK_GRAY + " [" + ChatColor.YELLOW + TextUtil.toTiny(String.valueOf(kills)) + ChatColor.DARK_GRAY + "]";
        }

        boolean isStaff = TTRCore.isAdmin(player);

        if (team != null) {
            boolean isLeader = team.isLeader(player.getUniqueId());
            String leaderBadge = isLeader ? ChatColor.GOLD + "★" + TextUtil.toTiny("Líder ") : "";
            String teamPrefix = TextUtil.toTiny(team.getIdentifier().substring(0, 1).toUpperCase());
            ChatColor color = team.getColor();
            String staffBadge = isStaff ? DestinyTheme.DESTINY_ROLE_BADGE + " " : "";
            formattedName = color + " ▪ " + staffBadge + leaderBadge + color + "" + ChatColor.BOLD + teamPrefix + ChatColor.DARK_GRAY + " | " + color + player.getName() + killsInfo;
        } else {
            if (isStaff) {
                formattedName = TextUtil.color("&#888888▪ ") + DestinyTheme.DESTINY_ROLE_BADGE + ChatColor.DARK_GRAY + " | " + ChatColor.WHITE + player.getName();
            } else {
                formattedName = ChatColor.GRAY + " ▪ " + ChatColor.WHITE + player.getName();
            }
        }

        player.setPlayerListName(formattedName);
    }

    private String getMatchStatus() {
        if (plugin.getCurrentMatch() == null) return ChatColor.RED + TextUtil.toTiny("Offline");

        MatchStatus status = plugin.getCurrentMatch().getStatus();
        if (status == MatchStatus.INGAME) {
            String time = plugin.getCurrentMatch().getFormattedTime();
            return ChatColor.GREEN + TextUtil.toTiny("En Partida ") + ChatColor.DARK_GRAY + "» " + ChatColor.WHITE + TextUtil.toTiny(time);
        } else if (status == MatchStatus.LOBBY) {
            return ChatColor.YELLOW + TextUtil.toTiny("Esperando jugadores...");
        } else if (status == MatchStatus.STARTING) {
            return ChatColor.GOLD + TextUtil.toTiny("Iniciando...");
        } else {
            return ChatColor.RED + TextUtil.toTiny("Terminado");
        }
    }

    private String getPingDisplay(int ping) {
        if (ping < 60) return ChatColor.GREEN + TextUtil.toTiny(ping + "ms") + ChatColor.DARK_GRAY + " ▂▃▅";
        if (ping < 120) return ChatColor.YELLOW + TextUtil.toTiny(ping + "ms") + ChatColor.DARK_GRAY + " ▂▃";
        return ChatColor.RED + TextUtil.toTiny(ping + "ms") + ChatColor.DARK_GRAY + " ▂";
    }
}
