package me.PauMAVA.TTR.config;

import me.PauMAVA.TTR.TTRCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TTRConfigManager {

    private FileConfiguration config;

    public TTRConfigManager(FileConfiguration config) {
        this.config = config;
    }

    public void reload() {
        TTRCore.getInstance().reloadConfig();
        this.config = TTRCore.getInstance().getConfig();
    }

    public String getLocale() {
        return config.getString("locale", "es");
    }

    public void setEnableOnStart(boolean enable) {
        config.set("enable_on_start", enable);
        TTRCore.getInstance().saveConfig();
    }

    public Location getLocationSafe(String path) {
        if (!config.contains(path)) return null;

        // Si ya está serializado como objeto Location directo de Bukkit
        if (config.get(path) instanceof Location) {
            return config.getLocation(path);
        }

        String worldName = config.getString(path + ".world");
        if (worldName == null) return null;

        double x = config.getDouble(path + ".x");
        double y = config.getDouble(path + ".y");
        double z = config.getDouble(path + ".z");
        float yaw = (float) config.getDouble(path + ".yaw", 0.0);
        float pitch = (float) config.getDouble(path + ".pitch", 0.0);

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            try {
                world = Bukkit.createWorld(new WorldCreator(worldName));
            } catch (Exception e) {
                return null;
            }
        }

        if (world == null) return null;
        return new Location(world, x, y, z, yaw, pitch);
    }

    public Location getLobbyLocation() {
        Location loc = getLocationSafe("map.lobby");
        if (loc == null && !Bukkit.getWorlds().isEmpty()) {
            return Bukkit.getWorlds().get(0).getSpawnLocation();
        }
        return loc;
    }

    public void setLobby(Location location) {
        saveLocation("map.lobby", location);
    }

    public Set<String> getTeamNames() {
        ConfigurationSection section = config.getConfigurationSection("teams");
        return (section != null) ? section.getKeys(false) : Set.of("Red", "Blue");
    }

    public ChatColor getTeamColor(String teamIdentifier) {
        String colorName = config.getString("teams." + teamIdentifier + ".color");
        try {
            return ChatColor.valueOf(colorName);
        } catch (Exception e) {
            return teamIdentifier.equalsIgnoreCase("Red") ? ChatColor.RED : ChatColor.BLUE;
        }
    }

    public Location getTeamSpawn(String teamIdentifier) {
        return getLocationSafe("teams." + teamIdentifier + ".spawn");
    }

    public void setTeamSpawn(String teamIdentifier, Location location) {
        saveLocation("teams." + teamIdentifier + ".spawn", location);
    }

    public List<Location> getTeamCages(String teamIdentifier) {
        List<Location> cages = new ArrayList<>();
        Location singleCage = getLocationSafe("teams." + teamIdentifier + ".cage");
        if (singleCage != null) cages.add(singleCage);
        return cages;
    }

    public void setTeamCage(String teamIdentifier, Location location) {
        saveLocation("teams." + teamIdentifier + ".cage", location);
    }

    public List<Location> getTeamCages() {
        List<Location> all = new ArrayList<>();
        Set<String> names = getTeamNames();
        if (names != null) {
            for (String team : names) {
                all.addAll(getTeamCages(team));
            }
        }
        return all;
    }

    public List<Location> getSpawns(String type) {
        List<?> rawList = config.getList("spawns." + type);
        List<Location> locations = new ArrayList<>();

        if (rawList != null) {
            for (Object obj : rawList) {
                if (obj instanceof Location) {
                    locations.add((Location) obj);
                } else if (obj instanceof ConfigurationSection) {
                    ConfigurationSection sec = (ConfigurationSection) obj;
                    String wName = sec.getString("world");
                    World w = Bukkit.getWorld(wName);
                    if (w == null && wName != null) w = Bukkit.createWorld(new WorldCreator(wName));
                    if (w != null) {
                        locations.add(new Location(w, sec.getDouble("x"), sec.getDouble("y"), sec.getDouble("z")));
                    }
                }
            }
        }
        return locations;
    }

    public void addSpawn(String type, Location loc) {
        List<Location> list = getSpawns(type);
        list.add(loc);
        config.set("spawns." + type, list);
        TTRCore.getInstance().saveConfig();
    }

    private void saveLocation(String path, Location loc) {
        config.set(path + ".world", loc.getWorld().getName());
        config.set(path + ".x", loc.getX());
        config.set(path + ".y", loc.getY());
        config.set(path + ".z", loc.getZ());
        config.set(path + ".yaw", loc.getYaw());
        config.set(path + ".pitch", loc.getPitch());
        TTRCore.getInstance().saveConfig();
    }

    public int getMaxPoints() {
        return config.getInt("match.maxpoints", 10);
    }

    public int getMatchDuration() {
        return config.getInt("match.duration", 1200);
    }

    public int getMatchTime() {
        return config.getInt("match.time", 6000);
    }

    public String getWeather() {
        return config.getString("match.weather", "CLEAR");
    }

    public boolean isAutoStartEnabled() {
        return config.getBoolean("autostart.enabled", true);
    }

    public int getAutoStartPlayers() {
        return config.getInt("autostart.count", 4);
    }

    public int getAutoStartCountdown() {
        return config.getInt("autostart.countdown", 10);
    }

    public boolean isAutoRestoreMap() {
        return config.getBoolean("rollback.auto_restore_on_end", true);
    }
}
