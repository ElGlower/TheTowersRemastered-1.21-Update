package me.PauMAVA.TTR.ui;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.network.NetworkFairnessManager;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.util.TextUtil;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TTRCustomTab extends BukkitRunnable {

    private final TTRCore plugin;
    private int animStep = 0;

    // Cache para evitar envíos de paquetes idénticos (Anti-Packet Flooding)
    private static final Map<UUID, String> lastFormattedNames = new ConcurrentHashMap<>();
    private static final Map<UUID, String> lastHeaders = new ConcurrentHashMap<>();
    private static final Map<UUID, String> lastFooters = new ConcurrentHashMap<>();
    private static final Map<String, String> lastAssignedTeam = new ConcurrentHashMap<>();

    public TTRCustomTab(TTRCore plugin) {
        this.plugin = plugin;
    }

    public static void clearCache(UUID uuid) {
        lastFormattedNames.remove(uuid);
        lastHeaders.remove(uuid);
        lastFooters.remove(uuid);
    }

    @Override
    public void run() {
        if (!plugin.enabled()) return;

        animStep++;

        // 1. Actualizar el nombre en lista de cada jugador solo si cambió (O(N) en vez de O(N^2))
        for (Player target : Bukkit.getOnlinePlayers()) {
            updatePlayerListName(target);
        }

        // 2. Actualizar Header y Footer de cada jugador (solo si cambió el texto)
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateTabHeaderFooter(player);
            updatePlayerSorting(player);
        }
    }

    private void updatePlayerListName(Player target) {
        TTRTeam team = plugin.getTeamHandler() != null ? plugin.getTeamHandler().getPlayerTeam(target) : null;
        boolean isStaff = TTRCore.isAdmin(target);

        String killsInfo = "";
        if (plugin.getCurrentMatch() != null && plugin.getCurrentMatch().getStatus() == MatchStatus.INGAME) {
            int kills = plugin.getCurrentMatch().getKills(target);
            killsInfo = ChatColor.DARK_GRAY + " [" + ChatColor.YELLOW + TextUtil.toTiny(String.valueOf(kills)) + ChatColor.DARK_GRAY + "]";
        }

        String rolePrefix;
        ChatColor nameColor;

        if (isStaff) {
            rolePrefix = DestinyTheme.DESTINY_ROLE_BADGE + " ";
            nameColor = ChatColor.WHITE;
        } else if (team != null) {
            boolean isLeader = team.isLeader(target.getUniqueId());
            boolean isRed = team.getIdentifier().equalsIgnoreCase("Red");

            if (isRed) {
                rolePrefix = isLeader ? "§6★ §c[" + TextUtil.toTiny("Rojo") + "] " : "§c[" + TextUtil.toTiny("Rojo") + "] ";
                nameColor = ChatColor.RED;
            } else {
                rolePrefix = isLeader ? "§6★ §9[" + TextUtil.toTiny("Azul") + "] " : "§9[" + TextUtil.toTiny("Azul") + "] ";
                nameColor = ChatColor.BLUE;
            }
        } else {
            MatchStatus status = plugin.getCurrentMatch() != null ? plugin.getCurrentMatch().getStatus() : MatchStatus.STOPPED;
            if (status == MatchStatus.INGAME) {
                rolePrefix = "§7[" + TextUtil.toTiny("Espec") + "] ";
                nameColor = ChatColor.GRAY;
            } else {
                rolePrefix = "§7[" + TextUtil.toTiny("Lobby") + "] ";
                nameColor = ChatColor.WHITE;
            }
        }

        String tabFormattedName = rolePrefix + nameColor + target.getName() + killsInfo;
        String prev = lastFormattedNames.get(target.getUniqueId());

        if (!tabFormattedName.equals(prev)) {
            lastFormattedNames.put(target.getUniqueId(), tabFormattedName);
            try {
                net.kyori.adventure.text.Component comp = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(tabFormattedName);
                target.playerListName(comp);
            } catch (Throwable t) {
                target.setPlayerListName(tabFormattedName);
            }
        }
    }

    private void updateTabHeaderFooter(Player player) {
        String animatedTitle = DestinyTheme.SHINE_FRAMES[animStep % DestinyTheme.SHINE_FRAMES.length];
        String animatedFooter = DestinyTheme.FOOTER_FRAMES[(animStep / 2) % DestinyTheme.FOOTER_FRAMES.length];

        String header = "\n" +
                animatedTitle + "\n" +
                ChatColor.DARK_GRAY + "§m                             \n" +
                getMatchStatus() + "\n";

        int playingCount = Bukkit.getOnlinePlayers().size();
        int maxPlayers = Bukkit.getMaxPlayers();
        int stabilizedPing = NetworkFairnessManager.getInstance().getStabilizedPing(player);

        String footer = "\n" +
                ChatColor.GRAY + TextUtil.toTiny("Jugadores: ") + ChatColor.AQUA + TextUtil.toTiny(String.valueOf(playingCount)) +
                ChatColor.DARK_GRAY + "/" + ChatColor.GRAY + TextUtil.toTiny(String.valueOf(maxPlayers)) +
                ChatColor.DARK_GRAY + "  ▪  " + ChatColor.GRAY + TextUtil.toTiny("Ping: ") + getPingDisplay(stabilizedPing) + "\n" +
                ChatColor.DARK_GRAY + "§m                             \n" +
                animatedFooter + "\n";

        String prevHeader = lastHeaders.get(player.getUniqueId());
        String prevFooter = lastFooters.get(player.getUniqueId());

        if (!header.equals(prevHeader) || !footer.equals(prevFooter)) {
            lastHeaders.put(player.getUniqueId(), header);
            lastFooters.put(player.getUniqueId(), footer);
            player.setPlayerListHeaderFooter(header, footer);
        }
    }

    private void updatePlayerSorting(Player player) {
        Scoreboard board = player.getScoreboard();
        if (board == null) return;

        for (Player target : Bukkit.getOnlinePlayers()) {
            syncPlayerTeamInScoreboard(board, target);
        }
    }

    private void syncPlayerTeamInScoreboard(Scoreboard board, Player target) {
        TTRTeam team = plugin.getTeamHandler() != null ? plugin.getTeamHandler().getPlayerTeam(target) : null;
        boolean isStaff = TTRCore.isAdmin(target);

        String teamKey;
        String rolePrefix;
        NamedTextColor teamColor;

        if (isStaff) {
            teamKey = "00_destiny";
            rolePrefix = DestinyTheme.DESTINY_ROLE_BADGE + " ";
            teamColor = NamedTextColor.WHITE;
        } else if (team != null) {
            boolean isLeader = team.isLeader(target.getUniqueId());
            boolean isRed = team.getIdentifier().equalsIgnoreCase("Red");
            if (isRed) {
                teamKey = isLeader ? "10_red_l" : "11_red";
                rolePrefix = isLeader ? "§6★ §c[" + TextUtil.toTiny("Rojo") + "] " : "§c[" + TextUtil.toTiny("Rojo") + "] ";
                teamColor = NamedTextColor.RED;
            } else {
                teamKey = isLeader ? "20_blue_l" : "21_blue";
                rolePrefix = isLeader ? "§6★ §9[" + TextUtil.toTiny("Azul") + "] " : "§9[" + TextUtil.toTiny("Azul") + "] ";
                teamColor = NamedTextColor.BLUE;
            }
        } else {
            MatchStatus status = plugin.getCurrentMatch() != null ? plugin.getCurrentMatch().getStatus() : MatchStatus.STOPPED;
            if (status == MatchStatus.INGAME) {
                teamKey = "95_spec";
                rolePrefix = "§7[" + TextUtil.toTiny("Espec") + "] ";
                teamColor = NamedTextColor.GRAY;
            } else {
                teamKey = "90_lobby";
                rolePrefix = "§7[" + TextUtil.toTiny("Lobby") + "] ";
                teamColor = NamedTextColor.GRAY;
            }
        }

        try {
            Team tabTeam = board.getTeam(teamKey);
            if (tabTeam == null) {
                tabTeam = board.registerNewTeam(teamKey);
                tabTeam.color(teamColor);
                tabTeam.prefix(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(rolePrefix));
            }

            // Clave única (tablero + jugador) para evitar recalcular si no cambió
            String cacheKey = System.identityHashCode(board) + "_" + target.getName();
            String currentTeam = lastAssignedTeam.get(cacheKey);

            if (teamKey.equals(currentTeam) && tabTeam.hasEntry(target.getName())) {
                return; // ¡YA ESTÁ CORRECTAMENTE ASIGNADO! CERO PAQUETES DE RED ENVIADOS
            }

            // Si cambió de equipo, desvincular de equipos anteriores
            for (Team t : board.getTeams()) {
                if (t.getName().matches("\\d{2}_.*") && t.hasEntry(target.getName())) {
                    t.removeEntry(target.getName());
                }
            }
            tabTeam.addEntry(target.getName());
            lastAssignedTeam.put(cacheKey, teamKey);
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
        if (ping < 80) return ChatColor.GREEN + TextUtil.toTiny(ping + "ms") + ChatColor.DARK_GRAY + " ▂▃▅";
        if (ping < 160) return ChatColor.YELLOW + TextUtil.toTiny(ping + "ms") + ChatColor.DARK_GRAY + " ▂▃";
        return ChatColor.GOLD + TextUtil.toTiny(ping + "ms") + ChatColor.DARK_GRAY + " ▂";
    }
}
