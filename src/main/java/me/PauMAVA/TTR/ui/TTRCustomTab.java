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

        String killsInfo = "";
        if (plugin.getCurrentMatch() != null && plugin.getCurrentMatch().getStatus() == MatchStatus.INGAME) {
            int kills = plugin.getCurrentMatch().getKills(target);
            killsInfo = ChatColor.DARK_GRAY + " [" + ChatColor.YELLOW + TextUtil.toTiny(String.valueOf(kills)) + ChatColor.DARK_GRAY + "]";
        }

        String rolePrefix;
        String teamKey;
        ChatColor nameColor;
        NamedTextColor teamColor;

        if (isStaff) {
            teamKey = "00_destiny";
            rolePrefix = DestinyTheme.DESTINY_ROLE_BADGE + " ";
            nameColor = ChatColor.WHITE;
            teamColor = NamedTextColor.WHITE;
        } else if (team != null) {
            boolean isLeader = team.isLeader(target.getUniqueId());
            boolean isRed = team.getIdentifier().equalsIgnoreCase("Red");

            if (isRed) {
                teamKey = isLeader ? "10_red_l" : "11_red";
                rolePrefix = isLeader ? "§6★ §c[" + TextUtil.toTiny("Rojo") + "] " : "§c[" + TextUtil.toTiny("Rojo") + "] ";
                nameColor = ChatColor.RED;
                teamColor = NamedTextColor.RED;
            } else {
                teamKey = isLeader ? "20_blue_l" : "21_blue";
                rolePrefix = isLeader ? "§6★ §9[" + TextUtil.toTiny("Azul") + "] " : "§9[" + TextUtil.toTiny("Azul") + "] ";
                nameColor = ChatColor.BLUE;
                teamColor = NamedTextColor.BLUE;
            }
        } else {
            MatchStatus status = plugin.getCurrentMatch() != null ? plugin.getCurrentMatch().getStatus() : MatchStatus.STOPPED;
            if (status == MatchStatus.INGAME) {
                teamKey = "95_spec";
                rolePrefix = "§7[" + TextUtil.toTiny("Espec") + "] ";
                nameColor = ChatColor.GRAY;
                teamColor = NamedTextColor.GRAY;
            } else {
                teamKey = "90_lobby";
                rolePrefix = "§7[" + TextUtil.toTiny("Lobby") + "] ";
                nameColor = ChatColor.WHITE;
                teamColor = NamedTextColor.GRAY;
            }
        }

        String tabFormattedName = rolePrefix + nameColor + target.getName() + killsInfo;
        try {
            net.kyori.adventure.text.Component comp = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(tabFormattedName);
            target.playerListName(comp);
        } catch (Throwable t) {
            target.setPlayerListName(tabFormattedName);
        }

        assignToScoreboardTeam(board, target, teamKey, rolePrefix, teamColor);
    }

    private void assignToScoreboardTeam(Scoreboard board, Player p, String teamKey, String rolePrefix, NamedTextColor teamColor) {
        try {
            Team tabTeam = board.getTeam(teamKey);
            if (tabTeam == null) {
                tabTeam = board.registerNewTeam(teamKey);
            }
            tabTeam.color(teamColor);
            tabTeam.prefix(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(rolePrefix));

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
