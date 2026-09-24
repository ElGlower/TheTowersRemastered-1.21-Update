package me.PauMAVA.TTR.web;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.teams.TTRTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Gestor de Telemetría y Estadísticas Web en Tiempo Real para DESTINYOWNERS.
 * Registra partidas, eventos de gol, bajas y estado de jugadores en formato JSON
 * asíncrono sin bloquear el hilo principal del servidor.
 */
public class WebStatsManager {

    private static WebStatsManager instance;
    private final TTRCore plugin;

    private final List<Map<String, Object>> killfeed = new CopyOnWriteArrayList<>();
    private final List<Map<String, Object>> matchHistory = new CopyOnWriteArrayList<>();
    private final Map<UUID, Map<String, Object>> playerStats = new ConcurrentHashMap<>();

    private int annualMatchCount = 142; // Contador base escalable a 100+ partidas anuales

    public WebStatsManager(TTRCore plugin) {
        this.plugin = plugin;
    }

    public static WebStatsManager getInstance() {
        if (instance == null) {
            instance = new WebStatsManager(TTRCore.getInstance());
        }
        return instance;
    }

    public void recordKill(Player killer, Player victim, String cause, double distance) {
        CompletableFuture.runAsync(() -> {
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("timestamp", Instant.now().toString());
            event.put("killer", killer != null ? killer.getName() : "Vacío/Entorno");
            event.put("victim", victim.getName());
            event.put("cause", cause);
            event.put("distance", Math.round(distance * 10.0) / 10.0);

            killfeed.add(0, event);
            if (killfeed.size() > 50) {
                killfeed.remove(killfeed.size() - 1);
            }

            // Actualizar estadísticas individuales
            if (killer != null) {
                Map<String, Object> kStats = playerStats.computeIfAbsent(killer.getUniqueId(), k -> createDefaultPlayerStats(killer.getName()));
                kStats.put("kills", ((int) kStats.getOrDefault("kills", 0)) + 1);
            }
            Map<String, Object> vStats = playerStats.computeIfAbsent(victim.getUniqueId(), k -> createDefaultPlayerStats(victim.getName()));
            vStats.put("deaths", ((int) vStats.getOrDefault("deaths", 0)) + 1);

            saveLiveJson();
        });
    }

    public void recordGoal(Player scorer, TTRTeam team, int currentTeamScore, int maxScore) {
        CompletableFuture.runAsync(() -> {
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("type", "GOAL");
            event.put("timestamp", Instant.now().toString());
            event.put("scorer", scorer.getName());
            event.put("team", team.getIdentifier());
            event.put("score", currentTeamScore + "/" + maxScore);

            killfeed.add(0, event);
            if (killfeed.size() > 50) {
                killfeed.remove(killfeed.size() - 1);
            }

            Map<String, Object> sStats = playerStats.computeIfAbsent(scorer.getUniqueId(), k -> createDefaultPlayerStats(scorer.getName()));
            sStats.put("goals", ((int) sStats.getOrDefault("goals", 0)) + 1);

            saveLiveJson();
        });
    }

    public void recordMatchEnd(TTRTeam winner, int durationSecs) {
        CompletableFuture.runAsync(() -> {
            annualMatchCount++;
            Map<String, Object> matchRecord = new LinkedHashMap<>();
            matchRecord.put("matchNumber", annualMatchCount);
            matchRecord.put("timestamp", Instant.now().toString());
            matchRecord.put("winner", winner != null ? winner.getIdentifier() : "Empate");
            matchRecord.put("durationSeconds", durationSecs);
            matchRecord.put("redPoints", plugin.getTeamHandler().getTeam("Red") != null ? plugin.getTeamHandler().getTeam("Red").getPoints() : 0);
            matchRecord.put("bluePoints", plugin.getTeamHandler().getTeam("Blue") != null ? plugin.getTeamHandler().getTeam("Blue").getPoints() : 0);

            matchHistory.add(0, matchRecord);
            saveLiveJson();
        });
    }

    public Map<String, Object> getCurrentLiveState() {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("platform", "DESTINYOWNERS");
        state.put("gameMode", "THE TOWERS");
        state.put("version", "Paper 26.3 / Java 25");
        state.put("status", plugin.getCurrentMatch() != null ? plugin.getCurrentMatch().getStatus().name() : MatchStatus.STOPPED.name());

        if (plugin.getCurrentMatch() != null) {
            state.put("formattedTime", plugin.getCurrentMatch().getFormattedTime());
        }

        // Marcador
        Map<String, Object> scoreMap = new LinkedHashMap<>();
        TTRTeam red = plugin.getTeamHandler() != null ? plugin.getTeamHandler().getTeam("Red") : null;
        TTRTeam blue = plugin.getTeamHandler() != null ? plugin.getTeamHandler().getTeam("Blue") : null;
        scoreMap.put("red", red != null ? red.getPoints() : 0);
        scoreMap.put("blue", blue != null ? blue.getPoints() : 0);
        state.put("score", scoreMap);

        // Jugadores activos
        List<Map<String, Object>> players = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            Map<String, Object> pStats = playerStats.computeIfAbsent(p.getUniqueId(), k -> createDefaultPlayerStats(p.getName()));
            pStats.put("name", p.getName());

            Map<String, Object> pData = new LinkedHashMap<>();
            pData.put("name", p.getName());
            pData.put("uuid", p.getUniqueId().toString());
            pData.put("health", Math.round(p.getHealth() * 10.0) / 10.0);
            pData.put("ping", p.getPing());
            TTRTeam t = plugin.getTeamHandler() != null ? plugin.getTeamHandler().getPlayerTeam(p) : null;
            pData.put("team", t != null ? t.getIdentifier() : "LOBBY");
            pData.put("isLeader", t != null && t.isLeader(p.getUniqueId()));
            players.add(pData);
        }
        state.put("players", players);
        state.put("killfeed", new ArrayList<>(killfeed));
        state.put("matches", new ArrayList<>(matchHistory));
        state.put("annualMatches", annualMatchCount);
        state.put("leaderboard", getLeaderboardMap());

        return state;
    }

    private final java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(3))
            .build();

    public void startSyncTask() {
        loadPersistedStats();
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::saveLiveJson, 40L, 60L);
    }

    public void loadPersistedStats() {
        CompletableFuture.runAsync(() -> {
            try {
                File worldContainer = Bukkit.getWorldContainer();
                File[] candidates = new File[] {
                    new File(worldContainer, "the-towers/stats"),
                    new File(worldContainer, "the-towers/players/stats"),
                    new File("the-towers/stats"),
                    new File("the-towers/players/stats"),
                    new File(worldContainer, "world/stats"),
                    new File("world/stats")
                };
                File statsDir = null;
                for (File c : candidates) {
                    if (c.exists() && c.isDirectory()) {
                        statsDir = c;
                        break;
                    }
                }
                if (statsDir == null) return;

                File usercacheFile = new File(worldContainer, "usercache.json");
                if (!usercacheFile.exists()) {
                    usercacheFile = new File("usercache.json");
                }

                Map<String, String> uuidToName = new HashMap<>();
                if (usercacheFile.exists()) {
                    try {
                        String cacheContent = java.nio.file.Files.readString(usercacheFile.toPath(), StandardCharsets.UTF_8);
                        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"uuid\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"name\"\\s*:\\s*\"([^\"]+)\"");
                        java.util.regex.Matcher m = p.matcher(cacheContent);
                        while (m.find()) {
                            uuidToName.put(m.group(1).toLowerCase(), m.group(2));
                        }
                    } catch (Exception ignored) {}
                }

                File[] files = statsDir.listFiles((dir, name) -> name.endsWith(".json"));
                if (files == null) return;

                for (File f : files) {
                    try {
                        String fileName = f.getName();
                        String uuidStr = fileName.substring(0, fileName.length() - 5);
                        UUID uuid = UUID.fromString(uuidStr);

                        String content = java.nio.file.Files.readString(f.toPath(), StandardCharsets.UTF_8);
                        int kills = extractJsonInt(content, "minecraft:player_kills");
                        int deaths = extractJsonInt(content, "minecraft:deaths");

                        String name = uuidToName.get(uuidStr.toLowerCase());
                        if (name == null) {
                            try {
                                name = Bukkit.getOfflinePlayer(uuid).getName();
                            } catch (Exception ignored) {}
                        }
                        if (name == null || name.isBlank() || name.startsWith("Unknown_")) continue;

                        final String playerName = name;
                        Map<String, Object> stats = playerStats.computeIfAbsent(uuid, k -> createDefaultPlayerStats(playerName));
                        stats.put("name", playerName);
                        stats.put("kills", kills);
                        stats.put("deaths", deaths);
                    } catch (Exception ignored) {}
                }

                saveLiveJson();
            } catch (Exception ignored) {}
        });
    }

    private int extractJsonInt(String json, String key) {
        try {
            int idx = json.indexOf("\"" + key + "\"");
            if (idx == -1) return 0;
            int colon = json.indexOf(":", idx);
            if (colon == -1) return 0;
            int end = colon + 1;
            while (end < json.length() && Character.isWhitespace(json.charAt(end))) {
                end++;
            }
            int numStart = end;
            while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
                end++;
            }
            if (numStart < end) {
                return Integer.parseInt(json.substring(numStart, end));
            }
        } catch (Exception ignored) {}
        return 0;
    }

    private void syncToFirebase(String path, String json) {
        try {
            java.net.URI uri = java.net.URI.create("https://destinyowners-23-default-rtdb.firebaseio.com/" + path + ".json");
            String method = path.equals("leaderboard") ? "PATCH" : "PUT";
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .method(method, java.net.http.HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .timeout(java.time.Duration.ofSeconds(5))
                    .build();
            httpClient.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.discarding());
        } catch (Throwable ignored) {}
    }

    public Map<String, Object> getLeaderboardMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<UUID, Map<String, Object>> entry : playerStats.entrySet()) {
            map.put(entry.getKey().toString(), entry.getValue());
        }
        return map;
    }

    private void saveLiveJson() {
        try {
            Map<String, Object> live = getCurrentLiveState();
            String liveJson = toJsonString(live);

            // 1. Guardar archivo local
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) dataFolder.mkdirs();
            File jsonFile = new File(dataFolder, "web_live_stats.json");
            try (FileWriter writer = new FileWriter(jsonFile, StandardCharsets.UTF_8)) {
                writer.write(liveJson);
            }

            // 2. Sincronización en tiempo real con Firebase RTDB
            syncToFirebase("live", liveJson);
            if (!playerStats.isEmpty()) {
                syncToFirebase("leaderboard", toJsonString(getLeaderboardMap()));
            }
        } catch (Exception ignored) {}
    }

    private String toJsonString(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(entry.getKey()).append("\":");
            sb.append(objectToJson(entry.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }

    private String objectToJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return "\"" + obj.toString().replace("\"", "\\\"") + "\"";
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
        if (obj instanceof Map) return toJsonString((Map<String, Object>) obj);
        if (obj instanceof List<?> list) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : list) {
                if (!first) sb.append(",");
                first = false;
                sb.append(objectToJson(item));
            }
            sb.append("]");
            return sb.toString();
        }
        return "\"" + obj.toString() + "\"";
    }

    public Map<String, Object> getPlayerStats(UUID uuid) {
        return playerStats.get(uuid);
    }

    public Map<String, Object> getPlayerStatsByName(String name) {
        for (Map<String, Object> s : playerStats.values()) {
            if (name.equalsIgnoreCase((String) s.get("name"))) {
                return s;
            }
        }
        return null;
    }

    private Map<String, Object> createDefaultPlayerStats(String name) {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("name", name);
        stats.put("kills", 0);
        stats.put("deaths", 0);
        stats.put("goals", 0);
        stats.put("wins", 0);
        return stats;
    }
}
