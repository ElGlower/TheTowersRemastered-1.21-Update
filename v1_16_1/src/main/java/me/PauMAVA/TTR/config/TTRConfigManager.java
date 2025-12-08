package me.PauMAVA.TTR.config;

import me.PauMAVA.TTR.TTRCore;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TTRConfigManager {

    private final FileConfiguration config;

    public TTRConfigManager(FileConfiguration config) {
        this.config = config;
    }

    public void setEnableOnStart(boolean enable) {
        config.set("enable_on_start", enable);
        TTRCore.getInstance().saveConfig();
    }

    public String getLocale() {
        return config.getString("locale", "es_ES");
    }

    public Location getLobbyLocation() {
        return (Location) config.get("map.lobby");
    }

    public void setLobby(Location location) {
        config.set("map.lobby", location);
        TTRCore.getInstance().saveConfig();
    }

    public Set<String> getTeamNames() {
        ConfigurationSection section = config.getConfigurationSection("teams");
        return (section != null) ? section.getKeys(false) : null;
    }

    public ChatColor getTeamColor(String teamIdentifier) {
        String colorName = config.getString("teams." + teamIdentifier + ".color");
        try {
            return ChatColor.valueOf(colorName);
        } catch (Exception e) {
            return ChatColor.WHITE;
        }
    }

    public Location getTeamSpawn(String teamIdentifier) {
        return (Location) config.get("teams." + teamIdentifier + ".spawn");
    }

    public void setTeamSpawn(String teamIdentifier, Location location) {
        config.set("teams." + teamIdentifier + ".spawn", location);
        TTRCore.getInstance().saveConfig();
    }

    public List<Location> getTeamCages(String teamIdentifier) {
        List<?> list = config.getList("cages." + teamIdentifier);
        List<Location> locs = new ArrayList<>();
        if (list != null) {
            for (Object obj : list) {
                if (obj instanceof Location) locs.add((Location) obj);
            }
        }
        return locs;
    }

    public List<Location> getTeamCages() {
        List<Location> all = new ArrayList<>();
        if (getTeamNames() != null) {
            for (String team : getTeamNames()) {
                all.addAll(getTeamCages(team));
            }
        }
        return all;
    }

    public int getMaxPoints() {
        return config.getInt("match.maxpoints", 10);
    }

    // ↓↓↓↓↓ ESTOS SON LOS MÉTODOS QUE TE FALTABAN ↓↓↓↓↓
    public String getWeather() {
        return config.getString("match.weather", "CLEAR");
    }

    public int getMatchTime() {
        return config.getInt("match.time", 6000);
    }
    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    public List<Location> getSpawns(String type) {
        List<?> list = config.getList("spawns." + type);
        List<Location> locs = new ArrayList<>();

        if (list != null) {
            for (Object obj : list) {
                if (obj instanceof Location) {
                    locs.add((Location) obj);
                }
            }
        }
        return locs;
    }

    public void addSpawn(String type, Location location) {
        List<Location> currentList = getSpawns(type);
        if (currentList == null) currentList = new ArrayList<>();
        currentList.add(location);

        config.set("spawns." + type, currentList);
        TTRCore.getInstance().saveConfig();
    }
}