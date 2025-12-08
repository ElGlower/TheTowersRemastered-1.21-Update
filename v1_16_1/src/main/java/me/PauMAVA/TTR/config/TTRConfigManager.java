/*
 * TheTowersRemastered (TTR)
 * Copyright (c) 2019-2021  Pau Machetti Vallverdú
 * [Licencia...]
 */

package me.PauMAVA.TTR.config;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.teams.TTRTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.*;

public class TTRConfigManager {

    private FileConfiguration configuration;
    private World world;
    private ConfigurationSection teamsSection;
    private ConfigurationSection matchSection;
    private ConfigurationSection mapSection;
    private ConfigurationSection autoStartSection;

    public TTRConfigManager(FileConfiguration configuration) {
        this.configuration = configuration;
        if (!TTRCore.getInstance().getServer().getWorlds().isEmpty()) {
            this.world = TTRCore.getInstance().getServer().getWorlds().get(0);
        }

        if (!new File(TTRCore.getInstance().getDataFolder() + "/config.yml").exists()) {
            setUpFile();
            saveConfig();
        } else {
            this.teamsSection = this.configuration.getConfigurationSection("teams");
            this.mapSection = this.configuration.getConfigurationSection("map");
            this.matchSection = this.configuration.getConfigurationSection("match");
            this.autoStartSection = this.configuration.getConfigurationSection("autostart");
        }
    }

    public int getMaxPoints() {
        return this.matchSection.getInt("maxpoints");
    }

    public int getMaxHealth() {
        return this.matchSection.getInt("maxhealth");
    }

    public int getTime() {
        return this.matchSection.getInt("time");
    }

    public String getWeather() {
        return this.matchSection.getString("weather");
    }

    public Location getLobbyLocation() {
        if (this.mapSection.isConfigurationSection("lobby")) {
            return configToLocation(this.mapSection.getConfigurationSection("lobby"));
        }
        return this.mapSection.getLocation("lobby");
    }

    public List<Location> getIronSpawns() {
        List<?> list = this.mapSection.getList("ironspawns");
        if (list == null || list.isEmpty()) return new ArrayList<>();

        try {
            return (List<Location>) list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<Location> getXPSpawns() {
        List<?> list = this.mapSection.getList("xpspawns");
        if (list == null || list.isEmpty()) return new ArrayList<>();
        try {
            return (List<Location>) list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public int getTeamCount() {
        if (this.teamsSection == null) return 0;
        return this.teamsSection.getKeys(false).size();
    }

    public Set<String> getTeamNames() {
        if (this.teamsSection == null) return new HashSet<>();
        return this.teamsSection.getKeys(false);
    }

    private ConfigurationSection getTeam(String teamName) {
        if (this.teamsSection == null) return null;
        for (String key : this.teamsSection.getKeys(false)) {
            if (key.equalsIgnoreCase(teamName)) {
                return this.teamsSection.getConfigurationSection(key);
            }
        }
        return null;
    }

    public ChatColor getTeamColor(String teamName) {
        ConfigurationSection section = getTeam(teamName);
        if (section != null) {
            try {
                return ChatColor.valueOf(section.getString("color"));
            } catch (Exception e) {
                return ChatColor.WHITE;
            }
        }
        return ChatColor.WHITE;
    }

    public Location getTeamSpawn(String teamName) {
        ConfigurationSection teamSec = getTeam(teamName);
        if (teamSec != null && teamSec.isConfigurationSection("spawn")) {
            return configToLocation(teamSec.getConfigurationSection("spawn"));
        }
        return null;
    }

    public Location getTeamCage(String teamName) {
        ConfigurationSection teamSec = getTeam(teamName);
        if (teamSec != null && teamSec.isConfigurationSection("cage")) {
            return configToLocation(teamSec.getConfigurationSection("cage"));
        }
        return null;
    }

    public HashMap<Location, TTRTeam> getTeamCages() {
        HashMap<Location, TTRTeam> cages = new HashMap<Location, TTRTeam>();
        for (String teamName : getTeamNames()) {
            Location loc = getTeamCage(teamName);
            if (loc != null) {
                cages.put(loc, TTRCore.getInstance().getTeamHandler().getTeam(teamName));
            }
        }
        return cages;
    }

    public boolean isEnabled() {
        return this.configuration.getBoolean("enable_on_start");
    }

    public void setEnableOnStart(boolean value) {
        this.configuration.set("enable_on_start", value);
        saveConfig();
    }

    public String getLocale() {
        return this.configuration.getString("locale", "en");
    }

    private void saveConfig() {
        TTRCore.getInstance().saveConfig();
    }

    public void resetFile() {
        setUpFile();
    }


    private Location configToLocation(ConfigurationSection section) {
        if (section == null) return null;

        String worldName = section.getString("world");
        if (worldName == null) return null;

        World w = Bukkit.getWorld(worldName);
        if (w == null) {
            w = Bukkit.getWorlds().get(0);
        }

        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw = (float) section.getDouble("yaw");
        float pitch = (float) section.getDouble("pitch");

        return new Location(w, x, y, z, yaw, pitch);
    }

    private void setUpFile() {
        this.configuration.addDefault("enable_on_start", false);
        this.configuration.addDefault("locale", "en");

        this.autoStartSection = this.configuration.createSection("autostart");
        autoStartSection.addDefault("enabled", true);
        autoStartSection.addDefault("count", 4);

        this.matchSection = this.configuration.createSection("match");
        matchSection.addDefault("time", 10000);
        matchSection.addDefault("weather", "CLEAR");
        matchSection.addDefault("maxpoints", 10);
        matchSection.addDefault("maxhealth", 20);

        this.mapSection = this.configuration.createSection("map");
        // Nota: Para la creación por defecto usamos objetos Location, pero el lector manual
        // los leerá bien de todas formas cuando se guardan.
        if (this.world != null) {
            mapSection.addDefault("lobby", new Location(this.world, 0, 207, 1014));
        }

        this.configuration.options().copyDefaults(true);
    }
}