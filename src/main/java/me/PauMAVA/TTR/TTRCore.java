package me.PauMAVA.TTR;

import me.PauMAVA.TTR.commands.*;
import me.PauMAVA.TTR.config.TTRConfigManager;
import me.PauMAVA.TTR.modes.AuctionDraftManager;
import me.PauMAVA.TTR.modes.LeaderVoteManager;
import me.PauMAVA.TTR.modes.TeamSelectionMode;
import me.PauMAVA.TTR.lang.LanguageManager;
import me.PauMAVA.TTR.listeners.GameJoinListener;
import me.PauMAVA.TTR.listeners.TeamCombatListener;
import me.PauMAVA.TTR.listeners.TeamSelectListener;
import me.PauMAVA.TTR.match.AutoStarter;
import me.PauMAVA.TTR.match.GameEventManager;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.match.TTRMatch;
import me.PauMAVA.TTR.rollback.RollbackManager;
import me.PauMAVA.TTR.teams.TTRTeamHandler;
import me.PauMAVA.TTR.ui.BeaconShopListener;
import me.PauMAVA.TTR.ui.ConfigGUIListener;
import me.PauMAVA.TTR.ui.ScoreboardHandler;
import me.PauMAVA.TTR.ui.TTRCustomTab;
import me.PauMAVA.TTR.util.EventListener;
import me.PauMAVA.TTR.util.TextUtil;
import me.PauMAVA.TTR.util.TTRPrefix;
import me.PauMAVA.TTR.util.TTRTabCompleter;
import me.PauMAVA.TTR.world.TTRWorldHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class TTRCore extends JavaPlugin {

    private static TTRCore instance;
    private TTRTeamHandler teamHandler;
    private TTRConfigManager configManager;
    private LanguageManager languageManager;
    private TTRMatch currentMatch;
    private TTRWorldHandler worldHandler;
    private ScoreboardHandler scoreboard;
    private GameEventManager eventManager;
    private AutoStarter autoStarter;
    private RollbackManager rollbackManager;
    private me.PauMAVA.TTR.rollback.WorldBackupManager worldBackupManager;

    private LeaderVoteManager leaderVoteManager;
    private AuctionDraftManager auctionDraftManager;
    private me.PauMAVA.TTR.modes.ModeAnnouncementManager modeAnnouncementManager;
    private me.PauMAVA.TTR.modes.TeamSelectionMode currentSelectionMode = me.PauMAVA.TTR.modes.TeamSelectionMode.STANDARD;
    private boolean beaconShopEnabled = true;

    private boolean counting = false;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.configManager = new TTRConfigManager(getConfig());
        this.teamHandler = new TTRTeamHandler();
        this.languageManager = new LanguageManager(this);
        this.eventManager = new GameEventManager();
        this.autoStarter = new AutoStarter(this, getConfig());
        this.rollbackManager = new RollbackManager(this);
        this.worldBackupManager = new me.PauMAVA.TTR.rollback.WorldBackupManager(this);
        this.leaderVoteManager = new LeaderVoteManager();
        this.auctionDraftManager = new AuctionDraftManager();
        this.modeAnnouncementManager = new me.PauMAVA.TTR.modes.ModeAnnouncementManager();

        this.beaconShopEnabled = getConfig().getBoolean("beacon_shop.enabled", true);
        boolean eventsEnabled = getConfig().getBoolean("events.enabled", true);
        this.eventManager.toggleAutoMode(eventsEnabled);

        String modeStr = getConfig().getString("selection_mode", "STANDARD");
        try {
            this.currentSelectionMode = me.PauMAVA.TTR.modes.TeamSelectionMode.valueOf(modeStr.toUpperCase());
        } catch (Exception e) {
            this.currentSelectionMode = me.PauMAVA.TTR.modes.TeamSelectionMode.STANDARD;
        }

        this.teamHandler.setUpDefaultTeams();
        if (this.configManager.getLobbyLocation() != null) {
            org.bukkit.World w = this.configManager.getLobbyLocation().getWorld();
            if (w != null && !this.worldBackupManager.hasBackup(w.getName())) {
                this.worldBackupManager.createBackup(w);
            }
            this.worldHandler = new TTRWorldHandler(this, w);
            this.worldHandler.setUpWorld();
        }

        registerCommands();

        // Listeners
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
        getServer().getPluginManager().registerEvents(new BeaconShopListener(), this);
        getServer().getPluginManager().registerEvents(new ConfigGUIListener(), this);
        getServer().getPluginManager().registerEvents(new GameJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new TeamCombatListener(), this);
        getServer().getPluginManager().registerEvents(new TeamSelectListener(this), this);
        getServer().getPluginManager().registerEvents(new me.PauMAVA.TTR.ui.ModesGUIListener(), this);

        // Simple Voice Chat API Integration
        me.PauMAVA.TTR.voice.VoiceChatManager.getInstance().init();

        if (getConfig().getBoolean("enable_on_start", true)) {
            this.currentMatch = new TTRMatch(MatchStatus.LOBBY);
        } else {
            this.currentMatch = new TTRMatch(MatchStatus.STOPPED);
        }

        this.scoreboard = new ScoreboardHandler(this);
        new TTRCustomTab(this).runTaskTimer(this, 0L, 20L);

        Bukkit.getConsoleSender().sendMessage(TTRPrefix.TTR_SUCCESS + 
                ChatColor.GREEN + TextUtil.toTiny("Destiny Towers 26.3 / 26.2 habilitado correctamente!"));
    }

    @Override
    public void onDisable() {
        if (this.currentMatch != null) this.currentMatch.cleanup();
        if (this.rollbackManager != null) this.rollbackManager.stopTracking();
        if (this.worldHandler != null) this.worldHandler.restoreDifficulty();
        Bukkit.getConsoleSender().sendMessage(TTRPrefix.TTR_ERROR + 
                ChatColor.RED + TextUtil.toTiny("Destiny Towers deshabilitado."));
    }

    private void registerCommands() {
        // Comando Maestro
        MainCommand mainCmd = new MainCommand();
        registerCmd("destinytowers", mainCmd);
        registerCmd("dt", mainCmd);
        registerCmd("ttr", mainCmd);
        registerCmd("bid", new me.PauMAVA.TTR.commands.BidCommand());
        registerCmd("stats", new me.PauMAVA.TTR.commands.StatsCommand());

        // Comandos de partida y control
        registerCmd("ttrstart", new StartCommand());
        registerCmd("ttrstop", new StopCommand());
        registerCmd("ttrresetmap", new ResetMapCommand());
        registerCmd("ttrenable", new EnableDisableCommand(this));
        registerCmd("ttrdisable", new EnableDisableCommand(this));
        registerCmd("ttrset", new SetupCommand());
        registerCmd("ttrrevive", new ReviveCommand());
        registerCmd("ttrforcejoin", new ForceJoinCommand());
        registerCmd("ttrconfig", new ConfigCommand());
        registerCmd("ttrevent", new EventCommand());
        registerCmd("ttrspectate", new SpectateCommand());
        registerCmd("ttrplay", new JoinCommand());

        // Comando /setlobby directamente soportado
        PluginCommand setLobbyCmd = getCommand("setlobby");
        if (setLobbyCmd != null) {
            setLobbyCmd.setExecutor(new CommandExecutor() {
                @Override
                public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(TTRPrefix.TTR_ERROR + "Solo jugadores.");
                        return true;
                    }
                    Player player = (Player) sender;
                    if (!player.hasPermission("destinytowers.admin") && !player.hasPermission("ttr.admin")) {
                        player.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("No tienes permiso."));
                        return true;
                    }
                    Location loc = player.getLocation();
                    configManager.setLobby(loc);
                    if (worldHandler == null && loc.getWorld() != null) {
                        worldHandler = new TTRWorldHandler(TTRCore.this, loc.getWorld());
                        worldHandler.setUpWorld();
                    }
                    player.sendMessage(TTRPrefix.TTR_SUCCESS + 
                            TextUtil.toTiny("¡Lobby establecido con éxito en tu posición!"));
                    return true;
                }
            });
        }

        // TabCompleter inteligente para todos los comandos
        TTRTabCompleter completer = new TTRTabCompleter();
        for (String cmd : getDescription().getCommands().keySet()) {
            PluginCommand pc = getCommand(cmd);
            if (pc != null) {
                pc.setTabCompleter(completer);
            }
        }
    }

    private void registerCmd(String name, CommandExecutor executor) {
        PluginCommand cmd = getCommand(name);
        if (cmd != null) cmd.setExecutor(executor);
    }

    public static TTRCore getInstance() { return instance; }
    public TTRTeamHandler getTeamHandler() { return teamHandler; }
    public TTRConfigManager getConfigManager() { return configManager; }
    public LanguageManager getLanguageManager() { return languageManager; }
    public TTRMatch getCurrentMatch() { return currentMatch; }
    public TTRWorldHandler getWorldHandler() { return worldHandler; }
    public ScoreboardHandler getScoreboard() { return scoreboard; }
    public GameEventManager getEventManager() { return eventManager; }
    public AutoStarter getAutoStarter() { return autoStarter; }
    public RollbackManager getRollbackManager() { return rollbackManager; }
    public me.PauMAVA.TTR.rollback.WorldBackupManager getWorldBackupManager() { return worldBackupManager; }
    public LeaderVoteManager getLeaderVoteManager() { return leaderVoteManager; }
    public AuctionDraftManager getAuctionDraftManager() { return auctionDraftManager; }
    public me.PauMAVA.TTR.modes.ModeAnnouncementManager getModeAnnouncementManager() { return modeAnnouncementManager; }

    public me.PauMAVA.TTR.modes.TeamSelectionMode getCurrentSelectionMode() { return currentSelectionMode; }
    public void setCurrentSelectionMode(me.PauMAVA.TTR.modes.TeamSelectionMode mode) {
        this.currentSelectionMode = mode;
        getConfig().set("selection_mode", mode.name());
        saveConfig();
    }

    public boolean isBeaconShopEnabled() { return beaconShopEnabled; }
    public void setBeaconShopEnabled(boolean enabled) {
        this.beaconShopEnabled = enabled;
        getConfig().set("beacon_shop.enabled", enabled);
        saveConfig();
    }

    public void resetMatchLogic() {
        if (this.currentMatch != null) this.currentMatch.cleanup();
        this.currentMatch = new TTRMatch(MatchStatus.LOBBY);
        if (this.teamHandler != null) this.teamHandler.clearTeams();
        if (this.leaderVoteManager != null) this.leaderVoteManager.cancelVoting();
        if (this.auctionDraftManager != null) this.auctionDraftManager.cancelDraft();
        if (this.autoStarter != null) this.autoStarter.cancelCountdown();

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.getInventory().clear();
            p.getInventory().setArmorContents(null);
            p.getInventory().setItemInOffHand(null);
            for (org.bukkit.potion.PotionEffect pe : p.getActivePotionEffects()) {
                p.removePotionEffect(pe.getType());
            }
            p.setFireTicks(0);
            p.setFreezeTicks(0);
            if (p.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH) != null) {
                p.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).setBaseValue(20.0);
            }
            p.setHealth(20.0);
            p.setFoodLevel(20);
            p.setGameMode(org.bukkit.GameMode.ADVENTURE);
            this.currentMatch.giveLobbyItems(p);

            Location lobby = this.configManager.getLobbyLocation();
            if (lobby != null && lobby.getWorld() != null) {
                // Solo teletransportar si el jugador no está ya en el lobby para evitar doble teletransporte
                if (!p.getWorld().equals(lobby.getWorld()) || p.getLocation().distanceSquared(lobby) > 36.0) {
                    p.teleport(lobby);
                }
            }

            if (this.scoreboard != null) this.scoreboard.update(p);
        }
        if (this.scoreboard != null) {
            this.scoreboard.startScoreboardTask();
            this.scoreboard.updateAll();
        }
    }

    public boolean isCounting() { return counting; }
    public void setCounting(boolean counting) { this.counting = counting; }
    public boolean enabled() { return isEnabled(); }

    public static boolean isAdmin(Player player) {
        if (player == null) return false;
        return player.isOp() || player.hasPermission("destinytowers.admin") || player.hasPermission("ttr.admin");
    }

    public static boolean isAdmin(UUID uuid) {
        if (uuid == null) return false;
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) return isAdmin(p);
        OfflinePlayer off = Bukkit.getOfflinePlayer(uuid);
        return off.isOp();
    }
}
