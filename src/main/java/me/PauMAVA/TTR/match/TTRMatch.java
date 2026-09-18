package me.PauMAVA.TTR.match;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.util.TextUtil;
import me.PauMAVA.TTR.util.TTRPrefix;
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
    private final HashMap<Player, Integer> kills = new HashMap<>();
    private BossBar gameBar;
    private int regenTaskID;
    private int matchTaskID;
    private int prepTaskID = -1;
    private int prepRemaining = 0;
    private int remainingTime;
    private int maxPointsToWin;

    public TTRMatch(MatchStatus initialStatus) {
        status = initialStatus;
    }

    public boolean isOnCourse() {
        return this.status == MatchStatus.INGAME;
    }

    public boolean isPreparing() {
        return this.status == MatchStatus.PREPARATION;
    }

    public void startPreparationPhase(int seconds) {
        this.status = MatchStatus.PREPARATION;
        this.prepRemaining = seconds;

        TTRCore.getInstance().getTeamHandler().loadSpawnsFromConfig();
        ChestRestockManager.getInstance().restockAndPurgeArenaChests(true);

        TTRTeam red = TTRCore.getInstance().getTeamHandler().getTeam("Red");
        TTRTeam blue = TTRCore.getInstance().getTeamHandler().getTeam("Blue");
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (TTRCore.isAdmin(p)) continue;
            if (TTRCore.getInstance().getTeamHandler().getPlayerTeam(p) == null) {
                if (red != null && blue != null) {
                    if (red.getPlayers().size() <= blue.getPlayers().size()) {
                        TTRCore.getInstance().getTeamHandler().addPlayerToTeam(p, "Red");
                    } else {
                        TTRCore.getInstance().getTeamHandler().addPlayerToTeam(p, "Blue");
                    }
                }
            }
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (TTRCore.isAdmin(p)) continue;
            TTRTeam team = TTRCore.getInstance().getTeamHandler().getPlayerTeam(p);
            if (team != null && team.getSpawnPoint() != null) {
                p.teleport(team.getSpawnPoint());
            }
            p.getInventory().clear();
            p.getInventory().setArmorContents(null);
            p.getInventory().setItemInOffHand(null);
            p.setGameMode(GameMode.SURVIVAL);
            p.setHealth(20.0);
            p.setFoodLevel(20);
            for (PotionEffect pe : p.getActivePotionEffects()) {
                p.removePotionEffect(pe.getType());
            }
        }

        Bukkit.broadcastMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "🛡 " +
                TextUtil.toTiny("¡FASE DE PREPARACIÓN!") + " 🛡");
        Bukkit.broadcastMessage(ChatColor.GRAY + TextUtil.toTiny("Tienes ") + ChatColor.WHITE + seconds + "s" +
                ChatColor.GRAY + TextUtil.toTiny(" para inspeccionar los cofres de tu base y coordinar con tu equipo."));
        Bukkit.broadcastMessage(ChatColor.RED + TextUtil.toTiny("Nota: No puedes romper bloques ni agarrar objetos en esta fase."));
        Bukkit.broadcastMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

        if (prepTaskID != -1) {
            Bukkit.getScheduler().cancelTask(prepTaskID);
        }

        prepTaskID = new BukkitRunnable() {
            @Override
            public void run() {
                if (status != MatchStatus.PREPARATION) {
                    this.cancel();
                    return;
                }

                if (prepRemaining <= 0) {
                    this.cancel();
                    startMatch();
                    return;
                }

                String title = TextUtil.color("&#FFFFFF" + TextUtil.toTiny("Preparación: ") + "&#FF5555§l" + prepRemaining + "s");
                String sub = TextUtil.color("&#FFFF55" + TextUtil.toTiny("¡Inspecciona los cofres!"));
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (prepRemaining <= 5 || prepRemaining == 10 || prepRemaining == seconds) {
                        p.sendTitle(title, sub, 0, 25, 5);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, (prepRemaining <= 3) ? 2.0f : 1.2f);
                    }
                    p.sendActionBar(net.kyori.adventure.text.Component.text(title));
                }

                prepRemaining--;
            }
        }.runTaskTimer(TTRCore.getInstance(), 0L, 20L).getTaskId();
    }

    public void startMatch() {
        TTRCore.getInstance().getTeamHandler().loadSpawnsFromConfig();
        TTRCore.getInstance().getTeamHandler().resetMatchStats();

        TTRTeam red = TTRCore.getInstance().getTeamHandler().getTeam("Red");
        TTRTeam blue = TTRCore.getInstance().getTeamHandler().getTeam("Blue");
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (TTRCore.getInstance().getTeamHandler().getPlayerTeam(p) == null) {
                if (red != null && blue != null) {
                    if (red.getPlayers().size() <= blue.getPlayers().size()) {
                        TTRCore.getInstance().getTeamHandler().addPlayerToTeam(p, "Red");
                    } else {
                        TTRCore.getInstance().getTeamHandler().addPlayerToTeam(p, "Blue");
                    }
                }
            }
        }

        this.status = MatchStatus.INGAME;
        this.lootSpawner = new LootSpawner();
        this.checker = new CageChecker();

        // Iniciar rastreo de regeneración de mapa
        if (TTRCore.getInstance().getRollbackManager() != null) {
            TTRCore.getInstance().getRollbackManager().startTracking();
        }

        // Iniciar eventos de caos
        if (TTRCore.getInstance().getEventManager() != null) {
            TTRCore.getInstance().getEventManager().startCycle();
        }

        this.remainingTime = TTRCore.getInstance().getConfigManager().getMatchDuration();
        this.maxPointsToWin = TTRCore.getInstance().getConfigManager().getMaxPoints();

        if (this.gameBar != null) this.gameBar.removeAll();
        this.gameBar = Bukkit.createBossBar(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + TextUtil.toTiny("DESTINY TOWERS"), BarColor.PURPLE, BarStyle.SOLID);

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

        if (this.gameBar != null) {
            this.gameBar.addPlayer(player);
        }

        Location teamSpawn = playerTeam.getSpawnPoint();
        if (teamSpawn == null) {
            teamSpawn = TTRCore.getInstance().getConfigManager().getTeamSpawn(playerTeam.getIdentifier());
        }

        if (teamSpawn != null) {
            player.teleport(teamSpawn);
        } else {
            Location lobby = TTRCore.getInstance().getConfigManager().getLobbyLocation();
            if (lobby != null) player.teleport(lobby);
        }

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setItemInOffHand(null);
        player.setGameMode(GameMode.SURVIVAL);

        if (player.getAttribute(Attribute.MAX_HEALTH) != null) {
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20.0);
        }
        player.setHealth(20);
        player.setFoodLevel(20);

        equipPlayer(player, playerTeam.getIdentifier());
        this.kills.putIfAbsent(player, 0);

        String title = ChatColor.GOLD + "" + ChatColor.BOLD + TextUtil.toTiny("DESTINY TOWERS");
        String sub = ChatColor.YELLOW + TextUtil.toTiny("¡A luchar por la victoria!");
        player.sendTitle(title, sub, 10, 60, 20);
    }

    public void equipPlayer(Player player, String teamIdentifier) {
        TTRTeam team = TTRCore.getInstance().getTeamHandler().getTeam(teamIdentifier);
        ChatColor chatColor = TTRCore.getInstance().getConfigManager().getTeamColor(teamIdentifier);
        Color armorColor = (chatColor == ChatColor.RED) ? Color.RED : Color.BLUE;
        Material glassMaterial = (chatColor == ChatColor.RED) ? Material.RED_STAINED_GLASS : Material.BLUE_STAINED_GLASS;

        ItemStack[] armor = new ItemStack[]{
                new ItemStack(Material.LEATHER_BOOTS), new ItemStack(Material.LEATHER_LEGGINGS),
                new ItemStack(Material.LEATHER_CHESTPLATE), new ItemStack(Material.LEATHER_HELMET)
        };

        int protLevel = (team != null) ? team.getArmorProtectionLevel() : 0;

        for (ItemStack item : armor) {
            LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
            if (meta != null) {
                meta.setColor(armorColor);
                meta.setUnbreakable(true);
                meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
                if (protLevel > 0) {
                    meta.addEnchant(Enchantment.PROTECTION, protLevel, true);
                }
                item.setItemMeta(meta);
            }
        }
        player.getInventory().setArmorContents(armor);

        player.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
        player.getInventory().addItem(new ItemStack(glassMaterial, 32));
        player.getInventory().addItem(new ItemStack(Material.BREAD, 16));

        if (team != null) {
            if (team.hasTeamSpeed()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
            }
            if (team.hasTeamHaste()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, 0, false, false));
            }
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
                if (t != null) {
                    if (t.getPoints() > maxP) {
                        maxP = t.getPoints();
                        winner = t;
                        draw = false;
                    } else if (t.getPoints() == maxP) {
                        draw = true;
                    }
                }
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
                if (team != null) {
                    ChatColor color = TTRCore.getInstance().getConfigManager().getTeamColor(teamName);
                    sb.append(color).append(ChatColor.BOLD).append(TextUtil.toTiny(teamName))
                            .append(": ").append(ChatColor.WHITE).append(team.getPoints()).append("   ");
                    if (team.getPoints() > highestPoints) highestPoints = team.getPoints();
                }
            }
        }
        this.gameBar.setTitle(sb.toString().trim());
        double progress = (maxPointsToWin > 0) ? (double) highestPoints / maxPointsToWin : 0.0;
        if (progress > 1.0) progress = 1.0;
        this.gameBar.setProgress(progress);
    }

    public void cleanup() {
        if (this.prepTaskID != -1) {
            Bukkit.getScheduler().cancelTask(this.prepTaskID);
            this.prepTaskID = -1;
        }

        if (this.lootSpawner != null) this.lootSpawner.stopSpawning();
        if (this.checker != null) this.checker.stopChecking();

        Bukkit.getScheduler().cancelTask(this.regenTaskID);
        Bukkit.getScheduler().cancelTask(this.matchTaskID);

        if (TTRCore.getInstance().getEventManager() != null) {
            TTRCore.getInstance().getEventManager().stopCycle();
            TTRCore.getInstance().getEventManager().stopCurrentEvent();
        }

        TTRCore.getInstance().getScoreboard().stopScoreboardTask();
        if (this.gameBar != null) this.gameBar.removeAll();
    }

    public void endMatch(TTRTeam team) {
        this.status = MatchStatus.ENDED;
        cleanup();

        // 1. REGENERACIÓN AUTOMÁTICA DEL MAPA SIN REINICIAR
        if (TTRCore.getInstance().getRollbackManager() != null) {
            if (TTRCore.getInstance().getConfigManager().isAutoRestoreMap()) {
                int restored = TTRCore.getInstance().getRollbackManager().restoreMap();
                Bukkit.broadcastMessage(TTRPrefix.TTR_GAME + ChatColor.AQUA + 
                        TextUtil.toTiny("Mapa regenerado automáticamente (") + 
                        ChatColor.YELLOW + TextUtil.toTiny(String.valueOf(restored)) + ChatColor.AQUA + TextUtil.toTiny(" bloques restaurados)."));
            }
            TTRCore.getInstance().getRollbackManager().stopTracking();
        }

        TTRCore.getInstance().getTeamHandler().clearTeams();

        ChatColor teamColor = (team != null) ? TTRCore.getInstance().getConfigManager().getTeamColor(team.getIdentifier()) : ChatColor.WHITE;
        String teamName = (team != null) ? team.getIdentifier() : "Empate";

        List<Map.Entry<Player, Integer>> topKillers = kills.entrySet().stream()
                .sorted(Map.Entry.<Player, Integer>comparingByValue().reversed())
                .limit(3)
                .toList();

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            String titleWinner = teamColor + "" + ChatColor.BOLD + TextUtil.toTiny("GANADOR: " + teamName.toUpperCase());
            player.sendTitle(titleWinner, ChatColor.AQUA + TextUtil.toTiny("¡Partida Finalizada!"), 10, 100, 20);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 10, 1);

            player.sendMessage(" ");
            player.sendMessage(centerText(ChatColor.GOLD + "" + ChatColor.BOLD + TextUtil.toTiny("FIN DE LA PARTIDA")));
            player.sendMessage(centerText(ChatColor.GRAY + TextUtil.toTiny("Ganador: ") + teamColor + TextUtil.toTiny(teamName)));

            if (!topKillers.isEmpty()) {
                player.sendMessage(centerText(ChatColor.AQUA + "--- " + TextUtil.toTiny("TOP ASESINOS") + " ---"));
                int i = 1;
                for (Map.Entry<Player, Integer> entry : topKillers) {
                    player.sendMessage(centerText(ChatColor.YELLOW + "#" + TextUtil.toTiny(String.valueOf(i)) + " " + ChatColor.WHITE + TextUtil.toTiny(entry.getKey().getName()) + ": " + ChatColor.RED + TextUtil.toTiny(String.valueOf(entry.getValue()))));
                    i++;
                }
            }

            player.sendMessage(" ");

            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.getInventory().setItemInOffHand(null);
            player.setGameMode(GameMode.ADVENTURE);

            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }

            player.setFireTicks(0);
            player.setFreezeTicks(0);
            if (player.getAttribute(Attribute.MAX_HEALTH) != null) {
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20.0);
            }
            player.setHealth(20);
            player.setFoodLevel(20);

            giveLobbyItems(player);

            Location lobby = TTRCore.getInstance().getConfigManager().getLobbyLocation();
            if (lobby != null) player.teleport(lobby);

            TTRCore.getInstance().getScoreboard().update(player);
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
        int spaces = Math.max(0, (maxWidth - ChatColor.stripColor(text).length()) / 2);
        return " ".repeat(spaces) + text;
    }

    public void giveLobbyItems(Player p) {
        p.getInventory().clear();
        p.getInventory().setArmorContents(null);
        p.getInventory().setItemInOffHand(null);

        ItemStack star = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = star.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "★ " + TextUtil.toTiny("Elegir Equipo") + ChatColor.GRAY + " (" + TextUtil.toTiny("Clic Derecho") + ")");
            star.setItemMeta(meta);
        }
        p.getInventory().setItem(4, star);

        if (p.hasPermission("ttr.admin")) {
            ItemStack config = new ItemStack(Material.COMPARATOR);
            ItemMeta cMeta = config.getItemMeta();
            if (cMeta != null) {
                cMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "⚙ " + TextUtil.toTiny("Configuración") + ChatColor.GRAY + " (" + TextUtil.toTiny("Clic Derecho") + ")");
                config.setItemMeta(cMeta);
            }
            p.getInventory().setItem(0, config);
        }
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
