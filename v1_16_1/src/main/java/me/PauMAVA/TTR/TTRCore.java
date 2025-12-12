package me.PauMAVA.TTR;

import me.PauMAVA.TTR.commands.*;
import me.PauMAVA.TTR.config.TTRConfigManager;
import me.PauMAVA.TTR.listeners.GameJoinListener;
import me.PauMAVA.TTR.listeners.TeamCombatListener;
import me.PauMAVA.TTR.teams.TTRTeamHandler;
import me.PauMAVA.TTR.match.GameEventManager;
import me.PauMAVA.TTR.lang.LanguageManager;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.match.TTRMatch;
import me.PauMAVA.TTR.ui.BeaconShopListener;
import me.PauMAVA.TTR.ui.ScoreboardHandler;
import me.PauMAVA.TTR.util.EventListener;
import me.PauMAVA.TTR.util.TTRTabCompleter;
import me.PauMAVA.TTR.world.TTRWorldHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TTRCore extends JavaPlugin {

    private static TTRCore instance;
    private TTRTeamHandler teamHandler;
    private TTRConfigManager configManager;
    private LanguageManager languageManager;
    private TTRMatch currentMatch;
    private TTRWorldHandler worldHandler;
    private ScoreboardHandler scoreboard;
    private GameEventManager eventManager;

    private boolean counting = false;

    @Override
    public void onEnable() {
        instance = this;
        // Guardar config default
        saveDefaultConfig();

        this.configManager = new TTRConfigManager(getConfig());
        this.teamHandler = new TTRTeamHandler();
        this.languageManager = new LanguageManager(this);
        this.eventManager = new GameEventManager();

        this.teamHandler.setUpDefaultTeams();
        if (this.configManager.getLobbyLocation() != null) {
            this.worldHandler = new TTRWorldHandler(this, this.configManager.getLobbyLocation().getWorld());
            this.worldHandler.setUpWorld();
        }

        registerCommands();

        // --- REGISTRO DE EVENTOS (LISTENERS) ---
        // Listener General (Existente)
        getServer().getPluginManager().registerEvents(new EventListener(this), this);

        // [NUEVO] Tienda del Faro
        getServer().getPluginManager().registerEvents(new BeaconShopListener(), this);
        getServer().getPluginManager().registerEvents(new GameJoinListener(this), this); // Pasamos 'this' (TTRCore) como Main
        getServer().getPluginManager().registerEvents(new TeamCombatListener(), this);

        getServer().getPluginManager().registerEvents(new me.PauMAVA.TTR.listeners.TeamSelectListener(this), this);
        if (getConfig().getBoolean("enable_on_start")) {
            this.currentMatch = new TTRMatch(MatchStatus.LOBBY);
        } else {
            this.currentMatch = new TTRMatch(MatchStatus.STOPPED);
        }

        this.scoreboard = new ScoreboardHandler(this);
        new me.PauMAVA.TTR.ui.TTRCustomTab(this).runTaskTimer(this, 0L, 20L);

        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "The Towers Remastered (1.21) Habilitado Correctamente!");
    }

    @Override
    public void onDisable() {
        if (this.currentMatch != null) this.currentMatch.cleanup();
        if (this.worldHandler != null) this.worldHandler.restoreDifficulty();
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "The Towers Remastered Deshabilitado.");
    }

    private void registerCommands() {
        registerCmd("ttrstart", new StartCommand());
        registerCmd("ttrenable", new EnableDisableCommand(this));
        registerCmd("ttrdisable", new EnableDisableCommand(this));
        registerCmd("ttrset", new SetupCommand());
        registerCmd("ttrstop", new StopCommand());
        registerCmd("ttrrevive", new ReviveCommand());
        registerCmd("ttrforcejoin", new ForceJoinCommand());
        registerCmd("ttrconfig", new ConfigCommand());
        registerCmd("ttrevent", new EventCommand());
        registerCmd("ttrspectate", new SpectateCommand());
        registerCmd("ttrplay", new JoinCommand());

        // [NUEVO] Comando /setlobby implementado aquí mismo para ahorrar archivos
        PluginCommand setLobbyCmd = getCommand("setlobby");
        if (setLobbyCmd != null) {
            setLobbyCmd.setExecutor(new CommandExecutor() {
                @Override
                public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("Solo jugadores.");
                        return true;
                    }
                    Player player = (Player) sender;
                    if (!player.hasPermission("ttr.admin")) {
                        player.sendMessage(ChatColor.RED + "No tienes permiso.");
                        return true;
                    }
                    // Guardar ubicación en la config raíz
                    getConfig().set("lobby", player.getLocation());
                    saveConfig();

                    // Si usas el ConfigManager, recargarlo podría ser útil, pero esto basta por ahora
                    player.sendMessage(ChatColor.GREEN + "¡Lobby establecido! " + ChatColor.GRAY + "Los jugadores aparecerán aquí al entrar.");
                    return true;
                }
            });
        }

        // Auto-TabCompleter para los comandos registrados en plugin.yml
        for (String cmd : getDescription().getCommands().keySet()) {
            PluginCommand pc = getCommand(cmd);
            if (pc != null && pc.getTabCompleter() == null) { // Solo si no tiene uno ya
                pc.setTabCompleter(new TTRTabCompleter());
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

    public void resetMatchLogic() {
        if (this.currentMatch != null) this.currentMatch.cleanup();
        this.currentMatch = new TTRMatch(MatchStatus.LOBBY);
        this.scoreboard.updateAll();
    }

    public boolean isCounting() { return counting; }
    public void setCounting(boolean counting) { this.counting = counting; }
    public boolean enabled() { return isEnabled(); }
}