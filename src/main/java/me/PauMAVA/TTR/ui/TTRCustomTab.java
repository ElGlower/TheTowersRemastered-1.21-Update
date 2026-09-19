package me.PauMAVA.TTR.ui;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

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

        int playingCount = Bukkit.getOnlinePlayers().size();
        int ping = player.getPing();

        String footer = "\n" +
                ChatColor.GRAY + TextUtil.toTiny("Jugadores: ") + ChatColor.AQUA + TextUtil.toTiny(String.valueOf(playingCount)) +
                ChatColor.DARK_GRAY + "  ▪  " + ChatColor.GRAY + TextUtil.toTiny("Ping: ") + getPingDisplay(ping) + "\n" +
                ChatColor.DARK_GRAY + "§m                             \n" +
                animatedFooter + "\n";

        player.setPlayerListHeaderFooter(header, footer);
        updatePlayerNameAndSorting(player);
    }

    private void updatePlayerNameAndSorting(Player player) {
        TTRTeam team = plugin.getTeamHandler() != null ? plugin.getTeamHandler().getPlayerTeam(player) : null;
        boolean isStaff = TTRCore.isAdmin(player);
        String formattedName;

        String killsInfo = "";
        if (plugin.getCurrentMatch() != null && plugin.getCurrentMatch().getStatus() == MatchStatus.INGAME) {
            int kills = plugin.getCurrentMatch().getKills(player);
            killsInfo = ChatColor.DARK_GRAY + " [" + ChatColor.YELLOW + TextUtil.toTiny(String.valueOf(kills)) + ChatColor.DARK_GRAY + "]";
        }

        // Determinar prefijo y equipo de scoreboard para ordenamiento estricto
        String teamKey;
        if (isStaff && (team == null || plugin.getCurrentMatch() == null || plugin.getCurrentMatch().getStatus() != MatchStatus.INGAME)) {
            teamKey = "00_admin";
            formattedName = TextUtil.color("&#FF5555§l[ADMIN] ") + ChatColor.WHITE + player.getName();
        } else if (team != null) {
            boolean isLeader = team.isLeader(player.getUniqueId());
            ChatColor color = team.getColor();
            boolean isRed = team.getIdentifier().equalsIgnoreCase("Red");

            if (isRed) {
                teamKey = isLeader ? "10_red_l" : "11_red";
            } else {
                teamKey = isLeader ? "20_blue_l" : "21_blue";
            }

            String leaderStar = isLeader ? ChatColor.GOLD + "★ " : "";
            String teamTag = color + "[" + TextUtil.toTiny(team.getIdentifier().toUpperCase()) + "] ";
            formattedName = leaderStar + teamTag + color + player.getName() + killsInfo;
        } else {
            teamKey = "90_spec";
            formattedName = ChatColor.GRAY + "[ESPEC] " + player.getName();
        }

        player.setPlayerListName(formattedName);

        // Asignar al equipo del Scoreboard para forzar el ordenamiento nativo en Tab
        Scoreboard board = player.getScoreboard();
        if (board != null) {
            assignToScoreboardTeam(board, player, teamKey);
        }
    }

    private void assignToScoreboardTeam(Scoreboard board, Player p, String teamKey) {
        try {
            Team tabTeam = board.getTeam(teamKey);
            if (tabTeam == null) {
                tabTeam = board.registerNewTeam(teamKey);
                if (teamKey.startsWith("10") || teamKey.startsWith("11")) {
                    tabTeam.color(NamedTextColor.RED);
                } else if (teamKey.startsWith("20") || teamKey.startsWith("21")) {
                    tabTeam.color(NamedTextColor.BLUE);
                } else if (teamKey.startsWith("00")) {
                    tabTeam.color(NamedTextColor.GOLD);
                } else {
                    tabTeam.color(NamedTextColor.GRAY);
                }
            }
            if (!tabTeam.hasEntry(p.getName())) {
                // Remover de otros equipos antes de reasignar
                for (Team t : board.getTeams()) {
                    if (t.getName().matches("\\d{2}_.*") && t.hasEntry(p.getName())) {
                        t.removeEntry(p.getName());
                    }
                }
                tabTeam.addEntry(p.getName());
            }
        } catch (Throwable ignored) {}
    }

    private String getMatchStatus() {
        if (plugin.getCurrentMatch() == null) return ChatColor.GRAY + TextUtil.toTiny("Esperando...");

        MatchStatus status = plugin.getCurrentMatch().getStatus();
        if (status == MatchStatus.INGAME) {
            String time = plugin.getCurrentMatch().getFormattedTime();
            return ChatColor.GREEN + TextUtil.toTiny("En Partida ") + ChatColor.DARK_GRAY + "» " + ChatColor.WHITE + TextUtil.toTiny(time);
        } else if (status == MatchStatus.PREPARATION) {
            int rem = plugin.getCurrentMatch().getPrepRemaining();
            return ChatColor.AQUA + TextUtil.toTiny("Preparación en Bases ") + ChatColor.DARK_GRAY + "» " + ChatColor.YELLOW + TextUtil.toTiny(rem + "s");
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
