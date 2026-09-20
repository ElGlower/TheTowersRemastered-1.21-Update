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
        int maxPlayers = Bukkit.getMaxPlayers();
        int ping = player.getPing();

        String footer = "\n" +
                ChatColor.GRAY + TextUtil.toTiny("Jugadores: ") + ChatColor.AQUA + TextUtil.toTiny(String.valueOf(playingCount)) +
                ChatColor.DARK_GRAY + "/" + ChatColor.GRAY + TextUtil.toTiny(String.valueOf(maxPlayers)) +
                ChatColor.DARK_GRAY + "  ▪  " + ChatColor.GRAY + TextUtil.toTiny("Ping: ") + getPingDisplay(ping) + "\n" +
                ChatColor.DARK_GRAY + "§m                             \n" +
                animatedFooter + "\n";

        player.setPlayerListHeaderFooter(header, footer);
        updatePlayerNameAndSorting(player);
    }

    private void updatePlayerNameAndSorting(Player player) {
        Scoreboard board = player.getScoreboard();
        if (board == null) return;

        for (Player target : Bukkit.getOnlinePlayers()) {
            syncPlayerInScoreboard(board, player, target);
        }
    }

    private void syncPlayerInScoreboard(Scoreboard board, Player viewer, Player target) {
        TTRTeam team = plugin.getTeamHandler() != null ? plugin.getTeamHandler().getPlayerTeam(target) : null;
        boolean isStaff = TTRCore.isAdmin(target);
        String formattedName;

        String killsInfo = "";
        if (plugin.getCurrentMatch() != null && plugin.getCurrentMatch().getStatus() == MatchStatus.INGAME) {
            int kills = plugin.getCurrentMatch().getKills(target);
            killsInfo = ChatColor.DARK_GRAY + " [" + ChatColor.YELLOW + TextUtil.toTiny(String.valueOf(kills)) + ChatColor.DARK_GRAY + "]";
        }

        String teamKey;
        if (isStaff) {
            teamKey = "00_destiny";
            formattedName = ChatColor.WHITE + target.getName();
        } else if (team != null) {
            boolean isLeader = team.isLeader(target.getUniqueId());
            ChatColor color = team.getColor();
            boolean isRed = team.getIdentifier().equalsIgnoreCase("Red");

            if (isRed) {
                teamKey = isLeader ? "10_red_l" : "11_red";
            } else {
                teamKey = isLeader ? "20_blue_l" : "21_blue";
            }

            formattedName = color + target.getName() + killsInfo;
        } else {
            teamKey = "90_spec";
            formattedName = ChatColor.GRAY + target.getName();
        }

        if (viewer.equals(target)) {
            target.setPlayerListName(formattedName);
        }

        assignToScoreboardTeam(board, target, teamKey);
    }

    private void assignToScoreboardTeam(Scoreboard board, Player p, String teamKey) {
        try {
            Team tabTeam = board.getTeam(teamKey);
            if (tabTeam == null) {
                tabTeam = board.registerNewTeam(teamKey);
                if (teamKey.startsWith("10")) {
                    tabTeam.color(NamedTextColor.RED);
                    tabTeam.prefix(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize("§6★ §c[" + TextUtil.toTiny("Rojo") + "] "));
                } else if (teamKey.startsWith("11")) {
                    tabTeam.color(NamedTextColor.RED);
                    tabTeam.prefix(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize("§c[" + TextUtil.toTiny("Rojo") + "] "));
                } else if (teamKey.startsWith("20")) {
                    tabTeam.color(NamedTextColor.BLUE);
                    tabTeam.prefix(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize("§6★ §9[" + TextUtil.toTiny("Azul") + "] "));
                } else if (teamKey.startsWith("21")) {
                    tabTeam.color(NamedTextColor.BLUE);
                    tabTeam.prefix(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize("§9[" + TextUtil.toTiny("Azul") + "] "));
                } else if (teamKey.startsWith("00")) {
                    tabTeam.color(NamedTextColor.WHITE);
                    tabTeam.prefix(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(DestinyTheme.DESTINY_ROLE_BADGE + " "));
                } else {
                    tabTeam.color(NamedTextColor.GRAY);
                    tabTeam.prefix(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize("§7[" + TextUtil.toTiny("Espec") + "] "));
                }
            }
            if (!tabTeam.hasEntry(p.getName())) {
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
