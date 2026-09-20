package me.PauMAVA.TTR.ui;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardHandler {

    private final TTRCore plugin;
    private int taskID = -1;
    // Map de líneas previas por jugador para actualizar sin parpadeo (flicker-free)
    private final Map<UUID, Scoreboard> playerBoards = new ConcurrentHashMap<>();
    private final Map<UUID, List<String>> playerLastLines = new ConcurrentHashMap<>();

    private int animStep = 0;

    public ScoreboardHandler(TTRCore plugin) {
        this.plugin = plugin;
    }

    public void startScoreboardTask() {
        stopScoreboardTask();
        this.taskID = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    animStep++;
                    updateAll();
                } catch (Throwable t) {
                    // Prevenir que el task muera ante cualquier excepción
                }
            }
        }.runTaskTimer(plugin, 0L, 5L).getTaskId();
    }

    public void stopScoreboardTask() {
        if (taskID != -1) {
            Bukkit.getScheduler().cancelTask(taskID);
            taskID = -1;
        }
    }

    public void updateAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            updateScoreboard(p);
        }
    }

    public void update(Player p) {
        updateScoreboard(p);
    }

    public void refreshScoreboard() {
        updateAll();
    }

    public void removePlayer(Player p) {
        playerBoards.remove(p.getUniqueId());
        playerLastLines.remove(p.getUniqueId());
    }

    private void updateScoreboard(Player player) {
        try {
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            if (manager == null || !player.isOnline()) return;

            Scoreboard board = playerBoards.computeIfAbsent(player.getUniqueId(), uuid -> manager.getNewScoreboard());
            if (player.getScoreboard() != board) {
                player.setScoreboard(board);
            }

            Objective obj = board.getObjective("TTR");
            String animatedTitle = DestinyTheme.SHINE_FRAMES[animStep % DestinyTheme.SHINE_FRAMES.length];
            if (obj == null) {
                obj = board.registerNewObjective("TTR", Criteria.DUMMY, net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(animatedTitle));
                obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            } else {
                obj.displayName(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(animatedTitle));
                if (obj.getDisplaySlot() != DisplaySlot.SIDEBAR) {
                    obj.setDisplaySlot(DisplaySlot.SIDEBAR);
                }
            }
            try {
                obj.numberFormat(io.papermc.paper.scoreboard.numbers.NumberFormat.blank());
            } catch (Throwable ignored) {}

            List<String> newLines = new ArrayList<>();
            newLines.add(ChatColor.DARK_GRAY + "§m                      ");

            MatchStatus status = (plugin.getCurrentMatch() != null) ? plugin.getCurrentMatch().getStatus() : MatchStatus.STOPPED;

            if (status == MatchStatus.INGAME) {
                String time = plugin.getCurrentMatch().getFormattedTime();
                newLines.add(ChatColor.GRAY + "» " + ChatColor.WHITE + TextUtil.toTiny("Tiempo: ") + ChatColor.GREEN + TextUtil.toTiny(time));

                if (plugin.getEventManager() != null && plugin.getEventManager().getCurrentEvent() != null) {
                    String evName = TextUtil.toTiny(capitalize(plugin.getEventManager().getCurrentEvent()));
                    newLines.add(ChatColor.GRAY + "» " + ChatColor.WHITE + TextUtil.toTiny("Evento: ") + ChatColor.GOLD + evName);
                }

                newLines.add("§1§r");

                // Puntos de los equipos
                newLines.add(ChatColor.WHITE + "" + ChatColor.BOLD + TextUtil.toTiny("Puntos:"));
                if (plugin.getTeamHandler() != null) {
                    for (TTRTeam team : plugin.getTeamHandler().getTeams()) {
                        String teamName = TextUtil.toTiny(capitalize(team.getIdentifier()));
                        int points = team.getPoints();
                        int max = (plugin.getCurrentMatch() != null) ? plugin.getCurrentMatch().getMaxPointsToWin() : 10;
                        newLines.add(team.getColor() + " ▪ " + ChatColor.WHITE + teamName + ChatColor.GRAY + ": " + ChatColor.YELLOW + TextUtil.toTiny(String.valueOf(points)) + ChatColor.DARK_GRAY + "/" + ChatColor.GRAY + TextUtil.toTiny(String.valueOf(max)));
                    }
                }

                newLines.add("§2§r");

                // Equipo del jugador
                TTRTeam playerTeam = plugin.getTeamHandler().getPlayerTeam(player);
                String teamName = (playerTeam != null) ? 
                        playerTeam.getColor() + TextUtil.toTiny(capitalize(playerTeam.getIdentifier())) : 
                        ChatColor.GRAY + TextUtil.toTiny("Espectador");
                newLines.add(ChatColor.GRAY + "» " + ChatColor.WHITE + TextUtil.toTiny("Tu Equipo: ") + teamName);

                // Kills
                int kills = plugin.getCurrentMatch().getKills(player);
                newLines.add(ChatColor.GRAY + "» " + ChatColor.WHITE + TextUtil.toTiny("Asesinatos: ") + ChatColor.GREEN + TextUtil.toTiny(String.valueOf(kills)));

            } else {
                newLines.add(ChatColor.GRAY + "» " + ChatColor.WHITE + TextUtil.toTiny("Estado: ") + getMatchState(status));

                int playingCount = Bukkit.getOnlinePlayers().size();
                int maxPlayers = Bukkit.getMaxPlayers();
                newLines.add(ChatColor.GRAY + "» " + ChatColor.WHITE + TextUtil.toTiny("Jugadores: ") + ChatColor.AQUA + TextUtil.toTiny(String.valueOf(playingCount)) + ChatColor.DARK_GRAY + "/" + ChatColor.GRAY + TextUtil.toTiny(String.valueOf(maxPlayers)));

                if (status == MatchStatus.PREPARATION) {
                    int prepRem = plugin.getCurrentMatch() != null ? plugin.getCurrentMatch().getPrepRemaining() : 0;
                    newLines.add(ChatColor.GRAY + "» " + ChatColor.WHITE + TextUtil.toTiny("Combate en: ") + ChatColor.YELLOW + TextUtil.toTiny(prepRem + "s"));
                } else if (plugin.getAutoStarter() != null && plugin.isCounting()) {
                    int cd = plugin.getAutoStarter().getCountdown();
                    newLines.add(ChatColor.GRAY + "» " + ChatColor.WHITE + TextUtil.toTiny("Iniciando: ") + ChatColor.YELLOW + TextUtil.toTiny(cd + "s"));
                }

                newLines.add("§1§r");

                TTRTeam playerTeam = plugin.getTeamHandler() != null ? plugin.getTeamHandler().getPlayerTeam(player) : null;
                String teamName;
                if (playerTeam != null) {
                    teamName = playerTeam.getColor() + TextUtil.toTiny(capitalize(playerTeam.getIdentifier()));
                } else if (TTRCore.isAdmin(player)) {
                    teamName = DestinyTheme.DESTINY_ROLE_BADGE;
                } else {
                    teamName = ChatColor.GRAY + TextUtil.toTiny("Lobby");
                }
                newLines.add(ChatColor.GRAY + "» " + ChatColor.WHITE + (playerTeam != null ? TextUtil.toTiny("Tu Equipo: ") : TextUtil.toTiny("Tu Rol: ")) + teamName);
            }

            // Footer DestinyOwners
            newLines.add(ChatColor.DARK_GRAY + "§m                      §r");
            String animatedFooter = DestinyTheme.FOOTER_FRAMES[(animStep / 2) % DestinyTheme.FOOTER_FRAMES.length];
            newLines.add(animatedFooter);

            // Comparar con líneas anteriores para evitar parpadeo
            List<String> lastLines = playerLastLines.getOrDefault(player.getUniqueId(), Collections.emptyList());
            if (!newLines.equals(lastLines)) {
                // Limpiar líneas anteriores que ya no existan
                for (String oldLine : lastLines) {
                    if (!newLines.contains(oldLine)) {
                        board.resetScores(oldLine);
                    }
                }

                // Aplicar nuevas puntuaciones de arriba a abajo
                int score = newLines.size();
                for (String lineText : newLines) {
                    Score s = obj.getScore(lineText);
                    s.setScore(score--);
                    try {
                        s.numberFormat(io.papermc.paper.scoreboard.numbers.NumberFormat.blank());
                    } catch (Throwable ignored) {}
                }

                playerLastLines.put(player.getUniqueId(), newLines);
            }
        } catch (Throwable t) {
            // Prevenir cualquier caída en la actualización
        }
    }

    private String getMatchState(MatchStatus status) {
        switch (status) {
            case LOBBY: return ChatColor.YELLOW + TextUtil.toTiny("Esperando...");
            case STARTING: return ChatColor.GOLD + TextUtil.toTiny("Iniciando...");
            case PREPARATION: return ChatColor.AQUA + TextUtil.toTiny("Preparación en Bases");
            case INGAME: return ChatColor.GREEN + TextUtil.toTiny("En Curso");
            case ENDED: return ChatColor.RED + TextUtil.toTiny("Terminado");
            default: return ChatColor.GRAY + TextUtil.toTiny("En Espera");
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
