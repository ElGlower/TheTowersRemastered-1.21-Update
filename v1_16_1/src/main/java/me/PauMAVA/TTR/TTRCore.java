/*
 * TheTowersRemastered (TTR)
 * Copyright (c) 2019-2021  Pau Machetti Vallverdú
 * [Licencia...]
 */
package me.PauMAVA.TTR;

import me.PauMAVA.TTR.commands.*;
import me.PauMAVA.TTR.config.TTRConfigManager;
import me.PauMAVA.TTR.lang.LanguageManager;
import me.PauMAVA.TTR.lang.PluginString;
import me.PauMAVA.TTR.match.AutoStarter;
import me.PauMAVA.TTR.match.GameEventManager; // <--- IMPORTANTE
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.match.TTRMatch;
import me.PauMAVA.TTR.teams.TTRTeamHandler;
import me.PauMAVA.TTR.ui.TeamSelector;
import me.PauMAVA.TTR.ui.TTRCustomTab;
import me.PauMAVA.TTR.ui.TTRScoreboard;
import me.PauMAVA.TTR.util.EventListener;
import me.PauMAVA.TTR.util.PacketInterceptor;
import me.PauMAVA.TTR.world.TTRWorldHandler;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TTRCore extends JavaPlugin {

    private static TTRCore instance;
    private boolean enabled = false;
    private TTRMatch match;
    private TTRTeamHandler teamHandler;
    private TTRConfigManager configManager;
    private TTRWorldHandler worldHandler;
    private TTRCustomTab customTab;
    private TTRScoreboard scoreboard;
    private AutoStarter autoStarter;
    private LanguageManager languageManager;
    private PacketInterceptor packetInterceptor;
    private boolean isCounting = false;

    // --- NUEVA VARIABLE ---
    private GameEventManager eventManager;

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        this.configManager = new TTRConfigManager(this.getConfig());
        this.languageManager = new LanguageManager(this);

        if (this.getConfig().getBoolean("enable_on_start")) {
            enabled = true;
        } else {
            getLogger().warning("" + PluginString.DISABLED_ON_STARTUP_NOTICE);
        }

        this.packetInterceptor = new PacketInterceptor(this);
        for (Player player: this.getServer().getOnlinePlayers()) {
            this.packetInterceptor.addPlayer(player);
        }

        if (enabled) {
            String worldName = this.getConfig().getString("map.lobby.world");
            World gameWorld = loadGameWorld(worldName);

            this.customTab = new TTRCustomTab(this);
            this.scoreboard = new TTRScoreboard();
            this.match = new TTRMatch(MatchStatus.PREGAME);
            this.customTab.runTaskTimer(this, 0L, 20L);
            this.teamHandler = new TTRTeamHandler();
            this.teamHandler.setUpDefaultTeams();

            // --- INICIALIZAR EL GESTOR DE EVENTOS ---
            this.eventManager = new GameEventManager();

            if (gameWorld != null) {
                this.worldHandler = new TTRWorldHandler(this, gameWorld);
                this.worldHandler.setUpWorld();
            } else {
                if (!this.getServer().getWorlds().isEmpty()) {
                    this.worldHandler = new TTRWorldHandler(this, this.getServer().getWorlds().get(0));
                }
            }

            this.getServer().getPluginManager().registerEvents(new EventListener(this), this);
            this.getServer().getPluginManager().registerEvents(new TeamSelector(), this);

            // --- REGISTRAR TIENDA DE FARO ---
            this.getServer().getPluginManager().registerEvents(new me.PauMAVA.TTR.ui.BeaconShop(), this);

        } else {
            this.match = new TTRMatch(MatchStatus.DISABLED);
        }

        // --- REGISTRO DE COMANDOS ---
        this.getCommand("ttrstart").setExecutor(new StartMatchCommand());
        this.getCommand("ttrset").setExecutor(new SetupCommand());
        this.getCommand("ttrstop").setExecutor(new StopMatchCommand());
        this.getCommand("ttrrevive").setExecutor(new ReviveCommand());
        this.getCommand("ttrforcejoin").setExecutor(new ForceJoinCommand());
        this.getCommand("ttrconfig").setExecutor(new ConfigCommand());
        this.getCommand("ttrenable").setExecutor(new EnableDisableCommand(this));
        this.getCommand("ttrdisable").setExecutor(new EnableDisableCommand(this));

        // --- REGISTRAR COMANDO DE EVENTOS ---
        this.getCommand("ttrevent").setExecutor(new EventCommand());

        this.autoStarter = new AutoStarter(this, this.getConfig());
    }

    private World loadGameWorld(String worldName) {
        if (worldName == null) return null;
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            try {
                world = Bukkit.createWorld(new WorldCreator(worldName));
            } catch (Exception e) { e.printStackTrace(); }
        }
        return world;
    }

    public void resetMatchLogic() {
        if (this.match != null) {
            this.match.cleanup(); // Usamos cleanup para evitar bucles infinitos
        }

        this.match = new TTRMatch(MatchStatus.PREGAME);
        this.teamHandler = new TTRTeamHandler();
        this.teamHandler.setUpDefaultTeams();

        org.bukkit.Location lobby = this.configManager.getLobbyLocation();
        for (Player p : this.getServer().getOnlinePlayers()) {
            p.getInventory().clear();
            p.setGameMode(org.bukkit.GameMode.ADVENTURE);
            p.setHealth(20);
            p.setFoodLevel(20);

            org.bukkit.inventory.ItemStack selector = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHER_STAR);
            org.bukkit.inventory.meta.ItemMeta meta = selector.getItemMeta();
            meta.setDisplayName(org.bukkit.ChatColor.GREEN + "Elegir Equipo");
            selector.setItemMeta(meta);
            p.getInventory().setItem(4, selector);

            if (lobby != null) p.teleport(lobby);
            this.autoStarter.addPlayerToQueue(p);
        }
    }

    @Override
    public void onDisable() {
        try {
            if (this.customTab != null) this.customTab.cancel();
            if (this.scoreboard != null) this.scoreboard.removeScoreboard();
            if (this.packetInterceptor != null) {
                for (Player player: this.getServer().getOnlinePlayers()) {
                    this.packetInterceptor.removePlayer(player);
                }
            }
        } catch (Exception ignored) {}
    }

    public static TTRCore getInstance() { return instance; }
    public boolean enabled() { return this.enabled; }
    public TTRMatch getCurrentMatch() { return this.match; }
    public TTRTeamHandler getTeamHandler() { return this.teamHandler; }
    public TTRConfigManager getConfigManager() { return this.configManager; }
    public TTRWorldHandler getWorldHandler() { return worldHandler; }
    public TTRScoreboard getScoreboard() { return scoreboard; }
    public boolean isCounting() { return isCounting; }
    public void setCounting(boolean counting) { isCounting = counting; }
    public AutoStarter getAutoStarter() { return autoStarter; }
    public LanguageManager getLanguageManager() { return languageManager; }
    public PacketInterceptor getPacketInterceptor() { return packetInterceptor; }

    // --- GETTER NECESARIO PARA QUE NO DE ERROR ---
    public GameEventManager getEventManager() { return this.eventManager; }
}