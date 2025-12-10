package me.PauMAVA.TTR.match;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.teams.TTRTeam;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class TTRMatch {

    private MatchStatus status;
    private LootSpawner lootSpawner;
    private CageChecker checker;
    private HashMap<Player, Integer> kills = new HashMap<>();
    private BossBar gameBar;
    private int regenTaskID;
    private int matchTaskID;
    private int remainingTime;
    private int maxPointsToWin;

    public TTRMatch(MatchStatus initialStatus) {
        status = initialStatus;
    }

    public boolean isOnCourse() {
        return this.status == MatchStatus.INGAME;
    }

    public void startMatch() {
        TTRCore.getInstance().getTeamHandler().loadSpawnsFromConfig();
        TTRCore.getInstance().getTeamHandler().restartTeams();

        TTRTeam red = TTRCore.getInstance().getTeamHandler().getTeam("Red");
        TTRTeam blue = TTRCore.getInstance().getTeamHandler().getTeam("Blue");
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (TTRCore.getInstance().getTeamHandler().getPlayerTeam(p) == null) {
                if (red.getPlayers().size() <= blue.getPlayers().size()) {
                    TTRCore.getInstance().getTeamHandler().addPlayerToTeam(p, "Red");
                } else {
                    TTRCore.getInstance().getTeamHandler().addPlayerToTeam(p, "Blue");
                }
            }
        }

        this.status = MatchStatus.INGAME;
        this.lootSpawner = new LootSpawner();
        this.checker = new CageChecker();

        if (TTRCore.getInstance().getEventManager() != null) {
            TTRCore.getInstance().getEventManager().startCycle();
        }

        this.remainingTime = TTRCore.getInstance().getConfig().getInt("match.duration", 1200);
        this.maxPointsToWin = TTRCore.getInstance().getConfig().getInt("match.maxpoints", 10);

        if (this.gameBar != null) this.gameBar.removeAll();
        this.gameBar = Bukkit.createBossBar(ChatColor.LIGHT_PURPLE + "THE TOWERS", BarColor.PURPLE, BarStyle.SOLID);

        HashMap<Location, TTRTeam> cageMap = new HashMap<>();
        Set<String> teamNames = TTRCore.getInstance().getConfigManager().getTeamNames();
        if (teamNames != null) {
            for (String teamName : teamNames) {
                TTRTeam team = TTRCore.getInstance().getTeamHandler().getTeam(teamName);
                List<Location> locs = TTRCore.getInstance().getConfigManager().getTeamCages(teamName);
                if (locs != null && team != null) {
                    for (Location loc : locs) cageMap.put(loc, team);
                }
            }
        }

        if (!cageMap.isEmpty()) {
            this.checker.setCages(cageMap, 2);
            this.checker.startChecking();
        }

        this.lootSpawner.startSpawning();

        if (TTRCore.getInstance().getWorldHandler() != null) {
            TTRCore.getInstance().getWorldHandler().configureTime();
            TTRCore.getInstance().getWorldHandler().configureWeather();
        }

        TTRCore.getInstance().getScoreboard().startScoreboardTask();

        startRegenTask();
        startMatchTimer();
        updateBossBar();

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            joinPlayerToMatch(player);
        }
    }

    public void joinPlayerToMatch(Player player) {
        TTRTeam playerTeam = TTRCore.getInstance().getTeamHandler().getPlayerTeam(player);
        if (playerTeam == null) {
            player.setGameMode(GameMode.SPECTATOR);
            return;
        }

        this.gameBar.addPlayer(player);

        Location teamSpawn = playerTeam.getSpawnPoint();
        if (teamSpawn == null) {
            teamSpawn = TTRCore.getInstance().getConfigManager().getTeamSpawn(playerTeam.getIdentifier());
        }

        if (teamSpawn != null) {
            player.teleport(teamSpawn);
        } else {
            player.sendMessage(ChatColor.RED + "¡ERROR! No hay spawn definido.");
        }

        player.getInventory().clear();
        player.setGameMode(GameMode.SURVIVAL);

        if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
        }
        player.setHealth(20);
        player.setFoodLevel(20);

        equipPlayer(player, playerTeam.getIdentifier());
        this.kills.putIfAbsent(player, 0);

        player.sendTitle(ChatColor.GOLD + "THE TOWERS", ChatColor.YELLOW + "¡A luchar!", 10, 60, 20);
    }

    public void equipPlayer(Player player, String teamIdentifier) {
        TTRTeam team = TTRCore.getInstance().getTeamHandler().getTeam(teamIdentifier);
        ChatColor chatColor = TTRCore.getInstance().getConfigManager().getTeamColor(teamIdentifier);
        Color armorColor = (chatColor == ChatColor.RED) ? Color.RED : Color.BLUE;
        Material glassMaterial = Material.WHITE_STAINED_GLASS;
        if (chatColor == ChatColor.RED) glassMaterial = Material.RED_STAINED_GLASS;
        else if (chatColor == ChatColor.BLUE) glassMaterial = Material.BLUE_STAINED_GLASS;

        ItemStack[] armor = new ItemStack[]{
                new ItemStack(Material.LEATHER_BOOTS), new ItemStack(Material.LEATHER_LEGGINGS),
                new ItemStack(Material.LEATHER_CHESTPLATE), new ItemStack(Material.LEATHER_HELMET)
        };

        int protLevel = (team != null) ? team.getArmorProtectionLevel() : 0;

        for (ItemStack item : armor) {
            LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
            if (meta != null) {
                meta.setColor(armorColor);
                meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
                meta.setUnbreakable(true);
                if (protLevel > 0) meta.addEnchant(Enchantment.PROTECTION, protLevel, true);
                item.setItemMeta(meta);
            }
        }
        player.getInventory().setArmorContents(armor);
        player.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
        player.getInventory().addItem(new ItemStack(glassMaterial, 32));
        player.getInventory().addItem(new ItemStack(Material.BREAD, 16));

        if (team != null) {
            if (team.hasTeamSpeed()) player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
            if (team.hasTeamHaste()) player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, 0, false, false));
        }
    }

    private void startMatchTimer() {
        this.matchTaskID = new BukkitRunnable() {
            @Override
            public void run() {
                if (status != MatchStatus.INGAME) { this.cancel(); return; }
                remainingTime--;
                if (remainingTime <= 0) {
                    checkWinnerAndEnd();
                    this.cancel();
                }
            }
        }.runTaskTimer(TTRCore.getInstance(), 0L, 20L).getTaskId();
    }

    private void checkWinnerAndEnd() {
        TTRTeam winner = null;
        int maxP = -1;
        boolean draw = false;
        Set<String> teamNames = TTRCore.getInstance().getConfigManager().getTeamNames();
        if (teamNames != null) {
            for (String teamName : teamNames) {
                TTRTeam t = TTRCore.getInstance().getTeamHandler().getTeam(teamName);
                if (t.getPoints() > maxP) {
                    maxP = t.getPoints();
                    winner = t;
                    draw = false;
                } else if (t.getPoints() == maxP) draw = true;
            }
        }
        if (draw) endMatch(null); else endMatch(winner);
    }

    private void startRegenTask() {
        this.regenTaskID = new BukkitRunnable() {
            @Override
            public void run() {
                if (status != MatchStatus.INGAME) { this.cancel(); return; }
                for (Player p : Bukkit.getOnlinePlayers()) {
                    TTRTeam team = TTRCore.getInstance().getTeamHandler().getPlayerTeam(p);
                    if (team == null) continue;
                    Location spawn = TTRCore.getInstance().getConfigManager().getTeamSpawn(team.getIdentifier());
                    if (spawn != null && p.getWorld().equals(spawn.getWorld())) {
                        if (p.getLocation().distance(spawn) < 8) {
                            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 2, true, false));
                        }
                    }
                }
            }
        }.runTaskTimer(TTRCore.getInstance(), 0L, 20L).getTaskId();
    }

    public void updateBossBar() {
        if (this.gameBar == null) return;
        StringBuilder sb = new StringBuilder();
        int highestPoints = 0;
        Set<String> teamNames = TTRCore.getInstance().getConfigManager().getTeamNames();
        if (teamNames != null) {
            for (String teamName : teamNames) {
                TTRTeam team = TTRCore.getInstance().getTeamHandler().getTeam(teamName);
                ChatColor color = TTRCore.getInstance().getConfigManager().getTeamColor(teamName);
                sb.append(color).append(ChatColor.BOLD).append(teamName)
                        .append(": ").append(ChatColor.WHITE).append(team.getPoints()).append("   ");
                if (team.getPoints() > highestPoints) highestPoints = team.getPoints();
            }
        }
        this.gameBar.setTitle(sb.toString());
        double progress = (double) highestPoints / maxPointsToWin;
        if (progress > 1.0) progress = 1.0;
        this.gameBar.setProgress(progress);
    }

    public void cleanup() {
        if (this.lootSpawner != null) this.lootSpawner.stopSpawning();
        if (this.checker != null) this.checker.stopChecking();
        Bukkit.getScheduler().cancelTask(this.regenTaskID);
        Bukkit.getScheduler().cancelTask(this.matchTaskID);
        TTRCore.getInstance().getScoreboard().stopScoreboardTask();
        if (this.gameBar != null) this.gameBar.removeAll();
    }

    public void endMatch(TTRTeam team) {
        this.status = MatchStatus.ENDED;
        cleanup();
        TTRCore.getInstance().getTeamHandler().clearTeams();

        ChatColor teamColor = (team != null) ? TTRCore.getInstance().getConfigManager().getTeamColor(team.getIdentifier()) : ChatColor.WHITE;
        String teamName = (team != null) ? team.getIdentifier() : "Empate";

        List<Map.Entry<Player, Integer>> topKillers = kills.entrySet().stream()
                .sorted(Map.Entry.<Player, Integer>comparingByValue().reversed())
                .limit(3)
                .toList();

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            player.sendTitle(teamColor + "GANADOR: " + teamName, ChatColor.AQUA + "GG!", 10, 100, 20);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 10, 1);
            player.sendMessage(" ");
            player.sendMessage(centerText(ChatColor.GOLD + "" + ChatColor.BOLD + "FIN DE LA PARTIDA"));
            player.sendMessage(centerText(ChatColor.GRAY + "Ganador: " + teamColor + teamName));
            player.sendMessage(" ");
            player.sendMessage(centerText(ChatColor.AQUA + "--- TOP ASESINOS ---"));
            if (topKillers.isEmpty()) {
                player.sendMessage(centerText(ChatColor.GRAY + "Nadie murió..."));
            } else {
                int i = 1;
                for (Map.Entry<Player, Integer> entry : topKillers) {
                    player.sendMessage(centerText(ChatColor.YELLOW + "#" + i + " " + ChatColor.WHITE + entry.getKey().getName() + ": " + ChatColor.RED + entry.getValue()));
                    i++;
                }
            }
            player.sendMessage(" ");
            player.sendMessage(centerText(ChatColor.YELLOW + "Reiniciando en 5 segundos..."));

            player.getInventory().clear();
            player.setGameMode(GameMode.ADVENTURE);

            // RESETEAR ATRIBUTOS ANTES DE CURAR
            if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
            }
            player.setHealth(20);
            player.setFoodLevel(20);

            giveLobbyItems(player);

            Location lobby = TTRCore.getInstance().getConfigManager().getLobbyLocation();
            if (lobby != null) player.teleport(lobby);
        }

        if (TTRCore.getInstance().getWorldHandler() != null) {
            TTRCore.getInstance().getWorldHandler().restoreDifficulty();
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                TTRCore.getInstance().resetMatchLogic();
            }
        }.runTaskLater(TTRCore.getInstance(), 100L);
    }

    private String centerText(String text) {
        int maxWidth = 60;
        int spaces = (maxWidth - ChatColor.stripColor(text).length()) / 2;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < spaces; i++) builder.append(" ");
        builder.append(text);
        return builder.toString();
    }

    public void giveLobbyItems(Player p) {
        ItemStack star = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = star.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + "Selector de Equipos " + ChatColor.GRAY + "(Click Derecho)");
            star.setItemMeta(meta);
        }
        p.getInventory().setItem(4, star);
    }

    public void playerDeath(Player player, Player killer) {
        if (killer != null) kills.put(killer, getKills(killer) + 1);
    }

    public MatchStatus getStatus() { return this.status; }
    public int getKills(Player player) { return this.kills.getOrDefault(player, 0); }
    public BossBar getBossBar() { return this.gameBar; }
    public String getFormattedTime() {
        int minutes = remainingTime / 60;
        int seconds = remainingTime % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    public int getMaxPointsToWin() { return this.maxPointsToWin; }
    public void setRemainingTime(int seconds) { this.remainingTime = seconds; }
    public void setMaxPointsToWin(int points) { this.maxPointsToWin = points; updateBossBar(); }
}